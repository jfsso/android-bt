package li.vin.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

/*package*/ class BtLeDeviceConnection extends BluetoothGattCallback implements DeviceConnection {
  private static final String TAG = BtLeDeviceConnection.class.getSimpleName();

  private final PublishSubject<ConnectionStateChangeMsg> connectionStateObservable = PublishSubject.create();
  private final PublishSubject<ServiceMsg> serviceObservable = PublishSubject.create();
  private final PublishSubject<BluetoothGattCharacteristic> characteristicReadObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicChangeMsg> characteristicChangedObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicWriteMsg> characteristicWriteObservable = PublishSubject.create();
  private final PublishSubject<DescriptorWriteMsg> descriptorWriteObservable = PublishSubject.create();

  private final PublishSubject<ConnectableObservable<?>> writeQueue = PublishSubject.create();

  private final Map<UUID, Observable<?>> mUuidObservables = new ConcurrentHashMap<>();
  private final Map<Param<?>, Observable<?>> mParamObservables = new IdentityHashMap<>();

  private final Context mContext;
  private final BluetoothDevice mDevice;
  private final String mUnlockKey;

  public BtLeDeviceConnection(@NonNull Context context, @NonNull BluetoothDevice device, @NonNull String unlockKey) {
    mContext = context;
    mDevice = device;
    mUnlockKey = unlockKey;
  }

  @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    Log.i(TAG, String.format("device(%s) onConnectionStateChange status(%s) newState(%s)",
      gatt.getDevice(), Utils.gattStatus(status), Utils.btState(newState)));

    this.connectionStateObservable.onNext(new ConnectionStateChangeMsg(gatt, status, newState));
  }

  @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    Log.i(TAG, String.format("device(%s) onServicesDiscovered status(%s)", gatt.getDevice(),
      Utils.gattStatus(status)));

    this.serviceObservable.onNext(new ServiceMsg(gatt, status));
  }

  @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, String.format("device(%s) onCharacteristicRead characteristic(%s) status(%s)",
      gatt.getDevice(), characteristic.getUuid(), Utils.gattStatus(status)));

    this.characteristicReadObservable.onNext(characteristic);
//    this.characteristicChangedObservable.onNext(new CharacteristicChangeMsg(gatt, characteristic));
  }

  @Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//    Log.i(TAG, String.format("device(%s) onCharacteristicChanged characteristic(%s)",
//      gatt.getDevice(), characteristic.getUuid()));
    this.characteristicChangedObservable.onNext(new CharacteristicChangeMsg(gatt, characteristic));
  }

  @Override public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, String.format("device(%s) onCharacteristicWrite characteristic(%s) status(%s)",
      gatt.getDevice(), characteristic, Utils.gattStatus(status)));

    this.characteristicWriteObservable.onNext(new CharacteristicWriteMsg(gatt, characteristic, status));
  }

  @Override public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    Log.i(TAG, String.format("device(%s) onDescriptorWrite descriptor(%s) status(%s)",
      gatt.getDevice(), descriptor.getCharacteristic().getUuid(), Utils.gattStatus(status)));

    this.descriptorWriteObservable.onNext(new DescriptorWriteMsg(gatt, descriptor, status));
  }

  @Override public Observable<Void> resetDtcs() {
    final ConnectableObservable<Void> clearDtcsObservable = connectionObservable
      .flatMap(new Func1<GattService, Observable<Void>>() {
        @Override public Observable<Void> call(final GattService gs) {
          return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override public void call(Subscriber<? super Void> subscriber) {
              final BluetoothGattCharacteristic characteristic = gs.service.getCharacteristic(Uuids.CLEAR_DTCS);
              if (characteristic == null) {
                throw new RuntimeException("bluetooth service is missing the CLEAR_DTCS characteristic");
              }

              if (gs.gatt.writeCharacteristic(characteristic)) {
                subscriber.onNext(null);
                subscriber.onCompleted();
              } else {
                subscriber.onError(new RuntimeException("failed to initiate write to clear DTCs"));
              }
            }
          }).flatMap(new Func1<Void, Observable<Void>>() {
            @Override public Observable<Void> call(Void aVoid) {
              return characteristicWriteObservable
                .filter(new Func1<CharacteristicWriteMsg, Boolean>() {
                  @Override public Boolean call(CharacteristicWriteMsg msg) {
                    return Uuids.CLEAR_DTCS.equals(msg.characteristic.getUuid());
                  }
                })
                .first()
                .map(new Func1<CharacteristicWriteMsg, Void>() {
                  @Override public Void call(CharacteristicWriteMsg characteristicWriteMsg) {
                    return null;
                  }
                });
            }
          });
        }
      })
      .publish();

    writeQueue.onNext(clearDtcsObservable);

    return clearDtcsObservable.asObservable();
  }

  @Override public <T> Observable<T> observe(@NonNull final Param<T> param) {
    if (!(param instanceof ParamImpl)) {
      throw new AssertionError("all Params must be instances of ParamImpl");
    }

    return observe((ParamImpl<T, ?>) param);
  }

  private <T, P> Observable<T> observe(@NonNull final ParamImpl<T, P> param) {
    @SuppressWarnings("unchecked")
    Observable<T> paramObservable = (Observable<T>) mParamObservables.get(param);
    if (paramObservable == null) {
      @SuppressWarnings("unchecked")
      Observable<P> uuidObservable = (Observable<P>) mUuidObservables.get(param.uuid);
      if (uuidObservable == null) {
        final Func1<BluetoothGattCharacteristic, Boolean> matchesUuid = new Func1<BluetoothGattCharacteristic, Boolean>() {
          @Override public Boolean call(BluetoothGattCharacteristic characteristic) {
            return param.uuid.equals(characteristic.getUuid());
          }
        };

        final Func1<BluetoothGattCharacteristic, P> parseCharacteristic = new Func1<BluetoothGattCharacteristic, P>() {
          @Override public P call(BluetoothGattCharacteristic characteristic) {
            return param.parseCharacteristic(characteristic);
          }
        };

        uuidObservable = connectionObservable
          .flatMap(new Func1<GattService, Observable<BluetoothGattCharacteristic>>() {
            @Override public Observable<BluetoothGattCharacteristic> call(final GattService gs) {
              final BluetoothGattCharacteristic characteristic = gs.service.getCharacteristic(param.uuid);
              if (characteristic == null) {
                throw new RuntimeException("no such characteristic: " + param.uuid);
              }

              final ConnectableObservable<BluetoothGattCharacteristic> readObservable = param.shouldRead
                ? makeReadObservable(param, gs.gatt, characteristic)
                : null;

              final ConnectableObservable<DescriptorWriteMsg> notiObservable = param.hasNotifications
                ? makeNotiObservable(param, gs.gatt, characteristic)
                : null;


              if (readObservable == null && notiObservable == null) {
                return Observable.empty();
              }

              if (readObservable != null && notiObservable == null) {
                writeQueue.onNext(readObservable);
                return readObservable;
              }

              final Observable<BluetoothGattCharacteristic> notiValObservable = notiObservable
                .flatMap(getCharacteristicChanges)
                .map(pluckCharacteristic);

              if (readObservable == null) {
                writeQueue.onNext(notiObservable);
                return notiValObservable;
              } else {
                writeQueue.onNext(readObservable);
                writeQueue.onNext(notiObservable);
                return Observable.merge(readObservable, notiValObservable);
              }
            }
          })
          .filter(matchesUuid)
          .map(parseCharacteristic)
          .share();

        uuidObservable.doOnUnsubscribe(new Action0() {
          @Override public void call() {
            Log.d("uuid.doOnUnsubscribe", "unsubscribing " + param.uuid);
            mUuidObservables.remove(param.uuid);
          }
        });

        mUuidObservables.put(param.uuid, uuidObservable);
      }

      final Func1<P, Boolean> matchesParsed = new Func1<P, Boolean>() {
        @Override public Boolean call(P s) {
          return param.matches(s);
        }
      };

      final Func1<P, T> parseVal = new Func1<P, T>() {
        @Override public T call(P s) {
          return param.parseVal(s);
        }
      };

      paramObservable = uuidObservable
        .filter(matchesParsed)
        .map(parseVal)
        .distinctUntilChanged()
        .share();

      paramObservable.doOnUnsubscribe(new Action0() {
        @Override
        public void call() {
          Log.d("param.doOnUnsubscribe", "unsubscribing " + param.uuid);
          mParamObservables.remove(param);
        }
      });

      mParamObservables.put(param, paramObservable);
    }

    return paramObservable;
  }

  private final Func1<Object, Observable<CharacteristicChangeMsg>> getCharacteristicChanges = new Func1<Object, Observable<CharacteristicChangeMsg>>() {
    @Override public Observable<CharacteristicChangeMsg> call(Object o) {
      return characteristicChangedObservable;
    }
  };

  private static final Func1<CharacteristicChangeMsg, BluetoothGattCharacteristic> pluckCharacteristic = new Func1<CharacteristicChangeMsg, BluetoothGattCharacteristic>() {
    @Override public BluetoothGattCharacteristic call(CharacteristicChangeMsg characteristicChangeMsg) {
      return characteristicChangeMsg.characteristic;
    }
  };

  private final Observable.OnSubscribe<Void> startGattOnSubscribe = new Observable.OnSubscribe<Void>()  {
    @Override public void call(Subscriber<? super Void> subscriber) {
      final BluetoothGatt gatt = mDevice.connectGatt(mContext, false, BtLeDeviceConnection.this);
      final Subscription writeQueueSubscription = writeQueue
        .onBackpressureBuffer()
        .subscribe(new WriteQueueConsumer());

      subscriber.add(Subscriptions.create(new Action0() {
        @Override
        public void call() {
          Log.d("GattConnection", "disconnecting from gatt after all unsubscribed");
          writeQueueSubscription.unsubscribe();
          gatt.disconnect();
          gatt.close();
        }
      }));

      subscriber.onNext(null);
      subscriber.onCompleted();
    }
  };

  private static final Observable.Operator<Void, ConnectionStateChangeMsg> waitForConnectionOperator = new Observable.Operator<Void, ConnectionStateChangeMsg>() {
    @Override public Subscriber<? super ConnectionStateChangeMsg> call(final Subscriber<? super Void> subscriber) {
      return new Subscriber<ConnectionStateChangeMsg>() {
        @Override public void onCompleted() {
          subscriber.onCompleted();
        }

        @Override public void onError(Throwable e) {
          subscriber.onError(e);
        }

        @Override public void onNext(ConnectionStateChangeMsg msg) {
//          Log.d("waitForConnectionOperator", "got connection state change msg");
          if (BluetoothProfile.STATE_CONNECTED == msg.newState) {
            msg.gatt.discoverServices();
            subscriber.onNext(null);
          } else if (BluetoothProfile.STATE_DISCONNECTED == msg.newState) {
            subscriber.onCompleted();
          }
        }
      };
    }
  };

  private final Func1<Void, Observable<Void>> waitForConnection = new Func1<Void, Observable<Void>>() {
    @Override public Observable<Void> call(Void aVoid) {
      // leave a subscriber on the gatt observable. not leaving this can lead to a
      // race condition in which the gatt observable thinks all subscribers
      // have unsubscribed because a ConnectableObservable from the write queue
      // has not yet been connected.
      return connectionStateObservable
        .lift(waitForConnectionOperator)
        /*.first()*/;
    }
  };

  private static final Observable.Operator<GattService, ServiceMsg> waitForServiceOperator = new Observable.Operator<GattService, ServiceMsg>() {
    @Override public Subscriber<? super ServiceMsg> call(final Subscriber<? super GattService> subscriber) {
      return new Subscriber<ServiceMsg>() {
        @Override public void onCompleted() {
          subscriber.onCompleted();
        }

        @Override public void onError(Throwable e) {
          subscriber.onError(e);
        }

        @Override public void onNext(ServiceMsg msg) {
          Log.d("waitForServiceOperator", "got service msg");
          if (BluetoothGatt.GATT_SUCCESS == msg.status) {
            final BluetoothGattService service = msg.gatt.getService(Uuids.SERVICE);
            if (service == null) {
              // TODO: better error
              subscriber.onError(new RuntimeException("service not found: " + Uuids.SERVICE));
            } else {
              subscriber.onNext(new GattService(msg.gatt, service));
//              subscriber.onCompleted(); // Do we need to complete here?
            }
          } else {
            subscriber.onError(new RuntimeException("failed to find services")); // TODO: better error
          }
        }
      };
    }
  };

  private final Func1<Void, Observable<GattService>> waitForService = new Func1<Void, Observable<GattService>>() {
    @Override public Observable<GattService> call(Void aVoid) {
      return serviceObservable
        .lift(waitForServiceOperator)
        .first();
    }
  };

  private final Func1<GattService, Observable<GattService>> unlockDevice = new Func1<GattService, Observable<GattService>>() {
    private final Charset ascii = Charset.forName("ASCII");

    @Override public Observable<GattService> call(final GattService gs) {
      final BluetoothGattCharacteristic characteristic = gs.service.getCharacteristic(Uuids.UNLOCK);
      if (characteristic == null) {
        throw new RuntimeException("no such characteristic: " + Uuids.UNLOCK);
      }

      characteristic.setValue(mUnlockKey.getBytes(ascii));
      if (!gs.gatt.writeCharacteristic(characteristic)) {
        throw new RuntimeException("failed to start write to unlock device");
      }

      return characteristicWriteObservable
        .first()
        .map(new Func1<CharacteristicWriteMsg, GattService>() {
          @Override public GattService call(CharacteristicWriteMsg msg) {
            Log.d("unlockService", "got write confirmation");
            if (BluetoothGatt.GATT_SUCCESS == msg.status) {
              return gs;
            } else {
              throw new RuntimeException("failed to unlock service: " + Utils.gattStatus(msg.status));
            }
          }
        });
    }
  };

  private final Observable<GattService> connectionObservable = Observable
    .create(startGattOnSubscribe)
    .flatMap(waitForConnection)
    .flatMap(waitForService)
    .flatMap(unlockDevice)
    .share();

  private static final class WriteQueueConsumer extends Subscriber<ConnectableObservable<?>> {
    @Override public void onCompleted() { }
    @Override public void onError(Throwable e) { }

    @Override public void onStart() {
      request(1);
    }

    @Override public void onNext(ConnectableObservable<?> connectableObservable) {
      Log.i("WriteQueue", "consuming next item");

      connectableObservable.subscribe(new Subscriber<Object>() {
        @Override public void onNext(Object whatevs) { }

        @Override public void onError(Throwable e) {
          Log.d("requestNext", "requesting next writeQueue item");
          WriteQueueConsumer.this.request(1);
        }

        @Override public void onCompleted() {
          Log.d("requestNext", "requesting next writeQueue item");
          WriteQueueConsumer.this.request(1);
        }
      });

      connectableObservable.connect();
    }
  }

  private ConnectableObservable<BluetoothGattCharacteristic> makeReadObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        if (gatt.readCharacteristic(characteristic)) {
          subscriber.onNext(null);
          subscriber.onCompleted();
        } else {
          subscriber.onError(new RuntimeException("failed to initiate read of characteristic " + param.uuid));
        }
      }
    }).flatMap(new Func1<Void, Observable<BluetoothGattCharacteristic>>() {
      @Override public Observable<BluetoothGattCharacteristic> call(Void aVoid) {
        return characteristicReadObservable
          .filter(new Func1<BluetoothGattCharacteristic, Boolean>() {
            @Override public Boolean call(BluetoothGattCharacteristic chara) {
              Log.i("makeReadObservable", "comparing " + param.uuid + " to " + chara.getUuid());
              return param.uuid.equals(chara.getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<BluetoothGattCharacteristic>() {
            @Override public void call(BluetoothGattCharacteristic characteristic) {
              Log.i("makeReadObservable", "finished read of " + param.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private ConnectableObservable<DescriptorWriteMsg> makeNotiObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        if (gatt.setCharacteristicNotification(characteristic, true)) {
          final BluetoothGattDescriptor descriptor =
            characteristic.getDescriptor(Uuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);

          // different Bluetooth Profile implementations support updates via either notification or indication.
          // detect which this device supports and use it.
          final int propNoti = BluetoothGattCharacteristic.PROPERTY_NOTIFY;

          final byte[] enableNotificationsValue = (characteristic.getProperties() & propNoti) == propNoti
            ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;

          descriptor.setValue(enableNotificationsValue);

          if (gatt.writeDescriptor(descriptor)) {
            subscriber.add(Subscriptions.create(new Action0() {
              @Override public void call() {
                Log.d("GattNotificationsOff", characteristic.getUuid().toString());
                gatt.setCharacteristicNotification(characteristic, false);

                final ConnectableObservable<? extends Object> stopNotiObservable =
                  makeStopNotiObservable(param, gatt, descriptor);

                Log.d("ChangeSubscriber", "queuing notification stop for " + param.uuid);
                writeQueue.onNext(stopNotiObservable);
              }
            }));

            subscriber.onNext(null);
            subscriber.onCompleted();

            return;
          }
        }

        subscriber.onError(new RuntimeException("failed to initiate streaming for characteristic " + param.uuid));
      }
    }).flatMap(new Func1<Void, Observable<DescriptorWriteMsg>>() {
      @Override public Observable<DescriptorWriteMsg> call(Void aVoid) {
        return descriptorWriteObservable
          .filter(new Func1<DescriptorWriteMsg, Boolean>() {
            @Override public Boolean call(DescriptorWriteMsg msg) {
              return param.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<DescriptorWriteMsg>() {
            @Override public void call(DescriptorWriteMsg msg) {
              Log.i("SubscribeToChanges", "enabled notifications for " + param.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private ConnectableObservable<? extends Object> makeStopNotiObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        if (gatt.writeDescriptor(descriptor)) {
          subscriber.onNext(null);
          subscriber.onCompleted();
        } else {
          subscriber.onError(new RuntimeException("failed to initiate stopping of notifications for " + param.uuid));
        }
      }
    }).flatMap(new Func1<Void, Observable<?>>() {
      @Override public Observable<?> call(Void aVoid) {
        return descriptorWriteObservable
          .filter(new Func1<DescriptorWriteMsg, Boolean>() {
            @Override public Boolean call(DescriptorWriteMsg msg) {
              return param.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<DescriptorWriteMsg>() {
            @Override public void call(DescriptorWriteMsg msg) {
              Log.i("SubscribeToChanges", "finished turning off notifications for " + param.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private static final class ConnectionStateChangeMsg {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    // CHECKSTYLE.ON
    public final int status;
    public final int newState;

    public ConnectionStateChangeMsg(BluetoothGatt gatt, int status, int newState) {
      this.gatt = gatt;
      this.status = status;
      this.newState = newState;
    }
  }

  private static final class ServiceMsg {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    // CHECKSTYLE.ON
    public final int status;

    public ServiceMsg(BluetoothGatt gatt, int status) {
      this.gatt = gatt;
      this.status = status;
    }
  }

  private static final class CharacteristicChangeMsg {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    public final BluetoothGattCharacteristic characteristic;
    // CHECKSTYLE.ON

    public CharacteristicChangeMsg(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      this.gatt = gatt;
      this.characteristic = characteristic;
    }
  }

  private static final class CharacteristicWriteMsg {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    public final BluetoothGattCharacteristic characteristic;
    // CHECKSTYLE.ON
    public final int status;

    public CharacteristicWriteMsg(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      this.gatt = gatt;
      this.characteristic = characteristic;
      this.status = status;
    }
  }

  private static final class DescriptorWriteMsg {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    public final BluetoothGattDescriptor descriptor;
    // CHECKSTYLE.ON
    public final int status;

    public DescriptorWriteMsg(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      this.gatt = gatt;
      this.descriptor = descriptor;
      this.status = status;
    }
  }

  private static final class GattService {
    // CHECKSTYLE.OFF: VisibilityModifier
    public final BluetoothGatt gatt;
    public final BluetoothGattService service;
    // CHECKSTYLE.ON

    public GattService(BluetoothGatt gatt, BluetoothGattService service) {
      this.gatt = gatt;
      this.service = service;
    }
  }

}

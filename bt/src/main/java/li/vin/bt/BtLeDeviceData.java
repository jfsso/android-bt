package li.vin.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
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

/*package*/ class BtLeDeviceData extends BluetoothGattCallback implements DeviceConnection {
  private static final String TAG = BtLeDeviceData.class.getSimpleName();

  private final PublishSubject<ConnectionStateChangeMsg> connectionStateObservable = PublishSubject.create();
  private final PublishSubject<ServiceMsg> serviceObservable = PublishSubject.create();
  private final PublishSubject<BluetoothGattCharacteristic> characteristicReadObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicChangeMsg> characteristicChangedObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicWriteMsg> characteristicWriteObservable = PublishSubject.create();
  private final PublishSubject<DescriptorWriteMsg> descriptorWriteObservable = PublishSubject.create();

  private final PublishSubject<ConnectableObservable<?>> writeQueue = PublishSubject.create();

  private final Map<UUID, Observable<?>> mUuidObservables = new ConcurrentHashMap<>();
  private final Map<Param<?, ?>, Observable<?>> mPidObservables = new IdentityHashMap<>();

  private final Context mContext;
  private final BluetoothDevice mDevice;
  private final String mUnlockKey;

  public BtLeDeviceData(Context context, BluetoothDevice device, String unlockKey) {
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
    Log.i(TAG, String.format("device(%s) onCharacteristicChanged characteristic(%s)",
      gatt.getDevice(), characteristic.getUuid()));
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

  @Override public ObdPair getLatest(final Param<?, ?> pid) {
    throw new UnsupportedOperationException("getLatest is not yet implemented");
  }

  @Override public <T, P> Observable<T> observe(final Param<T, P> pid) {
    if (pid == null) {
      throw new IllegalArgumentException("pid == null");
    }

    @SuppressWarnings("unchecked")
    Observable<T> pidObservable = (Observable<T>) mPidObservables.get(pid);
    if (pidObservable == null) {
      @SuppressWarnings("unchecked")
      Observable<P> uuidObservable = (Observable<P>) mUuidObservables.get(pid.uuid);
      if (uuidObservable == null) {
        final Func1<BluetoothGattCharacteristic, Boolean> matchesUuid = new Func1<BluetoothGattCharacteristic, Boolean>() {
          @Override public Boolean call(BluetoothGattCharacteristic characteristic) {
            return pid.uuid.equals(characteristic.getUuid());
          }
        };

        final Func1<BluetoothGattCharacteristic, P> parseCharacteristic = new Func1<BluetoothGattCharacteristic, P>() {
          @Override public P call(BluetoothGattCharacteristic characteristic) {
            return pid.parseCharacteristic(characteristic);
          }
        };

        uuidObservable = connectionObservable
          .flatMap(new Func1<GattService, Observable<BluetoothGattCharacteristic>>() {
            @Override public Observable<BluetoothGattCharacteristic> call(final GattService gs) {
              final BluetoothGattCharacteristic characteristic = gs.service.getCharacteristic(pid.uuid);
              if (characteristic == null) {
                throw new RuntimeException("no such characteristic: " + pid.uuid);
              }

              final ConnectableObservable<BluetoothGattCharacteristic> readObservable = pid.shouldRead
                ? makeReadObservable(pid, gs.gatt, characteristic)
                : null;

              final ConnectableObservable<DescriptorWriteMsg> notiObservable = pid.hasNotifications
                ? makeNotiObservable(pid, gs.gatt, characteristic)
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
            Log.d("uuid.doOnUnsubscribe", "unsubscribing " + pid.uuid);
            mUuidObservables.remove(pid.uuid);
          }
        });

        mUuidObservables.put(pid.uuid, uuidObservable);
      }

      final Func1<P, Boolean> matchesParsed = new Func1<P, Boolean>() {
        @Override public Boolean call(P s) {
          return pid.matches(s);
        }
      };

      final Func1<P, T> parseVal = new Func1<P, T>() {
        @Override public T call(P s) {
          return pid.parseVal(s);
        }
      };

      pidObservable = uuidObservable
        .filter(matchesParsed)
        .map(parseVal)
        .distinctUntilChanged()
        .share();

      pidObservable.doOnUnsubscribe(new Action0() {
        @Override public void call() {
          Log.d("pid.doOnUnsubscribe", "unsubscribing " + pid.uuid);
          mPidObservables.remove(pid);
        }
      });

      mPidObservables.put(pid, pidObservable);
    }

    return pidObservable;
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
      final BluetoothGatt gatt = mDevice.connectGatt(mContext, false, BtLeDeviceData.this);
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
      return connectionStateObservable
        .lift(waitForConnectionOperator)
        .first();
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
    @Override public void onCompleted() {}
    @Override public void onError(Throwable e) {}

    @Override public void onStart() {
      request(1);
    }

    @Override public void onNext(ConnectableObservable<?> connectableObservable) {
      Log.i("WriteQueue", "consuming next item");

      connectableObservable.subscribe(new Subscriber<Object>() {
        @Override public void onNext(Object whatevs) {}

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

  private ConnectableObservable<BluetoothGattCharacteristic> makeReadObservable(final Param<?, ?> pid,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        if (gatt.readCharacteristic(characteristic)) {
          subscriber.onNext(null);
          subscriber.onCompleted();
        } else {
          subscriber.onError(new RuntimeException("failed to initiate read of characteristic " + pid.uuid));
        }
      }
    }).flatMap(new Func1<Void, Observable<BluetoothGattCharacteristic>>() {
      @Override public Observable<BluetoothGattCharacteristic> call(Void aVoid) {
        return characteristicReadObservable
          .filter(new Func1<BluetoothGattCharacteristic, Boolean>() {
            @Override public Boolean call(BluetoothGattCharacteristic chara) {
              Log.i("makeReadObservable", "comparing " + pid.uuid + " to " + chara.getUuid());
              return pid.uuid.equals(chara.getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<BluetoothGattCharacteristic>() {
            @Override public void call(BluetoothGattCharacteristic characteristic) {
              Log.i("makeReadObservable", "finished read of " + pid.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private ConnectableObservable<DescriptorWriteMsg> makeNotiObservable(final Param<?, ?> pid,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        if (gatt.setCharacteristicNotification(characteristic, true)) {
          final BluetoothGattDescriptor descriptor =
            characteristic.getDescriptor(Uuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);

          descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

          if (gatt.writeDescriptor(descriptor)) {
            subscriber.add(Subscriptions.create(new Action0() {
              @Override public void call() {
                Log.d("GattNotificationsOff", characteristic.getUuid().toString());
                gatt.setCharacteristicNotification(characteristic, false);

                final ConnectableObservable<? extends Object> stopNotiObservable =
                  makeStopNotiObservable(pid, gatt, descriptor);

                Log.d("ChangeSubscriber", "queuing notification stop for " + pid.uuid);
                writeQueue.onNext(stopNotiObservable);
              }
            }));

            subscriber.onNext(null);
            subscriber.onCompleted();

            return;
          }
        }

        subscriber.onError(new RuntimeException("failed to initiate streaming for characteristic " + pid.uuid));
      }
    }).flatMap(new Func1<Void, Observable<DescriptorWriteMsg>>() {
      @Override public Observable<DescriptorWriteMsg> call(Void aVoid) {
        return descriptorWriteObservable
          .filter(new Func1<DescriptorWriteMsg, Boolean>() {
            @Override public Boolean call(DescriptorWriteMsg msg) {
              return pid.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<DescriptorWriteMsg>() {
            @Override public void call(DescriptorWriteMsg msg) {
              Log.i("SubscribeToChanges", "enabled notifications for " + pid.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private ConnectableObservable<? extends Object> makeStopNotiObservable(final Param<?, ?> pid,
      final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        descriptor.setValue(new byte[] { 0x00, 0x00 });
        if (gatt.writeDescriptor(descriptor)) {
          subscriber.onNext(null);
          subscriber.onCompleted();
        } else {
          subscriber.onError(new RuntimeException("failed to initiate stopping of notifications for " + pid.uuid));
        }
      }
    }).flatMap(new Func1<Void, Observable<?>>() {
      @Override public Observable<?> call(Void aVoid) {
        return descriptorWriteObservable
          .filter(new Func1<DescriptorWriteMsg, Boolean>() {
            @Override public Boolean call(DescriptorWriteMsg msg) {
              return pid.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
            }
          })
          .first()
          /*.doOnNext(new Action1<DescriptorWriteMsg>() {
            @Override public void call(DescriptorWriteMsg msg) {
              Log.i("SubscribeToChanges", "finished turning off notifications for " + pid.uuid);
            }
          })*/;
      }
    }).publish();
  }

  private static final class ConnectionStateChangeMsg {
    public final BluetoothGatt gatt;
    public final int status;
    public final int newState;

    public ConnectionStateChangeMsg(BluetoothGatt gatt, int status, int newState) {
      this.gatt = gatt;
      this.status = status;
      this.newState = newState;
    }
  }

  private static final class ServiceMsg {
    public final BluetoothGatt gatt;
    public final int status;

    public ServiceMsg(BluetoothGatt gatt, int status) {
      this.gatt = gatt;
      this.status = status;
    }
  }

  private static final class CharacteristicChangeMsg {
    public final BluetoothGatt gatt;
    public final BluetoothGattCharacteristic characteristic;

    public CharacteristicChangeMsg(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      this.gatt = gatt;
      this.characteristic = characteristic;
    }
  }

  private static final class CharacteristicWriteMsg {
    public final BluetoothGatt gatt;
    public final BluetoothGattCharacteristic characteristic;
    public final int status;

    public CharacteristicWriteMsg(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      this.gatt = gatt;
      this.characteristic = characteristic;
      this.status = status;
    }
  }

  private static final class DescriptorWriteMsg {
    public final BluetoothGatt gatt;
    public final BluetoothGattDescriptor descriptor;
    public final int status;

    public DescriptorWriteMsg(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      this.gatt = gatt;
      this.descriptor = descriptor;
      this.status = status;
    }
  }

  private static final class GattService {
    public final BluetoothGatt gatt;
    public final BluetoothGattService service;

    public GattService(BluetoothGatt gatt, BluetoothGattService service) {
      this.gatt = gatt;
      this.service = service;
    }
  }

}

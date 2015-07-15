package li.vin.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/*package*/ class DeviceServiceBinder extends IVinliService.Stub {
  private static final String TAG = DeviceServiceBinder.class.getSimpleName();
  private static final long DEFAULT_SCAN_PERIOD = 10000;

  private Map<Object, Subscription> mSubscriptions = new ConcurrentHashMap<>();

  private final Context mContext;

  public DeviceServiceBinder(Context context) {
    super();
    Log.d(TAG, "constructor");
    mContext = context;
  }

  @Override public void observeBool(final String name, final IVinliServiceCallbackBool cb) {
    try {
      final Subscription sub = observe(name, Params.<Boolean>paramFor(name)).subscribe(new Subscriber<Boolean>() {
        @Override public void onCompleted() {
          try {
            cb.onCompleted();
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onError(Throwable e) {
          Log.e(TAG,  name, e);
          try {
            cb.onError(e.getMessage());
          } catch (RemoteException re) {
            Log.e(TAG, name, re);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onNext(Boolean b) {
          try {
            cb.onNext(b);
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }
        }
      });

      mSubscriptions.put(cb, sub);
    } catch (Exception e) {
      try {
        cb.onError(e.getMessage());
      } catch (RemoteException re) {
        Log.e(TAG, name, re);
      }
    }
  }

  @Override public void observeDtc(final String name, final IVinliServiceCallbackDtc cb) {
    try {
      final Subscription sub = observe(name, Params.<List<String>>paramFor(name)).subscribe(new Subscriber<List<String>>() {
        @Override public void onCompleted() {
          try {
            cb.onCompleted();
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onError(Throwable e) {
          Log.e(TAG,  name, e);
          try {
            cb.onError(e.getMessage());
          } catch (RemoteException re) {
            Log.e(TAG, name, re);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onNext(List<String> dtcs) {
          try {
            cb.onNext(dtcs);
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }
        }
      });

      mSubscriptions.put(cb, sub);
    } catch (Exception e) {
      try {
        cb.onError(e.getMessage());
      } catch (RemoteException re) {
        Log.e(TAG, name, re);
      }
    }
  }

  @Override public void observeFloat(final String name, final IVinliServiceCallbackFloat cb) {
    try {
      final Subscription sub = observe(name, Params.<Float>paramFor(name)).subscribe(new Subscriber<Float>() {
        @Override public void onCompleted() {
          try {
            cb.onCompleted();
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onError(Throwable e) {
          Log.e(TAG,  name, e);
          try {
            cb.onError(e.getMessage());
          } catch (RemoteException re) {
            Log.e(TAG, name, re);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onNext(Float f) {
          try {
            cb.onNext(f);
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }
        }
      });

      mSubscriptions.put(cb, sub);
    } catch (Exception e) {
      try {
        cb.onError(e.getMessage());
      } catch (RemoteException re) {
        Log.e(TAG, name, re);
      }
    }
  }

  @Override public void observeInt(final String name, final IVinliServiceCallbackInt cb) {
    try {
      final Subscription sub = observe(name, Params.<Integer>paramFor(name)).subscribe(new Subscriber<Integer>() {
        @Override public void onCompleted() {
          try {
            cb.onCompleted();
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onError(Throwable e) {
          Log.e(TAG,  name, e);
          try {
            cb.onError(e.getMessage());
          } catch (RemoteException re) {
            Log.e(TAG, name, re);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onNext(Integer i) {
          try {
            cb.onNext(i);
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }
        }
      });

      mSubscriptions.put(cb, sub);
    } catch (Exception e) {
      try {
        cb.onError(e.getMessage());
      } catch (RemoteException re) {
        Log.e(TAG, name, re);
      }
    }
  }

  @Override public void observeString(final String name, final IVinliServiceCallbackString cb) {
    try {
      final Subscription sub = observe(name, Params.<String>paramFor(name)).subscribe(new Subscriber<String>() {
        @Override public void onCompleted() {
          try {
            cb.onCompleted();
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onError(Throwable e) {
          Log.e(TAG,  name, e);
          try {
            cb.onError(e.getMessage());
          } catch (RemoteException re) {
            Log.e(TAG, name, re);
          }

          mSubscriptions.remove(cb);
        }

        @Override public void onNext(String s) {
          try {
            cb.onNext(s);
          } catch (RemoteException e) {
            Log.e(TAG, name, e);
          }
        }
      });

      mSubscriptions.put(cb, sub);
    } catch (Exception e) {
      try {
        cb.onError(e.getMessage());
      } catch (RemoteException re) {
        Log.e(TAG, name, re);
      }
    }
  }

  @Override public void unsubscribeBool(IVinliServiceCallbackBool cb) {
    final Subscription sub = mSubscriptions.remove(cb);
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  @Override public void unsubscribeDtc(IVinliServiceCallbackDtc cb) {
    final Subscription sub = mSubscriptions.remove(cb);
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  @Override public void unsubscribeFloat(IVinliServiceCallbackFloat cb) {
    final Subscription sub = mSubscriptions.remove(cb);
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  @Override public void unsubscribeInt(IVinliServiceCallbackInt cb) {
    final Subscription sub = mSubscriptions.remove(cb);
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  @Override public void unsubscribeString(IVinliServiceCallbackString cb) {
    final Subscription sub = mSubscriptions.remove(cb);
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  public void unsubscribeAll() {
    for (Subscription sub : mSubscriptions.values()) {
      sub.unsubscribe();
    }

    mSubscriptions.clear();
  }

  public void resetDtcs(final IVinliServiceCallbackBool cb) {
    Log.d(TAG, "resetDtcs");
    final ConnectableObservable<Void> clearDtcsObservable = connectionObservable
      .flatMap(new Func1<GattService, Observable<CharacteristicWriteMsg>>() {
        @Override public Observable<CharacteristicWriteMsg> call(final GattService gs) {
          Log.d(TAG, "resetDtcs: connected");
          final BluetoothGattCharacteristic characteristic = gs.service.getCharacteristic(Uuids.CLEAR_DTCS);
          if (characteristic == null) {
            throw new RuntimeException("bluetooth service is missing the CLEAR_DTCS characteristic");
          }

          characteristic.setValue(new byte[] {0x00});

          if (!gs.gatt.writeCharacteristic(characteristic)) {
            throw new RuntimeException("failed to initiate write to clear DTCs");
          }

          Log.d(TAG, "resetDtcs: waiting for write confirmation");
          return characteristicWriteObservable;
        }
      })
      .filter(new Func1<CharacteristicWriteMsg, Boolean>() {
        @Override public Boolean call(CharacteristicWriteMsg msg) {
          return Uuids.CLEAR_DTCS.equals(msg.characteristic.getUuid());
        }
      })
      .first()
      .map(new Func1<CharacteristicWriteMsg, Void>() {
        @Override public Void call(CharacteristicWriteMsg msg) {
          if (BluetoothGatt.GATT_SUCCESS != msg.status) {
            throw new RuntimeException("failed to clear the DTCs");
          }
          Log.d(TAG, "resetDtcs: dtcs reset");
          return null;
        }
      })
      .publish();

    clearDtcsObservable.subscribe(new Subscriber<Void>() {
      @Override public void onCompleted() {
        try {
          cb.onCompleted();
        } catch (RemoteException re) {
          Log.e(TAG, "clearDtcs", re);
        }
      }

      @Override public void onError(Throwable e) {
        try {
          cb.onError(e.getMessage());
        } catch (RemoteException re) {
          Log.e(TAG, "clearDtcs", re);
        }
      }

      @Override public void onNext(Void aVoid) {
        try {
          cb.onNext(true);
        } catch (RemoteException re) {
          Log.e(TAG, "clearDtcs", re);
        }
      }
    });

    writeQueue.onNext(clearDtcsObservable);
  }

  private <T, P> Observable<T> observe(@NonNull final String name, @NonNull final ParamImpl<T, P> param) {
    Log.d(TAG, "observing " + name);

    @SuppressWarnings("unchecked")
    Observable<T> paramObservable = (Observable<T>) mParamObservables.get(param);
    if (paramObservable == null) {
      Log.d(TAG, "creating param observable for " + name);
      @SuppressWarnings("unchecked")
      Observable<P> uuidObservable = (Observable<P>) mUuidObservables.get(param.uuid);
      if (uuidObservable == null) {
        Log.d(TAG, "creating UUID observable for " + name);

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
                Log.d(TAG, "no read or notifications for " + param.uuid + " due to " + name);
                return Observable.empty();
              }

              if (readObservable != null && notiObservable == null) {
                Log.d(TAG, "queueing read for " + param.uuid + " due to " + name);
                writeQueue.onNext(readObservable);
                return readObservable;
              }

              final Observable<BluetoothGattCharacteristic> notiValObservable = notiObservable
                .flatMap(getCharacteristicChanges)
                .map(pluckCharacteristic)
                .doOnUnsubscribe(new Action0() {
                  @Override public void call() {
                    Log.d(TAG, "GattNotificationsOff " + characteristic.getUuid());
                    gs.gatt.setCharacteristicNotification(characteristic, false);

                    final BluetoothGattDescriptor descriptor =
                      characteristic.getDescriptor(Uuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);

                    Log.d(TAG, "queuing notification stop for " + param.uuid);
                    writeQueue.onNext(makeStopNotiObservable(param, gs.gatt, descriptor));
                  }
                })
                .share();

              if (readObservable == null) {
                Log.d(TAG, "queueing notifications for " + param.uuid + " due to " + name);
                writeQueue.onNext(notiObservable);
                return notiValObservable;
              } else {
                Log.d(TAG, "queueing read & notifications for " + param.uuid + " due to " + name);
                writeQueue.onNext(readObservable);
                writeQueue.onNext(notiObservable);
                return Observable.merge(readObservable, notiValObservable);
              }
            }
          })
          .filter(new Func1<BluetoothGattCharacteristic, Boolean>() {
            @Override public Boolean call(BluetoothGattCharacteristic characteristic) {
              return param.uuid.equals(characteristic.getUuid());
            }
          })
          .map(new Func1<BluetoothGattCharacteristic, P>() {
            @Override public P call(BluetoothGattCharacteristic characteristic) {
//              Log.d(TAG, "parsing value for " + name);
              return param.parseCharacteristic(characteristic);
            }
          })
          .doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
              Log.d(TAG, "unsubscribed " + param.uuid);
              mUuidObservables.remove(param.uuid);
            }
          })
          .share();

        mUuidObservables.put(param.uuid, uuidObservable);
      } else {
        Log.d(TAG, "UUID observable already exists for " + name);
      }

      paramObservable = uuidObservable
        .filter(new Func1<P, Boolean>() {
          @Override public Boolean call(P s) {
            return param.matches(s);
          }
        })
        .map(new Func1<P, T>() {
          @Override public T call(P s) {
            return param.parseVal(s);
          }
        })
//        .doOnNext(new Action1<T>() {
//          @Override public void call(T t) {
//            Log.d(TAG, "value for " + name + ": " + t);
//          }
//        })
        .distinctUntilChanged()
        .doOnUnsubscribe(new Action0() {
          @Override
          public void call() {
            Log.d(TAG, "unsubscribed " + name);
            mParamObservables.remove(param);
          }
        })
        .share();

      mParamObservables.put(param, paramObservable);
    } else {
      Log.d(TAG, "param observable already exists for " + name);
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

  private final PublishSubject<ConnectionStateChangeMsg> connectionStateObservable = PublishSubject.create();
  private final PublishSubject<ServiceMsg> serviceObservable = PublishSubject.create();
  private final PublishSubject<BluetoothGattCharacteristic> characteristicReadObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicChangeMsg> characteristicChangedObservable = PublishSubject.create();
  private final PublishSubject<CharacteristicWriteMsg> characteristicWriteObservable = PublishSubject.create();
  private final PublishSubject<DescriptorWriteMsg> descriptorWriteObservable = PublishSubject.create();

  private final BluetoothGattCallback mBtGattCb = new BluetoothGattCallback() {
    @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      Log.i(TAG, String.format("device(%s) onConnectionStateChange status(%s) newState(%s)",
        gatt.getDevice(), Utils.gattStatus(status), Utils.btState(newState)));

      connectionStateObservable.onNext(new ConnectionStateChangeMsg(gatt, status, newState));
    }

    @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      Log.i(TAG, String.format("device(%s) onServicesDiscovered status(%s)", gatt.getDevice(),
        Utils.gattStatus(status)));

      serviceObservable.onNext(new ServiceMsg(gatt, status));
    }

    @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      Log.i(TAG, String.format("device(%s) onCharacteristicRead characteristic(%s) status(%s)",
        gatt.getDevice(), characteristic.getUuid(), Utils.gattStatus(status)));

      characteristicReadObservable.onNext(characteristic);
//    characteristicChangedObservable.onNext(new CharacteristicChangeMsg(gatt, characteristic));
    }

    @Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//    Log.i(TAG, String.format("device(%s) onCharacteristicChanged characteristic(%s)",
//      gatt.getDevice(), characteristic.getUuid()));
      characteristicChangedObservable.onNext(new CharacteristicChangeMsg(gatt, characteristic));
    }

    @Override public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      Log.i(TAG, String.format("device(%s) onCharacteristicWrite characteristic(%s) status(%s)",
        gatt.getDevice(), characteristic, Utils.gattStatus(status)));

      characteristicWriteObservable.onNext(new CharacteristicWriteMsg(gatt, characteristic, status));
    }

    @Override public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      Log.i(TAG, String.format("device(%s) onDescriptorWrite descriptor(%s) status(%s)",
        gatt.getDevice(), descriptor.getCharacteristic().getUuid(), Utils.gattStatus(status)));

      descriptorWriteObservable.onNext(new DescriptorWriteMsg(gatt, descriptor, status));
    }
  };

  private final PublishSubject<ConnectableObservable<?>> writeQueue = PublishSubject.create();

  private final Map<UUID, Observable<?>> mUuidObservables = new ConcurrentHashMap<>();
  private final Map<Param<?>, Observable<?>> mParamObservables = new ConcurrentHashMap<>();

  private final Observable<GattService> connectionObservable = Observable.create(new Observable.OnSubscribe<BluetoothDevice>() {
    @Override public void call(final Subscriber<? super BluetoothDevice> subscriber) {
      final BluetoothManager manager =
        (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

      if (manager == null) {
        subscriber.onError(new BluetoothException("failed to get the Android bluetooth service"));
        return;
      }

      final BluetoothAdapter adapter = manager.getAdapter();

      if (adapter == null || !adapter.isEnabled()) {
        subscriber.onError(new BluetoothDisabledException("bluetooth is disabled"));
        return;
      }

      final Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
      if (!bondedDevices.isEmpty()) {
        boolean found = false;
        for (BluetoothDevice bd : bondedDevices) {
          for (ParcelUuid pu : bd.getUuids()) {
            if (Uuids.SERVICE.equals(pu.getUuid())) {
              found = true;
              subscriber.onNext(bd);
              break;
            }
          }
        }

        if (found) {
          Log.d(TAG, "found bonded Vinli device");
          subscriber.onCompleted();
          return;
        } else {
          Log.d(TAG, "no bonded Vinli devices found");
        }
      }

      final BluetoothAdapter.LeScanCallback listener = new BluetoothAdapter.LeScanCallback() {
        @Override public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
          Log.d(TAG, "Found device " + device + " UUIDs: " + Arrays.toString(device.getUuids()));

          final List<UUID> uuids = parseUuids(scanRecord);
          for (UUID uuid : parseUuids(scanRecord)) {
            if (Uuids.SERVICE.equals(uuid)) {
              subscriber.onNext(device);
              break;
            }
          }
        }
      };

      Log.d(TAG, "looking for device...");

      final Action0 stopScan = new Action0() {
        private boolean called = false;

        @Override public void call() {
          if (!called) {
            adapter.stopLeScan(listener);
            called = true;
          }
        }
      };

      final Runnable timeoutListener = new Runnable() {
        @Override public void run() {
          Log.d(TAG, "device scanned timed out");
          stopScan.call();
          subscriber.onCompleted();
        }
      };

      final Looper l = Looper.myLooper();
      if (l == null) {
        Log.d(TAG, "preparing looper");
        Looper.prepare();
      } else {
        Log.d(TAG, "looper already created");
      }

      final Handler handler = new Handler();
      handler.postDelayed(timeoutListener, DEFAULT_SCAN_PERIOD);
      adapter.startLeScan(listener);

      subscriber.add(Subscriptions.create(new Action0() {
        @Override public void call() {
          Log.d(TAG, "all unsubscribed from device scan");
          stopScan.call();
          handler.removeCallbacks(timeoutListener);
        }
      }));
    }
  })
  .first()
  .flatMap(new Func1<BluetoothDevice, Observable<ConnectionStateChangeMsg>>() {
    @Override public Observable<ConnectionStateChangeMsg> call(BluetoothDevice device) {
      Log.d(TAG, "found device");
      final BluetoothGatt gatt = device.connectGatt(mContext, true, mBtGattCb);
      return connectionStateObservable;
    }
  })
  .filter(new Func1<ConnectionStateChangeMsg, Boolean>() {
    @Override public Boolean call(ConnectionStateChangeMsg msg) {
      return BluetoothProfile.STATE_CONNECTED == msg.newState;
    }
  })
  .first()
  .flatMap(new Func1<ConnectionStateChangeMsg, Observable<ServiceMsg>>() {
    @Override public Observable<ServiceMsg> call(ConnectionStateChangeMsg msg) {
      Log.d(TAG, "connected to device. Discovering services...");
      msg.gatt.discoverServices();
      return serviceObservable;
    }
  })
  .flatMap(new Func1<ServiceMsg, Observable<GattService>>() {
    @Override public Observable<GattService> call(ServiceMsg msg) {
      if (BluetoothGatt.GATT_SUCCESS != msg.status) {
        throw new RuntimeException("failed to find services"); // TODO: better error
      }

      final BluetoothGattService service = msg.gatt.getService(Uuids.SERVICE);
      if (service == null) {
        throw new RuntimeException("service not found: " + Uuids.SERVICE); // TODO: better error
      }

      Log.d(TAG, "found Vinli service. Unlocking device...");

      final BluetoothGattCharacteristic characteristic = service.getCharacteristic(Uuids.UNLOCK);
      if (characteristic == null) {
        throw new RuntimeException("no such characteristic: " + Uuids.UNLOCK);
      }

      // TODO: get unlock key from app
      characteristic.setValue("123123".getBytes(Charset.forName("ASCII")));
      if (!msg.gatt.writeCharacteristic(characteristic)) {
        throw new RuntimeException("failed to start write to unlock device");
      }

      return characteristicWriteObservable
        .filter(new Func1<CharacteristicWriteMsg, Boolean>() {
          @Override public Boolean call(CharacteristicWriteMsg msg) {
            return Uuids.UNLOCK.equals(msg.characteristic.getUuid());
          }
        })
        .first()
        .map(new Func1<CharacteristicWriteMsg, GattService>() {
          @Override public GattService call(CharacteristicWriteMsg msg) {
            if (BluetoothGatt.GATT_SUCCESS != msg.status) {
              throw new RuntimeException("failed to unlock service: " + Utils.gattStatus(msg.status));
            }

            Log.d(TAG, "device unlocked");

            return new GattService(msg.gatt, service);
          }
        });
    }
  })
  .takeUntil(connectionStateObservable.filter(new Func1<ConnectionStateChangeMsg, Boolean>() {
    @Override public Boolean call(ConnectionStateChangeMsg msg) {
      return BluetoothProfile.STATE_DISCONNECTED == msg.newState;
    }
  }))
  .lift(new Observable.Operator<GattService, GattService>() {
    @Override public Subscriber<? super GattService> call(Subscriber<? super GattService> subscriber) {
      return new GattSubscriber(subscriber);
    }
  })
  .replay(1)
  .refCount();

  private final class GattSubscriber extends Subscriber<GattService> {
    private final Subscriber<? super GattService> mSubscriber;

    private BluetoothGatt mGatt;

    public GattSubscriber(@NonNull Subscriber<? super GattService> subscriber) {
      mSubscriber = subscriber;
    }

    @Override public void onStart() {
      Log.d(TAG, "starting device connection");
      final Subscription writeQueueSubscription = writeQueue
        .onBackpressureBuffer()
        .subscribe(new WriteQueueConsumer());

      mSubscriber.add(Subscriptions.create(new Action0() {
        @Override public void call() {
          Log.d(TAG, "disconnecting from gatt after all unsubscribed");
          writeQueueSubscription.unsubscribe();

          connectionStateObservable
            .filter(new Func1<ConnectionStateChangeMsg, Boolean>() {
              @Override public Boolean call(ConnectionStateChangeMsg msg) {
                return BluetoothProfile.STATE_DISCONNECTED == msg.newState;
              }
            })
            .first()
            .subscribe(new Subscriber<ConnectionStateChangeMsg>() {
              @Override public void onCompleted() { }

              @Override public void onError(Throwable e) {
                Log.e(TAG, "failed to disconnect from gatt", e);
              }

              @Override public void onNext(ConnectionStateChangeMsg msg) {
                msg.gatt.close();
              }
            });

          if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
          }

          GattSubscriber.this.unsubscribe();
        }
      }));
    }

    @Override public void onCompleted() {
      if (!mSubscriber.isUnsubscribed()) {
        mSubscriber.onCompleted();
      }
    }

    @Override public void onError(Throwable e) {
      if (!mSubscriber.isUnsubscribed()) {
        mSubscriber.onError(e);
      }
    }

    @Override public void onNext(GattService gs) {
      mGatt = gs.gatt;
      if (!mSubscriber.isUnsubscribed()) {
        mSubscriber.onNext(gs);
      }
    }
  }

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
          Log.e(TAG, "WriteQueue item failed", e);
          Log.d(TAG, "requesting next writeQueue item");
          WriteQueueConsumer.this.request(1);
        }

        @Override public void onCompleted() {
          Log.d(TAG, "requesting next writeQueue item");
          WriteQueueConsumer.this.request(1);
        }
      });

      connectableObservable.connect();
    }
  }

  private ConnectableObservable<BluetoothGattCharacteristic> makeReadObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return characteristicReadObservable
      .filter(new Func1<BluetoothGattCharacteristic, Boolean>() {
        @Override public Boolean call(BluetoothGattCharacteristic chara) {
          Log.i("makeReadObservable", "comparing " + param.uuid + " to " + chara.getUuid());
          return param.uuid.equals(chara.getUuid());
        }
      })
      .first()
      .doOnSubscribe(new Action0() {
        @Override public void call() {
          if (!gatt.readCharacteristic(characteristic)) {
            throw new RuntimeException("failed to initiate read of characteristic " + param.uuid);
          }
        }
      })
      .publish();
  }

  private ConnectableObservable<DescriptorWriteMsg> makeNotiObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
    return descriptorWriteObservable
      .filter(new Func1<DescriptorWriteMsg, Boolean>() {
        @Override public Boolean call(DescriptorWriteMsg msg) {
          return param.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
        }
      })
      .first()
      .lift(new Observable.Operator<DescriptorWriteMsg, DescriptorWriteMsg>() {
        @Override public Subscriber<? super DescriptorWriteMsg> call(Subscriber<? super DescriptorWriteMsg> subscriber) {
          if (!gatt.setCharacteristicNotification(characteristic, true)) {
            throw new RuntimeException("failed to initiate streaming for characteristic " + param.uuid);
          }

          final BluetoothGattDescriptor descriptor =
            characteristic.getDescriptor(Uuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);

          // different Bluetooth Profile implementations support updates via either notification or indication.
          // detect which this device supports and use it.
          final int propNoti = BluetoothGattCharacteristic.PROPERTY_NOTIFY;

          final byte[] enableNotificationsValue = (characteristic.getProperties() & propNoti) == propNoti
            ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;

          descriptor.setValue(enableNotificationsValue);

          if (!gatt.writeDescriptor(descriptor)) {
            throw new RuntimeException("failed to initiate streaming for characteristic " + param.uuid);
          }

          return subscriber;
        }
      })
      .publish();
  }

  private ConnectableObservable<? extends Object> makeStopNotiObservable(final ParamImpl<?, ?> param,
      final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor) {
    return descriptorWriteObservable
      .filter(new Func1<DescriptorWriteMsg, Boolean>() {
        @Override public Boolean call(DescriptorWriteMsg msg) {
          return param.uuid.equals(msg.descriptor.getCharacteristic().getUuid());
        }
      })
      .first()
      .doOnSubscribe(new Action0() {
        @Override public void call() {
          descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
          if (!gatt.writeDescriptor(descriptor)) {
            throw new RuntimeException("failed to initiate stopping of notifications for " + param.uuid);
          }
        }
      })
      .publish();
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

  /**
   * Work-around for device filtering not working for 128-bit UUIDs
   * <a href="http://stackoverflow.com/a/19060589">Implementation found here</a>
   */
  private static final List<UUID> parseUuids(byte[] advertisedData) {
    final List<UUID> uuids = new ArrayList<>();

    final ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
    while (buffer.remaining() > 2) {
      byte length = buffer.get();
      if (length == 0) {
        break;
      }

      byte type = buffer.get();
      switch (type) {
        case 0x02: // Partial list of 16-bit UUIDs
        case 0x03: // Complete list of 16-bit UUIDs
          while (length >= 2) {
            uuids.add(UUID.fromString(String.format(
              "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
            length -= 2;
          }
          break;

        case 0x06: // Partial list of 128-bit UUIDs
        case 0x07: // Complete list of 128-bit UUIDs
          while (length >= 16) {
            long lsb = buffer.getLong();
            long msb = buffer.getLong();
            uuids.add(new UUID(msb, lsb));
            length -= 16;
          }
          break;

        default:
          buffer.position(buffer.position() + length - 1);
          break;
      }
    }

    return uuids;
  }
}

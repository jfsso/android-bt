package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

import static android.text.TextUtils.getTrimmedLength;

/*package*/ class BtLeDeviceConnection extends BluetoothGattCallback implements DeviceConnection {
  private static final String TAG = BtLeDeviceConnection.class.getSimpleName();

  private final Map<Param<?>, Observable<?>> paramObservables = new IdentityHashMap<>();
  private final Set<Object> ops = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

  private IDevServ devServ;
  private boolean isServiceBound;
  private final HashSet<Runnable> runOnServiceConnected = new HashSet<>();

  private final Handler handler = new Handler(Looper.getMainLooper());
  private WeakReference<Context> contextRef;
  /*package*/ final String chipId;
  /*package*/ final String deviceName;
  /*package*/ final String deviceIcon;
  /*package*/ final String deviceId;

  public BtLeDeviceConnection(@NonNull Context context, @NonNull String chipId, String deviceName,
      String deviceIcon, @NonNull String deviceId) {
    this.chipId = chipId;
    this.deviceName = deviceName;
    this.deviceIcon = deviceIcon;
    this.deviceId = deviceId;
    updateContext(context);
  }

  private Context context() {
    return contextRef.get();
  }

  /*package*/ void updateContext(@NonNull Context context) {
    contextRef = new WeakReference<>(context);
  }

  @Override public String chipId() {
    return chipId;
  }

  @Override public String deviceName() {
    return deviceName;
  }

  @Override public String deviceIcon() {
    return deviceIcon;
  }

  @NonNull @Override public String deviceId() {
    return deviceId;
  }

  @NonNull @Override public Observable<Void> resetDtcs() {
    return serviceObservable.flatMap(new Func1<IDevServ, Observable<Void>>() {
      @Override public Observable<Void> call(final IDevServ iVinliService) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
          @Override public void call(final Subscriber<? super Void> subscriber) {
            try {
              final String opUuid =
                  iVinliService.resetDtcs(chipId, new IVinliServiceCallbackBool.Stub() {
                    @Override public void onCompleted() throws RemoteException {
                      if (!subscriber.isUnsubscribed()) {
                        Log.d(TAG, "RESETDCS ONCOMPLETED");
                        subscriber.onCompleted();
                      }
                    }

                    @Override public void onError(String err) throws RemoteException {
                      if (!subscriber.isUnsubscribed()) {
                        Log.d(TAG, "RESETDCS onError " + err);
                        subscriber.onError(new RuntimeException(err));
                      }
                    }

                    @Override public void onNext(boolean val) throws RemoteException {
                      if (!subscriber.isUnsubscribed()) {
                        Log.d(TAG, "RESETDCS onNext");
                        subscriber.onNext(null);
                      }
                    }
                  });

              if (opUuid == null || getTrimmedLength(opUuid) == 0) {
                throw new NullPointerException("empty UUID returned by resetDtcs.");
              }

              putOp(opUuid);
              subscriber.add(Subscriptions.create(new Action0() {
                @Override public void call() {
                  try {
                    iVinliService.cancelOp(opUuid);
                    Log.d(TAG, "successful cancelOp resetDtcs (" + opUuid + ")");
                  } catch (Exception e) {
                    Log.e(TAG, "failed to cancelOp resetDtcs (" + opUuid + ")", e);
                  }
                  removeOp(opUuid);
                }
              }));
            } catch (Exception e) {
              Log.e(TAG, "failed to reset DTCs", e);
              subscriber.onError(new RuntimeException("failed to reset DTCs", e));
            }
          }
        });
      }
    });
  }

  @NonNull @Override public Observable<SupportedPids> supportedPids() {
    return observe(Params.PIDS).map(new Func1<String, SupportedPids>() {
      @Override public SupportedPids call(String rawPids) {
        return new SupportedPids(rawPids);
      }
    });
  }

  @NonNull @Override public <T> Observable<T> observe(@NonNull final Param<T> param) {
    final String name = Params.nameFor(param);
    if (name == null) {
      return Observable.error(new RuntimeException("unrecognized param"));
    }
    return putOrCreateParamObs(param, new ObservableFactory<T>() {
      @Override public Observable<T> create() {
        Log.d(TAG, "creating param observable for " + name);
        return serviceObservable.flatMap(param.getServiceFunc(chipId, name))
            .onBackpressureLatest()
            .doOnUnsubscribe(new Action0() {
              @Override public void call() {
                Log.d(TAG, "all unsubscribed from " + name);
                removeParamObs(param);
              }
            })
            .share();
      }
    });
  }

  public void shutdown() {
    handler.removeCallbacks(shutdown);
    handler.post(shutdown);
  }

  private interface ObservableFactory<T> {
    Observable<T> create();
  }

  private <T> Observable<T> putOrCreateParamObs(Param<T> param, ObservableFactory<T> factory) {
    Observable<T> result;
    synchronized (paramObservables) {
      Observable<?> prev = paramObservables.get(param);
      if (prev == null) {
        paramObservables.put(param, result = factory.create());
      } else {
        //noinspection unchecked
        result = (Observable<T>) prev;
      }
    }
    putOp(param);
    return result;
  }

  private void removeParamObs(Param<?> param) {
    synchronized (paramObservables) {
      paramObservables.remove(param);
    }
    removeOp(param);
  }

  private void putOp(Object op) {
    synchronized (ops) {
      ops.add(op);
    }
  }

  private void removeOp(Object op) {
    boolean emptied;
    synchronized (ops) {
      ops.remove(op);
      emptied = ops.isEmpty();
    }
    if (emptied) {
      handler.removeCallbacks(shutdown);
      handler.post(shutdown);
    }
  }

  private final Runnable shutdown = new Runnable() {
    @Override public void run() {
      handler.removeCallbacks(this);
      try {
        Context context = context();
        if (context == null) throw new Exception("no Context available.");
        context.unbindService(servConn);
        Log.d(TAG, "Vinli device service successfully unbound.");
        isServiceBound = false;
        devServ = null;
        dispatchAndClear(runOnServiceConnected);
        synchronized (ops) {
          ops.clear();
        }
      } catch (Exception e) {
        Log.e(TAG, "Vinli device service unbind error: " + e);
      }
    }
  };

  // safely dispatch from copied set to prevent side effects, concurrent mod, etc.
  private static void dispatchAndClear(Set<Runnable> runnables) {
    Set<Runnable> copy = new HashSet<>(runnables);
    runnables.clear();
    for (Runnable r : copy) r.run();
  }

  private final ServiceConnection servConn = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, final IBinder service) {
      devServ = IDevServ.Stub.asInterface(service);
      dispatchAndClear(runOnServiceConnected);
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      devServ = null;
    }
  };

  final Observable<IDevServ> serviceObservable =
      Observable.create(new Observable.OnSubscribe<IDevServ>() {
        @Override public void call(final Subscriber<? super IDevServ> subscriber) {
          handler.post(new Runnable() {
            @Override public void run() {
              if (subscriber.isUnsubscribed()) return;

              if (devServ != null) {
                subscriber.onNext(devServ);
                subscriber.onCompleted();
                return;
              }

              final Runnable runWithService = new Runnable() {
                @Override public void run() {
                  if (subscriber.isUnsubscribed()) return;

                  if (devServ != null) {
                    subscriber.onNext(devServ);
                    subscriber.onCompleted();
                  } else {
                    subscriber.onError(new Exception("service was unbound."));
                  }
                }
              };

              runOnServiceConnected.add(runWithService);
              if (isServiceBound) return;

              try {
                Context context = context();
                if (context == null) throw new Exception("no Context available.");
                Intent i = new Intent();
                i.setClassName(context.getString(R.string.my_vinli_package_name),
                    context.getString(R.string.device_service_component_name));
                if (!context.bindService(i, servConn, Context.BIND_AUTO_CREATE)) {
                  throw new Exception("bindService call returned false.");
                }
                Log.d(TAG, "Vinli device service successfully bound.");
                isServiceBound = true;
              } catch (Exception e) {
                Log.e(TAG, "Vinli device service bind error: " + e);
                subscriber.onError(e);
              }
            }
          });
        }
      });
}

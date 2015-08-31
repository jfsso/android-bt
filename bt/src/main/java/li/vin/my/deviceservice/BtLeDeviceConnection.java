package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import rx.functions.Func2;

/*package*/ class BtLeDeviceConnection extends BluetoothGattCallback implements DeviceConnection {
  private static final String TAG = BtLeDeviceConnection.class.getSimpleName();

  private final Map<Object, Observable<?>> paramObservables = new IdentityHashMap<>();
  private final Set<Object> ops = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

  private volatile IDevServ devServ;
  private volatile boolean isServiceBound;
  private final HashSet<Runnable> runOnServiceConnected = new HashSet<>();
  private final Cancelations cancelations = Cancelations.createGroup();

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
    contextRef = new WeakReference<>(context.getApplicationContext());
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
    return doOp("resetDtcs", null,
        new DeviceServiceFuncResetDtcs(chipId));
  }

  @NonNull @Override public <T> Observable<T> observe(@NonNull final Param<T> param) {
    final String name = Params.nameFor(param);
    if (name == null) {
      return Observable.error(new RuntimeException("unrecognized param"));
    }
    return doOp(param, name, param.getServiceFunc(chipId, name));
  }

  @NonNull @Override public Observable<SupportedPids> supportedPids() {
    return observe(Params.PIDS).map(new Func1<String, SupportedPids>() {
      @Override public SupportedPids call(String rawPids) {
        Log.i(TAG, "got raw supported pids '" + rawPids + "'");
        return new SupportedPids(rawPids);
      }
    });
  }

  private <T> Observable<T> doOp(final Object opKey, final String opLabel,
      final DeviceServiceFunc<T> func) {
    return getOrCreateOp(opKey, new ObservableFactory<T>() {
      @Override public Observable<T> create() {
        Log.d(TAG, "creating param observable for " + (opLabel == null ? opKey : opLabel));
        return serviceObservable.flatMap(func.setCancelations(cancelations))
            .retry(retryOnDisconnect)
            .doOnUnsubscribe(func.cancelOpAction)
            .onBackpressureLatest()
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                putOp(opKey);
              }
            })
            .doOnUnsubscribe(new Action0() {
              @Override public void call() {
                Log.d(TAG, "all unsubscribed from " + (opLabel == null ? opKey : opLabel));
                removeOp(opKey);
              }
            })
            .share();
      }
    });
  }

  private final Func2<Integer, Throwable, Boolean> retryOnDisconnect =
      new Func2<Integer, Throwable, Boolean>() {
        @Override public Boolean call(Integer integer, Throwable throwable) {
          Log.i(TAG, "retryOnDisconnect...");
          if (throwable instanceof ServiceDisconnectedException) {
            isServiceBound = false;
            return true;
          }
          return false;
          //if (!(throwable instanceof ServiceDisconnectedException)) return false;
          ////noinspection ThrowableResultOfMethodCallIgnored
          //boolean retry = tryBind() == null;
          //if (!retry) shutdown();
          //Log.i(TAG, "retryOnDisconnect retry " + retry);
          //return retry;
        }
      };

  public void shutdown() {
    isServiceBound = false;
    handler.removeCallbacks(shutdown);
    handler.post(shutdown);
  }

  private interface ObservableFactory<T> {
    Observable<T> create();
  }

  private <T> Observable<T> getOrCreateOp(Object opKey, ObservableFactory<T> factory) {
    Observable<T> result;
    synchronized (paramObservables) {
      Observable<?> prev = paramObservables.get(opKey);
      if (prev == null) {
        paramObservables.put(opKey, result = factory.create());
      } else {
        //noinspection unchecked
        result = (Observable<T>) prev;
      }
    }
    return result;
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
      shutdown();
    }
  }

  private final Runnable shutdown = new Runnable() {
    @Override public void run() {
      Log.e(TAG, "shutdown.");
      handler.removeCallbacks(this);
      try {
        Context context = context();
        if (context == null) throw new Exception("no Context available.");
        context.unbindService(servConn);
        Log.d(TAG, "Vinli device service successfully unbound.");
      } catch (Exception e) {
        Log.e(TAG, "Vinli device service unbind error: " + e);
      }
      isServiceBound = false;
      devServ = null;
      paramObservables.clear();
      dispatchAndClear(runOnServiceConnected);
      synchronized (ops) {
        ops.clear();
      }
      cancelations.cancelAll(new Exception("Service binding has shut down."));
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
      Log.i(TAG, "onServiceConnected");
      devServ = IDevServ.Stub.asInterface(service);
      dispatchAndClear(runOnServiceConnected);
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      Log.i(TAG, "onServiceDisconnected");
      devServ = null;
      cancelations.cancelAll(new ServiceDisconnectedException());
    }
  };

  private @Nullable Throwable tryBind() {
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
      return e;
    }
    return null;
  }

  final Observable<IDevServ> serviceObservable =
      Observable.create(new Observable.OnSubscribe<IDevServ>() {
        @Override public void call(final Subscriber<? super IDevServ> subscriber) {
          Log.i(TAG, "serviceObservable subscribed.");
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

              Throwable t = tryBind();
              if (t != null) subscriber.onError(t);
            }
          });
        }
      });

  private final class ServiceDisconnectedException extends Exception {
  }
}

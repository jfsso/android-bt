package li.vin.bt;

import android.bluetooth.BluetoothGattCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.IdentityHashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class BtLeDeviceConnection extends BluetoothGattCallback implements DeviceConnection {
  private static final String TAG = BtLeDeviceConnection.class.getSimpleName();

  private final Map<Param<?>, Observable<?>> mParamObservables = new IdentityHashMap<>();

  private final Context mContext;
  private final String mUnlockKey;

  public BtLeDeviceConnection(@NonNull Context context, @NonNull String unlockKey) {
    mContext = context;
    mUnlockKey = unlockKey;
  }

  @Override public Observable<Void> resetDtcs() {
    return serviceObservable.flatMap(new Func1<IVinliService, Observable<Void>>() {
      @Override public Observable<Void> call(final IVinliService iVinliService) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
          @Override public void call(final Subscriber<? super Void> subscriber) {
            try {
              iVinliService.resetDtcs(new IVinliServiceCallbackBool.Stub() {
                @Override public void onCompleted() throws RemoteException {
                  if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                  }
                }

                @Override public void onError(String err) throws RemoteException {
                  if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(new RuntimeException(err));
                  }
                }

                @Override public void onNext(boolean val) throws RemoteException {
                  if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(null);
                  }
                }
              });
            } catch (RemoteException e) {
              subscriber.onError(new RuntimeException("failed to reset DTCs", e));
            }
          }
        });
      }
    });
  }

  @Override public <T> Observable<T> observe(@NonNull final Param<T> param) {
    final String name = Params.nameFor(param);
    if (name == null) {
      return Observable.error(new RuntimeException("unrecognized param"));
    }

    @SuppressWarnings("unchecked")
    Observable<T> paramObservable = (Observable<T>) mParamObservables.get(param);
    if (paramObservable == null) {
      Log.d(TAG, "creating param observable for " + name);
      paramObservable = serviceObservable
        .flatMap(param.getServiceFunc(name))
        .doOnUnsubscribe(new Action0() {
          @Override public void call() {
            Log.d(TAG, "all unsubscribed from " + name);
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

  final Observable<IVinliService> serviceObservable = Observable
    .create(new Observable.OnSubscribe<IVinliService>() {
      @Override public void call(final Subscriber<? super IVinliService> subscriber) {
        final ServiceConnection serviceConnection = new ServiceConnection() {
          @Override public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "connected to device service");
            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(IVinliService.Stub.asInterface(service));
            }
          }

          @Override public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "disconnected from device service");
            if (!subscriber.isUnsubscribed()) {
              subscriber.onCompleted();
            }
          }
        };

        try {
          Log.d(TAG, "binding to device service...");
          final boolean connected = mContext.bindService(new Intent(mContext, DeviceService.class),
            serviceConnection,
            Context.BIND_AUTO_CREATE);

          if (!connected) {
            subscriber.onError(new RuntimeException("failed to connect to Vinli device service"));
          } else {
            subscriber.add(Subscriptions.create(new Action0() {
              @Override public void call() {
                Log.d(TAG, "unbinding from service due to all unsubscribing");
                mContext.unbindService(serviceConnection);
              }
            }));
          }
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    })
    .replay(1)
    .refCount();
}

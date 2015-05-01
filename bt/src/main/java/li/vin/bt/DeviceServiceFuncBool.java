package li.vin.bt;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class DeviceServiceFuncBool implements Func1<IVinliService, Observable<Boolean>> {
  private static final String TAG = DeviceServiceFuncBool.class.getSimpleName();

  private final String mName;

  public DeviceServiceFuncBool(@NonNull String name) {
    mName = name;
  }

  @Override public Observable<Boolean> call(final IVinliService iVinliService) {
    return Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override public void call(final Subscriber<? super Boolean> subscriber) {
        try {
          final IVinliServiceCallbackBool listener = new IVinliServiceCallbackBool.Stub() {
            @Override public void onCompleted() throws RemoteException {
              subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(boolean val) throws RemoteException {
              subscriber.onNext(val);
            }
          };

          iVinliService.observeBool(mName, listener);

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.unsubscribeBool(listener);
              } catch (RemoteException e) {
                Log.e(TAG, "failed to unsubscribe from " + mName, e);
              }
            }
          }));
        } catch (RemoteException e) {
          subscriber.onError(e);
        }
      }
    });
  }
}

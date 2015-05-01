package li.vin.bt;


import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class DeviceServiceFuncInt implements Func1<IVinliService, Observable<Integer>> {
  private static final String TAG = DeviceServiceFuncInt.class.getSimpleName();

  private final String mName;

  public DeviceServiceFuncInt(@NonNull String name) {
    mName = name;
  }

  @Override public Observable<Integer> call(final IVinliService iVinliService) {
    return Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override public void call(final Subscriber<? super Integer> subscriber) {
        try {
          final IVinliServiceCallbackInt listener = new IVinliServiceCallbackInt.Stub() {
            @Override public void onCompleted() throws RemoteException {
              subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(int val) throws RemoteException {
              subscriber.onNext(val);
            }
          };

          iVinliService.observeInt(mName, listener);

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.unsubscribeInt(listener);
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

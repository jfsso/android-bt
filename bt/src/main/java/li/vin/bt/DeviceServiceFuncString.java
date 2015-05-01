package li.vin.bt;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class DeviceServiceFuncString implements Func1<IVinliService, Observable<String>> {
  private static final String TAG = DeviceServiceFuncString.class.getSimpleName();

  private final String mName;

  public DeviceServiceFuncString(@NonNull String name) {
    mName = name;
  }

  @Override public Observable<String> call(final IVinliService iVinliService) {
    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(final Subscriber<? super String> subscriber) {
        try {
          final IVinliServiceCallbackString listener = new IVinliServiceCallbackString.Stub() {
            @Override public void onCompleted() throws RemoteException {
              subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(String val) throws RemoteException {
              subscriber.onNext(val);
            }
          };

          iVinliService.observeString(mName, listener);

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.unsubscribeString(listener);
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

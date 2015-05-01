package li.vin.bt;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class DeviceServiceFuncFloat implements Func1<IVinliService, Observable<Float>> {
  private static final String TAG = DeviceServiceFuncFloat.class.getSimpleName();

  private final String mName;

  public DeviceServiceFuncFloat(@NonNull String name) {
    mName = name;
  }

  @Override public Observable<Float> call(final IVinliService iVinliService) {
    return Observable.create(new Observable.OnSubscribe<Float>() {
      @Override public void call(final Subscriber<? super Float> subscriber) {
        try {
          final IVinliServiceCallbackFloat listener = new IVinliServiceCallbackFloat.Stub() {
            @Override public void onCompleted() throws RemoteException {
              subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(float val) throws RemoteException {
              subscriber.onNext(val);
            }
          };

          iVinliService.observeFloat(mName, listener);

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.unsubscribeFloat(listener);
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

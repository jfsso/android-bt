package li.vin.bt;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/*package*/ class DeviceServiceFuncDtc implements Func1<IVinliService, Observable<List<String>>> {
  private static final String TAG = DeviceServiceFuncDtc.class.getSimpleName();

  private final String mName;

  public DeviceServiceFuncDtc(@NonNull String name) {
    mName = name;
  }

  @Override public Observable<List<String>> call(final IVinliService iVinliService) {
    return Observable.create(new Observable.OnSubscribe<List<String>>() {
      @Override public void call(final Subscriber<? super List<String>> subscriber) {
        try {
          final IVinliServiceCallbackDtc listener = new IVinliServiceCallbackDtc.Stub() {
            @Override public void onCompleted() throws RemoteException {
              subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(List<String> val) throws RemoteException {
              subscriber.onNext(val);
            }
          };

          iVinliService.observeDtc(mName, listener);

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.unsubscribeDtc(listener);
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

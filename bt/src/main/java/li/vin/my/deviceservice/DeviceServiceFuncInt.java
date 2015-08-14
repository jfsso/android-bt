package li.vin.my.deviceservice;


import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

import static android.text.TextUtils.getTrimmedLength;

/*package*/ class DeviceServiceFuncInt implements Func1<IDevServ, Observable<Integer>> {
  private static final String TAG = DeviceServiceFuncInt.class.getSimpleName();

  private final String name;
  private final String chipId;

  public DeviceServiceFuncInt(@NonNull String chipId, @NonNull String name) {
    this.name = name;
    this.chipId = chipId;
  }

  @Override public Observable<Integer> call(final IDevServ iVinliService) {
    return Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override public void call(final Subscriber<? super Integer> subscriber) {
        try {
          IVinliServiceCallbackInt listener = new IVinliServiceCallbackInt.Stub() {
            @Override public void onCompleted() throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(int val) throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onNext(val);
            }
          };

          final String opUuid = iVinliService.observeInt(chipId, name, listener);
          if (opUuid == null || getTrimmedLength(opUuid) == 0) {
            throw new NullPointerException("empty UUID returned by observeInt.");
          }

          subscriber.add(Subscriptions.create(new Action0() {
            @Override public void call() {
              try {
                iVinliService.cancelOp(opUuid);
              } catch (Exception e) {
                Log.e(TAG, "failed to cancelOp " + name + " (" + opUuid + ")", e);
              }
            }
          }));
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    });
  }
}

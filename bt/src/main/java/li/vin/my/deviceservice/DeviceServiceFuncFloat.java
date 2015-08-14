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

/*package*/ class DeviceServiceFuncFloat implements Func1<IDevServ, Observable<Float>> {
  private static final String TAG = DeviceServiceFuncFloat.class.getSimpleName();

  private final String name;
  private final String chipId;

  public DeviceServiceFuncFloat(@NonNull String chipId, @NonNull String name) {
    this.name = name;
    this.chipId = chipId;
  }

  @Override public Observable<Float> call(final IDevServ iVinliService) {
    return Observable.create(new Observable.OnSubscribe<Float>() {
      @Override public void call(final Subscriber<? super Float> subscriber) {
        try {
          IVinliServiceCallbackFloat listener = new IVinliServiceCallbackFloat.Stub() {
            @Override public void onCompleted() throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            }

            @Override public void onError(String err) throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onError(new RuntimeException(err));
            }

            @Override public void onNext(float val) throws RemoteException {
              if (!subscriber.isUnsubscribed()) subscriber.onNext(val);
            }
          };

          final String opUuid = iVinliService.observeFloat(chipId, name, listener);
          if (opUuid == null || getTrimmedLength(opUuid) == 0) {
            throw new NullPointerException("empty UUID returned by observeFloat.");
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

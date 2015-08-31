package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.ref.WeakReference;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;

import static android.text.TextUtils.getTrimmedLength;

/*package*/ abstract class DeviceServiceFunc<T> implements Func1<IDevServ, Observable<T>> {
  private String TAG() {
    return getClass().getSimpleName();
  }

  protected final String name;
  protected final String chipId;
  private Cancelations cancelations;

  private String opUuid;
  private WeakReference<IDevServ> servRef = new WeakReference<>(null);

  public DeviceServiceFunc(@NonNull String chipId, @NonNull String name) {
    this.name = name;
    this.chipId = chipId;
  }

  public final DeviceServiceFunc<T> setCancelations(Cancelations cancelations) {
    synchronized (DeviceServiceFunc.this) {
      this.cancelations = cancelations;
    }
    return this;
  }

  public final Action0 cancelOpAction = new Action0() {
    @Override public void call() {
      synchronized (DeviceServiceFunc.this) {
        try {
          servRef.get().cancelOp(opUuid);
        } catch (Exception e) {
          Log.e(TAG(), "failed to cancelOp " + name + " (" + opUuid + ")", e);
        }
      }
    }
  };

  protected abstract String initOp(IDevServ iVinliService, Subscriber<? super T> subscriber)
      throws Exception;

  @Override public Observable<T> call(final IDevServ iVinliService) {
    synchronized (DeviceServiceFunc.this) {
      servRef = new WeakReference<>(iVinliService);
    }
    return Observable.create(new Observable.OnSubscribe<T>() {
      @Override public void call(final Subscriber<? super T> subscriber) {
        try {
          synchronized (DeviceServiceFunc.this) {
            opUuid = initOp(iVinliService, subscriber);
            if (opUuid == null || getTrimmedLength(opUuid) == 0) {
              throw new NullPointerException("empty UUID returned by initOp " + TAG());
            }
            if (cancelations != null) cancelations.create(subscriber);
          }
        } catch (Exception e) {
          Log.e(TAG(), "initOp failed " + name + " (" + opUuid + ")", e);
          subscriber.onError(e);
        }
      }
    });
  }
}

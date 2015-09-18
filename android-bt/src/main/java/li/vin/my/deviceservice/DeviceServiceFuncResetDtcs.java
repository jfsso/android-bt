package li.vin.my.deviceservice;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import rx.Subscriber;

/*package*/ class DeviceServiceFuncResetDtcs extends DeviceServiceFunc<Void> {

  public DeviceServiceFuncResetDtcs(@NonNull String chipId) {
    super(chipId, "resetDtcs");
  }

  @Override
  protected String initOp(IDevServ iVinliService, final Subscriber<? super Void> subscriber)
      throws Exception {
    return iVinliService.resetDtcs(chipId, new IVinliServiceCallbackBool.Stub() {
      @Override public void onCompleted() throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
      }

      @Override public void onError(String err) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onError(new Exception(err));
      }

      @Override public void onNext(boolean val) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onNext(null);
      }
    });
  }
}

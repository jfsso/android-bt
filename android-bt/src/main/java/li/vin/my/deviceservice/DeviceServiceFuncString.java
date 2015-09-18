package li.vin.my.deviceservice;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import rx.Subscriber;

/*package*/ class DeviceServiceFuncString extends DeviceServiceFunc<String> {

  public DeviceServiceFuncString(@NonNull String chipId, @NonNull String name) {
    super(chipId, name);
  }

  @Override
  protected String initOp(IDevServ iVinliService, final Subscriber<? super String> subscriber)
      throws Exception {
    return iVinliService.observeString(chipId, name, new IVinliServiceCallbackString.Stub() {
      @Override public void onCompleted() throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
      }

      @Override public void onError(String err) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onError(new Exception(err));
      }

      @Override public void onNext(String val) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onNext(val);
      }
    });
  }
}

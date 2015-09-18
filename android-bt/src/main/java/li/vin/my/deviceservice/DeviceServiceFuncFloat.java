package li.vin.my.deviceservice;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import rx.Subscriber;

/*package*/ class DeviceServiceFuncFloat extends DeviceServiceFunc<Float> {

  public DeviceServiceFuncFloat(@NonNull String chipId, @NonNull String name) {
    super(chipId, name);
  }

  @Override
  protected String initOp(IDevServ iVinliService, final Subscriber<? super Float> subscriber)
      throws Exception {
    return iVinliService.observeFloat(chipId, name, new IVinliServiceCallbackFloat.Stub() {
      @Override public void onCompleted() throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
      }

      @Override public void onError(String err) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onError(new Exception(err));
      }

      @Override public void onNext(float val) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onNext(val);
      }
    });
  }
}

package li.vin.my.deviceservice;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import java.util.List;
import rx.Subscriber;

/*package*/ class DeviceServiceFuncDtc extends DeviceServiceFunc<List<String>> {

  public DeviceServiceFuncDtc(@NonNull String chipId, @NonNull String name) {
    super(chipId, name);
  }

  @Override
  protected String initOp(IDevServ iVinliService, final Subscriber<? super List<String>> subscriber)
      throws Exception {
    return iVinliService.observeDtc(chipId, name, new IVinliServiceCallbackDtc.Stub() {
      @Override public void onCompleted() throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
      }

      @Override public void onError(String err) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onError(new Exception(err));
      }

      @Override public void onNext(List<String> val) throws RemoteException {
        if (!subscriber.isUnsubscribed()) subscriber.onNext(val);
      }
    });
  }
}

package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamAccelFloat extends ParamAccel<Float> {

  @Override Func1<IDevServ, Observable<Float>> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncFloat(chipId, name);
  }

}

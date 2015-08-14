package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamAccelBool extends ParamAccel<Boolean> {

  @Override Func1<IDevServ, Observable<Boolean>> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncBool(chipId, name);
  }

}

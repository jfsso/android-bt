package li.vin.bt;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamAccelBool extends ParamAccel<Boolean> {

  @Override Func1<IVinliService, Observable<Boolean>> getServiceFunc(@NonNull String name) {
    return new DeviceServiceFuncBool(name);
  }

}

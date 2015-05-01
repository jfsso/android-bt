package li.vin.bt;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamAccelFloat extends ParamAccel<Float> {

  @Override Func1<IVinliService, Observable<Float>> getServiceFunc(@NonNull String name) {
    return new DeviceServiceFuncFloat(name);
  }

}

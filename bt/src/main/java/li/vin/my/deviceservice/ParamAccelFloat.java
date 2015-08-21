package li.vin.my.deviceservice;

import android.support.annotation.NonNull;

/*package*/ abstract class ParamAccelFloat extends ParamAccel<Float> {

  @Override DeviceServiceFunc<Float> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncFloat(chipId, name);
  }

}

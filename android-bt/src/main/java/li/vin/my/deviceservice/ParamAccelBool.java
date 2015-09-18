package li.vin.my.deviceservice;

import android.support.annotation.NonNull;

/*package*/ abstract class ParamAccelBool extends ParamAccel<Boolean> {

  @Override DeviceServiceFunc<Boolean> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncBool(chipId, name);
  }

}

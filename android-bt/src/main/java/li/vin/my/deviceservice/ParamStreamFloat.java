package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;

/*package*/ abstract class ParamStreamFloat extends ParamStream<Float> {

  public ParamStreamFloat(String code) {
    super(code);
  }

  public ParamStreamFloat(String code, UUID uuid, boolean shouldRead) {
    super(code, uuid, shouldRead);
  }

  @Override DeviceServiceFunc<Float> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncFloat(chipId, name);
  }

}

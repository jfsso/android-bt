package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;

/*package*/ abstract class ParamStreamInt extends ParamStream<Integer> {

  public ParamStreamInt(String code) {
    super(code);
  }

  public ParamStreamInt(String code, UUID uuid, boolean shouldRead) {
    super(code, uuid, shouldRead);
  }

  @Override DeviceServiceFunc<Integer> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncInt(chipId, name);
  }

}

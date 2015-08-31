package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;

/*package*/ class PIDParam extends ParamStream<String> {

  public PIDParam(String code) {
    super(code);
  }

  public PIDParam(String code, UUID uuid, boolean shouldRead) {
    super(code, uuid, shouldRead);
  }

  @Override DeviceServiceFunc<String> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncString(chipId, name);
  }

  @Override String parseVal(String val) {
    return val == null || getCode() == null ? val : val.substring(getCode().length());
  }
}

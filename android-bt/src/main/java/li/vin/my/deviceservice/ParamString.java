package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;

/*package*/ class ParamString extends ParamPlain<String> {
  public ParamString(UUID uuid) {
    super(uuid);
  }

  public ParamString(UUID uuid, boolean hasNotifications, boolean shouldRead) {
    super(uuid, hasNotifications, shouldRead);
  }

  @Override DeviceServiceFunc<String> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncString(chipId, name);
  }

  @Override public final String parseVal(final String val) {
    return val;
  }
}

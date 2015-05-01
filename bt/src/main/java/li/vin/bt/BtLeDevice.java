package li.vin.bt;

import android.content.Context;
import android.support.annotation.NonNull;

/*package*/ final class BtLeDevice implements Device {

  public String getName() {
    return "kyle";
  }

  public DeviceConnection createDeviceConnection(@NonNull Context context, @NonNull String unlockKey) {
    return new BtLeDeviceConnection(context, unlockKey);
  }

}

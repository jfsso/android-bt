package li.vin.bt;

import android.content.Context;

public interface Device {
  String getName();
  DeviceConnection createDeviceConnection(Context context, String unlockKey);
}

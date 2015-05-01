package li.vin.bt;

import android.content.Context;

public final class VinliDevices {

  private static DeviceConnection sDeviceConn = null;

  public static final DeviceConnection createDeviceConnection(Context context, String unlockKey) {
    if (sDeviceConn == null) {
      sDeviceConn = new BtLeDeviceConnection(context, unlockKey);
    }

    return sDeviceConn;
  }

  private VinliDevices() { }
}

package li.vin.bt;

import android.content.Context;

public final class VinliDevices {

  private static BtLeDeviceConnection sDeviceConn = null;

  public static final DeviceConnection createDeviceConnection(Context context) {
    if (sDeviceConn == null) {
      sDeviceConn = new BtLeDeviceConnection(context);
    } else {
      sDeviceConn.updateContext(context);
    }

    return sDeviceConn;
  }

  private VinliDevices() { }
}

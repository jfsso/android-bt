package li.vin.bt;

import android.content.Context;
import android.os.Parcelable;

public interface Device extends Parcelable {
  String getName();
  DeviceConnection createDeviceConnection(Context context, String unlockKey);
}

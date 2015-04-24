package li.vin.bt;

import android.content.Context;
import android.os.Parcelable;

public interface Device extends Parcelable {
  public String getName();
  public DeviceConnection createDeviceConnection(Context context, String unlockKey);
}

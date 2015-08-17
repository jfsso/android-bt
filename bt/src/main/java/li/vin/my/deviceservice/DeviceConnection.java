package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import rx.Observable;

public interface DeviceConnection {
  /** Observe a given parameter. */
  @NonNull <T> Observable<T> observe(@NonNull Param<T> pid);

  /** Reset the DTCs. */
  @NonNull Observable<Void> resetDtcs();

  /** Determine which OBD-II PIDs are supported. */
  @NonNull Observable<SupportedPids> supportedPids();

  /** Get the chip ID of the current device. */
  @Nullable String chipId();

  /** Get the name of the current device. */
  @Nullable String deviceName();

  /** Get the URL for the icon image of the current device. */
  @Nullable String deviceIcon();

  /** Get the device id of the current device (valid for backend data lookup). */
  @NonNull String deviceId();
}

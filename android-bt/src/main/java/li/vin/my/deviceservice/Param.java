package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Param<Output> {

  /*package*/ Param() {
  }

  /*package*/
  abstract DeviceServiceFunc<Output> getServiceFunc(@NonNull String chipId,
      @NonNull String name);

  public @Nullable String getCode() {
    return null;
  }
}

package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import rx.Observable;
import rx.functions.Func1;

public abstract class Param<Output> {

  /*package*/ Param() {
  }

  /*package*/
  abstract Func1<IDevServ, Observable<Output>> getServiceFunc(@NonNull String chipId,
      @NonNull String name);

  public @Nullable String getCode() {
    return null;
  }
}

package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;
import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamStreamInt extends ParamStream<Integer> {

  public ParamStreamInt(String code) {
    super(code);
  }

  public ParamStreamInt(String code, UUID uuid, boolean shouldRead) {
    super(code, uuid, shouldRead);
  }

  @Override Func1<IDevServ, Observable<Integer>> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncInt(chipId, name);
  }

}

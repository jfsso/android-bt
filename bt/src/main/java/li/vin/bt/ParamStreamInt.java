package li.vin.bt;

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

  @Override Func1<IVinliService, Observable<Integer>> getServiceFunc(@NonNull String name) {
    return new DeviceServiceFuncInt(name);
  }

}

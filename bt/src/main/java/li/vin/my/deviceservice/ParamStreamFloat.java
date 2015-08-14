package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.UUID;
import rx.Observable;
import rx.functions.Func1;

/*package*/ abstract class ParamStreamFloat extends ParamStream<Float> {

  public ParamStreamFloat(String code) {
    super(code);
  }

  public ParamStreamFloat(String code, UUID uuid, boolean shouldRead) {
    super(code, uuid, shouldRead);
  }

  @Override Func1<IDevServ, Observable<Float>> getServiceFunc(@NonNull String chipId, @NonNull String name) {
    return new DeviceServiceFuncFloat(chipId, name);
  }

}

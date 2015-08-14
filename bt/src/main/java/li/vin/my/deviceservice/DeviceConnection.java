package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import rx.Observable;

public interface DeviceConnection {
  <T> Observable<T> observe(@NonNull Param<T> pid);

  Observable<Void> resetDtcs();

  Observable<SupportedPids> supportedPids();
}

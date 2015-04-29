package li.vin.bt;

import android.support.annotation.NonNull;

import rx.Observable;

public interface DeviceConnection {
  <T> Observable<T> observe(@NonNull Param<T> pid);

  Observable<Void> resetDtcs();
}

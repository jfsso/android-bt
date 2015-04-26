package li.vin.bt;

import rx.Observable;

public interface DeviceConnection {
  ObdPair getLatest(Param<?> pid);

  <T> Observable<T> observe(Param<T> pid);
}

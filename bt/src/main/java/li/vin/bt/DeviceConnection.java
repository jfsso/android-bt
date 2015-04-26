package li.vin.bt;

import rx.Observable;

public interface DeviceConnection {
  <T> Observable<T> observe(Param<T> pid);
}

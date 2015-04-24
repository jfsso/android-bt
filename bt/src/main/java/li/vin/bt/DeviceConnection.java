package li.vin.bt;

import rx.Observable;

public interface DeviceConnection {
  ObdPair getLatest(Param<?, ?> pid);

  <T, P> Observable<T> observe(Param<T, P> pid);
}

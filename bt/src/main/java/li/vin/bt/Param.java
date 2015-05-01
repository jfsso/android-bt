package li.vin.bt;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func1;

public abstract class Param<Output> {
  /*package*/ Param() { }

  /*package*/ abstract Func1<IVinliService, Observable<Output>> getServiceFunc(@NonNull String name);
}

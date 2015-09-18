package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by christophercasey on 8/18/15.
 */
/*package*/ final class Cancelations {

  /*package*/ static abstract class Cancelation {
    public abstract void cancel(@NonNull Throwable t);
    private Cancelation() {
    }
  }

  private final Set<Cancelation> cancelations =
      Collections.newSetFromMap(new ConcurrentHashMap<Cancelation, Boolean>());

  /*package*/ void cancelAll(@NonNull Throwable t) {
    for (Cancelation c : new HashSet<>(cancelations)) c.cancel(t);
  }

  /*package*/ Cancelation create(@NonNull final Subscriber<?> s) {
    final Cancelation c = new Cancelation() {
      @Override public void cancel(@NonNull Throwable t) {
        cancelations.remove(this);
        if (!s.isUnsubscribed()) {
          s.onError(t);
        }
      }
    };
    cancelations.add(c);
    s.add(Subscriptions.create(new Action0() {
      @Override public void call() {
        cancelations.remove(c);
      }
    }));
    return c;
  }

  public static Cancelations createGroup() {
    return new Cancelations();
  }

  private Cancelations() {
  }
}

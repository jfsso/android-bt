package li.vin.bt;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.charset.Charset;
import java.util.UUID;

public abstract class Param<T, P> {
  protected static final Charset ASCII = Charset.forName("ASCII");

  /*package*/ final UUID uuid;
  /*package*/ final boolean hasNotifications;
  /*package*/ final boolean shouldRead;

  /*package*/ Param(UUID uuid) {
    this(uuid, true, false);
  }

  /*package*/ Param(UUID uuid, boolean hasNotifications, boolean shouldRead) {
    this.uuid = uuid;
    this.hasNotifications = hasNotifications;
    this.shouldRead = shouldRead;
  }

  /*package*/ Boolean matches(final P val) {
    return Boolean.TRUE;
  }

  /*package*/ abstract T parseVal(final P val);

  /*package*/ abstract P parseCharacteristic(final BluetoothGattCharacteristic characteristic);
}

package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCharacteristic;
import java.nio.charset.Charset;
import java.util.UUID;

/*package*/ abstract class ParamImpl<Output, Input> extends Param<Output> {
  protected static final Charset ASCII = Charset.forName("ASCII");

  // CHECKSTYLE.OFF: VisibilityModifier
  /*package*/ final UUID uuid;
  /*package*/ final boolean hasNotifications;
  /*package*/ final boolean shouldRead;
  // CHECKSTYLE.ON: VisibilityModifier

  /*package*/ ParamImpl(UUID uuid) {
    this(uuid, true, false);
  }

  /*package*/ ParamImpl(UUID uuid, boolean hasNotifications, boolean shouldRead) {
    this.uuid = uuid;
    this.hasNotifications = hasNotifications;
    this.shouldRead = shouldRead;
  }

  /*package*/ Boolean matches(final Input val) {
    return Boolean.TRUE;
  }

  /*package*/ abstract Output parseVal(final Input val);

  /*package*/ abstract Input parseCharacteristic(final BluetoothGattCharacteristic characteristic);
}

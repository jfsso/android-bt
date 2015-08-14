package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCharacteristic;

/*package*/ abstract class ParamAccel<T> extends ParamImpl<T, byte[]> {
  protected static final float ACCEL_CONVERT = 9.81f / 64;

  public ParamAccel() {
    super(Uuids.ACCEL, true, true);
  }

  @Override public byte[] parseCharacteristic(final BluetoothGattCharacteristic characteristic) {
    final byte[] val = characteristic.getValue();
    if (val == null) {
      throw new RuntimeException("val == null");
    }

    return val;
  }
}

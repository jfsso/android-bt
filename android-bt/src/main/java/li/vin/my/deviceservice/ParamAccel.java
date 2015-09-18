package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCharacteristic;

/*package*/ abstract class ParamAccel<T> extends ParamImpl<T, byte[]> {
  private static final float ACCEL_CONVERT_TWOBYTE = (9.807f / 16384f);
  private static final float ACCEL_CONVERT_ONEBYTE = (9.807f / 64f);

  protected static float accelConvert(byte[] bytes, int idx) {
    if (bytes.length == 14) {
      String byteStr = new String(bytes, idx * 4, 4, ASCII);
      return Integer.valueOf(byteStr, 16).shortValue() * ACCEL_CONVERT_TWOBYTE;
    } else if (bytes.length == 4) {
      return bytes[idx] * ACCEL_CONVERT_ONEBYTE;
    }
    throw new RuntimeException("unknown accelerometer format.");
  }

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

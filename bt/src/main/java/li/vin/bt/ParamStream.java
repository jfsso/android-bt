package li.vin.bt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/*package*/ abstract class ParamStream<T> extends Param<T, String> {
  protected static final int HEX = 16;

  private final String mCode;

  public ParamStream(String code) {
    this(code, Uuids.STREAM);
  }

  public ParamStream(String code, UUID uuid) {
    this(code, uuid, false);
  }

  public ParamStream(String code, UUID uuid, boolean shouldRead) {
    super(uuid, true, shouldRead);
    mCode = code;
  }

  @Override public String parseCharacteristic(BluetoothGattCharacteristic characteristic) {
    final byte[] val = characteristic.getValue();
    if (val == null) {
      throw new RuntimeException("val == null");
    }

//      Log.d("parseCharacteristic", new String(val, 0, val.length, ASCII));

    if (val[0] != '4' || val[1] != '1') {
      throw new RuntimeException("streaming characteristic did not start with 41. Was instead: " + new String(val, 0, 2, ASCII));
    }

    int valLen = 0;
    for (int i = 0; i < val.length; i++) {
      final byte c = val[i];
      if (c == '\r' || c == 0) {
        break;
      }
      ++valLen;
    }

    if (valLen == 0) {
      throw new RuntimeException("valLen == 0");
    }

    return new String(val, 2, valLen - 2, ASCII);
  }

  @Override public Boolean matches(final String val) {
    Log.i("StreamPid", "matching " + mCode + " against " + val);
    return val.startsWith(mCode) ? Boolean.TRUE : Boolean.FALSE;
  }
}

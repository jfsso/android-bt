package li.vin.bt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/*package*/ abstract class ParamStream<T> extends ParamImpl<T, String> {
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

    int valStart = 2;
    if (val[0] != '4' || val[1] != '1') {
      if (val[0] == '\n' && val[1] == '4' && val[2] == '1') {
        Log.d("ParamStream", "first char was newline");
        valStart += 1;
      } else {
        throw new RuntimeException("streaming characteristic did not start with 41. Was instead: " + new String(val, 0, val.length, ASCII));
      }
    }

    int valLen = 0;
    for (int i = valStart; i < val.length; i++) {
      final byte c = val[i];
      if (c == '\r' || c == 0) {
        break;
      }
      ++valLen;
    }

    if (valLen == 0) {
      throw new RuntimeException("valLen == 0");
    }

    return new String(val, valStart, valLen, ASCII);
  }

  @Override public Boolean matches(final String val) {
    Log.i("StreamPid", "matching " + mCode + " against " + val);
    return val.startsWith(mCode) ? Boolean.TRUE : Boolean.FALSE;
  }
}

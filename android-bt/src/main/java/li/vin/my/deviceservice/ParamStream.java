package li.vin.my.deviceservice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;
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
    if (val.length == 0) {
      throw new RuntimeException("val empty.");
    }

    Log.d("parseCharacteristic ("+characteristic.getUuid()+")", bytesToHex(val) + " : " + new String(val, 0, val.length, ASCII));//new String(val, 0, val.length, ASCII));

    // trim empties from start
    int stt = 0;
    while (stt < val.length && (Character.isWhitespace(val[stt]) || val[stt] == 0)) {
      stt++;
    }

    // trim empties from end
    int end = val.length-1;
    while (end >= 0 && (Character.isWhitespace(val[end]) || val[end] == 0)) {
      end--;
    }
    end++;

    // validate there's still a nonempty value
    int len = end - stt;
    if (len < 2) {
      throw new RuntimeException("trimmed len < 2.");
    }

    if (val[stt] == '4' && val[stt+1] == '1') {
      if (end > stt+2) {
        return new String(val, stt + 2, end - (stt + 2), ASCII);
      } else {
        throw new RuntimeException("empty streaming characteristic " + characteristic.getUuid());
      }
    } else if (len >= 5 && val[stt] == 'S' && val[stt+1] == 'V' && val[stt+2] == 'E' &&
        val[stt+3] == 'R' && val[stt+4] == ':') {
      if (end > stt+5) {
        return "SVER:" + new String(val, stt + 5, end - (stt + 5), ASCII);
      } else {
        throw new RuntimeException("empty streaming characteristic " + characteristic.getUuid());
      }
    } else if (val[stt] == 'B' && val[stt+1] == ':') {
      if (end > stt+2) {
        return "B:" + new String(val, stt + 2, end - (stt + 2), ASCII);
      } else {
        throw new RuntimeException("empty streaming characteristic " + characteristic.getUuid());
      }
    } else if (val[stt] == 'S' && val[stt+1] == ':') {
      if (end > stt+2) {
        return "S:" + new String(val, stt + 2, end - (stt + 2), ASCII);
      } else {
        throw new RuntimeException("empty streaming characteristic " + characteristic.getUuid());
      }
    } else if (val[stt] == 'P' && val[stt+1] == '0') {
      return "P0";
    } else if (val[stt] == 'P' && val[stt+1] == '1') {
      return "P1";
    } else if (val[stt] == 'C' && val[stt+1] == '0') {
      return "C0";
    } else if (val[stt] == 'C' && val[stt+1] == '1') {
      return "C1";
    } else if (val[stt] == 'G' && val[stt+1] == '0') {
      return "G0";
    } else if (val[stt] == 'G' && val[stt+1] == '1') {
      return "G1";
    } else {
      throw new RuntimeException("unknown streaming characteristic("+
          new String(val, stt, end - stt, ASCII)+") " +
          characteristic.getUuid());
    }

    //int valStart = 2;
    //
    //if (val[0] != '4' || val[1] != '1') {
    //  if (val[0] == '\n' && val[1] == '4' && val[2] == '1') {
    //    Log.d("ParamStream", "first char was newline");
    //    valStart += 1;
    //  } else if (val[0] == 'P' && (val[1] == '0' || val[1] == '1')) {
    //    return val[1] == '0' ? "P0" : "P1";
    //  } else {
    //    throw new RuntimeException("streaming characteristic did not start with 41. Was instead: " + new String(val, 0, val.length, ASCII));
    //  }
    //}
    //
    //int valLen = 0;
    //for (int i = valStart; i < val.length; i++) {
    //  final byte c = val[i];
    //  if (c == '\r' || c == 0) {
    //    break;
    //  }
    //  ++valLen;
    //}
    //
    //if (valLen == 0) {
    //  throw new RuntimeException("valLen == 0");
    //}
    //
    //return new String(val, valStart, valLen, ASCII);
  }

  @Override public Boolean matches(final String val) {
    //    Log.i("StreamPid", "matching " + mCode + " against " + val);
    return val.startsWith(mCode) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override public @Nullable String getCode() {
    return mCode;
  }

  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}

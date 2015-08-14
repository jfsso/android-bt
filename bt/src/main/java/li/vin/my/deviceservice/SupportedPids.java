package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by christophercasey on 8/5/15.
 */
public final class SupportedPids {

  public static class InvalidParamException extends Exception {
    private InvalidParamException(String msg) {
      super(msg);
    }
  }

  private final String raw;
  private HashMap<String,Boolean> supportMap;

  /*package*/ SupportedPids(@NonNull String raw) {
    this.raw = raw;
  }

  public <T extends Param<?>> boolean supports(@NonNull T param) throws InvalidParamException {
    String code = param.getCode();
    if (code == null) throw new InvalidParamException("Must provide Param with nonnull code.");
    code = code.toLowerCase(Locale.US);
    if (supportMap == null) buildSupportMap();
    Boolean result = supportMap.get(code);
    return result == null ? false : result;
  }

  @Override public String toString() {
    if (supportMap == null) buildSupportMap();
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String,Boolean> entry : supportMap.entrySet()) {
      String key = entry.getKey();
      Boolean val = entry.getValue();
      if (sb.length() != 0) sb.append("::");
      sb.append("key='")
          .append(key)
          .append("',val='")
          .append(val)
          .append("'");
    }
    return sb.toString();
  }

  private void buildSupportMap() {
    supportMap = new HashMap<>();
    ArrayList<String> groups = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<raw.length(); i++) {
      if (sb.length() == 8) {
        groups.add(sb.toString());
        sb.delete(0, 8);
      }
      sb.append(raw.charAt(i));
    }
    if (sb.length() == 8) {
      groups.add(sb.toString());
    }
    for (int i=0; i<groups.size(); i++) {
      parseBitflags(i, groups.get(i));
    }
  }

  private void parseBitflags(int group, String bitflags) {
    int groupStart = group * 32 + 1;
    for (int i=0; i<8; i++) {
      int j = i * 4;
      int hexInt = Integer.parseInt(bitflags.substring(i, i+1), 16);
      String bin = Integer.toBinaryString(hexInt);
      while (bin.length() < 4) bin = '0' + bin;
      putIntoMap(groupStart + j, bin.charAt(0) == '1');
      putIntoMap(groupStart + j + 1, bin.charAt(1) == '1');
      putIntoMap(groupStart + j + 2, bin.charAt(2) == '1');
      putIntoMap(groupStart + j + 3, bin.charAt(3) == '1');
    }
  }

  private void putIntoMap(int index, boolean flag) {
    String hex = Integer.toHexString(index).toLowerCase(Locale.US);
    while (hex.length() < 2) hex = '0' + hex;
    supportMap.put(hex, flag);
  }
}

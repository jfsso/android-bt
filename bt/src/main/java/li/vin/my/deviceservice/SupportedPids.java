package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by christophercasey on 8/5/15.
 */
public final class SupportedPids {

  private static final Set<String> KNOWN_UNSUPPORTED_PIDS;
  static {
    KNOWN_UNSUPPORTED_PIDS = new HashSet<>();

    // markers for supported pids
    KNOWN_UNSUPPORTED_PIDS.add("00");
    KNOWN_UNSUPPORTED_PIDS.add("20");
    KNOWN_UNSUPPORTED_PIDS.add("40");
    KNOWN_UNSUPPORTED_PIDS.add("60");
    KNOWN_UNSUPPORTED_PIDS.add("80");

    // dtcs
    KNOWN_UNSUPPORTED_PIDS.add("01");

    // accelerometer
    KNOWN_UNSUPPORTED_PIDS.add("0c");

    // >2 byte pids from https://en.wikipedia.org/wiki/OBD-II_PIDs#Standard_PIDs
    KNOWN_UNSUPPORTED_PIDS.add("24");
    KNOWN_UNSUPPORTED_PIDS.add("25");
    KNOWN_UNSUPPORTED_PIDS.add("26");
    KNOWN_UNSUPPORTED_PIDS.add("27");
    KNOWN_UNSUPPORTED_PIDS.add("28");
    KNOWN_UNSUPPORTED_PIDS.add("29");
    KNOWN_UNSUPPORTED_PIDS.add("2a");
    KNOWN_UNSUPPORTED_PIDS.add("2b");

    KNOWN_UNSUPPORTED_PIDS.add("34");
    KNOWN_UNSUPPORTED_PIDS.add("35");
    KNOWN_UNSUPPORTED_PIDS.add("36");
    KNOWN_UNSUPPORTED_PIDS.add("37");
    KNOWN_UNSUPPORTED_PIDS.add("38");
    KNOWN_UNSUPPORTED_PIDS.add("39");
    KNOWN_UNSUPPORTED_PIDS.add("3a");
    KNOWN_UNSUPPORTED_PIDS.add("3b");

    KNOWN_UNSUPPORTED_PIDS.add("41");
    KNOWN_UNSUPPORTED_PIDS.add("4f");
    KNOWN_UNSUPPORTED_PIDS.add("50");

    KNOWN_UNSUPPORTED_PIDS.add("64");
    KNOWN_UNSUPPORTED_PIDS.add("66");
    KNOWN_UNSUPPORTED_PIDS.add("67");
    KNOWN_UNSUPPORTED_PIDS.add("68");
    KNOWN_UNSUPPORTED_PIDS.add("69");
    KNOWN_UNSUPPORTED_PIDS.add("6a");
    KNOWN_UNSUPPORTED_PIDS.add("6b");
    KNOWN_UNSUPPORTED_PIDS.add("6c");
    KNOWN_UNSUPPORTED_PIDS.add("6d");
    KNOWN_UNSUPPORTED_PIDS.add("6e");
    KNOWN_UNSUPPORTED_PIDS.add("6f");

    KNOWN_UNSUPPORTED_PIDS.add("70");
    KNOWN_UNSUPPORTED_PIDS.add("71");
    KNOWN_UNSUPPORTED_PIDS.add("72");
    KNOWN_UNSUPPORTED_PIDS.add("73");
    KNOWN_UNSUPPORTED_PIDS.add("74");
    KNOWN_UNSUPPORTED_PIDS.add("75");
    KNOWN_UNSUPPORTED_PIDS.add("76");
    KNOWN_UNSUPPORTED_PIDS.add("77");
    KNOWN_UNSUPPORTED_PIDS.add("78");
    KNOWN_UNSUPPORTED_PIDS.add("79");
    KNOWN_UNSUPPORTED_PIDS.add("7a");
    KNOWN_UNSUPPORTED_PIDS.add("7b");
    KNOWN_UNSUPPORTED_PIDS.add("7c");
    KNOWN_UNSUPPORTED_PIDS.add("7f");

    KNOWN_UNSUPPORTED_PIDS.add("81");
    KNOWN_UNSUPPORTED_PIDS.add("82");
    KNOWN_UNSUPPORTED_PIDS.add("83");

    KNOWN_UNSUPPORTED_PIDS.add("a0");
    KNOWN_UNSUPPORTED_PIDS.add("c0");
  }

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

  public String getRaw() {
    return raw;
  }

  public <T extends Param<?>> boolean supports(@NonNull T param) throws InvalidParamException {
    String code = param.getCode();
    if (code == null) throw new InvalidParamException("Must provide Param with nonnull code.");
    code = code.toLowerCase(Locale.US);
    if (KNOWN_UNSUPPORTED_PIDS.contains(code)) return false;
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

package li.vin.bt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Params {
  public static final Param<Float> ACCEL_X = new ParamAccel<Float>() {
    @Override public Float parseVal(byte[] val) {
      return val[0] * ACCEL_CONVERT;
    }
  };

  public static final Param<Float> ACCEL_Y = new ParamAccel<Float>() {
    @Override public Float parseVal(byte[] val) {
      return val[1] * ACCEL_CONVERT;
    }
  };

  public static final Param<Float> ACCEL_Z = new ParamAccel<Float>() {
    @Override public Float parseVal(byte[] val) {
      return val[2] * ACCEL_CONVERT;
    }
  };

  public static final Param<Float> AIR_FLOW = new ParamStream<Float>("10") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(0, 2), HEX);
      final int b = Integer.parseInt(val.substring(2, 4), HEX);

      return (a * 256f + b) / 100f;
    }
  };

  public static final Param<String> CHIP_ID = new ParamString(Uuids.CHIP_ID, false, true);

  public static final Param<Boolean> COLLISION = new ParamAccel<Boolean>() {
    @Override public Boolean parseVal(byte[] val) {
      return val[3] == 1 ? Boolean.TRUE : Boolean.FALSE;
    }
  };

  public static final Param<Float> COOLANT_TEMP_C = new ParamStream<Float>("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val, HEX);

      return a - 40f;
    }
  };

  public static final Param<Float> COOLANT_TEMP_F = new ParamStream<Float>("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val, HEX);

      return (a - 40) * 1.8f + 32;
    }
  };

  public static final Param<List<String>> DTC_CODES = new ParamPlain<List<String>>(Uuids.DTCS, false, true) {
    @Override public List<String> parseVal(final String val) {
      if (val == null) {
        return Collections.emptyList();
      }

      return Arrays.asList(val.split(",")); // remove "D:" from beginning
    }
  };

  public static final Param<Integer> ENGINE_RUNTIME = new ParamStream<Integer>("1F") {
    @Override public Integer parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(0, 2), HEX);
      final int b = Integer.parseInt(val.substring(2, 4), HEX);

      return (a * 256) + b;
    }
  };

  public static final Param<Float> FUEL_LEVEL = new ParamStream<Float>("2F") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.valueOf(val, HEX);

      return (a * 100f) / 255f;
    }
  };

  public static final Param<Float> ENGINE_LOAD = new ParamStream<Float>("04") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);

      return a * 100f / 255f;
    }
  };

  public static final Param<Float> RPM = new ParamStream<Float>("0C", Uuids.RPM, true) {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256f) + b) / 4f;
    }
  };

  public static final Param<Integer> SPEED_KPH = new ParamStream<Integer>("0D") {
    @Override public Integer parseVal(final String val) {
      return Integer.valueOf(val.substring(2), HEX); // remove 0D from beginning
    }
  };

  public static final Param<Integer> SPEED_MPH = new ParamStream<Integer>("0D") {
    private static final float KPH_TO_MPH = 0.621371f;

    @Override public Integer parseVal(final String val) {
      return Math.round(Integer.parseInt(val.substring(2), HEX) * KPH_TO_MPH); // remove 0D from beginning
    }
  };

  public static final Param<String> VIN = new ParamString(Uuids.VIN, false, true);

  private Params() { }
}

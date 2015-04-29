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

  /**
   * Calculated Load Value<br/>
   * units: %
   */
  public static final Param<Float> CALCULATED_LOAD_VALUE = new ParamStream<Float>("04") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return (a * 100) / 255f;
    }
  };

  public static final Param<String> CHIP_ID = new ParamString(Uuids.CHIP_ID, false, true);

  public static final Param<Boolean> COLLISION = new ParamAccel<Boolean>() {
    @Override public Boolean parseVal(byte[] val) {
      return val[3] == 1 ? Boolean.TRUE : Boolean.FALSE;
    }
  };

  /**
   * Control Module Voltage<br/>
   * units: V
   */
  public static final Param<Float> CONTROL_MODULE_VOLTAGE = new ParamStream<Float>("42") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 1000f;
    }
  };

  /**
   * Coolant temperature<br/>
   * units: °C
   */
  public static final Param<Float> COOLANT_TEMP_C = new ParamStream<Float>("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return a - 40f;
    }
  };

  /**
   * Coolant temperature<br/>
   * units: °F
   */
  public static final Param<Float> COOLANT_TEMP_F = new ParamStream<Float>("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return ((a - 40) * 1.8f) + 32;
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

  /**
   * Fuel level input<br/>
   * units: %
   */
  public static final Param<Float> FUEL_LEVEL_INPUT = new ParamStream<Float>("2F") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.valueOf(val.substring(2), HEX);

      return (a * 100) / 255f;
    }
  };

  /**
   * Mass Airflow<br/>
   * units: g/s
   */
  public static final Param<Float> MASS_AIRFLOW = new ParamStream<Float>("10") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 100f;
    }
  };

  /**
   * Oxygen Sensor Bank 1 - sensor 1 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1A_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("24");

  /**
   * Oxygen Sensor Bank 1 - sensor 1 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_1A_VOLTAGE = new ParamO2sVoltage("24");

  /**
   * Oxygen Sensor Bank 1 - sensor 2 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1B_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("25");

  /**
   * Oxygen Sensor Bank 1 - sensor 2 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_1B_VOLTAGE = new ParamO2sVoltage("25");

  /**
   * Oxygen Sensor Bank 1 - sensor 3 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1C_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("26");

  /**
   * Oxygen Sensor Bank 1 - sensor 3 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_1C_VOLTAGE = new ParamO2sVoltage("26");

  /**
   * Oxygen Sensor Bank 1 - sensor 4 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1D_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("27");

  /**
   * Oxygen Sensor Bank 1 - sensor 4 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_1D_VOLTAGE = new ParamO2sVoltage("27");

  /**
   * Oxygen Sensor Bank 2 - sensor 1 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2A_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("28");

  /**
   * Oxygen Sensor Bank 2 - sensor 1 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_2A_VOLTAGE = new ParamO2sVoltage("28");

  /**
   * Oxygen Sensor Bank 2 - sensor 2 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2B_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("29");

  /**
   * Oxygen Sensor Bank 2 - sensor 2 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_2B_VOLTAGE = new ParamO2sVoltage("29");

  /**
   * Oxygen Sensor Bank 2 - sensor 3 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2C_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("2A");

  /**
   * Oxygen Sensor Bank 2 - sensor 3 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_2C_VOLTAGE = new ParamO2sVoltage("2A");

  /**
   * Oxygen Sensor Bank 2 - sensor 4 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2D_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("2B");

  /**
   * Oxygen Sensor Bank 2 - sensor 4 (wide range 02S): Voltage<br/>
   * units: V
   */
  public static final Param<Float> O2S_2D_VOLTAGE = new ParamO2sVoltage("2B");

  /**
   * Revolutions per Minute<br/>
   * units: r/m
   */
  public static final Param<Float> RPM = new ParamStream<Float>("0C", Uuids.RPM, true) {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 4f;
    }
  };

  /**
   * Runtime since engine start<br/>
   * units: s
   */
  public static final Param<Integer> RUNTIME_SINCE_ENGINE_START = new ParamStream<Integer>("1F") {
    @Override public Integer parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(0, 2), HEX);
      final int b = Integer.parseInt(val.substring(2, 4), HEX);

      return (a * 256) + b;
    }
  };

  /**
   * Vehicle speed<br/>
   * units: km/h
   */
  public static final Param<Integer> SPEED_KPH = new ParamStream<Integer>("0D") {
    @Override public Integer parseVal(final String val) {
      return Integer.valueOf(val.substring(2), HEX); // remove 0D from beginning
    }
  };

  /**
   * Vehicle speed<br/>
   * units: m/h
   */
  public static final Param<Integer> SPEED_MPH = new ParamStream<Integer>("0D") {
    private static final float KPH_TO_MPH = 0.621371f;

    @Override public Integer parseVal(final String val) {
      return Math.round(Integer.parseInt(val.substring(2), HEX) * KPH_TO_MPH); // remove 0D from beginning
    }
  };

  public static final Param<String> VIN = new ParamString(Uuids.VIN, false, true);

  private Params() { }
}

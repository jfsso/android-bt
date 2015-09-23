package li.vin.my.deviceservice;

import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Params {

  public static final Param<String> ACCEL_RAW = new ParamPlain<String>(Uuids.ACCEL, true, false) {
    @Override
    DeviceServiceFunc<String> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncString(chipId, name);
    }

    @Override
    String parseVal(String val) {
      return val;
    }
  };

  public static final Param<Float> ACCEL_X = new ParamAccelFloat() {
    @Override public Float parseVal(byte[] val) {
      return accelConvert(val, 0);
    }
  };

  public static final Param<Float> ACCEL_Y = new ParamAccelFloat() {
    @Override public Float parseVal(byte[] val) {
      return accelConvert(val, 1);
    }
  };

  public static final Param<Float> ACCEL_Z = new ParamAccelFloat() {
    @Override public Float parseVal(byte[] val) {
      return accelConvert(val, 2);
    }
  };

  /**
   * Calculated Load Value<br>
   * units: %
   */
  public static final Param<Float> CALCULATED_LOAD_VALUE = new ParamStreamFloat("04") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return (a * 100) / 255f;
    }
  };

  public static final Param<String> CHIP_ID = new ParamString(Uuids.CHIP_ID, false, true);

  public static final Param<Boolean> COLLISION = new ParamAccelBool() {
    @Override public Boolean parseVal(byte[] val) {
      return !(new String(val, val.length-1, 1, ASCII).equals("0")) ? Boolean.TRUE : Boolean.FALSE;
    }
  };

  /**
   * Control Module Voltage<br>
   * units: V
   */
  public static final Param<Float> CONTROL_MODULE_VOLTAGE = new ParamStreamFloat("42") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 1000f;
    }
  };

  /**
   * Coolant temperature<br>
   * units: °C
   */
  public static final Param<Float> COOLANT_TEMP_C = new ParamStreamFloat("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return a - 40f;
    }
  };

  /**
   * Coolant temperature<br>
   * units: °F
   */
  public static final Param<Float> COOLANT_TEMP_F = new ParamStreamFloat("05") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2), HEX);

      return ((a - 40) * 1.8f) + 32;
    }
  };

  public static final Param<List<String>> DTCS = new ParamPlain<List<String>>(Uuids.DTCS, false, true) {
    @Override public List<String> parseVal(final String val) {
      if (val == null) {
        return Collections.emptyList();
      }

      return Arrays.asList(val.split(",")); // remove "D:" from beginning
    }

    @Override DeviceServiceFunc<List<String>> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncDtc(chipId, name);
    }
  };

  /**
   * Fuel level input<br>
   * units: %
   */
  public static final Param<Float> FUEL_LEVEL_INPUT = new ParamStreamFloat("2F") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.valueOf(val.substring(2), HEX);

      return (a * 100) / 255f;
    }
  };

  /**
   * Mass Airflow<br>
   * units: g/s
   */
  public static final Param<Float> MASS_AIRFLOW = new ParamStreamFloat("10") {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 100f;
    }
  };

  public static final Param<String> MASS_AIRFLOW_DOS = new ParamStream<String>("66") {
    @Override
    DeviceServiceFunc<String> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncString(chipId, name);
    }

    @Override
    String parseVal(String val) {
      return val;
    }
  };

  /**
   * Oxygen Sensor Bank 1 - sensor 1 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1A_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("24");

  /**
   * Oxygen Sensor Bank 1 - sensor 1 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_1A_VOLTAGE = new ParamO2sVoltage("24");

  /**
   * Oxygen Sensor Bank 1 - sensor 2 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1B_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("25");

  /**
   * Oxygen Sensor Bank 1 - sensor 2 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_1B_VOLTAGE = new ParamO2sVoltage("25");

  /**
   * Oxygen Sensor Bank 1 - sensor 3 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1C_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("26");

  /**
   * Oxygen Sensor Bank 1 - sensor 3 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_1C_VOLTAGE = new ParamO2sVoltage("26");

  /**
   * Oxygen Sensor Bank 1 - sensor 4 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_1D_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("27");

  /**
   * Oxygen Sensor Bank 1 - sensor 4 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_1D_VOLTAGE = new ParamO2sVoltage("27");

  /**
   * Oxygen Sensor Bank 2 - sensor 1 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2A_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("28");

  /**
   * Oxygen Sensor Bank 2 - sensor 1 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_2A_VOLTAGE = new ParamO2sVoltage("28");

  /**
   * Oxygen Sensor Bank 2 - sensor 2 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2B_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("29");

  /**
   * Oxygen Sensor Bank 2 - sensor 2 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_2B_VOLTAGE = new ParamO2sVoltage("29");

  /**
   * Oxygen Sensor Bank 2 - sensor 3 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2C_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("2A");

  /**
   * Oxygen Sensor Bank 2 - sensor 3 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_2C_VOLTAGE = new ParamO2sVoltage("2A");

  /**
   * Oxygen Sensor Bank 2 - sensor 4 (wide range O2S): Equivalence Ratio
   */
  public static final Param<Float> O2S_2D_EQUIVALENCE_RATIO = new ParamO2sEquivalenceRatio("2B");

  /**
   * Oxygen Sensor Bank 2 - sensor 4 (wide range 02S): Voltage<br>
   * units: V
   */
  public static final Param<Float> O2S_2D_VOLTAGE = new ParamO2sVoltage("2B");

  /**
   * Revolutions per Minute<br>
   * units: r/m
   */
  public static final Param<Float> RPM = new ParamStreamFloat("0C", Uuids.RPM, false) {
    @Override public Float parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(2, 4), HEX);
      final int b = Integer.parseInt(val.substring(4, 6), HEX);

      return ((a * 256) + b) / 4f;
    }
  };

  /**
   * Runtime since engine start<br>
   * units: s
   */
  public static final Param<Integer> RUNTIME_SINCE_ENGINE_START = new ParamStreamInt("1F") {
    @Override public Integer parseVal(final String val) {
      final int a = Integer.parseInt(val.substring(0, 2), HEX);
      final int b = Integer.parseInt(val.substring(2, 4), HEX);

      return (a * 256) + b;
    }
  };

  /**
   * Vehicle speed<br>
   * units: km/h
   */
  public static final Param<Integer> SPEED_KPH = new ParamStreamInt("0D") {
    @Override public Integer parseVal(final String val) {
      return Integer.valueOf(val.substring(2), HEX); // remove 0D from beginning
    }
  };

  /**
   * Vehicle speed<br>
   * units: m/h
   */
  public static final Param<Integer> SPEED_MPH = new ParamStreamInt("0D") {
    private static final float KPH_TO_MPH = 0.621371f;

    @Override public Integer parseVal(final String val) {
      return Math.round(Integer.parseInt(val.substring(2), HEX) * KPH_TO_MPH); // remove 0D from beginning
    }
  };

  public static final Param<String> VIN = new ParamString(Uuids.VIN, false, true);

  public static final Param<String> PIDS = new ParamString(Uuids.PIDS, false, true);

  public static final ParamStream<Boolean> POWER_STATUS = new ParamStream<Boolean>("P") {
    @Override Boolean parseVal(String val) {
      return val.contains("1") ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override DeviceServiceFunc<Boolean> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncBool(chipId, name);
    }
  };

  public static final ParamStream<Boolean> CONNECTION_STATUS = new ParamStream<Boolean>("C") {
    @Override Boolean parseVal(String val) {
      return val.contains("1") ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override DeviceServiceFunc<Boolean> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncBool(chipId, name);
    }
  };

  public static final ParamStream<Boolean> GPS_STATUS = new ParamStream<Boolean>("G") {
    @Override Boolean parseVal(String val) {
      return val.contains("1") ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override DeviceServiceFunc<Boolean> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncBool(chipId, name);
    }
  };

  public static final Param<String> CONNECTION_STRENGTH = new ParamStream<String>("S:") {
    @Override
    DeviceServiceFunc<String> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncString(chipId, name);
    }

    @Override
    String parseVal(String val) {
      return val;
    }
  };

  public static final ParamStream<Float> BATTERY_VOLTAGE = new ParamStream<Float>("B:") {
    @Override Float parseVal(String val) {
      return Integer.valueOf(val.substring(2), HEX) * 0.006f;
    }

    @Override DeviceServiceFunc<Float> getServiceFunc(@NonNull String chipId, @NonNull String name) {
      return new DeviceServiceFuncFloat(chipId, name);
    }
  };

  private static final ConcurrentHashMap<String,PIDParam> pidParams =
      new ConcurrentHashMap<>();
  public static Param<String> getPidParam(String code) {
    PIDParam param = pidParams.get(code);
    if (param == null) {
      param = new PIDParam(code);
      PIDParam old = pidParams.putIfAbsent(code, param);
      param = old != null ? old : param;
    }
    return param;
  }

  /*package*/ @SuppressWarnings("unchecked") static <T> ParamImpl<T, ?> paramFor(@NonNull String name) throws RuntimeException {
    try {
      if (name.startsWith("PIDParam")) {
        return (ParamImpl<T, ?>) getPidParam(name.substring("PIDParam".length()));
      }
      return (ParamImpl<T, ?>) Params.class.getField(name).get(null);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException("failed to find Param " + name, e);
    }
  }

  /*package*/ static String nameFor(@NonNull Param<?> p) {
    if (p instanceof PIDParam) {
      return "PIDParam"+p.getCode();
    }
    try {
      for (Field f : Params.class.getFields()) {
        if (f.get(null) == p) {
          return f.getName();
        }
      }
    } catch (IllegalAccessException e) {
      Log.e("Params", "failed to get name for param", e);
    }

    return null;
  }

  private Params() { }
}

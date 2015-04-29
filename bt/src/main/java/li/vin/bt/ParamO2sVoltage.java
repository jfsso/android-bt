package li.vin.bt;

import android.util.Log;

/*package*/ class ParamO2sVoltage extends ParamStream<Float> {
  private static final float RATIO = 8 / 65535f;

  public ParamO2sVoltage(String code) {
    super(code);
  }

  @Override public Float parseVal(final String val) {
    Log.d("O2S Voltage", "val: " + val);
    final int c = Integer.parseInt(val.substring(6, 8), HEX);
    final int d = Integer.parseInt(val.substring(8, 10), HEX);

    return ((c * 256) + d) * RATIO;
  }
}

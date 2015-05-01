package li.vin.bt;

import android.util.Log;

/*package*/ class ParamO2sEquivalenceRatio extends ParamStreamFloat {
  private static final float RATIO = 2 / 65535f;

  public ParamO2sEquivalenceRatio(String code) {
    super(code);
  }

  @Override public Float parseVal(final String val) {
    Log.d("O2S Equivalence", "val: " + val);
    final int a = Integer.parseInt(val.substring(2, 4), HEX);
    final int b = Integer.parseInt(val.substring(4, 6), HEX);

    return ((a * 256) + b) * RATIO;
  }
}

package li.vin.bt;

import java.util.UUID;

/*package*/ class ParamString extends ParamPlain<String> {
  public ParamString(UUID uuid) {
    super(uuid);
  }

  public ParamString(UUID uuid, boolean hasNotifications, boolean shouldRead) {
    super(uuid, hasNotifications, shouldRead);
  }

  @Override public final String parseVal(final String val) {
    return val;
  }
}

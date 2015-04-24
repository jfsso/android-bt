package li.vin.bt;

import java.util.AbstractMap;

public final class ObdPair extends AbstractMap.SimpleImmutableEntry<String, String> {

  public ObdPair(String theKey, String theValue) {
    super(theKey, theValue);
  }

}

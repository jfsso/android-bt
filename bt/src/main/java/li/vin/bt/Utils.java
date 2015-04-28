package li.vin.bt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

/*package*/ class Utils {
  public static final String btState(final int state) {
    switch (state) {
      case BluetoothProfile.STATE_CONNECTED: return "STATE_CONNECTED";
      case BluetoothProfile.STATE_CONNECTING: return "STATE_CONNECTING";
      case BluetoothProfile.STATE_DISCONNECTED: return "STATE_DISCONNECTED";
      case BluetoothProfile.STATE_DISCONNECTING: return "STATE_DISCONNECTING";
      default: return "Unrecognized Gatt State: " + state;
    }
  }

  public static final String gattStatus(final int status) {
    switch (status) {
      case BluetoothGatt.GATT_FAILURE: return "GATT_FAILURE";
      case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION: return "GATT_INSUFFICIENT_AUTHENTICATION";
      case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION: return "GATT_INSUFFICIENT_ENCRYPTION";
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH: return "GATT_INVALID_ATTRIBUTE_LENGTH";
      case BluetoothGatt.GATT_INVALID_OFFSET: return "GATT_INVALID_OFFSET";
      case BluetoothGatt.GATT_READ_NOT_PERMITTED: return "GATT_READ_NOT_PERMITTED";
      case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED: return "GATT_REQUEST_NOT_SUPPORTED";
      case BluetoothGatt.GATT_SUCCESS: return "GATT_SUCCESS";
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED: return "GATT_WRITE_NOT_PERMITTED";
      default: return "Unrecognized Gatt Status: " + status;
    }
  }

  private Utils() { }
}

package li.vin.my.deviceservice;

import java.util.UUID;

/*package*/ final class Uuids {
  public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  public static final UUID SERVICE = UUID.fromString("e4888211-50f0-412d-9c9a-75015eb36586");

  /**
   * Attribute to write anything to to clear the DTCs
   */
  public static final UUID CLEAR_DTCS = UUID.fromString("00f8e5ab-f0d3-42f7-bc42-de3696b0a522");

  /**
   * Diagnostic Trouble Codes<br>
   * format: D:P0100,P0200,P0300,U0100... ect
   */
  public static final UUID DTCS = UUID.fromString("82e6de6b-b610-455e-bf53-0166f4d6e493");

  /**
   * Hex bitflag string representing vehicle support of OBD-II PIDs.<br>
   * format: B3824000<br><br>
   * <a href="https://en.wikipedia.org/wiki/OBD-II_PIDs#Bitwise_encoded_PIDs">See Wikipedia entry for more info.</a>
   */
  public static final UUID PIDS = UUID.fromString("86754999-7269-4e55-b4e7-01c017a16f3a");

  /**
   * BLE113 Serial Number<br>
   * format: B4994C697008
   */
  public static final UUID CHIP_ID = UUID.fromString("a9b47333-f59f-4390-82a3-e5af6b8b75dc");

  /**
   * Car's Vehicle Identification Number<br>
   * format: V:1G1JC5444R7252367
   */
  public static final UUID VIN = UUID.fromString("707d0997-0880-4ab7-b900-c779bcb08a11");

  /**
   * Stream of all car data except:
   *  rpm(0C),
   *  VIN,
   *  DTCs
   * format: 41XX{data} where XX === PID
   */
  public static final UUID STREAM = UUID.fromString("180c5783-aa91-43c0-a8f3-d12a7668b339");

  /**
   * Engine Revolutions Per Minute<br>
   * format: 401C{data}
   */
  public static final UUID RPM = UUID.fromString("3197f839-9920-4fea-a3a6-f3d45c3eaa97");

  /**
   * Accelerometer data
   * format: 4 byte value (X, Y, Z, collision)
   */
  public static final UUID ACCEL = UUID.fromString("51c5848d-40ec-4d0c-918c-628db566432c");

  public static final UUID BATTERY_VOLTAGE = UUID.fromString("2db17ea0-78ab-420b-8449-f791c5e8c82b");

  private Uuids() {
  }
}

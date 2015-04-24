package li.vin.bt;

import java.util.UUID;

/*package*/ final class Uuids {
  public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  public static final UUID SERVICE = UUID.fromString("e4888211-50f0-412d-9c9a-75015eb36586");

  /**
   * Attribute to write anything to to clear the DTCs
   */
  public static final UUID CLEAR_DTC = UUID.fromString("00f8e5ab-f0d3-42f7-bc42-de3696b0a522");

  /**
   * Diagnostic Trouble Codes<br>
   * format: D:P0100,P0200,P0300,U0100... ect
   */
  public static final UUID DTC_CODES = UUID.fromString("82e6de6b-b610-455e-bf53-0166f4d6e493");

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
   *  speed(0D),
   *  rpm(0C),
   *  load(04),
   *  VIN,
   *  DTCs
   * format: 41XX{data} where XX === PID
   */
  public static final UUID STREAM = UUID.fromString("180c5783-aa91-43c0-a8f3-d12a7668b339");

  /**
   * Attribute to write the secret to begin streaming data
   */
  public static final UUID UNLOCK = UUID.fromString("60f38902-b638-4aaa-b7eb-218769047cd8");

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

  /**
   * Accelerometer Interrupt Input
   * format: "Crash Detected" <—Subject to change.
   */
  public static final UUID CRASH = UUID.fromString("5d053d4d-0420-4e44-b386-c97d3cab5845");

  private Uuids() {
  }
}

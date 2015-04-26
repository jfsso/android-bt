package li.vin.bt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/*package*/ final class BtLeDevice implements Device {
  private final BluetoothDevice mDevice;

  /*package*/ BtLeDevice(BluetoothDevice device) {
    mDevice = device;
  }

  private BtLeDevice(Parcel in) {
    mDevice = in.readParcelable(null);
  }

  public String getName() {
    String name = mDevice.getName();
    if (name == null) {
      name = mDevice.getAddress();
    }
    return name;
  }

  public DeviceConnection createDeviceConnection(Context context, String unlockKey) {
    return new BtLeDeviceConnection(context, mDevice, unlockKey);
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeParcelable(mDevice, flags);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator<BtLeDevice> CREATOR = new Parcelable.Creator<BtLeDevice>() {
    public BtLeDevice createFromParcel(Parcel in) {
      return new BtLeDevice(in);
    }

    public BtLeDevice[] newArray(int size) {
      return new BtLeDevice[size];
    }
  };
}

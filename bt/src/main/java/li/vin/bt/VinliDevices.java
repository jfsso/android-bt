package li.vin.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public final class VinliDevices {
  private static final String TAG = VinliDevices.class.getSimpleName();
  private static final long DEFAULT_SCAN_PERIOD = 10000;

  public static Observable<Device> createDeviceObservable(final Context context) {
    return createDeviceObservable(context, DEFAULT_SCAN_PERIOD);
  }

  public static Observable<Device> createDeviceObservable(final Context context,
      final long scanTimeout) {
    if (context == null) {
      throw new IllegalArgumentException("context is null");
    }
    if (scanTimeout <= 0) {
      throw new IllegalArgumentException("scan timeout must be greater than zero");
    }

    final Context appContext = context.getApplicationContext();

    return Observable.create(new Observable.OnSubscribe<Device>() {
      @Override public void call(final Subscriber<? super Device> subscriber) {
        final BluetoothManager manager =
          (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);

        if (manager == null) {
          subscriber.onError(new BluetoothException("failed to get the Android bluetooth service"));
          return;
        }

        final BluetoothAdapter adapter = manager.getAdapter();

        if (adapter == null || !adapter.isEnabled()) {
          subscriber.onError(new BluetoothDisabledException("bluetooth is disabled"));
          return;
        }

        final BluetoothAdapter.LeScanCallback listener = new BluetoothAdapter.LeScanCallback() {
          @Override public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "Found device " + device + " UUIDs: " + Arrays.toString(device.getUuids()));

            if (isVinliDevice(scanRecord)) {
              final Device d = new BtLeDevice(device);
              subscriber.onNext(d);
            }
          }
        };

        final Runnable timeoutListener = new Runnable() {
          @Override public void run() {
            adapter.stopLeScan(listener);
            subscriber.onCompleted();
          }
        };

        final Handler handler = new Handler();
        handler.postDelayed(timeoutListener, scanTimeout);

        subscriber.add(Subscriptions.create(new Action0() {
          @Override public void call() {
            adapter.stopLeScan(listener);
            handler.removeCallbacks(timeoutListener);
          }
        }));
      }
    });
  }

  private VinliDevices() {
  }

  /**
   * Work-around for device filtering not working for 128-bit UUIDs
   * <a href="http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation/21986475#21986475">Implementation found here</a>
   */
  private static boolean isVinliDevice(final byte[] advertisedData) {
    int offset = 0;
    while (offset < (advertisedData.length - 2)) {
      int len = advertisedData[offset++];
      if (len == 0) {
        break;
      }

      int type = advertisedData[offset++];
      switch (type) {
        case 0x02: // Partial list of 16-bit UUIDs
        case 0x03: // Complete list of 16-bit UUIDs
          while (len > 1) {
            offset += 2;
            len -= 2;
          }
          break;
        case 0x06:// Partial list of 128-bit UUIDs
        case 0x07:// Complete list of 128-bit UUIDs
          // Loop through the advertised 128-bit UUID's.
          while (len >= 16) {
            try {
              // Wrap the advertised bits and order them.
              final ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
              final long mostSignificantBit = buffer.getLong();
              final long leastSignificantBit = buffer.getLong();
              final UUID uuid = new UUID(leastSignificantBit, mostSignificantBit);
              if (uuid.equals(Uuids.SERVICE)) {
                return true;
              }
            } catch (IndexOutOfBoundsException e) {
              Log.e(TAG, "Failed to filter out only Vinli devices", e);
            } finally {
              // Move the offset to read the next uuid.
              offset += 15;
              len -= 16;
            }
          }
          break;
        default:
          offset += (len - 1);
          break;
      }
    }

    return false;
  }
}

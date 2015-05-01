package li.vin.bt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;

public final class VinliDevices {
  private static final String TAG = VinliDevices.class.getSimpleName();
  private static final long DEFAULT_SCAN_PERIOD = 10000;

  public static Observable<Device> createDeviceObservable(@NonNull final Context context) {
    return createDeviceObservable(context, DEFAULT_SCAN_PERIOD);
  }

  public static Observable<Device> createDeviceObservable(@NonNull final Context context,
      final long scanTimeout) {
    if (scanTimeout <= 0) {
      throw new IllegalArgumentException("scan timeout must be greater than zero");
    }

    final Context appContext = context.getApplicationContext();

    return Observable.create(new Observable.OnSubscribe<Device>() {
      @Override public void call(final Subscriber<? super Device> subscriber) {
        subscriber.onNext(new BtLeDevice());
        subscriber.onCompleted();
      }
    });
  }

  private VinliDevices() {
  }

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

  /**
   * Work-around for device filtering not working for 128-bit UUIDs
   * <a href="http://stackoverflow.com/a/19060589">Implementation found here</a>
   */
  private static final List<UUID> parseUuids(byte[] advertisedData) {
    final List<UUID> uuids = new ArrayList<>();

    final ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
    while (buffer.remaining() > 2) {
      byte length = buffer.get();
      if (length == 0) break;

      byte type = buffer.get();
      switch (type) {
        case 0x02: // Partial list of 16-bit UUIDs
        case 0x03: // Complete list of 16-bit UUIDs
          while (length >= 2) {
            uuids.add(UUID.fromString(String.format(
              "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
            length -= 2;
          }
          break;

        case 0x06: // Partial list of 128-bit UUIDs
        case 0x07: // Complete list of 128-bit UUIDs
          while (length >= 16) {
            long lsb = buffer.getLong();
            long msb = buffer.getLong();
            uuids.add(new UUID(msb, lsb));
            length -= 16;
          }
          break;

        default:
          buffer.position(buffer.position() + length - 1);
          break;
      }
    }

    return uuids;
  }
}

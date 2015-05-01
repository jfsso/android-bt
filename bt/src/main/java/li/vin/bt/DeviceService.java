package li.vin.bt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DeviceService extends Service {
  private static final String TAG = DeviceService.class.getSimpleName();

  private DeviceServiceBinder mBinder;

  @Override public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind");
    if (mBinder == null) {
      mBinder = new DeviceServiceBinder(this);
    }
    return mBinder;
  }

  @Override public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind");
    mBinder.unsubscribeAll();
    return false;
  }

}

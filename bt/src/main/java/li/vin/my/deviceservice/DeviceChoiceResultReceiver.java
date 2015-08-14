package li.vin.my.deviceservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by christophercasey on 8/13/15.
 */
public final class DeviceChoiceResultReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context context, Intent intent) {
    VinliDevices.deliverChipId(context,
        intent == null ? null : intent.getStringExtra("li.vin.my.chip_id"));
  }
}

package li.vin.my.deviceservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by christophercasey on 8/21/15.
 */
public class MyVinliProxyActivity extends Activity {
  private static final String TAG = MyVinliProxyActivity.class.getSimpleName();

  private enum Mode {
    CHIP_ID("li.vin.my.OAuthActivity"),
    ENABLE_BT("li.vin.my.EnableBluetoothActivity");

    private final String myVinliActivity;

    Mode(String myVinliActivity) {
      this.myVinliActivity = myVinliActivity;
    }
  }

  private static final Set<MyVinliProxyActivity> allInstances =
      Collections.newSetFromMap(new WeakHashMap<MyVinliProxyActivity, Boolean>());

  /*package*/
  static void killAll() {
    for (MyVinliProxyActivity activity : new HashSet<>(allInstances)) {
      if (activity != null) activity.deliverResult(false);
    }
  }

  /*package*/
  static void launchChipIdProxy(@NonNull Activity activity, @NonNull String clientId,
      @NonNull String redirectUri) {
    Intent i = new Intent(activity, MyVinliProxyActivity.class);
    i.putExtra("mode", Mode.CHIP_ID);
    i.putExtra("li.vin.my.client_id", clientId);
    i.putExtra("li.vin.my.redirect_uri", redirectUri);
    i.putExtra("li.vin.my.choose_device", true);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    activity.startActivity(i);
  }

  /*package*/
  static void launchEnableBtProxy(@NonNull Activity actvity) {
    Intent i = new Intent(actvity, MyVinliProxyActivity.class);
    i.putExtra("mode", Mode.ENABLE_BT);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    actvity.startActivity(i);
  }

  private String chipId, devName, devIcon, devId;
  private boolean resultDelivered;
  private Mode mode;

  @Override protected void onCreate(Bundle savedInstanceState) {
    allInstances.add(this);
    super.onCreate(savedInstanceState);

    Intent i = getIntent();
    if (i == null) {
      deliverResult(false);
      return;
    }

    mode = (Mode) i.getSerializableExtra("mode");
    if (mode == null) {
      deliverResult(false);
      return;
    }

    try {
      Intent ii = new Intent();
      ii.setClassName("li.vin.my", mode.myVinliActivity);
      ii.putExtras(i);
      startActivityForResult(ii, 111);
      Log.i(TAG, "onCreate startActivityForResult success mode " + mode.name());
    } catch (Exception e) {
      Log.e(TAG, "onCreate startActivityForResult failed", e);
      deliverResult(false);
    }
  }

  @Override protected void onPause() {
    Log.i(TAG, "onPause isFinishing " + isFinishing() + "mode " + mode.name());
    deliverResult(true);
    super.onPause();
  }

  @Override protected void onStop() {
    Log.i(TAG, "onStop isFinishing " + isFinishing() + "mode " + mode.name());
    deliverResult(false);
    super.onStop();
  }

  @Override protected void onDestroy() {
    Log.i(TAG, "onDestroy isFinishing " + isFinishing() + "mode " + mode.name());
    deliverResult(false);
    super.onDestroy();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 111) {
      Log.i(TAG, "onActivityResult 111 mode " + mode.name());
      chipId = data == null ? null : data.getStringExtra("li.vin.my.chip_id");
      devName = data == null ? null : data.getStringExtra("li.vin.my.device_name");
      devIcon = data == null ? null : data.getStringExtra("li.vin.my.device_icon");
      devId = data == null ? null : data.getStringExtra("li.vin.my.device_id");
      deliverResult(false);
    }
  }

  private void deliverResult(boolean checkIsFinishing) {
    if ((!checkIsFinishing || isFinishing()) && !resultDelivered) {
      resultDelivered = true;
      Log.i(TAG, "deliverResult mode " + mode.name());
      switch (mode) {
        case CHIP_ID:
          VinliDevices.deliverChipId(this, chipId, devName, devIcon, devId);
          break;
        case ENABLE_BT:
          VinliDevices.deliverEnableBt();
          break;
      }
      finishActivity(111);
      finish();
    }
  }
}

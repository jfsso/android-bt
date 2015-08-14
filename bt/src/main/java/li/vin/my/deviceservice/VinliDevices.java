package li.vin.my.deviceservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import li.vin.my.deviceservice.utils.ViewUtil;
import rx.Observable;
import rx.Subscriber;

public final class VinliDevices {
  private static final String TAG = VinliDevices.class.getSimpleName();
  private static final String SHARED_PREFS_NAME = TAG + ".sharedprefs";
  private static final String CHIP_ID_KEY = TAG + ".chipid";

  private static volatile BtLeDeviceConnection deviceConn = null;
  private static boolean choosingDevice;
  private static final Set<Runnable> chooseDeviceCallbacks = new HashSet<>();

  private static BtLeDeviceConnection makeOrUpdateConnection(Context context, String chipId) {
    BtLeDeviceConnection result;
    synchronized (VinliDevices.class) {
      result = deviceConn;
      if (result == null) {
        deviceConn = result = new BtLeDeviceConnection(context, chipId);
      } else {
        if (!deviceConn.chipId.equals(chipId)) {
          deviceConn.shutdown();
          deviceConn = result = new BtLeDeviceConnection(context, chipId);
        } else {
          deviceConn.updateContext(context);
        }
      }
    }
    return result;
  }

  /*package*/ static void deliverChipId(@NonNull Context context, String chipId) {
    if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
      context.getApplicationContext()
          .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(CHIP_ID_KEY, chipId)
          .apply();
    }
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override public void run() {
        if (choosingDevice) {
          choosingDevice = false;
          Set<Runnable> cbs = new HashSet<>(chooseDeviceCallbacks);
          chooseDeviceCallbacks.clear();
          for (Runnable r : cbs) {
            r.run();
          }
        }
      }
    });
  }

  public static @Nullable Observable<DeviceConnection> connect(@NonNull final Context context,
      @NonNull final String clientId, @NonNull final String redirectUri) {
    return connect(context, clientId, redirectUri, false);
  }

  public static @Nullable Observable<DeviceConnection> connect(@NonNull final Context context,
      @NonNull final String clientId, @NonNull final String redirectUri, boolean forceFreshChoice) {
    if (!isMyVinliInstalled(context)) return null;

    if (forceFreshChoice) {
      context.getApplicationContext()
          .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(CHIP_ID_KEY, null)
          .apply();
    }

    return Observable.create(new Observable.OnSubscribe<DeviceConnection>() {
      @Override public void call(final Subscriber<? super DeviceConnection> subscriber) {
        if (subscriber.isUnsubscribed()) return;

        String chipId = context.getApplicationContext()
            .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(CHIP_ID_KEY, null);

        if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
          subscriber.onNext(makeOrUpdateConnection(context, chipId));
          subscriber.onCompleted();
          return;
        }

        final Runnable deviceChosen = new Runnable() {
          @Override public void run() {
            if (subscriber.isUnsubscribed()) return;

            String chipId = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(CHIP_ID_KEY, null);

            if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
              subscriber.onNext(makeOrUpdateConnection(context, chipId));
              subscriber.onCompleted();
            } else {
              subscriber.onError(new RuntimeException("device not chosen."));
            }
          }
        };

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override public void run() {
            chooseDeviceCallbacks.add(deviceChosen);
            if (!choosingDevice) {
              choosingDevice = true;
              try {
                Intent i = new Intent();
                i.setClassName("li.vin.my", "li.vin.my.OAuthActivity");
                i.putExtra("li.vin.my.client_id", clientId);
                i.putExtra("li.vin.my.redirect_uri", redirectUri);
                i.putExtra("li.vin.my.choose_device", true);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(i);
              } catch (Exception e) {
                Log.e(TAG, "failed to start ChooseDeviceProxyActivity: " + e);
                choosingDevice = false;
                chooseDeviceCallbacks.remove(deviceChosen);
                subscriber.onError(e);
              }
            }
          }
        });
      }
    });
  }

  /** Request user installation of the My Vinli app. This should only be called in response to
   *  {@link #connect(Context,String,String)} returning null. */
  public static void requestInstallMyVinli(@NonNull Activity context) {
    final WeakReference<Activity> ctxRef = new WeakReference<>(context);
    ViewUtil.yesNoAlert(context,
        "Install My Vinli",
        "This app requires My Vinli. Install now?",
        context.getString(android.R.string.yes), context.getString(android.R.string.no),
        new ViewUtil.AlertCallback() {
          @Override public void onAlertComplete(boolean yes) {
            if (!yes) return;
            Activity context = ctxRef.get();
            if (context == null) return;
            String pkgName = context.getResources().getBoolean(R.bool.test_fake_install_flow) ?
                context.getString(R.string.test_fake_install_flow_package) :
                context.getString(R.string.my_vinli_package_name);
            try {
              context.startActivity(
                  new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkgName)));
            } catch (Exception e1) {
              try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName)));
              } catch (Exception e2) {
                Toast.makeText(context, "Could not install My Vinli app.", Toast.LENGTH_SHORT).show();
              }
            }
          }
        });
  }

  private static boolean isMyVinliInstalled(Context context) {
    PackageManager pm = context.getPackageManager();
    //noinspection ConstantConditions
    String pkgName = context.getResources().getBoolean(R.bool.test_fake_install_flow) ?
        context.getString(R.string.test_fake_install_flow_package) :
        context.getString(R.string.my_vinli_package_name);
    boolean installed;
    try {
      pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
      installed = true;
    } catch (PackageManager.NameNotFoundException e) {
      installed = false;
    }
    return installed;
  }

  private VinliDevices() {
  }
}

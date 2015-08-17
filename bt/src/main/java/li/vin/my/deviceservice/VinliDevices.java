package li.vin.my.deviceservice;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public final class VinliDevices {
  private static final String TAG = VinliDevices.class.getSimpleName();
  private static final String SHARED_PREFS_NAME = TAG + ".sharedprefs";
  private static final String CHIP_ID_KEY = TAG + ".chipid";
  private static final String DEV_NAME_KEY = TAG + ".devicename";
  private static final String DEV_IC_KEY = TAG + ".deviceicon";

  private static volatile BtLeDeviceConnection deviceConn = null;

  private static BtLeDeviceConnection makeOrUpdateConnection(Context context, String chipId,
      String name, String icon) {
    BtLeDeviceConnection result;
    synchronized (VinliDevices.class) {
      result = deviceConn;
      if (result == null) {
        deviceConn = result = new BtLeDeviceConnection(context, chipId, name, icon);
      } else {
        if (!deviceConn.chipId.equals(chipId)) {
          deviceConn.shutdown();
          deviceConn = result = new BtLeDeviceConnection(context, chipId, name, icon);
        } else {
          deviceConn.updateContext(context);
        }
      }
    }
    return result;
  }

  /**
   * Convenience to connect to the last known cached device, not forcing a fresh device scan.
   *
   * @see #connect(Context, String, String, boolean)
   */
  public static @NonNull Observable<DeviceConnection> connect(@NonNull Context context,
      @NonNull final String clientId, @NonNull final String redirectUri) {
    return connect(context, clientId, redirectUri, false);
  }

  /**
   * Attempt to make a connection with My Vinli. The returned {@link Observable} can be flatmapped
   * to any My Vinli device capabilities exposed by the {@link DeviceConnection} interface.
   * It is important to note that this Observable will immediately emit an error if the My Vinli
   * app is not installed, so it is advisable to use {@link #isMyVinliInstalledAndUpdated(Context)}
   * and
   * {@link #launchMarketToMyVinli(Context)} to handle this scenario in advance.
   *
   * @param clientId OAuth Client ID of the application requesting a connection.
   * @param redirectUri OAuth redirect URI of the application requesting a connection.
   * @param forceFreshChoice Forces My Vinli to initiate a Bluetooth scan of nearby devices and
   * make a fresh choice rather than automatically connecting to the last known device. It may be
   * helpful to set this to true in response to a user action explicitly requesting a fresh choice.
   */
  public static @NonNull Observable<DeviceConnection> connect(@NonNull final Context context,
      @NonNull final String clientId, @NonNull final String redirectUri, boolean forceFreshChoice) {
    if (!isMyVinliInstalledAndUpdated(context)) {
      return Observable.error(new Exception(
          "My Vinli is not installed - use isMyVinliInstalledAndUpdated "
              + "and launchMarketToMyVinli to handle this error."));
    }

    if (forceFreshChoice) {
      context.getApplicationContext()
          .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(CHIP_ID_KEY, null)
          .putString(DEV_NAME_KEY, null)
          .putString(DEV_IC_KEY, null)
          .apply();
    }

    final WeakReference<Context> ctx = new WeakReference<>(context);

    return enableBt(context).flatMap(new Func1<Void, Observable<DeviceConnection>>() {
      @Override public Observable<DeviceConnection> call(Void aVoid) {
        return Observable.create(new Observable.OnSubscribe<DeviceConnection>() {
          @Override public void call(final Subscriber<? super DeviceConnection> subscriber) {
            if (subscriber.isUnsubscribed()) return;
            Context context = ctx.get();
            if (context == null) {
              subscriber.onError(new Exception("Context ref went null."));
              return;
            }

            SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String chipId = prefs.getString(CHIP_ID_KEY, null);
            String devName = prefs.getString(DEV_NAME_KEY, null);
            String devIcon = prefs.getString(DEV_IC_KEY, null);

            if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
              subscriber.onNext(makeOrUpdateConnection(context, chipId, devName, devIcon));
              subscriber.onCompleted();
              return;
            }

            appCtxRef.set(context.getApplicationContext());
            clientIdRef.compareAndSet(null, clientId);
            redirectUriRef.compareAndSet(null, redirectUri);

            final Runnable deviceChosen = new Runnable() {
              @Override public void run() {
                if (subscriber.isUnsubscribed()) return;
                Context context = ctx.get();
                if (context == null) {
                  subscriber.onError(new Exception("Context ref went null."));
                  return;
                }

                SharedPreferences prefs =
                    appCtxRef.get().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                String chipId = prefs.getString(CHIP_ID_KEY, null);
                String devName = prefs.getString(DEV_NAME_KEY, null);
                String devIcon = prefs.getString(DEV_IC_KEY, null);

                if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
                  subscriber.onNext(makeOrUpdateConnection(context, chipId, devName, devIcon));
                  subscriber.onCompleted();
                } else {
                  subscriber.onError(new RuntimeException("device not chosen."));
                }
              }
            };

            reqChipIdObs.subscribe(new Subscriber<Void>() {
              @Override public void onCompleted() {
                if (!isUnsubscribed()) unsubscribe();
                deviceChosen.run();
              }

              @Override public void onError(Throwable e) {
                if (!isUnsubscribed()) unsubscribe();
                deviceChosen.run();
              }

              @Override public void onNext(Void aVoid) {
                if (!isUnsubscribed()) unsubscribe();
                deviceChosen.run();
              }
            });
          }
        });
      }
    });
  }

  /**
   * Check whether or not My Vinli is currently installed. If this returns false, My Vinli
   * must be installed before a {@link DeviceConnection} will be available. In this scenario, it
   * is advisable to call {@link #launchMarketToMyVinli(Context)} in response to direct
   * user interaction, such as a dialog requesting that the user install My Vinli.
   *
   * @see #createMyVinliInstallRequestDialog(Context)
   */
  public static boolean isMyVinliInstalledAndUpdated(Context context) {
    PackageManager pm = context.getPackageManager();
    //noinspection ConstantConditions
    String pkgName =
        context.getResources().getBoolean(R.bool.test_fake_install_flow) ? context.getString(
            R.string.test_fake_install_flow_package)
            : context.getString(R.string.my_vinli_package_name);
    int versionCode;
    try {
      pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
      PackageInfo packageInfo = pm.getPackageInfo(pkgName, 0);
      versionCode = packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
    return versionCode >= context.getResources().getInteger(R.integer.min_supported_myvinli_ver);
  }

  /**
   * Launch a market Activity for installation of My Vinli. It is best to call this in response
   * to direct user interaction after {@link #isMyVinliInstalledAndUpdated(Context)} returns false,
   * such as
   * in a dialog requesting that the user install My Vinli.
   *
   * @see #createMyVinliInstallRequestDialog(Context)
   */
  public static void launchMarketToMyVinli(@NonNull Context context) {
    String pkgName =
        context.getResources().getBoolean(R.bool.test_fake_install_flow) ? context.getString(
            R.string.test_fake_install_flow_package)
            : context.getString(R.string.my_vinli_package_name);
    try {
      context.startActivity(
          new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkgName)));
    } catch (Exception e1) {
      try {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName)));
      } catch (Exception e2) {
        Toast.makeText(context, "Could not launch market to My Vinli app.", Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  /**
   * Convenience for creating an {@link AlertDialog} that prompts the user to install My Vinli.
   * it is the caller's responsibility to call {@link AlertDialog#show()} on the returned
   * {@link AlertDialog}.
   */
  public static @NonNull AlertDialog createMyVinliInstallRequestDialog(@NonNull Context context) {
    final WeakReference<Context> ctx = new WeakReference<>(context);
    return new AlertDialog.Builder(context).setTitle("Get My Vinli")
        .setMessage("My Vinli must be installed and fully updated for this app to function "
            + "properly. Install or update now?")
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            Context context = ctx.get();
            if (context != null) launchMarketToMyVinli(context);
          }
        })
        .setNegativeButton(android.R.string.no, null)
        .create();
  }

  /** Helper to quickly and safely determine if the default Bluetooth adapter is enabled. */
  public static boolean isBluetoothEnabled(@NonNull Context context) {
    try {
      BluetoothManager mgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
      if (mgr == null) {
        Log.e(TAG, "isBluetoothEnabled found null BluetoothManager.");
        return false;
      }
      BluetoothAdapter adapter = mgr.getAdapter();
      if (adapter == null) {
        Log.e(TAG, "isBluetoothEnabled found null BluetoothAdapter.");
        return false;
      }
      return adapter.isEnabled();
    } catch (Exception e) {
      Log.e(TAG, "isBluetoothEnabled error: " + e);
      return false;
    }
  }

  private static Observable<Void> enableBt(@NonNull Context context) {
    final WeakReference<Context> ctx = new WeakReference<>(context);
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(final Subscriber<? super Void> subscriber) {
        if (subscriber.isUnsubscribed()) return;
        Context context = ctx.get();
        if (context == null) {
          subscriber.onError(new Exception("Context ref went null."));
          return;
        }

        if (isBluetoothEnabled(context)) {
          subscriber.onNext(null);
          subscriber.onCompleted();
          return;
        }

        appCtxRef.set(context.getApplicationContext());

        final Runnable deliverResult = new Runnable() {
          @Override public void run() {
            if (subscriber.isUnsubscribed()) return;
            if (isBluetoothEnabled(appCtxRef.get())) {
              subscriber.onNext(null);
              subscriber.onCompleted();
            } else {
              subscriber.onError(new Exception("Failed to enable Bluetooth."));
            }
          }
        };

        enableBtObs.subscribe(new Subscriber<Void>() {
          @Override public void onCompleted() {
            if (!isUnsubscribed()) unsubscribe();
            deliverResult.run();
          }

          @Override public void onError(Throwable e) {
            if (!isUnsubscribed()) unsubscribe();
            deliverResult.run();
          }

          @Override public void onNext(Void aVoid) {
            if (!isUnsubscribed()) unsubscribe();
            deliverResult.run();
          }
        });
      }
    });
  }

  private static final AtomicReference<Context> appCtxRef = new AtomicReference<>();
  private static final PublishSubject<Void> enableBtSubject = PublishSubject.create();
  private static final Observable<Void> enableBtObs = enableBtSubject.doOnSubscribe(new Action0() {
    @Override public void call() {
      Log.i(TAG, "enableBtObs onSubscribe.");
      boolean registered;
      try {
        Intent i = new Intent();
        i.setClassName("li.vin.my", "li.vin.my.EnableBluetoothActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appCtxRef.get()
            .getApplicationContext()
            .registerReceiver(enableBtReceiver,
                new IntentFilter("li.vin.action.BLUETOOTH_ENABLED"));
        appCtxRef.get().getApplicationContext().startActivity(i);
        registered = true;
      } catch (Exception e) {
        Log.e(TAG, "registerReceiver li.vin.action.BLUETOOTH_ENABLED " + e);
        registered = false;
      }
      if (registered) {
        new Handler(Looper.getMainLooper()).postAtTime(new Runnable() {
          @Override public void run() {
            enableBtSubject.onNext(null);
          }
        }, enableBtReceiver, SystemClock.uptimeMillis() + 10000);
      } else {
        enableBtSubject.onNext(null);
      }
    }
  }).doOnNext(new Action1<Void>() {
    @Override public void call(Void aVoid) {
      Log.i(TAG, "enableBtObs onNext.");
      new Handler(Looper.getMainLooper()).removeCallbacksAndMessages(enableBtReceiver);
      appCtxRef.get().unregisterReceiver(enableBtReceiver);
    }
  }).doOnUnsubscribe(new Action0() {
    @Override public void call() {
      Log.i(TAG, "enableBtObs onUnsubscribe.");
    }
  }).publish().refCount();

  private static final BroadcastReceiver enableBtReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      enableBtSubject.onNext(null);
    }
  };

  private static final AtomicReference<String> clientIdRef = new AtomicReference<>();
  private static final AtomicReference<String> redirectUriRef = new AtomicReference<>();
  private static final PublishSubject<Void> reqChipIdSubject = PublishSubject.create();
  private static final Observable<Void> reqChipIdObs =
      reqChipIdSubject.doOnSubscribe(new Action0() {
        @Override public void call() {
          Log.i(TAG, "reqChipIdObs onSubscribe.");
          try {
            Intent i = new Intent();
            i.setClassName("li.vin.my", "li.vin.my.OAuthActivity");
            i.putExtra("li.vin.my.client_id", clientIdRef.get());
            i.putExtra("li.vin.my.redirect_uri", redirectUriRef.get());
            i.putExtra("li.vin.my.choose_device", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appCtxRef.get()
                .getApplicationContext()
                .registerReceiver(reqChipIdReceiver,
                    new IntentFilter("li.vin.action.DEVICE_CHOSEN"));
            appCtxRef.get().getApplicationContext().startActivity(i);
          } catch (Exception e) {
            Log.e(TAG, "registerReceiver li.vin.action.BLUETOOTH_ENABLED " + e);
            reqChipIdSubject.onNext(null);
          }
        }
      }).doOnNext(new Action1<Void>() {
        @Override public void call(Void aVoid) {
          Log.i(TAG, "reqChipIdObs onNext - unregging receiver.");
          appCtxRef.get().unregisterReceiver(reqChipIdReceiver);
        }
      }).doOnUnsubscribe(new Action0() {
        @Override public void call() {
          Log.i(TAG, "reqChipIdObs onUnsubscribe.");
        }
      }).publish().refCount();

  private static final BroadcastReceiver reqChipIdReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String chipId = intent == null ? null : intent.getStringExtra("li.vin.my.chip_id");
      String devName = intent == null ? null : intent.getStringExtra("li.vin.my.device_name");
      String devIcon = intent == null ? null : intent.getStringExtra("li.vin.my.device_icon");
      if (chipId != null && TextUtils.getTrimmedLength(chipId) != 0) {
        context.getApplicationContext()
            .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(CHIP_ID_KEY, chipId)
            .putString(DEV_NAME_KEY, devName)
            .putString(DEV_IC_KEY, devIcon)
            .apply();
      }
      reqChipIdSubject.onNext(null);
    }
  };

  private VinliDevices() {
  }
}

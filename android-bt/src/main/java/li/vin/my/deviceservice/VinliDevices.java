package li.vin.my.deviceservice;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.internal.operators.OperatorReplayFix;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static android.text.TextUtils.getTrimmedLength;

public final class VinliDevices {
  private static final String TAG = VinliDevices.class.getSimpleName();
  private static final String SHARED_PREFS_NAME = TAG + ".sharedprefs";
  private static final String CHIP_ID_KEY = TAG + ".chipid";
  private static final String DEV_NAME_KEY = TAG + ".devicename";
  private static final String DEV_IC_KEY = TAG + ".deviceicon";
  private static final String DEV_ID_KEY = TAG + ".deviceid";

  private static volatile BtLeDeviceConnection deviceConn = null;

  private static BtLeDeviceConnection makeOrUpdateConnection(Context context, String chipId,
      String name, String icon, String id) {
    BtLeDeviceConnection result;
    synchronized (VinliDevices.class) {
      result = deviceConn;
      if (result == null) {
        deviceConn = result = new BtLeDeviceConnection(context, chipId, name, icon, id);
      } else {
        if (!deviceConn.chipId.equals(chipId)) {
          deviceConn.shutdown();
          deviceConn = result = new BtLeDeviceConnection(context, chipId, name, icon, id);
        } else {
          deviceConn.updateContext(context);
        }
      }
    }
    return result;
  }

  /**
   * Determine whether or not the given {@link Intent} is related to Vinli and relevant to the
   * current application. If this returns false, the data is invalid or possibly related to an
   * unknown or unrelated Vinli device, and should be ignored.
   */
  @SuppressWarnings("unused") public static boolean intentIsRelevant(@NonNull Context context,
      Intent intent) {
    if (intent == null) return false;
    String intentTarget = intent.getStringExtra("li.vin.my.chip_id");
    return intentTarget != null && intentTarget.equals(context.getApplicationContext()
        .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        .getString(CHIP_ID_KEY, null));
  }

  /**
   * Convenience to connect to the last known cached device, not forcing a fresh device scan. This
   * should almost always be used unless the user explicitly requests a fresh device choice.
   *
   * @see #connect(Context, String, String, boolean)
   */
  @SuppressWarnings("unused") public static @NonNull Observable<DeviceConnection> connect(
      @NonNull Context context, @NonNull final String clientId, @NonNull final String redirectUri) {
    return connect(context, clientId, redirectUri, false);
  }

  /**
   * Attempt to make a connection with My Vinli. The returned {@link Observable} can be flatmapped
   * to any My Vinli device capabilities exposed by the {@link DeviceConnection} interface.
   * It is important to note that this Observable will immediately emit an error if the My Vinli
   * app is not installed, so it is advisable to use {@link #isMyVinliInstalledAndUpdated(Context)}
   * and {@link #launchMarketToMyVinli(Context)} to handle this scenario in advance.
   *
   * <br><br>
   *
   * Note that if this is the first time a connection is being established and no cached connection
   * is available, this will fail if not called from an Activity context since UI presentation is
   * required. Also, each call to connect will abort any previous pending connects, so connect
   * should only be called once per Component lifecycle sequence, and the resulting observable
   * reused.
   *
   * @param clientId OAuth Client ID of the application requesting a connection.
   * @param redirectUri OAuth redirect URI of the application requesting a connection.
   * @param forceFreshDevice Forces My Vinli to initiate a Bluetooth scan of nearby devices and
   * make a fresh choice rather than automatically connecting to the last known device. It may be
   * helpful to set this to true in response to a user action explicitly requesting a fresh choice.
   */
  public static @NonNull Observable<DeviceConnection> connect(@NonNull final Context context,
      @NonNull final String clientId, @NonNull final String redirectUri, boolean forceFreshDevice) {
    if (!isMyVinliInstalledAndUpdated(context)) {
      return Observable.error(new Exception(
          "My Vinli is not installed - use isMyVinliInstalledAndUpdated "
              + "and launchMarketToMyVinli to handle this error."));
    }
    ConnectAttempt connectAttempt = new ConnectAttempt.Builder().context(context)
        .clientId(clientId)
        .redirectUri(redirectUri)
        .build();
    if (forceFreshDevice) {
      connectAttempt.clearCache();
    }
    btResult.onNext(connectAttempt);
    connectResult.onNext(connectAttempt);
    mainInit.onNext(connectAttempt);
    return mainBtAndConnect;
  }

  private static final BehaviorSubject<ConnectAttempt> mainInit = BehaviorSubject.create();
  private static final PublishSubject<ConnectAttempt> btResult = PublishSubject.create();
  private static final PublishSubject<ConnectAttempt> connectResult = PublishSubject.create();

  private static boolean checkBtAttempt(ConnectAttempt connAttempt,
      Subscriber<? super ConnectAttempt> subscriber, boolean errorIfNone) {
    if (subscriber.isUnsubscribed()) return true;
    Context context = connAttempt.context();
    if (context != null && isBluetoothEnabled(context)) {
      subscriber.onNext(connAttempt);
      subscriber.onCompleted();
      return true;
    } else if (errorIfNone) {
      subscriber.onError(new RuntimeException("enable bt failed."));
    }
    return false;
  }

  private static boolean checkConnectAttempt(ConnectAttempt connAttempt,
      Subscriber<? super DeviceConnection> subscriber, boolean errorIfNone) {
    if (subscriber.isUnsubscribed()) return true;
    Context context = connAttempt.context();
    if (context != null &&
        connAttempt.chipId != null && getTrimmedLength(connAttempt.chipId) != 0 &&
        connAttempt.devId != null && getTrimmedLength(connAttempt.devId) != 0) {
      subscriber.onNext(makeOrUpdateConnection(context, connAttempt.chipId, connAttempt.devName,
          connAttempt.devIcon, connAttempt.devId));
      subscriber.onCompleted();
      return true;
    } else if (errorIfNone) {
      subscriber.onError(new RuntimeException("connection failed."));
    }
    return false;
  }

  private static final Func1<ConnectAttempt, Observable<ConnectAttempt>> mainBt =
      new Func1<ConnectAttempt, Observable<ConnectAttempt>>() {
        @Override public Observable<ConnectAttempt> call(final ConnectAttempt connAttempt) {

          Context context = connAttempt.context();
          if (context == null) return Observable.error(new RuntimeException("no context."));
          if (isBluetoothEnabled(context)) return Observable.just(connAttempt);

          return Observable.create(new Observable.OnSubscribe<ConnectAttempt>() {
            @Override public void call(final Subscriber<? super ConnectAttempt> subscriber) {

              if (checkBtAttempt(connAttempt, subscriber, false)) {
                return;
              }

              Context ctx = connAttempt.context();
              if (ctx == null) {
                subscriber.onError(new RuntimeException("no context."));
                return;
              }
              final Context appContext = ctx.getApplicationContext();

              final BroadcastReceiver recv = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                  btResult.onNext(connAttempt);
                }
              };

              btResult.subscribe(new Subscriber<ConnectAttempt>() {
                @Override public void onCompleted() {
                  if (!isUnsubscribed()) unsubscribe();
                }

                @Override public void onError(Throwable e) {
                  if (!isUnsubscribed()) unsubscribe();
                }

                @Override public void onNext(ConnectAttempt connectAttempt) {
                  if (!isUnsubscribed()) unsubscribe();
                  try {
                    appContext.unregisterReceiver(recv);
                  } catch (Exception ignored) {
                  }
                  checkBtAttempt(connectAttempt, subscriber, true);
                }
              });

              try {
                appContext.registerReceiver(recv,
                    new IntentFilter("li.vin.action.BLUETOOTH_ENABLED"));
                Intent i = new Intent();
                i.setClassName("li.vin.my", "li.vin.my.EnableBluetoothActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                connAttempt.activity().startActivity(i);
                Log.i(TAG, "startActivity EnableBluetoothActivity success");
              } catch (Exception e) {
                Log.i(TAG, "startActivity EnableBluetoothActivity failed", e);
                btResult.onNext(connAttempt);
              }
            }
          }).delaySubscription(100, TimeUnit.MILLISECONDS);
        }
      };

  private static final Func1<ConnectAttempt, Observable<DeviceConnection>> mainConnect =
      new Func1<ConnectAttempt, Observable<DeviceConnection>>() {
        @Override public Observable<DeviceConnection> call(final ConnectAttempt ca) {

          Context context = ca.context();
          if (context == null) return Observable.error(new RuntimeException("no context."));
          final ConnectAttempt connAttempt = ca.fromCache();
          if (connAttempt.chipId != null && getTrimmedLength(connAttempt.chipId) != 0 &&
              connAttempt.devId != null && getTrimmedLength(connAttempt.devId) != 0) {
            return Observable.just(
                (DeviceConnection) makeOrUpdateConnection(context, connAttempt.chipId,
                    connAttempt.devName, connAttempt.devIcon, connAttempt.devId));
          }

          return Observable.create(new Observable.OnSubscribe<DeviceConnection>() {
            @Override public void call(final Subscriber<? super DeviceConnection> subscriber) {

              if (checkConnectAttempt(connAttempt, subscriber, false)) {
                return;
              }

              Context ctx = connAttempt.context();
              if (ctx == null) {
                subscriber.onError(new RuntimeException("no context."));
                return;
              }
              final Context appContext = ctx.getApplicationContext();

              final BroadcastReceiver recv = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                  try {
                    connectResult.onNext(connAttempt.fromIntent(intent).toCache());
                  } catch (Exception e) {
                    Log.e(TAG, "connectResult onReceive error", e);
                    connectResult.onNext(connAttempt);
                  }
                }
              };

              connectResult.subscribe(new Subscriber<ConnectAttempt>() {
                @Override public void onCompleted() {
                  if (!isUnsubscribed()) unsubscribe();
                }

                @Override public void onError(Throwable e) {
                  if (!isUnsubscribed()) unsubscribe();
                }

                @Override public void onNext(ConnectAttempt connectAttempt) {
                  if (!isUnsubscribed()) unsubscribe();
                  try {
                    appContext.unregisterReceiver(recv);
                  } catch (Exception ignored) {
                  }
                  checkConnectAttempt(connectAttempt, subscriber, true);
                }
              });

              try {
                appContext.registerReceiver(recv, new IntentFilter("li.vin.action.DEVICE_CHOSEN"));
                Intent i = new Intent();
                connAttempt.toIntent(i);
                i.putExtra("li.vin.my.choose_device", true);
                i.setClassName("li.vin.my", "li.vin.my.OAuthActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                connAttempt.activity().startActivity(i);
                Log.i(TAG, "startActivity OAuthActivity success");
              } catch (Exception e) {
                Log.i(TAG, "startActivity OAuthActivity failed", e);
                connectResult.onNext(connAttempt);
              }
            }
          }).delaySubscription(100, TimeUnit.MILLISECONDS);
        }
      };

  private static final Observable<DeviceConnection> mainBtAndConnect;

  static {
    // TODO: remove this workaround when rxjava is past version 1.0.14
    mainBtAndConnect = OperatorReplayFix.create(mainInit.flatMap(mainBt).flatMap(mainConnect), 1)
        .refCount()
        .take(1);
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
   * It is the caller's responsibility to call {@link AlertDialog#show()} on the returned
   * {@link AlertDialog}.
   */
  @SuppressWarnings("unused") public static @NonNull AlertDialog createMyVinliInstallRequestDialog(
      @NonNull Context context) {
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
      Log.e(TAG, "isBluetoothEnabled error", e);
      return false;
    }
  }

  /*package*/ static class ConnectAttempt {
    private final WeakReference<Context> contextRef;
    /*package*/ @NonNull final String clientId;
    /*package*/ @NonNull final String redirectUri;

    /*package*/
    @NonNull Activity activity() {
      Context context = contextRef.get();
      if (!(context instanceof Activity)) throw new ClassCastException("not an activity.");
      return (Activity) context;
    }

    private final String chipId;
    private final String devName;
    private final String devIcon;
    private final String devId;

    /*package*/ static class Builder {
      private WeakReference<Context> contextRef;
      private String clientId;
      private String redirectUri;

      private String chipId;
      private String devName;
      private String devIcon;
      private String devId;

      private Builder context(Context context) {
        contextRef = new WeakReference<>(context);
        return this;
      }

      private Builder clientId(String clientId) {
        this.clientId = clientId;
        return this;
      }

      private Builder redirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
      }

      /*package*/ Builder chipId(String chipId) {
        this.chipId = chipId;
        return this;
      }

      /*package*/ Builder devName(String devName) {
        this.devName = devName;
        return this;
      }

      /*package*/ Builder devIcon(String devIcon) {
        this.devIcon = devIcon;
        return this;
      }

      /*package*/ Builder devId(String devId) {
        this.devId = devId;
        return this;
      }

      /*package*/ ConnectAttempt build() {
        Context context;
        if (contextRef == null || (context = contextRef.get()) == null) {
          throw new NullPointerException("need context.");
        }
        if (clientId == null) throw new NullPointerException("need client id.");
        if (redirectUri == null) throw new NullPointerException("need redirect uri.");
        return new ConnectAttempt(context, clientId, redirectUri, chipId, devName, devIcon, devId);
      }
    }

    private ConnectAttempt(@NonNull Context context, @NonNull String clientId,
        @NonNull String redirectUri, String chipId, String devName, String devIcon, String devId) {
      this.contextRef = new WeakReference<>(context);
      this.clientId = clientId;
      this.redirectUri = redirectUri;
      this.chipId = chipId;
      this.devName = devName;
      this.devIcon = devIcon;
      this.devId = devId;
    }

    /*package*/ Builder builder() {
      return new Builder().context(contextRef.get())
          .clientId(clientId)
          .redirectUri(redirectUri)
          .chipId(chipId)
          .devName(devName)
          .devIcon(devIcon)
          .devId(devId);
    }

    private @Nullable Context context() {
      return contextRef.get();
    }

    private ConnectAttempt toIntent(@NonNull Intent i) {
      i.putExtra("li.vin.my.client_id", clientId);
      i.putExtra("li.vin.my.redirect_uri", redirectUri);
      return this;
    }

    private ConnectAttempt fromIntent(Intent i) {
      Context context = context();
      if (context == null) throw new NullPointerException("no context.");
      String chipId = i == null ? null : i.getStringExtra("li.vin.my.chip_id");
      String devName = i == null ? null : i.getStringExtra("li.vin.my.device_name");
      String devIcon = i == null ? null : i.getStringExtra("li.vin.my.device_icon");
      String devId = i == null ? null : i.getStringExtra("li.vin.my.device_id");
      return builder().chipId(chipId).devName(devName).devIcon(devIcon).devId(devId).build();
    }

    private ConnectAttempt fromCache() {
      Context context = context();
      if (context == null) throw new NullPointerException("no context.");
      SharedPreferences prefs = context.getApplicationContext()
          .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
      String chipId = prefs.getString(CHIP_ID_KEY, null);
      String devName = prefs.getString(DEV_NAME_KEY, null);
      String devIcon = prefs.getString(DEV_IC_KEY, null);
      String devId = prefs.getString(DEV_ID_KEY, null);
      return builder().chipId(chipId).devName(devName).devIcon(devIcon).devId(devId).build();
    }

    private ConnectAttempt toCache() {
      Context context = context();
      if (context == null) throw new NullPointerException("no context.");
      if (chipId != null && getTrimmedLength(chipId) != 0 &&
          devId != null && getTrimmedLength(devId) != 0) {
        context.getApplicationContext()
            .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(CHIP_ID_KEY, chipId)
            .putString(DEV_NAME_KEY, devName)
            .putString(DEV_IC_KEY, devIcon)
            .putString(DEV_ID_KEY, devId)
            .apply();
      }
      return this;
    }

    private ConnectAttempt clearCache() {
      Context context = context();
      if (context == null) throw new NullPointerException("no context.");
      context.getApplicationContext()
          .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .clear()
          .apply();
      return this;
    }
  }

  private VinliDevices() {
  }
}

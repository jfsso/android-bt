package li.vin.my.deviceservice.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * Created by christophercasey on 8/6/15.
 */
public final class ViewUtil {

  public interface AlertCallback {
    void onAlertComplete(boolean yes);
  }

  private static class AlertCallbackDispatcher {
    private AlertCallback cb;
    private boolean dispatched;

    AlertCallbackDispatcher(@NonNull AlertCallback cb) {
      this.cb = cb;
    }

    private void cleanup() {
      dispatched = true;
      cb = null;
    }

    void yes() {
      if (!dispatched) cb.onAlertComplete(true);
      cleanup();
    }

    void no() {
      if (!dispatched) cb.onAlertComplete(false);
      cleanup();
    }

    void cancel() {
      cleanup();
    }
  }

  public static void yesNoAlert(@NonNull Context ctx,
      String title, String msg, String yes, String no,
      final @NonNull AlertCallback callback) {
    yesNoAlert(ctx, title, msg, yes, no, callback, true);
  }

  public static void yesNoAlert(@NonNull Context ctx,
      String title, String msg, String yes, String no,
      final @NonNull AlertCallback callback,
      boolean cancelOnTouchOutside) {
    final AlertCallbackDispatcher cb = new AlertCallbackDispatcher(callback);
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle(title).setMessage(msg);
    if (yes != null) {
      builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          cb.yes();
          dialog.dismiss();
        }
      });
    }
    if (no != null) {
      builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          cb.no();
          dialog.dismiss();
        }
      });
    }
    AlertDialog dg = builder.create();
    dg.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        cb.cancel();
        dialog.dismiss();
      }
    });
    dg.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        cb.cancel();
        dialog.dismiss();
      }
    });
    dg.setCanceledOnTouchOutside(cancelOnTouchOutside);
    dg.show();
  }

  private ViewUtil() { }
}

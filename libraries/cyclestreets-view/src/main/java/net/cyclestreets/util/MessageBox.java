package net.cyclestreets.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class MessageBox
{
  public static final DialogInterface.OnClickListener NoAction = (arg0, arg1) -> { };

  public static void YesNo(final View parent,
                           final int msg,
                           final DialogInterface.OnClickListener yesAction) {
    YesNo(parent.getContext(), msg, yesAction);
  }

  public static void YesNo(final View parent,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction) {
    YesNo(parent.getContext(), msg, yesAction);
  }

  public static void YesNo(final Context context,
                           final int msg,
                           final DialogInterface.OnClickListener yesAction) {
    YesNo(context, context.getString(msg), yesAction, NoAction);
  }

  public static void YesNo(final Context context,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction) {
    YesNo(context, msg, yesAction, NoAction);
  }

  public static void YesNo(final View parent,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction,
                           final DialogInterface.OnClickListener noAction) {
    YesNo(parent.getContext(), msg, yesAction, noAction);
  }

  public static void YesNo(final Context context,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction,
                           final DialogInterface.OnClickListener noAction) {
    final AlertDialog.Builder alertbox = Dialog.newBuilder(context);
    alertbox.setMessage(msg)
            .setPositiveButton("Yes", yesAction)
            .setNegativeButton("No", noAction);
    Dialog.show(alertbox);
  }

  public static void OK(final View parent, final int msg) {
    OK(parent.getContext(), parent.getContext().getString(msg));
  }

  public static void OK(final View parent, final String msg) { OK(parent.getContext(), msg); }
  public static void OK(final Context context, final String msg) { OK(context, msg, NoAction); }

  public static void OK(final View parent,
                        final String msg,
                        final DialogInterface.OnClickListener okAction) { OK(parent.getContext(), msg, okAction); }

  public static void OK(final Context context,
                        final String msg,
                        final DialogInterface.OnClickListener okAction) {
    final AlertDialog.Builder alertbox = Dialog.newBuilder(context);
    alertbox.setMessage(msg)
            .setPositiveButton("OK", okAction);
    Dialog.show(alertbox);
  }

  public static void OkHtml(final Context context,
                            final String msg,
                            final DialogInterface.OnClickListener okAction) {
    final AlertDialog.Builder alertbox = Dialog.newBuilder(context);
    alertbox.setMessage(HtmlKt.fromHtml(msg))
        .setPositiveButton("OK", okAction);
    Dialog.show(alertbox);
  }

  public static void OKAndFinish(final View view,
                                 final String msg,
                                 final Activity activity,
                                 final boolean finishOnOK) {
    MessageBox.OK(view,
                  msg,
                  (arg0, arg1) -> {
                    if (finishOnOK)
                      activity.finish();
                  });
  }
}

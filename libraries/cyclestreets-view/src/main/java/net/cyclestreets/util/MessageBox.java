package net.cyclestreets.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class MessageBox 
{
  public static final DialogInterface.OnClickListener NoAction =
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {  }
      };
        
  static public void YesNo(final View parent,
                           final int msg,
                           final DialogInterface.OnClickListener yesAction)
  {
    YesNo(parent.getContext(), msg, yesAction);
  } // YesNo
  
  static public void YesNo(final View parent,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction)
  {
    YesNo(parent.getContext(), msg, yesAction);
  }

  static public void YesNo(final Context context,
                           final int msg,
                           final DialogInterface.OnClickListener yesAction)
  {
    YesNo(context, context.getString(msg), yesAction, NoAction);
  } // YesNo

  static public void YesNo(final Context context,
                           final String msg,
                           final DialogInterface.OnClickListener yesAction)
  {
    YesNo(context, msg, yesAction, NoAction);
  } // YesNo
  
  static public void YesNo(final View parent, 
                           final String msg, 
                           final DialogInterface.OnClickListener yesAction,
                           final DialogInterface.OnClickListener noAction)
  {
    YesNo(parent.getContext(), msg, yesAction, noAction);
  }

  static public void YesNo(final Context context, 
      final String msg, 
      final DialogInterface.OnClickListener yesAction,
      final DialogInterface.OnClickListener noAction)
{
    final AlertDialog.Builder alertbox = Dialog.newBuilder(context);
    alertbox.setMessage(msg)
            .setPositiveButton("Yes", yesAction)
            .setNegativeButton("No", noAction);
    Dialog.show(alertbox);
  } // YesNo
  
  static public void OK(final View parent, final int msg)
  {
    OK(parent.getContext(), parent.getContext().getString(msg));
  } // OK
  
  static public void OK(final View parent, final String msg) { OK(parent.getContext(), msg); } 
  static public void OK(final Context context, final String msg) { OK(context, msg, NoAction); }
  
  static public void OK(final View parent,
                        final String msg,
                        final DialogInterface.OnClickListener okAction) 
  { OK(parent.getContext(), msg, okAction); }
  
  static public void OK(final Context context,
                        final String msg,
                        final DialogInterface.OnClickListener okAction)
  {
    final AlertDialog.Builder alertbox = Dialog.newBuilder(context);
    alertbox.setMessage(msg)
            .setPositiveButton("OK", okAction);
    Dialog.show(alertbox);
  } // OK
  
  
  
  static public void OKAndFinish(final View view,
                                 final String msg,
                                 final Activity activity,
                                 final boolean finishOnOK)
  {
    MessageBox.OK(view, 
                  msg, 
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                      if(finishOnOK)
                        activity.finish();
                    }
                  });
  } // OKAndFinish  
} // class MessageBox

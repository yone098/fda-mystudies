package com.harvard;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;

public class MyAlertDialog extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    AlertDialog.Builder Builder =
        new AlertDialog.Builder(this)
            .setMessage(R.string.registration_message)
            .setTitle(R.string.why_register)
            .setCancelable(false)
            .setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    MyAlertDialog.this.finish();
                  }
                });
    AlertDialog alertDialog = Builder.create();
    alertDialog.show();
  }
}

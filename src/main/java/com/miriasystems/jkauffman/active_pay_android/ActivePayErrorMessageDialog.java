package com.miriasystems.jkauffman.active_pay_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jkauffman on 4/12/2016.
 */
//Dialog class
public class ActivePayErrorMessageDialog extends Dialog implements android.view.View.OnClickListener {
    public Dialog d;
    public Button okay;
    public String strErrorTitle;
    public String strErrorDescription;
    public String strErrorCode;

    public ActivePayErrorMessageDialog(Activity a, String strErrorTitle, String strErrorDescription, String strErrorCode) {
        super(a);
        this.strErrorTitle = strErrorTitle;
        this.strErrorCode = strErrorCode;
        this.strErrorDescription = strErrorDescription;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.resource_active_pay_error_dialog);
        okay = (Button) findViewById(R.id.btn_okay);
        okay.setOnClickListener(this);

        TextView tvErrorTitle = (TextView) findViewById(R.id.txt_error_title);
        TextView tvErrorDescription = (TextView) findViewById(R.id.txt_error_description);
        TextView tvErrorCode = (TextView) findViewById(R.id.txt_error_code);

        tvErrorTitle.setText(strErrorTitle);
        tvErrorDescription.setText(strErrorDescription);
        tvErrorCode.setText(strErrorCode);


    }

    @Override
    public void onClick(View v) {
        dismiss();

    }


}
package com.miriasystems.jkauffman.active_pay_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jkauffman on 4/12/2016.
 */
//Dialog class
public class ActivePayYesNoDialog extends Dialog implements View.OnClickListener {

    public Button okay;
    public Button cancel;
    public String strDialogTitle;
    public String strDialogDescription;
    public String authToken;
    public Activity a;
    private Boolean result = null;


    public ActivePayYesNoDialog(Activity a, String strDialogTitle, String strDialogDescription, String authToken) {
        super(a);
        this.a = a;
        this.strDialogTitle = strDialogTitle;
        this.strDialogDescription = strDialogDescription;
        this.authToken = authToken;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.resource_active_pay_yes_no_dialog);
        okay = (Button) findViewById(R.id.btn_yes_no_submit);
        okay.setOnClickListener(this);

        cancel = (Button) findViewById(R.id.btn_yes_no_cancel);
        cancel.setOnClickListener(this);

        TextView tvYesNoTitle = (TextView) findViewById(R.id.txt_yes_no_title);
        TextView tvYesNoDescription = (TextView) findViewById(R.id.txt_yes_no_description);

        tvYesNoTitle.setText(strDialogTitle);
        tvYesNoDescription.setText(strDialogDescription);


    }

   public Boolean getResult(){
       return result;
   }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_yes_no_submit:
                result = true;
                System.out.println("SUBMIT");
                dismiss();

                break;
            case R.id.btn_yes_no_cancel:
                result = false;
                System.out.println("CANCEL");
                dismiss();

                break;
            default:
                result = false;
                System.out.println("DEFAULT");
                dismiss();
                break;

        }

    }


}
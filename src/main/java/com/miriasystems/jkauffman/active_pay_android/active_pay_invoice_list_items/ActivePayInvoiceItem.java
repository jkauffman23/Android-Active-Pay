package com.miriasystems.jkauffman.active_pay_android.active_pay_invoice_list_items;

import java.text.NumberFormat;

/**
 * Created by jkauffman on 3/28/2016.
 */
public class ActivePayInvoiceItem {

    private String seqId;
    private String vendorName;
    private String grossAmount;
    private ActivePayInvoiceHeader headerInformation;

    public ActivePayInvoiceItem(String si, String vn, String ga){
        vendorName = vn;
        seqId = si;

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            grossAmount = formatter.format(Double.parseDouble(ga));
        } catch (NumberFormatException nfe) {
            grossAmount = "$0.00";
        }

    }
    public void setSeqId(String si){
        vendorName = si;
    }

    public void setVendorName(String vn){
        vendorName = vn;
    }
    public void setGrossAmount(String ga){

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            grossAmount = formatter.format(Double.parseDouble(ga));
        } catch (NumberFormatException nfe) {
            grossAmount = "$0.00";
        }

    }

    public String getSeqId(){
        return seqId;
    }
    public String getVendorName(){
        return vendorName;
    }
    public String getGrossAmount(){
        return grossAmount;
    }

}

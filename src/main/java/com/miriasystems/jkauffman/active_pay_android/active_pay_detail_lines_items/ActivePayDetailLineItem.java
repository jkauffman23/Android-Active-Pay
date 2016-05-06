package com.miriasystems.jkauffman.active_pay_android.active_pay_detail_lines_items;

import java.text.NumberFormat;

/**
 * Created by jkauffman on 3/28/2016.
 */
public class ActivePayDetailLineItem {

    private String itemCode;
    private String itemDescription;
    private String quantity;
    private String lineTotal;

    public ActivePayDetailLineItem(String ic, String id, String qty, String lt){
        itemCode = ic;
        itemDescription = id;
        quantity = qty;

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            lineTotal = formatter.format(Double.parseDouble(lt));
        } catch (NumberFormatException nfe) {
            lineTotal = "$0.00";
        }

    }
    public void setitemCode(String ic){itemCode = ic;}
    public void setItemDescription(String id){itemDescription = id;}
    public void setQuantity(String qty){
        quantity = qty;
    }
    public void setLineTotal(String lt){

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            lineTotal = formatter.format(Double.parseDouble(lt));
        } catch (NumberFormatException nfe) {
            lineTotal = "$0.00";
        }

    }

    public String getItemCode(){
        return itemCode;
    }
    public String getItemDescription(){
        return itemDescription;
    }
    public String getQuantity(){
        return quantity;
    }
    public String getLineTotal(){
        return lineTotal;
    }

}

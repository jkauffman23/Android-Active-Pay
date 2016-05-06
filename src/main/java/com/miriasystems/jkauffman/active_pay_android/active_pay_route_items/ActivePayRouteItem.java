package com.miriasystems.jkauffman.active_pay_android.active_pay_route_items;

import java.text.NumberFormat;

/**
 * Created by jkauffman on 3/28/2016.
 */
public class ActivePayRouteItem {

    private String firstname;
    private String lastname;
    private String username;
    private String seqid;

    public ActivePayRouteItem(String firstname, String lastname, String username, String seqid) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.seqid = seqid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeqid() {
        return seqid;
    }

    public void setSeqid(String seqid) {
        this.seqid = seqid;
    }



}

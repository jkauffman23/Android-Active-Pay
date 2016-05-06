package com.miriasystems.jkauffman.active_pay_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miriasystems.jkauffman.active_pay_android.active_pay_invoice_list_items.ActivePayInvoiceItem;


/**
 * A fragment representing a single Invoice detail screen.
 * This fragment is either contained in a {@link ActivePayInvoiceListActivity}
 * in two-pane mode (on tablets) or a {@link ActivePayInvoiceHeaderActivity}
 * on handsets.
 */
public class ActivePayInvoiceListFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ActivePayInvoiceItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActivePayInvoiceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_active_pay_invoice_header_table, container, false);

        // Show the dummy content as text in a TextView.
        //if (mItem != null) {
        //    ((TextView) rootView.findViewById(R.id.content_active_pay_invoice_header_table)).setText(mItem.getSeqId());
        //}

        return rootView;
    }
}

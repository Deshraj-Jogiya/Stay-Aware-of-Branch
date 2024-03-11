package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class PaymentInvoiceView extends SlidingFragmentActivity {

    private String invoiceID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_payment_invoice_view);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        findViewById(R.id.gen_loader).setVisibility(View.GONE);

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        invoiceID = getIntent().getStringExtra("invoice_id");

        ImageView ToggleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        if(invoiceID != null){
            loadData();
        }else{
            changePageView(pageLayer.ErrorLoading);
        }


        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(invoiceID != null){
                    loadData();
                }else{
                    changePageView(pageLayer.ErrorLoading);
                }
            }
        });
    }


    private enum pageLayer {
        ErrorLoading,
        DataView,
        Loading
    }

    public void changePageView(pageLayer layerIndex) {
        findViewById(R.id.error_view).setVisibility(layerIndex.equals(pageLayer.ErrorLoading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.loading_view).setVisibility(layerIndex.equals(pageLayer.Loading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.data_view).setVisibility(layerIndex.equals(pageLayer.DataView) ? View.VISIBLE : View.GONE);
    }

    public void loadData() {
        String TOKEN = Concurrent.getAppToken(getBaseContext());
        if (TOKEN != null) {
            changePageView(pageLayer.Loading);

            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOAD_INVOICE_DETAILS + "/" + invoiceID);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changePageView(pageLayer.ErrorLoading);
                            if (e instanceof ConnectException) {
                                Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    showError(e.getMessage());
                                } else {
                                    showError("5012");
                                }
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response)serverResponse;
                    final String response;
                    try {
                        response = responseObj.body().string();
                    } catch (Exception e) {
                        showError("5001");
                        return;
                    }

                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder = null;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                            Log.e("zAzaza"," "+ValuesHolder);

                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder == null) {
                                            changePageView(pageLayer.ErrorLoading);
                                            return;
                                        }

                                        JsonObject paymentData = ValuesHolder.get("payment").getAsJsonObject();
                                        JsonObject userData = ValuesHolder.get("user").getAsJsonObject();

                                        if(paymentData != null ){
                                            JsonArray paymentRows = new JsonArray();
                                            JsonArray collectionsArray = new JsonArray();
                                            if(paymentData.has("paymentRows") && !paymentData.get("paymentRows").isJsonNull()){
                                                paymentRows = paymentData.get("paymentRows").getAsJsonArray();
                                            }
                                            if(ValuesHolder.has("collection") && !ValuesHolder.get("collection").isJsonNull()){
                                                collectionsArray = ValuesHolder.get("collection").getAsJsonArray();
                                            }


                                            /**********************************
                                             *  Header Part
                                             *  *******************************
                                             */
                                            String currencySymbolValue = Concurrent.tagsStringValidator(ValuesHolder,"currency_symbol");

                                            //===== Inv name ======//
                                            String headerInvoiceNameValue = Concurrent.tagsStringValidator(paymentData,"paymentTitle");
                                            if(!headerInvoiceNameValue.equals("")) ((TextView)findViewById(R.id.headerInvoiceName)).setText(headerInvoiceNameValue);

                                            //===== Inv Description ======//
                                            String headerInvoiceDescValue = Concurrent.tagsStringValidator(paymentData,"paymentDescription");
                                            if(!headerInvoiceDescValue.equals("")) ((TextView)findViewById(R.id.headerInvoiceDesc)).setText(headerInvoiceDescValue);

                                            //===== Total Amount ======//
                                            String headerTotalValue = Concurrent.tagsStringValidator(ValuesHolder,"totalWithTax");
                                            if(!headerTotalValue.equals("")) ((TextView)findViewById(R.id.headerTotal)).setText(currencySymbolValue+" "+headerTotalValue);

                                            //===== Paid Amount ======//
                                            String headerPaidValue = Concurrent.tagsStringValidator(paymentData,"paidAmount");
                                            if(!headerPaidValue.equals("")) ((TextView)findViewById(R.id.headerPaid)).setText(currencySymbolValue+" "+headerPaidValue);

                                            //===== Paid Amount ======//
                                            String headerPendingValue = Concurrent.tagsStringValidator(ValuesHolder,"pendingAmount");
                                            if(!headerPendingValue.equals("")) ((TextView)findViewById(R.id.headerPending)).setText(currencySymbolValue+" "+headerPendingValue);

                                            /**
                                             *  Cart Header
                                             */
                                            //===== Paid Status ======//
                                            Integer payStatusValue = Concurrent.tagsIntValidator(paymentData,"paymentStatus");
                                            String statusText = "NA";
                                            if (payStatusValue == 0)
                                                statusText = Concurrent.getLangSubWords("unpaid", "UNPAID");
                                            else if (payStatusValue == 1)
                                                statusText = Concurrent.getLangSubWords("paid", "PAID");
                                            else if (payStatusValue == 2)
                                                statusText = Concurrent.getLangSubWords("ppaid", "Partially Paid");

                                            ((TextView)findViewById(R.id.payStatus)).setText(statusText);

                                            //===== Pay Method ======//
                                            String payMethodValue = Concurrent.tagsStringValidator(paymentData,"paidMethod");
                                            if(!payMethodValue.equals("")) ((TextView)findViewById(R.id.payMethod)).setText(payMethodValue);
                                            else findViewById(R.id.payMethod).setVisibility(View.GONE);

                                            //===== Pay Date ======//
                                            String payDateValue = Concurrent.tagsStringValidator(paymentData,"paidTime");
                                            if(!payDateValue.equals("") && payDateValue.contains("/")) ((TextView)findViewById(R.id.payDate)).setText(payDateValue);
                                            else findViewById(R.id.payDate).setVisibility(View.GONE);

                                            /**********************************
                                             *  From User
                                             *  *******************************
                                             */

                                            //===== From User FullName ======//
                                            String fromNameValue = Concurrent.tagsStringValidator(ValuesHolder,"siteTitle");
                                            if(!fromNameValue.equals("")) ((TextView)findViewById(R.id.fromName)).setText(fromNameValue);

                                            //===== From User Phone ======//
                                            String fromPhoneValue = Concurrent.tagsStringValidator(ValuesHolder,"phoneNo");
                                            if(!fromPhoneValue.equals("")) ((TextView)findViewById(R.id.fromPhone)).setText(fromPhoneValue);
                                            else findViewById(R.id.fromPhoneCon).setVisibility(View.GONE);

                                            //===== From User Email ======//
                                            String fromEmailValue = Concurrent.tagsStringValidator(ValuesHolder,"systemEmail");
                                            if(!fromEmailValue.equals("")) ((TextView)findViewById(R.id.fromEmail)).setText(fromEmailValue);
                                            else findViewById(R.id.fromEmailCon).setVisibility(View.GONE);

                                            //===== From User Address 1 ======//
                                            String fromAddress1Value = Concurrent.tagsStringValidator(ValuesHolder,"address");
                                            if(!fromAddress1Value.equals("")) ((TextView)findViewById(R.id.fromAddress1)).setText(fromAddress1Value);
                                            else findViewById(R.id.fromAddress1Con).setVisibility(View.GONE);

                                            //===== From User Address 2 ======//
                                            String fromAddress2Value = Concurrent.tagsStringValidator(ValuesHolder,"address2");
                                            if(!fromAddress2Value.equals("")) ((TextView)findViewById(R.id.fromAddress2)).setText(fromAddress2Value);
                                            else findViewById(R.id.fromAddress2Con).setVisibility(View.GONE);

                                            /**********************************
                                             *  To User
                                             *  *******************************
                                             */

                                            //===== From User FullName ======//
                                            String toNameValue = Concurrent.tagsStringValidator(userData,"fullName");
                                            if(!toNameValue.equals("")) ((TextView)findViewById(R.id.toName)).setText(toNameValue);

                                            //===== From User Photo ======//
                                            String toUserIDValue = Concurrent.tagsStringValidator(userData,"id");
                                            if(!toUserIDValue.equals("")){
                                                CustomImageView toUserImage = (CustomImageView) findViewById(R.id.toPhoto);
                                                toUserImage.profileID = String.valueOf(toUserIDValue);
                                                toUserImage.load();
                                            }

                                            //===== From User Phone ======//
                                            //String toPhoneValue = Concurrent.tagsStringValidator(userData,"phoneNo");
                                            String toPhoneValue = Concurrent.tagsStringValidator(userData,"username");
                                            if(!toPhoneValue.equals("")) ((TextView)findViewById(R.id.toPhone)).setText(toPhoneValue);
                                            else findViewById(R.id.toPhoneCon).setVisibility(View.GONE);


                                            String toLevel = Concurrent.tagsStringValidator(userData,"cat_title");
                                            if(!toPhoneValue.equals("")) ((TextView)findViewById(R.id.toLevel2)).setText(toLevel);
                                            else findViewById(R.id.toLevel).setVisibility(View.GONE);

                                            //===== From User Email ======//
                                            String toEmailValue = Concurrent.tagsStringValidator(userData,"email");
                                            if(!toEmailValue.equals("")) ((TextView)findViewById(R.id.toEmail)).setText(toEmailValue);
                                            else findViewById(R.id.toEmailCon).setVisibility(View.GONE);

                                            //===== From User Address 1 ======//
                                            String toAddress1Value = Concurrent.tagsStringValidator(userData,"className");
                                            if(!toAddress1Value.equals("")) ((TextView)findViewById(R.id.toAddress1)).setText(toAddress1Value);
                                            else findViewById(R.id.toAddress1Con).setVisibility(View.GONE);

                                            //===== From User Address 2 ======//
                                            String toClassValue = Concurrent.tagsStringValidator(userData,"sectionTitle");
                                            if(!toClassValue.equals("")) ((TextView)findViewById(R.id.toClass)).setText(toClassValue);
                                            else findViewById(R.id.toClassCon).setVisibility(View.GONE);

                                            String toSectionValue = Concurrent.tagsStringValidator(userData,"sectionName");
                                            if(!toSectionValue.equals("")) ((TextView)findViewById(R.id.toSection)).setText(toSectionValue);
                                            else findViewById(R.id.toClassCon).setVisibility(View.GONE);


                                            /**********************************
                                             *  Sum Subtotal and tax
                                             *  *******************************
                                             */

                                            //===== Subtotal ======//
                                            String subtotalValue = Concurrent.tagsStringValidator(paymentData,"paymentAmount");
                                            if(!subtotalValue.equals("")) ((TextView)findViewById(R.id.subTotal)).setText(currencySymbolValue+" "+subtotalValue);

                                            //===== Tax ======//
                                            String payTaxValue = Concurrent.tagsStringValidator(ValuesHolder,"amountTax");
                                            if(!payTaxValue.equals("")){
                                                ((TextView)findViewById(R.id.payTax)).setText(currencySymbolValue+" "+payTaxValue);
                                                ((TextView)findViewById(R.id.payTaxPercent)).setText(Concurrent.getLangSubWords("payTax", "Payment Tax")+" ("+Concurrent.tagsStringValidator(ValuesHolder,"paymentTax")+"%)");

                                            }

                                            //===== Total ======//
                                            String payTotalValue = Concurrent.tagsStringValidator(ValuesHolder,"totalWithTax");
                                            if(!payTotalValue.equals("")) {
                                                ((TextView) findViewById(R.id.payTotal)).setText(payTotalValue);
                                                ((TextView) findViewById(R.id.payTotalCurrency)).setText(currencySymbolValue);
                                            }

                                            /**********************************
                                             *  Date and due date
                                             *  *******************************
                                             */
                                            //===== Invoice Date ======//
                                            String invoiceDateValue = Concurrent.tagsStringValidator(paymentData,"paymentDate");
                                            if(!invoiceDateValue.equals("")) /*((TextView)findViewById(R.id.invoiceDate)).setText(Concurrent.getLangSubWords("date", "Date Payment")+" : "+invoiceDateValue);*/
                                                ((TextView)findViewById(R.id.invoiceDate)).setText("Payment Date"+" : "+invoiceDateValue);

                                            //===== Invoice Due Date ======//
                                            String invoiceDueDateValue = Concurrent.tagsStringValidator(paymentData,"dueDate");
                                            if(!invoiceDueDateValue.equals("")) ((TextView)findViewById(R.id.invoiceDueDate)).setText(Concurrent.getLangSubWords("dueDate", "Due Date")+" : "+invoiceDueDateValue);

                                            /**********************************
                                             *  Products & Payments
                                             *  *******************************
                                             */

                                            //===== Products ======//
                                            LinearLayout pLayoutParent = (LinearLayout) findViewById(R.id.products_layout_parent);
                                            LinearLayout pLayout = (LinearLayout) findViewById(R.id.products_layout);

                                            if(paymentRows != null && paymentRows.size() > 0){
                                                pLayoutParent.setVisibility(View.VISIBLE);

                                                for(JsonElement pRow:paymentRows){
                                                    JsonObject CurrObj = pRow.getAsJsonObject();
                                                    if(CurrObj != null){
                                                        View itemView = getLayoutInflater().inflate(R.layout.page_invoice_product_row, null);

                                                        String pRowTitle = Concurrent.tagsStringValidator(CurrObj,"title");
                                                        if(pRowTitle != null)((TextView)itemView.findViewById(R.id.product)).setText(pRowTitle);

                                                        ((TextView)itemView.findViewById(R.id.subtotal)).setText(currencySymbolValue+" "+Concurrent.tagsStringValidator(CurrObj,"amount"));
                                                        pLayout.addView(itemView);
                                                    }
                                                }
                                            }else{
                                                pLayoutParent.setVisibility(View.GONE);
                                            }

                                            //===== Collections ======//

                                            LinearLayout payLayoutParent = (LinearLayout) findViewById(R.id.payments_layout_parent);
                                            LinearLayout payLayout = (LinearLayout) findViewById(R.id.payments_layout);

                                            if(collectionsArray != null && collectionsArray.size() > 0){
                                                payLayoutParent.setVisibility(View.VISIBLE);

                                                for(JsonElement pRow:collectionsArray){
                                                    JsonObject CurrObj = pRow.getAsJsonObject();
                                                    if(CurrObj != null){
                                                        View itemView = getLayoutInflater().inflate(R.layout.page_invoice_payment_row, null);

                                                        ((TextView)itemView.findViewById(R.id.collectionDate)).setText(Concurrent.tagsStringValidator(CurrObj,"collectionDate"));
                                                        ((TextView)itemView.findViewById(R.id.collectionAmount)).setText(currencySymbolValue+" "+Concurrent.tagsStringValidator(CurrObj,"collectionAmount"));
                                                        ((TextView)itemView.findViewById(R.id.collectionMethod)).setText(Concurrent.tagsStringValidator(CurrObj,"collectionMethod"));

                                                        String notes = Concurrent.tagsStringValidator(CurrObj,"collectionNote");
                                                        if (notes.equals("")){
                                                            itemView.findViewById(R.id.collectionNote).setVisibility(View.GONE);
                                                        }else{
                                                            ((TextView)itemView.findViewById(R.id.collectionNote)).setText(Concurrent.getLangSubWords("notes", "Notes")+" : "+notes);
                                                        }
                                                        payLayout.addView(itemView);
                                                    }
                                                }
                                            }else{
                                                payLayoutParent.setVisibility(View.GONE);
                                            }

                                        }else{
                                            changePageView(pageLayer.ErrorLoading);
                                        }
                                        changePageView(pageLayer.DataView);
                                    } else {
                                        showError("5010");
                                    }
                                } catch (final Exception e) {
                                    showError("5002");
                                }
                            }
                        });
                    } else {
                        showError("5001");

                    }
                }
            });
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        changePageView(pageLayer.ErrorLoading);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

}
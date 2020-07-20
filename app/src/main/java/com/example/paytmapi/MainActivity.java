package com.example.paytmapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.parthav46.httphandler.HttpRequestCallback;
import io.github.parthav46.httphandler.HttpResponseCallback;

public class MainActivity extends AppCompatActivity {

    final String ORDER_ID = "ID" + System.currentTimeMillis();
    final int requestCode = 2;
    LoaderManager loaderManager;
    String bodyData = "";

    MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activity = this;
        loaderManager = LoaderManager.getInstance(this);

        findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bodyData = getPaytmParams();

                Bundle params = new Bundle();
                params.putString("url", Constants.CHECKSUM);
                params.putString("data", bodyData);

                loaderManager.initLoader(0, params, new HttpRequestCallback(getBaseContext(), new HttpResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        if(response != null) {
                            try {

                                JSONObject paytmParams = new JSONObject();

                                JSONObject head = new JSONObject();

                                String checksum = new JSONObject(response).getString("checksum");
                                Log.e("checksum", checksum);
                                head.put("signature", checksum);

                                paytmParams.put("head", head);
                                paytmParams.put("body", new JSONObject(bodyData));



                                String url = "https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction?mid=" + Constants.MERCHANT_ID + "&orderId=" + ORDER_ID;
                                Bundle params = new Bundle();
                                params.putString("url", url);
                                params.putString("data", paytmParams.toString());

                                loaderManager.initLoader(1, params, new HttpRequestCallback(getBaseContext(), new HttpResponseCallback() {
                                    @Override
                                    public void onResponse(String response) {
                                        if(response != null) {
                                            try {
                                                processPaytmTransaction(new JSONObject(response));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }));
            }
        });
    }

    String getPaytmParams () {
        JSONObject paytmParams;
        try {
            JSONObject body = new JSONObject();
            body.put("requestType", "Payment");
            body.put("mid", Constants.MERCHANT_ID);
            body.put("websiteName", Constants.WEBSITE);
            body.put("orderId", ORDER_ID);
            body.put("callbackUrl", Constants.CALLBACK);

            JSONObject txnAmount = new JSONObject();
            txnAmount.put("value", "1.00");
            txnAmount.put("currency", "INR");

            JSONObject userInfo = new JSONObject();
            userInfo.put("custId", "CUST_001");

            body.put("txnAmount", txnAmount);
            body.put("userInfo", userInfo);

            /*
             * Generate checksum by parameters we have in body
             * You can get Checksum JAR from https://developer.paytm.com/docs/checksum/
             * Find your Merchant Key in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys
             */

            paytmParams = body;

        } catch (Exception e) {
            e.printStackTrace();
            paytmParams = new JSONObject();
        }
        return paytmParams.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.requestCode && data != null) {
            String nsdk = data.getStringExtra("nativeSdkForMerchantMessage");
            String response = data.getStringExtra("response");
            Toast.makeText(this, nsdk + response, Toast.LENGTH_SHORT).show();
        }
    }

    void processPaytmTransaction(JSONObject data) {
        try {
            Log.i("CHECKSUM", data.getJSONObject("body").toString());
            Log.i("CHECKSUM", data.getJSONObject("head").getString("signature"));
            Log.e("CHECKSUM", PaytmChecksum.verifySignature(data.getJSONObject("body").toString(), Constants.MERCHANT_KEY, data.getJSONObject("head").getString("signature")) ? "MATCH" : "MISMATCH");
            Log.e("TXN_TOKEN", data.getJSONObject("body").getString("txnToken"));

            PaytmOrder paytmOrder = new PaytmOrder(ORDER_ID, Constants.MERCHANT_ID, data.getJSONObject("body").getString("txnToken"), "1.00", Constants.CALLBACK);
            TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
                @Override
                public void onTransactionResponse(Bundle bundle) {
                    Toast.makeText(getApplicationContext(), "Payment Transaction response " + bundle.toString(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void networkNotAvailable() {
                    Log.e("RESPONSE", "network not available");
                }

                @Override
                public void onErrorProceed(String s) {
                    Log.e("RESPONSE", "error proceed: " + s);

                }

                @Override
                public void clientAuthenticationFailed(String s) {
                    Log.e("RESPONSE", "client auth failed: " + s);

                }

                @Override
                public void someUIErrorOccurred(String s) {
                    Log.e("RESPONSE", "UI error occured: " + s);

                }

                @Override
                public void onErrorLoadingWebPage(int i, String s, String s1) {
                    Log.e("RESPONSE", "error loading webpage: " + s + "--" + s1);

                }

                @Override
                public void onBackPressedCancelTransaction() {
                    Log.e("RESPONSE", "back pressed");

                }

                @Override
                public void onTransactionCancel(String s, Bundle bundle) {
                    Log.e("RESPONSE", "transaction cancel: " + s);

                }
            });
            transactionManager.startTransaction(this.activity, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
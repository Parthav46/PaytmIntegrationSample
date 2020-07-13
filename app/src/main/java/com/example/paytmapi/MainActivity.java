package com.example.paytmapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    final String ORDER_ID = "ID" + System.currentTimeMillis();
    final int requestCode = 2;
    LoaderManager loaderManager;

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
                String url = "https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction?mid=" + Constants.MERCHANT_ID + "&orderId=" + ORDER_ID;
                String data = getPaytmParams();

                Bundle params = new Bundle();
                params.putString("url", url);
                params.putString("data", data);

                loaderManager.initLoader(0, params, httpRequestCallback);
            }
        });
    }

    String getPaytmParams () {
        JSONObject paytmParams = new JSONObject();
        try {
            JSONObject body = new JSONObject();
            body.put("requestType", "Payment");
            body.put("mid", Constants.MERCHANT_ID);
            body.put("websiteName", Constants.WEBSITE);
            body.put("orderId", ORDER_ID);

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

            String checksum = PaytmChecksum.generateSignature(body.toString(), Constants.MERCHANT_KEY);

            JSONObject head = new JSONObject();
            head.put("signature", checksum);

            paytmParams.put("body", body);
            paytmParams.put("head", head);

        } catch (Exception e) {
            e.printStackTrace();
            paytmParams = new JSONObject();
        }
        return paytmParams.toString();
    }

    LoaderManager.LoaderCallbacks<JSONObject> httpRequestCallback = new LoaderManager.LoaderCallbacks<JSONObject>() {
        String bodyData;

        @NonNull
        @Override
        public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
            assert args != null;
            try {
                JSONObject obj = new JSONObject(args.getString("data", ""));
                bodyData = obj.getJSONObject("body").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new HttpRequestAsyncLoader(getBaseContext(), args.getString("url", null), args.getString("data", null), HttpRequestAsyncLoader.Request.POST);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
            System.out.println("-----OUTPUT-----");
            if(data == null) System.out.println("x----FAIL----x");
            else {
                System.out.println(data.toString());
                try {
                    Log.i("CHECKSUM", data.getJSONObject("body").toString());
                    Log.i("CHECKSUM", data.getJSONObject("head").getString("signature"));
                    Log.e("CHECKSUM", PaytmChecksum.verifySignature(data.getJSONObject("body").toString(), Constants.MERCHANT_KEY, data.getJSONObject("head").getString("signature")) ? "MATCH" : "MISMATCH");
                    Log.e("TXN_TOKEN", data.getJSONObject("body").getString("txnToken"));

                    String host = "https://securegw.paytm.in/";
                    String callBackUrl = host + "theia/paytmCallback?ORDER_ID=" + ORDER_ID;

                    PaytmOrder paytmOrder = new PaytmOrder(ORDER_ID, Constants.MERCHANT_ID, data.getJSONObject("body").getString("txnToken"), "1.00", callBackUrl);
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
                    transactionManager.startTransaction(activity, requestCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.requestCode && data != null) {
            String nsdk = data.getStringExtra("nativeSdkForMerchantMessage");
            String response = data.getStringExtra("response");
            Toast.makeText(this, nsdk + response, Toast.LENGTH_SHORT).show();
        }
    }
}
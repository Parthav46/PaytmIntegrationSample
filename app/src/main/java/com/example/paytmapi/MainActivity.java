package com.example.paytmapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.os.Bundle;
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
                params.putString("url", "http://192.168.29.51:8000");
                params.putString("data", bodyData);

                loaderManager.initLoader(0, params, checksumCallback);

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
            body.put("callbackUrl", "https://merchant.com/callback");

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

//            String checksum = PaytmChecksum.generateSignature(body.toString(), Constants.MERCHANT_KEY);
//            Log.e("CHECKSUM", PaytmChecksum.verifySignature(body.toString(), Constants.MERCHANT_KEY, checksum) ? "MATCH" : "MISMATCH");

            paytmParams = body;

        } catch (Exception e) {
            e.printStackTrace();
            paytmParams = new JSONObject();
        }
        return paytmParams.toString();
    }

    LoaderManager.LoaderCallbacks<JSONObject> checksumCallback = new LoaderManager.LoaderCallbacks<JSONObject>() {
        @NonNull
        @Override
        public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
            assert args != null;
            return new HttpRequestAsyncLoader(getBaseContext(), args.getString("url", null), args.getString("data", null), HttpRequestAsyncLoader.Request.POST);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {

            JSONObject paytmParams = new JSONObject();

            JSONObject head = new JSONObject();
            try {
                head.put("signature", data.getString("checksum"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                paytmParams.put("body", new JSONObject(bodyData));
                paytmParams.put("head", head);


                String url = "https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction?mid=" + Constants.MERCHANT_ID + "&orderId=" + ORDER_ID;
                Bundle params = new Bundle();
                params.putString("url", url);
                params.putString("data", paytmParams.toString());

                loaderManager.initLoader(1, params, httpRequestCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

        }
    };

    LoaderManager.LoaderCallbacks<JSONObject> httpRequestCallback = new LoaderManager.LoaderCallbacks<JSONObject>() {
        @NonNull
        @Override
        public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
            assert args != null;
            return new HttpRequestAsyncLoader(getBaseContext(), args.getString("url", null), args.getString("data", null), HttpRequestAsyncLoader.Request.POST);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
            System.out.println("-----OUTPUT-----");
            if(data == null) System.out.println("x----FAIL----x");
            else {
                System.out.println(data.toString());
                try {
                    System.out.println(data.getJSONObject("body").getString("txnToken"));
                    PaytmOrder paytmOrder = new PaytmOrder(ORDER_ID, Constants.MERCHANT_ID, data.getJSONObject("body").getString("txnToken"), "1.00", "https://merchant.com/callback");
                    TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
                        @Override
                        public void onTransactionResponse(Bundle bundle) {
                            Toast.makeText(getApplicationContext(), "Payment Transaction response " + bundle.toString(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void networkNotAvailable() {

                        }

                        @Override
                        public void onErrorProceed(String s) {

                        }

                        @Override
                        public void clientAuthenticationFailed(String s) {

                        }

                        @Override
                        public void someUIErrorOccurred(String s) {

                        }

                        @Override
                        public void onErrorLoadingWebPage(int i, String s, String s1) {

                        }

                        @Override
                        public void onBackPressedCancelTransaction() {

                        }

                        @Override
                        public void onTransactionCancel(String s, Bundle bundle) {

                        }
                    });
                    transactionManager.startTransaction(activity, requestCode);
                } catch (JSONException e) {
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
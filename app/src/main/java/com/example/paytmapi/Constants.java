package com.example.paytmapi;

public class Constants {

    // Paytm Merchant Staging Credentials obtained from https://dashboard.paytm.com/next/apikeys
    final static String MERCHANT_ID = "IoEjZE36507440637572";
    final static String WEBSITE = "WEBSTAGING";
    final static String HOST = "http://192.168.1.8:5001/donationapp-test/us-central1"; // Firebase functions url
    final static String CALLBACK = HOST + "/response";
    final static String CHECKSUM = HOST + "/checksum";
}

# Paytm All-in-One SDK Sample

This repo consists of sample Android app project for using [Paytm All-in-One SDK](https://developer.paytm.com/docs/all-in-one-sdk) implemented along with server side code run on Firebase Functions. The server side code can be found at [PaytmIntegrationSampleServer](https://github.com/Parthav46/PaytmIntegrationSampleServer).

## Prerequisite:

1. Obtain Paytm Merchant credentials from [https://dashboard.paytm.com/next/apikeys](https://dashboard.paytm.com/next/apikeys)
2. Setup your server side code on Firebase functions from [PaytmIntegrationSampleServer](https://github.com/Parthav46/PaytmIntegrationSampleServer)

## Setup:

- Fork and open the repo in Android Studio
- Replace the paytm merchant credentials in [Constants.java](./app/src/main/java/com/example/paytmapi/Constants.java) with your credentials obtained from [https://dashboard.paytm.com/next/apikeys](https://dashboard.paytm.com/next/apikeys)
- Uninstall release version of Paytm app if installed on your testing device and install the [Paytm Staging](https://drive.google.com/file/d/1BfvEa04KStQ0WDGG9L4Fd75lKJlrp1X9/view?usp=sharing) app instead. <br> 
    Login Credentials: <br>
    &emsp; Username:- 7777777777 <br>
    &emsp; Password:- Paytm12345
    
 ## Contributing Guidelines:
- Fork the repo to your account and then clone it on your machine
- Make a separate branch for the changes you make with a meaningful name
- Commit your changes to the branch
- Make a Pull Request to [master](../../tree/master) with PR body containing description of changes you made and link to its issue.

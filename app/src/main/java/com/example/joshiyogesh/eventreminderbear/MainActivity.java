package com.example.joshiyogesh.eventreminderbear;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
* MainActivity used for Google Api. It demonstrates how to use authorization to
*add event in Google Calender
* */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICE = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNT = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleAccountCredential googleAccountCredential;
    private static final String[] scopes = {CalendarScopes.CALENDAR_READONLY};

    ProgressDialog progressDialog;
    /**
    * Create the main Activity.
    * @param savedInstanceState previously saved instance state
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Initialize credentials and service object
            googleAccountCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(scopes)).
                    setBackOff(new ExponentialBackOff());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Calling Google Calendar API ...");


        sendResultToApi();

    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */

    private void sendResultToApi(){
        if(! isGooglePlayServiceAvailable()){
            acquireGooglePlayService();
        }
        else if (googleAccountCredential.getSelectedAccountName() == null){
            chooseAccount();
        }
        else if (! isDeviceOnline()){
            Toast.makeText(MainActivity.this,"Check Your Network Connection",Toast.LENGTH_LONG).show();
        }
        else{
            new MakeRequestTask(googleAccountCredential).execute();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServiceAvailable(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
    * Checks whether Device has Active Network Connection or not.
     * @return true if Network (Internet) is available ;
     * false otherwise
     */
    private boolean isDeviceOnline(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayService(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (googleApiAvailability.isUserResolvableError(connectionStatusCode)){
            showGooglePlayServiceAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    /**
    *Display an Dialog showing Google Play Service is missing or Out of date
     * @paramConnectionStatusCode code describing the prescence or Lack :: Goggle Play service on Device
     */
    void showGooglePlayServiceAvailabilityErrorDialog(int connectionStatuscode){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = googleApiAvailability.getErrorDialog(MainActivity.this,connectionStatuscode,REQUEST_GOOGLE_PLAY_SERVICE);
        dialog.show();

    }

    /**
    * Display currently LogIn Account on Device for selecting Account ,
    *so that CalendarId can be get For That Account..
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. The setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     *  */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNT)
    private void chooseAccount(){
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)){
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME,null);
            if (accountName != null){
                googleAccountCredential.setSelectedAccountName(accountName);
                sendResultToApi();
            }
            else {
                //start a dialog so that user can select Acount Name
                startActivityForResult(googleAccountCredential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
            }
        }else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNT,
                    Manifest.permission.GET_ACCOUNTS);

        }

    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @paramrequestCode code indicating which activity result is incoming.
     * @paramresultCode code indicating the result of the incoming
     *     activity result.
     * @paramdata Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQUEST_GOOGLE_PLAY_SERVICE:
                if (resultCode != RESULT_OK){
                    Toast.makeText(MainActivity.this,"This App Requires Google Play Service ! Please Install it and Try Again Later",Toast.LENGTH_LONG).show();
                }else {
                    sendResultToApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        googleAccountCredential.setSelectedAccountName(accountName);
                        sendResultToApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    sendResultToApi();
                }
                break;

        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }


    /**
    * this methods helps to call PickerDialog (Choosing Date)
    */
    public void setDate(View view) {
        PickerDialog pickerDialogs = new PickerDialog();
        pickerDialogs.show(getSupportFragmentManager(),"Date_Picker");
    }

    /**---------methods implemented from EasyPermissions.PermissionCallbacks---------*/
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
    /**------------------------------------------------------------------------------*/


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */

    private class MakeRequestTask extends AsyncTask<Void,Void,Void>{
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        Event event;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }
        @Override
        protected void onPreExecute(){
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
            Toast.makeText(MainActivity.this, "Your Event Has been Successfully added  "+event.getHtmlLink(), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServiceAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(MainActivity.this,"The following error occurred:\n"+mLastError.getMessage(),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this,"Request Cancelled",Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            createEvent();
            return null;
        }
        /**
         * createEvent() method help in adding Event in google play service Calendar
        * */
        private void createEvent(){
             event = new Event()
                    .setSummary("There is Placement Drive")
                    .setLocation("Jalandhar")
                    .setDescription("Elite SmartCare Solutions is going to visit");
            DateTime date = new DateTime("2017-04-11T09:00:00-05:30");
            EventDateTime start = new EventDateTime()
                    .setDateTime(date)
                    .setTimeZone("Asia/Calcutta");
            event.setStart(start);

            DateTime endDateTime = new DateTime("2017-04-12T09:00:00-05:30");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Calcutta");
            event.setEnd(end);

            String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
            event.setRecurrence(Arrays.asList(recurrence));

            EventAttendee[] attendees = new EventAttendee[] {
                    new EventAttendee().setEmail("yogeshcj.cs.14@nitj.ac.in"),
                    new EventAttendee().setEmail("joshiyogesh002@gmail.com"),
            };
            event.setAttendees(Arrays.asList(attendees));

            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = "primary";
            try {
                event = mService.events().insert(calendarId, event).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

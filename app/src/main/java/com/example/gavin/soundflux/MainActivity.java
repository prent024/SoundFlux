package com.example.gavin.soundflux;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.text.DecimalFormat;


public class MainActivity extends FragmentActivity
            implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    private GoogleApiClient mGoogleApiClient;
    private AudioManager audioMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the two error textviews
        TextView susp = (TextView)findViewById(R.id.susp);
        susp.setVisibility(View.GONE);
        TextView issues = (TextView)findViewById(R.id.issues);
        issues.setVisibility(View.GONE);
        // Initialize the Audio Manager
        audioMan = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // Create a GoogleApiClient instance
        buildGoogleApiClient();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        TextView susp = (TextView)findViewById(R.id.susp);
        susp.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        TextView issues = (TextView)findViewById(R.id.issues);
        issues.setVisibility(View.VISIBLE);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected float convertToMPH(float mps) {
        return mps * 2.23494f;
    }

    protected float convertToMPS(float mph) {
        return mph / 2.23494f;
    }

    protected boolean isEmpty(String s) {
        return s.matches("");
    }

    @Override
    public void onLocationChanged(Location location) {
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
        EditText thresh_edit = (EditText)findViewById(R.id.thresh);
        EditText act_edit    = (EditText)findViewById(R.id.act);
        EditText pass_edit   = (EditText)findViewById(R.id.pass);
        String thresh   = thresh_edit.getText().toString();
        String act      = act_edit.getText().toString();
        String pass     = pass_edit.getText().toString();
        if(!isEmpty(thresh) && !isEmpty(pass) && !isEmpty(act)) {
            float t = Float.parseFloat(thresh);
            int a = Integer.parseInt(act);
            int p = Integer.parseInt(pass);
            if (location.getSpeed() > convertToMPS(t)) {
                audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, a, 0);
            } else {
                audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0);
            }
            TextView text = (TextView) findViewById(R.id.speed);
            text.setText("Speed: " + Float.toString(convertToMPH(location.getSpeed())) + " mph");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}

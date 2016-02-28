package com.embedonix.chronometer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    /**
     * Key for getting saved start time of Chronometer class
     * this is used for onResume/onPause/etc.
     */
    public static final String START_TIME = "START_TIME";
    /**
     * Same story, but to tell whether the Chronometer was running or not
     */
    public static final String CHRONO_WAS_RUNNING = "CHRONO_WAS_RUNNING";
    /**
     * Same story, but if chronometer was stopped, we dont want to lose the stop time shows in
     * the tv_timer
     */
    public static final String TV_TIMER_TEXT = "TV_TIMER_TEXT";
    /**
     * Same story, we don't want to lose recorded laps
     */
    public static final String ET_LAPST_TEXT = "ET_LAPST_TEXT";
    /**
     * Same story...keeps the value of the lap counter
     */
    public static final String LAP_COUNTER  = "LAP_COUNTER";

    //Member variables to access UI Elements
    Button mBtnStart, mBtnLap, mBtnStop; //buttons
    TextView mTvTimer; //timer textview
    EditText mEtLaps; //laps text view
    ScrollView mSvLaps; //scroll view which wraps the et_laps

    //keep track of how many times btn_lap was clicked
    int mLapCounter = 1;

    //Instance of Chronometer
    Chronometer mChrono;

    //Thread for chronometer
    Thread mThreadChrono;

    //Reference to the MainActivity (this class!)
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiating all member variables

        mContext = this;

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnLap = (Button) findViewById(R.id.btn_lap);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mTvTimer = (TextView) findViewById(R.id.tv_timer);
        mEtLaps = (EditText) findViewById(R.id.et_laps);
        mEtLaps.setEnabled(false); //prevent the et_laps to be editable

        mSvLaps = (ScrollView) findViewById(R.id.sv_lap);


        //btn_start click handler
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the chronometer has not been instantiated before...
                if(mChrono == null) {
                    //instantiate the chronometer
                    mChrono = new Chronometer(mContext);
                    //run the chronometer on a separate thread
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();

                    //start the chronometer!
                    mChrono.start();

                    //clear the perilously populated et_laps
                    mEtLaps.setText(""); //empty string!

                    //reset the lap counter
                    mLapCounter = 1;
                }
            }
        });

        //btn_stop click handler
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the chronometer had been instantiated before...
                if(mChrono != null) {
                    //stop the chronometer
                    mChrono.stop();
                    //stop the thread
                    mThreadChrono.interrupt();
                    mThreadChrono = null;
                    //kill the chrono class
                    mChrono = null;
                }
            }
        });

        mBtnLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if chrono is not running we shouldn't capture the lap!
                if(mChrono == null) {
                    Toast.makeText(mContext
                            , R.string.warning_lap_button, Toast.LENGTH_SHORT).show();
                    return; //do nothing!
                }

                //we just simply copy the current text of tv_timer and append it to et_laps
                mEtLaps.append("LAP " + String.valueOf(mLapCounter++)
                        + "   " + mTvTimer.getText() + "\n");

                //scroll to the bottom of et_laps
                mSvLaps.post(new Runnable() {
                    @Override
                    public void run() {
                        mSvLaps.smoothScrollTo(0, mEtLaps.getBottom());
                    }
                });
            }
        });
    }

    /**
     * Update the text of tv_timer
     * @param timeAsText the text to update tv_timer with
     */
    public void updateTimerText(final String timeAsText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTimer.setText(timeAsText);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInstance();

        //stop background services and notifications
        ((ChronometerApplication)getApplication()).stopBackgroundServices();
        ((ChronometerApplication)getApplication()).cancelNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInstance();

        if(mChrono != null && mChrono.isRunning()) {
            //start background notification and timer
            ((ChronometerApplication)getApplication())
                    .startBackgroundServices(mChrono.getStartTime());
        }
    }

    @Override
    protected void onDestroy() {

        saveInstance();

        //When back button is pressed, app will be destoyed by OS. We do not want this to stop us
        //from showing the notification if the chronometer is running!
        if(mChrono == null || !mChrono.isRunning()) {
            //stop background services and notifications
            ((ChronometerApplication) getApplication()).stopBackgroundServices();
            ((ChronometerApplication) getApplication()).cancelNotification();
        }

        super.onDestroy();
    }

    /**
     * If the application goes to background or orientation change or any other possibility that
     * will pause the application, we save some instance values, to resume back from last state
     */
    private void saveInstance() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //TODO move tags to a static class
        if(mChrono != null && mChrono.isRunning()) {
            editor.putBoolean(CHRONO_WAS_RUNNING, mChrono.isRunning());
            editor.putLong(START_TIME, mChrono.getStartTime());
            editor.putInt(LAP_COUNTER, mLapCounter);
        } else {
            editor.putBoolean(CHRONO_WAS_RUNNING, false);
            editor.putLong(START_TIME, 0); //0 means chronometer was not active! a redundant check!
            editor.putInt(LAP_COUNTER, 1);
        }

        //We save the lap text in any case. only a new click on start button should clear this text!
        editor.putString(ET_LAPST_TEXT, mEtLaps.getText().toString());

        //Same story for timer text
        editor.putString(TV_TIMER_TEXT, mTvTimer.getText().toString());

        editor.commit();
    }

    /**
     * Load the shared preferences to resume last known state of the application
     */
    private void loadInstance() {

        SharedPreferences pref = getPreferences(MODE_PRIVATE);

        //if chronometer was running
        if(pref.getBoolean(CHRONO_WAS_RUNNING, false)) {
            //get the last start time from the bundle
            long lastStartTime = pref.getLong(START_TIME, 0);
            //if the last start time is not 0
            if(lastStartTime != 0) { //because 0 means value was not saved correctly!

                if(mChrono == null) { //make sure we dont create new instance and thread!

                    if(mThreadChrono != null) { //if thread exists...first interrupt and nullify it!
                        mThreadChrono.interrupt();
                        mThreadChrono = null;
                    }

                    //start chronometer with old saved time
                    mChrono = new Chronometer(mContext, lastStartTime);
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();
                    mChrono.start();
                }
            }
        }

        //we will load the lap text anyway in any case!
        //set the old value of lap counter
        mLapCounter = pref.getInt(LAP_COUNTER, 1);

        String oldEtLapsText = pref.getString(ET_LAPST_TEXT, "");
        if(!oldEtLapsText.isEmpty()) { //if old timer was saved correctly
            mEtLaps.setText(oldEtLapsText);
        }

        String oldTvTimerText = pref.getString(TV_TIMER_TEXT, "");
        if(!oldTvTimerText.isEmpty()){
            mTvTimer.setText(oldTvTimerText);
        }
    }
}

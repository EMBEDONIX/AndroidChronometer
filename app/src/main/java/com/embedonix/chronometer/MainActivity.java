package com.embedonix.chronometer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

        //if application was paused or killed or anything, we resume the last state!
        if(savedInstanceState != null) {

            //if chronometer was running
            if(savedInstanceState.getBoolean(CHRONO_WAS_RUNNING)) {
                //get the last start time from the bundle
                long lastStartTime = savedInstanceState.getLong(START_TIME, 0);
                //if the last start time is not 0
                if(lastStartTime != 0) {
                    mChrono = new Chronometer(mContext, lastStartTime);
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();
                    mChrono.start();

                    //set the old value of lap counter
                    mLapCounter = savedInstanceState.getInt(LAP_COUNTER, 1);
                }
            }  else { //if chrono was not running but it was stopped and tv_timer had a time!
                String oldStoppedTimerText = savedInstanceState.getString(TV_TIMER_TEXT);
                if(!oldStoppedTimerText.isEmpty()) { //if old timer was saved correctly
                    mTvTimer.setText(oldStoppedTimerText);
                }
            }
        }

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

    /**
     * If the application goes to background or orientation change or any other possibility that
     * will pause the application, we save some instance values, to resume back from last state
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        //if chronometer is running, we get its start time and save it in the bundle
        if(mChrono != null && mChrono.isRunning()) {
            outState.putBoolean(CHRONO_WAS_RUNNING, mChrono.isRunning());
            outState.putLong(START_TIME, mChrono.getStartTime());
            outState.putInt(LAP_COUNTER, mLapCounter);
        } else {
            outState.putBoolean(CHRONO_WAS_RUNNING, false);
            outState.putLong(START_TIME, 0);
            outState.putString(TV_TIMER_TEXT, mTvTimer.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }
}

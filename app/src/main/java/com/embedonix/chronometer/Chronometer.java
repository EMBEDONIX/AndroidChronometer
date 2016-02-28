package com.embedonix.chronometer;

import android.content.Context;

/**
 * A simple Runnable class to generate time difference since a starting time in milliseconds
 *
 *
 * Created by saeid on 24.01.16.
 */
public class Chronometer implements Runnable {

    //Some constants for milliseconds to hours, minutes, and seconds conversion
    public static final long MILLIS_TO_MINUTES = 60000;
    public static final long MILLS_TO_HOURS = 3600000;

    /**
     * Context which is responsible for this instance of the class
     */
    Context mContext;
    /**
     * Starting time
     */
    long mStartTime;
    /**
     * If the class is running or not
     */
    boolean mIsRunning;

    /**
     * Constructor for the class for normal usage
     * @param context the Activity which is responsible fot this insatnce of class
     */
    public Chronometer(Context context) {
        mContext = context;
    }

    /**
     * Constructor which takes context and also an already set starting time
     * this is used mainly for onResume if the application was stopped for any reason
     * @param context
     * @param startTime
     */
    public Chronometer(Context context, long startTime) {
        this(context);
        mStartTime = startTime;
    }

    /**
     * Starts the chronometer
     */
    public void start() {
        if(mStartTime == 0) { //if the start time was not set before! e.g. by second constructor
            mStartTime = System.currentTimeMillis();
        }
        mIsRunning = true;
    }

    /**
     * Stops the chronometer
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * Check if the chronometer is running or not
     * @return true if running, false if not running
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * Get the start time of the class
     * @return start time in milliseconds
     */
    public long getStartTime() {
        return mStartTime;
    }


    @Override
    public void run() {
        while(mIsRunning) {
            //We do not call ConvertTimeToString here because it will add some overhead
            //therefore we do the calculation without any function calls!

            //Here we calculate the difference of starting time and current time
            long since = System.currentTimeMillis() - mStartTime;

            //convert the resulted time difference into hours, minutes, seconds and milliseconds
            int seconds = (int) (since / 1000) % 60;
            int minutes = (int) ((since / (MILLIS_TO_MINUTES)) % 60);
            //int hours = (int) ((since / (MILLS_TO_HOURS)) % 24); //this resets to  0 after 24 hour!
            int hours = (int) ((since / (MILLS_TO_HOURS))); //this does not reset to 0!
            int millis = (int) since % 1000; //the last 3 digits of millisecs

            ((MainActivity) mContext).updateTimerText(String.format("%02d:%02d:%02d:%03d"
                    , hours, minutes, seconds, millis));

            //Sleep the thread for a short amount, to prevent high CPU usage!
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

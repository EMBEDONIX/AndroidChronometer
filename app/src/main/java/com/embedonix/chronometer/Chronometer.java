package com.embedonix.chronometer;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A simple Runnable class to generate time difference since a starting time in milliseconds
 *
 *
 * Created by saeid on 24.01.16.
 */
public class Chronometer implements Runnable {

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
     * Used to convert Date object to string
     */
    SimpleDateFormat mSdf;

    /**
     * Constructor for the class for normal usage
     * @param context the Activity which is responsible fot this insatnce of class
     */
    public Chronometer(Context context) {
        mContext = context;
        //instantiating the date formatter
        mSdf = new SimpleDateFormat("HH:mm:ss:SSS");
        //set time to UTC so we start from 0!
        mSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
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
            //Here we calculate the difference of starting time and current time
            long since = System.currentTimeMillis() - mStartTime;
            //convert date to string
            String sinceToString = mSdf.format(new Date(since));
            //call the method from the activity to update the text of the TextView for timer
            ((MainActivity)mContext).updateTimerText(sinceToString);

            //Sleep the thread for a short amount, to prevent high CPU usage!
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

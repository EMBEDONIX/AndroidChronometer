package com.embedonix.chronometer;

/**
 * Created by saeid on 27.02.16.
 *
 * Some constants and helper methods
 */
public class Helpers {

    /**
     * Converts a time in milliseconds to String in hh:mm:ss format
     * @param timeInMillis
     * @return
     */
    public static String ConvertTimeToString(long timeInMillis) {

        //convert the resulted time difference into hours, minutes, seconds and milliseconds
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (60000)) % 60);
        //int hours = (int) ((timeInMillis / (3600000)) % 24); //this resets to 0 after 24 hours
        int hours = (int) ((timeInMillis / (3600000))); //this does not reset :P
        //we dont need milisecs to be returned by this method
        //int millis = (int) timeInMillis % 1000; //the last 3 digits of millisecs

        return String.format("%02d:%02d:%02d"
                , hours, minutes, seconds);
    }
}

package org.urbancortex.presenter;

import android.app.Application;

import java.io.File;

public class Presenter
        extends Application
{
    public static Event[] experimentEvents;
    protected static int index = 0;
    protected static boolean isRecording = false;
    public static long startMillis;
    public static long startTime;
    static File fileDirectory = null;
    static File fileWriteDirectory = null;
    static File imgDirectory;

    public Presenter() {}
}

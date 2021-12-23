
package com.myapp.app101.gui;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;



abstract class Timer
        extends Handler {




    private long mTickInterval;


    private boolean mIsRunning;
    private int mTickCount;

    private long mNextTime;

    private long mAccumTime;
    private long mLastLogTime;


    private final Runnable runner = new Runnable() {

        public final void run() {
            if (mIsRunning) {
                long now = SystemClock.uptimeMillis();

                mAccumTime += now - mLastLogTime;
                mLastLogTime = now;

                if (!step(mTickCount++, mAccumTime)) {

                    mNextTime += mTickInterval;
                    if (mNextTime <= now)
                        mNextTime += mTickInterval;
                    postAtTime(runner, mNextTime);
                } else {
                    mIsRunning = false;
                    done();
                }
            }
        }

    };



    public Timer(long ival) {
        mTickInterval = ival;
        mIsRunning = false;
        mAccumTime = 0;
    }



    public void start() {
        if (mIsRunning)
            return;

        mIsRunning = true;

        long now = SystemClock.uptimeMillis();

        mLastLogTime = now;

        // Schedule the first event at once.
        mNextTime = now;
        postAtTime(runner, mNextTime);
    }



    public void stop() {
        if (mIsRunning) {
            mIsRunning = false;
            long now = SystemClock.uptimeMillis();
            mAccumTime += now - mLastLogTime;
            mLastLogTime = now;
        }
    }


    public final void reset() {
        stop();
        mTickCount = 0;
        mAccumTime = 0;
    }


    public final boolean isRunning() {
        return mIsRunning;
    }



    public final long getTime() {
        return mAccumTime;
    }


    protected abstract boolean step(int count, long time);


    protected void done() {
    }


    void saveState(Bundle outState) {
        if (mIsRunning) {
            long now = SystemClock.uptimeMillis();
            mAccumTime += now - mLastLogTime;
            mLastLogTime = now;
        }

        outState.putLong("tickInterval", mTickInterval);
        outState.putBoolean("isRunning", mIsRunning);
        outState.putInt("tickCount", mTickCount);
        outState.putLong("accumTime", mAccumTime);
    }


    boolean restoreState(Bundle map) {
        return restoreState(map, true);
    }


    boolean restoreState(Bundle map, boolean run) {
        mTickInterval = map.getLong("tickInterval");
        mIsRunning = map.getBoolean("isRunning");
        mTickCount = map.getInt("tickCount");
        mAccumTime = map.getLong("accumTime");
        mLastLogTime = SystemClock.uptimeMillis();


        if (mIsRunning) {
            if (run)
                start();
            else
                mIsRunning = false;
        }

        return true;
    }
}

package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.freedesktop.gstreamer.GStreamer;

public class AudioStreamer {

    private static final String TAG = "AudioStreamer";

    private static final String STATE_PAUSED = "STATE_PAUSED";

    private static final String STATE_READY = "STATE_READY";

    private static final String STATE_PLAYING = "STATE_PLAYING";

    private static final String STATE_NULL = "NULL";

    private native void nativeInit();     // Initialize native code, build pipeline, etc

    private native void nativeFinalize(); // Destroy pipeline and shutdown native code

    private native void nativePlay();     // Set pipeline to STATE_PLAYING

    private native void nativePause();    // Set pipeline to STATE_PAUSED

    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks

    private native void nativeInitPipeline(String pipeline);

    private native void nativeEnableSpeakers();

    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to STATE_PLAYING

    private boolean is_playing = false;

    private String mName;

    private String currentPipeline;

    private int playPipelineTries;

    private OnErrorListener mListener;

    public AudioStreamer(Context context, String name, OnErrorListener listener) {
        this.mName = name;
        this.mListener = listener;
        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(context);
        } catch (Exception e) {
            return;
        }
        is_playing_desired = false;
        nativeInit();
    }

    protected void destroy() {
        nativeFinalize();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void onError(final String message) {
        Log.w(TAG, mName + "error" + ": " + message);
        mListener.onError(mName, message);
    }

    private void onStateChanged(final String newState) {
        Log.wtf(TAG, mName + " new state:" + newState);
        if (newState.equals(STATE_PAUSED) && is_playing_desired) {
            if (playPipelineTries >= 4) {
                mListener.onError(mName, "Number of playing attempts exceeded.");
            } else {
                Log.wtf(TAG, "onPause else!!!");

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!is_playing) {
                            nativePlay();
                            playPipelineTries++;
                            Log.wtf(TAG, "nativePlay!!!");
                        }
                    }
                }, 500);
            }
        } else if (newState.equals(STATE_PLAYING)) {
            is_playing = true;
        } else if (newState.equals(STATE_NULL)) {
            is_playing = false;
        }
    }

    private void onStateChanged() {
        Log.wtf(TAG, "onStateChanged");
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized() {
        Log.i("GStreamer " + mName, "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }
    }

    public void onPause() {
        Log.wtf(TAG, "onPause()");
        if (is_playing_desired) {

        }
    }

    public void onStart() {
        Log.wtf(TAG, "onStart");
        this.playPipelineTries = 0;
    }

    public void startVOIPStreaming(String pipeline) {
        is_playing_desired = true;
        this.playPipelineTries = 0;
        is_playing = false;
        this.currentPipeline = pipeline;
        nativeInitPipeline(pipeline);
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("audiostreamer");
        nativeClassInit();
    }

    public void stop() {
        is_playing_desired = false;
        nativePause();
    }

    public void finalize() {
        nativeFinalize();
    }

    public void resume() {
        if (this.currentPipeline != null) {
            is_playing_desired = true;
            nativeInitPipeline(this.currentPipeline);
        }
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    protected interface OnErrorListener {
        void onError(String streamerName, String message);
    }
}
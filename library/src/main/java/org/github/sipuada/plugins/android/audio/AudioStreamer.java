package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.util.Log;

import org.freedesktop.gstreamer.GStreamer;

public class AudioStreamer {

    private static final String TAG = "AudioStreamer";

    private native void nativeInit();     // Initialize native code, build pipeline, etc

    private native void nativeFinalize(); // Destroy pipeline and shutdown native code

    private native void nativePlay();     // Set pipeline to PLAYING

    private native void nativePause();    // Set pipeline to PAUSED

    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks

    private native void nativeInitPipeline(String pipeline);

    private native void nativeEnableSpeakers();

    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    private String mName;

    private String currentPipeline;

    public AudioStreamer(Context context, String name) {
        this.mName = name;
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
    private void setMessage(final String message) {
        Log.w(TAG, mName + " :" + message);
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized() {
        Log.i("GStreamer "+mName, "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }
    }

    public void startVOIPStreaming( String pipeline) {
        is_playing_desired = true;
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

    public void finalize(){
        nativeFinalize();
    }

    public void resume() {
        if( this.currentPipeline != null){
            is_playing_desired = true;
            nativeInitPipeline(this.currentPipeline);
        }
    }
}
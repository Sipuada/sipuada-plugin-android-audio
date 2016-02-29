package org.github.sipuada.plugins.android.audio;

import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Handler;
import android.util.Log;
import org.github.sipuada.plugins.android.audio.utils.SipuadaLog;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SipuadaAudioManager {
    private Context mContext;
    private AudioManager mAudioManager;
    private AudioStream mAudioStream;
    private AudioGroup mAudioGroup;
    private InetAddress mLocalAddress;

    private static final String TAG = "SipuadaAudioManager";

    public SipuadaAudioManager(Context context, String localAddress) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            mLocalAddress = InetAddress.getByName(localAddress);
            mAudioGroup = new AudioGroup();
            mAudioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
        } catch (UnknownHostException e) {
            SipuadaLog.error("Failed to create InetAddress", e);
        }
    }

    public void setupAudioStream() {
        try {
            mAudioStream = new AudioStream(mLocalAddress);
            mAudioStream.setCodec(AudioCodec.PCMU);
            mAudioStream.setMode(RtpStream.MODE_NORMAL);
        } catch (SocketException e) {
            SipuadaLog.error("Failed to create AudioStream", e);
        }
    }

    public AudioCodec[] getCodecs() {
        AudioCodec codecs[] = AudioCodec.getCodecs();
        AudioCodec[] availableCodecs = new AudioCodec[1];

        for (AudioCodec codec : codecs) {
//            int codecConstant = 0;
//            for (int j = 0; j < SdpConstants.avpTypeNames.length; j++) {
//                if (codec.rtpmap.split("/")[0].equals(SdpConstants.avpTypeNames[j])) {
//                    codecConstant = j;
//                }
//            }
            // Adds only PCMA
            if (codec.rtpmap.split("/")[0].equals("PCMU")) {
                availableCodecs[0] = codec;
            }
        }
        SipuadaLog.verbose("codecs size:" + availableCodecs.length);
        return availableCodecs;
    }

    public int getAudioStreamPort() {
        return mAudioStream.getLocalPort();
    }

    // Start sending/receiving media
    public void startStreaming(int remoteRtpPort, String remoteIp, AudioCodec codec) {
        Log.i(TAG, "Starting streaming: " + remoteIp + "/" + remoteRtpPort);

        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);

        // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.i(TAG, "onAudioFocusChange: " + focusChange);
            }
        }, AudioManager.MODE_IN_CALL, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //////// DEBUG
            if (mAudioManager.isBluetoothA2dpOn()) {
                // Adjust output for Bluetooth.
                Log.i(TAG, "Using Bluetooth");
            } else if (mAudioManager.isSpeakerphoneOn()) {
                // Adjust output for Speakerphone.
                Log.i(TAG, "Using Speaker");
            } else if (mAudioManager.isMicrophoneMute()) {
                // Adjust output for headsets
                Log.i(TAG, "Using Microphone is mute");
            } else {
                // If audio plays and noone can hear it, is it still playing?
                Log.i(TAG, "Using None ??");
//                audio.setSpeakerphoneOn(true);
//                audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);
            }
            //audio.setSpeakerphoneOn(false);

            Log.i(TAG, "Vol/max: " + mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) + "/"
                    + mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));

            try {
                mAudioStream.associate(InetAddress.getByName(remoteIp), remoteRtpPort);
            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.out.println("IllegalArgumentException: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalStateException e) {
                System.out.println("IllegalStateException: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                mAudioStream.join(mAudioGroup);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Cannot receive audio focus; media stream not setup");
        }
    }

    public void stopStreaming() {
        // workaround: android RTP facilities seem to induce around 500ms delay in the incoming media stream.
        // Let's delay the media tear-down to avoid media truncation for now
        final SipuadaAudioManager finalSipSoundManager = this;
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Trying stop audio streaming: ");
                if (mAudioStream != null) {
                    Log.i(TAG, "Releasing Audio: ");
                    try {
                        mAudioStream.join(null);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    mAudioGroup.clear();
                    if (mAudioStream.isBusy()) {
                        Log.i(TAG, "AudioStream is busy");
                    }
                    //audioStream.release();
                    mAudioStream = null;
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);

                    // Abandon audio focus when playback complete
                    mAudioManager.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            Log.i(TAG, "onAudioFocusChange: " + focusChange);
                        }
                    });
                }
            }
        };
        mainHandler.postDelayed(myRunnable, 500);
    }

}

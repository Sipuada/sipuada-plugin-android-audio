package org.github.sipuada.plugins.android.audio;

import java.util.Arrays;

public class SipuadaAudioCodec {
    /**
     * The RTP payload type of the encoding.
     */
    public final int type;

    /**
     * The encoding parameters to be used in the corresponding SDP attribute.
     */
    public final String rtpmap;

    /**
     * The format parameters to be used in the corresponding SDP attribute.
     */
    public final String fmtp;

    public static final SipuadaAudioCodec PCMA = new SipuadaAudioCodec(8, "PCMA/8000", null);

    public static final SipuadaAudioCodec SPEEX_8000 = new SipuadaAudioCodec(97, "SPEEX/8000", null);

    public static final SipuadaAudioCodec SPEEX_16000 = new SipuadaAudioCodec(97, "SPEEX/16000", null);

    public static final SipuadaAudioCodec SPEEX_32000 = new SipuadaAudioCodec(97, "SPEEX/32000", null);

    private static final SipuadaAudioCodec[] sCodecs = {PCMA, SPEEX_8000, SPEEX_16000, SPEEX_32000};

    public SipuadaAudioCodec(int type, String rtpmap, String fmtp) {
        this.type = type;
        this.rtpmap = rtpmap;
        this.fmtp = fmtp;
    }

    public static SipuadaAudioCodec[] getCodecs() {
        return Arrays.copyOf(sCodecs, sCodecs.length);
    }
}

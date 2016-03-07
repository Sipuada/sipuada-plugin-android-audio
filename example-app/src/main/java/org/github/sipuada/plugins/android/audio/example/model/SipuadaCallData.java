package org.github.sipuada.plugins.android.audio.example.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SipuadaCallData implements Parcelable {

    private String callId;
    private final String username;
    private final String primaryHost;
    private final String remoteUsername;
    private final String remoteHost;

    public SipuadaCallData(String callId, String username, String primaryHost,
                           String remoteUsername, String remoteHost) {
        this.callId = callId;
        this.username = username;
        this.primaryHost = primaryHost;
        this.remoteUsername = remoteUsername;
        this.remoteHost = remoteHost;
    }

    protected SipuadaCallData(Parcel in) {
        callId = in.readString();
        username = in.readString();
        primaryHost = in.readString();
        remoteUsername = in.readString();
        remoteHost = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(callId);
        dest.writeString(username);
        dest.writeString(primaryHost);
        dest.writeString(remoteUsername);
        dest.writeString(remoteHost);
    }

    public static final Creator<SipuadaCallData> CREATOR = new Creator<SipuadaCallData>() {
        @Override
        public SipuadaCallData createFromParcel(Parcel in) {
            return new SipuadaCallData(in);
        }

        @Override
        public SipuadaCallData[] newArray(int size) {
            return new SipuadaCallData[size];
        }
    };

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getUsername() {
        return username;
    }

    public String getPrimaryHost() {
        return primaryHost;
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

}

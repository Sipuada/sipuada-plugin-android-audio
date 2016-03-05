package org.github.sipuada.plugins.android.audio.example.view;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.viewstate.RestoreableViewState;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CallViewState implements RestoreableViewState<SipuadaViewApi> {

    private ArrayList<SipuadaCall> callsInformation = new ArrayList<>();

    public enum SipuadaCallState {

        CALL_MAKING, CALL_MAKING_ACCEPTED, CALL_MAKING_DECLINED, CALL_RECEIVING,
        CALL_RECEIVING_ACCEPT, CALL_RECEIVING_DECLINE, CALL_IN_PROGRESS, CALL_FINISHED

    }

    public static class SipuadaCall implements Parcelable {

        private SipuadaCallState callState;
        private SipuadaCallData callData;

        protected SipuadaCall(Parcel in) {
            callState = (SipuadaCallState) in.readSerializable();
            callData = in.readParcelable(SipuadaCallData.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(callState);
            dest.writeParcelable(callData, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SipuadaCall> CREATOR = new Creator<SipuadaCall>() {
            @Override
            public SipuadaCall createFromParcel(Parcel in) {
                return new SipuadaCall(in);
            }

            @Override
            public SipuadaCall[] newArray(int size) {
                return new SipuadaCall[size];
            }
        };

        public SipuadaCallState getCallState() {
            return callState;
        }

        public void setCallState(SipuadaCallState state) {
            callState = state;
        }

        public SipuadaCallData getCallData() {
            return callData;
        }

        public void setCallData(SipuadaCallData data) {
            callData = data;
        }

    }

    @Override
    public void saveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SipuadaApplication.KEY_CALLS_INFO,
                callsInformation);
    }

    @Override
    public RestoreableViewState<SipuadaViewApi> restoreInstanceState(Bundle savedInstanceState) {
        callsInformation = savedInstanceState
                .getParcelableArrayList(SipuadaApplication.KEY_CALLS_INFO);
        return this;
    }

    @Override
    public void apply(SipuadaViewApi sipuadaCallView, boolean retained) {
//        switch (callState) {
//            case CALL_MAKING:
//            default:
//                sipuadaCallView.showMakingCall(callData);
//                break;
//            case CALL_MAKING_ACCEPTED:
//                sipuadaCallView.showMakingCallAccepted(callData);
//                break;
//            case CALL_MAKING_DECLINED:
//                sipuadaCallView.showMakingCallDeclined(callData);
//                break;
//            case CALL_RECEIVING:
//                sipuadaCallView.showReceivingCall(callData);
//                break;
//            case CALL_RECEIVING_ACCEPT:
//                sipuadaCallView.showReceivingCallAccept(callData);
//                break;
//            case CALL_RECEIVING_DECLINE:
//                sipuadaCallView.showReceivingCallDecline(callData);
//                break;
//            case CALL_IN_PROGRESS:
//                sipuadaCallView.showCallInProgress(callData);
//                break;
//            case CALL_FINISHED:
//                sipuadaCallView.showCallFinished(callData);
//                break;
//        }
    }

}

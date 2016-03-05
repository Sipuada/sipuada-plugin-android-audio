package org.github.sipuada.plugins.android.audio.example.view;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.viewstate.RestoreableViewState;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CallViewState implements RestoreableViewState<CallViewApi> {

    private ArrayList<SipuadaCall> callsInformation = new ArrayList<>();

    public enum SipuadaCallState {

        CALL_MAKING, CALL_MAKING_ACCEPTED, CALL_MAKING_DECLINED, CALL_RECEIVING,
        CALL_RECEIVING_ACCEPT, CALL_RECEIVING_DECLINE, CALL_IN_PROGRESS, CALL_FINISHED

    }

    public static class SipuadaCall implements Parcelable {

        private SipuadaCallState callState;
        private SipuadaCallData callData;

        public SipuadaCall(SipuadaCallState state, SipuadaCallData data) {
            callState = state;
            callData = data;
        }

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

    }

    @Override
    public void saveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SipuadaApplication.KEY_CALLS_INFO,
                callsInformation);
    }

    @Override
    public RestoreableViewState<CallViewApi> restoreInstanceState(Bundle savedInstanceState) {
        callsInformation = savedInstanceState
                .getParcelableArrayList(SipuadaApplication.KEY_CALLS_INFO);
        return this;
    }

    @Override
    public void apply(CallViewApi sipuadaCallView, boolean retained) {
        boolean notifyInsteadOfShow = false;
        for (SipuadaCall sipuadaCall : callsInformation) {
            SipuadaCallData sipuadaCallData = sipuadaCall.getCallData();
            switch (sipuadaCall.getCallState()) {
                case CALL_IN_PROGRESS:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyCallInProgress(sipuadaCallData);
                    } else {
                        notifyInsteadOfShow = true;
                        sipuadaCallView.showCallInProgress(sipuadaCallData);
                    }
                    break;
                case CALL_RECEIVING_ACCEPT:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyReceivingCallAccept(sipuadaCallData);
                    } else {
                        notifyInsteadOfShow = true;
                        sipuadaCallView.showReceivingCallAccept(sipuadaCallData);
                    }
                    break;
                case CALL_MAKING_ACCEPTED:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyMakingCallAccepted(sipuadaCallData);
                    } else {
                        notifyInsteadOfShow = true;
                        sipuadaCallView.showMakingCallAccepted(sipuadaCallData);
                    }
                    break;
                case CALL_RECEIVING_DECLINE:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyReceivingCallDecline(sipuadaCallData);
                    } else {
                        notifyInsteadOfShow = true;
                        sipuadaCallView.showReceivingCallDecline(sipuadaCallData);
                    }
                    break;
                case CALL_MAKING_DECLINED:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyMakingCallDeclined(sipuadaCallData);
                    } else {
                        notifyInsteadOfShow = true;
                        sipuadaCallView.showMakingCallDeclined(sipuadaCallData);
                    }
                    break;
                case CALL_RECEIVING:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyReceivingCall(sipuadaCallData);
                    } else {
                        sipuadaCallView.showReceivingCall(sipuadaCallData);
                    }
                    break;
                case CALL_MAKING:
                    if (notifyInsteadOfShow) {
                        //sipuadaCallView.notifyMakingCall(sipuadaCallData);
                    } else {
                        sipuadaCallView.showMakingCall(sipuadaCallData);
                    }
                    break;
                case CALL_FINISHED:
                    if (!notifyInsteadOfShow) {
                        sipuadaCallView.showCallFinished(sipuadaCallData);
                    }
                    break;
            }
        }
    }

    public void addSipuadaCall(SipuadaCallState sipuadaCallState, SipuadaCallData sipuadaCallData) {
        callsInformation.add(new SipuadaCall(sipuadaCallState, sipuadaCallData));
        final SipuadaCallState[] statesPriority = new SipuadaCallState[]{
                SipuadaCallState.CALL_IN_PROGRESS,
                SipuadaCallState.CALL_RECEIVING_ACCEPT,
                SipuadaCallState.CALL_MAKING_ACCEPTED,
                SipuadaCallState.CALL_RECEIVING_DECLINE,
                SipuadaCallState.CALL_MAKING_DECLINED,
                SipuadaCallState.CALL_RECEIVING,
                SipuadaCallState.CALL_MAKING,
                SipuadaCallState.CALL_FINISHED
        };
        Collections.sort(callsInformation, new Comparator<SipuadaCall>() {

            @Override
            public int compare(SipuadaCall call, SipuadaCall anotherCall) {
                if (call.getCallState() == anotherCall.getCallState()) {
                    return 0;
                }
                for (SipuadaCallState stateWithPriority : statesPriority) {
                    if (call.getCallState() == stateWithPriority) {
                        return -1;
                    } else if (anotherCall.getCallState() == stateWithPriority) {
                        return 1;
                    }
                }
                return 0;
            }

        });
    }

}

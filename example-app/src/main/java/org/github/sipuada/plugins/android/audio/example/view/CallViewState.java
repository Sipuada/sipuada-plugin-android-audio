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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CallViewState implements RestoreableViewState<CallViewApi> {

    private ArrayList<SipuadaCall> callsInformation = new ArrayList<>();
    private Lock lock = new ReentrantLock();

    public enum SipuadaCallState {

        CALL_MAKING, CALL_MAKING_CANCELABLE, CALL_MAKING_RINGING, CALL_MAKING_CANCEL,
        CALL_MAKING_CANCELED, CALL_MAKING_FAILED, CALL_MAKING_DECLINED,
        CALL_RECEIVING, CALL_RECEIVING_CANCELED, CALL_RECEIVING_FAILED,
        CALL_RECEIVING_ACCEPT, CALL_RECEIVING_DECLINE,
        CALL_IN_PROGRESS, CALL_FAILED, CALL_FINISHED

    }

    public static class SipuadaCall implements Parcelable {

        private SipuadaCallState callState;
        private SipuadaCallData callData;
        private String stateInformation;

        public SipuadaCall(SipuadaCallState state, SipuadaCallData data, String information) {
            callState = state;
            callData = data;
            stateInformation = information;
        }

        protected SipuadaCall(Parcel in) {
            callState = (SipuadaCallState) in.readSerializable();
            callData = in.readParcelable(SipuadaCallData.class.getClassLoader());
            stateInformation = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(callState);
            dest.writeParcelable(callData, flags);
            dest.writeString(stateInformation);
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

        public SipuadaCallData getCallData() {
            return callData;
        }

        public String getStateInformation() {
            return stateInformation;
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
        sipuadaCallView.updatePresenter(this);
        lock.lock();
        try {
//        boolean notifyInsteadOfShow = false;
            for (SipuadaCall sipuadaCall : callsInformation) {
                SipuadaCallData sipuadaCallData = sipuadaCall.getCallData();
                String information = sipuadaCall.getStateInformation();
                switch (sipuadaCall.getCallState()) {
                    case CALL_IN_PROGRESS:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyCallInProgress(sipuadaCallData);
//                    } else {
//                        notifyInsteadOfShow = true;
                        sipuadaCallView.showCallInProgress(sipuadaCallData);
//                    }
                        break;
                    case CALL_RECEIVING_ACCEPT:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyReceivingCallAccept(sipuadaCallData);
//                    } else {
//                        notifyInsteadOfShow = true;
                        sipuadaCallView.showReceivingCallAccept(sipuadaCallData);
//                    }
                        break;
                    case CALL_RECEIVING_DECLINE:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyReceivingCallDecline(sipuadaCallData);
//                    } else {
//                        notifyInsteadOfShow = true;
                        sipuadaCallView.showReceivingCallDecline(sipuadaCallData);
//                    }
                        break;
                    case CALL_MAKING_RINGING:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCallRinging(sipuadaCallData);
//                    } else {
//                        notifyInsteadOfShow = true;
                        sipuadaCallView.showMakingCallRinging(sipuadaCallData);
//                    }
                        break;
                    case CALL_MAKING_DECLINED:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCallDeclined(sipuadaCallData);
//                    } else {
//                        notifyInsteadOfShow = true;
                        sipuadaCallView.showMakingCallDeclined(sipuadaCallData);
//                    }
                        break;
                    case CALL_RECEIVING_CANCELED:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyReceivingCallCanceled(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showReceivingCallCanceled(sipuadaCallData, information);
//                    }
                        break;
                    case CALL_RECEIVING_FAILED:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyReceivingCallFailed(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showReceivingCallFailed(sipuadaCallData, information);
//                    }
                        break;
                    case CALL_MAKING_CANCEL:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyCancelingCall(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showCancelingCall(sipuadaCallData);
//                    }
                        break;
                    case CALL_MAKING_CANCELED:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCallCanceled(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showMakingCallCanceled(sipuadaCallData, information);
//                    }
                        break;
                    case CALL_MAKING_FAILED:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCallFailed(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showMakingCallFailed(sipuadaCallData, information);
//                    }
                        break;
                    case CALL_RECEIVING:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyReceivingCall(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showReceivingCall(sipuadaCallData);
//                    }
                        break;
                    case CALL_MAKING_CANCELABLE:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCallCancelable(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showMakingCallCancelable(sipuadaCallData);
//                    }
                        break;
                    case CALL_MAKING:
//                    if (notifyInsteadOfShow) {
//                        //sipuadaCallView.notifyMakingCall(sipuadaCallData);
//                    } else {
                        sipuadaCallView.showMakingCall(sipuadaCallData);
//                    }
                        break;
                    case CALL_FAILED:
                        sipuadaCallView.showCallFailed(sipuadaCallData, information);
                        break;
                    case CALL_FINISHED:
//                    if (!notifyInsteadOfShow) {
                        sipuadaCallView.showCallFinished(sipuadaCallData);
//                    }
                        break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void addOrModifySipuadaCall(SipuadaCallState sipuadaCallState,
                                       SipuadaCallData sipuadaCallData, String stateInformation) {
        lock.lock();
        try {
            Iterator<SipuadaCall> iterator = callsInformation.iterator();
            while (iterator.hasNext()) {
                SipuadaCall sipuadaCall = iterator.next();
                String sipuadaCallId = sipuadaCall.getCallData().getCallId();
                if (sipuadaCallId == null) {
                    if (sipuadaCallData.getUsername().equals(sipuadaCall.getCallData().getUsername()) &&
                            sipuadaCallData.getPrimaryHost().equals(sipuadaCall.getCallData().getPrimaryHost()) &&
                            sipuadaCallData.getRemoteUsername().equals(sipuadaCall.getCallData().getRemoteUsername()) &&
                            sipuadaCallData.getRemoteHost().equals(sipuadaCall.getCallData().getRemoteHost())) {
                        iterator.remove();
                    }
                } else if (sipuadaCallId.equals(sipuadaCallData.getCallId())) {
                    iterator.remove();
                }
            }
            callsInformation.add(new SipuadaCall(sipuadaCallState, sipuadaCallData, stateInformation));
            final SipuadaCallState[] statesPriority = new SipuadaCallState[]{
                    SipuadaCallState.CALL_IN_PROGRESS,
                    SipuadaCallState.CALL_RECEIVING,
                    SipuadaCallState.CALL_MAKING_CANCELABLE,
                    SipuadaCallState.CALL_MAKING,
                    SipuadaCallState.CALL_MAKING_RINGING,
                    SipuadaCallState.CALL_MAKING_DECLINED,
                    SipuadaCallState.CALL_RECEIVING_ACCEPT,
                    SipuadaCallState.CALL_RECEIVING_DECLINE,
                    SipuadaCallState.CALL_RECEIVING_CANCELED,
                    SipuadaCallState.CALL_RECEIVING_FAILED,
                    SipuadaCallState.CALL_MAKING_CANCEL,
                    SipuadaCallState.CALL_MAKING_CANCELED,
                    SipuadaCallState.CALL_MAKING_FAILED,
                    SipuadaCallState.CALL_FAILED,
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
        } finally {
            lock.unlock();
        }
    }

    public void removeSipuadaCall(SipuadaCallData sipuadaCallData) {
        lock.lock();
        try {
            Iterator<SipuadaCall> iterator = callsInformation.iterator();
            while (iterator.hasNext()) {
                SipuadaCall sipuadaCall = iterator.next();
                if (sipuadaCall.getCallData().getCallId().equals(sipuadaCallData.getCallId())) {
                    iterator.remove();
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public List<SipuadaCall> getSipuadaCallsInformation() {
        lock.lock();
        List<SipuadaCall> currentCallsInformation;
        try {
            currentCallsInformation = new ArrayList<>(callsInformation);
        } finally {
            lock.unlock();
        }
        return currentCallsInformation;
    }

}

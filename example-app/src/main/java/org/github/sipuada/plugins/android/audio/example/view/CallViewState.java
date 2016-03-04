package org.github.sipuada.plugins.android.audio.example.view;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.viewstate.RestoreableViewState;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;

public class CallViewState implements RestoreableViewState<SipuadaViewApi> {

    public enum SipuadaCallState {

        CALL_MAKING, CALL_MAKING_ACCEPTED, CALL_MAKING_DECLINED, CALL_RECEIVING,
        CALL_RECEIVING_ACCEPT, CALL_RECEIVING_DECLINE, CALL_IN_PROGRESS, CALL_FINISHED

    }
    private SipuadaCallState callState;
    private SipuadaCallData callData;

    @Override
    public void saveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putSerializable(SipuadaApplication.KEY_CALL_STATE, callState);
        savedInstanceState.putParcelable(SipuadaApplication.KEY_CALL_DATA, callData);
    }

    @Override
    public RestoreableViewState<SipuadaViewApi> restoreInstanceState(Bundle bundle) {
        callState = (SipuadaCallState) bundle.getSerializable(SipuadaApplication.KEY_CALL_STATE);
        callData = bundle.getParcelable(SipuadaApplication.KEY_CALL_DATA);
        return this;
    }

    @Override
    public void apply(SipuadaViewApi sipuadaCallView, boolean retained) {
        switch (callState) {
            case CALL_MAKING:
            default:
//                sipuadaCallView.showMakingCall(callData);
                break;
            case CALL_MAKING_ACCEPTED:
//                sipuadaCallView.showMakingCallAccepted(callData);
                break;
            case CALL_MAKING_DECLINED:
//                sipuadaCallView.showMakingCallDeclined(callData);
                break;
            case CALL_RECEIVING:
//                sipuadaCallView.showReceivingCall(callData);
                break;
            case CALL_RECEIVING_ACCEPT:
//                sipuadaCallView.showReceivingCallAccept(callData);
                break;
            case CALL_RECEIVING_DECLINE:
//                sipuadaCallView.showReceivingCallDecline(callData);
                break;
            case CALL_IN_PROGRESS:
//                sipuadaCallView.showCallInProgress(callData);
                break;
            case CALL_FINISHED:
//                sipuadaCallView.showCallFinished(callData);
                break;
        }
    }

    public void setCallState(SipuadaCallState state) {
        callState = state;
    }

    public void setCallData(SipuadaCallData data) {
        callData = data;
    }

}

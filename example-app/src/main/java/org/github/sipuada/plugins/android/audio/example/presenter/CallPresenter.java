package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.view.CallViewApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CallPresenter extends SipuadaPresenter<CallViewApi> implements CallPresenterApi {

    public enum CallAction {
        MAKE_CALL, RECEIVE_CALL, FINISH_CALL
    }

    @Override
    protected void doUponServiceConnected() {}

    @Override
    protected void doUponServiceDisconnected() {}

    @Override
    public void performAction(CallAction callAction, SipuadaCallData sipuadaCallData) {
        switch (callAction) {
            case MAKE_CALL:
                makeCall(sipuadaCallData);
                break;
            case RECEIVE_CALL:
                receiveCall(sipuadaCallData);
                break;
            case FINISH_CALL:
                finishCall(sipuadaCallData);
                break;
            default:
                break;
        }
    }

    @Override
    public void makeCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCall(sipuadaCallData);
        }
    }

    @Override
    public void cancelCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showCancelingCall(sipuadaCallData);
        }
    }

    @Override
    public void failCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showFailingCall(sipuadaCallData);
        }
    }

    @Override
    public void callAccepted(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCallAccepted(sipuadaCallData);
        }
    }

    @Override
    public void callDeclined(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCallDeclined(sipuadaCallData);
        }
    }

    Map<String, SipuadaCallData> incomingCalls = Collections
            .synchronizedMap(new HashMap<String, SipuadaCallData>());
    Map<String, SipuadaCallData> establishedCalls = Collections
            .synchronizedMap(new HashMap<String, SipuadaCallData>());

    @Override
    public void receiveCall(SipuadaCallData sipuadaCallData) {
        incomingCalls.put(sipuadaCallData.getCallId(), sipuadaCallData);
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCall(sipuadaCallData);
        }
    }

    @Override
    public void callCanceled(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallCanceled(sipuadaCallData);
        }
    }

    @Override
    public void callFailed(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallFailed(sipuadaCallData);
        }
    }

    @Override
    public void acceptCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallAccept(sipuadaCallData);
        }
        mSipuadaService.acceptInviteFromUser(sipuadaCallData.getUsername(),
                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
    }

    @Override
    public void declineCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallDecline(sipuadaCallData);
        }
        incomingCalls.remove(sipuadaCallData.getCallId());
        mSipuadaService.declineInviteFromUser(sipuadaCallData.getUsername(),
                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
    }

    @Override
    public void establishCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showCallInProgress(sipuadaCallData);
        }
        establishedCalls.put(sipuadaCallData.getCallId(), sipuadaCallData);
    }

    @Override
    public void finishCall(SipuadaCallData sipuadaCallData) {
        finishCall(sipuadaCallData, true);
    }

    private void finishCall(SipuadaCallData sipuadaCallData, boolean doFinishCall) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showCallFinished(sipuadaCallData);
        }
//        if (doFinishCall) {
//        mSipuadaService.finishCall(sipuadaCallData.getUsername(),
//                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
//        }
    }

    @Override
    @Subscribe
    public void onCallInvitationCanceled(final CallInvitationCanceled event) {
        SipuadaCallData sipuadaCallData = incomingCalls.remove(event.getCallId());
        if (sipuadaCallData != null) {
            callCanceled(sipuadaCallData);
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationFailed(final CallInvitationFailed event) {
        SipuadaCallData sipuadaCallData = incomingCalls.remove(event.getCallId());
        if (sipuadaCallData != null) {
            callFailed(sipuadaCallData);
        }
    }

    @Override
    @Subscribe
    public void onCallEstablished(final EstablishedCallStarted event) {
        SipuadaCallData sipuadaCallData = incomingCalls.remove(event.getCallId());
        if (sipuadaCallData != null) {
            establishCall(sipuadaCallData);
        }
    }

    @Override
    @Subscribe
    public void onCallFinished(EstablishedCallFinished event) {
        SipuadaCallData sipuadaCallData = establishedCalls.remove(event.getCallId());
        if (sipuadaCallData != null) {
            finishCall(sipuadaCallData, false);
        }
    }

    @Override
    @Subscribe
    public void onCallFailure(EstablishedCallFailed event) {

    }

}

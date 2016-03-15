package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.view.CallViewApi;

public interface CallPresenterApi extends SipuadaPresenterApi<CallViewApi> {

    void performAction(CallPresenter.CallAction callAction, SipuadaCallData sipuadaCallData);

    void makeCall(SipuadaCallData sipuadaCallData);

    void callCancelable(SipuadaCallData sipuadaCallData);

    void cancelCall(SipuadaCallData sipuadaCallData);

    void outgoingCallCanceled(SipuadaCallData sipuadaCallData, String reason);

    void outgoingCallFailed(SipuadaCallData sipuadaCallData, String reason);

    void callRinging(SipuadaCallData sipuadaCallData);

    void callAccepted(SipuadaCallData sipuadaCallData);

    void callDeclined(SipuadaCallData sipuadaCallData);

    void receiveCall(SipuadaCallData sipuadaCallData);

    void incomingCallCanceled(SipuadaCallData sipuadaCallData, String reason);

    void incomingCallFailed(SipuadaCallData sipuadaCallData, String reason);

    void acceptCall(SipuadaCallData sipuadaCallData);

    void declineCall(SipuadaCallData sipuadaCallData);

    void establishCall(SipuadaCallData sipuadaCallData);

    void finishCall(SipuadaCallData sipuadaCallData);

    void closeCall(SipuadaCallData sipuadaCallData);

    class CallInvitationSent {

        private final SipuadaCallData callData;

        public CallInvitationSent(SipuadaCallData callData) {
            this.callData = callData;
        }

        public SipuadaCallData getCallData() {
            return callData;
        }

    }
    @Subscribe
    void onCallInvitationSent(CallInvitationSent event);

    class CallInvitationCouldNotBeSent {

        private final SipuadaCallData callData;

        public CallInvitationCouldNotBeSent(SipuadaCallData callData) {
            this.callData = callData;
        }

        public SipuadaCallData getCallData() {
            return callData;
        }

    }
    @Subscribe
    void onCallInvitationCouldNotBeSent(CallInvitationCouldNotBeSent event);

    class CallInvitationCanceled {

        private final String reason;
        private final String callId;

        public CallInvitationCanceled(String reason, String callId) {
            this.reason = reason;
            this.callId = callId;
        }

        public String getReason() {
            return reason;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallInvitationCanceled(CallInvitationCanceled event);

    class CallInvitationCancelCouldNotBeSent {

        private final String callId;

        public CallInvitationCancelCouldNotBeSent(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallInvitationCancelCouldNotBeSent(CallInvitationCancelCouldNotBeSent event);

    class CallInvitationFailed {

        private final String reason;
        private final String callId;

        public CallInvitationFailed(String reason, String callId) {
            this.reason = reason;
            this.callId = callId;
        }

        public String getReason() {
            return reason;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallInvitationFailed(CallInvitationFailed event);

    class CallInvitationAnswerCouldNotBeSent {

        private final String callId;

        public CallInvitationAnswerCouldNotBeSent(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallInvitationAnswerCouldNotBeSent(CallInvitationAnswerCouldNotBeSent event);

    class EstablishedCallStarted {

        private final String callId;

        public EstablishedCallStarted(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallEstablished(EstablishedCallStarted event);

    class EstablishedCallFinished {

        private final String callId;

        public EstablishedCallFinished(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallFinished(EstablishedCallFinished event);

    class EstablishedCallFinishCouldNotBeSent {

        private final String callId;

        public EstablishedCallFinishCouldNotBeSent(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onEstablishedCallFinishCouldNotBeSent(EstablishedCallFinishCouldNotBeSent event);

    class EstablishedCallFailed {

        private final String callId;

        public EstablishedCallFailed(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }

    }
    @Subscribe
    void onCallFailure(EstablishedCallFailed event);

}

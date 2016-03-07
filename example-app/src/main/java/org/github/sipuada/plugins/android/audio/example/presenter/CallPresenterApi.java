package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.view.CallViewApi;

public interface CallPresenterApi extends SipuadaPresenterApi<CallViewApi> {

    void performAction(CallPresenter.CallAction callAction, SipuadaCallData sipuadaCallData);

    void makeCall(SipuadaCallData sipuadaCallData);

    void cancelCall(SipuadaCallData sipuadaCallData);

    void failCall(SipuadaCallData sipuadaCallData);

    void callAccepted(SipuadaCallData sipuadaCallData);

    void callDeclined(SipuadaCallData sipuadaCallData);

    void receiveCall(SipuadaCallData sipuadaCallData);

    void callCanceled(SipuadaCallData sipuadaCallData);

    void callFailed(SipuadaCallData sipuadaCallData);

    void acceptCall(SipuadaCallData sipuadaCallData);

    void declineCall(SipuadaCallData sipuadaCallData);

    void establishCall(SipuadaCallData sipuadaCallData);

    void finishCall(SipuadaCallData sipuadaCallData);

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

    class EstablishedCallFailed {

        private final String reason;
        private final String callId;

        public EstablishedCallFailed(String reason, String callId) {
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
    void onCallFailure(EstablishedCallFailed event);

}

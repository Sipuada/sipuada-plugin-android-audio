package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

public interface CallPresenterApi extends SipuadaPresenterApi<SipuadaViewApi> {

    interface OutgoingCallInvitationCallback {

        void onWaiting(String callId);

        void onRinging(String callId);

        void onDeclined();

        void onAccepted(String callId);

        void onFailed(String reason);

        void onCanceled(String reason);

    }

    void inviteUser(String username, String primaryHost, String remoteUser,
                    OutgoingCallInvitationCallback callback);

    void cancelInviteToUser(String username, String primaryHost, String callId);

    interface IncomingCallInvitationCallback {

        void onFailed(String reason);

        void onCanceled(String reason);

    }

    void willAnswerInviteFromUser(String callId, IncomingCallInvitationCallback callback);

    void acceptInviteFromUser(String username, String primaryHost, String callId);

    void declineInviteFromUser(String username, String primaryHost, String callId);

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

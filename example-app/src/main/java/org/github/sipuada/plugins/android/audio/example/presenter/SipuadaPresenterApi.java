package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public interface SipuadaPresenterApi extends MvpPresenter<SipuadaViewApi> {

    void bindToSipuadaService();

    void unbindFromSipuadaService();

    boolean sipuadaServiceIsConnected();

    void createSipuada(String username, String primaryHost, String password);

    interface RegistrationCallback {

        void onSuccess(List<String> registeredContacts);

        void onFailed(String reason);

    }

    void registerAddresses(String username, String primaryHost, RegistrationCallback callback);

    interface CallInvitationCallback {

        void onWaiting(String callId);

        void onRinging(String callId);

        void onDeclined();

        void onAccepted(String callId);

        void onFailed(String reason);

        void onCanceled(String reason);

    }

    void inviteUser(String username, String primaryHost, String remoteUser,
                    CallInvitationCallback callback);

    void cancelInviteToUser(String username, String primaryHost, String callId);

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

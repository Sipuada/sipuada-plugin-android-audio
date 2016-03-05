package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.MainViewApi;

import java.util.List;

public interface MainPresenterApi extends SipuadaPresenterApi<MainViewApi> {

    interface FetchUsersCredentialsCallback {

        void onSuccess(List<SipuadaUserCredentials> usersCredentials);

    }

    void createSipuada(String username, String primaryHost, String password);

    interface RegistrationCallback {

        void onSuccess(List<String> registeredContacts);

        void onFailed(String reason);

    }

    void registerAddresses(String username, String primaryHost, RegistrationCallback callback);

    //TODO TODO TODO TODO remove the stuff below as it will belong to the CallPresenterApi only

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

}

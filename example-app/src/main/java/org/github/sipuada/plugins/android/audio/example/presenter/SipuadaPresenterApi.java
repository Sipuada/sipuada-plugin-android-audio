package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

public interface SipuadaPresenterApi extends MvpPresenter<SipuadaViewApi> {

    void bindToSipuadaService();

    void unbindFromSipuadaService();

    boolean sipuadaServiceIsConnected();

    void createSipuada(String username, String primaryHost, String password);

    void registerAddresses(String username, String primaryHost,
                           SipuadaApi.RegistrationCallback callback);

    void inviteUser(String username, String primaryHost, String remoteUser,
                    SipuadaApi.CallInvitationCallback callback);

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

}

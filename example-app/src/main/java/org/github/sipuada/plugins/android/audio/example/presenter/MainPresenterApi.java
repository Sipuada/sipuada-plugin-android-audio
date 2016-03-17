package org.github.sipuada.plugins.android.audio.example.presenter;

import android.javax.sdp.SessionDescription;
import android.javax.sip.header.ContentTypeHeader;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public interface MainPresenterApi extends SipuadaPresenterApi<SipuadaViewApi> {

    void createSipuada(String username, String primaryHost, String password);

    void updateSipuada(SipuadaUserCredentials oldUserCredentials, String username,
                       String primaryHost, String password);

    interface RegistrationCallback {

        void onSuccess(List<String> registeredContacts);

        void onFailed(String reason);

    }

    void registerAddresses(String username, String primaryHost, RegistrationCallback callback);


    class OptionsQueryingSent {

        private final String username;
        private final String primaryHost;
        private final String remoteUsername;
        private final String remoteHost;

        public OptionsQueryingSent(String username, String primaryHost, String remoteUsername, String remoteHost) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.remoteUsername = remoteUsername;
            this.remoteHost = remoteHost;
        }

        public String getUsername() {
            return username;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public String getRemoteUsername() {
            return remoteUsername;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

    }
    @Subscribe
    void onOptionsQueryingSent(OptionsQueryingSent event);

    interface OptionsQueryingCallback {

        void onOptionsQueryingSuccess(String callId, SessionDescription content);

        void onOptionsQueryingFailed(String reason);

    }

    void queryingOptions(String username, String primaryHost, String remoteUsername, String remoteHost, OptionsQueryingCallback callback);

    class MessageSent {

        private final String username;
        private final String primaryHost;
        private final String remoteUsername;
        private final String remoteHost;

        public MessageSent(String username, String primaryHost, String remoteUsername, String remoteHost) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.remoteUsername = remoteUsername;
            this.remoteHost = remoteHost;
        }

        public String getUsername() {
            return username;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public String getRemoteUsername() {
            return remoteUsername;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

    }
    @Subscribe
    void onMessageSent(MessageSent event);

    interface MessageSendingCallback {

        void onMessageSendingSuccess(String callId, String content, ContentTypeHeader contentTypeHeader);

        void onMessageSendingFailed(String reason);

    }

    void sendMessage(String username, String primaryHost, String remoteUsername, String remoteHost, String content, ContentTypeHeader contentTypeHeader, MessageSendingCallback callback);

}

package org.github.sipuada.plugins.android.audio.example.presenter;

import android.javax.sdp.SessionDescription;
import android.javax.sip.header.ContentTypeHeader;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.MainViewApi;

import java.util.List;

public interface MainPresenterApi extends SipuadaPresenterApi<MainViewApi> {

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

    class MessageReceived {

        private final String remoteUsername;
        private final String remoteHost;
        private final String content;
        private final ContentTypeHeader contentTypeHeader;

        public MessageReceived(String remoteUsername, String remoteHost, String content, ContentTypeHeader contentTypeHeader) {
            this.remoteUsername = remoteUsername;
            this.remoteHost = remoteHost;
            this.content = content;
            this.contentTypeHeader = contentTypeHeader;
        }

        public String getRemoteUsername() {
            return remoteUsername;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public String getContent() {
            return content;
        }

        public ContentTypeHeader getContentTypeHeader() {
            return contentTypeHeader;
        }

    }
    @Subscribe
    void onMessageReceived(MessageReceived event);

    interface MessageSendingCallback {

        void onMessageSendingSuccess(String callId);

        void onMessageSendingFailed(String reason);

    }

    void sendMessage(String username, String primaryHost, String remoteUsername, String remoteHost, String content, ContentTypeHeader contentTypeHeader, MessageSendingCallback callback);

}

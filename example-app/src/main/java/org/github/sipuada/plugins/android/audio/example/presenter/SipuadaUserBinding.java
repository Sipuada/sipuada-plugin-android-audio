package org.github.sipuada.plugins.android.audio.example.presenter;

import org.github.sipuada.SipuadaApi;

public class SipuadaUserBinding {

    protected final SipuadaUserCredentials mCredentials;
    protected final SipuadaApi.SipuadaListener mListener;
    protected final String[] mLocalAddresses;

    public class SipuadaUserCredentials {

        protected final String username, primaryHost, password;

        public SipuadaUserCredentials(String username, String primaryHost, String password) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.password = password;
        }

    }

    public SipuadaUserBinding(String username, String primaryHost, String password,
                              SipuadaApi.SipuadaListener listener, String... localAddresses) {
        mCredentials = new SipuadaUserCredentials(username, primaryHost, password);
        mListener = listener;
        mLocalAddresses = localAddresses;
    }

}

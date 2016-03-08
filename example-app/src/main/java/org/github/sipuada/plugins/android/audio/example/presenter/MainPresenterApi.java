package org.github.sipuada.plugins.android.audio.example.presenter;

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

}

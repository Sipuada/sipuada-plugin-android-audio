package org.github.sipuada.plugins.android.audio.example.presenter;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.MainViewApi;

import java.util.List;

public interface MainPresenterApi extends SipuadaPresenterApi<MainViewApi> {

    interface FetchUsersCredentialsCallback {

        void onSuccess(List<SipuadaUserCredentials> usersCredentials);

    }

    void createSipuada(String username, String primaryHost, String password);

    void updateSipuada(SipuadaUserCredentials oldUserCredentials, String username,
                       String primaryHost, String password);

    interface RegistrationCallback {

        void onSuccess(List<String> registeredContacts);

        void onFailed(String reason);

    }

    void registerAddresses(String username, String primaryHost, RegistrationCallback callback);

}

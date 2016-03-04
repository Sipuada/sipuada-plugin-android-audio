package org.github.sipuada.plugins.android.audio.example.view;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;

import java.util.List;

public interface MainViewApi extends SipuadaViewApi {

    void refreshUsersCredentialsList(List<SipuadaUserCredentials> usersCredentials);

}

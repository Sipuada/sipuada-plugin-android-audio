package org.github.sipuada.plugins.android.audio.example.view;

import android.content.ServiceConnection;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;

import java.util.List;

public interface SipuadaViewApi extends MvpView {

    boolean doBindToSipuadaService(ServiceConnection serviceConnection);

    void sipuadaServiceConnected();

    void sipuadaServiceDisconnected();

    void doUnbindFromSipuadaService(ServiceConnection serviceConnection);

    void refreshUsersCredentialsList(List<SipuadaUserCredentials> usersCredentials);

}

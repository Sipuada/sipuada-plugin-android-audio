package org.github.sipuada.plugins.android.audio.example.presenter;

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

}

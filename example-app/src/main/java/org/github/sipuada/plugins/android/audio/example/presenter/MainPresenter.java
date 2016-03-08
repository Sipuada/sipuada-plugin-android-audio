package org.github.sipuada.plugins.android.audio.example.presenter;

import android.util.Log;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public class MainPresenter extends SipuadaPresenter<SipuadaViewApi> implements MainPresenterApi {

    @Override
    protected void doUponServiceConnected() {
        fetchLocalUsersThenRefresh();
    }

    @Override
    protected void doUponServiceDisconnected() {}

    @Override
    public void createSipuada(String username, String primaryHost, String password) {
        sipuadaService.createSipuada(new SipuadaUserCredentials(username, primaryHost, password));
        fetchLocalUsersThenRefresh();
    }

    @Override
    public void updateSipuada(SipuadaUserCredentials oldUserCredentials, String username,
                              String primaryHost, String password) {
        sipuadaService.updateSipuada(oldUserCredentials, new SipuadaUserCredentials(username,
                primaryHost, password));
        fetchLocalUsersThenRefresh();
    }

    @Override
    public void registerAddresses(String username, String primaryHost, final RegistrationCallback callback) {
        sipuadaService.registerAddresses(username, primaryHost, new SipuadaApi.RegistrationCallback() {

            @Override
            public void onRegistrationSuccess(final List<String> registeredContacts) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onRegistrationSuccess; registeredContacts:{%s}]",
                                registeredContacts));
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onSuccess(registeredContacts);
                    }

                });
            }

            @Override
            public void onRegistrationFailed(final String reason) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onRegistrationFailed; reason:{%s}]", reason));
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onFailed(reason);
                    }

                });
            }

        });
    }

}

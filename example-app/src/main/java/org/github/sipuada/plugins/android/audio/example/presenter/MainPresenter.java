package org.github.sipuada.plugins.android.audio.example.presenter;

import android.javax.sdp.SessionDescription;
import android.util.Log;

import com.google.common.eventbus.Subscribe;

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


    @Override
    public void queryingOptions(String username, String primaryHost, String remoteUsername, String remoteHost, final OptionsQueryingCallback callback) {
        sipuadaService.queryOptions(username, primaryHost, remoteUsername, remoteHost, new SipuadaApi.OptionsQueryingCallback() {

            @Override
            public void onOptionsQueryingSuccess(final String callId, final SessionDescription sessionDescription) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onOptionsQueryingSuccess; callId:{%s}, sessionDescription:{%s}]",
                                callId, sessionDescription.toString()));
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onOptionsQueryingSuccess(callId, sessionDescription);
                    }
                });
            }

            @Override
            public void onOptionsQueryingFailed(final String reason) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onOptionsQueryingFailed; reason:{%s}]", reason));
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onOptionsQueryingFailed(reason);
                    }
                });
            }
        });
    }

    @Override
    @Subscribe
    public void onOptionsQueryingSent(OptionsQueryingSent event) {
        Log.d(SipuadaApplication.TAG,
                String.format("[onOptionsQueryingSent...]"));
    }

}

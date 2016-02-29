package org.github.sipuada.plugins.android.audio.example.presenter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.github.sipuada.Sipuada;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SipuadaService extends Service {

    private final IBinder binder = new SipuadaBinder();
    private Map<String, Sipuada> sipuadaInstances = new HashMap<>();

    public class SipuadaBinder extends Binder {
        public SipuadaService getService() {
            return SipuadaService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize(List<SipuadaUserBinding> usersBindings) {
        //initialize plugin
        for (SipuadaUserBinding userBinding : usersBindings) {
            String username = userBinding.mCredentials.username;
            String primaryHost = userBinding.mCredentials.primaryHost;
            String password = userBinding.mCredentials.password;
            Sipuada sipuada = new Sipuada(userBinding.mListener,
                    username, primaryHost, password, userBinding.mLocalAddresses);
            sipuadaInstances.put(getSipuadaInstanceKey(username, primaryHost), sipuada);
            //sipuada.registerPlugin(plugin);
        }
    }

    public Sipuada getSipuada(String username, String primaryHost) {
        return sipuadaInstances.get(getSipuadaInstanceKey(username, primaryHost));
    }

    @Override
    public void onDestroy() {
        for (String usernameAtPrimaryHost : sipuadaInstances.keySet()) {
            Sipuada sipuada = sipuadaInstances.get(usernameAtPrimaryHost);
            sipuada.destroySipuada();
        }
    }

    private String getSipuadaInstanceKey(String username, String primaryHost) {
        return String.format("%s@%s", username, primaryHost);
    }

}

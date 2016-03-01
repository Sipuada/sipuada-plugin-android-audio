package org.github.sipuada.plugins.android.audio.example.presenter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SipuadaService extends Service {

    private static final String TAG = SipuadaService.class.toString();

    private final IBinder binder = new SipuadaBinder();
    private Map<String, Sipuada> sipuadaInstances = new HashMap<>();

    public class SipuadaBinder extends Binder {
        public SipuadaService getService() {
            return SipuadaService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialize();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize() {
        List<SipuadaUserBinding> usersBindings = new ArrayList<>();
        SipuadaApi.SipuadaListener listener = new SipuadaApi.SipuadaListener() {

            @Override
            public boolean onCallInvitationArrived(String callId) {
                Log.d(TAG, String.format("[onCallInvitationArrived; callId:{%s}]", callId));
                if (!SipuadaApplication.CURRENTLY_BUSY_FROM_DB) {
                    Intent intent = new Intent(getApplicationContext(), SipuadaActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return SipuadaApplication.CURRENTLY_BUSY_FROM_DB;
            }

            @Override
            public void onCallInvitationCanceled(String reason, String callId) {
                Log.d(TAG, String.format("[onCallInvitationCanceled; reason:{%s}, callId:{%s}]", reason, callId));
            }

            @Override
            public void onCallInvitationFailed(String reason, String callId) {
                Log.d(TAG, String.format("[onCallInvitationFailed; reason:{%s}, callId:{%s}]", reason, callId));
            }

            @Override
            public void onCallEstablished(String callId) {
                Log.d(TAG, String.format("[onCallEstablished; callId:{%s}]", callId));
            }

            @Override
            public void onCallFinished(String callId) {
                Log.d(TAG, String.format("[onCallFinished; callId:{%s}]", callId));
            }

            @Override
            public void onCallFailure(String reason, String callId) {
                Log.d(TAG, String.format("[onCallFailure; reason:{%s}, callId:{%s}]", reason, callId));
            }

        };
        for (String username : SipuadaApplication.USERNAMES_FROM_DB) {
            usersBindings.add(new SipuadaUserBinding(username, SipuadaApplication.PRIMARY_HOST_FROM_DB,
                    username, listener, getLocalAddresses()));
        }
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

    private String[] getLocalAddresses() {
        List<String> localAdresses = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return new String[0];
            }
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : addresses) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        if (inetAddress instanceof Inet4Address) {
                            String localIpAddress = inetAddress.getHostAddress();
                            int localPort = 50000 + (new Random()).nextInt(10000);
                            localAdresses.add(String.format("%s:%s/TCP", localIpAddress, localPort));
                        }
                    }
                }
            }
        } catch (SocketException ioException) {
            return new String[0];
        }
        return localAdresses.toArray(new String[localAdresses.size()]);
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
        super.onDestroy();
    }

    private String getSipuadaInstanceKey(String username, String primaryHost) {
        return String.format("%s@%s", username, primaryHost);
    }

}

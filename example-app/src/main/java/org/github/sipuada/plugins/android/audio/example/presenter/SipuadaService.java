package org.github.sipuada.plugins.android.audio.example.presenter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.activeandroid.query.Select;

import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SipuadaService extends Service {

    private SipuadaServiceHandler serviceHandler;
    private final IBinder binder = new SipuadaBinder();

    private Map<String, Sipuada> sipuadaInstances = new HashMap<>();

    private static final int INITIALIZE_SIPUADAS = 0;
    private static final int ADD_NEW_SIPUADA = 1;
    private static final int REGISTER_ADDRESSES = 2;

    private final class SipuadaServiceHandler extends Handler {

        public SipuadaServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case INITIALIZE_SIPUADAS:
                    initialize();
                    break;
                case ADD_NEW_SIPUADA:
                    doCreateSipuada((SipuadaUserCredentials) message.obj);
                    break;
                case REGISTER_ADDRESSES:
                    doRegisterAddresses((RegisterAddressesOperation) message.obj);
                    break;
                default:
                    break;
            }
        }

    }

    public class SipuadaBinder extends Binder {
        public SipuadaService getService() {
            return SipuadaService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("SipuadaService",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        serviceHandler = new SipuadaServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceHandler.sendMessage(serviceHandler.obtainMessage(INITIALIZE_SIPUADAS));
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        for (String usernameAtPrimaryHost : sipuadaInstances.keySet()) {
            Sipuada sipuada = sipuadaInstances.get(usernameAtPrimaryHost);
            sipuada.destroySipuada();
        }
        super.onDestroy();
    }

    public void createSipuada(SipuadaUserCredentials userCredentials) {
        Message message = serviceHandler.obtainMessage(ADD_NEW_SIPUADA);
        message.obj = userCredentials;
        serviceHandler.sendMessage(message);
    }

    protected class RegisterAddressesOperation {

        private final String username;
        private final String primaryHost;
        private final SipuadaApi.RegistrationCallback callback;

        public RegisterAddressesOperation(String username, String primaryHost,
                                          SipuadaApi.RegistrationCallback callback) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.callback = callback;
        }

        public String getUsername() {
            return username;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public SipuadaApi.RegistrationCallback getCallback() {
            return callback;
        }

    }

    public void registerAddresses(String username, String primaryHost,
                                     SipuadaApi.RegistrationCallback callback) {
        Message message = serviceHandler.obtainMessage(REGISTER_ADDRESSES);
        message.obj = new RegisterAddressesOperation(username, primaryHost, callback);
        serviceHandler.sendMessage(message);
    }

    private void initialize() {
        List<SipuadaUserCredentials> usersCredentials = new Select()
                .from(SipuadaUserCredentials.class).execute();
        for (SipuadaUserCredentials userCredentials : usersCredentials) {
//            AndroidAudioSipuadaPlugin sipuadaPluginForAudio =
//                    new AndroidAudioSipuadaPlugin(userCredentials.username,
//                            localAddresses[0], getApplicationContext());
            String username = userCredentials.username;
            String primaryHost = userCredentials.primaryHost;
            String password = userCredentials.password;
            String[] localAddresses = getLocalAddresses();
            Sipuada sipuada = new Sipuada(sipuadaListener,
                    username, primaryHost, password, localAddresses);
//            sipuada.registerPlugin(sipuadaPluginForAudio);
            sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
        }
    }

    private void doCreateSipuada(SipuadaUserCredentials userCredentials) {
        String[] localAddresses = getLocalAddresses();
        String username = userCredentials.getUsername();
        String primaryHost = userCredentials.getPrimaryHost();
        String password = userCredentials.getPassword();
//        AndroidAudioSipuadaPlugin sipuadaPluginForAudio =
//                new AndroidAudioSipuadaPlugin(username, localAddresses[0], getApplicationContext());
        Sipuada sipuada = new Sipuada(sipuadaListener,
                username, primaryHost, password, localAddresses);
//        sipuada.registerPlugin(sipuadaPluginForAudio);
        sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
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

    private String getSipuadaKey(String username, String primaryHost) {
        return String.format("%s@%s", username, primaryHost);
    }

    private void doRegisterAddresses(RegisterAddressesOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.registerAddresses(operation.getCallback());
    }

    private Sipuada getSipuada(String username, String primaryHost) {
        return sipuadaInstances.get(getSipuadaKey(username, primaryHost));
    }

    SipuadaApi.SipuadaListener sipuadaListener = new SipuadaApi.SipuadaListener() {

        @Override
        public boolean onCallInvitationArrived(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationArrived; callId:{%s}]", callId));
            if (!SipuadaApplication.CURRENTLY_BUSY_FROM_DB) {
                Intent intent = new Intent(getApplicationContext(), SipuadaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return SipuadaApplication.CURRENTLY_BUSY_FROM_DB;
        }

        @Override
        public void onCallInvitationCanceled(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationCanceled; reason:{%s}, callId:{%s}]", reason, callId));
        }

        @Override
        public void onCallInvitationFailed(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationFailed; reason:{%s}, callId:{%s}]", reason, callId));
        }

        @Override
        public void onCallEstablished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallEstablished; callId:{%s}]", callId));
        }

        @Override
        public void onCallFinished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFinished; callId:{%s}]", callId));
        }

        @Override
        public void onCallFailure(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFailure; reason:{%s}, callId:{%s}]", reason, callId));
        }

    };

}

package org.github.sipuada.plugins.android.audio.example.presenter;

import android.app.Service;
import android.content.Intent;
import android.javax.sip.header.ContentTypeHeader;
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
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.AndroidAudioSipuadaPlugin;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SipuadaService extends Service {

    private SipuadaServiceHandler serviceHandler;
    private final IBinder binder = new SipuadaBinder();

    private final Map<String, Sipuada> sipuadaInstances = new HashMap<>();
    private final EventBus eventBus = new EventBus();

    private static final int INITIALIZE_SIPUADAS = 0;
    private static final int FETCH_USERS_CREDENTIALS = 1;
    private static final int ADD_NEW_SIPUADA = 2;
    private static final int UPDATE_SIPUADA = 3;
    private static final int REGISTER_ADDRESSES = 4;
    private static final int INVITE_USER = 5;
    private static final int CANCEL_USER_INVITE = 6;
    private static final int ACCEPT_USER_INVITE = 7;
    private static final int DECLINE_USER_INVITE = 8;
    private static final int FINISH_CALL = 9;

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
                case FETCH_USERS_CREDENTIALS:
                    doFetchCurrentUsersCredentials((MainPresenterApi
                            .FetchUsersCredentialsCallback) message.obj);
                    break;
                case ADD_NEW_SIPUADA:
                    doCreateSipuada((SipuadaUserCredentials) message.obj);
                    break;
                case UPDATE_SIPUADA:
                    doUpdateSipuada((UpdateSipuadaOperation) message.obj);
                    break;
                case REGISTER_ADDRESSES:
                    doRegisterAddresses((RegisterAddressesOperation) message.obj);
                    break;
                case INVITE_USER:
                    doInviteUser((InviteUserOperation) message.obj);
                    break;
                case CANCEL_USER_INVITE:
                    doCancelInviteToUser((SipuadaCallData) message.obj);
                    break;
                case ACCEPT_USER_INVITE:
                    doAcceptInviteFromUser((SipuadaCallData) message.obj);
                    break;
                case DECLINE_USER_INVITE:
                    doDeclineInviteFromUser((SipuadaCallData) message.obj);
                    break;
                case FINISH_CALL:
                    doFinishCall((SipuadaCallData) message.obj);
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
        eventBus.register(this);
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
        eventBus.unregister(this);
        for (String usernameAtPrimaryHost : sipuadaInstances.keySet()) {
            Sipuada sipuada = sipuadaInstances.get(usernameAtPrimaryHost);
            sipuada.destroySipuada();
        }
        super.onDestroy();
    }

    protected void registerSipuadaPresenter(SipuadaPresenterApi presenter) {
        eventBus.register(presenter);
    }

    public void fetchCurrentUsersCredentials(MainPresenterApi.FetchUsersCredentialsCallback callback) {
        Message message = serviceHandler.obtainMessage(FETCH_USERS_CREDENTIALS);
        message.obj = callback;
        serviceHandler.sendMessage(message);
    }

    public void createSipuada(SipuadaUserCredentials userCredentials) {
        Message message = serviceHandler.obtainMessage(ADD_NEW_SIPUADA);
        message.obj = userCredentials;
        serviceHandler.sendMessage(message);
    }

    protected class UpdateSipuadaOperation {

        private final SipuadaUserCredentials oldUserCredentials;
        private final SipuadaUserCredentials newUserCredentials;

        public UpdateSipuadaOperation(SipuadaUserCredentials oldUserCredentials,
                                      SipuadaUserCredentials newUserCredentials) {
            this.oldUserCredentials = oldUserCredentials;
            this.newUserCredentials = newUserCredentials;
        }

        public SipuadaUserCredentials getOldUserCredentials() {
            return oldUserCredentials;
        }

        public SipuadaUserCredentials getNewUserCredentials() {
            return newUserCredentials;
        }

    }

    public void updateSipuada(SipuadaUserCredentials oldUserCredentials, SipuadaUserCredentials newUserCredentials) {
        Message message = serviceHandler.obtainMessage(UPDATE_SIPUADA);
        message.obj = new UpdateSipuadaOperation(oldUserCredentials, newUserCredentials);
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

    protected class InviteUserOperation {

        private final SipuadaCallData callData;
        private final SipuadaApi.CallInvitationCallback callback;

        public InviteUserOperation(SipuadaCallData callData, SipuadaApi.CallInvitationCallback callback) {
            this.callData = callData;
            this.callback = callback;
        }

        public SipuadaCallData getCallData() {
            return callData;
        }

        public SipuadaApi.CallInvitationCallback getCallback() {
            return callback;
        }

    }

    public void inviteUser(SipuadaCallData callData, SipuadaApi.CallInvitationCallback callback) {
        Message message = serviceHandler.obtainMessage(INVITE_USER);
        message.obj = new InviteUserOperation(callData, callback);
        serviceHandler.sendMessage(message);
    }

    public void cancelInviteToUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(CANCEL_USER_INVITE);
        message.obj = new SipuadaCallData(callId, username, primaryHost, null, null);
        serviceHandler.sendMessage(message);
    }

    public void acceptInviteFromUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(ACCEPT_USER_INVITE);
        message.obj = new SipuadaCallData(callId, username, primaryHost, null, null);
        serviceHandler.sendMessage(message);
    }

    public void declineInviteFromUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(DECLINE_USER_INVITE);
        message.obj = new SipuadaCallData(callId, username, primaryHost, null, null);
        serviceHandler.sendMessage(message);
    }

    public void finishCall(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(FINISH_CALL);
        message.obj = new SipuadaCallData(callId, username, primaryHost, null, null);
        serviceHandler.sendMessage(message);
    }

    private final SipuadaApi.RegistrationCallback registrationCallback = new SipuadaApi
            .RegistrationCallback() {

        @Override
        public void onRegistrationSuccess(List<String> registeredContacts) {
            Log.d(SipuadaApplication.TAG, "Registration refresh of: " + Arrays
                    .toString(registeredContacts.toArray(new String[registeredContacts.size()])));
        }

        @Override
        public void onRegistrationFailed(String reason) {
            Log.d(SipuadaApplication.TAG, "Registration refresh failed: " + reason);
        }

    };

    private void initialize() {
        List<SipuadaUserCredentials> usersCredentials = new Select()
                .from(SipuadaUserCredentials.class).execute();
        for (SipuadaUserCredentials userCredentials : usersCredentials) {
            final String username = userCredentials.getUsername();
            final String primaryHost = userCredentials.getPrimaryHost();
            String[] localAddresses = getLocalAddresses(primaryHost);
            AndroidAudioSipuadaPlugin sipuadaPluginForAudio =
                    new AndroidAudioSipuadaPlugin(userCredentials.getUsername(), getApplicationContext());
            String password = userCredentials.getPassword();
            Sipuada sipuada = new Sipuada(new SipuadaServiceListener() {

                @Override
                public boolean onCallInvitationArrived(final String callId, final String remoteUsername,
                                                       final String remoteHost) {
                    Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationArrived;" +
                            " callId:{%s}]", callId));
                    return handleIncomingCallInvitation(callId, username, primaryHost,
                            remoteUsername, remoteHost);
                }

            }, username, primaryHost, password, localAddresses);
            sipuada.registerPlugin(sipuadaPluginForAudio);
            sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
            sipuada.registerAddresses(registrationCallback);
        }
    }

    private void doFetchCurrentUsersCredentials(MainPresenterApi.FetchUsersCredentialsCallback callback) {
        List<SipuadaUserCredentials> usersCredentials = new Select()
                .from(SipuadaUserCredentials.class).execute();
        callback.onSuccess(usersCredentials);
    }

    private final Map<String, Timer> callInvitationDispatchers = new HashMap<>();

    private void doCreateSipuada(SipuadaUserCredentials userCredentials) {
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        String[] localAddresses = getLocalAddresses(primaryHost);
        final String password = userCredentials.getPassword();
        AndroidAudioSipuadaPlugin sipuadaPluginForAudio = new AndroidAudioSipuadaPlugin(username, getApplicationContext());
        Sipuada sipuada = new Sipuada(new SipuadaServiceListener() {

            @Override
            public boolean onCallInvitationArrived(String callId, String remoteUsername,
                                                   String remoteHost) {
                Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationArrived;" +
                        " callId:{%s}]", callId));
                return handleIncomingCallInvitation(callId, username, primaryHost,
                        remoteUsername, remoteHost);
            }

        }, username, primaryHost, password, localAddresses);
        sipuada.registerPlugin(sipuadaPluginForAudio);
        sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
        sipuada.registerAddresses(registrationCallback);
    }

    private void doUpdateSipuada(UpdateSipuadaOperation operation) {
        SipuadaUserCredentials oldUserCredentials = operation.getOldUserCredentials();
        final String oldUsername = oldUserCredentials.getUsername();
        final String oldPrimaryHost = oldUserCredentials.getPrimaryHost();
        removeSipuada(oldUsername, oldPrimaryHost);
        SipuadaUserCredentials newUserCredentials = operation.getNewUserCredentials();
        final String newUsername = newUserCredentials.getUsername();
        final String newPrimaryHost = newUserCredentials.getPrimaryHost();
        final String newPassword = newUserCredentials.getPassword();
        String[] localAddresses = getLocalAddresses(newPrimaryHost);
        Sipuada sipuada = new Sipuada(new SipuadaServiceListener() {

            @Override
            public boolean onCallInvitationArrived(String callId, String remoteUsername,
                                                   String remoteHost) {
                Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationArrived;" +
                        " callId:{%s}]", callId));
                return handleIncomingCallInvitation(callId, newUsername, newPrimaryHost,
                        remoteUsername, remoteHost);
            }

        }, newUsername, newPrimaryHost, newPassword, localAddresses);
        sipuadaInstances.put(getSipuadaKey(newUsername, newPrimaryHost), sipuada);
        sipuada.registerAddresses(registrationCallback);
    }

    private boolean handleIncomingCallInvitation(final String callId, final String username,
                                                 final String primaryHost, final String remoteUsername, final String remoteHost) {
//        if (!SipuadaApplication.CURRENTLY_BUSY_FROM_DB) {
        Timer timer = new Timer();
        callInvitationDispatchers.put(callId, timer);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                callInvitationDispatchers.remove(callId);
                Intent intent = new Intent(getApplicationContext(),
                        CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(SipuadaApplication.KEY_CALL_ACTION,
                        CallPresenter.CallAction.RECEIVE_CALL);
                intent.putExtra(SipuadaApplication.KEY_CALL_ID, callId);
                intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_USERNAME, remoteUsername);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_HOST, remoteHost);
                startActivity(intent);
            }

        }, 2000);
//        } else {
//            //TODO add a notification for this incoming call invitation while you're busy
//        }
//        return SipuadaApplication.CURRENTLY_BUSY_FROM_DB;
        return false;
    }

    private String[] getLocalAddresses(String primaryHost) {
        int primaryHostPort = 5060;
        InetAddress primaryHostAddress;
        try {
            if (primaryHost.contains(":")) {
                primaryHostAddress = InetAddress.getByName(primaryHost.split(":")[0]);
                primaryHostPort = Integer.parseInt(primaryHost.split(":")[1].split(";")[0]);
            } else {
                primaryHostAddress = InetAddress.getByName(primaryHost);
            }
        } catch (UnknownHostException invalidHost) {
            return new String[0];
        }
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
                        Log.wtf(SipuadaApplication.TAG, "IS " + inetAddress + " REACHABLE?");
                        if (inetAddress instanceof Inet4Address) {
                            int localPort = 50000 + (new Random()).nextInt(10000);
                            boolean isReachable;
                            try {
                                isReachable = isAddressReachable(primaryHostAddress, primaryHostPort,
                                        inetAddress, localPort);
                            } catch (IOException ioException) {
                                isReachable = false;
                            }
                            Log.wtf(SipuadaApplication.TAG, "VEREDICT: " + isReachable);
                            if (isReachable) {
                                String localIpAddress = inetAddress.getHostAddress();
                                localAdresses.add(String.format("%s:%s/TCP", localIpAddress, localPort));
                            }
                        } else {
                            Log.wtf(SipuadaApplication.TAG, "VEREDICT: false, is IPv6");
                        }
                    }
                }
            }
        } catch (SocketException ioException) {
            return new String[0];
        }
        return localAdresses.toArray(new String[localAdresses.size()]);
    }

    private boolean isAddressReachable(InetAddress destination, int destinationPort,
                                       InetAddress source, int sourcePort) throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(source, sourcePort));
        socket.connect(new InetSocketAddress(destination, destinationPort), 5000);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            return socket.isConnected();
        } finally {
            socket.close();
        }
    }

    private String getSipuadaKey(String username, String primaryHost) {
        return String.format("%s@%s", username, primaryHost);
    }

    private void doRegisterAddresses(RegisterAddressesOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.registerAddresses(operation.getCallback());
    }

    private void doInviteUser(InviteUserOperation operation) {
        SipuadaCallData sipuadaCallData = operation.getCallData();
        String username = sipuadaCallData.getUsername();
        String primaryHost = sipuadaCallData.getPrimaryHost();
        String remoteUsername = sipuadaCallData.getRemoteUsername();
        String remoteHost = sipuadaCallData.getRemoteHost();
        Sipuada sipuada = getSipuada(username, primaryHost);
        String callId = sipuada.inviteToCall(remoteUsername, remoteHost, operation.getCallback());
        sipuadaCallData.setCallId(callId);
        eventBus.post(new CallPresenterApi.CallInvitationSent(sipuadaCallData));
    }

    public void doCancelInviteToUser(SipuadaCallData operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.cancelCallInvitation(operation.getCallId());
    }

    public void doAcceptInviteFromUser(SipuadaCallData operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.acceptCallInvitation(operation.getCallId());
//        SipuadaApplication.CURRENTLY_BUSY_FROM_DB = true;
    }

    public void doFinishCall(SipuadaCallData operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.finishCall(operation.getCallId());
//        SipuadaApplication.CURRENTLY_BUSY_FROM_DB = false;
    }

    public void doDeclineInviteFromUser(SipuadaCallData operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.declineCallInvitation(operation.getCallId());
    }

    private Sipuada getSipuada(String username, String primaryHost) {
        return sipuadaInstances.get(getSipuadaKey(username, primaryHost));
    }

    private void removeSipuada(String username, String primaryHost) {
        Sipuada sipuada = sipuadaInstances.remove(getSipuadaKey(username, primaryHost));
        if (sipuada != null) {
            sipuada.destroySipuada();
        }
        SipuadaUserCredentials self = new Select().from(SipuadaUserCredentials.class)
                .where("Username = ? AND PrimaryHost = ?", username, primaryHost).executeSingle();
        self.delete();
    }

    abstract class SipuadaServiceListener implements SipuadaApi.SipuadaListener {

        @Override
        public abstract boolean onCallInvitationArrived(String callId, String remoteUsername,
                                                        String remoteHost);

        @Override
        public void onCallInvitationCanceled(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationCanceled;" +
                    " reason:{%s}, callId:{%s}]", reason, callId));
            Timer callInvitationDispatcher = callInvitationDispatchers.remove(callId);
            if (callInvitationDispatcher != null) {
                callInvitationDispatcher.cancel();
            } else {
                //TODO add a notification for this canceled incoming call invitation
            }
            eventBus.post(new CallPresenterApi.CallInvitationCanceled(reason, callId));
        }

        @Override
        public void onCallInvitationFailed(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallInvitationFailed;" +
                    " reason:{%s}, callId:{%s}]", reason, callId));
            Timer callInvitationDispatcher = callInvitationDispatchers.remove(callId);
            if (callInvitationDispatcher != null) {
                callInvitationDispatcher.cancel();
            } else {
                //TODO add a notification for this failed incoming call invitation
            }
            eventBus.post(new CallPresenterApi.CallInvitationFailed(reason, callId));
        }

        @Override
        public void onCallEstablished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallEstablished;" +
                    " callId:{%s}]", callId));
            eventBus.post(new CallPresenterApi.EstablishedCallStarted(callId));
        }

        @Override
        public void onCallFinished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFinished;" +
                    " callId:{%s}]", callId));
//            SipuadaApplication.CURRENTLY_BUSY_FROM_DB = false;
            eventBus.post(new CallPresenterApi.EstablishedCallFinished(callId));
        }

        @Override
        public void onCallFailure(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFailure;" +
                    " reason:{%s}, callId:{%s}]", reason, callId));
//            SipuadaApplication.CURRENTLY_BUSY_FROM_DB = false;
            eventBus.post(new CallPresenterApi.EstablishedCallFailed(reason, callId));
        }

        @Override
        public void onInfoReceived(String callId, ContentTypeHeader contentType, String content) {}

        @Override
        public void onMessageReceived(String callId, ContentTypeHeader contentType, String content) {}

    }

    @Subscribe
    public void onEventCouldNotBeReceivedByAPresenter(final DeadEvent event) {
        Log.w(SipuadaApplication.TAG, String.format("[onEventCouldNotBeReceivedByAPresenter;" +
                " event:{%s}]", event.getEvent()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                eventBus.post(event.getEvent());
            }

        }, 1000);
    }

}

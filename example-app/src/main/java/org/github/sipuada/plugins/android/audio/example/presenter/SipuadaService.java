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
import com.google.common.eventbus.EventBus;

import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.IncomingCallInvitationActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;
import org.github.sipuada.plugins.nop.NoOperationSipuadaPlugin;

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
    private static final int REGISTER_ADDRESSES = 3;
    private static final int INVITE_USER = 4;
    private static final int CANCEL_USER_INVITE = 5;
    private static final int ACCEPT_USER_INVITE = 6;
    private static final int DECLINE_USER_INVITE = 7;

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
                    doFetchCurrentUsersCredentials((SipuadaPresenter
                            .FetchUsersCredentialsCallback) message.obj);
                    break;
                case ADD_NEW_SIPUADA:
                    doCreateSipuada((SipuadaUserCredentials) message.obj);
                    break;
                case REGISTER_ADDRESSES:
                    doRegisterAddresses((RegisterAddressesOperation) message.obj);
                    break;
                case INVITE_USER:
                    doInviteUser((InviteUserOperation) message.obj);
                    break;
                case CANCEL_USER_INVITE:
                    doCancelInviteToUser((UserInviteOperation) message.obj);
                    break;
                case ACCEPT_USER_INVITE:
                    doAcceptInviteFromUser((UserInviteOperation) message.obj);
                    break;
                case DECLINE_USER_INVITE:
                    doDeclineInviteFromUser((UserInviteOperation) message.obj);
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

    protected void registerSipuadaPresenter(SipuadaPresenterApi presenter) {
        eventBus.register(presenter);
    }

    public void fetchCurrentUsersCredentials(SipuadaPresenter.FetchUsersCredentialsCallback callback) {
        Message message = serviceHandler.obtainMessage(FETCH_USERS_CREDENTIALS);
        message.obj = callback;
        serviceHandler.sendMessage(message);
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

    protected class InviteUserOperation {

        private final String username;
        private final String primaryHost;
        private final String remoteUsername;
        private final String remoteHost;
        private final SipuadaApi.CallInvitationCallback callback;

        public InviteUserOperation(String username, String primaryHost,
                                   String remoteUsername, String remoteHost,
                                   SipuadaApi.CallInvitationCallback callback) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.remoteUsername = remoteUsername;
            this.remoteHost = remoteHost;
            this.callback = callback;
        }

        public String getUsername() {
            return username;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public String getRemoteUsername() {
            return remoteUsername;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public SipuadaApi.CallInvitationCallback getCallback() {
            return callback;
        }

    }

    public void inviteUser(String username, String primaryHost, String remoteUser,
                           SipuadaApi.CallInvitationCallback callback) {
        Message message = serviceHandler.obtainMessage(INVITE_USER);
        String remoteUsername = remoteUser.split("@")[0];
        String remoteHost = remoteUser.split("@")[1];
        message.obj = new InviteUserOperation(username, primaryHost,
                remoteUsername, remoteHost, callback);
        serviceHandler.sendMessage(message);
    }

    protected class UserInviteOperation {

        private final String username;
        private final String primaryHost;
        private final String callId;

        public UserInviteOperation(String username, String primaryHost, String callId) {
            this.username = username;
            this.primaryHost = primaryHost;
            this.callId = callId;
        }

        public String getUsername() {
            return username;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public String getCallId() {
            return callId;
        }

    }

    public void cancelInviteToUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(CANCEL_USER_INVITE);
        message.obj = new UserInviteOperation(username, primaryHost, callId);
        serviceHandler.sendMessage(message);
    }

    public void acceptInviteFromUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(ACCEPT_USER_INVITE);
        message.obj = new UserInviteOperation(username, primaryHost, callId);
        serviceHandler.sendMessage(message);
    }

    public void declineInviteFromUser(String username, String primaryHost, String callId) {
        Message message = serviceHandler.obtainMessage(DECLINE_USER_INVITE);
        message.obj = new UserInviteOperation(username, primaryHost, callId);
        serviceHandler.sendMessage(message);
    }

    private void initialize() {
        List<SipuadaUserCredentials> usersCredentials = new Select()
                .from(SipuadaUserCredentials.class).execute();
        for (SipuadaUserCredentials userCredentials : usersCredentials) {
//            AndroidAudioSipuadaPlugin sipuadaPluginForAudio =
//                    new AndroidAudioSipuadaPlugin(userCredentials.username,
//                            localAddresses[0], getApplicationContext());
            NoOperationSipuadaPlugin noopSipuadaPlugin = new NoOperationSipuadaPlugin();
            final String username = userCredentials.getUsername();
            final String primaryHost = userCredentials.getPrimaryHost();
            String password = userCredentials.getPassword();
            String[] localAddresses = getLocalAddresses();
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
//            sipuada.registerPlugin(sipuadaPluginForAudio);
            sipuada.registerPlugin(noopSipuadaPlugin);
            sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
        }
    }

    private void doFetchCurrentUsersCredentials(SipuadaPresenter.FetchUsersCredentialsCallback callback) {
        List<SipuadaUserCredentials> usersCredentials = new Select()
                .from(SipuadaUserCredentials.class).execute();
        callback.onSuccess(usersCredentials);
    }

    private final Map<String, Timer> callInvitationDispatchers = new HashMap<>();

    private void doCreateSipuada(SipuadaUserCredentials userCredentials) {
        String[] localAddresses = getLocalAddresses();
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        String password = userCredentials.getPassword();
//        AndroidAudioSipuadaPlugin sipuadaPluginForAudio =
//                new AndroidAudioSipuadaPlugin(username, localAddresses[0], getApplicationContext());
        NoOperationSipuadaPlugin noopSipuadaPlugin = new NoOperationSipuadaPlugin();
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
//        sipuada.registerPlugin(sipuadaPluginForAudio);
        sipuada.registerPlugin(noopSipuadaPlugin);
        sipuadaInstances.put(getSipuadaKey(username, primaryHost), sipuada);
    }

    private boolean handleIncomingCallInvitation(final String callId, final String username,
                final String primaryHost, final String remoteUsername, final String remoteHost) {
        if (!SipuadaApplication.CURRENTLY_BUSY_FROM_DB) {
            Timer timer = new Timer();
            callInvitationDispatchers.put(callId, timer);
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    callInvitationDispatchers.remove(callId);
                    Intent intent = new Intent(getApplicationContext(),
                            IncomingCallInvitationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(SipuadaApplication.KEY_CALL_ID, callId);
                    intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                    intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                    intent.putExtra(SipuadaApplication.KEY_REMOTE_USERNAME, remoteUsername);
                    intent.putExtra(SipuadaApplication.KEY_REMOTE_HOST, remoteHost);
                    startActivity(intent);
                }

            }, 2000);
        } else {
            //TODO add a notification for this incoming call invitation while you're busy
        }
        return SipuadaApplication.CURRENTLY_BUSY_FROM_DB;
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

    private void doInviteUser(InviteUserOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.inviteToCall(operation.getRemoteUsername(), operation.getRemoteHost(),
                operation.getCallback());
    }

    public void doCancelInviteToUser(UserInviteOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.cancelCallInvitation(operation.getCallId());
    }

    public void doAcceptInviteFromUser(UserInviteOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.acceptCallInvitation(operation.getCallId());
    }

    public void doDeclineInviteFromUser(UserInviteOperation operation) {
        Sipuada sipuada = getSipuada(operation.getUsername(), operation.getPrimaryHost());
        sipuada.declineCallInvitation(operation.getCallId());
    }

    private Sipuada getSipuada(String username, String primaryHost) {
        return sipuadaInstances.get(getSipuadaKey(username, primaryHost));
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
            eventBus.post(new SipuadaPresenterApi.CallInvitationCanceled(reason, callId));
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
            eventBus.post(new SipuadaPresenterApi.CallInvitationFailed(reason, callId));
        }

        @Override
        public void onCallEstablished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallEstablished;" +
                    " callId:{%s}]", callId));
            eventBus.post(new SipuadaPresenterApi.EstablishedCallStarted(callId));
        }

        @Override
        public void onCallFinished(String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFinished;" +
                    " callId:{%s}]", callId));
            eventBus.post(new SipuadaPresenterApi.EstablishedCallFinished(callId));
        }

        @Override
        public void onCallFailure(String reason, String callId) {
            Log.d(SipuadaApplication.TAG, String.format("[onCallFailure;" +
                    " reason:{%s}, callId:{%s}]", reason, callId));
            eventBus.post(new SipuadaPresenterApi.EstablishedCallFailed(reason, callId));
        }

        @Override
        public void onInfoReceived(String callId, ContentTypeHeader contentType, String content) {}

        @Override
        public void onMessageReceived(String callId, ContentTypeHeader contentType, String content) {}

    }

}

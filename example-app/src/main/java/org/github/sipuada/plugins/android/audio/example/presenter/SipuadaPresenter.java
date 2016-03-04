package org.github.sipuada.plugins.android.audio.example.presenter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.common.eventbus.Subscribe;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SipuadaPresenter extends MvpBasePresenter<SipuadaViewApi> implements SipuadaPresenterApi {

    private SipuadaService mSipuadaService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface FetchUsersCredentialsCallback {

        void onSuccess(List<SipuadaUserCredentials> usersCredentials);

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            mSipuadaService.registerSipuadaPresenter(SipuadaPresenter.this);
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceConnected();
                fetchCurrentUsersCredentialsThenRefresh();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mSipuadaService = null;
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceDisconnected();
            }
        }

    };

    @Override
    public boolean sipuadaServiceIsConnected() {
        return mSipuadaService != null;
    }

    @Override
    public void bindToSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doBindToSipuadaService(mConnection);
        }
    }

    @Override
    public void unbindFromSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doUnbindFromSipuadaService(mConnection);
        }
    }

    @Override
    public void createSipuada(String username, String primaryHost, String password) {
        mSipuadaService.createSipuada(new SipuadaUserCredentials(username, primaryHost, password));
        fetchCurrentUsersCredentialsThenRefresh();
    }

    @Override
    public void registerAddresses(String username, String primaryHost, final RegistrationCallback callback) {
        mSipuadaService.registerAddresses(username, primaryHost, new SipuadaApi.RegistrationCallback() {

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

    private final Map<String, OutgoingCallInvitationCallback> pendingOutgoingCallInvitations =
            Collections.synchronizedMap(new HashMap<String, OutgoingCallInvitationCallback>());

    @Override
    public void inviteUser(String username, String primaryHost, String remoteUser,
                           final OutgoingCallInvitationCallback callback) {
        mSipuadaService.inviteUser(username, primaryHost, remoteUser,
                new SipuadaApi.CallInvitationCallback() {

            @Override
            public void onWaitingForCallInvitationAnswer(final String callId) {
                Log.d(SipuadaApplication.TAG, String
                        .format("[onWaitingForCallInvitationAnswer; callId:{%s}]", callId));
                pendingOutgoingCallInvitations.put(callId, callback);
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onWaiting(callId);
                    }

                });
            }

            @Override
            public void onCallInvitationRinging(final String callId) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onCallInvitationRinging; callId:{%s}]", callId));
                pendingOutgoingCallInvitations.put(callId, callback);
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onRinging(callId);
                    }

                });
            }

            @Override
            public void onCallInvitationDeclined(final String callId) {
                Log.d(SipuadaApplication.TAG,
                        String.format("[onCallInvitationDeclined; callId:{%s}]", callId));
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onDeclined();
                    }

                });
            }

        });
    }

    @Override
    public void cancelInviteToUser(String username, String primaryHost, String callId) {
        mSipuadaService.cancelInviteToUser(username, primaryHost, callId);
    }

    private final Map<String, IncomingCallInvitationCallback> pendingIncomingCallInvitations =
            Collections.synchronizedMap(new HashMap<String, IncomingCallInvitationCallback>());

    @Override
    public void willAnswerInviteFromUser(String callId, IncomingCallInvitationCallback callback) {
        pendingIncomingCallInvitations.put(callId, callback);
    }

    @Override
    public void acceptInviteFromUser(String username, String primaryHost, String callId) {
        mSipuadaService.acceptInviteFromUser(username, primaryHost, callId);
    }

    @Override
    public void declineInviteFromUser(String username, String primaryHost, String callId) {
        mSipuadaService.declineInviteFromUser(username, primaryHost, callId);
    }

    @Override
    @Subscribe
    public void onCallInvitationCanceled(final CallInvitationCanceled event) {
        final OutgoingCallInvitationCallback outgoingCallback = pendingOutgoingCallInvitations
                .remove(event.getCallId());
        if (outgoingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    outgoingCallback.onCanceled(event.getReason());
                }

            });
        }
        final IncomingCallInvitationCallback incomingCallback = pendingIncomingCallInvitations
                .remove(event.getCallId());
        if (incomingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    incomingCallback.onCanceled(event.getReason());
                }

            });
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationFailed(final CallInvitationFailed event) {
        final OutgoingCallInvitationCallback outgoingCallback = pendingOutgoingCallInvitations
                .remove(event.getCallId());
        if (outgoingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    outgoingCallback.onFailed(event.getReason());
                }

            });
        }
        final IncomingCallInvitationCallback incomingCallback = pendingIncomingCallInvitations
                .remove(event.getCallId());
        if (incomingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    incomingCallback.onFailed(event.getReason());
                }

            });
        }
    }

    @Override
    @Subscribe
    public void onCallEstablished(final EstablishedCallStarted event) {
        final OutgoingCallInvitationCallback callback = pendingOutgoingCallInvitations
                .remove(event.getCallId());
        if (callback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    callback.onAccepted(event.getCallId());
                }

            });
        }
    }

    @Override
    @Subscribe
    public void onCallFinished(EstablishedCallFinished event) {

    }

    @Override
    @Subscribe
    public void onCallFailure(EstablishedCallFailed event) {

    }

    private void fetchCurrentUsersCredentialsThenRefresh() {
        mSipuadaService.fetchCurrentUsersCredentials(new FetchUsersCredentialsCallback() {

            @Override
            public void onSuccess(final List<SipuadaUserCredentials> usersCredentials) {
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            //noinspection ConstantConditions
                            getView().refreshUsersCredentialsList(usersCredentials);
                        }
                    }

                });
            }

        });
    }

}

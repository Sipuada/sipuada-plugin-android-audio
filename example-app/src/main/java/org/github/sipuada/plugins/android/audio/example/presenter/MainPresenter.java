package org.github.sipuada.plugins.android.audio.example.presenter;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.MainViewApi;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPresenter extends SipuadaPresenter<MainViewApi>
        implements MainPresenterApi<MainViewApi> {

    @Override
    protected void doUponServiceConnected() {
        fetchCurrentUsersCredentialsThenRefresh();
    }

    @Override
    protected void doUponServiceDisconnected() {}

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

    //TODO TODO TODO TODO remove the stuff below as it will belong to the CallPresenterApi only

    private final Map<String, MainPresenterApi.OutgoingCallInvitationCallback> pendingOutgoingCallInvitations =
            Collections.synchronizedMap(new HashMap<String, MainPresenterApi.OutgoingCallInvitationCallback>());

    @Override
    public void inviteUser(String username, String primaryHost, String remoteUser,
                           final MainPresenterApi.OutgoingCallInvitationCallback callback) {
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

    @Override
    @Subscribe
    public void onCallInvitationCanceled(final MainPresenterApi.CallInvitationCanceled event) {
        final MainPresenterApi.OutgoingCallInvitationCallback outgoingCallback = pendingOutgoingCallInvitations
                .remove(event.getCallId());
        if (outgoingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    outgoingCallback.onCanceled(event.getReason());
                }

            });
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationFailed(final MainPresenterApi.CallInvitationFailed event) {
        final MainPresenterApi.OutgoingCallInvitationCallback outgoingCallback = pendingOutgoingCallInvitations
                .remove(event.getCallId());
        if (outgoingCallback != null) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    outgoingCallback.onFailed(event.getReason());
                }

            });
        }
    }

    @Override
    @Subscribe
    public void onCallEstablished(final MainPresenterApi.EstablishedCallStarted event) {
        final MainPresenterApi.OutgoingCallInvitationCallback callback = pendingOutgoingCallInvitations
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

}

package org.github.sipuada.plugins.android.audio.example.presenter;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;
import org.github.sipuada.plugins.android.audio.example.view.CallViewApi;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CallPresenter extends SipuadaPresenter<CallViewApi> implements CallPresenterApi {

    @Override
    public void performAction(CallActivity.CallAction callAction, SipuadaCallData sipuadaCallData) {
        switch (callAction) {
            case DO_NOTHING:
                break;
            case RECEIVE_CALL:
//                receiveCall(sipuadaCallData);
                break;
            case FINISH_CALL:
//                finishCall(sipuadaCallData);
                break;
            case MAKE_CALL:
//                makeCall(sipuadaCallData);
            default:
                break;

        }
    }

    @Override
    protected void doUponServiceConnected() {}

    @Override
    protected void doUponServiceDisconnected() {}

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

    @Override
    public void makeCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCall(sipuadaCallData);
        }
    }

    @Override
    public void callAccepted(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCallAccepted(sipuadaCallData);
        }
    }

    @Override
    public void callDeclined(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showMakingCallDeclined(sipuadaCallData);
        }
    }

    @Override
    public void receiveCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCall(sipuadaCallData);
        }
    }

    @Override
    public void acceptCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallAccept(sipuadaCallData);
        }
    }

    @Override
    public void declineCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showReceivingCallDecline(sipuadaCallData);
        }
    }

    @Override
    public void establishCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showCallInProgress(sipuadaCallData);
        }
    }

    @Override
    public void finishCall(SipuadaCallData sipuadaCallData) {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showCallFinished(sipuadaCallData);
        }
    }

}

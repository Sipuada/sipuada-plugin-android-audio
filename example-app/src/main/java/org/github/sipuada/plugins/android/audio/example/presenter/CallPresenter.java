package org.github.sipuada.plugins.android.audio.example.presenter;

import com.google.common.eventbus.Subscribe;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.view.CallViewApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CallPresenter extends SipuadaPresenter<CallViewApi> implements CallPresenterApi {

    private static final String REASON_SEND_FAILED = "Operation could not be sent.";

    private final Map<String, SipuadaCallData> outgoingCalls = Collections
            .synchronizedMap(new HashMap<String, SipuadaCallData>());
    private final Map<String, SipuadaCallData> incomingCalls = Collections
            .synchronizedMap(new HashMap<String, SipuadaCallData>());
    private final Map<String, SipuadaCallData> establishedCalls = Collections
            .synchronizedMap(new HashMap<String, SipuadaCallData>());

    public enum CallAction {
        MAKE_CALL, RECEIVE_CALL
    }

    protected class ScheduledSipuadaCallAction {

        private CallAction scheduledCallAction;
        private SipuadaCallData scheduledCallActionData;

        public ScheduledSipuadaCallAction(CallAction callAction, SipuadaCallData callData) {
            this.scheduledCallAction = callAction;
            this.scheduledCallActionData = callData;
        }

        public CallAction getAction() {
            return scheduledCallAction;
        }

        public SipuadaCallData getActionData() {
            return scheduledCallActionData;
        }

    }
    private List<ScheduledSipuadaCallAction> scheduledCallActions = new LinkedList<>();

    @Override
    protected void doUponServiceConnected() {
        Iterator<ScheduledSipuadaCallAction> iterator = scheduledCallActions.iterator();
        while (iterator.hasNext()) {
            ScheduledSipuadaCallAction scheduledSipuadaCallAction = iterator.next();
            CallAction callAction = scheduledSipuadaCallAction.getAction();
            SipuadaCallData sipuadaCallData = scheduledSipuadaCallAction.getActionData();
            doPerformCallAction(callAction, sipuadaCallData);
            iterator.remove();
        }
    }

    @Override
    protected void doUponServiceDisconnected() {}

    @Override
    public void performAction(CallAction callAction, SipuadaCallData sipuadaCallData) {
        if (sipuadaServiceIsConnected()) {
            doPerformCallAction(callAction, sipuadaCallData);
        } else {
            scheduledCallActions.add(new ScheduledSipuadaCallAction(callAction, sipuadaCallData));
        }
    }

    private void doPerformCallAction(CallAction callAction, SipuadaCallData sipuadaCallData) {
        switch (callAction) {
            case MAKE_CALL:
                makeCall(sipuadaCallData);
                break;
            default:
            case RECEIVE_CALL:
                receiveCall(sipuadaCallData);
                break;
        }
    }

    @Override
    public void makeCall(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCall(sipuadaCallData);
                }
            }

        });
        sipuadaService.inviteUser(sipuadaCallData, new SipuadaApi.CallInvitationCallback() {

                @Override
                public void onWaitingForCallInvitationAnswer(String callId) {
                    callCancelable(sipuadaCallData);
                }

                @Override
                public void onCallInvitationRinging(String callId) {
                    callRinging(sipuadaCallData);
                }

                @Override
                public void onCallInvitationDeclined(String callId) {
                    callDeclined(sipuadaCallData);
                }

            }
        );
    }

    @Override
    public void callCancelable(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCallCancelable(sipuadaCallData);
                }
            }

        });
    }

    @Override
    public void cancelCall(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showCancelingCall(sipuadaCallData);
                }
            }

        });
        sipuadaService.cancelInviteToUser(sipuadaCallData.getUsername(),
                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
    }

    @Override
    public void outgoingCallCanceled(final SipuadaCallData sipuadaCallData, final String reason) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCallCanceled(sipuadaCallData, reason);
                }
            }

        });
    }

    @Override
    public void outgoingCallFailed(final SipuadaCallData sipuadaCallData, final String reason) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCallFailed(sipuadaCallData, reason);
                }
            }

        });
    }

    @Override
    public void callRinging(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCallRinging(sipuadaCallData);
                }
            }

        });
    }

    @Override
    public void callDeclined(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showMakingCallDeclined(sipuadaCallData);
                }
            }

        });
    }

    @Override
    public void receiveCall(final SipuadaCallData sipuadaCallData) {
        incomingCalls.put(sipuadaCallData.getCallId(), sipuadaCallData);
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showReceivingCall(sipuadaCallData);
                }
            }

        });
    }

    @Override
    public void incomingCallCanceled(final SipuadaCallData sipuadaCallData, final String reason) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showReceivingCallCanceled(sipuadaCallData, reason);
                }
            }

        });
    }

    @Override
    public void incomingCallFailed(final SipuadaCallData sipuadaCallData, final String reason) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showReceivingCallFailed(sipuadaCallData, reason);
                }
            }

        });
    }

    @Override
    public void acceptCall(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showReceivingCallAccept(sipuadaCallData);
                }
            }

        });
        sipuadaService.acceptInviteFromUser(sipuadaCallData.getUsername(),
                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
    }

    @Override
    public void declineCall(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showReceivingCallDecline(sipuadaCallData);
                }
            }

        });
        sipuadaService.declineInviteFromUser(sipuadaCallData.getUsername(),
                sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
    }

    @Override
    public void establishCall(final SipuadaCallData sipuadaCallData) {
        establishedCalls.put(sipuadaCallData.getCallId(), sipuadaCallData);
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showCallInProgress(sipuadaCallData);
                }
            }

        });
    }

    @Override
    public void finishCall(SipuadaCallData sipuadaCallData) {
        finishCall(sipuadaCallData, true);
    }

    private void finishCall(final SipuadaCallData sipuadaCallData, boolean doFinishCall) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().showCallFinished(sipuadaCallData);
                }
            }

        });
        if (doFinishCall) {
            sipuadaService.finishCall(sipuadaCallData.getUsername(),
                    sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
        }
    }

    @Override
    public void closeCall(final SipuadaCallData sipuadaCallData) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isViewAttached()) {
                    //noinspection ConstantConditions
                    getView().dismissCall(sipuadaCallData);
                }
            }

        });
    }

    @Override
    @Subscribe
    public void onCallInvitationSent(final CallInvitationSent event) {
        outgoingCalls.put(event.getCallData().getCallId(), event.getCallData());
    }

    @Override
    @Subscribe
    public void onCallInvitationCouldNotBeSent(final CallInvitationCouldNotBeSent event) {
        outgoingCallFailed(event.getCallData(), REASON_SEND_FAILED);
    }

    @Override
    @Subscribe
    public void onCallInvitationCanceled(final CallInvitationCanceled event) {
        SipuadaCallData incomingSipuadaCallData = incomingCalls.get(event.getCallId());
        if (incomingSipuadaCallData != null) {
            incomingCallCanceled(incomingSipuadaCallData, event.getReason());
        }
        final SipuadaCallData outgoingSipuadaCallData = outgoingCalls.get(event.getCallId());
        if (outgoingSipuadaCallData != null) {
            outgoingCallCanceled(outgoingSipuadaCallData, event.getReason());
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationCancelCouldNotBeSent(final CallInvitationCancelCouldNotBeSent event) {
        final SipuadaCallData outgoingSipuadaCallData = outgoingCalls.get(event.getCallId());
        if (outgoingSipuadaCallData != null) {
            outgoingCallFailed(outgoingSipuadaCallData, REASON_SEND_FAILED);
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationFailed(final CallInvitationFailed event) {
        SipuadaCallData incomingSipuadaCallData = incomingCalls.get(event.getCallId());
        if (incomingSipuadaCallData != null) {
            incomingCallFailed(incomingSipuadaCallData, event.getReason());
        }
        final SipuadaCallData outgoingSipuadaCallData = outgoingCalls.get(event.getCallId());
        if (outgoingSipuadaCallData != null) {
            outgoingCallFailed(outgoingSipuadaCallData, event.getReason());
        }
    }

    @Override
    @Subscribe
    public void onCallInvitationAnswerCouldNotBeSent(final CallInvitationAnswerCouldNotBeSent event) {
        final SipuadaCallData incomingSipuadaCallData = incomingCalls.get(event.getCallId());
        if (incomingSipuadaCallData != null) {
            incomingCallFailed(incomingSipuadaCallData, REASON_SEND_FAILED);
        }
    }

    @Override
    @Subscribe
    public void onCallEstablished(final EstablishedCallStarted event) {
        SipuadaCallData incomingSipuadaCallData = incomingCalls.get(event.getCallId());
        if (incomingSipuadaCallData != null) {
            establishCall(incomingSipuadaCallData);
        }
        final SipuadaCallData outgoingSipuadaCallData = outgoingCalls.get(event.getCallId());
        if (outgoingSipuadaCallData != null) {
            establishCall(outgoingSipuadaCallData);
        }
    }

    @Override
    @Subscribe
    public void onCallFinished(EstablishedCallFinished event) {
        SipuadaCallData sipuadaCallData = establishedCalls.get(event.getCallId());
        if (sipuadaCallData != null) {
            finishCall(sipuadaCallData, false);
        }
    }

    @Override
    @Subscribe
    public void onEstablishedCallFinishCouldNotBeSent(EstablishedCallFinishCouldNotBeSent event) {
        SipuadaCallData sipuadaCallData = establishedCalls.get(event.getCallId());
        if (sipuadaCallData != null) {
            finishCall(sipuadaCallData, false);
        }
    }

    @Override
    @Subscribe
    public void onCallFailure(EstablishedCallFailed event) {
        SipuadaCallData sipuadaCallData = establishedCalls.get(event.getCallId());
        if (sipuadaCallData != null) {
            finishCall(sipuadaCallData, false);
        }
    }

}

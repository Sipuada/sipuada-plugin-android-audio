package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.viewstate.RestoreableViewState;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.renderers.CallRendererBuilder;

import java.util.Arrays;

import butterknife.Bind;

public class CallActivity extends SipuadaViewStateActivity<CallViewApi, CallPresenterApi>
        implements CallViewApi {

    @Bind(R.id.sipuplug_andrdio_example_IncomingCallsSummary) TextView callsSummary;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;

    private RVRendererAdapter<CallViewState.SipuadaCall> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        callsSummary.setEnabled(false);
        adapter = new RVRendererAdapter<>(getLayoutInflater(), new CallRendererBuilder(getPresenter()),
                new ListAdapteeCollection<>(Arrays.asList(new CallViewState.SipuadaCall[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setEnabled(false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleCallActionIntent(intent);
    }

    @Override
    public void onNewViewStateInstance() {
        Intent intent = getIntent();
        handleCallActionIntent(intent);
    }

    @Override
    protected void onSipuadaServiceConnected() {
        callsSummary.setEnabled(true);
        recyclerView.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public RestoreableViewState createViewState() {
        return new CallViewState();
    }

    @Override
    protected void onSipuadaServiceDisconnected() {
        callsSummary.setEnabled(false);
        recyclerView.setEnabled(false);
        adapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CallPresenter createPresenter() {
        return new CallPresenter();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                || super.onKeyDown(keyCode, event);
    }

    private void handleCallActionIntent(Intent intent) {
        CallPresenter.CallAction sipuadaCallAction = (CallPresenter.CallAction) intent
                .getSerializableExtra(SipuadaApplication.KEY_CALL_ACTION);
        String callId = intent.getStringExtra(SipuadaApplication.KEY_CALL_ID);
        String username = intent.getStringExtra(SipuadaApplication.KEY_USERNAME);
        String primaryHost = intent.getStringExtra(SipuadaApplication.KEY_PRIMARY_HOST);
        String remoteUsername = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_USERNAME);
        String remoteHost = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_HOST);
        final SipuadaCallData sipuadaCallData =
                new SipuadaCallData(callId, username, primaryHost, remoteUsername, remoteHost);
        addSipuadaCall(sipuadaCallAction, sipuadaCallData);
        presenter.performAction(sipuadaCallAction, sipuadaCallData);
    }

    @Override
    public void showMakingCall(SipuadaCallData sipuadaCallData) {
        addSipuadaCall(CallPresenter.CallAction.MAKE_CALL, sipuadaCallData);
    }

    @Override
    public void showMakingCallCancelable(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCELABLE, sipuadaCallData);
    }

    @Override
    public void showCancelingCall(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCEL, sipuadaCallData);
    }

    @Override
    public void showMakingCallCanceled(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCELED, sipuadaCallData);
    }

    @Override
    public void showMakingCallFailed(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_FAILED, sipuadaCallData);
    }

    @Override
    public void showMakingCallRinging(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_RINGING, sipuadaCallData);
    }

    @Override
    public void showMakingCallAccepted(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_ACCEPTED, sipuadaCallData);
    }

    @Override
    public void showMakingCallDeclined(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_DECLINED, sipuadaCallData);
    }

    @Override
    public void showReceivingCall(SipuadaCallData sipuadaCallData) {
        addSipuadaCall(CallPresenter.CallAction.RECEIVE_CALL, sipuadaCallData);
    }

    @Override
    public void showReceivingCallCanceled(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_CANCELED, sipuadaCallData);
    }

    @Override
    public void showReceivingCallFailed(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_FAILED, sipuadaCallData);
    }

    @Override
    public void showReceivingCallAccept(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_ACCEPT, sipuadaCallData);
    }

    @Override
    public void showReceivingCallDecline(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_DECLINE, sipuadaCallData);
    }

    @Override
    public void showCallInProgress(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_IN_PROGRESS, sipuadaCallData);
    }

    @Override
    public void showCallFinished(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_FINISHED, sipuadaCallData);
    }

    @Override
    public void dismissCall(SipuadaCallData sipuadaCallData) {
        closeSipuadaCall(sipuadaCallData);
    }

    private void addSipuadaCall(CallPresenter.CallAction sipuadaCallAction,
                                SipuadaCallData sipuadaCallData) {
        CallViewState callsViewState = (CallViewState) getViewState();
        CallViewState.SipuadaCallState sipuadaCallState;
        switch (sipuadaCallAction) {
            case MAKE_CALL:
                sipuadaCallState = CallViewState.SipuadaCallState.CALL_MAKING;
                break;
            default:
            case RECEIVE_CALL:
                sipuadaCallState = CallViewState.SipuadaCallState.CALL_RECEIVING;
                break;
        }
        callsViewState.addOrModifySipuadaCall(sipuadaCallState, sipuadaCallData);
        refreshCallDataList(callsViewState);
    }

    private void setSipuadaCall(CallViewState.SipuadaCallState sipuadaCallState,
                                SipuadaCallData sipuadaCallData) {
        CallViewState callsViewState = (CallViewState) getViewState();
        callsViewState.addOrModifySipuadaCall(sipuadaCallState, sipuadaCallData);
        refreshCallDataList(callsViewState);
    }

    private void closeSipuadaCall(SipuadaCallData sipuadaCallData) {
        CallViewState callsViewState = (CallViewState) getViewState();
        callsViewState.removeSipuadaCall(sipuadaCallData);
        refreshCallDataList(callsViewState);
    }

    private void refreshCallDataList(CallViewState callsViewState) {
        adapter.clear();
        int pendingOutgoingCallsNumber = 0, pendingIncomingCallsNumber = 0,
                establishedCallsNumber = 0, finishedCallsNumber = 0;
        for (int i = 0; i < callsViewState.getSipuadaCallsCount(); i++) {
            CallViewState.SipuadaCall sipuadaCall = callsViewState.getSipuadaCall(i);
            if (sipuadaCall.getCallState() == CallViewState.SipuadaCallState.CALL_FINISHED) {
                finishedCallsNumber++;
            }
            else {
                switch (sipuadaCall.getCallState()) {
                    case CALL_MAKING:
                    case CALL_MAKING_RINGING:
                    case CALL_MAKING_ACCEPTED:
                    case CALL_MAKING_DECLINED:
                        pendingOutgoingCallsNumber++;
                        break;
                    case CALL_RECEIVING:
                    case CALL_RECEIVING_ACCEPT:
                    case CALL_RECEIVING_DECLINE:
                        pendingIncomingCallsNumber++;
                        break;
                    case CALL_IN_PROGRESS:
                        establishedCallsNumber++;
                        break;
                    case CALL_MAKING_CANCEL:
                    case CALL_MAKING_CANCELED:
                    case CALL_MAKING_FAILED:
                    case CALL_RECEIVING_CANCELED:
                    case CALL_RECEIVING_FAILED:
                    case CALL_FINISHED:
                        finishedCallsNumber++;
                        break;
                }
                pendingIncomingCallsNumber++;
            }
            adapter.add(sipuadaCall);
        }
        StringBuilder summary = new StringBuilder();
        if (establishedCallsNumber == 1) {
            summary.append(String.format("%d established call", establishedCallsNumber));
        } else if (establishedCallsNumber > 1) {
            summary.append(String.format("%d established calls", establishedCallsNumber));
        }
        if (pendingIncomingCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d incoming call invite", pendingIncomingCallsNumber));
        } else if (pendingIncomingCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d incoming call invites", pendingIncomingCallsNumber));
        }
        if (pendingOutgoingCallsNumber == 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d outgoing call invite", pendingOutgoingCallsNumber));
        } else if (pendingOutgoingCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d outgoing call invites", pendingOutgoingCallsNumber));
        }
        if (finishedCallsNumber == 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d call finished", finishedCallsNumber));
        } else if (finishedCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d calls finished", finishedCallsNumber));
        }
        summary.append("...");
        if (pendingIncomingCallsNumber == 0 && finishedCallsNumber == 0) {
            summary.append("Finishing...");
            finish();
        }
        callsSummary.setText(summary);
        adapter.notifyDataSetChanged();
    }

}
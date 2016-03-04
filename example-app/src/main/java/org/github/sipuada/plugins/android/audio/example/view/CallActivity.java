package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.TextView;

import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.renderers.CallInvitationEntriesRendererBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;

public class CallActivity extends SipuadaActivity<CallPresenterApi> {

    @Bind(R.id.sipuplug_andrdio_example_IncomingCallsSummary) TextView incomingCallsSummary;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;

    private RVRendererAdapter<SipuadaCallData> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_invitation);
        incomingCallsSummary.setEnabled(false);
        adapter = new RVRendererAdapter<>(getLayoutInflater(),
                new CallInvitationEntriesRendererBuilder(getPresenter(), this),
                new ListAdapteeCollection<>(Arrays.asList(new SipuadaCallData[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setEnabled(false);
        handleIncomingCallInvitation(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIncomingCallInvitation(intent);
    }

    private void handleIncomingCallInvitation(Intent intent) {
        String callId = intent.getStringExtra(SipuadaApplication.KEY_CALL_ID);
        String username = intent.getStringExtra(SipuadaApplication.KEY_USERNAME);
        String primaryHost = intent.getStringExtra(SipuadaApplication.KEY_PRIMARY_HOST);
        String remoteUsername = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_USERNAME);
        String remoteHost = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_HOST);
        final SipuadaCallData sipuadaCallData =
                new SipuadaCallData(callId, username, primaryHost, remoteUsername, remoteHost);
        getPresenter().willAnswerInviteFromUser(callId, new CallPresenterApi
                .IncomingCallInvitationCallback() {

            @Override
            public void onFailed(String reason) {
//                sipuadaCallData.setFinished(reason);
                refreshIncomingCalls();
            }

            @Override
            public void onCanceled(String reason) {
//                sipuadaCallData.setFinished(reason);
                refreshIncomingCalls();
            }

        });
        List<SipuadaCallData> sipuadaCallDatas = new LinkedList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            sipuadaCallDatas.add(adapter.getItem(i));
        }
        adapter.clear();
        adapter.add(sipuadaCallData);
        adapter.addAll(sipuadaCallDatas);
        refreshIncomingCalls();
    }

    public void closeFinishedCallInvitation(SipuadaCallData sipuadaCallData) {
        adapter.remove(sipuadaCallData);
        refreshIncomingCalls();
    }

    private void refreshIncomingCalls() {
        int pendingIncomingCallsNumber = 0/*, finishedIncomingCallsNumber = 0*/;
        for (int i = 0; i < adapter.getItemCount(); i++) {
//            if (adapter.getItem(i).isFinished()) {
//                finishedIncomingCallsNumber++;
//            }
//            else {
                pendingIncomingCallsNumber++;
//            }
        }
        String summary = "Incoming calls...";
        if (pendingIncomingCallsNumber == 1) {
            summary = String.format("%d incoming call invite...", pendingIncomingCallsNumber);
        } else if (pendingIncomingCallsNumber > 1) {
            summary = String.format("%d incoming call invites...", pendingIncomingCallsNumber);
//        } else if (finishedIncomingCallsNumber > 1) {
//            summary = String.format("%d incoming call invites finished.",
//                    finishedIncomingCallsNumber);
//        } else if (finishedIncomingCallsNumber == 1) {
//            summary = String.format("%d incoming call invite finished.",
//                    finishedIncomingCallsNumber);
        } else if (pendingIncomingCallsNumber == 0/* && finishedIncomingCallsNumber == 0*/) {
            finish();
        }
        incomingCallsSummary.setText(summary);
        adapter.notifyDataSetChanged();
    }

    public void declineRemainingCallInvitations(SipuadaCallData sipuadaCallData) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            SipuadaCallData otherCallInvitation = adapter.getItem(i);
            if (otherCallInvitation.getCallId().equals(sipuadaCallData.getCallId())) {
                continue;
            }
            getPresenter().declineInviteFromUser(sipuadaCallData.getUsername(),
                    sipuadaCallData.getPrimaryHost(), sipuadaCallData.getCallId());
            finish();
        }
    }

    @Override
    protected void onSipuadaServiceConnected() {
        incomingCallsSummary.setEnabled(true);
        recyclerView.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSipuadaServiceDisconnected() {
        incomingCallsSummary.setEnabled(false);
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

}

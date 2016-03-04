package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.TextView;

import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.renderers.CallInvitationEntriesRendererBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;

public class IncomingCallInvitationActivity extends SipuadaActivity {

    @Bind(R.id.sipuplug_andrdio_example_IncomingCallsSummary) TextView incomingCallsSummary;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;

    private RVRendererAdapter<IncomingCallInvitation> adapter;

    public class IncomingCallInvitation {

        private final String callId;
        private final String username;
        private final String primaryHost;
        private final String remoteUsername;
        private final String remoteHost;
        private boolean finished = false;
        private String reason = null;

        public IncomingCallInvitation(String callId, String username, String primaryHost,
                                      String remoteUsername, String remoteHost) {
            this.callId = callId;
            this.username = username;
            this.primaryHost = primaryHost;
            this.remoteUsername = remoteUsername;
            this.remoteHost = remoteHost;
        }

        public String getCallId() {
            return callId;
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

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(String reason) {
            this.finished = true;
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public int hashCode() {
            return callId.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof IncomingCallInvitation) {
                IncomingCallInvitation that = (IncomingCallInvitation) other;
                return this.getCallId().equals(that.getCallId());
            }
            return false;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_invitation);
        incomingCallsSummary.setEnabled(false);
        adapter = new RVRendererAdapter<>(getLayoutInflater(),
                new CallInvitationEntriesRendererBuilder(getPresenter(), this),
                new ListAdapteeCollection<>(Arrays.asList(new IncomingCallInvitation[]{})));
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
        final IncomingCallInvitation incomingCallInvitation =
                new IncomingCallInvitation(callId, username, primaryHost, remoteUsername, remoteHost);
        getPresenter().willAnswerInviteFromUser(callId, new SipuadaPresenterApi
                .IncomingCallInvitationCallback() {

            @Override
            public void onFailed(String reason) {
                incomingCallInvitation.setFinished(reason);
                refreshIncomingCalls();
            }

            @Override
            public void onCanceled(String reason) {
                incomingCallInvitation.setFinished(reason);
                refreshIncomingCalls();
            }

        });
        List<IncomingCallInvitation> incomingCallInvitations = new LinkedList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            incomingCallInvitations.add(adapter.getItem(i));
        }
        adapter.clear();
        adapter.add(incomingCallInvitation);
        adapter.addAll(incomingCallInvitations);
        refreshIncomingCalls();
    }

    public void closeFinishedCallInvitation(IncomingCallInvitation incomingCallInvitation) {
        adapter.remove(incomingCallInvitation);
        refreshIncomingCalls();
    }

    private void refreshIncomingCalls() {
        int pendingIncomingCallsNumber = 0, finishedIncomingCallsNumber = 0;
        for (int i=0; i<adapter.getItemCount(); i++) {
            if (adapter.getItem(i).isFinished()) {
                finishedIncomingCallsNumber++;
            }
            else {
                pendingIncomingCallsNumber++;
            }
        }
        String summary = "Incoming calls...";
        if (pendingIncomingCallsNumber == 1) {
            summary = String.format("%d incoming call invite...", pendingIncomingCallsNumber);
        } else if (pendingIncomingCallsNumber > 1) {
            summary = String.format("%d incoming call invites...", pendingIncomingCallsNumber);
        } else if (finishedIncomingCallsNumber > 1) {
            summary = String.format("%d incoming call invites finished.",
                    finishedIncomingCallsNumber);
        } else if (finishedIncomingCallsNumber == 1) {
            summary = String.format("%d incoming call invite finished.",
                    finishedIncomingCallsNumber);
        } else if (pendingIncomingCallsNumber == 0 && finishedIncomingCallsNumber == 0) {
            finish();
        }
        incomingCallsSummary.setText(summary);
        adapter.notifyDataSetChanged();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                || super.onKeyDown(keyCode, event);
    }

}

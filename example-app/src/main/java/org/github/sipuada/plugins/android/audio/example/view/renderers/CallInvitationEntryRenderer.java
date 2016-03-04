package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.IncomingCallInvitationActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallInvitationEntryRenderer
        extends Renderer<IncomingCallInvitationActivity.IncomingCallInvitation> {

    @Bind(R.id.sipuplug_andrdio_example_EntryRemoteUsernameAtAddress) TextView remoteUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_EntryLocalUsernameAtAddress) TextView localUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_AcceptButton) Button acceptButton;
    @Bind(R.id.sipuplug_andrdio_example_DeclineButton) TextView declineButton;
    @Bind(R.id.sipuplug_andrdio_example_CloseButton) TextView closeButton;
    @Bind(R.id.sipuplug_andrdio_example_IncomingCallStatus) TextView incomingCallStatus;

    private final SipuadaPresenterApi presenter;
    private final IncomingCallInvitationActivity activity;

    public CallInvitationEntryRenderer(SipuadaPresenterApi presenter,
                                       IncomingCallInvitationActivity activity) {
        this.presenter = presenter;
        this.activity = activity;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_incoming_call_invitation, parent, false);
        ButterKnife.bind(this, inflatedView);
        return inflatedView;
    }

    @Override
    public void render() {
        final IncomingCallInvitationActivity.IncomingCallInvitation incomingCallInvitation = getContent();
        String callId = incomingCallInvitation.getCallId();
        String username = incomingCallInvitation.getUsername();
        String primaryHost = incomingCallInvitation.getPrimaryHost();
        String remoteUsername = incomingCallInvitation.getRemoteUsername();
        String remoteHost = incomingCallInvitation.getRemoteHost();
        remoteUsernameAtHost.setText(String.format("%s@%s", remoteUsername, remoteHost));
        remoteUsernameAtHost.setSelected(true);
        localUsernameAtHost.setText(String.format("%s@%s", username, primaryHost));
        localUsernameAtHost.setSelected(true);
        acceptButton.getBackground().setAlpha(115);
        declineButton.getBackground().setAlpha(115);
        closeButton.setEnabled(false);
        closeButton.setOnClickListener(null);
        closeButton.setVisibility(View.GONE);
        if (!presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Binding to SipuadaService...";
            incomingCallStatus.setText(statusMessage);
            acceptButton.setEnabled(false);
            acceptButton.setVisibility(View.VISIBLE);
            acceptButton.setOnClickListener(null);
            declineButton.setEnabled(false);
            declineButton.setVisibility(View.VISIBLE);
            declineButton.setOnClickListener(null);
            return;
        }
        else if (incomingCallInvitation.isFinished()) {
            renderFinished(incomingCallInvitation);
            return;
        }
        String statusMessage = "Waiting for your decision...";
        incomingCallStatus.setText(statusMessage);
        incomingCallStatus.setSelected(true);
        renderAccept(callId, username, primaryHost);
        renderDecline(callId, username, primaryHost, incomingCallInvitation);
    }

    private void renderAccept(final String callId, final String username, final String primaryHost) {
        acceptButton.setEnabled(true);
        acceptButton.setVisibility(View.VISIBLE);
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String statusMessage = "Please wait...";
                incomingCallStatus.setText(statusMessage);
                incomingCallStatus.setSelected(true);
                presenter.acceptInviteFromUser(username, primaryHost, callId);
            }

        });
    }

    private void renderDecline(final String callId, final String username, final String primaryHost,
            final IncomingCallInvitationActivity.IncomingCallInvitation incomingCallInvitation) {
        declineButton.setEnabled(true);
        declineButton.setVisibility(View.VISIBLE);
        declineButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.declineInviteFromUser(username, primaryHost, callId);
                incomingCallInvitation.setFinished("Declined by you.");
                renderFinished(incomingCallInvitation);
            }

        });
    }

    private void renderFinished(final IncomingCallInvitationActivity
            .IncomingCallInvitation incomingCallInvitation) {
        StringBuilder statusMessage = new StringBuilder("Call invitation finished.");
        String reasonWhy = incomingCallInvitation.getReason();
        if (reasonWhy != null) {
            statusMessage.append(String.format(" Reason: %s", reasonWhy));
        }
        incomingCallStatus.setText(statusMessage);
        incomingCallStatus.setSelected(true);
        acceptButton.setEnabled(false);
        acceptButton.setVisibility(View.GONE);
        acceptButton.setOnClickListener(null);
        declineButton.setEnabled(false);
        declineButton.setVisibility(View.GONE);
        declineButton.setOnClickListener(null);
        closeButton.setEnabled(true);
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                activity.closeFinishedCallInvitation(incomingCallInvitation);
            }

        });
    }

}

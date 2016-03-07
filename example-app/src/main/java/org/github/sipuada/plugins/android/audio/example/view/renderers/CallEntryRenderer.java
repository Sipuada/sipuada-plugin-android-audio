package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallEntryRenderer extends Renderer<CallViewState.SipuadaCall> {

    @Bind(R.id.sipuplug_andrdio_example_EntryRemoteUsernameAtAddress) TextView remoteUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_EntryLocalUsernameAtAddress) TextView localUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_AcceptButton) Button acceptButton;
    @Bind(R.id.sipuplug_andrdio_example_DeclineButton) TextView declineButton;
    @Bind(R.id.sipuplug_andrdio_example_CloseButton) TextView closeButton;
    @Bind(R.id.sipuplug_andrdio_example_IncomingCallStatus) TextView incomingCallStatus;

    private final CallPresenterApi presenter;
    private final CallActivity activity;

    public CallEntryRenderer(CallPresenterApi presenter,
                             CallActivity activity) {
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
        CallViewState.SipuadaCall callInformation = getContent();
        final SipuadaCallData sipuadaCallData = callInformation.getCallData();
        String callId = sipuadaCallData.getCallId();
        String username = sipuadaCallData.getUsername();
        String primaryHost = sipuadaCallData.getPrimaryHost();
        String remoteUsername = sipuadaCallData.getRemoteUsername();
        String remoteHost = sipuadaCallData.getRemoteHost();
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
//        else if (sipuadaCallData.isFinished()) {
//            renderFinished(sipuadaCallData);
//            return;
//        }
        String statusMessage = "Waiting for your decision...";
        incomingCallStatus.setText(statusMessage);
        incomingCallStatus.setSelected(true);
        renderAccept(callId, username, primaryHost, sipuadaCallData);
        renderDecline(callId, username, primaryHost, sipuadaCallData);
    }

    private void renderAccept(final String callId, final String username, final String primaryHost,
            final SipuadaCallData sipuadaCallData) {
        acceptButton.setEnabled(true);
        acceptButton.setVisibility(View.VISIBLE);
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String statusMessage = "Please wait...";
                incomingCallStatus.setText(statusMessage);
                incomingCallStatus.setSelected(true);
                presenter.acceptCall(sipuadaCallData);
            }

        });
    }

    private void renderDecline(final String callId, final String username, final String primaryHost,
            final SipuadaCallData sipuadaCallData) {
        declineButton.setEnabled(true);
        declineButton.setVisibility(View.VISIBLE);
        declineButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.declineCall(sipuadaCallData);
                renderFinished(sipuadaCallData);
            }

        });
    }

    private void renderFinished(final SipuadaCallData sipuadaCallData) {
        StringBuilder statusMessage = new StringBuilder("Call invitation finished.");
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
                activity.closeCall(sipuadaCallData);
            }

        });
    }

}

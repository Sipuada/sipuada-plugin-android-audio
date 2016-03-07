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
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallReceivingRenderer extends Renderer<CallViewState.SipuadaCall> {

    @Bind(R.id.sipuplug_andrdio_example_EntryRemoteUsernameAtAddress) TextView remoteUser;
    @Bind(R.id.sipuplug_andrdio_example_EntryLocalUsernameAtAddress) TextView localUser;
    @Bind(R.id.sipuplug_andrdio_example_AcceptButton) Button acceptButton;
    @Bind(R.id.sipuplug_andrdio_example_DeclineButton) TextView declineButton;
    @Bind(R.id.sipuplug_andrdio_example_CallStatus) TextView callStatus;

    protected final CallPresenterApi presenter;

    public CallReceivingRenderer(CallPresenterApi presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_call_receiving, parent, false);
        ButterKnife.bind(this, inflatedView);
        return inflatedView;
    }

    @Override
    public void render() {
        CallViewState.SipuadaCall callInformation = getContent();
        final SipuadaCallData sipuadaCallData = callInformation.getCallData();
        String username = sipuadaCallData.getUsername();
        String primaryHost = sipuadaCallData.getPrimaryHost();
        String remoteUsername = sipuadaCallData.getRemoteUsername();
        String remoteHost = sipuadaCallData.getRemoteHost();
        remoteUser.setText(String.format("%s@%s", remoteUsername, remoteHost));
        remoteUser.setSelected(true);
        localUser.setText(String.format("%s@%s", username, primaryHost));
        localUser.setSelected(true);
        if (!presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Please wait...";
            callStatus.setText(statusMessage);
            acceptButton.setEnabled(false);
            acceptButton.setOnClickListener(null);
            declineButton.setEnabled(false);
            declineButton.setOnClickListener(null);
            return;
        }
        String statusMessage = "Waiting for your decision...";
        callStatus.setText(statusMessage);
        callStatus.setSelected(true);
        renderAccept(sipuadaCallData);
        renderDecline(sipuadaCallData);
    }

    private void renderAccept(final SipuadaCallData sipuadaCallData) {
        acceptButton.setEnabled(true);
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.acceptCall(sipuadaCallData);
            }

        });
    }

    private void renderDecline(final SipuadaCallData sipuadaCallData) {
        declineButton.setEnabled(true);
        declineButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.declineCall(sipuadaCallData);
            }

        });
    }

}

package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallFinishedRenderer extends Renderer<CallViewState.SipuadaCall> {

    @Bind(R.id.sipuplug_andrdio_example_EntryRemoteUsernameAtAddress) TextView remoteUser;
    @Bind(R.id.sipuplug_andrdio_example_CallRelationship) TextView callRelationship;
    @Bind(R.id.sipuplug_andrdio_example_EntryLocalUsernameAtAddress) TextView localUser;
    @Bind(R.id.sipuplug_andrdio_example_CloseButton) TextView closeButton;
    @Bind(R.id.sipuplug_andrdio_example_CallStatus) TextView callStatus;

    protected final CallPresenterApi presenter;

    public CallFinishedRenderer(CallPresenterApi presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_call_finished, parent, false);
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
        String callRelationshipMessage = "was connected to";
        callRelationship.setText(callRelationshipMessage);
        localUser.setText(String.format("%s@%s", username, primaryHost));
        localUser.setSelected(true);
        if (!presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Please wait...";
            callStatus.setText(statusMessage);
            closeButton.setEnabled(false);
            closeButton.setOnClickListener(null);
            return;
        }
        String statusMessage = "Call finished.";
        callStatus.setText(statusMessage);
        callStatus.setSelected(true);
        renderClose(sipuadaCallData);
    }

    private void renderClose(final SipuadaCallData sipuadaCallData) {
        closeButton.setEnabled(true);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.closeCall(sipuadaCallData);
            }

        });
    }

}

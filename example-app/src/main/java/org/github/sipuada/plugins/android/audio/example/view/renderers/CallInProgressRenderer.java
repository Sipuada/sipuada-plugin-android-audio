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

public class CallInProgressRenderer extends Renderer<CallViewState.SipuadaCall> {

    @Bind(R.id.sipuplug_andrdio_example_EntryRemoteUsernameAtAddress) TextView remoteUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_EntryLocalUsernameAtAddress) TextView localUsernameAtHost;
    @Bind(R.id.sipuplug_andrdio_example_FinishButton) TextView finishButton;
    @Bind(R.id.sipuplug_andrdio_example_CallStatus) TextView incomingCallStatus;

    private final CallPresenterApi presenter;

    public CallInProgressRenderer(CallPresenterApi presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_call_in_progress, parent, false);
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
        remoteUsernameAtHost.setText(String.format("%s@%s", remoteUsername, remoteHost));
        remoteUsernameAtHost.setSelected(true);
        localUsernameAtHost.setText(String.format("%s@%s", username, primaryHost));
        localUsernameAtHost.setSelected(true);
        if (!presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Please wait...";
            incomingCallStatus.setText(statusMessage);
            finishButton.setEnabled(false);
            finishButton.setOnClickListener(null);
            return;
        }
        String statusMessage = "Call in progress...";
        incomingCallStatus.setText(statusMessage);
        incomingCallStatus.setSelected(true);
        renderFinish(sipuadaCallData);
    }

    private void renderFinish(final SipuadaCallData sipuadaCallData) {
        finishButton.setEnabled(true);
        finishButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.finishCall(sipuadaCallData);
            }

        });
    }

}

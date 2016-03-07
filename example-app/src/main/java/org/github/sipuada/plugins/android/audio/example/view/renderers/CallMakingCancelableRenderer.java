package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.view.View;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

public class CallMakingCancelableRenderer extends CallMakingRenderer {

    public CallMakingCancelableRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        CallViewState.SipuadaCall callInformation = getContent();
        final SipuadaCallData sipuadaCallData = callInformation.getCallData();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Calling...";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
            renderCancel(sipuadaCallData);
        }
    }

    private void renderCancel(final SipuadaCallData sipuadaCallData) {
        cancelButton.setEnabled(true);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                presenter.cancelCall(sipuadaCallData);
            }

        });
    }

}

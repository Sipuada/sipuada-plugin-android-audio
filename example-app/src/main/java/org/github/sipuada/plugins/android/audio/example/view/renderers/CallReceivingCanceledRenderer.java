package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

public class CallReceivingCanceledRenderer extends CallFinishedRenderer {

    public CallReceivingCanceledRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        CallViewState.SipuadaCall callInformation = getContent();
        String callRelationshipMessage = "was calling";
        callRelationship.setText(callRelationshipMessage);
        if (presenter.sipuadaServiceIsConnected()) {
            callStatus.setText(String.format("Call was canceled: %s",
                    callInformation.getStateInformation()));
            callStatus.setSelected(true);
        }
    }

}

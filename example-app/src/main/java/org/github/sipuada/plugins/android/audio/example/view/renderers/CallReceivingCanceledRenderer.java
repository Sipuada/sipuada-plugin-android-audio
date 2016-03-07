package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallReceivingCanceledRenderer extends CallFinishedRenderer {

    public CallReceivingCanceledRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        String callRelationshipMessage = "was calling";
        callRelationship.setText(callRelationshipMessage);
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Call was canceled.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

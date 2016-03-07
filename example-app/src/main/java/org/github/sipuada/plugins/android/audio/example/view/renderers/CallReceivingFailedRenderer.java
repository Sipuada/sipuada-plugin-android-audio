package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallReceivingFailedRenderer extends CallFinishedRenderer {

    public CallReceivingFailedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        String callRelationshipMessage = "was calling";
        callRelationship.setText(callRelationshipMessage);
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Call failed.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

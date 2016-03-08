package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingDeclinedRenderer extends CallFinishedRenderer {

    public CallMakingDeclinedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        String tempUser = localUser.getText().toString();
        localUser.setText(remoteUser.getText());
        remoteUser.setText(tempUser);
        String callRelationshipMessage = "was calling";
        callRelationship.setText(callRelationshipMessage);
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Call was declined.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

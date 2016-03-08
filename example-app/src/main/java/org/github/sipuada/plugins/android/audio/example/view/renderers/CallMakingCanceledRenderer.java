package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

public class CallMakingCanceledRenderer extends CallFinishedRenderer {

    public CallMakingCanceledRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        CallViewState.SipuadaCall callInformation = getContent();
        String tempUser = localUser.getText().toString();
        localUser.setText(remoteUser.getText());
        remoteUser.setText(tempUser);
        String callRelationshipMessage = "was calling";
        callRelationship.setText(callRelationshipMessage);
        if (presenter.sipuadaServiceIsConnected()) {
            callStatus.setText(String.format("Call was canceled: %s",
                    callInformation.getStateInformation()));
            callStatus.setSelected(true);
        }
    }

}

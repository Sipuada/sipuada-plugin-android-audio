package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

public class CallFailedRenderer extends CallFinishedRenderer {

    public CallFailedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        CallViewState.SipuadaCall callInformation = getContent();
        if (presenter.sipuadaServiceIsConnected()) {
            callStatus.setText(String.format("Established call failed: %s",
                    callInformation.getStateInformation()));
            callStatus.setSelected(true);
        }
    }

}

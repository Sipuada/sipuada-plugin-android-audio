package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingAcceptedRenderer extends CallMakingRenderer {

    public CallMakingAcceptedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Accepted.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingFailedRenderer extends CallMakingRenderer {

    public CallMakingFailedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Failed.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

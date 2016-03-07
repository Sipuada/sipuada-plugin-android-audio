package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingCanceledRenderer extends CallMakingRenderer {

    public CallMakingCanceledRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Canceled.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingRingingRenderer extends CallMakingCancelableRenderer {

    public CallMakingRingingRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Ringing...";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

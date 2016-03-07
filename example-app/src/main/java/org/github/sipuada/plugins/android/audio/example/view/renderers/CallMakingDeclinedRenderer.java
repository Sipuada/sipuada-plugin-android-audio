package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingDeclinedRenderer extends CallMakingRenderer {

    public CallMakingDeclinedRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            String statusMessage = "Declined.";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

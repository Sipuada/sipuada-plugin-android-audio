package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallMakingCancelRenderer extends CallMakingRenderer {

    public CallMakingCancelRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            cancelButton.setEnabled(false);
            cancelButton.setOnClickListener(null);
            cancelButton.setAlpha(0.65f);
            String statusMessage = "Cancelling...";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

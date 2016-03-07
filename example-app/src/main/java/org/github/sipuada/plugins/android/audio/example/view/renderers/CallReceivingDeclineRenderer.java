package org.github.sipuada.plugins.android.audio.example.view.renderers;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;

public class CallReceivingDeclineRenderer extends CallReceivingRenderer {

    public CallReceivingDeclineRenderer(CallPresenterApi presenter) {
        super(presenter);
    }

    @Override
    public void render() {
        super.render();
        if (presenter.sipuadaServiceIsConnected()) {
            acceptButton.setEnabled(false);
            acceptButton.setOnClickListener(null);
            declineButton.setEnabled(true);
            declineButton.setOnClickListener(null);
            String statusMessage = "Declining call...";
            callStatus.setText(statusMessage);
            callStatus.setSelected(true);
        }
    }

}

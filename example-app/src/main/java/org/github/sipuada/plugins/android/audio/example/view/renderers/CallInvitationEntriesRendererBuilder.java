package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;

import java.util.LinkedList;
import java.util.List;

public class CallInvitationEntriesRendererBuilder
        extends RendererBuilder<SipuadaCallData> {

    public CallInvitationEntriesRendererBuilder(CallPresenterApi presenter, CallActivity activity) {
        setPrototypes(getPrototypes(presenter, activity));
    }

    @Override
    protected Class getPrototypeClass(SipuadaCallData content) {
        return CallInvitationEntryRenderer.class;
    }

    private List<Renderer<SipuadaCallData>>
    getPrototypes(CallPresenterApi presenter, CallActivity activity) {
        List<Renderer<SipuadaCallData>> prototypes = new LinkedList<>();
        prototypes.add(new CallInvitationEntryRenderer(presenter, activity));
        return prototypes;
    }

}

package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

import java.util.LinkedList;
import java.util.List;

public class CallsRendererBuilder
        extends RendererBuilder<CallViewState.SipuadaCall> {

    public CallsRendererBuilder(CallPresenterApi presenter, CallActivity activity) {
        setPrototypes(getPrototypes(presenter, activity));
    }

    @Override
    protected Class getPrototypeClass(CallViewState.SipuadaCall content) {
        return CallEntryRenderer.class;
    }

    private List<Renderer<CallViewState.SipuadaCall>>
    getPrototypes(CallPresenterApi presenter, CallActivity activity) {
        List<Renderer<CallViewState.SipuadaCall>> prototypes = new LinkedList<>();
        prototypes.add(new CallEntryRenderer(presenter, activity));
        return prototypes;
    }

}

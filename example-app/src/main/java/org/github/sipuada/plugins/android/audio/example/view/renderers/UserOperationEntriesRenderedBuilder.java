package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;

import java.util.LinkedList;
import java.util.List;

public class UserOperationEntriesRenderedBuilder extends RendererBuilder<String> {

    public UserOperationEntriesRenderedBuilder(SipuadaActivity activity) {
        setPrototypes(getPrototypes(activity));
    }

    @Override
    protected Class getPrototypeClass(String content) {
        return UserOperationEntryRenderer.class;
    }

    private List<Renderer<String>> getPrototypes(SipuadaActivity activity) {
        List<Renderer<String>> prototypes = new LinkedList<>();
        prototypes.add(new UserOperationEntryRenderer(activity));
        return prototypes;
    }

}

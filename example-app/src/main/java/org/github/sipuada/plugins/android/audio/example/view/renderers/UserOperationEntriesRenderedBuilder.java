package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;

import java.util.LinkedList;
import java.util.List;

public class UserOperationEntriesRenderedBuilder extends RendererBuilder<SipuadaUserCredentials> {

    public UserOperationEntriesRenderedBuilder(SipuadaActivity activity) {
        setPrototypes(getPrototypes(activity));
    }

    @Override
    protected Class getPrototypeClass(SipuadaUserCredentials content) {
        return UserOperationsEntryRenderer.class;
    }

    private List<Renderer<SipuadaUserCredentials>> getPrototypes(SipuadaActivity activity) {
        List<Renderer<SipuadaUserCredentials>> prototypes = new LinkedList<>();
        prototypes.add(new UserOperationsEntryRenderer(activity));
        return prototypes;
    }

}

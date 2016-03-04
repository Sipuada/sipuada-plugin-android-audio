package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenterApi;

import java.util.LinkedList;
import java.util.List;

public class UserOperationsEntriesRendererBuilder extends RendererBuilder<SipuadaUserCredentials> {

    public UserOperationsEntriesRendererBuilder(MainPresenterApi presenter) {
        setPrototypes(getPrototypes(presenter));
    }

    @Override
    protected Class getPrototypeClass(SipuadaUserCredentials content) {
        return UserOperationsEntryRenderer.class;
    }

    private List<Renderer<SipuadaUserCredentials>> getPrototypes(MainPresenterApi presenter) {
        List<Renderer<SipuadaUserCredentials>> prototypes = new LinkedList<>();
        prototypes.add(new UserOperationsEntryRenderer(presenter));
        return prototypes;
    }

}

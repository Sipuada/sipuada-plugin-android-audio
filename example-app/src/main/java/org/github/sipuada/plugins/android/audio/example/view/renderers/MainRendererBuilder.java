package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenterApi;

import java.util.LinkedList;
import java.util.List;

public class MainRendererBuilder extends RendererBuilder<SipuadaUserCredentials> {

    public MainRendererBuilder(MainPresenterApi presenter) {
        setPrototypes(getPrototypes(presenter));
    }

    @Override
    protected Class getPrototypeClass(SipuadaUserCredentials content) {
        return MainRenderer.class;
    }

    private List<Renderer<SipuadaUserCredentials>> getPrototypes(MainPresenterApi presenter) {
        List<Renderer<SipuadaUserCredentials>> prototypes = new LinkedList<>();
        prototypes.add(new MainRenderer(presenter));
        return prototypes;
    }

}

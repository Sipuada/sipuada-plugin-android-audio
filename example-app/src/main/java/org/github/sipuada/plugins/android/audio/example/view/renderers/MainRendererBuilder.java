package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.MainActivity;

import java.util.LinkedList;
import java.util.List;

public class MainRendererBuilder extends RendererBuilder<SipuadaUserCredentials> {

    public MainRendererBuilder(MainPresenterApi presenter, MainActivity activity) {
        setPrototypes(getPrototypes(presenter, activity));
    }

    @Override
    protected Class getPrototypeClass(SipuadaUserCredentials content) {
        return MainRenderer.class;
    }

    private List<Renderer<SipuadaUserCredentials>> getPrototypes(MainPresenterApi presenter,
                                                                 MainActivity activity) {
        List<Renderer<SipuadaUserCredentials>> prototypes = new LinkedList<>();
        prototypes.add(new MainRenderer(presenter, activity));
        return prototypes;
    }

}

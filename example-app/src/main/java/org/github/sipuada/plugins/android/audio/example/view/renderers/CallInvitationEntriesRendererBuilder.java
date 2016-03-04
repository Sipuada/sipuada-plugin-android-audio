package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.IncomingCallInvitationActivity;

import java.util.LinkedList;
import java.util.List;

public class CallInvitationEntriesRendererBuilder
        extends RendererBuilder<IncomingCallInvitationActivity.IncomingCallInvitation> {

    public CallInvitationEntriesRendererBuilder(SipuadaPresenterApi presenter,
                                                IncomingCallInvitationActivity activity) {
        setPrototypes(getPrototypes(presenter, activity));
    }

    @Override
    protected Class getPrototypeClass(IncomingCallInvitationActivity.IncomingCallInvitation content) {
        return CallInvitationEntryRenderer.class;
    }

    private List<Renderer<IncomingCallInvitationActivity.IncomingCallInvitation>>
    getPrototypes(SipuadaPresenterApi presenter, IncomingCallInvitationActivity activity) {
        List<Renderer<IncomingCallInvitationActivity
                .IncomingCallInvitation>> prototypes = new LinkedList<>();
        prototypes.add(new CallInvitationEntryRenderer(presenter, activity));
        return prototypes;
    }

}

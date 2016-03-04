package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;

import java.util.LinkedList;
import java.util.List;

public class CallInvitationEntriesRendererBuilder
        extends RendererBuilder<CallActivity.IncomingCallInvitation> {

    public CallInvitationEntriesRendererBuilder(CallPresenterApi presenter,
                                                CallActivity activity) {
        setPrototypes(getPrototypes(presenter, activity));
    }

    @Override
    protected Class getPrototypeClass(CallActivity.IncomingCallInvitation content) {
        return CallInvitationEntryRenderer.class;
    }

    private List<Renderer<CallActivity.IncomingCallInvitation>>
    getPrototypes(CallPresenterApi presenter, CallActivity activity) {
        List<Renderer<CallActivity
                .IncomingCallInvitation>> prototypes = new LinkedList<>();
        prototypes.add(new CallInvitationEntryRenderer(presenter, activity));
        return prototypes;
    }

}

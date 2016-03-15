package org.github.sipuada.plugins.android.audio.example.view.renderers;

import com.pedrogomez.renderers.Renderer;
import com.pedrogomez.renderers.RendererBuilder;

import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallViewState;

import java.util.LinkedList;
import java.util.List;

public class CallRendererBuilder
        extends RendererBuilder<CallViewState.SipuadaCall> {

    public CallRendererBuilder(CallPresenterApi presenter) {
        setPrototypes(getPrototypes(presenter));
    }

    @Override
    protected Class getPrototypeClass(CallViewState.SipuadaCall content) {
        switch (content.getCallState()) {
            case CALL_MAKING:
                return CallMakingRenderer.class;
            case CALL_MAKING_CANCELABLE:
                return CallMakingCancelableRenderer.class;
            case CALL_MAKING_RINGING:
                return CallMakingRingingRenderer.class;
            case CALL_MAKING_CANCEL:
                return CallMakingCancelRenderer.class;
            case CALL_MAKING_CANCELED:
                return CallMakingCanceledRenderer.class;
            case CALL_MAKING_FAILED:
                return CallMakingFailedRenderer.class;
            case CALL_MAKING_DECLINED:
                return CallMakingDeclinedRenderer.class;
            default:
            case CALL_RECEIVING:
                return CallReceivingRenderer.class;
            case CALL_RECEIVING_CANCELED:
                return CallReceivingCanceledRenderer.class;
            case CALL_RECEIVING_FAILED:
                return CallReceivingFailedRenderer.class;
            case CALL_RECEIVING_ACCEPT:
                return CallReceivingAcceptRenderer.class;
            case CALL_RECEIVING_DECLINE:
                return CallReceivingDeclineRenderer.class;
            case CALL_IN_PROGRESS:
                return CallInProgressRenderer.class;
            case CALL_FINISHED:
                return CallFinishedRenderer.class;
        }
    }

    private List<Renderer<CallViewState.SipuadaCall>>
    getPrototypes(CallPresenterApi presenter) {
        List<Renderer<CallViewState.SipuadaCall>> prototypes = new LinkedList<>();
        prototypes.add(new CallMakingRenderer(presenter));
        prototypes.add(new CallMakingCancelableRenderer(presenter));
        prototypes.add(new CallMakingRingingRenderer(presenter));
        prototypes.add(new CallMakingCancelRenderer(presenter));
        prototypes.add(new CallMakingCanceledRenderer(presenter));
        prototypes.add(new CallMakingFailedRenderer(presenter));
        prototypes.add(new CallMakingDeclinedRenderer(presenter));
        prototypes.add(new CallReceivingRenderer(presenter));
        prototypes.add(new CallReceivingCanceledRenderer(presenter));
        prototypes.add(new CallReceivingFailedRenderer(presenter));
        prototypes.add(new CallReceivingAcceptRenderer(presenter));
        prototypes.add(new CallReceivingDeclineRenderer(presenter));
        prototypes.add(new CallInProgressRenderer(presenter));
        prototypes.add(new CallFinishedRenderer(presenter));
        return prototypes;
    }

}

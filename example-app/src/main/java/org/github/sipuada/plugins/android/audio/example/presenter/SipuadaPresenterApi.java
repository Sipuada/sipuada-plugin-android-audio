package org.github.sipuada.plugins.android.audio.example.presenter;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

public interface SipuadaPresenterApi<V extends SipuadaViewApi> extends MvpPresenter<V> {

    void bindToSipuadaService();

    void unbindFromSipuadaService();

    boolean sipuadaServiceIsConnected();

}

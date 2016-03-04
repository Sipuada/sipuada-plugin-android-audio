package org.github.sipuada.plugins.android.audio.example.presenter;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

public interface SipuadaPresenterApi<V extends MvpView> extends MvpPresenter<V> {

    void bindToSipuadaService();

    void unbindFromSipuadaService();

    boolean sipuadaServiceIsConnected();

}

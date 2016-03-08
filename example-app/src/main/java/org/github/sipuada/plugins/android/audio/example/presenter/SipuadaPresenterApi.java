package org.github.sipuada.plugins.android.audio.example.presenter;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public interface SipuadaPresenterApi<V extends SipuadaViewApi> extends MvpPresenter<V> {

    void bindToSipuadaService();

    void unbindFromSipuadaService();

    boolean sipuadaServiceIsConnected();

    interface FetchUsersCredentialsCallback {

        void onSuccess(List<SipuadaUserCredentials> usersCredentials);

    }

    void fetchLocalUsersThenRefresh();

}

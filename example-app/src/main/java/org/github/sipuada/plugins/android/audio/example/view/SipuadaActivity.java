package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenterApi;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;

import java.util.List;

public abstract class SipuadaActivity extends MvpActivity<SipuadaViewApi, SipuadaPresenterApi>
        implements SipuadaViewApi {

    private boolean mBoundToSipuadaService = false;

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().bindToSipuadaService();
    }

    @Override
    public boolean doBindToSipuadaService(ServiceConnection serviceConnection) {
        Intent intent = new Intent(this, SipuadaService.class);
        return bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void sipuadaServiceConnected() {
        mBoundToSipuadaService = true;
        onSipuadaServiceConnected();
    }

    protected abstract void onSipuadaServiceConnected();

    @Override
    public void sipuadaServiceDisconnected() {
        mBoundToSipuadaService = false;
        onSipuadaServiceDisconnected();
    }

    protected abstract void onSipuadaServiceDisconnected();

    @Override
    protected void onStop() {
        super.onStop();
        if (mBoundToSipuadaService) {
            getPresenter().unbindFromSipuadaService();
        }
    }

    public void doUnbindFromSipuadaService(ServiceConnection serviceConnection) {
        unbindService(serviceConnection);
        mBoundToSipuadaService = false;
    }

    @NonNull
    @Override
    public SipuadaPresenter createPresenter() {
        return new SipuadaPresenter();
    }

    @Override
    public void refreshUsersCredentialsList(List<SipuadaUserCredentials> usersCredentials){}

}

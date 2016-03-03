package org.github.sipuada.plugins.android.audio.example.presenter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public class SipuadaPresenter extends MvpBasePresenter<SipuadaViewApi>
        implements SipuadaPresenterApi {

    private SipuadaService mSipuadaService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface FetchUsersCredentialsCallback {

        void onSuccess(List<SipuadaUserCredentials> usersCredentials);

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceConnected();
                fetchCurrentUsersCredentialsThenRefresh();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mSipuadaService = null;
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceDisconnected();
            }
        }

    };

    @Override
    public boolean sipuadaServiceIsConnected() {
        return mSipuadaService != null;
    }

    @Override
    public void bindToSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doBindToSipuadaService(mConnection);
        }
    }

    @Override
    public void unbindFromSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doUnbindFromSipuadaService(mConnection);
        }
    }

    @Override
    public void createSipuada(String username, String primaryHost, String password) {
        mSipuadaService.createSipuada(new SipuadaUserCredentials(username, primaryHost, password));
        fetchCurrentUsersCredentialsThenRefresh();
    }

    @Override
    public void registerAddresses(String username, String primaryHost,
                                  SipuadaApi.RegistrationCallback callback) {
        mSipuadaService.registerAddresses(username, primaryHost, callback);
    }

    @Override
    public void inviteUser(String username, String primaryHost, String remoteUser,
                           SipuadaApi.CallInvitationCallback callback) {
        mSipuadaService.inviteUser(username, primaryHost, remoteUser, callback);
    }

    private void fetchCurrentUsersCredentialsThenRefresh() {
        mSipuadaService.fetchCurrentUsersCredentials(new FetchUsersCredentialsCallback() {

            @Override
            public void onSuccess(final List<SipuadaUserCredentials> usersCredentials) {
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            //noinspection ConstantConditions
                            getView().refreshViewData(usersCredentials);
                        }
                    }

                });
            }

        });
    }

}

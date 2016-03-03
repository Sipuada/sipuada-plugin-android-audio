package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.joanzapata.iconify.IconDrawable;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaPresenterApi;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.view.renderers.UserOperationEntriesRenderedBuilder;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SipuadaActivity extends MvpActivity<SipuadaViewApi, SipuadaPresenterApi>
        implements SipuadaViewApi {

    private static final int REQUEST_NEW_USER_CREDENTIALS = 1;

    @Bind(R.id.sipuplug_andrdio_example_AppToolbar) Toolbar appToolbar;
    @Bind(R.id.sipuplug_andrdio_example_FloatingActionButton) FloatingActionButton floatingActionButton;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;
    @Bind(R.id.sipuplug_andrdio_example_EmptyTextView) TextView emptyView;

    private RVRendererAdapter<SipuadaUserCredentials> adapter;
    private boolean mBoundToSipuadaService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sipuada);
        ButterKnife.bind(this);
        setSupportActionBar(appToolbar);
        appToolbar.setTitle(getTitle());
        appToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
        IconDrawable iconDrawable = new IconDrawable(getApplicationContext(), "md-add")
                .actionBarSize().colorRes(android.R.color.black);
        floatingActionButton.setImageDrawable(iconDrawable);
        adapter = new RVRendererAdapter<>(getLayoutInflater(),
                new UserOperationEntriesRenderedBuilder(getPresenter()),
                new ListAdapteeCollection<>(Arrays.asList(new SipuadaUserCredentials[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        floatingActionButton.setEnabled(false);
        floatingActionButton.setOnClickListener(null);
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
        floatingActionButton.setEnabled(true);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewUserCredentialsDialog.class);
                startActivityForResult(intent, REQUEST_NEW_USER_CREDENTIALS);
            }

        });
    }

    @Override
    public void sipuadaServiceDisconnected() {
        mBoundToSipuadaService = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_USER_CREDENTIALS
                && resultCode == RESULT_OK) {
            String username = data.getStringExtra("username");
            String primaryHost = data.getStringExtra("primaryHost");
            String password = data.getStringExtra("password");
            getPresenter().createSipuada(username, primaryHost, password);
        }
    }

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
    public void refreshViewData(List<SipuadaUserCredentials> usersCredentials) {
        adapter.clear();
        adapter.addAll(new ListAdapteeCollection<>(usersCredentials));
        if (usersCredentials.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

}

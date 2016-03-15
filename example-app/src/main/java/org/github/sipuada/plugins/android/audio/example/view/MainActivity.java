package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.renderers.MainRendererBuilder;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends SipuadaActivity<SipuadaViewApi, MainPresenterApi>
        implements SipuadaViewApi {

    public static final int REQUEST_NEW_USER_CREDENTIALS = 1;
    public static final int REQUEST_UPDATE_USER_CREDENTIALS = 2;

    @Bind(R.id.sipuplug_andrdio_example_AppToolbar) Toolbar appToolbar;
    @Bind(R.id.sipuplug_andrdio_example_FloatingActionButton) FloatingActionButton floatingActionButton;
    @Bind(R.id.sipuplug_andrdio_example_ProgressBar) ProgressBar progressBar;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;
    @Bind(R.id.sipuplug_andrdio_example_EmptyTextView) TextView emptyView;

    private RVRendererAdapter<SipuadaUserCredentials> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(appToolbar);
        appToolbar.setTitle(getTitle());
        appToolbar.setTitleTextColor(ContextCompat
                .getColor(getApplicationContext(), android.R.color.white));
        IconDrawable iconDrawable = new IconDrawable(getApplicationContext(), "md-add")
                .actionBarSize().colorRes(android.R.color.black);
        floatingActionButton.setImageDrawable(iconDrawable);
        adapter = new RVRendererAdapter<>(getLayoutInflater(),
                new MainRendererBuilder(getPresenter(), this),
                new ListAdapteeCollection<>(Arrays.asList(new SipuadaUserCredentials[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        floatingActionButton.setEnabled(false);
        floatingActionButton.setOnClickListener(null);
    }

    @Override
    protected void onSipuadaServiceConnected() {
        floatingActionButton.setEnabled(true);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CredentialsActivity.class);
                startActivityForResult(intent, REQUEST_NEW_USER_CREDENTIALS);
            }

        });
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSipuadaServiceDisconnected() {
        floatingActionButton.setEnabled(false);
        floatingActionButton.setOnClickListener(null);
        progressBar.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_USER_CREDENTIALS
                && resultCode == RESULT_OK) {
            String username = data.getStringExtra(SipuadaApplication.KEY_USERNAME);
            String primaryHost = data.getStringExtra(SipuadaApplication.KEY_PRIMARY_HOST);
            String password = data.getStringExtra(SipuadaApplication.KEY_PASSWORD);
            getPresenter().createSipuada(username, primaryHost, password);
            adapter.clear();
            adapter.notifyDataSetChanged();
            emptyView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_UPDATE_USER_CREDENTIALS
            && resultCode == RESULT_OK) {
            String username = data.getStringExtra(SipuadaApplication.KEY_USERNAME);
            String primaryHost = data.getStringExtra(SipuadaApplication.KEY_PRIMARY_HOST);
            String password = data.getStringExtra(SipuadaApplication.KEY_PASSWORD);
            SipuadaUserCredentials oldUserCredentials = data.getParcelableExtra(SipuadaApplication
                    .KEY_USER_CREDENTIALS);
            getPresenter().updateSipuada(oldUserCredentials, username, primaryHost, password);
            adapter.clear();
            adapter.notifyDataSetChanged();
            emptyView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshUsersCredentialsList(List<SipuadaUserCredentials> usersCredentials) {
        adapter.clear();
        adapter.addAll(new ListAdapteeCollection<>(usersCredentials));
        if (usersCredentials.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

}

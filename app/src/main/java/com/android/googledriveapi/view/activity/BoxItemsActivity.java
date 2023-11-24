package com.android.googledriveapi.view.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.googledriveapi.R;
import com.android.googledriveapi.model.BoxFiles;
import com.android.googledriveapi.model.BoxFolders;
import com.android.googledriveapi.model.BoxItems;
import com.android.googledriveapi.view.adapter.BoxItemAdapter;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxCollection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoxItemsActivity extends AppCompatActivity {
    String refreshToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_items);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        showProgressBar(progressBar, recyclerView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String code = extras.getString("accessToken");
            refreshToken = extras.getString("refreshToken");
            if (!code.isEmpty()) {
                updateUI(code, progressBar, recyclerView);
            }
            // in case we don't get any code we should display an error
        }
    }

    private void updateUI(String code, ProgressBar progressBar, RecyclerView recyclerView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<BoxItems> items = getFolderItems(code);
            handler.post(() -> {
                recyclerView.setAdapter(new BoxItemAdapter(items));
                showData(progressBar, recyclerView);
            });
        });
    }

    private void showProgressBar(ProgressBar progressBar, RecyclerView recyclerView) {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showData(ProgressBar progressBar, RecyclerView recyclerView) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private List<BoxItems> getFolderItems(String code) {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "boxKeys",
                AppCompatActivity.MODE_PRIVATE
        );
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        String boxAccessToken = sharedPreferences.getString("accessToken", null);
        String boxRefreshToken = sharedPreferences.getString("refreshToken", null);

        BoxAPIConnection api = new BoxAPIConnection(getString(R.string.box_client_id), getString(R.string.box_client_secret), boxAccessToken, boxRefreshToken);

        myEdit.putString("accessToken", api.getAccessToken());
        myEdit.putString("refreshToken", api.getRefreshToken());
        myEdit.apply();
        Log.d("__AccessToken", api.getAccessToken());

        Iterable<BoxItem.Info> boxFolder = new BoxFolder(api, "0")
                .getChildren("name", "modified_by");

        List<BoxItems> items = new ArrayList<>();
        for (BoxItem.Info itemInfo : boxFolder) {
            if (itemInfo instanceof BoxFile.Info) {
                BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                Log.d("__BoxFiles", fileInfo.getJson());
                if (fileInfo.getName().endsWith(".mp3")){
                    BoxFiles file = new BoxFiles(fileInfo.getName(), fileInfo.getModifiedBy().getName());
                    items.add(file);
                }
            }
            else if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                Log.d("__BoxFolders", folderInfo.getJson());
                BoxFolders folder = new BoxFolders(folderInfo.getName(), folderInfo.getModifiedBy().getName());
                items.add(folder);
            }
        }
        return items;
    }
}
package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.adapters.MyRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.DialogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class ItemSelectActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private RelativeLayout loadingView;
    private TextView categoryTxt;

    MyRecyclerViewAdapter adapter;
    private ArrayList<ItemModel> items;
    private int selectedCatId = 0;
    private int currentPageNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_select);


        items = new ArrayList<>();

        loadingView = findViewById(R.id.loading_more);

        categoryTxt = findViewById(R.id.category_txt);
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(Common.currentActivity, numberOfColumns));
        adapter = new MyRecyclerViewAdapter(Common.currentActivity, items);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        refresh();

        categoryTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categories != null) {
                    DialogManager.showRadioDialog(currentActivity, null,
                            categories, 0, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    categoryTxt.setText(categories[which]);
                                    selectedCatId = which;
                                }
                            });
                }
            }
        });

        findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyItems(0);
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }

    private void refresh() {
        getMyItems(0);
    }

    private void getMyItems(int page) {
        loadingView.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.MY_ITEMS_URL + "?page=" + String.valueOf(page);

        RequestBody requestBody = new FormBody.Builder()
                .add("categoryID", String.valueOf(selectedCatId))
                .add("userID", Common.me.getId())
                .add("lang", "ja")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg(getString(R.string.error_title), result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                JSONArray itemObjs = result.getJSONArray("data");
                                if (itemObjs != null) {
                                    if (page == 0)
                                        items.clear();
                                    for (int i=0; i<itemObjs.length(); i++) {
                                        try {
                                            JSONObject object = (JSONObject) itemObjs.get(i);
                                            items.add(new ItemModel(object));
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                    adapter = new MyRecyclerViewAdapter(Common.currentActivity, items);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        ItemModel item = items.get(position);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("item", item);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
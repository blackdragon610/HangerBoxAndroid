package com.hanger_box.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
import com.hanger_box.activities.FavoriteDetailActivity;
import com.hanger_box.adapters.FavoriteRecyclerViewAdapter;
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
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment implements FavoriteRecyclerViewAdapter.ItemClickListener {

    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;

    FavoriteRecyclerViewAdapter adapter;
    private View root_view, parent_view;
    private RelativeLayout loadingView;

    private ArrayList<HashMap> items;
    private int selectedCatId = 0;
    private int currentPageNum = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        items = new ArrayList<>();

        loadingView = root_view.findViewById(R.id.loading_more);

        // set up the RecyclerView
        RecyclerView recyclerView = root_view.findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(Common.currentActivity, numberOfColumns));
        adapter = new FavoriteRecyclerViewAdapter(Common.currentActivity, items);
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

        return root_view;
    }
    private void refresh() {
        getFavorites(0);
    }

    private void getFavorites(int page) {
        loadingView.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.FAVORITES_URL + "?page=" + String.valueOf(page);

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
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
                                            HashMap map = new HashMap();
                                            map.put("item_1", new ItemModel(object.getJSONObject("item_1")));
                                            map.put("item_2", new ItemModel(object.getJSONObject("item_2")));
                                            items.add(map);
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                    adapter = new FavoriteRecyclerViewAdapter(Common.currentActivity, items);
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

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(currentActivity, FavoriteDetailActivity.class);
        intent.putExtra("favorite_item", items.get(position));
        startActivity(intent);
    }
}
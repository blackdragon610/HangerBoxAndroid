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
import android.widget.Toast;

import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyItemsFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {
    MyRecyclerViewAdapter adapter;
    private View root_view, parent_view;
    private RelativeLayout loadingView;
    private TextView categoryTxt;

    private ArrayList<ItemModel> items;
    private int selectedCatId = 0;
    private int currentPageNum = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_my_items, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        items = new ArrayList<>();

        loadingView = root_view.findViewById(R.id.loading_more);

        categoryTxt = root_view.findViewById(R.id.category_txt);
        // set up the RecyclerView
        RecyclerView recyclerView = root_view.findViewById(R.id.grid_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(Common.currentActivity, numberOfColumns));
        adapter = new MyRecyclerViewAdapter(Common.currentActivity, items);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.canScrollVertically(1)) {
                    Toast.makeText(currentActivity, "Start", Toast.LENGTH_LONG).show();
                }else if (recyclerView.canScrollVertically(-1)) {
                    Toast.makeText(currentActivity, "Last", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Toast.makeText(currentActivity, String.valueOf(dy), Toast.LENGTH_LONG).show();
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

        root_view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyItems(0);
            }
        });

        return root_view;
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

    public MyItemsFragment() {
        // Required empty public constructor
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyItemsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyItemsFragment newInstance(String param1, String param2) {
        MyItemsFragment fragment = new MyItemsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onItemClick(View view, int position) {
        ItemModel item = items.get(position);
        Intent intent = new Intent(currentActivity, AddItemActivity.class);
        intent.putExtra("from", "detail_item");
        intent.putExtra("item", item);
        startActivity(intent);
    }
}
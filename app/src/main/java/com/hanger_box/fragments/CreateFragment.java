package com.hanger_box.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hanger_box.R;
import com.hanger_box.activities.AddItemActivity;
import com.hanger_box.activities.ItemSelectActivity;
import com.hanger_box.activities.LoginActivity;
import com.hanger_box.activities.PrivacyActivity;
import com.hanger_box.adapters.MyRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFragment extends Fragment implements View.OnClickListener {

    private View root_view, parent_view;
    private ImageView downBtn, topCloseBtn, topItemImg;
    private ImageView upBtn, bottomCloseBtn, bottomItemImg;

    private ItemModel topItem, bottomItem;
    private LinearLayout loadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_create, null);
        parent_view = getActivity().findViewById(R.id.main_view);

        downBtn = root_view.findViewById(R.id.down_btn);
        downBtn.setVisibility(View.INVISIBLE);
        downBtn.setOnClickListener(this);

        topCloseBtn = root_view.findViewById(R.id.top_close);
        topCloseBtn.setVisibility(View.INVISIBLE);
        topCloseBtn.setOnClickListener(this);

        topItemImg = root_view.findViewById(R.id.top_item);
        topItemImg.setOnClickListener(this);

        upBtn = root_view.findViewById(R.id.up_btn);
        upBtn.setVisibility(View.INVISIBLE);
        upBtn.setOnClickListener(this);

        bottomCloseBtn = root_view.findViewById(R.id.bottom_close);
        bottomCloseBtn.setVisibility(View.INVISIBLE);
        bottomCloseBtn.setOnClickListener(this);

        bottomItemImg = root_view.findViewById(R.id.bottom_item);
        bottomItemImg.setOnClickListener(this);

        loadingView = parent_view.findViewById(R.id.loading_layout);

        root_view.findViewById(R.id.add_favorite_btn).setOnClickListener(this);

        // Inflate the layout for this fragment
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

    private void refresh() {
        if (LocalStorageManager.getObjectFromLocal("top_item") != null) {
            topItem = new ItemModel(Common.cm.convertToHashMapFromString(LocalStorageManager.getObjectFromLocal("top_item")));
            Picasso.with(Common.currentActivity)
                    .load(topItem.getImage())
                    .resize(500, 500)
                    .centerCrop()
                    .into(topItemImg);
            downBtn.setVisibility(View.VISIBLE);
            topCloseBtn.setVisibility(View.VISIBLE);
        }else {
            topItemClear();
        }
        if (LocalStorageManager.getObjectFromLocal("bottom_item") != null) {
            bottomItem = new ItemModel(Common.cm.convertToHashMapFromString(LocalStorageManager.getObjectFromLocal("bottom_item")));
            Picasso.with(Common.currentActivity)
                    .load(bottomItem.getImage())
                    .resize(500, 500)
                    .centerCrop()
                    .into(bottomItemImg);
            upBtn.setVisibility(View.VISIBLE);
            bottomCloseBtn.setVisibility(View.VISIBLE);
        }else {
            bottomItemClear();
        }
    }

    private void topItemClear() {
        LocalStorageManager.saveObjectToLocal(null, "top_item");
        topItem = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topItemImg.setImageDrawable(getActivity().getDrawable(R.mipmap.item_top));
        }
        downBtn.setVisibility(View.INVISIBLE);
        topCloseBtn.setVisibility(View.INVISIBLE);
    }

    private void bottomItemClear() {
        LocalStorageManager.saveObjectToLocal(null, "bottom_item");
        bottomItem = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bottomItemImg.setImageDrawable(getActivity().getDrawable(R.mipmap.item_bottom));
        }
        upBtn.setVisibility(View.INVISIBLE);
        bottomCloseBtn.setVisibility(View.INVISIBLE);
    }

    private void addFavoriteItem() {
        if (topItem == null && bottomItem == null) {
            Common.cm.showAlertDlg(getString(R.string.warning_title), getString(R.string.error_favorite), null, null);
            return;
        }
        loadingView.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.CREATE_FAVORITE_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("item_1", topItem == null ? "0" : String.valueOf(topItem.getId()))
                .add("item_2", bottomItem == null ? "0" : String.valueOf(bottomItem.getId()))
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
                                topItemClear();
                                bottomItemClear();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public CreateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.down_btn:
                if (bottomItem == null) {
                    LocalStorageManager.saveObjectToLocal(null, "top_item");
                }else {
                    LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(bottomItem.getMap()), "top_item");
                }
                LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(topItem.getMap()), "bottom_item");
                refresh();

                break;
            case R.id.top_close:
                topItemClear();
                break;
            case R.id.top_item:
                if (Common.me == null) {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                if (topItem == null) {
                    intent = new Intent(getActivity(), AddItemActivity.class);
                    intent.putExtra("from", "create_top");
                    startActivity(intent);
                }
                break;
            case R.id.up_btn:
                if (Common.me == null) {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                if (topItem == null) {
                    LocalStorageManager.saveObjectToLocal(null, "bottom_item");
                }else {
                    LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(topItem.getMap()), "bottom_item");
                }
                LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(bottomItem.getMap()), "top_item");
                refresh();

                break;
            case R.id.bottom_close:
                bottomItemClear();
                break;
            case R.id.bottom_item:
                if (bottomItem == null) {
                    intent = new Intent(getActivity(), AddItemActivity.class);
                    intent.putExtra("from", "create_bottom");
                    startActivity(intent);
                }
                break;
            case R.id.add_favorite_btn:
                addFavoriteItem();
                break;
            default:
                break;
        }
    }
}
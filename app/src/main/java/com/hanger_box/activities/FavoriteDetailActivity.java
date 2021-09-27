package com.hanger_box.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.adapters.MyRecyclerViewAdapter;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.rest.APIManager;
import com.hanger_box.utils.DialogManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class FavoriteDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;

    private TextView titleTxt;
    private LinearLayout loadingLayout;
    private ImageView itemImage1, itemImage2;
    private RelativeLayout deleteBtn;

    private String selectedItem = "item_1";
    private HashMap item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);

        currentActivity = this;

        titleTxt = findViewById(R.id.titleTxt);
        loadingLayout = findViewById(R.id.loading_layout);

        itemImage1 = findViewById(R.id.item_image1);
        itemImage1.setOnClickListener(this);

        itemImage2 = findViewById(R.id.item_image2);
        itemImage2.setOnClickListener(this);

        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        findViewById(R.id.back_btn).setOnClickListener(this);

        item = (HashMap) getIntent().getExtras().getSerializable("favorite_item");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemRefresh();
        }

        loadingLayout.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void itemRefresh() {
        ItemModel item1 = (ItemModel) item.get("item_1");
        if (item1.getImage() != null || item1.getImage() != "") {
            Picasso.with(Common.currentActivity)
                    .load(item1.getImage())
                    .centerCrop()
                    .resize(500, 500)
                    .into(itemImage1);
        }else {
            itemImage1.setImageDrawable(getDrawable(R.mipmap.item_top));
        }
        ItemModel item2 = (ItemModel) item.get("item_2");
        if (item1.getImage() != null || item1.getImage() != "") {
            Picasso.with(Common.currentActivity)
                    .load(item2.getImage())
                    .centerCrop()
                    .resize(500, 500)
                    .into(itemImage2);
        }else {
            itemImage2.setImageDrawable(getDrawable(R.mipmap.item_bottom));
        }
    }

    private void deleteFavorite() {
        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.DELETE_FAVORITE_URL + "/" + item.get("id");

        RequestBody requestBody = new FormBody.Builder()
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
                        loadingLayout.setVisibility(View.GONE);
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg(getString(R.string.error_title), result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                finish();
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
    public void onClick(View v) {
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.delete_btn:
                deleteFavorite();
                break;
            case R.id.item_image1:
                selectedItem = "item_1";
                ItemModel item1 = (ItemModel) item.get("item_1");
                if (item1 != null) {
                    intent = new Intent(currentActivity, AddItemActivity.class);
                    intent.putExtra("from", "detail_item");
                    intent.putExtra("item", item1);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }
                break;
            case R.id.item_image2:
                selectedItem = "item_2";
                ItemModel item2 = (ItemModel) item.get("item_2");
                if (item2 != null) {
                    intent = new Intent(currentActivity, AddItemActivity.class);
                    intent.putExtra("from", "detail_item");
                    intent.putExtra("item", item2);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ITEM_SELECT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    ItemModel updatedItem = (ItemModel) data.getSerializableExtra("item");
                    item.put(selectedItem, updatedItem);
                    itemRefresh();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}
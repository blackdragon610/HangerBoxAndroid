package com.hanger_box.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.Category;
import com.hanger_box.models.ItemModel;
import com.hanger_box.rest.APIManager;
import com.hanger_box.utils.DialogManager;
import com.hanger_box.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class AddItemActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_EDIT_ACTIVITY_ID = 1000;
    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;

    private TextView titleTxt;
    private LinearLayout loadingLayout, selectItemTypeLayout, imageLinkLayout, shopLinkLayout;
    private ImageView itemImage;
    private RelativeLayout saveBtn, editBtn, deleteBtn;
    private EditText imageLinkExt, shopLinkExt, affiliateExt, brandExt, priceExt, commentExt;
    private TextView categoryTxt, currencyTxt;

    private String from = "";
    private String state = "new";
    private String imagePath;

    private ItemModel item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        currentActivity = this;

        imagePath = "";

        titleTxt = findViewById(R.id.titleTxt);
        loadingLayout = findViewById(R.id.loading_layout);

        selectItemTypeLayout = findViewById(R.id.select_item_type_layout);

        imageLinkLayout = findViewById(R.id.image_link_layout);

        shopLinkLayout = findViewById(R.id.shop_link_layout);

        itemImage = findViewById(R.id.item_image);
        itemImage.setOnClickListener(this);

        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        editBtn = findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(this);

        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        imageLinkExt = findViewById(R.id.image_link_txt);
        shopLinkExt = findViewById(R.id.shop_link_txt);
        categoryTxt = findViewById(R.id.category_txt);
        categoryTxt.setOnClickListener(this);
        affiliateExt = findViewById(R.id.affiliate_txt);
        brandExt = findViewById(R.id.brand_txt);
        priceExt = findViewById(R.id.price_txt);
        currencyTxt = findViewById(R.id.currency_txt);
        currencyTxt.setOnClickListener(this);
        commentExt = findViewById(R.id.comment_txt);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.from_camera_layout).setOnClickListener(this);
        findViewById(R.id.from_gallery_layout).setOnClickListener(this);
        findViewById(R.id.from_item_layout).setOnClickListener(this);
        findViewById(R.id.from_link_layout).setOnClickListener(this);

        from = getIntent().getExtras().getString("from");
        if (from.equals("create_top") || from.equals("create_bottom") || from.equals("add_item")) {
            state = "new";
        }else if (from.equals("detail_item")) {
            state = "detail";
        }
        item = (ItemModel) getIntent().getExtras().getSerializable("item");
        changeSate();

        loadingLayout.setVisibility(View.GONE);
    }

    private void changeSate() {
        if (state.equals("new")) {
            titleTxt.setText(getString(R.string.add_item_title));
            selectItemTypeLayout.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }else if (state.equals("edit")) {
            titleTxt.setText(getString(R.string.edit_item_title));
            selectItemTypeLayout.setVisibility(View.GONE);
            if (item != null) {
                itemRefresh();
            }
            imageLinkExt.setEnabled(true);
            shopLinkExt.setEnabled(true);
            affiliateExt.setEnabled(true);
            brandExt.setEnabled(true);
            priceExt.setEnabled(true);
            commentExt.setEnabled(true);
            saveBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }else if (state.equals("detail")) {
            titleTxt.setText(getString(R.string.detail_item_title));
            selectItemTypeLayout.setVisibility(View.GONE);
            if (item != null) {
                itemRefresh();
            }

            imageLinkExt.setEnabled(false);
            shopLinkExt.setEnabled(false);
            affiliateExt.setEnabled(false);
            brandExt.setEnabled(false);
            priceExt.setEnabled(false);
            commentExt.setEnabled(false);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        }
    }

    private void itemRefresh() {
        itemImage.setVisibility(View.VISIBLE);
        Picasso.with(Common.currentActivity)
                .load(item.getImage())
                .centerCrop()
                .resize(500, 500)
                .into(itemImage);
        imageLinkLayout.setVisibility(View.GONE);
        if (state.equals("edit")) {
            shopLinkLayout.setVisibility(View.VISIBLE);
        }else if (state.equals("detail")) {
            if (item.getShopUrl() == null || item.getShopUrl().equals("")) {
                shopLinkLayout.setVisibility(View.GONE);
            }else {
                shopLinkLayout.setVisibility(View.VISIBLE);
                String html = "<a href="+item.getShopUrl()+">"+item.getShopUrl()+"</a>";
                Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
                shopLinkExt.setText(result);
                shopLinkExt.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
        if (item.getAffiliateUrl() == null || item.getAffiliateUrl().equals("")) {
        }else {
            String html = "<a href="+item.getAffiliateUrl()+" target='_blank' rel='nofollow sponsored noopener' style='word-wrap:break-word;'>"+item.getAffiliateUrl()+"</a>";
            Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
            affiliateExt.setText(result);
            affiliateExt.setMovementMethod(LinkMovementMethod.getInstance());
        }
        brandExt.setText(item.getBrand());
        priceExt.setText(String.valueOf(item.getPrice()));
        commentExt.setText(item.getComment());
        int catId = Integer.valueOf(item.getCategoryId());
        if (categories.length > 0) categoryTxt.setText(categories[catId]);
        currencyTxt.setText(item.getCurrency());
    }

    private void saveItem() {
        if (imagePath.equals("") && imageLinkExt.equals("")) {
            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_image), null, null);
            return;
        }
        if (categoryTxt.getText().toString().equals("")) {
            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_category), null, null);
            return;
        }
        String catName = categoryTxt.getText().toString();
        int catId = 0;
        for (int i=0; i<categories.length; i++) {
            if (categories[i].equals(catName)) {
                catId = i;
                break;
            }
        }

        loadingLayout.setVisibility(View.VISIBLE);
            final MediaType MEDIA_TYPE = imagePath.endsWith("png") ?

                    MediaType.parse("image/png") : MediaType.parse("image/jpeg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image_url", imageLinkExt.getText().toString())
                .addFormDataPart("shop_url", shopLinkExt.getText().toString())
                .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                .addFormDataPart("brand", brandExt.getText().toString())
                .addFormDataPart("category_id", String.valueOf(catId))
                .addFormDataPart("price", priceExt.getText().toString())
                .addFormDataPart("currency", currencyTxt.getText().toString())
                .addFormDataPart("comment", commentExt.getText().toString())
                .addFormDataPart("userID", Common.me.getId())
                .addFormDataPart("lang", "ja")
                .build();
        if (imagePath != "") {
            File file = new File(imagePath);
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, file))
                    .addFormDataPart("image_url", imageLinkExt.getText().toString())
                    .addFormDataPart("shop_url", shopLinkExt.getText().toString())
                    .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                    .addFormDataPart("brand", brandExt.getText().toString())
                    .addFormDataPart("category_id", String.valueOf(catId))
                    .addFormDataPart("price", priceExt.getText().toString())
                    .addFormDataPart("currency", currencyTxt.getText().toString())
                    .addFormDataPart("comment", commentExt.getText().toString())
                    .addFormDataPart("userID", Common.me.getId())
                    .addFormDataPart("lang", "ja")
                    .build();
        }

            Request request = new Request.Builder()
                    .url(Config.SERVER_URL + Config.CREATE_ITEM_URL)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingLayout.setVisibility(View.GONE);
                            cm.showAlertDlg(getString(R.string.error_title), e.getMessage().toString(), null, null);
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
                                            item = new ItemModel(result.getJSONObject("data"));
                                            if (from.equals("create_top")) {
                                                LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                                            }else if (from.equals("create_bottom")) {
                                                LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                                            }
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("item", item);
                                            setResult(Activity.RESULT_OK,returnIntent);
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
            });
    }

    private void updateItem() {
        String catName = categoryTxt.getText().toString();
        int catId = 0;
        for (int i=0; i<categories.length; i++) {
            if (categories[i].equals(catName)) {
                catId = i;
                break;
            }
        }

        loadingLayout.setVisibility(View.VISIBLE);
        final MediaType MEDIA_TYPE = imagePath.endsWith("png") ?

                MediaType.parse("image/png") : MediaType.parse("image/jpeg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", item.getId())
                .addFormDataPart("image_url", item.getImage())
                .addFormDataPart("shop_url", shopLinkExt.getText().toString())
                .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                .addFormDataPart("brand", brandExt.getText().toString())
                .addFormDataPart("category_id", String.valueOf(catId))
                .addFormDataPart("price", priceExt.getText().toString())
                .addFormDataPart("currency", currencyTxt.getText().toString())
                .addFormDataPart("comment", commentExt.getText().toString())
                .addFormDataPart("userID", Common.me.getId())
                .addFormDataPart("lang", "ja")
                .build();

        if (imagePath != "") {
            File file = new File(imagePath);
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", item.getId())
                    .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, file))
                    .addFormDataPart("image_url", item.getImage())
                    .addFormDataPart("shop_url", shopLinkExt.getText().toString())
                    .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                    .addFormDataPart("brand", brandExt.getText().toString())
                    .addFormDataPart("category_id", String.valueOf(catId))
                    .addFormDataPart("price", priceExt.getText().toString())
                    .addFormDataPart("currency", currencyTxt.getText().toString())
                    .addFormDataPart("comment", commentExt.getText().toString())
                    .addFormDataPart("userID", Common.me.getId())
                    .addFormDataPart("lang", "ja")
                    .build();
        }

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + Config.UPDATE_ITEM_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), e.getMessage().toString(), null, null);
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
                                        item = new ItemModel(result.getJSONObject("data"));
                                        if (from.equals("create_top")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                                        }else if (from.equals("create_bottom")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                                        }
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("item", item);
                                        setResult(Activity.RESULT_OK,returnIntent);
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
        });
    }

    private void showImageEditview(String type) {
        selectItemTypeLayout.setVisibility(View.GONE);
        itemImage.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);
        shopLinkLayout.setVisibility(View.GONE);
        imageLinkLayout.setVisibility(View.GONE);

        Intent intent = new Intent(getApplicationContext(), ImageEditActivity.class);
        intent.putExtra("from", type);
        startActivityForResult(intent, IMAGE_EDIT_ACTIVITY_ID);
    }

    private void deleteItem() {
        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.DELETE_ITEM_URL + "/" + item.getId();

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
            case R.id.save_btn:
                if (item == null) {
                    saveItem();
                }else {
                    updateItem();
                }
                break;
            case R.id.edit_btn:
                state = "edit";
                changeSate();
                break;
            case R.id.delete_btn:
                if (state.equals("detail")) {
                    deleteItem();
                }
                break;
            case R.id.item_image:
                if (state.equals("detail"))
                    return;
                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.image_types), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showImageEditview("from_camera");
                                }else {
                                    showImageEditview("from_gallery");
                                }
                            }
                        });

                break;
            case R.id.category_txt:
                if (state.equals("detail"))
                    return;
                if (categories != null) {
                    DialogManager.showRadioDialog(currentActivity, null,
                            categories, 0, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    categoryTxt.setText(categories[which]);
                                }
                            });
                }

                break;
            case R.id.currency_txt:
                if (state.equals("detail"))
                    return;

                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.currency), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currencyTxt.setText(getResources().getStringArray(R.array.currency)[which]);
                            }
                        });

                break;
            case R.id.from_camera_layout:
                showImageEditview("from_camera");

                break;
            case R.id.from_gallery_layout:
                showImageEditview("from_gallery");

                break;
            case R.id.from_item_layout:
                if (from.equals("create_top") || from.equals("create_bottom")) {
                    intent = new Intent(getApplicationContext(), ItemSelectActivity.class);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }

                break;
            case R.id.from_link_layout:
                selectItemTypeLayout.setVisibility(View.GONE);
                itemImage.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                shopLinkLayout.setVisibility(View.VISIBLE);
                imageLinkLayout.setVisibility(View.VISIBLE);
                state = "edit";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_EDIT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    imagePath = data.getStringExtra("path");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    itemImage.setImageBitmap(bitmap);
                    saveBtn.setVisibility(View.VISIBLE);
                    state = "edit";
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    imagePath = "";
                }
                break;
            case ITEM_SELECT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    item = (ItemModel) data.getSerializableExtra("item");
                    if (from.equals("create_top")) {
                        LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                        finish();
                    }else if (from.equals("create_bottom")) {
                        LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                        finish();
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }
}
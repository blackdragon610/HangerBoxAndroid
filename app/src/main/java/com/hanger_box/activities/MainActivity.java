package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.hanger_box.R;
import com.hanger_box.SwipeDisabledViewPager;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.fragments.CreateFragment;
import com.hanger_box.fragments.FavoriteFragment;
import com.hanger_box.fragments.LibrariesFragment;
import com.hanger_box.fragments.MyItemsFragment;
import com.hanger_box.fragments.MyPageFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;

import static com.hanger_box.common.Common.cm;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigation;
    private SwipeDisabledViewPager viewPager;
    private TextView titleTxt;
    private ImageView createLogoImg;
    private RelativeLayout addBtn;
    private RelativeLayout settingBtn;

    private FavoriteFragment favoriteFragment;
    private MyItemsFragment myItemsFragment;
    private CreateFragment createFragment;
    private LibrariesFragment librariesFragment;
    private MyPageFragment myPageFragment;

    private AdView bannerAdView;
    private boolean adLoaded = false;
    private int selectedIndex = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Common.currentActivity = this;

        getCategories();

        favoriteFragment = new FavoriteFragment();
        myItemsFragment = new MyItemsFragment();
        createFragment = new CreateFragment();
        librariesFragment = new LibrariesFragment();
        myPageFragment = new MyPageFragment();

        titleTxt = findViewById(R.id.titleTxt);
        titleTxt.setText(getResources().getString(R.string.app_name));
        titleTxt.setVisibility(View.INVISIBLE);

        createLogoImg = findViewById(R.id.create_logo);

        addBtn = findViewById(R.id.add_btn);
        addBtn.setVisibility(View.GONE);

        settingBtn = findViewById(R.id.setting_btn);
        settingBtn.setVisibility(View.GONE);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(5);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_favorite:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_my_items:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_create:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_libraries:
                        viewPager.setCurrentItem(3);
                        return true;
                    case R.id.navigation_my_page:
                        viewPager.setCurrentItem(4);
                        return true;
                }
                return false;
            }
        });
        navigation.setSelectedItemId(R.id.navigation_create);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;
                if (viewPager.getCurrentItem() == 0) {
                    showBannerAd();
                    addBtn.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.favorite_nav_title));
                } else if (viewPager.getCurrentItem() == 1) {
                    showBannerAd();
                    addBtn.setVisibility(View.VISIBLE);
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.my_items_nav_title));
                } else if (viewPager.getCurrentItem() == 2) {
                    hideBannerAd();
                    addBtn.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.GONE);
                    titleTxt.setVisibility(View.INVISIBLE);
                    createLogoImg.setVisibility(View.VISIBLE);
                } else if (viewPager.getCurrentItem() == 3) {
                    showBannerAd();
                    addBtn.setVisibility(View.VISIBLE);
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.libraries_nav_title));
                } else if (viewPager.getCurrentItem() == 4) {
                    showBannerAd();
                    addBtn.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.VISIBLE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.my_page_nav_title));
                } else {
                    showBannerAd();
                    addBtn.setVisibility(View.INVISIBLE);
                    settingBtn.setVisibility(View.INVISIBLE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedIndex == 1) {
                    Intent intent = new Intent(getApplicationContext(), AddItemActivity.class);
                    intent.putExtra("from", "add_item");
                    startActivity(intent);
                }else if (selectedIndex == 3) {

                }
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        //initializing the Google Admob SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                loadBannerAd();
                //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
//                Toast.makeText (MainActivity.this, "AdMob Sdk Initialize "+ initializationStatus.toString(), Toast.LENGTH_LONG).show();

            }
        });

        bannerAdView = (AdView) findViewById(R.id.bannerAdView);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // setting adLoaded to true
                adLoaded = true;
                showBannerAd();
                // Showing a simple Toast message to user when an ad is loaded
//                Toast.makeText (MainActivity.this, "Ad is Loaded", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // setting adLoaded to false
                adLoaded = false;

                // Showing a simple Toast message to user when and ad is failed to load
//                Toast.makeText (MainActivity.this, "Ad Failed to Load ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdOpened() {

                // Showing a simple Toast message to user when an ad opens and overlay and covers the device screen
//                Toast.makeText (MainActivity.this, "Ad Opened", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdClicked() {

                // Showing a simple Toast message to user when a user clicked the ad
//                Toast.makeText (MainActivity.this, "Ad Clicked", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdLeftApplication() {

                // Showing a simple Toast message to user when the user left the application
//                Toast.makeText (MainActivity.this, "Ad Left the Application", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdClosed() {

                // Showing a simple Toast message to user when the user interacted with ad and got the other app and then return to the app again
//                Toast.makeText (MainActivity.this, "Ad is Closed", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void loadBannerAd()
    {
        // Creating  a Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();

        // load Ad with the Request
        bannerAdView.loadAd(adRequest);

        // Showing a simple Toast message to user when an ad is Loading
//        Toast.makeText (MainActivity.this, "Banner Ad is loading ", Toast.LENGTH_LONG).show();

    }

    private void showBannerAd()
    {
        if ( adLoaded )
        {
            //showing the ad Banner Ad if it is loaded
            bannerAdView.setVisibility(View.VISIBLE);

            // Showing a simple Toast message to user when an banner ad is shown to the user
//            Toast.makeText (MainActivity.this, "Banner Ad Shown", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Load the banner ad if it is not loaded
            loadBannerAd();
        }
    }

    private void hideBannerAd()
    {
        // Hiding the Banner
        bannerAdView.setVisibility(View.GONE);
    }

    void changeTabIndexFromUrl(String changedUrl){
//        if (changedUrl.equals(Config.SERVER_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_home);
//        }else if (changedUrl.equals(Config.FACEBOOK_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_category);
//        }else if (changedUrl.equals(Config.SERVER_URL + Config.BOOKS_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_video);
//        }else if (changedUrl.equals(Config.SCHEDULE_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_favorite);
//        }else if (changedUrl.equals(Config.SERVER_URL + Config.PROFILE_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_profile);
//        }
    }

    private void getCategories() {
//        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        final String path = Config.SERVER_URL + Config.GET_CATEGORY_URL;
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        // Build form body.
        FormBody formBody = formBodyBuilder.build();
        formBodyBuilder.add("userID", Common.me.getId());
        formBodyBuilder.add("lang", "ja");

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        loadingLayout.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                        getCategories();
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
//                        loadingLayout.setVisibility(View.GONE);
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg(getString(R.string.error_title), result.getString("message"), null, null);
                                getCategories();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                JSONArray catObjs = result.getJSONArray("data");
                                if (catObjs != null) {
                                    Common.categories = new CharSequence[catObjs.length()];
                                    for (int i=0; i<catObjs.length(); i++) {
                                        try {
                                            JSONObject object = (JSONObject) catObjs.get(i);
                                            Common.categories[i] = object.getString("name");
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                }
                                JSONObject userInfo = result.getJSONObject("data");
                                LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(Common.me.getMap()), "account");
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        if (item.getTitle().equals(getResources().getString(R.string.setting_title))) {
//            Intent intent = new Intent(this, RegisterActivity.class);
//            intent.putExtra("fromAc", 2);
//            startActivity(intent);
//        }else if (item.getTitle().equals(getResources().getString(R.string.terms_menu_title))) {
//            Intent intent = new Intent(this, TermsAndUseActivity.class);
//            intent.putExtra("fromAc", 0);
//            startActivity(intent);
//        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }

    class MyAdapter extends FragmentStatePagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return favoriteFragment;
                case 1:
                    return myItemsFragment;
                case 2:
                    return createFragment;
                case 3:
                    return librariesFragment;
                case 4:
                    return myPageFragment;
                default:
                    throw new IllegalArgumentException("Unknown Tab!!");
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

}

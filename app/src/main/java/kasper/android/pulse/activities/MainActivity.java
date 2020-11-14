package kasper.android.pulse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FragmentsAdapter;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.ExploreFragment;
import kasper.android.pulse.fragments.DashboardFragment;
import kasper.android.pulse.fragments.SettingsFragment;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.rxbus.notifications.AppBarStateChanged;

public class MainActivity extends AppCompatActivity {

    private BlurView blurView;
    private FrameLayout navigationBackground;

    private FrameLayout botPickerShadow;
    private LinearLayout botPicker;
    private RecyclerView botPickerRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blurView = findViewById(R.id.blurView);
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(20)
                .setHasFixedTransformationMatrix(false);

        Core.getInstance().bus().register(this);

        ViewPager pagesSlider = findViewById(R.id.pagesSlider);
        pagesSlider.setOffscreenPageLimit(3);
        List<BaseFragment> fragmentList = new ArrayList<>();
        fragmentList.add(DashboardFragment.instantiate());
        pagesSlider.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), fragmentList));

        botPickerShadow = findViewById(R.id.botPickerShadow);
        botPicker = findViewById(R.id.botPicker);
        botPickerRV = findViewById(R.id.botPickerRV);
        navigationBackground = findViewById(R.id.navigationBackground);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.rss:
                        pagesSlider.setCurrentItem(0);
                        break;
                    case R.id.explore:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        break;
                }
                return true;
            }
        });

        pagesSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    navigation.setSelectedItemId(R.id.settings);
                else if (position == 1)
                    navigation.setSelectedItemId(R.id.rss);
                else if (position == 2)
                    navigation.setSelectedItemId(R.id.explore);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pagesSlider.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        if (((RelativeLayout.LayoutParams)botPicker.getLayoutParams()).rightMargin > GraphicHelper.dpToPx(-350)) {
            closeBotPicker();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    ValueAnimator va, va2;
    AppBarStateChanged.AppBarState appBarState = AppBarStateChanged.AppBarState.EXPANDED;

    @Subscribe
    public void onAppBarStateChanged(AppBarStateChanged appBarStateChanged) {
        if (appBarStateChanged.getState() == AppBarStateChanged.AppBarState.EXPANDED) {
            if (appBarState != appBarStateChanged.getState()) {
                if (va != null && va2 != null) {
                    va.cancel();
                    va2.cancel();
                }
                va = ValueAnimator.ofInt(20, 1);
                va.addUpdateListener(animation -> {
                    blurView.setBlurRadius((int) animation.getAnimatedValue());
                    if (((int) animation.getAnimatedValue()) == 1) {
                        blurView.setVisibility(View.GONE);
                    }
                });
                va2 = ValueAnimator.ofFloat(0, 1);
                va2.addUpdateListener(animation -> navigationBackground.setAlpha((float) animation.getAnimatedValue()));
                va.start();
                va2.start();
            }
        } else {
            if (appBarState != appBarStateChanged.getState()) {
                if (va != null && va2 != null) {
                    va.cancel();
                    va2.cancel();
                }
                blurView.setVisibility(View.VISIBLE);
                va = ValueAnimator.ofInt(1, 20);
                va.addUpdateListener(animation -> {
                    blurView.setBlurRadius((int) animation.getAnimatedValue());
                });
                va2 = ValueAnimator.ofFloat(1, 0);
                va2.addUpdateListener(animation -> navigationBackground.setAlpha((float) animation.getAnimatedValue()));
                va.start();
                va2.start();
            }
        }
        appBarState = appBarStateChanged.getState();
    }

    public void onBotPickerCloseBtnClicked(View view) {
        closeBotPicker();
    }

    private void closeBotPicker() {
        ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(0), GraphicHelper.dpToPx(-350));
        valAnim.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) botPicker.getLayoutParams();
            lp.rightMargin = (int) animation.getAnimatedValue();
            botPicker.setLayoutParams(lp);
        });
        ValueAnimator valAnim2 = ValueAnimator.ofFloat(GraphicHelper.dpToPx(0.45f), GraphicHelper.dpToPx(0f));
        valAnim2.addUpdateListener(animation -> {
            botPickerShadow.setAlpha((float)animation.getAnimatedValue());
        });
        valAnim.start();
        valAnim2.start();
        PulseHelper.setOnPreviewMode(false);
        PulseHelper.setPulseViewTablePreviews(new Hashtable<>());
    }
}

package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.synnapps.carouselview.CarouselView;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.StoreBotsAdapter;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.extras.HorizontalLinearDecoration;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RobotHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BotStoreActivity extends AppCompatActivity {

    LinearLayout container;
    CarouselView carouselView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_store);

        container = findViewById(R.id.botStoreContentContainer);
        carouselView = findViewById(R.id.botStoreCarouselView);

        RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
        Call<Packet> call = robotHandler.getBotStoreContent();
        NetworkHelper.requestServer(call, new ServerCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onRequestSuccess(Packet packet) {
                final List<Entities.BotStoreBanner> banners = packet.getBotStoreHeader().getBanners();
                final List<Entities.BotStoreSection> sections = packet.getBotStoreSections();
                carouselView.setViewListener(position -> {
                    @SuppressLint("InflateParams") View customView = getLayoutInflater()
                            .inflate(R.layout.carousel_banner_item, null);
                    GlideApp.with(BotStoreActivity.this).load(banners.get(position).getImagePath())
                            .into((ImageView) customView.findViewById(R.id.bannerImage));
                    ((TextView) customView.findViewById(R.id.bannerTitle)).setText(banners.get(position)
                            .getTitle());
                    return customView;
                });
                carouselView.setPageCount(banners.size());
                carouselView.setCurrentItem(0);
                for (Entities.BotStoreSection section : sections) {
                    TextView textView = new TextView(BotStoreActivity.this);
                    LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            GraphicHelper.dpToPx(56));
                    titleParams.setMargins(GraphicHelper.dpToPx(32), GraphicHelper.dpToPx(32)
                            , GraphicHelper.dpToPx(16), 0);
                    textView.setLayoutParams(titleParams);
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    textView.setTextSize(20);
                    textView.setTextColor(Color.WHITE);
                    textView.setText("Section " + section.getBotStoreSectionId());
                    container.addView(textView);
                    RecyclerView recyclerView = new RecyclerView(BotStoreActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, GraphicHelper.dpToPx(212));
                    params.setMargins(0, 0, 0, 0);
                    recyclerView.setLayoutParams(params);
                    recyclerView.setLayoutManager(new LinearLayoutManager(BotStoreActivity.this,
                            RecyclerView.HORIZONTAL, false));
                    recyclerView.addItemDecoration(new HorizontalLinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(16)));
                    List<Entities.Bot> bots = new ArrayList<>();
                    for (Entities.BotStoreBot botStoreBot : section.getBotStoreBots()) {
                        bots.add(botStoreBot.getBot());
                    }
                    recyclerView.setAdapter(new StoreBotsAdapter(BotStoreActivity.this, bots));
                    container.addView(recyclerView);
                }
            }

            @Override
            public void onServerFailure() {
                Toast.makeText(BotStoreActivity.this, "BotStore data fetch failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(BotStoreActivity.this, "BotStore data fetch failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackBtnClicked(View view) {
        onBackPressed();
    }
}

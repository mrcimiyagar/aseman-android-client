package kasper.android.pulse.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.BotsFullAdapter;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.WorkerUpdated;

public class AddBotToRoomActivity extends BaseActivity {

    boolean modified = false;
    String modifieds = "";

    Entities.Workership[] existingBots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bot_to_room);

        Core.getInstance().bus().register(this);

        long complexId = 0;
        long roomId = 0;

        if (getIntent().getExtras() != null) {
            complexId = getIntent().getExtras().getLong("complex_id");
            roomId = getIntent().getExtras().getLong("room_id");
        }
        existingBots = (Entities.Workership[]) getIntent().getExtras()
                .getSerializable("existing_bots");
        if (existingBots == null)
            existingBots = new Entities.Workership[0];

        RecyclerView recyclerView = findViewById(R.id.addBotToRoomRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new BotsFullAdapter(this, complexId, roomId,
                DatabaseHelper.getSubscribedBots(),
                new ArrayList<>(Arrays.asList(existingBots))));
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onWorkerUpdated(WorkerUpdated workerUpdated) {
        for (int counter = 0; counter < existingBots.length; counter++) {
            if (existingBots[counter].getWorkershipId() == workerUpdated.getWorkership().getWorkershipId()) {
                existingBots[counter].setPosX(workerUpdated.getWorkership().getPosX());
                existingBots[counter].setPosY(workerUpdated.getWorkership().getPosY());
                existingBots[counter].setWidth(workerUpdated.getWorkership().getWidth());
                existingBots[counter].setHeight(workerUpdated.getWorkership().getHeight());
                existingBots[counter].setAngle(workerUpdated.getWorkership().getAngle());
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                modified = true;
                if (data.getExtras() != null)
                    modifieds += ";" + data.getExtras().getLong("bot_id") + "_" +
                            data.getExtras().getInt("pos_x") + "_" +
                            data.getExtras().getInt("pos_y") + "_" +
                            data.getExtras().getInt("width") + "_" +
                            data.getExtras().getInt("height") + "_";
                if (modifieds.startsWith(";")) {
                    modifieds = modifieds.substring(1);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent()
                .putExtra("modified", modified)
                .putExtra("modifieds", modifieds));
        super.onBackPressed();
    }

    public void onBackBtnClicked(View view) {
        onBackPressed();
    }
}

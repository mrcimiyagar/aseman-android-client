package kasper.android.pulse.activities;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RobotHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkershipDataActivity extends AppCompatActivity {

    long botId, complexId, roomId;
    EditText xET, yET, widthET, heightET;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workership_data);

        xET = findViewById(R.id.widgetSettingsXET);
        yET = findViewById(R.id.widgetSettingsYET);
        widthET = findViewById(R.id.widgetSettingsWidthET);
        heightET = findViewById(R.id.widgetSettingsHeightET);

        if (getIntent().getExtras() != null) {
            botId = getIntent().getExtras().getLong("bot_id");
            complexId = getIntent().getExtras().getLong("complex_id");
            roomId = getIntent().getExtras().getLong("room_id");
            xET.setText("" + getIntent().getExtras().getInt("pos_x"));
            yET.setText("" + getIntent().getExtras().getInt("pos_y"));
            widthET.setText("" + getIntent().getExtras().getInt("width"));
            heightET.setText("" + getIntent().getExtras().getInt("height"));
        }
    }

    public void onBackBtnClicked(View view) {
        setResult(RESULT_CANCELED);
        onBackPressed();
    }

    public void onSaveBtnClicked(View view) {
        try {
            final int posX = Integer.parseInt(xET.getText().toString());
            final int posY = Integer.parseInt(yET.getText().toString());
            final int width = Integer.parseInt(widthET.getText().toString());
            final int height = Integer.parseInt(heightET.getText().toString());
            Packet packet = new Packet();
            Entities.Bot bot = new Entities.Bot();
            bot.setBaseUserId(botId);
            packet.setBot(bot);
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(complexId);
            packet.setComplex(complex);
            Entities.Room room = new Entities.Room();
            room.setRoomId(roomId);
            packet.setRoom(room);
            Entities.Workership ws = new Entities.Workership();
            ws.setPosX(posX);
            ws.setPosY(posY);
            ws.setWidth(width);
            ws.setHeight(height);
            packet.setWorkership(ws);
            RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
            Call<Packet> call = robotHandler.updateWorkership(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    GraphicHelper.getDesktopListener().workerUpdated(ws);
                    finish();
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(WorkershipDataActivity.this, "Worker update failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(WorkershipDataActivity.this, "Worker update failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "All inputs must be number", Toast.LENGTH_SHORT).show();
        }
    }
}

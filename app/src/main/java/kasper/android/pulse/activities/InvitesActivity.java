package kasper.android.pulse.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.InviteAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;

public class InvitesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        RecyclerView recyclerView = findViewById(R.id.activity_invites_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new InviteAdapter(this, DatabaseHelper.getMyInvites()));
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}

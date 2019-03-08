package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.UsersInviteAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;

import android.os.Bundle;
import android.view.View;

public class UserInvitesActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_invites);

        final long userId;
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("user_id"))
                userId = getIntent().getExtras().getLong("user_id");
            else
                userId = 0;
        } else
            userId = 0;

        recyclerView = findViewById(R.id.userInvitesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new UsersInviteAdapter(userId, DatabaseHelper.getInvitableComplexes()));
    }

    @Override
    protected void onDestroy() {
        if (recyclerView.getAdapter() != null)
            ((UsersInviteAdapter) recyclerView.getAdapter()).dispose();
        super.onDestroy();
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}

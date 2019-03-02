package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexesInviteAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ComplexesInviteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_picker);

        final long userId;
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("user_id"))
                userId = getIntent().getExtras().getLong("user_id");
            else
                userId = 0;
        } else
            userId = 0;

        RecyclerView recyclerView = findViewById(R.id.selectComplexRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new ComplexesInviteAdapter(userId, DatabaseHelper.getAdminedComplexes()));
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}

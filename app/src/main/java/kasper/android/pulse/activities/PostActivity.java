package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.PostSectionAdapter;
import kasper.android.pulse.models.entities.Entities;

public class PostActivity extends BaseActivity {

    private RecyclerView sectionsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Entities.Post post = (Entities.Post) getIntent().getExtras().getSerializable("post");

        sectionsRV = findViewById(R.id.sectionsRV);
        sectionsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        sectionsRV.setAdapter(new PostSectionAdapter(this, post));
    }
}

package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulse.R;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TitleEditorActivity extends AppCompatActivity {

    ExtendedEditText titleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_editor);

        titleTV = findViewById(R.id.titleET);

        if (getIntent().getExtras() != null)
            titleTV.setText(getIntent().getExtras().getString("title"));
    }

    public void onSaveBtnClicked(View view) {
        if (titleTV.getText().toString().length() > 0 && titleTV.getText().toString().length() <= 32) {
            setResult(RESULT_OK, new Intent().putExtra("title", titleTV.getText().toString()));
            finish();
        }
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}

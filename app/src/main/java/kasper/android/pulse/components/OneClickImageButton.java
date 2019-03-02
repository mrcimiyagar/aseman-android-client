package kasper.android.pulse.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

public class OneClickImageButton extends AppCompatImageButton {

    private boolean enabled = true;

    public OneClickImageButton(Context context) {
        super(context);
    }

    public OneClickImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OneClickImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        super.setOnClickListener(view -> {
            if (enabled) {
                enabled = false;
                l.onClick(view);
            }
        });
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }
}

package kasper.android.pulse.components;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class OneClickFAB extends FloatingActionButton {

    private boolean enabled = true;

    public OneClickFAB(Context context) {
        super(context);
    }

    public OneClickFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OneClickFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(view -> {
            if (enabled) {
                enabled = false;
                l.onClick(view);
            }
        });
    }

    public void enable() {
        this.enabled = true;
    }
}

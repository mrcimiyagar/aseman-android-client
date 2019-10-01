package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulseframework.models.Controls;

public class BotPicked {

    private Entities.Bot bot;
    private Controls.Control rootControl;
    private float x;
    private float y;
    private float innerX;
    private float innerY;

    public BotPicked(Entities.Bot bot, Controls.Control rootControl, float x, float y, float innerX, float innerY) {
        this.bot = bot;
        this.rootControl = rootControl;
        this.x = x;
        this.y = y;
        this.innerX = innerX;
        this.innerY = innerY;
    }

    public Entities.Bot getBot() {
        return bot;
    }

    public Controls.Control getRootControl() {
        return rootControl;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getInnerX() {
        return innerX;
    }

    public float getInnerY() {
        return innerY;
    }
}

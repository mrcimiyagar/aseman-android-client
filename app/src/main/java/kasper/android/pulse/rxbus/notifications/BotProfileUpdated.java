package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class BotProfileUpdated {

    private Entities.Bot bot;

    public BotProfileUpdated(Entities.Bot bot) {
        this.bot = bot;
    }

    public Entities.Bot getBot() {
        return bot;
    }
}

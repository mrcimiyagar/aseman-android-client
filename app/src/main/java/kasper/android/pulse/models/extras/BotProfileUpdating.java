package kasper.android.pulse.models.extras;

import kasper.android.pulse.models.entities.Entities;

public class BotProfileUpdating extends ProfileUpdating {

    private Entities.Bot bot;

    public BotProfileUpdating(String path, Entities.Bot bot) {
        super(path);
        this.bot = bot;
    }

    public Entities.Bot getBot() {
        return bot;
    }
}

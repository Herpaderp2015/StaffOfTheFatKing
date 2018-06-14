package com.sotfk;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.sotfk.NPCs.Raquao;

public abstract class OtherEvent extends Event{
    private static final Event RAQUAO = new Event() {
        @Override
        public void run(Group foreground) {
            Raquao raquao = new Raquao();
            foreground.addActor(raquao);
        }

        @Override
        public boolean canRun(Player player) {
            return true;
        }
    };

    public static final Event[] OTHER_EVENTS = {
            RAQUAO
    };
}

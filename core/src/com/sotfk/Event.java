package com.sotfk;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class Event {
    public abstract void run(Group foreground);

    public abstract boolean canRun(Player player);
}

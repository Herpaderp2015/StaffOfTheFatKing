package com.sotfk.NPCs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.sotfk.EventNPC;
import com.sotfk.PepSecret;
import com.sotfk.Player;

import static com.sotfk.PepSecret.*;

public class Raquao extends EventNPC {
    public Raquao() {
        super("raquao");
        moveBy(0, 36);
    }

    @Override
    public void event() {
        textBox.setText("Hey, do you want me to teach you some spells or something?", true);
        textBox.setVisible(true);
        textBox.awaitResponse(() -> {
            textBox.setText("Alright, your loss.", true);
            textBox.setVisible(true);
            textBox.awaitResponse(() -> {
                for(Actor a : getParent().getChildren()) {
                    if(a instanceof Player)
                        ((Player)a).setCurAnim("Walk");
                }
                bgMoving = true;
                inEvent = false;
                PepSecret.textBox.setVisible(false);
                addAction(Actions.sequence(Actions.moveBy(-720, 0, 4f), Actions.run(() -> {
                    for(Actor a : getParent().getChildren()) {
                        if(a instanceof Player)
                            ((Player)a).setCurAnim("Stand");
                    }
                    bgMoving = false;
                    encounterTime = 75;
                    remove();
                })));
            });
        });
    }
}

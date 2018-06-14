package com.sotfk;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.sotfk.Enemies.Bear;
import com.sotfk.Enemies.Lizard;

import static com.sotfk.PepSecret.textBox;

public abstract class EnemyEvent extends Event {
    private static final Event ONE_LIZARD = new Event() {
        @Override
        public void run(Group foreground) {
            Enemy en = new Lizard();
            foreground.addActor(en);
            textBox.setText("An enemy Lizard appeared!", false);
            textBox.setVisible(true);
        }

        @Override
        public boolean canRun(Player player) {
            return player.getLvl() <= 6;
        }
    };
    private static final Event ONE_BEAR = new Event() {
        @Override
        public void run(Group foreground) {
            Enemy en = new Bear();
            foreground.addActor(en);
            textBox.setText("An enemy Bear appeared!", false);
            textBox.setVisible(true);
        }

        @Override
        public boolean canRun(Player player) {
            return player.getLvl() <= 6;
        }
    };
    private static final Event TWO_LIZARDS = new Event() {
        @Override
        public void run(Group foreground) {
            Enemy en1 = new Lizard();
            en1.moveBy(0, 45);
            Enemy en2 = new Lizard();
            en2.moveBy(0, -45);
            foreground.addActor(en1);
            foreground.addActor(en2);
            textBox.setText("Two enemy Lizards appeared!", false);
            textBox.setVisible(true);
        }

        @Override
        public boolean canRun(Player player) {
            return false; //player.getLvl() >= 6 && player.getLvl() <= 50;
        }
    };

    public static final Event[] ENEMY_EVENTS = {
            ONE_LIZARD,
            ONE_BEAR,
            TWO_LIZARDS
    };
}

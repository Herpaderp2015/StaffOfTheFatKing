package com.sotfk.Enemies;

import com.sotfk.Enemy;

public class Lizard extends Enemy {
    public Lizard() {
        super("Lizard");
        players[0].addEnemy(1, this);
    }
}

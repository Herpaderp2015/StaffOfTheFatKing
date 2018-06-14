package com.sotfk.Enemies;

import com.sotfk.Enemy;

public class Bear extends Enemy {
    private final static int DEF_PER_SIZE[] = new int[] {25, 20, 15, 10, 5};
    private final static float LEVEL_DEF_PER_SIZE[] = new float[] {0.5f, 0.4f, 0.3f, 0.2f, 0.1f};

    public Bear() {
        super("Bear");
        foodDrop = 8;
        foodRatio = 0.8f;
        players[0].addEnemy(2, this);
    }

    @Override
    protected void onGrow() {
        setDef();
    }

    @Override
    protected void onShrink() {
        setDef();
    }

    private void setDef() {
        if(curSize < DEF_PER_SIZE.length)
            setStat("def", DEF_PER_SIZE[curSize] + (int)(players[0].getLvl() * LEVEL_DEF_PER_SIZE[curSize]));
        else
            setStat("def", DEF_PER_SIZE[4] + (int)(players[0].getLvl() * LEVEL_DEF_PER_SIZE[4]));
    }
}

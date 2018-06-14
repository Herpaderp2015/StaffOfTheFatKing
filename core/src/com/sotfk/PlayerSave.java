package com.sotfk;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Created by Jordan on 10/30/2017.
 */
public class PlayerSave
{
    public int lvl, food, xp, xpNeeded;
    public int atk, def, abi, spd, hp, maxHp;
    public String name;
    public int[] abiMenuData;
    public int[] statDist; // How many times a stat can be levelled, increasing by one per level if not at its max.
    public int availablePoints;
    public Color defBase;
    public Color defSecond;

    public void setToDefault() {
        lvl = 1;
        food = 500;
        xp = 0;
        xpNeeded = 12;
        atk = 3;
        def = 5;
        abi = 0;
        spd = 5;
        maxHp = 20;
        hp = maxHp;
        name = "Playerguy";
        abiMenuData = new int[] {0, 1};
        statDist = new int[5];
        availablePoints = 0;
        defBase = new Color(240 / 255f, 51 / 255f, 20 / 255f, 255 / 255f);
        defSecond = Color.WHITE;
    }

    public PlayerSave copy() {
        PlayerSave ps = new PlayerSave();
        ps.lvl = lvl;
        ps.food = food;
        ps.xp = xp;
        ps.xpNeeded = xpNeeded;
        ps.atk = atk;
        ps.def = def;
        ps.abi = abi;
        ps.spd = spd;
        ps.hp = hp;
        ps.maxHp = maxHp;
        ps.name = name;
        ps.abiMenuData = abiMenuData;
        ps.statDist = statDist;
        ps.availablePoints = availablePoints;
        ps.defBase = defBase;
        ps.defSecond = defSecond;
        return ps;
    }
}

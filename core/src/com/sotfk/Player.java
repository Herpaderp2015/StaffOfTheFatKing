package com.sotfk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;

import static com.sotfk.PepSecret.*;


public class Player extends Actor implements BattleEntity {
    private int lvl, size, targetSize, xp, xpNeeded;
    private int atk, def, abi, spd, hp, maxHp;
    private Label label;
    private CombatMenu combatMenu;
    private String doing;
    private int frames;
    private Runnable attackCommand;
    private boolean acting;
    private String name = "Playerguy";
    private Label hpLabel;
    private Group abiMenu, enMenu;
    private ArrayList<Integer> abiMenuData;
    private int[] statDist; // How many times a stat can be levelled, increasing by one per level if not at its max.
    private int availablePoints;
    private boolean waiting;
    private int foodTossed;
    private String[][] animDetails;
    private int[] animStartPoints;
    private String curAnim;
    private int curFrame, framesUntilChange;
    private int sprWidth, sprHeight;
    private int sprX, sprY, sprRegionX, sprRegionY, sprColOfs;
    private Runnable storedAction;
    private int blockCd;
    private int lvlUpAnim, curEmotion;
    private Color defBase = new Color(240 / 255f, 51 / 255f, 20 / 255f, 255 / 255f);
    private Color defSecond = Color.WHITE;

    public static int food, money;
    public static boolean selEnemy; // Whether or not it is time to select an enemy
    public static Enemy enemyTarget;
    private Sprite spr;
    private final ArrayList<Enemy> ENEMY_LIST = new ArrayList<>();
    private final Sprite PARTICLES = new Sprite((Texture) MANAGER.get("assets/Player/PlayerParticles.png"));
    private final String[] weightNames = new String[] {
            "a normal weight!", "fat!", "obese!", "morbidly obese!", "immobile...?", "The Staff Wielder!"
    };

    /*public Player(Skin skin, Group hud) {
        lvl = 1;
        size = 0;
        food = 500;
        xp = 0;
        xpNeeded = 12;
        atk = 3;
        def = 5;
        abi = 0;
        spd = 5;
        maxHp = 20;
        hp = maxHp;
        doing = "Stand";
        selEnemy = false;
        frames = 0;
        lvlUpAnim = -1;
        curEmotion = -1;
        targetSize = 0;
        attackCommand = null;
        acting = false;
        statDist = new int[5];
        hpLabel = new Label(Integer.toString(maxHp), skin);
        abiMenuData = new ArrayList<>();
        abiMenu = new Group();
        addAbility(0);
        addAbility(1);
        addAbility(2);
        addAbility(3);
        abiMenu.setVisible(false);
        enMenu = new Group() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                setPosition(Player.this.getX() + Player.this.getWidth() + 48, Player.this.getY());
                super.draw(batch, parentAlpha);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                setVisible(selEnemy);
            }
        };
        addEnemy(0, null);
        setBounds(128, 96, 72, 117);
        MANAGER.load("assets/Player/Male/size0.png", Texture.class);
        MANAGER.finishLoading();
        spr = new Sprite((Texture) MANAGER.get("assets/Player/Male/size0.png"));
        loadAnimData("assets/Player/Male/size0.txt");
        setCol();

        label = new Label("Level 1", skin);
        label.setPosition(0, Gdx.graphics.getHeight() - label.getHeight());
        hud.addActor(label);
        combatMenu = new CombatMenu();
        hud.addActor(combatMenu);
        hud.addActor(abiMenu);
        hud.addActor(enMenu);
    }*/

    public Player(PlayerSave ps, Skin skin, Group hud) {
        readPlayerSave(ps);
        doing = "Stand";
        selEnemy = false;
        frames = 0;
        targetSize = size;
        attackCommand = null;
        acting = false;
        hpLabel = new Label(Integer.toString(maxHp), skin);
        abiMenu.setVisible(false);
        enMenu = new Group() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                setPosition(Player.this.getX() + Player.this.getWidth() + 48, Player.this.getY());
                super.draw(batch, parentAlpha);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                setVisible(selEnemy);
            }
        };
        addEnemy(0, null);
        setBounds(128, 96, 72, 117);
        MANAGER.load("assets/Player/Male/size" + Integer.toString(size) + ".png", Texture.class);
        MANAGER.finishLoading();
        spr = new Sprite((Texture) MANAGER.get("assets/Player/Male/size" + Integer.toString(size) + ".png"));
        loadAnimData("assets/Player/Male/size" + Integer.toString(size) + ".txt");

        label = new Label("Level " + Integer.toString(lvl), skin);
        label.setPosition(0, Gdx.graphics.getHeight() - label.getHeight());
        hud.addActor(label);
        combatMenu = new CombatMenu();
        combatMenu.setVisible(false);
        hud.addActor(combatMenu);
        hud.addActor(abiMenu);
        hud.addActor(enMenu);

        lvlUpAnim = -1;
        curEmotion = -1;
    }

    public PlayerSave writePlayerSave() {
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
        ps.abiMenuData = new int[abiMenuData.size()];
        for(int i = 0; i < abiMenuData.size(); i++) {
            ps.abiMenuData[i] = abiMenuData.get(i);
        }
        ps.statDist = statDist;
        ps.availablePoints = availablePoints;
        ps.defBase = defBase;
        ps.defSecond = defSecond;
        return ps;
    }

    public void readPlayerSave(PlayerSave ps) {
        lvl = ps.lvl;
        size = ps.lvl / 10;
        food = ps.food;
        xp = ps.xp;
        xpNeeded = ps.xpNeeded;
        atk = ps.atk;
        def = ps.def;
        abi = ps.abi;
        spd = ps.spd;
        hp = ps.hp;
        maxHp = ps.maxHp;
        name = ps.name;
        abiMenuData = new ArrayList<>();
        abiMenu = new Group();
        for(int i : ps.abiMenuData)
            addAbility(i);
        statDist = ps.statDist;
        availablePoints = ps.availablePoints;
        defBase = ps.defBase;
        defSecond = ps.defSecond;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(lvlUpAnim > 0) {
            PARTICLES.setRegion(17, 33, 17, 39);
            PARTICLES.setSize(51, 117);
            PARTICLES.setPosition(getX() + 21 + (float)Math.pow(lvlUpAnim / 4f, 2), getY());
            PARTICLES.draw(batch);
        }

        //SPR.setRegion(48 * (size % 4), 46 * (int)Math.floor(size / 4f), 48, 46);
        spr.setRegion(sprRegionX, sprRegionY, sprWidth, sprHeight);
        spr.setSize(sprWidth * 3, sprHeight * 3);
        spr.setPosition(getX() + sprX, getY() + sprY);
        spr.setColor(defBase);
        spr.draw(batch, parentAlpha);
        spr.setRegion(sprRegionX + sprColOfs, sprRegionY, sprWidth, sprHeight);
        spr.setSize(sprWidth * 3, sprHeight * 3);
        spr.setPosition(getX() + sprX, getY() + sprY);
        spr.setColor(defSecond);
        spr.draw(batch, parentAlpha);

        if(lvlUpAnim > 0) {
            PARTICLES.setRegion(0, 33, 17, 39);
            PARTICLES.setSize(51, 117);
            PARTICLES.setPosition(getX() - (float)Math.pow(lvlUpAnim / 4f, 2), getY());
            PARTICLES.draw(batch);
        }

        if(curEmotion == -1) {
            PARTICLES.setRegion(0, 0, 32, 9);
            PARTICLES.setSize(96, 27);
            PARTICLES.setPosition(getX() + ((getWidth() - 96) / 2f), getY() + getHeight() + 27);
            PARTICLES.draw(batch);

            int height = 8 + (int)(Math.round(Math.sin(frames / 8f)));
            PARTICLES.setRegion(15 - ((frames / 4) % 15), 15, 3 * (int)(hp / (float)maxHp * 30), height);
            PARTICLES.setSize(3 * (int)(hp / (float)maxHp * 30), height);
            PARTICLES.setPosition(getX() + ((getWidth() - 96) / 2f) + 3, getY() + getHeight() + 42);
            PARTICLES.draw(batch);

            hpLabel.setText(Integer.toString(hp) + " / " + Integer.toString(maxHp));
            hpLabel.setPosition(getX() + ((getWidth() - hpLabel.getPrefWidth()) / 2), getY() + getHeight() + 54);
            hpLabel.draw(batch, parentAlpha);

            PARTICLES.setRegion(((frames / 4) % 15), 24, 90, height);
            PARTICLES.setSize(90, height);
            PARTICLES.setPosition(getX() + ((getWidth() - 96) / 2f) + 3, getY() + getHeight() + 30);
            PARTICLES.draw(batch);
        } else if (curEmotion > -1) {
            PARTICLES.setRegion(41 + (15 * curEmotion), 33, 15, 11);
            PARTICLES.setSize(45, 33);
            PARTICLES.setPosition(getX() + 66, getY() + getHeight() - 18);
            PARTICLES.draw(batch);
        }

        frames++;
        framesUntilChange--;
        if(framesUntilChange == 0) {
            nextAnimFrame();
        }
        if(blockCd > 0)
            blockCd--;
        if(lvlUpAnim > -1) {
            lvlUpAnim++;
            if(lvlUpAnim > 20) {
                lvlUpAnim = -1;
                curEmotion = -1;
                textBox.setVisible(false);
                if(targetSize != size) {
                    Array<Actor> actors = new Array(getParent().getChildren());
                    for(Actor a: actors)
                        if(a instanceof Enemy)
                            getParent().removeActor(a);
                    setCurAnim("GainStart");
                    curEmotion = 0;
                    storedAction = () -> {
                        size++;
                        String sizeStr = Integer.toString(size);
                        String oldSizeStr = Integer.toString(size - 1);
                        MANAGER.unload("assets/Player/Male/size" + oldSizeStr + ".png");
                        loadAnimData("assets/Player/Male/size" + sizeStr + ".txt");
                        String spritesheet = "assets/Player/Male/size" + sizeStr + ".png";
                        MANAGER.load(spritesheet, Texture.class);
                        MANAGER.finishLoadingAsset(spritesheet);
                        spr.setTexture(MANAGER.get(spritesheet));
                        if(size == targetSize) {
                            setCurAnim("Wait");
                            curEmotion = 3;
                            Timer.schedule(new Timer.Task() {
                                @Override
                                public void run() {
                                    curEmotion = 2;
                                    textBox.setText(name + " has gained weight! They are now " + weightNames[size], true);
                                    textBox.setVisible(true);
                                    PepSecret.textBox.setTimer(1.5f, () -> {
                                        curEmotion = -1;
                                        setCurAnim("Stand");
                                    });
                                }
                            }, 0.5f);
                        } else {
                            setCurAnim("Gain");
                        }
                    };
                }
            }
        }

        combatMenu.setVisible(PepSecret.inBattle && !abiMenu.isVisible() && !selEnemy);
    }

    protected void loadAnimData(String fileName) {
        String fileSplit;
        String[] segments;
        try {
            String line;

            BufferedReader bufferedReader =
                    Gdx.files.internal(fileName).reader(8192);

            StringBuilder builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            bufferedReader.close();

            segments = fileSplit.split("\\s*\\r?\\n\\s*");
            animDetails = new String[segments.length][4];
            for(int i = 0; i < segments.length; i++) {
                animDetails[i] = segments[i].split(" ");
            }
            segments = new String[1];

            //Preliminary assembly
            ArrayList<Integer> animNums = new ArrayList<>();
            for(int i = 0; i < animDetails.length; i++) {
                switch (animDetails[i][0]) {
                    case "SprSize":
                        sprWidth = Integer.parseInt(animDetails[i][1]);
                        sprHeight = Integer.parseInt(animDetails[i][2]);
                        setSize(sprWidth * 3, sprHeight * 3);
                        break;
                    case "Anim":
                        animNums.add(i);
                        break;
                    case "ColOfs":
                        sprColOfs = Integer.parseInt(animDetails[i][1]);
                    case "Size":
                        //curSize = Integer.parseInt(animDetails[i][1]);
                        break;
                }
            }
            animStartPoints = new int[animNums.size()];
            for(int i = 0; i < animNums.size(); i++)
                animStartPoints[i] = animNums.get(i);

            setCurAnim("Stand");

        } catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }

    private void setCol() {
        String fileSplit;
        String[] segments;
        String fileName = "assets/Player/primaryCol.txt";
        try {
            String line;

            BufferedReader bufferedReader =
                    Gdx.files.internal(fileName).reader(8192);

            StringBuilder builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            bufferedReader.close();

            segments = fileSplit.split("\\s*\\r?\\n\\s*");
            int[] rgb = new int[] {Integer.parseInt(segments[0]), Integer.parseInt(segments[1]), Integer.parseInt(segments[2])};
            defBase = new Color(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f, 1);

            fileName = "assets/Player/secondaryCol.txt";
            line = new String();
            bufferedReader =
                    Gdx.files.internal(fileName).reader(8192);

            builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            bufferedReader.close();

            segments = fileSplit.split("\\s*\\r?\\n\\s*");
            rgb = new int[] {Integer.parseInt(segments[0]), Integer.parseInt(segments[1]), Integer.parseInt(segments[2])};
            defSecond = new Color(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f, 1);
        } catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }

    public void setCurAnim(String anim) {
        for(int i : animStartPoints)
            if(animDetails[i][1].equals(anim)) {
                curAnim = anim;
                curFrame = i;
                nextAnimFrame();
                return;
            }
        curAnim = animDetails[animStartPoints[0]][1];
        curFrame = animStartPoints[0];
        nextAnimFrame();
    }

    private boolean hasAnim(String anim) {
        for(int i : animStartPoints)
            if(animDetails[i][1].equals(anim)) {
                return true;
            }
        return false;
    }

    private void nextAnimFrame() {
        curFrame++;
        String[] cF = animDetails[curFrame];
        switch (cF[0]) {
            case "Frame": // Changes which sprite from the spritesheet is shown
                spr.setRegion(Integer.parseInt(cF[1]), Integer.parseInt(cF[2]), sprWidth, sprHeight);
                sprRegionX = Integer.parseInt(cF[1]);
                sprRegionY = Integer.parseInt(cF[2]);
                framesUntilChange = Integer.parseInt(cF[3]);
                break;
            case "SetAnim": // Changes currently playing animation
                setCurAnim(cF[1]);
                break;
            case "DoAction": // Does a predetermined action
                if(storedAction != null)
                    storedAction.run();
                nextAnimFrame();
                break;
            case "SetSprite": // Changes the spritesheet
                String spritesheet = "assets/Player/Male/" + cF[1];
                MANAGER.load(spritesheet, Texture.class);
                MANAGER.finishLoadingAsset(spritesheet);
                spr.setTexture(MANAGER.get(spritesheet));
                nextAnimFrame();
                break;
            case "LoadAnimData":
                loadAnimData("assets/Player/Male/" + cF[1]);
                break;
            case "SprMove":
                sprX = Integer.parseInt(cF[1]);
                sprY = Integer.parseInt(cF[2]);
                nextAnimFrame();
                break;
            case "Wait":
                framesUntilChange = Integer.parseInt(cF[1]);
                break;
            case "SetSize":
                sprWidth = Integer.parseInt(cF[1]);
                sprHeight = Integer.parseInt(cF[2]);
                setSize(sprWidth * 3, sprHeight * 3);
                nextAnimFrame();
                break;
            case "SetColOfs":
                sprColOfs = Integer.parseInt(cF[1]);
                nextAnimFrame();
                break;
            case "SetEmotion":
                curEmotion = Integer.parseInt(cF[1]);
                nextAnimFrame();
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(waiting) {
            switch (doing) {
                case "FoodToss":
                    boolean doneEating = true;
                    for(Actor a : getParent().getChildren())
                        if(a instanceof Enemy && ((Enemy)a).getEating())
                            doneEating = false;
                    if(doneEating) {
                        acting = false;
                        PepSecret.textBox.setVisible(false);
                        doing = "Stand";
                        waiting = false;
                    }
                    break;
            }
        }
    }

    public void levelUp() {
        if(lvl < 50) {
            curEmotion = -2;
            PepSecret.bgMoving = false;
            lvl++;
            xpNeeded += (lvl + 2) / 6;
            label.setText("Level " + lvl);
            if(lvl == 50) {
                xp = xpNeeded;
            }
            if((int)Math.floor(lvl / 10f) != size) {
                targetSize = (int)Math.floor(lvl / 10f);
                //availablePoints += 2;
            } else {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        setCurAnim("EatFinish");
                        storedAction = null;
                    }
                }, 11 * Gdx.graphics.getDeltaTime());
            }
            availablePoints += targetSize + 2;
            lvlUpAnim = 0;
            checkStats();
        }
    }

    public void eat(int amt) {
        if(lvl < 50) {
            setCurAnim("EatStart");
            textBox.setText(name + " ate " + amt + " food!", false);
            textBox.setVisible(true);
            curEmotion = -2;
            food -= amt;
            storedAction = () -> {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        xp += amt;
                        while(xp >= xpNeeded) {
                            xp -= xpNeeded;
                            levelUp();
                        }
                        textBox.setText(name + " has reached level " + Integer.toString(lvl) + "!", false);
                        textBox.setVisible(true);
                        if(lvlUpAnim == -1) {
                            setCurAnim("EatFinish");
                            storedAction = () -> {
                                curEmotion = -1;
                                textBox.setVisible(false);
                            };
                        }
                    }
                }, 10 * Gdx.graphics.getDeltaTime());
            };
        }
    }

    public void checkStats() {
        int oldHp = maxHp;
        maxHp = 20 + 3 * statDist[0];
        atk = 3 + statDist[1];
        def = 5 + statDist[2];
        abi = 0 + statDist[3];
        spd = 5 + statDist[4];
        hp += maxHp - oldHp;
    }

    public void addToStatDist(int stat, int amt) {
        if(availablePoints >= amt && statDist[stat] + amt > -1) {
            statDist[stat] += amt;
            availablePoints -= amt;
            checkStats();
        }
    }

    public void setStatDist(int[] dist) {
        statDist = dist;
    }

    public void clickDown(float cX, float cY) {
        switch (doing) {
            case "Attack":
                attackCommand = () -> {
                    PepSecret.textBox.setText(name + " hit " + enemyTarget.getName() + "!", true);
                    PepSecret.textBox.setVisible(true);
                    setCurAnim("Attack");
                    storedAction = () -> {
                        enemyTarget.takeDmg(atk);
                        enemyTarget.getAnim().setCurAnim("Hit");
                        PepSecret.textBox.setTimer(0.75f, () -> acting = false);
                    };
                };
                doing = "Stand";
                break;
            case "FoodToss":
                if(!waiting && food > 0 && foodTossed < 10) {
                    food--;
                    foodTossed++;
                    setCurAnim("FoodToss");
                    float dir = PepSecret.getDirection(getX() + getWidth() / 2, cX, getY() + getWidth() / 2, cY);
                    if(!MANAGER.isLoaded("food.png")) {
                        MANAGER.load("assets/food.png", Texture.class);
                        MANAGER.finishLoading();
                    }
                    final Sprite SPR = new Sprite((Texture)MANAGER.get("assets/food.png"));
                    SPR.setRegion(GENERATOR.nextInt(4) * 16, 0, 16, 16);
                    SPR.setSize(48, 48);
                    Actor food = new Actor() {
                        private final float SPD = 16;
                        private Rectangle foodRect = new Rectangle(0, 0, 48, 48);

                        @Override
                        public void draw(Batch batch, float parentAlpha) {
                            SPR.setPosition(getX(), getY());
                            SPR.draw(batch, parentAlpha);
                        }

                        @Override
                        public void act(float delta) {
                            double rad = dir;
                            moveBy((float)Math.cos(rad) * SPD, (float)Math.sin(rad) * SPD);
                            foodRect.setPosition(getX(), getY());
                            for(Actor a : getParent().getChildren())
                                if(a instanceof Enemy) {
                                    Rectangle enRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                                    if(!waiting && enRect.overlaps(foodRect) && ((Enemy)a).getCurSize() < ((Enemy)a).getStat("sizes")) {
                                        ((Enemy)a).eatFood();
                                        ((Enemy)a).getAnim().setCurAnim("Hit");
                                        remove();
                                        return;
                                    }
                                }
                            if(getX() > 768 || getX() < -48 || getY() > 768 || getY() < -48)
                                remove();
                        }
                    };
                    food.setBounds(getX() + getWidth() / 2 - 24, getY() + getWidth() / 2 - 24, 48, 48);
                    getParent().addActor(food);
                }
                break;
            case "Stand":
                if(!curAnim.equals("Hit") && blockCd == 0 && curActing > -1 && battleEntities.get(curActing) instanceof Enemy) {
                    setCurAnim("Block");
                    blockCd = 40;
                }

            case "Absorb":
                attackCommand = this::absorb;
                doing = "Stand";
                break;
            case "MagicMissile":
                attackCommand = this::magicMissile;
                doing = "Stand";
                break;
        }
    }

    public String getName() {
        return name;
    }

    public void addFood(int food) {
        this.food += food;
    }

    public int getFood() {
        return food;
    }

    public int getXp() {
        return xp;
    }

    public int getXpNeeded() {
        return xpNeeded;
    }

    public String getCurAnim() {
        return curAnim;
    }

    public int getLvl() { return lvl; }

    public int getCurEmotion() { return curEmotion; }

    public int getAvailablePoints() { return availablePoints; }

    public void setAvailablePoints(int points) { availablePoints = points; }

    public int[] getStatDist() { return statDist; }

    public void addAbility(int abiId) {
        if(!MANAGER.isLoaded("abilities.png")) {
            MANAGER.load("assets/abilities.png", Texture.class);
            MANAGER.finishLoading();
        }
        final Sprite ABI_SPRITE = new Sprite((Texture)MANAGER.get("assets/abilities.png"));
        ABI_SPRITE.setRegion(0, 12 * abiId, 96, 12);
        ABI_SPRITE.setSize(288, 36);
        Actor abi = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                ABI_SPRITE.setPosition(getX(), getY());
                ABI_SPRITE.draw(batch, parentAlpha);
            }
        };
        abi.setSize(288, 36);
        abi.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Player.this.doAbility(abiId);
            }
        });
        for(Actor a : abiMenu.getChildren())
            a.moveBy(0, 36);
        abiMenu.addActor(abi);
        abiMenuData.add(abiId);
    }

    private void doAbility(int abiId) {
        switch (abiId) {
            case 1: // Food Toss
                attackCommand = () -> {
                    doing = "FoodToss";
                    foodTossed = 0;
                    PepSecret.textBox.setText(name + " used Food Toss!", true);
                    PepSecret.textBox.setVisible(true);
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            waiting = true;
                            for(Actor a : getParent().getChildren())
                                if(a instanceof Enemy)
                                    ((Enemy)a).startEating();
                        }
                    }, 5f);
                };
                break;
            case 2:
                textBox.setText("Select an enemy to Absorb.", false);
                textBox.setVisible(true);
                selEnemy = true;
                doing = "Absorb";
                break;
            case 3:
                textBox.setText("Select an enemy to attack with Magic Missile.", false);
                textBox.setVisible(true);
                selEnemy = true;
                doing = "MagicMissile";
                break;
            default:
                break;
        }
        abiMenu.setVisible(false);
        combatMenu.setVisible(!selEnemy);
    }

    public void addEnemy(int enId, Enemy en) {
        if(!MANAGER.isLoaded("assets/Enemies/enList.png")) {
            MANAGER.load("assets/Enemies/enList.png", Texture.class);
            MANAGER.finishLoading();
        }
        final Sprite EN_SPRITE = new Sprite((Texture)MANAGER.get("assets/Enemies/enList.png"));
        EN_SPRITE.setRegion(0, 12 * enId, 96, 12);
        EN_SPRITE.setSize(288, 36);
        Actor enActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                EN_SPRITE.setPosition(getX(), getY());
                EN_SPRITE.draw(batch, parentAlpha);
            }
        };
        enActor.setSize(288, 36);
        enActor.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(en != null) {
                    enemyTarget = en;
                    Player.this.clickDown(en.getX(), en.getY());
                } else {
                    Player.this.combatMenu.setVisible(true);
                    textBox.setVisible(false);
                }
                selEnemy = false;
            }
        });
        for(Actor a : enMenu.getChildren())
            a.moveBy(0, 36);
        enMenu.addActor(enActor);
        if(en != null) {
            ENEMY_LIST.add(en);
        }
    }

    public void clearEnemies() {
        Actor cancel = enMenu.getChildren().get(0);
        cancel.moveBy(0, -36 * (enMenu.getChildren().size - 1));
        enMenu.clear();
        enMenu.addActor(cancel);
        ENEMY_LIST.clear();
    }

    public void removeEnemy(Enemy en) {
        int enNum = ENEMY_LIST.indexOf(en) + 1;
        for(int i = 0; i < enNum; i++) {
            enMenu.getChildren().get(i).moveBy(0, -36);
        }
        enMenu.removeActor(enMenu.getChildren().get(enNum));
        ENEMY_LIST.remove(en);
    }

    @Override
    public Runnable getCommand() {
        return attackCommand;
    }

    @Override
    public void setCommand(Runnable command) {
        attackCommand = command;
    }

    @Override
    public int getStat(String stat) {
        switch (stat) {
            case "atk":
                return atk;
            case "def":
                return def;
            case "spd":
                return spd;
            case "abi":
                return abi;
            case "hp":
                return hp;
            case "maxHp":
                return maxHp;
            default:
                return -1;
        }
    }

    @Override
    public void setStat(String stat, int amt) {
        switch (stat) {
            case "atk":
                atk = amt;
                break;
            case "def":
                atk = amt;
                break;
            case "spd":
                atk = amt;
                break;
            case "abi":
                atk = amt;
                break;
            case "hp":
                atk = amt;
                break;
            case "maxHp":
                atk = amt;
                break;
        }
    }

    @Override
    public boolean isActing() {
        return acting;
    }

    @Override
    public void setActing(boolean acting) {
        this.acting = acting;
    }

    @Override
    public void takeDmg(int dmgAmt) {
        boolean blocking = curAnim.equals("Block");
        int dmgTaken = (int)(dmgAmt * (1 - def / 75f));
        if(blocking)
            dmgTaken /= 3f;
        if(dmgTaken < 1)
            dmgTaken = 1;
        hp -= dmgTaken;
        if(hp <= 0)
            remove();
        if(blocking)
            setCurAnim("BlockHit");
        else
            setCurAnim("Hit");
    }

    @Override
    public int getCurHp() {
        return hp;
    }

    @Override
    public void heal(int amt) {
        hp += amt;
        if(hp > maxHp)
            hp = maxHp;
    }



    private class CombatMenu extends Actor {
        private Sprite spr;

        public CombatMenu() {
            super();
            spr = new Sprite((Texture)MANAGER.get("assets/menu.png"));
            spr.setRegion(352, 0, 96, 48);
            spr.setSize(288, 144);
            spr.setPosition(Player.this.getX() + Player.this.getWidth() + 48, Player.this.getY());
            setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());

            addCaptureListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int clickPos = 3 - (int)(y / 36f);
                    switch (clickPos) {
                        case 0:
                            textBox.setText("Select an enemy to attack.", false);
                            textBox.setVisible(true);
                            selEnemy = true;
                            doing = "Attack";
                            setVisible(false);
                            break;
                        case 1:
                            abiMenu.setVisible(true);
                            abiMenu.setPosition(getX(), getY());
                            CombatMenu.this.setVisible(false);
                            break;
                    }
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setPosition(Player.this.getX() + Player.this.getWidth() + 48, Player.this.getY());
            setPosition(spr.getX(), spr.getY());
            if(Player.this.attackCommand == null && PepSecret.getInBattle())
                spr.draw(batch);
        }
    }


    private void absorb() {
        PepSecret.textBox.setText(name + " used Abosrb on " + enemyTarget.getName() + "!", true);
        PepSecret.textBox.setVisible(true);
        setCurAnim("Absorb");
        storedAction = () -> {
            int dmg = 5 + abi;
            dmg *= 1 + (0.25f * enemyTarget.getCurSize());
            enemyTarget.takeDmg(dmg);
            heal((int)(0.6f * dmg));
            enemyTarget.shrink(true);
            PepSecret.textBox.setTimer(0.75f, () -> acting = false);
        };
    }

    private void magicMissile() {
        PepSecret.textBox.setText(name + " fired a missile at " + enemyTarget.getName() + "!", true);
        PepSecret.textBox.setVisible(true);
        setCurAnim("Attack");
        storedAction = () -> {
            int dmg = 1 + abi;
            enemyTarget.takeDmg(dmg);
            enemyTarget.getAnim().setCurAnim("Hit");
            PepSecret.textBox.setTimer(0.75f, () -> acting = false);
        };
    }
}

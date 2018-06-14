package com.sotfk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static com.sotfk.PepSecret.MANAGER;


public class Enemy extends Actor implements BattleEntity {
    protected HashMap<String, String> stats;
    private String[][] animDetails;
    private int[] animStartPoints;
    protected int hp, curSize;
    private String folderName; // For enemy name, use (String)stats.get("name");
    private String curAnim;
    private int curFrame, framesUntilChange;
    private int sprWidth, sprHeight;
    private Runnable storedAction, attackCommand;
    private int sprX, sprY;
    private boolean acting;
    protected final Random GENERATOR = new Random();
    protected int newFoodEaten, totalFoodEaten;
    protected boolean eating, shrunk;
    private Sprite healthBar;
    private boolean healthBarVisible;
    private float healthBar_Current, healthBar_Goal; // For the health bar animation
    protected int foodDrop, foodReturn; // foodDrop: food always dropped; foodRatio: amount of food eaten to return on death; foodReturn: food player has fed the enemy
    protected float foodRatio;
    private Anim anim;

    public static Player[] players;
    public static Enemy[] enemies;

    public Enemy(String enName) {
        String spritesheet = "assets/Enemies/" + enName + "/Size0.png";
        if(!MANAGER.isLoaded(spritesheet)) {
            MANAGER.load(spritesheet, Texture.class);
            MANAGER.load("assets/Enemies/" + enName + "/weightgain.png", Texture.class);
            MANAGER.finishLoadingAsset(spritesheet);
        }
        setPosition(768, 96);
        addAction(Actions.sequence(Actions.moveBy(-180, 0, 1f), Actions.run(() -> {
            if(!PepSecret.getInBattle())
                for(Actor a : getParent().getChildren()) {
                    if(a instanceof Player)
                        ((Player)a).setCurAnim("Stand");
                }
            PepSecret.setInBattle(true);
            PepSecret.textBox.setVisible(false);
        })));

        folderName = enName;
        acting = false;
        healthBarVisible = false;
        loadStats("assets/Enemies/" + enName + "/stats.txt");
        hp = Integer.parseInt(stats.get("maxHp"));
        anim = new Anim(this, "assets/Enemies/" + enName + "/size0.txt");
        anim.setStoredAction(null);
        healthBar = new Sprite((Texture)MANAGER.get("assets/healthbars.png"));
        healthBar.setRegion(0, 12, 24, 3);
        healthBar.setSize(72, 9);
        foodDrop = 5;
        foodRatio = 0.6f;

        addCaptureListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(Player.selEnemy) {
                    Player.enemyTarget = Enemy.this;
                    Player.selEnemy = false;
                    players[0].clickDown(x, y);
                }
            }
        });
    }

    private void loadStats(String fileName) {
        String fileSplit;
        String[] segments;
        stats = new HashMap<>();
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
            for(int i = 0; i < segments.length; i++) {
                String[] stat = segments[i].split(":");
                try {
                    if(!Objects.equals(stat[0], "metabolism") && !Objects.equals(stat[0], "sizes")) {
                        float amt = Integer.parseInt(stat[1]);
                        amt += 0.5 * (players[0].getLvl() - 1);
                        stat[1] = Integer.toString((int)amt);
                    }
                } catch (NumberFormatException | NullPointerException e) {

                }
                stats.put(stat[0], stat[1]);
            }

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

    /*protected void loadAnimData(String fileName) {
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
                    case "Size":
                        curSize = Integer.parseInt(animDetails[i][1]);
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
                String spritesheet = "assets/Enemies/" + folderName + "/" + cF[1];
                MANAGER.load(spritesheet, Texture.class);
                MANAGER.finishLoadingAsset(spritesheet);
                spr.setTexture(MANAGER.get(spritesheet));
                nextAnimFrame();
                break;
            case "LoadAnimData":
                loadAnimData("assets/Enemies/" + folderName + "/" + cF[1]);
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
        }
    }*/

    @Override
    public void draw(Batch batch, float parentAlpha) {
        anim.draw(batch, parentAlpha);
        anim.act();

        if(healthBarVisible) {
            healthBar.setPosition(getX() + (getWidth() - healthBar.getWidth()) / 2f, getY() + getHeight() + 6);
            healthBar.draw(batch);
            int mode = eating ? 12 : 9;
            if(mode == 9)
                healthBar.setRegion(0, 23, healthBar_Current % 22, 1);
            else
                healthBar.setRegion(0, 32, healthBar_Current % 22, 1);
            healthBar.setSize((healthBar_Current % 22) * 3, 3);
            healthBar.translate(3, 3);
            healthBar.draw(batch);
            healthBar.setRegion(0, mode, 24, 3);
            healthBar.setSize(72, 9);
            if(healthBar_Current < healthBar_Goal)
                healthBar_Current += 0.5f;
        }
    }

    public String getName() {
        return stats.get("name");
    }

    public boolean getEating() {
        return eating;
    }

    public int getCurSize() {
        return curSize;
    }

    public Anim getAnim() { return anim; }

    public void eatFood() {
        newFoodEaten++;
        foodReturn++;
    }

    public void startEating() {
        if(newFoodEaten > 0) {
            eating = true;
            healthBar.setRegion(0, 12, 24, 3);
            healthBar.setSize(72, 9);

            healthBarVisible = true;
            float res = 22 / (float)Integer.parseInt(stats.get("metabolism"));
            healthBar_Current = (totalFoodEaten % Integer.parseInt(stats.get("metabolism"))) * (int)res;
            healthBar_Goal = healthBar_Current + (int)res;
            float delaySeconds = 2 * res * Gdx.graphics.getDeltaTime();
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    newFoodEaten--;
                    totalFoodEaten++;
                    if(newFoodEaten == 0) {
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                healthBarVisible = false;
                                eating = false;
                            }
                        }, 14 * Gdx.graphics.getDeltaTime());
                    } else {
                        healthBar_Current = (totalFoodEaten % Integer.parseInt(stats.get("metabolism"))) * (int)res;
                        healthBar_Goal = healthBar_Current + (int)res;
                        Timer.schedule(this, delaySeconds);
                    }

                    if(totalFoodEaten % Integer.parseInt(stats.get("metabolism")) == 0) {
                        anim.setCurAnim("Grow");
                        curSize++;
                        onGrow();
                        if(curSize > getStat("sizes"))
                            curSize = getStat("sizes");
                    }
                }
            }, delaySeconds);
        }
    }

    protected void onGrow() {

    }

    protected void onShrink() {

    }

    public void shrink(boolean loseFood) {
        if(curSize > 0) {
            String size = "assets/Enemies/" + folderName + "/size" + Integer.toString(curSize - 1);
            if(!MANAGER.isLoaded(size + ".png")) {
                MANAGER.load(size + ".png", Texture.class);
                MANAGER.finishLoading();
            }
            anim.loadAnimData(size + ".txt");
            if(loseFood)
                while(totalFoodEaten >= (curSize + 1) * getStat("metabolism"))
                    totalFoodEaten -= getStat("metabolism");
            shrunk = false;
            curSize--;
            onShrink();
        }
        anim.setCurAnim("Hit");
    }


    public static void setPlayers(Player[] players) {
        Enemy.players = players;
    }

    @Override
    public Runnable getCommand() {
        return attackCommand;
    }

    @Override
    public void setCommand(Runnable command) {
        boolean waitOnAttack = false;
        if(command == null) {
            if(shrunk && totalFoodEaten < getStat("sizes") * getStat("metabolism") && totalFoodEaten >= (getStat("sizes") - 1) * getStat("metabolism")) {
                shrink(false);
                waitOnAttack = true;
            }
            final boolean javaSucks = waitOnAttack;

            if(hp <= 0) {
                attackCommand = () -> {
                    acting = false;
                    attackCommand = null;
                };
            } else if(anim.hasAnim("Attack")) {
                attackCommand = () -> {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            final int TARGET_PLAYER = GENERATOR.nextInt(players.length);
                            anim.setCurAnim("Attack");
                            PepSecret.textBox.setText(getName() + " hit " + players[TARGET_PLAYER].getName() + "!", true);
                            PepSecret.textBox.setVisible(true);
                            anim.setStoredAction(() -> { // Runnableception
                                players[TARGET_PLAYER].takeDmg(Integer.parseInt(stats.get("atk")));
                                PepSecret.textBox.setTimer(0.75f, () -> {
                                    acting = false;
                                    attackCommand = null;
                                });
                            });
                            shrunk = false;
                        }
                    }, javaSucks ? 0.25f : 0);
                };
            } else
                attackCommand = () -> {
                    totalFoodEaten -= getStat("metabolism");
                    PepSecret.textBox.setText(getName() + " is too fat to move!", true);
                    PepSecret.textBox.setVisible(true);
                    if(totalFoodEaten < getStat("sizes") * getStat("metabolism") && totalFoodEaten >= (getStat("sizes") - 1) * getStat("metabolism"))
                        shrunk = true;
                    PepSecret.textBox.setTimer(0.75f, () -> {
                        acting = false;
                        attackCommand = null;
                    });
                };
        } else
            attackCommand = command;
    }

    @Override
    public int getStat(String stat) {
        return Integer.parseInt(stats.get(stat));
    }

    @Override
    public void setStat(String stat, int amt) {
        stats.put(stat, Integer.toString(amt));
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
        int actualDmgAmt = (int)(dmgAmt * (1 - Integer.parseInt(stats.get("def")) / 75f));
        if(actualDmgAmt < 1) {
            actualDmgAmt = 1;
        }
        hp -= actualDmgAmt;
        if(hp <= 0) {
            die();
        }
    }

    private void die() {
        for(Actor a : getParent().getChildren())
            if(a instanceof Player)
                ((Player)a).addFood(foodDrop + (int)(foodRatio * foodReturn));
        getParent().removeActor(this);
        for(Player player : players) {
            player.removeEnemy(this);
        }
    }

    @Override
    public int getCurHp() {
        return hp;
    }

    @Override
    public void heal(int amt) {
        hp += amt;
    }
}

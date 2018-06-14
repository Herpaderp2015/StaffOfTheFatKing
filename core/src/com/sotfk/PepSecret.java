package com.sotfk;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Timer;
import com.sotfk.Enemies.Bear;
import com.sotfk.Enemies.Lizard;

import java.util.ArrayList;
import java.util.Random;

import static com.sotfk.EnemyEvent.ENEMY_EVENTS;
import static com.sotfk.OtherEvent.OTHER_EVENTS;

public class PepSecret extends ApplicationAdapter implements InputProcessor {
	private Stage curStage, overworldStage;
	private Skin skin;
	private Player player;
	private Group background, foreground, hud;
    public static ArrayList<BattleEntity> battleEntities;
    public static int curActing;
    public static int encounterTime;
    private boolean rShiftDown;
    private int timeSinceNPC;

	public static boolean inBattle, inEvent, bgMoving;
	public static final Random GENERATOR = new Random();
    public static final AssetManager MANAGER = new AssetManager();
    public static TextBox textBox;
    public static Menu menu;
    public static Actor lvlBar;
	
	@Override
	public void create () {
	    overworldStage = new Stage() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(textBox.awaitingResponse) {
                    textBox.doResponse();
                    return true;
                } else {
                    if(curActing == -1)
                        return super.touchDown(screenX, screenY, pointer, button);
                    else {
                        player.clickDown(screenX, 768 - screenY);
                        return true;
                    }
                }
            }
        };
	    background = new Group();
	    foreground = new Group();
	    hud = new Group();
	    inBattle = false;
	    inEvent = false;
	    bgMoving = false;
	    curActing = -1;
	    encounterTime = 75;
	    timeSinceNPC = -1;
        MANAGER.load("assets/Player/PlayerParticles.png", Texture.class);
		MANAGER.load("assets/UISkin.json", Skin.class);
		MANAGER.load("assets/stage.png", Texture.class);
		MANAGER.load("assets/healthbars.png", Texture.class);
        MANAGER.load("assets/menu.png", Texture.class);
        MANAGER.load("assets/titlescreen.png", Texture.class);
        MANAGER.load("assets/titlemenu.png", Texture.class);
		MANAGER.finishLoading();
        Actor i = new Actor() {
            private Sprite bg = new Sprite((Texture)MANAGER.get("assets/stage.png"));
            private int scroll;

            @Override
            public void draw(Batch batch, float parentAlpha) {
                for(int offset = 0; offset <= 1536; offset += 768) {
                    bg.setRegion(0, 0, 256, 256);
                    bg.setSize(768, 768);
                    bg.setPosition(offset, 0);
                    bg.draw(batch, parentAlpha);
                    bg.setRegion(256, 256, 256, 256);
                    bg.setSize(768, 768);
                    bg.setPosition(offset - scroll / 2, 0);
                    bg.draw(batch, parentAlpha);
                    bg.setRegion(0, 256, 256, 256);
                    bg.setSize(768, 768);
                    bg.setPosition(offset - scroll, 0);
                    bg.draw(batch, parentAlpha);
                }
                if(!inBattle && !inEvent && bgMoving)
                    scroll += 3;
                if(scroll >= 1536)
                    scroll -= 1536;
            }
        };
        i.setSize(768, 768);
        background.addActor(i);
		skin = MANAGER.get("assets/UISkin.json");
        /*player = new Player(skin, hud);
        Enemy.setPlayers(new Player[] {player});
        foreground.addActor(player);*/
        lvlBar = new Actor() {
            private final Sprite SPR = new Sprite((Texture)MANAGER.get("assets/menu.png"));
            private int frames;

            @Override
            public void draw(Batch batch, float parentAlpha) {
                float x = (Gdx.graphics.getWidth() - 384) / 2f;
                SPR.setRegion(96, 36, 128, 16);
                SPR.setSize(384, 48);
                SPR.setPosition(x, 700);
                SPR.draw(batch);

                SPR.setRegion(128 - ((frames / 3) % 31), 68, (int)(player.getXp() / (float)player.getXpNeeded() * 128), 16);
                SPR.setSize(3 * (int)(player.getXp() / (float)player.getXpNeeded() * 128), 48);
                SPR.setPosition(x, 700);
                SPR.draw(batch);

                SPR.setRegion(96, 52, 128, 16);
                SPR.setSize(384, 48);
                SPR.setPosition(x, 700);
                SPR.draw(batch);

                frames++;
            }
        };
        hud.addActor(lvlBar);
        textBox = new TextBox();
        hud.addActor(textBox);
        menu = new Menu();
        overworldStage.addActor(background);
        overworldStage.addActor(foreground);
        overworldStage.addActor(hud);

        TitleStage titleStage = new TitleStage(this);

        curStage = titleStage;

        InputMultiplexer im = new InputMultiplexer(titleStage, this);
		Gdx.input.setInputProcessor(im);
		Gdx.graphics.setTitle("Staff of the Fat King");
	}


	@Override
	public void render () {
	    if(inBattle && battleEntities == null) {
	        battleEntities = new ArrayList<>();
	        for(Actor a : foreground.getChildren())
	            if(a instanceof BattleEntity)
	                battleEntities.add((BattleEntity)a);
        }
        if(curStage == overworldStage && player.getCommand() != null) {
	        act();
        }
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		curStage.act();
		curStage.draw();

		if(!inBattle && !inEvent && bgMoving && curStage == overworldStage) {
            encounterTime--;
            if(encounterTime == 0) {
                encounterTime = -1;
                boolean open = true;
                for(Actor a : foreground.getChildren())
                    if(a instanceof Enemy)
                        open = false;
                if(open) {
                    double doing = GENERATOR.nextDouble();
                    Event[] eventArray;
                    if((timeSinceNPC == -1 && doing < 1.35) || doing < 0.1 + (timeSinceNPC * 0.1)) {
                        eventArray = OTHER_EVENTS;
                        timeSinceNPC = 0;
                    } else {
                        eventArray = ENEMY_EVENTS;
                        timeSinceNPC++;
                    }
                    boolean usableEvent = false;
                    int enemyNum = 0;
                    while(!usableEvent) {
                        enemyNum = GENERATOR.nextInt(eventArray.length);
                        usableEvent = eventArray[enemyNum].canRun(player);
                    }
                    eventArray[enemyNum].run(foreground);
                }
            }
        }
        if(rShiftDown)
            player.levelUp();
	}
	
	@Override
	public void dispose () {
		overworldStage.dispose();
		MANAGER.dispose();
	}

	private void act() {
	    if(curActing == -1) {
            BattleEntity backup;
            boolean sorted = false;
            while(!sorted) {
                sorted = true;
                for(int i = 0; i < battleEntities.size() - 1; i++) {
                    if(battleEntities.get(i).getStat("spd") < battleEntities.get(i + 1).getStat("spd")) {
                        backup = battleEntities.get(i);
                        battleEntities.set(i, battleEntities.get(i + 1));
                        battleEntities.set(i + 1, backup);
                        sorted = false;
                    }
                }
            }
            curActing = 0;
            battleEntities.get(0).setActing(true);
            if(battleEntities.get(0).getCommand() == null)
                battleEntities.get(0).setCommand(null);
            battleEntities.get(0).getCommand().run();
        }

        if(!battleEntities.get(curActing).isActing()) {
	        curActing++;
	        if(curActing < battleEntities.size()) {
                BattleEntity bE = battleEntities.get(curActing);
                bE.setActing(true);
                if(bE.getCommand() == null)
                    bE.setCommand(null);
                bE.getCommand().run();
            } else {
	            player.setCommand(null);
	            curActing = -1;

	            boolean battleEnded = true;
	            for(BattleEntity battleEntity : battleEntities)
	                if(battleEntity instanceof Enemy && battleEntity.getCurHp() > 0)
	                    battleEnded = false;
	            if(battleEnded) {
	                inBattle = false;
	                battleEntities = null;
	                encounterTime = 75;
                    textBox.setText("All enemies were defeated!", false);
                    textBox.setVisible(true);
                    bgMoving = false;
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            textBox.setVisible(false);
                        }
                    }, 0.5f);
                    player.clearEnemies();
                }
            }
        }
    }

	public static void setInBattle(boolean inBattle) {
	    PepSecret.inBattle = inBattle;
    }

    public static boolean getInBattle() {
	    return PepSecret.inBattle;
    }

    public static float getDirection(float x1, float x2, float y1, float y2) {
        if(x1 - x2 == 0) { // Nobody likes dividing by zero
            if(y1 > y2) // Going straight down
                return 270;
            else // Going straight up
                return 90;
        }
        float dir = (float)Math.atan((y1 - y2) / (x1 - x2));
        if(x1 >= x2)
            dir += Math.PI;
        return dir;
    }

    public void begin(PlayerSave ps) {
	    player = new Player(ps, skin, hud);
        Enemy.setPlayers(new Player[] {player});
        foreground.addActor(player);
	    curStage = overworldStage;
	    inBattle = false;
	    inEvent = false;
        InputMultiplexer im = new InputMultiplexer(overworldStage, this);
        Gdx.input.setInputProcessor(im);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.SPACE:
                overworldStage.touchDown(Gdx.input.getX(), Gdx.input.getY(), 0, Input.Buttons.LEFT);
                break;
            case Input.Keys.F:
                player.addFood(100);
                break;
            case Input.Keys.SHIFT_RIGHT:
                if(!inBattle && !inEvent && !bgMoving && player.getCurEmotion() < 0)
                    for(int i = 0; i < 10; i++)
                        player.levelUp();
                break;
            case Input.Keys.ALT_RIGHT:
                if(!inBattle && !inEvent && !bgMoving &&  player.getCurEmotion() < 0)
                    player.levelUp();
                break;
            case Input.Keys.H:
                player.heal(player.getStat("maxHp"));
                break;
            case Input.Keys.S:
            {
                PlayerSave ps = player.writePlayerSave();
                new Json(JsonWriter.OutputType.json).toJson(ps, PlayerSave.class, Gdx.files.local("SaveData/Save" + TitleStage.saveFileSelected + ".json"));
            }
            break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
	    switch (keycode) {
            case Input.Keys.SHIFT_RIGHT:
                rShiftDown = false;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }



    public class TextBox extends Actor{
	    private Sprite spr;
	    private Label text;
	    private boolean slowScroll;
	    private int scrollTimer;
	    private String textStr;
	    private float finishDur, width;
	    private Runnable finishExtra;
	    private boolean awaitingResponse;

        public TextBox() {
            super();
            spr = new Sprite((Texture)MANAGER.get("assets/menu.png"));
            spr.setRegion(0, 144, 256, 64);
            spr.setSize(768, 192);
            spr.setPosition(0, 576);
            text = new Label("", skin) {
                @Override
                public void setText(CharSequence newText) {
                    String[] words = newText.toString().split(" ");
                    StringBuilder current = new StringBuilder();
                    StringBuilder next = new StringBuilder();
                    for(String word : words) {
                        next.append(word);
                        super.setText(next);
                        if(getPrefWidth() > 725) {
                            current.append('\n');
                            next.append('\n');
                        }
                        current.append(word);
                        current.append(' ');
                        next.append(' ');
                    }
                    super.setText(current);
                }
            };
            setVisible(false);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.draw(batch);
            text.setPosition((spr.getWidth() - width) / 2f, 576 + (192 - text.getHeight()) / 2f);
            text.draw(batch, parentAlpha);
            if(slowScroll) {
                text.setText(textStr.substring(0, scrollTimer));
                if(scrollTimer < textStr.length()) {
                    scrollTimer += 2;
                    if(scrollTimer >= textStr.length()) {
                        scrollTimer = textStr.length();
                    }
                } else if(finishDur > -1) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            setVisible(false);
                            finishExtra.run();
                        }
                    }, finishDur);
                    finishDur = -1;
                }
            }
        }

        public void setText(String textStr, boolean slow) {
            slowScroll = slow;
            scrollTimer = 1;
            finishDur = -1;
            text.setText(textStr);
            width = text.getPrefWidth();
            this.textStr = text.getText().toString(); // This is done to accommodate any added newlines
            if(slow) {
                text.setText(textStr.substring(0, 1));
            }
        }

        public void setTimer(float duration, Runnable extra) {
            if(!slowScroll) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        setVisible(false);
                        extra.run();
                    }
                }, duration);
            } else {
                finishDur = duration;
                finishExtra = extra;
            }
        }

        public void awaitResponse(Runnable afterResponse) {
            finishExtra = afterResponse;
            awaitingResponse = true;
        }

        public void doResponse() {
            awaitingResponse = false;
            finishExtra.run();
        }
    }

    public class Menu extends Actor{
        private Sprite spr;
        private int subMenu;
        private int eatAmt;
        private int[] statSto;
        private int pointSto;
        private float sprAlpha;
        private final Actor foodMenu = new Actor() {
            private Sprite spr;
            private Label label;

            @Override
            public void draw(Batch batch, float parentAlpha) {
                if(spr == null) {
                    spr = new Sprite((Texture)MANAGER.get("assets/menu.png"));
                    setBounds(Menu.this.getX(), Menu.this.getY(), 360, 72);
                    label = new Label("1 / " + Integer.toString(player.getFood()), skin);
                }
                if(player.getFood() > 0) {
                    if(eatAmt < 1)
                        eatAmt = 1;
                    spr.setRegion(96, 0, 120, 24);
                    spr.setSize(360, 72);
                    spr.setPosition(getX(), getY());
                    spr.draw(batch);
                    spr.setRegion(147, 25, 6, 6);
                    spr.setSize(18, 18);
                    float cursorX = getX() + 9 + (327 * eatAmt / (float)player.getFood());
                    spr.setPosition(cursorX, getY() + 51);
                    spr.draw(batch);

                    spr.setRegion(156, 24, 12, 12);
                    label.setText(Integer.toString(eatAmt) + " / " + Integer.toString(player.getFood()));
                    label.setPosition(cursorX + (18 - label.getPrefWidth()) / 2f,getY() + 70);
                    spr.setSize(label.getPrefWidth() + 2, label.getPrefHeight() + 2);
                    spr.setPosition(label.getX(), getY() + 69);
                    spr.draw(batch);
                    label.draw(batch, parentAlpha);
                } else {
                    setVisible(false);
                    subMenu = -1;
                }
            }
        };
        private final Actor lvlUpMenu = new Actor() {
            private Sprite spr;
            private Label label;

            @Override
            public void draw(Batch batch, float parentAlpha) {
                if(spr == null) {
                    spr = new Sprite((Texture)MANAGER.get("assets/menu.png"));
                    setBounds(Menu.this.getX(), Menu.this.getY(), 243, 168);
                    label = new Label(Integer.toString(player.getAvailablePoints()), skin);
                }
                setPosition(Menu.this.getX(), Menu.this.getY());
                spr.setRegion(96, 84, 81, 56);
                spr.setSize(243, 168);
                spr.setPosition(getX(), getY());
                spr.draw(batch, parentAlpha);

                label.setPosition(getX() + 180, getY() + 39);
                label.setText(Integer.toString(player.getAvailablePoints()));
                label.draw(batch, parentAlpha);

                for(int i = 0; i < 5; i++) {
                    int yOfs = 135 - 33 * i;
                    label.setPosition(getX() + 54, getY() + yOfs);
                    label.setText(Integer.toString(player.getStatDist()[i]));
                    label.draw(batch, parentAlpha);
                }
            }
        };

        public Menu() {
            super();
            spr = new Sprite((Texture)MANAGER.get("assets/menu.png"));
            spr.setRegion(256, 0, 96, 72);
            spr.setSize(288, 216);
            //spr.setPosition(player.getX() + player.getWidth() + 48, player.getY());
            setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
            subMenu = -1;
            statSto = new int[5];
            addCaptureListener(new ClickListener(Input.Buttons.LEFT) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(player.getCurEmotion() != -1)
                        return;
                    subMenu = 5 - ((int)y / 36);
                    switch (subMenu) {
                        case 3:
                            if(player.getFood() <= 0)
                                subMenu = -1;
                            break;
                        case 4:
                            statSto = player.getStatDist().clone();
                            pointSto = player.getAvailablePoints();
                            break;
                        case 5:
                            subMenu = -1;
                            bgMoving = true;
                            player.setCurAnim("Walk");
                            break;
                        default:
                            break;
                    }
                }
            });
            foodMenu.addCaptureListener(new ClickListener(Input.Buttons.LEFT) {
                boolean drag;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    drag = false;
                    boolean ret = false;
                    if(y < 36) {
                        int clickArea = (int)x / 36;
                        int stoLvl = player.getLvl();
                        switch(clickArea) {
                            case 1:
                                eatAmt -= 100;
                                break;
                            case 2:
                                eatAmt -= 10;
                                break;
                            case 3:
                                eatAmt--;
                                break;
                            case 5:
                                player.eat(eatAmt);
                            case 4:
                                subMenu = -1;
                                eatAmt = 1;
                                if(player.getLvl() != stoLvl) {
                                    subMenu = 4;
                                    statSto = player.getStatDist().clone();
                                    pointSto = player.getAvailablePoints();
                                }
                                foodMenu.setVisible(false);
                                break;
                            case 6:
                                eatAmt++;
                                break;
                            case 7:
                                eatAmt += 10;
                                break;
                            case 8:
                                eatAmt += 100;
                                break;
                        }
                        ret = true;
                    } else if (y > 51 && y < 60 && x > 12 && x < 348) {
                        eatAmt = (int)((x - 16f) * player.getFood() / 328f);
                        drag = true;
                        ret = true;
                    }
                    if(eatAmt > player.getFood())
                        eatAmt = player.getFood();
                    if(eatAmt < 1 && player.getFood() >= 1)
                        eatAmt = 1;
                    return ret;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if(drag) {
                        eatAmt = (int)((x - 16f) * player.getFood() / 328f);
                        if(eatAmt > player.getFood())
                            eatAmt = player.getFood();
                        if(eatAmt < 1 && player.getFood() >= 1)
                            eatAmt = 1;
                    }
                }
            });
            lvlUpMenu.addCaptureListener(new ClickListener(Input.Buttons.LEFT) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(x > 171 && y < 36) {
                        if(x < 207) {
                            for(int i = 0; i < 5; i++)
                                player.setStatDist(statSto);
                            player.setAvailablePoints(pointSto);
                        }
                        subMenu = -1;
                        lvlUpMenu.setVisible(false);
                    }
                    if(x >= 141 && x < 162) {
                        float ySec = y % 33;
                        if(ySec >= 6 && ySec < 27) {
                            player.addToStatDist(4 - (int)Math.floor(y / 33f), 1);
                        }
                    }
                    if(x >= 114 && x < 135) {
                        float ySec = y % 33;
                        if(ySec >= 6 && ySec < 27) {
                            player.addToStatDist(4 - (int)Math.floor(y / 33f), -1);
                        }
                    }
                }
            });
            lvlUpMenu.addCaptureListener(new ClickListener(Input.Buttons.RIGHT) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(x > 171 && y < 36) {
                        if(x < 207) {
                            for(int i = 0; i < 5; i++)
                                player.setStatDist(statSto);
                            player.setAvailablePoints(pointSto);
                        }
                        subMenu = -1;
                        lvlUpMenu.setVisible(false);
                    }
                    if(x >= 141 && x < 162) {
                        float ySec = y % 33;
                        if(ySec >= 6 && ySec < 27) {
                            int amt = 10;
                            if(player.getAvailablePoints() < 10)
                                amt = player.getAvailablePoints();
                            player.addToStatDist(4 - (int)Math.floor(y / 33f), amt);
                        }
                    }
                    if(x >= 114 && x < 135) {
                        float ySec = y % 33;
                        if(ySec >= 6 && ySec < 27) {
                            int amt = -10;
                            int stat = 4 - (int)Math.floor(y / 33f);
                            if(player.getStatDist()[stat] < 10)
                                amt = -1 * player.getStatDist()[stat];
                            player.addToStatDist(stat, amt);
                        }
                    }
                }
            });
            foodMenu.setVisible(false);
            lvlUpMenu.setVisible(false);

            hud.addActor(this);
            hud.addActor(foodMenu);
            hud.addActor(lvlUpMenu);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setPosition(player.getX() + player.getWidth() + 48, player.getY());
            setPosition(spr.getX(), spr.getY());
            foodMenu.setVisible(false);
            lvlUpMenu.setVisible(false);
            sprAlpha = 0.5f;
            if(player.getCurEmotion() != -1)
                return;
            switch (subMenu) {
                case 3:
                    foodMenu.setVisible(true);
                    break;
                case 4:
                    lvlUpMenu.setVisible(true);
                    break;
                default:
                    sprAlpha = 1f;
                    break;
            }
            spr.draw(batch, sprAlpha);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            setVisible(!inEvent && !inBattle && !bgMoving);
            if(inBattle || inEvent) {
                foodMenu.setVisible(false);
                lvlUpMenu.setVisible(false);
                subMenu = -1;
            }
        }
    }
}

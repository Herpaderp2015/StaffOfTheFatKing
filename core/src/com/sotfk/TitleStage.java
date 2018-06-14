package com.sotfk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import static com.sotfk.PepSecret.MANAGER;

public class TitleStage extends Stage {
    private Image titleScreen;
    private SaveFile saveFile1, saveFile2;
    private CharacterCreator characterCreator;
    private PepSecret pepSecret;
    private TextField nameField;
    private Warning warning;

    private final int[] weightXOfs = {0, 24, 48, 74, 102, 132, 176, 202};

    public static int saveFileSelected;

    public TitleStage(PepSecret pepSecret) {
        super();
        titleScreen = new Image((Texture)MANAGER.get("assets/titlescreen.png"));
        titleScreen.setSize(768, 768);
        warning = new Warning();
        saveFile1 = new SaveFile(1);
        saveFile1.setPosition(96, 440);
        saveFile2 = new SaveFile(2);
        saveFile2.setPosition(96, 184);

        characterCreator = new CharacterCreator();
        nameField = new TextField("Playerguy", (Skin)MANAGER.get("assets/UISkin.json")) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                setPosition(characterCreator.getX() + 48, characterCreator.getY() + 396);
                super.draw(batch, parentAlpha);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                setVisible(characterCreator.isVisible());
            }
        };

        this.pepSecret = pepSecret;
        addActor(titleScreen);
        addActor(saveFile1);
        addActor(saveFile2);
        addActor(characterCreator);
        addActor(nameField);
        addActor(warning);
    }


    private class SaveFile extends Actor {
        private Sprite spr;
        private int size;
        private Color baseCol;
        private Color secondCol;
        private Label label;
        private PlayerSave ps;

        private final Color defBase = new Color(240 / 255f, 51 / 255f, 20 / 255f, 255 / 255f);
        private final Color defSecond = Color.WHITE;

        public SaveFile(int fileNum) {
            Json json = new Json();
            FileHandle file = Gdx.files.internal("SaveData/Save" + Integer.toString(fileNum) + ".json");
            if(file.exists()) {
                setPs(json.fromJson(PlayerSave.class, file));
            } else {
                setPs(null);
            }
            /*if(file.exists()) {
                ps = json.fromJson(PlayerSave.class, file);
                size = ps.lvl / 10;
                baseCol = ps.defBase;
                secondCol = ps.defSecond;
            } else {
                size = 6;
                baseCol = defBase;
                secondCol = defSecond;
            }*/
            spr = new Sprite((Texture)MANAGER.get("assets/titlemenu.png"));
            label = new Label((ps != null) ? ps.name : "NEW PLAYER", (Skin)MANAGER.get("assets/UISkin.json"));
            setSize(576, 144);
            addCaptureListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    boolean correctY = y > 6 && y < 33;
                    boolean startButton = x > 30 && x < 108;
                    if(correctY && !characterCreator.isVisible() && !warning.isVisible()) {
                        if(ps == null && startButton) {
                            saveFileSelected = fileNum;
                            PlayerSave newPs = new PlayerSave();
                            newPs.setToDefault();
                            characterCreator.init(newPs);
                        } else {
                            if(startButton) {
                                saveFileSelected = fileNum;
                                pepSecret.begin(ps);
                            } else if(x > 138 && x < 201) {
                                saveFileSelected = fileNum;
                                characterCreator.init(ps);
                            } else if(x > 231 && x < 297) {
                                warning.setOnAccept(() -> {
                                    int saveFileOverwrite = fileNum == 1 ? 2 : 1;
                                    new Json(JsonWriter.OutputType.json).toJson(ps, PlayerSave.class, Gdx.files.local("SaveData/Save" + saveFileOverwrite + ".json"));
                                    if(saveFileOverwrite == 1)
                                        saveFile1.setPs(ps.copy());
                                    else
                                        saveFile2.setPs(ps.copy());
                                });
                            } else if(x > 327 && x < 420) {
                                warning.setOnAccept(() -> {
                                    Gdx.files.local("SaveData/Save" + fileNum + ".json").delete();
                                    setPs(null);
                                });
                            }
                        }
                    }
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setRegion(0, (size == 6) ? 48 : 0, 192, 48);
            spr.setSize(576, 144);
            spr.setPosition(getX(), getY());
            spr.setColor(Color.WHITE);
            spr.draw(batch, parentAlpha);

            int width = weightXOfs[size + 1] - weightXOfs[size];
            spr.setRegion(weightXOfs[size], 96, width, 43);
            spr.setSize(width * 3, 129);
            spr.setPosition(getX() + 450 + ((3f / 2f) * (40 - width)), getY());
            spr.setColor(baseCol);
            spr.draw(batch, parentAlpha);

            spr.setRegion(weightXOfs[size], 139, width, 43);
            spr.setSize(width * 3, 129);
            spr.setPosition(getX() + 450 + ((3f / 2f) * (40 - width)), getY());
            spr.setColor(secondCol);
            spr.draw(batch, parentAlpha);

            label.setText(ps != null ? ps.name : "NEW FILE");
            label.setPosition(getX() + 15, getY() + getHeight() - label.getPrefHeight() - 15);
            label.draw(batch, parentAlpha);

            if(ps != null) {
                label.setText("level " + Integer.toString(ps.lvl) + ", " + Integer.toString((int)(100 + Math.pow(ps.lvl, 7 / 4.0))) + " lbs");
                label.moveBy(0, -label.getPrefHeight());
                label.draw(batch, parentAlpha);
            }
        }

        public void setPs(PlayerSave playerSave) {
            ps = playerSave;
            if(ps != null) {
                size = ps.lvl / 10;
                baseCol = ps.defBase;
                secondCol = ps.defSecond;
            } else {
                size = 6;
                baseCol = defBase;
                secondCol = defSecond;
            }
        }
    }

    private class CharacterCreator extends Actor {
        private Sprite spr;
        private PlayerSave ps, colPs;
        private int numDragged;
        private Label label;

        private final int[] yMin = { // Numbers y must be greater than
                234,
                201,
                168,
                96,
                63,
                30
        }; // Add 9 to these for the numbers y must be less than

        public CharacterCreator() {
            spr = new Sprite((Texture)MANAGER.get("assets/titlemenu.png"));
            label = new Label("", (Skin)MANAGER.get("assets/UISkin.json"));
            numDragged = -1;
            setVisible(false);
            addCaptureListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    numDragged = -1;
                    if(x > 393 && x < 456 && y > 84 && y < 111) {
                        SaveFile sf;
                        switch (saveFileSelected) {
                            default: // Intentionally blank
                            case 1:
                                sf = saveFile1;
                                break;
                            case 2:
                                sf = saveFile2;
                                break;
                        }
                        ps.name = nameField.getText();
                        ps.defBase = colPs.defBase;
                        ps.defSecond = colPs.defSecond;
                        sf.setPs(ps);
                        new Json(JsonWriter.OutputType.json).toJson(ps, PlayerSave.class, Gdx.files.local("SaveData/Save" + saveFileSelected + ".json"));
                        setVisible(false);
                    } else if(x > 378 && x < 471 && y > 33 && y < 60) {
                        setVisible(false);
                    } else if(x > 60 && y < 321) {
                        for(int i = 0; i < 6; i++) {
                            if(y >= yMin[i] && y <= yMin[i] + 9)
                                numDragged = i;
                        }
                        if(numDragged != -1)
                            setCol(x);
                    }
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if(numDragged != -1) {
                        setCol(x);
                    }
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setRegion(0, 182, 169, 165);
            spr.setSize(spr.getRegionWidth() * 3, spr.getRegionHeight() * 3);
            spr.setPosition(384 - spr.getWidth() / 2, 384 - spr.getHeight() / 2);
            setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
            spr.setColor(Color.WHITE);
            spr.draw(batch, parentAlpha);

            int size = ps.lvl / 10;
            int width = weightXOfs[size + 1] - weightXOfs[size];
            spr.setRegion(weightXOfs[size], 96, width, 43);
            spr.setSize(width * 3, 129);
            spr.setPosition(getX() + 354 + ((3f / 2f) * (40 - width)), getY() + 303);
            spr.setColor(colPs.defBase);
            spr.draw(batch, parentAlpha);

            spr.setRegion(weightXOfs[size], 139, width, 43);
            spr.setSize(width * 3, 129);
            spr.setPosition(getX() + 354 + ((3f / 2f) * (40 - width)), getY() + 303);
            spr.setColor(colPs.defSecond);
            spr.draw(batch, parentAlpha);

            for(int i = 0; i < 6; i++) {
                float value = 0;
                switch (i) {
                    case 0:
                        value = colPs.defBase.r;
                        break;
                    case 1:
                        value = colPs.defBase.g;
                        break;
                    case 2:
                        value = colPs.defBase.b;
                        break;
                    case 3:
                        value = colPs.defSecond.r;
                        break;
                    case 4:
                        value = colPs.defSecond.g;
                        break;
                    case 5:
                        value = colPs.defSecond.b;
                        break;
                }
                value *= 255;

                spr.setColor(Color.WHITE);
                spr.setRegion(170 + ( 4 * (i % 3)), 182, 3, 3);
                spr.setSize(9, 9);
                spr.setPosition(getX() + 59 + (int)value, getY() + yMin[i]);
                spr.draw(batch, parentAlpha);

                label.setText(Integer.toString((int)value));
                label.setPosition(getX() + (60 - label.getPrefWidth()) / 2f, getY() + yMin[i]);
                label.draw(batch, parentAlpha);
            }
        }

        public void init(PlayerSave ps) {
            this.ps = ps;
            nameField.setText(ps.name);
            colPs = new PlayerSave();
            colPs.defBase = new Color(ps.defBase);
            colPs.defSecond = new Color(ps.defSecond);
            setVisible(true);
        }

        private void setCol(float x) {
            float value = x - 63;
            if(value < 0)
                value = 0;
            if(value > 255)
                value = 255;
            switch (numDragged) {
                case 0:
                    colPs.defBase.r = value / 255;
                    break;
                case 1:
                    colPs.defBase.g = value / 255;
                    break;
                case 2:
                    colPs.defBase.b = value / 255;
                    break;
                case 3:
                    colPs.defSecond.r = value / 255;
                    break;
                case 4:
                    colPs.defSecond.g = value / 255;
                    break;
                case 5:
                    colPs.defSecond.b = value / 255;
                    break;
                default:
                    Gdx.app.log("ERROR", "setCol tried to set a nonexistant color");
            }
        }
    }

    private class Warning extends Actor {
        private Sprite spr;
        private Runnable onAccept;

        public Warning() {
            spr = new Sprite((Texture)MANAGER.get("assets/titlemenu.png"));
            setVisible(false);

            addCaptureListener(new ClickListener(Input.Buttons.LEFT) {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    boolean yDown = y > 9 && y < 36;
                    if(yDown) {
                        if(x > 69 && x < 120) {
                            onAccept.run();
                            close();
                        } else if(x > 141 && x < 192) {
                            close();
                        }
                    }
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setRegion(169, 194, 87, 57);
            spr.setSize(261, 171);
            spr.setPosition(384 - spr.getWidth() / 2f, 384 - spr.getHeight() / 2f);
            setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
            spr.draw(batch, parentAlpha);
        }

        private void close() {
            onAccept = null;
            setVisible(false);
        }

        private void setOnAccept(Runnable accept) {
            onAccept = accept;
            setVisible(true);
        }
    }
}

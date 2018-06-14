package com.sotfk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Anim {
    private Actor parent;
    private Sprite[] sprites; // I don't care, I like Sprites more than TextureRegions.
    private String[][] animDetails;
    private int[] animStartPoints;
    private int curPos, framesUntilChange;
    private String curAnim;
    private Runnable storedAction;
    private int sprX, sprY;
    private int[] layerX, layerY;

    public static final AssetManager MANAGER = new AssetManager();

    public Anim(Actor parent, String loc) {
        this.parent = parent;
        loadAnimData(loc);
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
                if(animDetails[i][0].equals("Anim"))
                    animNums.add(i);
            }
            animStartPoints = new int[animNums.size()];
            for(int i = 0; i < animNums.size(); i++)
                animStartPoints[i] = animNums.get(i);

            curPos = 0;
            nextAnimFrame();

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

    private int animPos(String anim) {
        for(int i : animStartPoints)
            if(animDetails[i][1].equals(anim)) {
                return i;
            }
        return -1;
    }

    public void nextAnimFrame() {
        curPos++;
        String[] cF = animDetails[curPos];
        switch (cF[0]) {
            case "Frame": // Changes which sprite from the spritesheet is shown
            {
                int spr = Integer.parseInt(cF[1]);
                sprites[spr].setRegion(Integer.parseInt(cF[2]), Integer.parseInt(cF[3]), Integer.parseInt(cF[4]),
                        Integer.parseInt(cF[5]));
                sprites[spr].setSize(3 * Integer.parseInt(cF[4]), 3 * Integer.parseInt(cF[5]));
                nextAnimFrame();
            }
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
                String spritesheet = cF[1];
                MANAGER.load(spritesheet, Texture.class);
                MANAGER.finishLoadingAsset(spritesheet);
                for(int i = 0; i < sprites.length; i++) {
                    Sprite spr = sprites[i];
                    if(spr == null)
                        sprites[i] = new Sprite((Texture)MANAGER.get(spritesheet));
                    else
                        spr.setTexture(MANAGER.get(spritesheet));
                }
                nextAnimFrame();
                break;
            case "LoadAnimData":
                loadAnimData(cF[1]);
                break;
            case "SprMove":
                sprX += Integer.parseInt(cF[1]);
                sprY += Integer.parseInt(cF[2]);
                nextAnimFrame();
                break;
            case "LayerMove":
                layerX[Integer.parseInt(cF[1])] += Integer.parseInt(cF[2]);
                layerY[Integer.parseInt(cF[1])] += Integer.parseInt(cF[3]);
                nextAnimFrame();
                break;
            case "SprPosSet":
                sprX = Integer.parseInt(cF[1]);
                sprY = Integer.parseInt(cF[2]);
                nextAnimFrame();
                break;
            case "LayerPosSet":
                layerX[Integer.parseInt(cF[1])] = Integer.parseInt(cF[2]);
                layerY[Integer.parseInt(cF[1])] = Integer.parseInt(cF[3]);
                nextAnimFrame();
                break;
            case "Wait":
                framesUntilChange = Integer.parseInt(cF[1]);
                break;
            case "LayerCount":
                int layers = Integer.parseInt(cF[1]);
                sprites = new Sprite[layers];
                layerX = new int[layers];
                layerY = new int[layers];
                nextAnimFrame();
                break;
            case "HitBoxSize":
                parent.setSize(3 * Integer.parseInt(cF[1]), 3 * Integer.parseInt(cF[2]));
                nextAnimFrame();
                break;
            default:
                nextAnimFrame();
                break;
        }
    }

    public void setCurAnim(String anim) {
        int pos = animPos(anim);
        if(pos > -1) {
            curAnim = anim;
            curPos = pos;
        } else {
            curAnim = animDetails[animStartPoints[0]][1];
            curPos = animStartPoints[0];
        }
        nextAnimFrame();
    }

    public void setStoredAction(Runnable storedAction) {
        this.storedAction = storedAction;
    }

    public void draw(Batch batch, float parentAlpha) {
        for(int i = 0; i < sprites.length; i++) {
            Sprite spr = sprites[i];
            spr.setSize(3 * spr.getRegionWidth(), 3 * spr.getRegionHeight());
            spr.setPosition(parent.getX() + sprX + layerX[i], parent.getY() + sprY + layerY[i]);
            spr.draw(batch, parentAlpha);
        }
    }

    public void act() {
        framesUntilChange--;
        if(framesUntilChange == 0)
            nextAnimFrame();
    }

    public boolean hasAnim(String anim) {
        for(int i : animStartPoints)
            if(animDetails[i][1].equals(anim)) {
                return true;
            }
        return false;
    }
}

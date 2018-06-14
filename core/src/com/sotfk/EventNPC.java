package com.sotfk;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import static com.sotfk.PepSecret.MANAGER;

public abstract class EventNPC extends Actor {
    private Sprite spr;

    public EventNPC(String name) {
        String spritesheet = "assets/NPC/" + name + ".png";
        if(!MANAGER.isLoaded(spritesheet)) {
            MANAGER.load(spritesheet, Texture.class);
            MANAGER.finishLoadingAsset(spritesheet);
        }
        spr = new Sprite((Texture) MANAGER.get(spritesheet));
        spr.setScale(3f);
        setPosition(768, 96);
        addAction(Actions.sequence(Actions.moveBy(-180, 0, 1f), Actions.run(() -> {
            if(!PepSecret.getInBattle())
                for(Actor a : getParent().getChildren()) {
                    if(a instanceof Player)
                        ((Player)a).setCurAnim("Stand");
                }
            PepSecret.bgMoving = false;
            PepSecret.inEvent = true;
            event();
        })));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);
    }

    public abstract void event();
}

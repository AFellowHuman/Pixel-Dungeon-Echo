/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.etoitau.pixeldungeon.sprites;

import com.watabau.noosa.TextureFilm;
import com.etoitau.pixeldungeon.Assets;
import com.etoitau.pixeldungeon.effects.ArcherMaidenHalo;
import com.etoitau.pixeldungeon.items.weapon.missiles.Arrow;
import com.etoitau.pixeldungeon.scenes.GameScene;
import com.watabau.utils.Callback;

public class RedGirlSprite extends MobSprite {

    protected static final int FRAME_WIDTH = 12;
    protected static final int FRAME_HEIGHT = 15;

    protected static final int RUN_FRAMERATE = 20;

    public ArcherMaidenHalo halo = null;
    public boolean hasHalo = false;


    int cellToAttack = 0;

    public RedGirlSprite() {
        super();


        texture(Assets.RED_GIRL);

        TextureFilm frames = new TextureFilm(texture, FRAME_WIDTH, FRAME_HEIGHT);


        idle = new Animation(1, true);
        idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

        run = new Animation(RUN_FRAMERATE, true);
        run.frames(frames, 2, 3, 4, 5, 6, 7);

        die = new Animation(20, false);
        die.frames(frames, 8, 9, 10, 11, 12, 11);

        attack = new Animation(15, false);
        attack.frames(frames, 13, 14, 15, 0);

        zap = attack.clone();

        play(idle);
    }

    @Override
    public void attack(int cell) {

        cellToAttack = cell;
        turnTo(ch.pos, cell);
        play(zap);


    }

    public void haloUp() {
        if (hasHalo)
            return;

        hasHalo = true;
        add(State.ARCHERMAIDEN);
        GameScene.effect(halo = new ArcherMaidenHalo(this));
    }

    @Override
    public void onComplete(Animation anim) {

        if (anim == zap) {
            idle();

            ((MissileSprite) parent.recycle(MissileSprite.class)).
                    reset(ch.pos, cellToAttack, new Arrow(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    });
        } else {
            super.onComplete(anim);
        }
    }
}

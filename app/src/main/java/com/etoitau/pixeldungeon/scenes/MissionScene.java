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
package com.etoitau.pixeldungeon.scenes;

import com.watabau.noosa.Camera;
import com.watabau.noosa.Game;
import com.watabau.noosa.Group;
import com.watabau.noosa.SkinnedBlock;
import com.watabau.noosa.Visual;
import com.watabau.noosa.audio.Music;
import com.watabau.noosa.audio.Sample;
import com.watabau.noosa.particles.Emitter;
import com.etoitau.pixeldungeon.Assets;
import com.etoitau.pixeldungeon.Badges;
import com.etoitau.pixeldungeon.Dungeon;
import com.etoitau.pixeldungeon.DungeonTilemap;
import com.etoitau.pixeldungeon.FogOfWar;
import com.etoitau.pixeldungeon.PixelDungeon;
import com.etoitau.pixeldungeon.Statistics;
import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.blobs.Blob;
import com.etoitau.pixeldungeon.actors.mobs.ColdGirl;
import com.etoitau.pixeldungeon.actors.mobs.Mob;
import com.etoitau.pixeldungeon.effects.BannerSprites;
import com.etoitau.pixeldungeon.effects.BlobEmitter;
import com.etoitau.pixeldungeon.effects.EmoIcon;
import com.etoitau.pixeldungeon.effects.Flare;
import com.etoitau.pixeldungeon.effects.FloatingText;
import com.etoitau.pixeldungeon.effects.Ripple;
import com.etoitau.pixeldungeon.effects.SpellSprite;
import com.etoitau.pixeldungeon.items.Heap;
import com.etoitau.pixeldungeon.items.Item;
import com.etoitau.pixeldungeon.items.potions.Potion;
import com.etoitau.pixeldungeon.items.wands.WandOfBlink;
import com.etoitau.pixeldungeon.levels.Level;
import com.etoitau.pixeldungeon.levels.MovieLevel;
import com.etoitau.pixeldungeon.levels.RegularLevel;
import com.etoitau.pixeldungeon.levels.features.Chasm;
import com.etoitau.pixeldungeon.plants.Plant;
import com.etoitau.pixeldungeon.sprites.CharSprite;
import com.etoitau.pixeldungeon.sprites.DiscardedItemSprite;
import com.etoitau.pixeldungeon.sprites.HeroSprite;
import com.etoitau.pixeldungeon.sprites.ItemSprite;
import com.etoitau.pixeldungeon.sprites.LegendSprite;
import com.etoitau.pixeldungeon.sprites.PlantSprite;
import com.etoitau.pixeldungeon.ui.AttackIndicator;
import com.etoitau.pixeldungeon.ui.Banner;
import com.etoitau.pixeldungeon.ui.BusyIndicator;
import com.etoitau.pixeldungeon.ui.GameLog;
import com.etoitau.pixeldungeon.ui.HealthIndicator;
import com.etoitau.pixeldungeon.ui.MissionStatusPane;
import com.etoitau.pixeldungeon.ui.MissionToolbar;
import com.etoitau.pixeldungeon.ui.QuickSlot;
import com.etoitau.pixeldungeon.ui.StatusPane;
import com.etoitau.pixeldungeon.ui.Toast;
import com.etoitau.pixeldungeon.ui.Toolbar;
import com.etoitau.pixeldungeon.ui.Window;
import com.etoitau.pixeldungeon.utils.GLog;
import com.etoitau.pixeldungeon.windows.WndBag;
import com.etoitau.pixeldungeon.windows.WndBag.Mode;
import com.etoitau.pixeldungeon.windows.WndGame;
import com.etoitau.pixeldungeon.windows.WndStory;
import com.watabau.utils.Random;

import java.io.IOException;
import java.util.ArrayList;

public class MissionScene extends GameScene {



    public LegendSprite hero;
    public static boolean scenePause = false;
    protected MissionToolbar toolbar;


	@Override
	public void create() {
        if(Dungeon.depth != 0 && Dungeon.depth != ColdGirl.FROST_DEPTH) {
            Music.INSTANCE.play(Assets.TUNE, true);
            Music.INSTANCE.volume(1f);
        }
        else
        {
            Music.INSTANCE.play(Assets.TUNE_SPECIAL, true);
            Music.INSTANCE.volume(1f);
        }

		PixelDungeon.lastClass( Dungeon.hero.heroClass.ordinal() );

		super.originalCreate();
		Camera.main.zoom( defaultZoom + PixelDungeon.zoom() );

		scene = this;

		terrain = new Group();
		add( terrain );

		water = new SkinnedBlock(
			Level.WIDTH * DungeonTilemap.SIZE,
			Level.HEIGHT * DungeonTilemap.SIZE,
			Dungeon.level.waterTex() );
		terrain.add( water );

		ripples = new Group();
		terrain.add( ripples );

		tiles = new DungeonTilemap();
		terrain.add( tiles );

		Dungeon.level.addVisuals( this );

		plants = new Group();
		add( plants );

		int size = Dungeon.level.plants.size();
		for (int i=0; i < size; i++) {
			addPlantSprite( Dungeon.level.plants.valueAt( i ) );
		}

		heaps = new Group();
		add( heaps );

		size = Dungeon.level.heaps.size();
		for (int i=0; i < size; i++) {
			addHeapSprite( Dungeon.level.heaps.valueAt( i ) );
		}

		emitters = new Group();
		effects = new Group();
		emoicons = new Group();

		mobs = new Group();
		add( mobs );

		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite( mob );
			if (Statistics.amuletObtained) {
				mob.beckon( Dungeon.hero.pos );
			}
		}

		add( emitters );
		add( effects );

		gases = new Group();
		add( gases );

		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite( blob );
		}

		fog = new FogOfWar( Level.WIDTH, Level.HEIGHT );
		fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );
		add( fog );

		brightness( PixelDungeon.brightness() );


		spells = new Group();
		add( spells );

		statuses = new Group();
		add( statuses );

		add( emoicons );

		hero = new LegendSprite();
		hero.place( Dungeon.hero.pos );
		hero.updateArmor();
		mobs.add( hero );

		add( new HealthIndicator() );

		add( cellSelector = new CellSelector( tiles ) );

        MissionStatusPane sb = new MissionStatusPane();
		sb.camera = uiCamera;
		sb.setSize( uiCamera.width, 0 );
		add( sb );

		toolbar = new MissionToolbar();
		toolbar.camera = uiCamera;
		toolbar.setRect( 0,uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height() );
	    add( toolbar );

		AttackIndicator attack = new AttackIndicator();
		attack.camera = uiCamera;
		attack.setPos(
			uiCamera.width - attack.width(),
			toolbar.top() - attack.height() );
		add( attack );

		log = new GameLog();
		log.camera = uiCamera;
		log.setRect( 0, toolbar.top(), attack.left(),  0 );
		add( log );

		busy = new BusyIndicator();
		busy.camera = uiCamera;
		busy.x = 1;
		busy.y = sb.bottom() + 1;
		add( busy );

		switch (InterlevelScene.mode) {
		case RESURRECT:
			WandOfBlink.appear( Dungeon.hero, Dungeon.level.entrance );
			new Flare( 8, 32 ).color( 0xFFFF66, true ).show( hero, 2f ) ;
			break;
		case RETURN:
			WandOfBlink.appear(  Dungeon.hero, Dungeon.hero.pos );
			break;
		case FALL:
			Chasm.heroLand();
			break;
		case DESCEND:
			switch (Dungeon.depth) {
			case 1:
				WndStory.showChapter( WndStory.ID_SEWERS );
                if(PixelDungeon.itemDeg() == false)
                    WndStory.showStory( TXT_WARN_DEGRADATION );
				break;
			case 6:
				WndStory.showChapter( WndStory.ID_PRISON );
				break;
			case 11:
				WndStory.showChapter( WndStory.ID_CAVES );
				break;
			case 16:
				WndStory.showChapter( WndStory.ID_METROPOLIS );
				break;
			case 22:
				WndStory.showChapter( WndStory.ID_HALLS );
				break;
			}
			if (Dungeon.hero.isAlive() && Dungeon.depth != 22) {
				Badges.validateNoKilling();
			}
			break;
		default:
		}

		ArrayList<Item> dropped = Dungeon.droppedItems.get( Dungeon.depth );
		if (dropped != null) {
			for (Item item : dropped) {
				int pos = Dungeon.level.randomRespawnCell();
				if (item instanceof Potion) {
					((Potion)item).shatter( pos );
				} else if (item instanceof Plant.Seed) {
					Dungeon.level.plant( (Plant.Seed)item, pos );
				} else {
					Dungeon.level.drop( item, pos );
				}
			}
			Dungeon.droppedItems.remove( Dungeon.depth );
		}

		Camera.main.target = hero;

		if (InterlevelScene.mode != InterlevelScene.Mode.NONE && Dungeon.depth != 0) {
			if (Dungeon.depth < Statistics.deepestFloor) {
				GLog.h( TXT_WELCOME_BACK, Dungeon.depth );
			} else {
                if(Dungeon.depth != ColdGirl.FROST_DEPTH) {
                    GLog.h(TXT_WELCOME, Dungeon.depth);
                    Sample.INSTANCE.play(Assets.SND_DESCEND);
                }
                else
                {
                    GLog.h(TXT_FROST);
                    Sample.INSTANCE.play(Assets.SND_TELEPORT);
                }
			}
			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w( TXT_CHASM );
					break;
				case WATER:
					GLog.w( TXT_WATER );
					break;
				case GRASS:
					GLog.w( TXT_GRASS );
					break;
				default:
			}
			if (Dungeon.level instanceof RegularLevel &&
					((RegularLevel) Dungeon.level).secretDoors > Random.IntRange( 3, 4 )) {
				GLog.w( TXT_SECRETS );
			}
			if (Dungeon.nightMode && !Dungeon.bossLevel()) {
				GLog.w( TXT_NIGHT_MODE );
			}

			InterlevelScene.mode = InterlevelScene.Mode.NONE;

			fadeIn();
		}
	}

	public void destroy() {

		scene = null;
	//	Badges.saveGlobal();

		super.destroy();
	}

	@Override
	public synchronized void pause() {
		//try {
		//	Dungeon.saveAll();
		//	Badges.saveGlobal();
		//} catch (IOException e) {
			//
		//}
	}

	@Override
	public synchronized void update() {
		if (Dungeon.hero == null) {
			return;
		}

		super.update();

		water.offset( 0, -5 * Game.elapsed );

		Actor.process();

		if (Dungeon.hero.ready && !Dungeon.hero.paralysed) {
			log.newLine();
		}

		cellSelector.enabled = Dungeon.hero.ready;
	}

	@Override
	protected void onBackPressed() {
        if(Dungeon.depth == 0 && Dungeon.level instanceof MovieLevel)
        {
            Music.INSTANCE.enable(PixelDungeon.music());
            Game.switchScene(TitleScene.class);
            Dungeon.observe();
        }
		else if (!cancel()) {
			add( new WndGame() );
		}
	}

	@Override
	protected void onMenuPressed() {
		if (Dungeon.hero.ready) {
			selectItem( null, Mode.ALL, null );
		}
	}

}

package com.etoitau.pixeldungeon.actors.skills;

import com.watabau.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Awareness extends PassiveSkillA2 {


    {
        name = "Awareness";
        image = 75;
        tier = 2;
    }

    @Override
    public boolean dodgeChance() {
        if (level < 1) { return false; }

        if (Random.Int(100) < 25 * level) {
            castText = "Too easy..";
            castTextYell();
            return true;
        } else {
            // this would be too much shouting
//            castText = "I am losing my touch...";
//            castTextYell();
            return false;
        }
    }

    @Override
    protected boolean upgrade() {
        return true;
    }


    @Override
    public String info() {
        return "25% chance per level to dodge a hostile ranged attack.\n"
                + costUpgradeInfo();
    }
}

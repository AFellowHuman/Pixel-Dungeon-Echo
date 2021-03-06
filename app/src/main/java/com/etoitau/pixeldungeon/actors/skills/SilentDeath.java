package com.etoitau.pixeldungeon.actors.skills;


import com.watabau.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SilentDeath extends PassiveSkillB3 {


    {
        name = "Silent Death";
        castText = "Eternal Slumber";
        tier = 3;
        image = 59;
    }

    public boolean instantKill() {
        if (level < 1) { return false; }

        if (Random.Int(100) < 10 * level) {
            castText = "Eternal Slumber";
            castTextYell();
            return true;
        } else {
            castText = "Almost had them...";
            castTextYell();
            return false;
        }
    }

    @Override
    protected boolean upgrade() {
        return true;
    }

    @Override
    public String info() {
        return "10% per level chance to instantly kill a sleeping enemy.\n"
                + costUpgradeInfo();
    }
}

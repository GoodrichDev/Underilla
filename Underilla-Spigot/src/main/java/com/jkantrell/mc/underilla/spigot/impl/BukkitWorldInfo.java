package com.jkantrell.mc.underilla.spigot.impl;

import com.jkantrell.mc.underilla.core.api.WorldInfo;

public class BukkitWorldInfo implements WorldInfo {

    //FIELDS
    private final org.bukkit.generator.WorldInfo worldInfo_;


    //CONSTRUCTORS
    public BukkitWorldInfo(org.bukkit.generator.WorldInfo base) {
        this.worldInfo_ = base;
    }


    @Override
    public long getSeed() {
        return this.worldInfo_.getSeed();
    }

    @Override
    public int getMaxHeight() {
        return this.worldInfo_.getMaxHeight();
    }

    @Override
    public int getMinHeight() {
        return this.worldInfo_.getMinHeight();
    }
}

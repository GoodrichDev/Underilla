package com.jkantrell.mc.underilla.spigot.impl;

import org.bukkit.block.data.BlockData;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import org.bukkit.Material;

public class BukkitChunkData implements ChunkData {

    private org.bukkit.generator.ChunkGenerator.ChunkData chunkData;

    public BukkitChunkData(org.bukkit.generator.ChunkGenerator.ChunkData chunkData) {
        this.chunkData = chunkData;
    }

    @Override
    public int getMinHeight() {
        return chunkData.getMinHeight();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        BlockData data = chunkData.getBlockData(x, y, z);
        if (data == null) return null; // Handle null case
        return new BukkitBlock(data);
    }

    @Override
    public int getMaxHeight() {
        return chunkData.getMaxHeight();
    }

    @Override
    public com.jkantrell.mc.underilla.core.api.Biome getBiome(int x, int y, int z) {
        org.bukkit.block.Biome b = chunkData.getBiome(x, y, z);
        if (b == null) return null; // Handle null case
        return new BukkitBiome(b);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            // Log error or handle case when block is not an instance of BukkitBlock
            return;
        }
        chunkData.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, bukkitBlock.getBlockData());
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            // Log error or handle case when block is not an instance of BukkitBlock
            return;
        }
        chunkData.setBlock(x, y, z, bukkitBlock.getBlockData());
    }

    @Override
    public void setBiome(int x, int y, int z, com.jkantrell.mc.underilla.core.api.Biome biome) {
        // Implement biome setting logic if needed
    }
}

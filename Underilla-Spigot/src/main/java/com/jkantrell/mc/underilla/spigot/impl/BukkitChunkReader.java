package com.jkantrell.mc.underilla.spigot.impl;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mc.underilla.core.reader.TagInterpreter;
import com.jkantrell.mca.Chunk;
import com.jkantrell.nbt.tag.CompoundTag;
import com.jkantrell.nbt.tag.StringTag;

public class BukkitChunkReader extends ChunkReader {

    public BukkitChunkReader(Chunk chunk) { super(chunk); }

    @Override
    public Optional<Block> blockFromTag(CompoundTag tag) {
        if (tag == null) return Optional.empty();

        Material material = Material.matchMaterial(tag.getString("Name"));
        if (material == null) return Optional.empty();

        CompoundTag properties = tag.getCompoundTag("Properties");
        BukkitBlock block;

        try {
            block = new BukkitBlock(createBlockData(material, properties));
        } catch (IllegalArgumentException e) {
            block = new BukkitBlock(material.createBlockData()); // Fallback for incompatible data
        }

        return Optional.of(block);
    }

    private org.bukkit.block.data.BlockData createBlockData(Material material, CompoundTag properties) {
        if (properties == null) {
            return material.createBlockData();
        } else {
            String dataString = TagInterpreter.COMPOUND.interpretBlockDataString(properties);
            return material.createBlockData(dataString);
        }
    }

    @Override
    public Optional<Biome> biomeFromTag(StringTag tag) {
        if (tag == null || tag.getValue().isEmpty()) return Optional.empty();

        String[] parts = tag.getValue().split(":");
        String biomeName = parts.length > 1 ? parts[1] : parts[0];

        try {
            org.bukkit.block.Biome bukkitBiome = org.bukkit.block.Biome.valueOf(biomeName.toUpperCase());
            return Optional.of(new BukkitBiome(bukkitBiome));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Could not resolve biome: " + biomeName);
            return Optional.empty();
        }
    }
}
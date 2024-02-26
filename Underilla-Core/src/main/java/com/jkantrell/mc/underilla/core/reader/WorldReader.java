package com.jkantrell.mc.underilla.core.reader;

import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAFile;
import com.jkantrell.mca.MCAUtil;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WorldReader implements Reader {

    private static final String REGION_DIRECTORY = "region";

    private final File world_;
    private final File regions_;
    private final ConcurrentHashMap<Pair<Integer, Integer>, MCAFile> regionCache_;
    private final ConcurrentHashMap<Pair<Integer, Integer>, ChunkReader> chunkCache_;

    public WorldReader(String worldPath) throws NoSuchFieldException {
        this(new File(worldPath));
    }
    public WorldReader(String worldPath, int cacheSize) throws NoSuchFieldException {
        this(new File(worldPath), cacheSize);
    }
    public WorldReader(File worldDir) throws NoSuchFieldException {
        this(worldDir, 16);
    }

    public WorldReader(File worldDir, int cacheSize) throws NoSuchFieldException {
        validateWorldDirectory(worldDir);

        this.world_ = worldDir;
        this.regions_ = new File(worldDir, REGION_DIRECTORY);
        validateRegionDirectory(this.regions_);

        this.regionCache_ = new ConcurrentHashMap<>(cacheSize);
        this.chunkCache_ = new ConcurrentHashMap<>(cacheSize * 8);
    }

    public String getWorldName() {
        return this.world_.getName();
    }

    @Override
    public Optional<Block> blockAt(int x, int y, int z) {
        return getChunkReader(x, z).flatMap(c -> c.blockAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)));
    }

    @Override
    public Optional<Biome> biomeAt(int x, int y, int z) {
        return getChunkReader(x, z).flatMap(c -> c.biomeAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)));
    }

    public Optional<ChunkReader> readChunk(int x, int z) {
        return Optional.ofNullable(chunkCache_.computeIfAbsent(new Pair<>(x, z), k -> {
            MCAFile region = readRegion(x >> 5, z >> 5);
            if (region == null) return null;
            Chunk chunk = region.getChunk(Math.floorMod(x, 32), Math.floorMod(z, 32));
            return chunk == null ? null : newChunkReader(chunk);
        }));
    }

    protected abstract ChunkReader newChunkReader(Chunk chunk);

    private MCAFile readRegion(int x, int z) {
        return regionCache_.computeIfAbsent(new Pair<>(x, z), k -> {
            File regionFile = new File(regions_, String.format("r.%d.%d.mca", x, z));
            if (!regionFile.exists()) return null;
            try {
                return MCAUtil.read(regionFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private static void validateWorldDirectory(File worldDir) throws NoSuchFieldException {
        if (!(worldDir.exists() && worldDir.isDirectory())) {
            throw new NoSuchFieldException("World directory '" + worldDir.getPath() + "' does not exist.");
        }
    }

    private static void validateRegionDirectory(File regionDir) throws NoSuchFieldException {
        if (!(regionDir.exists() && regionDir.isDirectory())) {
            throw new NoSuchFieldException("World '" + regionDir.getParent() + "' doesn't have a 'region' directory.");
        }
    }

    private Optional<ChunkReader> getChunkReader(int x, int z) {
        return Optional.ofNullable(chunkCache_.get(new Pair<>(x, z)));
    }

    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }
}

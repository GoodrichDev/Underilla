package com.jkantrell.mc.underilla.core.reader;

import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.vector.LocatedBlock;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAUtil;
import com.jkantrell.mca.Section;
import com.jkantrell.nbt.tag.CompoundTag;
import com.jkantrell.nbt.tag.StringTag;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChunkReader implements Reader {

    // ASSETS
    public static final int MAXIMUM_SECTION_HEIGHT = 20, MINIMUM_SECTION_HEIGHT = -4;
    public static final int MAXIMUM_HEIGHT = MAXIMUM_SECTION_HEIGHT * 16, MINIMUM_HEIGHT = MINIMUM_SECTION_HEIGHT * 16;

    // FIELDS
    private final Chunk chunk_;
    private Integer airColumnHeight_ = null;

    // CONSTRUCTORS
    public ChunkReader(Chunk chunk) {
        this.chunk_ = chunk;
    }

    // GETTERS
    public int getX() {
        return this.chunk_.getX();
    }

    public int getZ() {
        return this.chunk_.getZ();
    }

    // UTIL
    @Override
    public Optional<Block> blockAt(int x, int y, int z) {
        if (this.chunk_ == null) { return Optional.empty(); }
        return this.blockFromTag(this.chunk_.getBlockStateAt(x, y, z));
    }

    @Override
    public Optional<Biome> biomeAt(int x, int y, int z) {
        Map<Integer, Section> sectionMap = this.chunk_.getSectionMap();
        int height = MCAUtil.blockToChunk(y);
        StringTag biomeTag;

        // Combine upward and downward search into a single efficient search
        for (int i = height; i <= MAXIMUM_SECTION_HEIGHT && i >= MINIMUM_SECTION_HEIGHT; i += i < height ? -1 : 1) {
            Section section = sectionMap.get(i);
            if (section != null) {
                biomeTag = section.getBiomeAt(x, Math.floorMod(y, 16), z);
                if (biomeTag != null) {
                    return this.biomeFromTag(biomeTag);
                }
            }
            // Switch direction after reaching initial height
            if (i == height) { i = height - 2; }
        }

        return Optional.empty();
    }

    public int airSectionsBottom() {
        if (this.airColumnHeight_ != null) {
            return this.airColumnHeight_;
        }

        this.airColumnHeight_ = calculateAirColumnHeight();
        return this.airColumnHeight_;
    }

    private int calculateAirColumnHeight() {
        Predicate<Section> isAir = s -> Optional.ofNullable(s.getBlockStatePalette().get(0))
                .flatMap(this::blockFromTag)
                .map(Block::isAir)
                .orElse(true);

        int lowest = MAXIMUM_HEIGHT;
        for (int height = MAXIMUM_HEIGHT - 1; height > MINIMUM_HEIGHT - 1; height--) {
            Section s = this.chunk_.getSection(height);
            if (s == null || isAir.test(s)) { continue; }
            lowest = height + 1;
            break;
        }

        return lowest * 16;
    }

    public List<LocatedBlock> locationsOf(Predicate<Block> checker, int under, int above) {
        Stream<Section> sectionStream = this.chunk_.getSections().stream().filter(s -> {
            int lower = s.getHeight() * 16, upper = lower + 15;
            return under > lower && above < upper;
        });
        Predicate<LocatedBlock> locationCheck = l -> l.y() > above && l.y() < under;
        return this.locationsOf(checker, locationCheck, sectionStream);
    }

    public List<LocatedBlock> locationsOf(Predicate<Block> checker) {
        // Example adjustment within locationsOf
        return this.chunk_.getSections().stream()
                .flatMap(section -> section.getBlockLocations(blockTag -> {
                    Block block = blockFromTag(blockTag).orElse(null);
                    return block != null && checker.test(block); // Null-safe check
                }).stream())
                .map(location -> new LocatedBlock(location.x(), location.y(), location.z(), blockFromTag(location.tag()).orElse(null)))
                .collect(Collectors.toList());
    }

    public List<LocatedBlock> locationsOf(Block block) {
        return this.locationsOf(block::equals);
    }
    public List<LocatedBlock> locationsOf(Block... blocks) {
        List<Block> materialList = Arrays.asList(blocks);
        return this.locationsOf(materialList::contains);
    }

    // ABSTRACT
    public abstract Optional<Block> blockFromTag(CompoundTag tag);

    public abstract Optional<Biome> biomeFromTag(StringTag tag);

    // PRIVATE UTIL
    private List<LocatedBlock> locationsOf(Predicate<Block> checker, Predicate<LocatedBlock> secondChecker, Stream<Section> sections) {
        return sections.flatMap(s -> s.getBlockLocations(t -> checker.test(this.blockFromTag(t).orElse(null)))
                        .stream().map(l -> new LocatedBlock(l.x(), l.y() + (s.getHeight() * 16), l.z(), this.blockFromTag(l.tag()).orElse(null))))
                .filter(secondChecker).toList();
    }
}
package com.jkantrell.mc.underilla.core.generation;

import com.jkantrell.mc.underilla.core.vector.Direction;
import com.jkantrell.mc.underilla.core.vector.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class Spreader {

    private Set<Vector<Integer>> initVectors_;
    private AtomicBoolean[] entityMap_;
    private int minX_, maxX_, minY_, maxY_, minZ_, maxZ_, dimX_, dimY_, dimZ_;
    private int iterations_ = 0;

    public Spreader setRootVectors(Collection<Vector<Integer>> vectors) {
        this.initVectors_ = new HashSet<>(vectors);
        return this;
    }

    public Spreader setContainer(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX_ = Math.min(x1, x2);
        this.minY_ = Math.min(y1, y2);
        this.minZ_ = Math.min(z1, z2);

        this.maxX_ = Math.max(x1, x2);
        this.maxY_ = Math.max(y1, y2);
        this.maxZ_ = Math.max(z1, z2);

        this.dimX_ = this.maxX_ - this.minX_ + 1;
        this.dimY_ = this.maxY_ - this.minY_ + 1;
        this.dimZ_ = this.maxZ_ - this.minZ_ + 1;

        this.entityMap_ = new AtomicBoolean[this.dimX_ * this.dimY_ * this.dimZ_];
        for (int i = 0; i < this.entityMap_.length; i++) {
            this.entityMap_[i] = new AtomicBoolean(false);
        }
        return this;
    }

    public Spreader setContainer(Vector<Integer> corner1, Vector<Integer> corner2) {
        return this.setContainer(corner1.x(), corner1.y(), corner1.z(), corner2.x(), corner2.y(), corner2.z());
    }

    public Spreader setIterationsAmount(int amount) {
        this.iterations_ = amount;
        return this;
    }

    public Set<Vector<Integer>> spread() {
        Set<Entity> entities = new HashSet<>();
        Set<Entity> current = new HashSet<>();
        for (Vector<Integer> v : this.initVectors_) {
            if (!this.contains(v) || !this.setMapIfAbsent(v.x(), v.y(), v.z())) continue;
            current.add(new Entity(v));
        }

        for (int i = 0; i < this.iterations_; i++) {
            Set<Entity> temp = new HashSet<>();
            for (Entity e : current) {
                e.spread(temp);
            }
            entities.addAll(current);
            current = temp;
        }

        Set<Vector<Integer>> result = new HashSet<>();
        for (Entity e : entities) {
            result.add(e.toVector());
        }
        return result;
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX_ && x <= maxX_ && y >= minY_ && y <= maxY_ && z >= minZ_ && z <= maxZ_;
    }

    public boolean contains(Vector<Integer> vector) {
        return contains(vector.x(), vector.y(), vector.z());
    }

    public boolean isPresent(int x, int y, int z) {
        if (!this.contains(x, y, z)) { return false; }
        return this.getMap(x, y, z);
    }

    public boolean isPresent(Vector<Integer> vector) {
        return this.isPresent(vector.x(), vector.y(), vector.z());
    }

    private boolean setMapIfAbsent(int x, int y, int z) {
        int index = getMapIndex(x, y, z);
        return !this.entityMap_[index].getAndSet(true);
    }

    private boolean getMap(int x, int y, int z) {
        return this.entityMap_[getMapIndex(x, y, z)].get();
    }

    private int getMapIndex(int x, int y, int z) {
        x -= this.minX_;
        y -= this.minY_;
        z -= this.minZ_;
        return (y * this.dimZ_ + z) * this.dimX_ + x;
    }

    private class Entity {
        private static final Direction[] DIRECTIONS = Direction.values();
        final Vector<Integer> position;

        Entity(Vector<Integer> position) {
            this.position = position;
        }

        void spread(Set<Entity> target) {
            for (Direction d : DIRECTIONS) {
                Vector<Integer> newPos = this.position.add(d.vector());
                if (!Spreader.this.contains(newPos) || !Spreader.this.setMapIfAbsent(newPos.x(), newPos.y(), newPos.z())) {
                    continue;
                }
                target.add(new Entity(newPos));
            }
        }

        Vector<Integer> toVector() {
            return this.position;
        }
    }
}
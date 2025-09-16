package com.github.tamaracvjetkovic.btree;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TreeNode {
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    int minDegree;
    boolean leaf;
    int keyCount;
    byte[][] keys;
    byte[][] data;
    TreeNode[] children;

    public TreeNode(int minDegree, boolean leaf) {
        this.minDegree = minDegree;
        this.leaf = leaf;
        this.keyCount = 0;
        this.keys = new byte[2 * minDegree - 1][];
        this.data = new byte[2 * minDegree - 1][];
        this.children = new TreeNode[2 * minDegree];
    }

    public boolean isLeaf() {
        return leaf;
    }

    public int getKeyCount() {
        return keyCount;
    }

    public byte[][] getKeys() {
        return keys;
    }

    public TreeNode[] getChildren() {
        return children;
    }

    byte[] get(byte[] key) {
        TreeNode node = this;
        node.lock.readLock().lock();

        try {
            while (true) {
                int pos = findKeyIndex(node, key);

                if (pos < node.keyCount && Arrays.compare(key, node.keys[pos]) == 0) {
                    byte[] value = node.data[pos];
                    return value == null ? null : Arrays.copyOf(value, value.length);
                }
                if (node.leaf) return null;

                TreeNode child = node.children[pos];
                child.lock.readLock().lock();
                node.lock.readLock().unlock();
                node = child;
            }

        } finally {
            if (node.lock.getReadHoldCount() > 0) {
                node.lock.readLock().unlock();
            }
        }
    }

    // the caller already locks the node
    private int findKeyIndex(TreeNode node, byte[] key) {
        int l = 0, r = node.keyCount - 1;

        while (l <= r) {
            int mid = (l + r) / 2;
            if (Arrays.compare(key, node.keys[mid]) > 0) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }
        return l;
    }

    // the caller already locks the node and the child node
    void splitChild(int index, TreeNode child) {
        TreeNode rightChild = new TreeNode(child.minDegree, child.leaf);

        for (int i = 0; i < minDegree - 1; i++) {
            rightChild.keys[i] = child.keys[i + minDegree];
            rightChild.data[i] = child.data[i + minDegree];
        }
        rightChild.keyCount = minDegree - 1;

        if (!child.leaf) {
            for (int i = 0; i < minDegree; i++) {
                rightChild.children[i] = child.children[i + minDegree];
            }
        }

        for (int i = keyCount; i > index; i--) {
            children[i + 1] = children[i];
        }
        children[index + 1] = rightChild;

        for (int i = keyCount - 1; i >= index; i--) {
            keys[i + 1] = keys[i];
            data[i + 1] = data[i];
        }
        keys[index] = child.keys[minDegree - 1];
        data[index] = child.data[minDegree - 1];
        keyCount++;

        child.keyCount = minDegree - 1;
    }

    // the caller already locked the node
    void insertNext(byte[] key, byte[] data) {
        int pos = findKeyIndex(this, key);

        if (leaf) {
            for (int i = keyCount - 1; i > pos - 1; i--) {
                keys[i + 1] = keys[i];
                this.data[i + 1] = this.data[i];
            }
            keys[pos] = key;
            this.data[pos] = data;
            keyCount++;

            this.lock.writeLock().unlock();
            return;
        }

        TreeNode child = children[pos];
        child.lock.writeLock().lock();

        if (child.keyCount == 2 * minDegree - 1) {
            splitChild(pos, child);
            if (Arrays.compare(keys[pos], key) < 0) {
                pos++;
            }

            // 'next' could be the original (now shrunk) child or the newly created right node
            TreeNode next = children[pos];
            if (next != child) {
                next.lock.writeLock().lock();
                child.lock.writeLock().unlock();
            }
            // else: already locked (since 'next' is the 'child')

            this.lock.writeLock().unlock();
            next.insertNext(key, data);

        } else {
            this.lock.writeLock().unlock();
            child.insertNext(key, data);
        }
    }
}

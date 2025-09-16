package com.github.tamaracvjetkovic.btree;

import java.util.Arrays;

public class BTree {
    TreeNode root;
    int minDegree;

    public BTree(int minDegree) {
        this.minDegree = minDegree;
        this.root = null;
    }

    public TreeNode getRoot() {
        return root;
    }

    public byte[] get(byte[] key) {
        if (root == null) return null;
        return root.get(key);
    }

    public void put(byte[] key, byte[] data) {
        // root null (empty tree)
        if (root == null) {
            root = new TreeNode(minDegree, true);
            root.keys[0] = key;
            root.data[0] = data;
            root.keyCount = 1;
            return;
        }

        root.lock.writeLock().lock();
        try {
            // root full
            if (root.keyCount == 2 * minDegree - 1) {
                TreeNode previousRoot = root;

                // newRoot won't be available to other threads, no need to lock
                TreeNode newRoot = new TreeNode(minDegree, false);
                newRoot.children[0] = previousRoot;
                newRoot.splitChild(0, previousRoot);

                int i = 0;
                if (Arrays.compare(newRoot.keys[0], key) < 0) i++;

                // 'newRootChild' could be the original (now shrunk) root or the newly created right child
                TreeNode newRootChild = newRoot.children[i];
                if (newRootChild != previousRoot) {
                    newRootChild.lock.writeLock().lock();
                    previousRoot.lock.writeLock().unlock();
                }
                // else: already locked (since 'newRootChild' is the 'previousRoot')

                root = newRoot;

                newRootChild.insertNext(key, data);
                return;
            }

            root.insertNext(key, data);

        } finally {
            if (root != null && root.lock.getWriteHoldCount() > 0) {
                root.lock.writeLock().unlock();
            }
        }
    }
}

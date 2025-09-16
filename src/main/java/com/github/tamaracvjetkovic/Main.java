package com.github.tamaracvjetkovic;

import com.github.tamaracvjetkovic.btree.BTree;
import com.github.tamaracvjetkovic.btree.TreeNode;

import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        BTree tree = new BTree(2);

        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17};

        for (int key : keys) {
            byte[] keyBytes = ByteBuffer.allocate(4).putInt(key).array();
            tree.put(keyBytes, ("Value_" + key).getBytes());
        }

        printTree(tree.getRoot(), "", true);

        check(tree, 6);
        check(tree, 15);
    }

    private static void check(BTree tree, int key) {
        byte[] keyBytes = ByteBuffer.allocate(4).putInt(key).array();
        if (tree.get(keyBytes) != null) {
            System.out.println("Key " + key + " is in the BTree");
        } else {
            System.out.println("Key " + key + " is NOT in the BTree");
        }
    }

    private static void printTree(TreeNode node, String indent, boolean isRoot) {
        if (node == null) return;

        String prefix = isRoot ? "\nRoot: " : "Child: ";
        System.out.println(indent + prefix + keysToString(node));

        if (!node.isLeaf()) {
            for (int i = 0; i <= node.getKeyCount(); i++) {
                printTree(node.getChildren()[i], indent + "___", false);
            }
        }
    }

    private static String keysToString(TreeNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < node.getKeyCount(); i++) {
            sb.append(ByteBuffer.wrap(node.getKeys()[i]).getInt());
            if (i < node.getKeyCount() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
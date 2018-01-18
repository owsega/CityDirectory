package com.owsega.citydirectory.model;

import java.util.Hashtable;

/**
 * A trie storing city names
 */
public class CitiesTrie {

    private Node start = new Node();

    public int find(String prefix) {
        prefix = prefix.toLowerCase();

        Node current = start;
        for (int i = 0; i < prefix.length(); i++) {
            current = current.getChild(prefix.charAt(i));
            if (current == null) return 0;
        }
        if (current.isComplete) {
            return current.num + 1;
        } else return current.num;
    }

    public void add(City city) {
        String name = city.toString().toLowerCase();
        Node current = start;
        for (int i = 0; i < name.length(); i++) {
            current = current.addChild(name.charAt(i));
        }
        current.isComplete = true;
        current.city = city;
    }

    class Node {
        Hashtable<Character, Node> children = new Hashtable<>();
        boolean isComplete;
        /**
         * number of children under this node
         */
        int num;
        /**
         * optional City at this node. Present only when isComplete is true
         */
        City city;

        Node getChild(char c) {
            return children.get(c);
        }

        Node addChild(char c) {
            num++;
            Node node = children.get(c);
            if (node == null) {
                node = new Node();
                children.put(c, node);
            }
            return node;
        }
    }

}
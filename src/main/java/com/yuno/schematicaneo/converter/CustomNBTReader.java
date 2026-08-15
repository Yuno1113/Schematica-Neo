package com.yuno.schematicaneo.converter;

import java.io.*;
import java.util.*;

public class CustomNBTReader {

    public static class Node {
        public final String name;
        public final Object value;
        public final Map<String, Node> children = new LinkedHashMap<>();

        public Node(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public Node getChild(String key) { return children.get(key); }
        public String getString(String key) {
            Node n = children.get(key);
            return n != null && n.value instanceof String ? (String) n.value : "";
        }
        public int getInteger(String key) {
            Node n = children.get(key);
            return n != null && n.value instanceof Integer ? (Integer) n.value : 0;
        }
        public int[] getIntArray(String key) {
            Node n = children.get(key);
            if (n == null) return null;
            if (n.value instanceof int[]) return (int[]) n.value;
            if (n.value instanceof List) {
                List<Node> list = (List<Node>) n.value;
                int[] arr = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Object v = list.get(i).value;
                    arr[i] = v instanceof Integer ? (Integer) v : 0;
                }
                return arr;
            }
            return null;
        }
        public long[] getLongArray(String key) {
            Node n = children.get(key);
            return n != null && n.value instanceof long[] ? (long[]) n.value : null;
        }
        public List<Node> getList(String key) {
            Node n = children.get(key);
            return n != null && n.value instanceof List ? (List<Node>) n.value : new ArrayList<>();
        }
    }

    public static Node readFile(File file) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            byte type = dis.readByte();
            if (type == 0) return null;
            String name = dis.readUTF();
            return readTag(dis, type, name);
        } finally {
            dis.close();
        }
    }

    public static Node readCompressed(File file) throws IOException {
        java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
        DataInputStream dis = new DataInputStream(gzip);
        try {
            byte type = dis.readByte();
            if (type == 0) return null;
            String name = dis.readUTF();
            return readTag(dis, type, name);
        } finally {
            dis.close();
        }
    }

    private static Node readTag(DataInputStream dis, byte type, String name) throws IOException {
        switch (type) {
            case 1: return new Node(name, dis.readByte());
            case 2: return new Node(name, dis.readShort());
            case 3: return new Node(name, dis.readInt());
            case 4: return new Node(name, dis.readLong());
            case 5: return new Node(name, dis.readFloat());
            case 6: return new Node(name, dis.readDouble());
            case 7: { int len = dis.readInt(); byte[] arr = new byte[len]; dis.readFully(arr); return new Node(name, arr); }
            case 8: return new Node(name, dis.readUTF());
            case 9: {
                byte listType = dis.readByte();
                int listLen = dis.readInt();
                List<Node> list = new ArrayList<>();
                for (int i = 0; i < listLen; i++) {
                    list.add(readTag(dis, listType, ""));
                }
                Node node = new Node(name, list);
                return node;
            }
            case 10: {
                Node node = new Node(name, null);
                while (true) {
                    byte childType = dis.readByte();
                    if (childType == 0) break;
                    String childName = dis.readUTF();
                    Node child = readTag(dis, childType, childName);
                    node.children.put(childName, child);
                }
                return node;
            }
            case 11: {
                int len = dis.readInt();
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readInt();
                return new Node(name, arr);
            }
            case 12: {
                int len = dis.readInt();
                long[] arr = new long[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readLong();
                return new Node(name, arr);
            }
            default:
                throw new IOException("Unknown NBT tag type: " + type);
        }
    }

    public static String formatBlockState(Node state) {
        String name = state.getString("Name");
        if (name.isEmpty()) name = "minecraft:air";

        Node props = state.getChild("Properties");
        if (props == null || props.children.isEmpty()) {
            return name;
        }

        StringBuilder sb = new StringBuilder(name);
        sb.append("[");
        boolean first = true;
        for (String key : props.children.keySet()) {
            if (!first) sb.append(",");
            sb.append(key).append("=").append(props.getString(key));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}

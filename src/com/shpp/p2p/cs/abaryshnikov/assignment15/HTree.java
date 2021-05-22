package com.shpp.p2p.cs.abaryshnikov.assignment15;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class contains Huffman tree root node
 * and main methods to work with Huffman tree
 * (creating, flattening/unflattening, encoding bytes,
 * decoding bit sequences etc.)
 */
public class HTree {

    private HTreeNode root = null;

    @SuppressWarnings("unused")
    private HTree() {} // explicitly forbidden default constructor

    /*
     * Following chunk of code contains constructors of a Huffman tree
     *  |
     *  V
     */

    /**
     * Constructor for creating Huffman Tree from unique bytes and their frequencies
     *
     * @param uniqueBytesAndFrequencies unique bytes of file associated with
     *                                  their appearance frequency
     */
     HTree(Map<Byte, Integer> uniqueBytesAndFrequencies) throws Exception {

         PriorityQueue<HTreeNode> nodes = new PriorityQueue<>();
         for (byte bt : uniqueBytesAndFrequencies.keySet()) {
            nodes.add(new HTreeNode(bt, uniqueBytesAndFrequencies.get(bt)));
         }

         if (nodes.size() < 2) {
             throw new Exception("Huffman algorithm needs at least two unique bytes in the input file");
         }

         while (nodes.size() > 1){
             HTreeNode left = nodes.poll();
             HTreeNode right = nodes.poll();
             //noinspection ConstantConditions
             root = new HTreeNode( left.getFrequency() + right.getFrequency(),
                                          left,
                                          right);
             nodes.add(root);
         }
     }

     /*
      * This chunk of code is commented because the other way to create H-Tree
      * using PriorityQueue is developed
      */

//    HTree(Map<Byte, Integer> uniqueBytesAndFrequencies) throws Exception {
//
//        ArrayList<HTreeNode> nodes = new ArrayList<>();
//
//        int maxFrequency = 1;
//        for (byte bt : uniqueBytesAndFrequencies.keySet()) {
//            nodes.add(new HTreeNode(bt, uniqueBytesAndFrequencies.get(bt)));
//            maxFrequency += uniqueBytesAndFrequencies.get(bt);
//        }
//
//        if (nodes.size() < 2) {
//            throw new Exception("Huffman algorithm needs at least two unique bytes in the input file");
//        }
//
//        HTreeNode newRoot;
//        while (nodes.size() > 2) {
//            HTreeNode[] twoNodes = findTwoLessFrequentNodes(nodes, maxFrequency);
//            newRoot = new HTreeNode(twoNodes[0].getFrequency() + twoNodes[1].getFrequency());
//            newRoot.setLeftChildNode(twoNodes[0].clone());
//            newRoot.setRightChildNode(twoNodes[1].clone());
//            nodes.add(newRoot.clone());
//        }
//
//        root = new HTreeNode(nodes.get(0).getFrequency() + nodes.get(1).getFrequency());
//        root.setLeftChildNode(nodes.get(0).clone());
//        root.setRightChildNode(nodes.get(1).clone());
//    }
//
//    /**
//     * this method helps to find two the least frequent occurring nodes in the list of nodes
//     */
//    private HTreeNode[] findTwoLessFrequentNodes(ArrayList<HTreeNode> nodes, int maxFrequency) {
//        HTreeNode[] twoLessFrequentNodes = new HTreeNode[2];
//
//        HTreeNode firstLessFrequent = null;
//        HTreeNode secondLessFrequent = null;
//
//        int minFrequency = maxFrequency;
//        for (HTreeNode node : nodes) {
//            if (node.getFrequency() <= minFrequency) {
//                minFrequency = node.getFrequency();
//                firstLessFrequent = node;
//            }
//        }
//        twoLessFrequentNodes[0] = firstLessFrequent.clone();
//        nodes.remove(firstLessFrequent);
//
//        minFrequency = maxFrequency;
//        for (HTreeNode node : nodes) {
//            if (node.getFrequency() <= minFrequency) {
//                minFrequency = node.getFrequency();
//                secondLessFrequent = node;
//            }
//        }
//        twoLessFrequentNodes[1] = secondLessFrequent.clone();
//        nodes.remove(secondLessFrequent);
//
//        return twoLessFrequentNodes;
//    }


    private final Queue<Byte> leaves = new LinkedBlockingQueue<>();
    private final Queue<Boolean> structure = new LinkedBlockingQueue<>();

    /**
     * Constructor for restoring Huffman Tree from its encoded (flattened) form
     *
     * @param treeStructure structure of Huffman tree encoded in bits that are
     *                      collected to byte[] array
     * @param treeLeaves    leaves (unique bytes of file) of Huffman tree that are
     *                      collected to byte[] array
     */
    HTree(byte[] treeStructure, byte[] treeLeaves) throws Exception {

        for (byte treeLeaf : treeLeaves) {
            leaves.add(treeLeaf);
        }

        byte bit;
        for (byte b : treeStructure) {
            for (int j = 0; j < 8; j++) {
                bit = (byte) (b >> j);
                if ((bit & 0x01) == 1) {
                    structure.add(true);
                } else {
                    structure.add(false);
                }
            }
        }

        if (!structure.poll()) {
            throw new Exception("Tree can`t start with leaf");
        }
        root = new HTreeNode();

        try {
            buildTree(root);
        } catch (Exception e) {
            throw new Exception("Error occurred during restoring Haffman Tree structure");
        }

    }

    /**
     * this method helps to restore (unflate) Huffman tree from its encoded form
     */
    private void buildTree(HTreeNode node) throws NullPointerException {

        if (structure.peek() != null && structure.poll()) {
            node.setLeftChildNode(new HTreeNode());
            buildTree(node.getLeftChildNode());
        } else {
            if (leaves.peek() != null) {
                node.setLeftChildNode(new HTreeNode(leaves.poll()));
            }
        }

        if (structure.peek() != null && structure.poll()) {
            node.setRightChildNode(new HTreeNode());
            buildTree(node.getRightChildNode());
        } else {
            if (leaves.peek() != null) {
                node.setRightChildNode(new HTreeNode(leaves.poll()));
            }
        }

    }

    /*
     * Following chunk of code contains methods for creating H-tree structure,
     * sequence of tree leaves (that are unique bytes) and length of the tree
     * (number of bits that presents the tree structure)
     *  |
     *  V
     */

    private byte[] treeStruct;
    private final Queue<Boolean> treeStructAsQueue = new LinkedBlockingQueue<>();
    private int nodesCounter = 0;

    /**
     * This method is used to get Huffman tree structure
     * as a sequence of bits  where '1' is a transitional node and '0' is
     * a "leaf" node that contains unique byte.
     * This sequence is formed during pre-order counter-clockwise tree traverse
     */
    public byte[] getTreeStructure() {
        if (nodesCounter == 0) {
            preOrderTraversal(root);

            BitSet buffer = new BitSet(8);
            ArrayList<Byte> temp = new ArrayList<>();

            while (treeStructAsQueue.peek() != null) {
                for (int i = 0; i < 8; i++) {
                    if (treeStructAsQueue.peek() != null) {
                        buffer.set(i, treeStructAsQueue.poll());
                    }
                }
                temp.add(buffer.toByteArray()[0]);
            }

            treeStruct = new byte[temp.size()];
            for (int i = 0; i < treeStruct.length; ++i) {
                treeStruct[i] = temp.get(i);
            }

        }

        return treeStruct;
    }

    /**
     * this method realises pre-order counter-clockwise tree traverse
     */
    private void preOrderTraversal(HTreeNode node) {
        if (node.isLeaf()) {
            treeStructAsQueue.add(false);
        } else {
            treeStructAsQueue.add(true);
        }
        ++nodesCounter;
        if (node.getLeftChildNode() != null) {
            preOrderTraversal(node.getLeftChildNode());
        }
        if (node.getRightChildNode() != null) {
            preOrderTraversal(node.getRightChildNode());
        }
    }


    public short getTreeSize() throws Exception {
        if (nodesCounter == 0) {
            getTreeStructure();
            if (nodesCounter == 0) {
                throw new Exception("Tree is not created");
            }
        }

        return (short) (nodesCounter);
    }


    private final ArrayList<Byte> treeLeavesList = new ArrayList<>();

    /**
     * This method is used to get Huffman tree leaves
     * (nodes that contain unique bytes)
     * as a sequence of bytes.
     * This sequence is formed during in-order counter-clockwise tree traverse.
     */
    public byte[] getTreeLeaves() {
        inOrder(root);

        byte[] treeLeavesArray = new byte[treeLeavesList.size()];
        for (int i = 0; i < treeLeavesArray.length; ++i) {
            treeLeavesArray[i] = treeLeavesList.get(i);
        }

        return treeLeavesArray;
    }

    /**
     * this method realises in-order counter-clockwise tree traverse
     */
    private void inOrder(HTreeNode node) {
        if (node.getLeftChildNode() != null) {
            inOrder(node.getLeftChildNode());
        } else {
            treeLeavesList.add(node.getUniqueByte());
            return;
        }
        if (node.getRightChildNode() != null) {
            inOrder(node.getRightChildNode());
        } else {
            treeLeavesList.add(node.getUniqueByte());
        }
    }


    /*
     * Following chunk of code contains methods that help
     * to encode unique byte with certain sequence of bits
     *  |
     *  V
     */

    private HashMap<Integer, Integer[]> bytesAndEncodingBits = new HashMap<>();

    /**
     * This method is used to get bits (and their number)
     * as a unique code for unique byte of file.
     *
     * @param byteToCode unique byte that have to be encoded with
     *                   certain sequence of bits;
     *
     * @return array of integers, where int[0] contains certain
     * sequence of bits and int[1] contains number
     * of significant bits in the int[0].
     */
    public int[] encodeByte(int byteToCode) throws Exception {

        if (bytesAndEncodingBits.isEmpty()) {
            getBytesAndEncodingBits();
        }
        Integer[] bitsAndTheirNumber = bytesAndEncodingBits.get(byteToCode);
        int[] bitsAndTheirNumberToReturn = new int[2];
        bitsAndTheirNumberToReturn[0] = bitsAndTheirNumber[0];
        bitsAndTheirNumberToReturn[1] = bitsAndTheirNumber[1];

        return bitsAndTheirNumberToReturn;
    }

    /**
     * This method is used for generating map of associated unique bytes and
     * their encoding in bits.
     *
     * Bits are presented in form of array of integers, where Integer[0] contains
     * certain sequence of bits and Integer[1] contains number
     * of significant bits in the Integer[0].
     *
     * The map is formed during in-order counter-clockwise tree traverse.
     */
    public HashMap<Integer, Integer[]> getBytesAndEncodingBits() {
        bytesAndEncodingBits = new HashMap<>();
        inOrderWithMarking(root);
        return bytesAndEncodingBits;
    }

    private final Deque<Integer> encodingBitsAsDeque = new ArrayDeque<>();

    /** this method realises in-order counter-clockwise tree traverse */
    private void inOrderWithMarking(HTreeNode node) {
        if (node.getLeftChildNode() != null) {
            encodingBitsAsDeque.add(0);
            inOrderWithMarking(node.getLeftChildNode());
            encodingBitsAsDeque.removeLast();
        } else {
            bytesAndEncodingBits.put((int) node.getUniqueByte(), dequeToBitsAndTheirNumber(encodingBitsAsDeque));
            return;
        }
        if (node.getRightChildNode() != null) {
            encodingBitsAsDeque.add(1);
            inOrderWithMarking(node.getRightChildNode());
            encodingBitsAsDeque.removeLast();
        } else {
            bytesAndEncodingBits.put((int) node.getUniqueByte(), dequeToBitsAndTheirNumber(encodingBitsAsDeque));
        }
    }

    /** this method converts values from deque to bits and store them in integer */
    private Integer[] dequeToBitsAndTheirNumber(Deque<Integer> deque) {
        Integer[] temp = deque.toArray(new Integer[0]);
        int bits = 0;
        for (int i = 0; i < temp.length; i++) {
            bits = (bits | (temp[i] << i));
        }
        Integer[] result = new Integer[2];
        result[0] = bits;
        result[1] = deque.size();

        return result;
    }

    /*
     * Following chunk of code contains methods that helps
     * to restore original byte encoded by certain bit sequence
     *  |
     *  V
     */

    private int flag = 0;
    private boolean isByteDecoded;
    private byte decodedByte;
    private HTreeNode currentNode;

    /**
     * This method traverses tree when the encoding bits are consequently
     * passed one by one and get to leaf with byte that is encoded
     * by passed sequence of bit.
     */
    public void decode(byte bit) {
        if (flag == 0) {
            currentNode = root;
            isByteDecoded = false;
            decodedByte = 0;
        }

        if (bit == 0) {
            currentNode = currentNode.getLeftChildNode();
        }
        if (bit == 1) {
            currentNode = currentNode.getRightChildNode();
        }

        if (currentNode.isLeaf()) {
            decodedByte = currentNode.getUniqueByte();
            isByteDecoded = true;
            return;
        }
        ++flag;
    }

    /**
     * This method returns decoded byte.
     * The readiness of the decoded byte
     * is checked by HTree.isByteDecoded() method.
     */
    public byte getDecodedByte() {
        isByteDecoded = false;
        flag = 0;
        return decodedByte;
    }

    /**
     * This method returns readiness of the decoded byte.
     * if byte is completely decoded it returns true
     */
    public boolean isByteDecoded() {
        return isByteDecoded;
    }


    /*
     * Following chunk of code contains methods that helps
     * to debug program
     *  |
     *  V
     */

    /**
     * This method is used for comparing Huffman trees
     * for debugging purposes.
     */
    public boolean isEqual(HTree otherTree) {
        byte[] thisTreeStructure = this.getTreeStructure();
        byte[] thisTreeLeaves = this.getTreeLeaves();
        short thisTreeSize;
        try {
            thisTreeSize = this.getTreeSize();
        } catch (Exception e) {
            return false;
        }

        byte[] otherTreeStructure = otherTree.getTreeStructure();
        byte[] otherTreeLeaves = otherTree.getTreeLeaves();
        short otherTreeSize;
        try {
            otherTreeSize = otherTree.getTreeSize();
        } catch (Exception e) {
            return false;
        }

        if (thisTreeSize != otherTreeSize) {
            return false;
        }

        if (thisTreeStructure.length != otherTreeStructure.length) {
            return false;
        }
        if (thisTreeLeaves.length != otherTreeLeaves.length) {
            return false;
        }

        for (int i = 0; i < thisTreeStructure.length; i++) {
            if (thisTreeStructure[i] != otherTreeStructure[i]) {
                return false;
            }
        }

        for (int i = 0; i < thisTreeLeaves.length; i++) {
            if (thisTreeLeaves[i] != otherTreeLeaves[i]) {
                return false;
            }
        }
        return true;
    }


}

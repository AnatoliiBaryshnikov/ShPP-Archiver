package com.shpp.p2p.cs.abaryshnikov.assignment15;

/**
 * This class presents a node of a Huffman tree
 */
public class HTreeNode implements Comparable<HTreeNode> {

    /*
     * Fields
     */

    private HTreeNode leftChildNode = null;
    private HTreeNode rightChildNode = null;
    private HTreeNode parentNode = null;

    private Byte uniqueByte = null;
    private int frequency;

    /*
     * Constructors
     */
    public HTreeNode(){
        frequency = 0;
    }

    HTreeNode(int frequency){
        this.frequency = frequency;
    }

    HTreeNode(int frequency, HTreeNode left, HTreeNode right){
        this.frequency = frequency;
        leftChildNode = left;
        rightChildNode = right;
    }

    HTreeNode(byte uniqueByte){
        this.uniqueByte = uniqueByte;
        this.frequency = 0;
    }

    HTreeNode(byte uniqueByte, int frequency){
        this.uniqueByte = uniqueByte;
        this.frequency = frequency;
    }

    /*
     * Methods
     */

    public boolean isLeaf() {
        return (uniqueByte != null);
    }

    public boolean isRoot() throws Exception {
        return (parentNode == null);
    }

    public boolean isRestored() throws Exception {
        return (frequency == 0);
    }

    public void setUniqueByte(Byte uniqueByte) {
        this.uniqueByte = uniqueByte;
    }

    public byte getUniqueByte() {
        return uniqueByte;
    }

    public int getFrequency(){
        return frequency;
    }

    public void setFrequency(int frequency){
        this.frequency = frequency;
    }

    public void setLeftChildNode (HTreeNode lNode){
        leftChildNode = lNode;
    }

    public void setRightChildNode (HTreeNode rNode){
        rightChildNode = rNode;
    }

    public HTreeNode getLeftChildNode (){
        return leftChildNode;
    }

    public HTreeNode getRightChildNode (){
        return rightChildNode;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public HTreeNode clone(){
        HTreeNode clone = new HTreeNode();
        clone.rightChildNode = this.rightChildNode;
        clone.leftChildNode = this.leftChildNode;
        clone.parentNode = this.parentNode;
        clone.uniqueByte = this.uniqueByte;
        clone.frequency = this.frequency;
        return clone;
    }

    @Override
    public int compareTo(HTreeNode otherNode) {
        return this.frequency - otherNode.frequency;
    }
}

package com.shpp.p2p.cs.abaryshnikov.assignment15;

import java.io.InputStream;
import java.util.TreeMap;


/**
 *  This class is used for testing and debugging code.
 *  It looks a bit messy - I know :)
 */

public class TestAndDebug implements Constants {

    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        testCompDecompr("mytest/dictionary.txt");
        testCompDecompr("mytest/mono.bmp");

        String inputData = "ab ab cab!";
        if ( testTreeEquality(inputData) ) {
            System.out.println("> Trees are equal");
        } else {
            System.err.println("Error: Trees are not equal");
        }

        System.out.println("Passed time: " + (System.currentTimeMillis()-start));

    }


    private static void testCompDecompr(String inputFName) {

        try {
            Thread.sleep(500); // pause for correct exception (if any) appearance in the console

            System.out.println(ANSI_YELLOW + "\n \t Processing file [" + inputFName + "]" + ANSI_RESET);

            String comprFName = inputFName + "_tst_compr.par";

            int indexOfExtDot = inputFName.lastIndexOf( ".");
            String extension = inputFName.substring(indexOfExtDot );
            String decomprFName = inputFName + "_tst_decompr_" + extension;

            int resultCompr  = Compressor.compress(inputFName, comprFName);

            if(resultCompr != RETURN_VALUE_SUCCESS){
                System.err.println("Error when compressing file [" + inputFName + "]");
                return;
            }

            Thread.sleep(500); // pause for correct exception (if any) appearance in the console

            int resultDecompr  = Decompressor.decompress(comprFName, decomprFName);

            if(resultDecompr != RETURN_VALUE_SUCCESS){
                System.err.println("Error when decompressing file [" + inputFName + "]");
                return;
            }

            InputStream o = IOStreamManager.createInputStream(inputFName);
            byte[] originalFile = IOStreamManager.readAllFromStreamToBuffer(o);

            InputStream d = IOStreamManager.createInputStream(inputFName);
            byte[] decompressedFile = IOStreamManager.readAllFromStreamToBuffer(d);

            if (originalFile.length != decompressedFile.length) {
                System.err.println("Error when compressing/decompressing file [" + inputFName + "]. Length of files are not equal");
                return;
            }

            for (int i = 0; i < originalFile.length; ++i) {
                if (originalFile[i] != decompressedFile[i]) {
                    System.err.println("Error when compressing/decompressing file [" + inputFName + "]. Bytes are not equal");
                    return;
                }
            }

            System.out.println( ANSI_GREEN + "File [" + inputFName +"] was successfully compressed and decompressed" + ANSI_RESET);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception was thrown when processing file [" + inputFName +"]");
        }

        System.err.println("Error when compressing/decompressing file [" + inputFName + "]. Bytes are not equal");
    }



    private static boolean testTreeEquality(String inputData){

        byte[] inputDataAsBytes = inputData.getBytes();

        TreeMap<Byte, Integer> uniqueBytesAndFrequencies = new TreeMap<>();
        for (Byte bt : inputDataAsBytes) {
            if (uniqueBytesAndFrequencies.containsKey(bt)) {
                uniqueBytesAndFrequencies.put(bt, uniqueBytesAndFrequencies.get(bt) + 1);
            } else {
                uniqueBytesAndFrequencies.put(bt, 1);
            }
        }

        HTree tree = null;
        try {
            tree = new HTree(uniqueBytesAndFrequencies);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        byte[] treeStructure = tree.getTreeStructure();
        byte[] treeLeaves = tree.getTreeLeaves();

        HTree restoredTree = null;
        try {
            restoredTree = new HTree(treeStructure, treeLeaves);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        byte[] restoredtreeStructure = restoredTree.getTreeStructure();
        byte[] restoredtreeLeaves = restoredTree.getTreeLeaves();

        return tree.isEqual(restoredTree);
    }



}

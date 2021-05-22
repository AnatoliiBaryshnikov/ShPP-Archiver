package com.shpp.p2p.cs.abaryshnikov.assignment15;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *  This class is used for decompressing (unarchiving) .par files
 */
public class Decompressor implements Constants {

    /**
     * This method is used for compressing file
     *
     * @param inputFName    path to .par file that need to be decompressed
     * @param outputFName   path to decompressed file
     */
    public static int decompress(String inputFName, String outputFName){

        System.out.println("Decompressing [" + inputFName + "] to [" + outputFName + "] ... ");

        /* remembering starting time */
        long start = System.currentTimeMillis();

        /*
         * creating streams
         */
        InputStream fin;
        OutputStream fout;
        try {
            fin = IOStreamManager.createInputStream(inputFName);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return RETURN_VALUE_FAIL;
        }
        try {
            fout = IOStreamManager.createOutputStream(outputFName);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            try {
                IOStreamManager.closeIOStreams(fin, null);
            } catch (IOException exception) {
                System.err.println(e.getMessage());
            }
            return RETURN_VALUE_FAIL;
        }

        /* reading input file size */
        long inputFileSize = IOStreamManager.getFileSize(inputFName);

        /*
         * Restoring Huffman tree from header of compressed file and decompressing
         */
        long uncomprFileSize;
        try {
            HTree tree = restoreHuffmanTree(fin);
            uncomprFileSize = getUncomprFileSize(fin);
            decompressFile(uncomprFileSize, tree, fin, fout);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return RETURN_VALUE_FAIL;
        }

        /* closing streams */
        try {
            IOStreamManager.closeIOStreams(fin, fout);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return RETURN_VALUE_FAIL;
        }

        /* timing and size output */
        long outputFileSize = IOStreamManager.getFileSize(outputFName);
        if (uncomprFileSize != outputFileSize){
            System.err.println("Size of original file and decompressed file are not equal. Some error occured");
            return RETURN_VALUE_FAIL;
        }
        System.out.println("Time to decompress: \t\t[" + (System.currentTimeMillis()-start) + "] ms"
                            +"\nSize of input file: \t\t[" + inputFileSize + "] bytes"
                            +"\nSize of decompressed file: \t[" + outputFileSize + "] bytes");

        return RETURN_VALUE_SUCCESS;
    }


    /**
     * This method is used for restoring Huffman tree from '.par' file
     * which header contains its flattened form.
     *
     * @param fin           file input stream that contains compressed '.par' file with header that
     *                      contains flattened Huffman tree;
     * @return              Huffman tree as instance of HTree class;
     * @throws Exception    when creation of Huffman tree is failed.
     */
    private static HTree restoreHuffmanTree(InputStream fin) throws Exception {

        /* reading size of tree */
        byte[] treeSizeAsBytes = fin.readNBytes(SHORT_SIZE_IN_BYTES);
        ByteBuffer wrapper = ByteBuffer.wrap(treeSizeAsBytes);
        short treeSize = wrapper.getShort();

        /* reading tree structure */
        int treeStructureSize = treeSize/8;
        if (treeSize%8 !=0){
            treeStructureSize +=1 ;
        }
        byte[] treeStructure = fin.readNBytes(treeStructureSize);

        /* counting leaves */
        int leavesCounter = 0;
        byte bit;
        for (byte b : treeStructure) {
            for (int j = 0; j < 8; j++) {
                bit = (byte) (b >> j);
                if ((bit & 0x01) == 0 & treeSize > 0) {
                    ++leavesCounter;
                }
                --treeSize;
            }
        }

        /* reading leaves */
        byte[] treeLeaves = fin.readNBytes(leavesCounter);

        /* creating Huffman Tree */
        return new HTree(treeStructure, treeLeaves);
    }

    /**
     * This method is used for getting decompressed file size from '.par' file
     * which header contains it.
     *
     * @param fin           file input stream that contains compressed '.par' file with header that
     *                      contains flattened Huffman tree;
     * @return              size of decompressed (original) file;
     * @throws Exception when reading from file is failed.
     */
    private static long getUncomprFileSize(InputStream fin) throws Exception{
        byte[] uncomprFileSizeAsBytes = fin.readNBytes(LONG_SIZE_IN_BYTES);
        ByteBuffer wrapper = ByteBuffer.wrap(uncomprFileSizeAsBytes);
        return wrapper.getLong();
    }

    /**
     * This method is used for decompressing file using Huffman algorithm
     * and bitwise operations.
     *
     * @param uncomprFileSize   size of decompressed (original) file;
     * @param tree              Huffman tree using which the input file was encoded;
     * @param fin               file input stream that contains input file (uncompressed);
     * @param fout              file output stream that contains output file (compressed);
     * @throws Exception        when decompressing is failed.
     */
    private static void decompressFile(long uncomprFileSize, HTree tree, InputStream fin, OutputStream fout) throws Exception {

        /* creating buffer in RAM to write decompressed bytes into */
        ArrayList<Byte> outputBuffer = new ArrayList<>();
        outputBuffer.ensureCapacity(OUTPUT_BUFFER_SIZE);

        /* preparing necessary variables */
        int shiftForCurrentIter = 0;
        byte[] inputBuffer = new byte[INPUT_BUFFER_SIZE];
        long decodedBytesCounter = 0;
        boolean isDecodingCurrentByte; // this flag is used for understanding if all bits from current byte are taken or not

        /*
         * reading file block by block, compressing and writing to file
         */
        while (fin.available() > 0) {

            if (fin.available() < INPUT_BUFFER_SIZE) {
                inputBuffer = new byte[fin.available()];
            }
            //noinspection ResultOfMethodCallIgnored
            fin.read(inputBuffer, 0, inputBuffer.length);

            /*
             * preforming main operations in decoding/decompressing of the bytes
             * that are present in the input buffer
             */
            for (byte currentByteToDecode : inputBuffer){

                isDecodingCurrentByte = true;

                while (isDecodingCurrentByte & decodedBytesCounter != uncomprFileSize){

                    /* feeding tree with bits until we get a decoded byte */
                    while (!tree.isByteDecoded()) {

                        /* returning to foreach loop to take next byte from buffer if current is decoded */
                        if (shiftForCurrentIter > 7) {
                            shiftForCurrentIter = 0;
                            isDecodingCurrentByte = false;
                            break;
                        }
                        /* feeding tree with bits from current byte 'b' */
                        byte bit = (byte) ((currentByteToDecode >> shiftForCurrentIter) & 1);
                        tree.decode(bit);
                        ++shiftForCurrentIter;
                    }

                    if (isDecodingCurrentByte){
                        /* adding decoded byte into output buffer */
                        outputBuffer.add(tree.getDecodedByte());
                        ++decodedBytesCounter;

                        /* if buffer is full or the end of file is met, bytes from buffer are written to file on hard drive*/
                        if (outputBuffer.size() > OUTPUT_BUFFER_SIZE) {
                            IOStreamManager.writeOutputBufferToFile(fout, outputBuffer);
                        }
                    }
                }
            }
        }

        /* writing last chunk of bytes */
        IOStreamManager.writeOutputBufferToFile(fout, outputBuffer);
    }

}

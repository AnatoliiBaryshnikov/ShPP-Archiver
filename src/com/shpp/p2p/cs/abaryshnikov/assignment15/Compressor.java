package com.shpp.p2p.cs.abaryshnikov.assignment15;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used for compressing (archiving) files to .par compressed format
 */
public class Compressor implements Constants {


    /**
     * This method is used for compressing file
     *
     * @param inputFName  path to file that need to be compressed;
     * @param outputFName path to compressed .par file.
     */
    public static int compress(String inputFName, String outputFName) {

        System.out.println("Compressing [" + inputFName + "] to [" + outputFName + "] ...");

        /* remembering starting time */
        long start = System.currentTimeMillis();

        /*
         * creating streams
         */
        InputStream fin;
        OutputStream fout;
        long inputFileSize;

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

        /*
         * forming header of the compressed file and performing compression
         */
        try {

            /* detecting size of file that need to be compressed */
            inputFileSize = IOStreamManager.getFileSize(inputFName);

            /* creating Huffman tree */
            HTree tree;
            short treeSize;

            tree = createHuffmanTree(fin);
            treeSize = tree.getTreeSize();

            /* encoding (flattening) Huffman Tree */
            byte[] treeStructure = tree.getTreeStructure();
            byte[] treeLeaves = tree.getTreeLeaves();

            /* writing header to output file */
            writeHeaderToOutputFile(fout,
                treeSize,
                treeStructure,
                treeLeaves,
                inputFileSize);

            /* recreating input stream and compressing input file */
            fin = IOStreamManager.createInputStream(inputFName);
            compressFile(tree, fin, fout);

        } catch (Exception e) {
            try {
                IOStreamManager.closeIOStreams(fin, fout);
            } catch (IOException exception) {
                System.err.println(e.getMessage());
            }
            System.err.println(e.getMessage());
            return RETURN_VALUE_FAIL;
        }

        /*
         * closing streams
         */
        try {
            IOStreamManager.closeIOStreams(fin, fout);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return RETURN_VALUE_FAIL;
        }

        /* Timing and size/effectiveness output */
        long outputFileSize = IOStreamManager.getFileSize(outputFName);
        System.out.println("Time to compress: \t\t\t[" + (System.currentTimeMillis() - start) + "] ms"
            + "\nSize of input file: \t\t[" + inputFileSize + "] bytes"
            + "\nSize of compressed file: \t[" + outputFileSize + "] bytes"
            + "\nCompression coef: \t\t\t[" + ((float) inputFileSize / (float) (outputFileSize) + "]"));

        return RETURN_VALUE_SUCCESS;
    }

    /**
     * This method is used for creating Huffman tree for unique bytes
     * of file that contained in input stream
     *
     * @param fin           file input stream that contains input file;
     * @throws Exception    when creating tree is failed.
     */
    private static HTree createHuffmanTree(InputStream fin) throws Exception {

        /* creating buffer and opening input stream */
        byte[] inputBuffer = new byte[INPUT_BUFFER_SIZE];

        /* determining unique bytes and counting frequencies */
        HashMap<Byte, Integer> uniqueBytesAndFrequencies = new HashMap<>();

        /* reading file block by block and counting unique bytes */
        while (fin.available() > 0) {

            if (fin.available() < INPUT_BUFFER_SIZE) {
                inputBuffer = new byte[fin.available()];
            }
            //noinspection ResultOfMethodCallIgnored
            fin.read(inputBuffer, 0, inputBuffer.length);

            for (Byte bt : inputBuffer) {
                if (uniqueBytesAndFrequencies.containsKey(bt)) {
                    uniqueBytesAndFrequencies.put(bt, uniqueBytesAndFrequencies.get(bt) + 1);
                } else {
                    uniqueBytesAndFrequencies.put(bt, 1);
                }
            }
        }

        /* creating Huffman Tree */
        return new HTree(uniqueBytesAndFrequencies);
    }

    /**
     * This method is used for writing header that contains
     * information about Huffman tree structure and file size
     * to the output (compressed) file
     *
     * @param fout              file output stream that contains output file;
     * @param treeSize          size of Huffman tree structure in bits;
     * @param treeStructure     Huffman tree structure as a sequence of bits
     *                          where '1' is a transitional node and '0' is
     *                          a "leaf" node that contains unique byte;
     * @param treeLeaves        sequence of unique bytes that appears in-order
     *                          in Huffman tree;
     * @param inputFileSize     size of original (uncompressed file);
     * @throws Exception        when writing header info to output file is failed.
     */
    private static void writeHeaderToOutputFile(OutputStream fout,
                                                short treeSize,
                                                byte[] treeStructure,
                                                byte[] treeLeaves,
                                                long inputFileSize) throws Exception {

        /* writing size of tree */
        ByteBuffer buf = ByteBuffer.allocate(SHORT_SIZE_IN_BYTES);
        buf.putShort(treeSize);
        IOStreamManager.writeFromBufferToStream(fout, buf.array());

        /* writing tree structure array */
        IOStreamManager.writeFromBufferToStream(fout, treeStructure);

        /* writing tree leaves array */
        IOStreamManager.writeFromBufferToStream(fout, treeLeaves);

        /* writing size of input (uncompressed) file */
        ByteBuffer dataLength = ByteBuffer.allocate(LONG_SIZE_IN_BYTES);
        dataLength.putLong(inputFileSize);
        IOStreamManager.writeFromBufferToStream(fout, dataLength.array());
    }

    /**
     * This method is used for compressing file using Huffman algorithm
     * and bitwise operations.
     *
     * @param tree      Huffman tree for input file;
     * @param fin       file input stream that contains input file (uncompressed);
     * @param fout      file output stream that contains output file (compressed);
     * @throws Exception    when compressing is failed.
     */
    private static void compressFile(HTree tree, InputStream fin, OutputStream fout) throws Exception {

        /* creating buffer in RAM to write compressed info into */
        ArrayList<Byte> outputBuffer = new ArrayList<>();
        outputBuffer.ensureCapacity(OUTPUT_BUFFER_SIZE);

        /* preparing necessary variables */
        int bitsContainer = 0;
        int containerShiftTrigger = 0;
        int bitsShiftTrigger = 0;
        int[] bitsAndTheirNumber;
        int bits;
        int significantBitsNumber;

        /* creating buffer and opening input stream */
        byte[] inputBuffer = new byte[INPUT_BUFFER_SIZE];

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
             * performing main operations on coding/compressing of bytes
             */
            for (byte currentByte : inputBuffer) {

                /* getting bits that encode current byte */
                bitsAndTheirNumber = tree.encodeByte(currentByte);
                bits = bitsAndTheirNumber[0];
                significantBitsNumber = bitsAndTheirNumber[1];

                /* writing bits that code current byte into bits container */
                while (bitsShiftTrigger < significantBitsNumber) {

                    /* if bits container is full, put it into output buffer as byte */
                    if (containerShiftTrigger > 7) {
                        outputBuffer.add((byte) (bitsContainer & 0xFF));
                        bitsContainer = 0;
                        containerShiftTrigger = 0;
                    }

                    bitsContainer = bitsContainer | ((bits & 1) << 8);
                    bits = bits >> 1;
                    bitsContainer = bitsContainer >> 1;
                    ++containerShiftTrigger;
                    ++bitsShiftTrigger;
                }
                bitsShiftTrigger = 0;

                /* if buffer is full, bytes from buffer are written to file on hard drive*/
                if (outputBuffer.size() > OUTPUT_BUFFER_SIZE) {
                    IOStreamManager.writeOutputBufferToFile(fout, outputBuffer);
                }
            }
        }

        /* completing last byte */
        while (containerShiftTrigger < 8) {
            bitsContainer = (bitsContainer >> 1);
            ++containerShiftTrigger;
        }
        outputBuffer.add((byte) (bitsContainer & 0xFF));

        /* writing last chunk of bytes */
        IOStreamManager.writeOutputBufferToFile(fout, outputBuffer);

    }

}

package com.shpp.p2p.cs.abaryshnikov.assignment15;

import java.io.*;
import java.util.ArrayList;

/**
 * This auxiliary class contains methods that allow for creating of IO streams
 * and reading/writing from/to the streams
 */
public class IOStreamManager implements Constants {

    /**
     * This method creates input data stream
     *
     * @param inputFName    path to file that is in-streamed
     * @return              InputStream
     * @throws IOException  when can`t create stream
     */
    public static InputStream createInputStream(String inputFName) throws IOException {

        InputStream fin;

        try {
            fin = new FileInputStream(inputFName);
        } catch (IOException e) {
            throw new IOException("Can`t create input stream. " +
                "Can`t open [" +inputFName+ "] - no such file or device was unplugged");
        }
        fin = new BufferedInputStream(fin); //todo: experiment with buffer size

        return fin;
    }

    /**
     * This method creates output data stream
     *
     * @param outputFName   path to file that is out-streamed
     * @return              OutputStream
     * @throws IOException  when can`t create stream
     */
    public static OutputStream createOutputStream(String outputFName) throws IOException {

        OutputStream fout;

        try {
            fout = new FileOutputStream(outputFName);
        } catch (IOException e) {
            throw new IOException("Can`t create output stream. " +
                "Device was unplugged or can`t create [" + outputFName + "]");
        }

        fout = new BufferedOutputStream(fout); //todo: experiment with buffer size

        return fout;
    }

    /**
     * This method is used for reading all available data from input stream to buffer
     * Be careful! reading all data with .available() method is not recommended for big files.
     * Recommended file size is less than 2 GB.
     *
     * @param fin           input stream
     * @return              byte array
     * @throws IOException  when reading from file failed
     */
    public static byte[] readAllFromStreamToBuffer(InputStream fin) throws IOException {
        byte[] buffer;
        try {
            buffer = new byte[fin.available()];
        } catch (IOException e) {
            throw new IOException("Can`t get amount of available data from file ");
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            fin.read(buffer);
        } catch (IOException e) {
            throw new IOException("Can`t read data from file");
        }

        return buffer;
    }

    /**
     * This method is used for writing data from buffer to output stream
     *
     * @param fout          output stream
     * @param buffer        buffer with data
     * @throws IOException  when writing to file failed
     */
    public static void writeFromBufferToStream(OutputStream fout, byte[] buffer) throws IOException {
        try {
            fout.write(buffer);
            fout.flush();       // todo: flush is here!
        } catch (IOException e) {
            throw new IOException("Can`t write data to file");
        }
    }

    /**
     * This method is used for closing input and output streams.
     * This is null-exception protected method, so if you want to close
     * only one stream you can pass another argument as null.
     *
     * @param fout          output stream or null
     * @param fin           input stream or null
     * @throws IOException  when closing streams failed
     */
    public static void closeIOStreams(InputStream fin, OutputStream fout) throws IOException {

        try {
            if (fin != null ){
                fin.close();
            }
        } catch (Exception eIn) {
            System.err.println(eIn.getMessage());

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception eOut) {
                System.err.println(eOut.getMessage());
                throw new IOException("Can`t close output stream correctly. Output file may be corrupted");
            }

            throw new IOException("Can`t close input stream correctly");
        }

        try {
            if (fout != null) {
                fout.close();
            }
        } catch (Exception eOut) {
            System.err.println(eOut.getMessage());
            throw new IOException("Can`t close output stream correctly. Output file may be corrupted");
        }

    }

    /**
     * This method returns the size of file in bytes
     *
     * @param fName    path to file that is in-streamed
     */
    public static long getFileSize (String fName){
        File file = new File(fName);
        return file.length();
    }

    /**
     * This method writes the containment of ArrayList<Byte> into file output stream as bytes
     * in the same sequence as they appear in the ArrayList
     *
     * @param fout          output stream
     * @param outputBuffer  ArrayList<Byte> that contains bytes must be written to output stream
     */
    public static void writeOutputBufferToFile(OutputStream fout, ArrayList<Byte> outputBuffer) throws IOException {
        byte[] out = new byte[outputBuffer.size()];
        for (int j = 0; j < outputBuffer.size(); j++) {
            out[j] = outputBuffer.get(j);
        }
        writeFromBufferToStream(fout, out);
        outputBuffer.clear();
    }

}

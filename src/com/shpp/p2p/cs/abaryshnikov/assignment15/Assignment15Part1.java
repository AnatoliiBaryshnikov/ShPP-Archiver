package com.shpp.p2p.cs.abaryshnikov.assignment15;

/**
 * ShPP Archiver
 *
 * This program realizes original Huffman algorithm
 * for compressing/decompressing files
 *
 * @author Anatolii Baryshnikov
 * @version 0.1.0
 *
 */
public class Assignment15Part1 implements Constants {

    /**
     * Main method.
     * The parameters are processed here and methods for
     * compressing/decompressing are also called from here.
     */
    public static void main(String[] args){

        String name_of_input_file;
        String name_of_archived_file;
        String name_of_unarchived_file;

        /* default logic, no parameters are provided */
        if (args.length == 0) {
            name_of_input_file = "test.txt";
            name_of_archived_file = "test.txt.par";
            int result = Compressor.compress(name_of_input_file, name_of_archived_file);
            ifCompressedCorrectly(result);
            return;
        }

        /* compressing/decompressing or showing help message when only one parameter is provided */
        if (args.length == 1) {

            if ( args[0].equals("-help") ){
                help();
                return;
            }

            name_of_input_file = args[0];
            int indexOfExtDot = name_of_input_file.lastIndexOf( ".");
            String extension = name_of_input_file.substring(indexOfExtDot );

            if ( extension.equals(".par") ){
                int result = Decompressor.decompress(name_of_input_file, (name_of_input_file.substring(0, indexOfExtDot ) + ".uar"));
                ifDecompressedCorrectly(result);
                return;
            }

            name_of_archived_file = name_of_input_file + ".par";
            int result =  Compressor.compress(name_of_input_file, name_of_archived_file);
            ifCompressedCorrectly(result);
            return;
        }

        /* compressing/decompressing when additional flags are provided */
        if (args.length == 3) {

            if (args[0].equals("-a") ){
                name_of_input_file = args[1];
                name_of_archived_file = args[2];
                int result = Compressor.compress(name_of_input_file, name_of_archived_file);
                ifCompressedCorrectly(result);
                return;
            }

            if (args[0].equals("-u") ){
                name_of_archived_file = args[1];
                name_of_unarchived_file = args[2];
                int result = Decompressor.decompress(name_of_archived_file, name_of_unarchived_file);
                ifDecompressedCorrectly(result);
                return;
            }

            System.err.println("Please, check parameters input or use parameter '-help' to get help:)");
            return;
        }

        System.err.println("Please, check parameters input or use parameter '-help' to get help:)");
    }

    /**
     * This method checks the returned result of the decompressing function
     * and provides appropriate CLI output.
     *
     * @param returnedResult int value that is returned by decompressing function
     */
    private static void ifDecompressedCorrectly(int returnedResult){
        if (returnedResult != RETURN_VALUE_SUCCESS){
            System.err.println("Some error occurred during decompressing. Result file could be corrupted.");
            return;
        }
        System.out.println("File decompressed successfully!");
    }

    /**
     * This method checks the returned result of the compressing function
     * and provides appropriate CLI output.
     *
     * @param returnedResult int value that is returned by compressing function
     */
    private static void ifCompressedCorrectly(int returnedResult){
        if (returnedResult != RETURN_VALUE_SUCCESS){
            System.err.println("Some error occurred during compressing. Result file could be corrupted.");
            return;
        }
        System.out.println("File compressed successfully!");
    }

    /** This method provides help message */
    private static void help(){
        System.out.println("\n" +
                           "                      -*-| ShPP Archiver 0.1.0 |-*-\n" +
                           "\n" +
                           "    This program can be used for compressing/decompressing files.\n" +
                           "The program is based on the Huffman algorithm. The format of the archived files\n" +
                           "is '.par'. The program has been developed as a study assignment during studying\n" +
                           "in Ш++ School https://programming.org.ua\n" +
                           "\n" +
                           "    If you run program without any parameters it tries to compress 'test.txt'\n" +
                           "file by default. If you run program and provide file name as a parameter,\n" +
                           "the file will be compressed. But in case the file extension is '.par' it is\n" +
                           "decompressed with the '.uar' extension of the result file. For example:\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "  |           program call            |           result                     |\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "  |    >*program*                     |      test.txt.uar (1)                |\n" +
                           "  |    >*program*  file.ext           |      test.ext.par                    |\n" +
                           "  |    >*program*  file.ext.par       |      test.ext.uar                    |\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "    (1) - if 'test.txt' is present\n" +
                           "\n" +
                           "    Also you can directly archive/unarchive file into other file using\n" +
                           "flags '-a' and '-u' respectively. For example:\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "  |           program call            |           result                     |\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "  |    >*program*  -a f.ext f.ext.par |      f.ext compressed to f.ext.par   |\n" +
                           "  |    >*program*  -u f.ext.par f.ext |      f.ext.par decompressed to f.ext |\n" +
                           "  ----------------------------------------------------------------------------\n" +
                           "\n" +
                           "    Have fun!:)\n");
    }


}

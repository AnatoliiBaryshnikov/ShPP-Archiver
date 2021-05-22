                        -*-| ShPP Archiver 0.1.0 |-*-

    This program can be used for compressing/decompressing files.
The program is based on the Huffman algorithm. The format of the archived files
is '.par'. The program has been developed as a study assignment during studying
in Ø++ School https://programming.org.ua

    If you run program without any parameters it tries to compress 'test.txt'
file by default. If you run program and provide file name as a parameter,
the file will be compressed. But in case the file extension is '.par' it is
decompressed with the '.uar' extension of the result file. For example:
----------------------------------------------------------------------------
|           program call            |           result                     |
----------------------------------------------------------------------------
|    >*program*                     |      test.txt.uar (1)                |
|    >*program*  file.ext           |      test.ext.par                    |
|    >*program*  file.ext.par       |      test.ext.uar                    |
----------------------------------------------------------------------------
    (1) - if 'test.txt' is present

    Also you can directly archive/unarchive file into other file using
flags '-a' and '-u' respectively. For example:
----------------------------------------------------------------------------
|           program call            |           result                     |
----------------------------------------------------------------------------
|    >*program*  -a f.ext f.ext.par |      f.ext compressed to f.ext.par   |
|    >*program*  -u f.ext.par f.ext |      f.ext.par decompressed to f.ext |
----------------------------------------------------------------------------

    Have fun!:)
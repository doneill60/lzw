/******************************************************************************
 *  Compilation:  javac MyLZW.java
 *  Execution:    java MyLZW - < input.txt   (compress)
 *  Execution:    java MyLZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   http://algs4.cs.princeton.edu/55compression/abraLZW.txt
 *                http://algs4.cs.princeton.edu/55compression/ababLZW.txt
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;
import edu.princeton.cs.algs4.TST;
import java.lang.Math;
import java.io.File;
import java.util.Arrays;

/*	This code was adapted from Algorithms 4th edition by Robert Sedgwick and Kevin Wayne
 *	for educational purposes only.
 *
 *
 *  The {@code MyLZW} class provides static methods for compressing
 *  and expanding a binary input using LZW compression over the 8-bit extended
 *  ASCII alphabet with 12-bit codewords.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/55compress">Section 5.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class MyLZW {
    private static final int R = 256;        // number of input chars/branching factor
	private static int mode;
	private static int W = 9;         // codeword width
    private static int L = (int)Math.pow(2,W);       // number of codewords = 2^W
	private static final int resetValue = (int)Math.pow(2,16);
	private static int compressedBits = 0;
	private static int uncompressedBits = 0;
	private static boolean initialRatio = false;
	private static int newRatio=0;
	private static int oldRatio=0;

    // Do not instantiate.
    private MyLZW() { }

    /**
     * Reads a sequence of 8-bit bytes from standard input; compresses
     * them using LZW compression with 12-bit codewords; and writes the results
     * to standard output.
     */
    public static void compress() {
		BinaryStdOut.write(mode, 9);
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
		//intitialize the symbol table to contain only the alphabet
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
		// R is codeword for EOF
        int code = R+1;  
		
        while (input.length() > 0) {
			// Find max prefix match s.
            String s = st.longestPrefixOf(input); 
			// Print s's encoding to file
            BinaryStdOut.write(st.get(s), W);  
			compressedBits += W;
            int t = s.length();
			uncompressedBits+=t*8;
			newRatio = uncompressedBits/compressedBits;
			if(code==resetValue && mode == 2){
				st = new TST<Integer>();
				for (int i = 0; i < R; i++)
					st.put("" + (char) i, i);
				code = R+1;
				W=9;
				L = (int)Math.pow(2,W);
			}
			if(code == resetValue && mode==3){
				if(!initialRatio){
					oldRatio=newRatio;
					initialRatio=true;
				}
				if(newRatio!=0 && oldRatio/newRatio>1.1){
					st = new TST<Integer>();
					for (int i = 0; i < R; i++)
						st.put("" + (char) i, i);
					code = R+1; 
					W=9;
					L = (int)Math.pow(2,W);
					oldRatio=0;
					newRatio=0;
					initialRatio = false;
				}	
			}
			//while you can peek ahead
			// Add s to symbol table.
            if (t < input.length() && code < L && W<=16) 
                st.put(input.substring(0, t + 1), code++);
			//increase codebook size
			if(code== L && W<16){
				W++;
				L = (int)Math.pow(2,W);
			}
			// Scan past s in input.
            input = input.substring(t);
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    }

    /**
     * Reads a sequence of bit encoded using LZW compression with
     * 12-bit codewords from standard input; expands them; and writes
     * the results to standard output.
     */
	
    public static void expand() {
		mode = BinaryStdIn.readInt(W);
		W=9;
		L = (int)Math.pow(2,W);
        String[] st = new String[L];
        int i; // next available codeword value
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";   		// (unused) lookahead for EOF
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        while (true) {
			//write to text your new value associated with codeword index
            BinaryStdOut.write(val);
			uncompressedBits+=val.length()*8;
			compressedBits += W;
			newRatio = uncompressedBits/compressedBits;
			//get new codeword
			if(mode==2 && i==resetValue){;
				st = new String[L];
				for (i = 0; i < R; i++)
					st[i] = "" + (char) i;
				st[i++] = "";
				i=R+1;
				W=9;
				L = (int)Math.pow(2,W);	
			}
			if(mode==3 && i==resetValue){
				if(!initialRatio){
					oldRatio=newRatio;
					initialRatio = true;
				}
				if(newRatio!=0 && oldRatio/newRatio>1.1){
					oldRatio = 0;
					newRatio = 0;
					initialRatio = false;
					st = new String[L];
					for (i = 0; i < R; i++)
						st[i] = "" + (char) i;
					st[i++] = "";
					i=R+1;
					W=9;
					L = (int)Math.pow(2,W);	
				}
			}
            codeword = BinaryStdIn.readInt(W);
			//got to the end
            if (codeword == R){
				break;
			}
			String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L && W<=16) st[i++] = val + s.charAt(0);
			if(i == L-1 && W<16){
				W++;
				L = (int)Math.pow(2,W);	
				st = Arrays.copyOf(st, L);
			}				
            val = s;
        }
        BinaryStdOut.close();
    }
    /**
     * Sample client that calls {@code compress()} if the command-line
     * argument is "-" an {@code expand()} if it is "+".
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
		if 			(args[0].equals("+")) expand();
		else if     (args[0].equals("-")){
			if			(args[1].equals("n")) mode = 1;
			else if 	(args[1].equals("r")) mode = 2;
			else if 	(args[1].equals("m")) mode = 3;
			compress();
		}		
		else throw new IllegalArgumentException("Illegal command line argument");
    }
	

}

/******************************************************************************
 *  Copyright 2002-2016, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/

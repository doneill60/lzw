# lzw
This is an adaptation of lzw code from algs4 by Robert Sedgewick and Kevin Wayne for a school project.
It is enhanced to include variable width codewords to help with the compression of more repetitive data.
Once the codebook is "full", it does one of three things,
1. Nothing
  (Continue to use the same patterns stored in the codebook)
2. Reset
  (Once the codebook becomes filled simply dump the codebook and start anew)
3. Monitor
  (Monitor the compression ratio. If it should exceed the specified ratio, then reset the codebook and start over.)

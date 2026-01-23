// Ali Raza - 400338538
// Lab 1: Prefix-free Codes Using Binary Trees
// McMaster University - COMP ENG 3SM4

import java.util.ArrayList;

public class BinTree {
    private TNode root;
    private int num;  // keeping track of how many codewords we have

    /* Default constructor - just need to start with an empty tree
     * Time: O(1) - this is instant since I'm just making one node
     * Space: O(1) - also just one node worth of space */
    public BinTree(){
        root = new TNode(null, null, null);  // empty root to start building from
    }

    /* This helper does the heavy lifting for inserting codewords
     * Time: O(|binary|) - I walk down the tree following the binary string, so it's proportional to how long the string is
     * Space: O(|binary|) - worst case I might need to create a whole new path, so same as the string length */
    private void insertCodeword(String symbol, String binary) throws IllegalArgumentException {
        TNode node = this.root;  // start at the top

        int n = binary.length();
        char c;

        // walk through each bit in the codeword
        for(int i = 0; i < n; i++){
            c = binary.charAt(i);

            // if we see a 0, go left. if there's nothing there yet, make a new node
            if(c == '0'){
                if(node.left == null){
                    node.left = new TNode(null, null, null);  // create internal node
                }
                node = node.left;

            } else {  // c must be '1', so go right
                if(node.right == null){
                    node.right = new TNode(null, null, null);
                }
                node = node.right;
            }

            // uh oh - if we hit an existing codeword while walking down, that means this new one is extending an existing codeword (prefix violation!)
            if(node.data != null)
                throw new IllegalArgumentException("Prefix condition violated!");
        }

        // now we're at where this codeword should end up
        // if this spot already has children, it means some other codeword extends past here
        // which would make us a prefix of that codeword - not allowed!
        if(node.left != null || node.right != null)
            throw new IllegalArgumentException("Prefix condition violated!");
        else {
            node.data = symbol;  // claim this spot
            num++;  // one more codeword in the tree
        }
    }

    /* Constructor that builds the whole tree from an array
     * Time: O(n*m) - need to validate all n strings, then insert each one. Each insertion walks down the tree, so roughly n times the average codeword length m
     * Space: O(n*m) - building a tree with n codewords, space grows with total length of all codewords */
    public BinTree(String[] a) throws IllegalArgumentException {
        // first pass - make sure everything in the array is actually binary
        int k = a.length;
        char c;

        for(int i = 0; i < k; i++){
            int n = a[i].length();
            // check every character in this string
            for(int j = 0; j < n; j++){
                c = a[i].charAt(j);
                if(c != '0' && c != '1'){  // found something that's not a bit!
                    throw new IllegalArgumentException("Invalid argument!");
                }
            }
        }

        // okay, all inputs are valid binary strings. let's build the tree
        this.root = new TNode(null, null, null);

        // insert each codeword one by one
        for(int i = 0; i < k; i++){
            String symbol = "c" + i;  // c0 for first codeword, c1 for second, etc.
            insertCodeword(symbol, a[i]);  // this will catch any prefix violations
        }
    }

    /* Print the tree in order - provided code from the manual
     * Time: O(n) - touch every node once where n is number of nodes
     * Space: O(h) - goes as deep as the tree height h */
    public void printTree(){
        printTree(root);
    }

    /* Helper for inorder traversal
     * Time: O(n) - visit each of n nodes exactly once
     * Space: O(h) - recursion stack depth equals tree height */
    private void printTree(TNode t){
        if(t != null){
            printTree(t.left);
            if(t.data == null)  // internal nodes show as "I"
                System.out.print("I ");
            else  // leaf nodes show their symbol
                System.out.print(t.data + " ");
            printTree(t.right);
        }
    }

    /* Simple getter for the count
     * Time: O(1) - instant, just returning a variable
     * Space: O(1) - no extra space needed */
    public int getNumberOfCodewords(){
        return num;
    }

    /* Calculate how tall the tree is
     * Time: O(n) - have to visit every node to find the deepest path
     * Space: O(h) - recursion stack can go as deep as the tree height */
    public int height(){
        return height(root);
    }

    /* Recursive helper to compute height
     * Time: O(n) - visits each node once in postorder fashion
     * Space: O(h) - call stack depth is proportional to height */
    private int height(TNode t){
        if(t == null){
            return -1;  // empty means height -1
        }
        // find the taller subtree and add 1 for this level
        int leftHeight = height(t.left);
        int rightHeight = height(t.right);
        return 1 + Math.max(leftHeight, rightHeight);
    }

    /* Get all the codewords in lexicographical order
     * Time: O(n*m) - visit all n nodes, building path strings as we go (each of avg length m)
     * Space: O(k*m) - need to store all k codewords we find, plus O(h) recursion overhead */
    public ArrayList<String> getCodewords(){
        ArrayList<String> list = new ArrayList<String>();
        getCodewords(root, "", list);  // start with empty path
        return list;
    }

    /* Helper that traverses tree collecting codewords
     * Time: O(n*m) - visit each node, string ops cost O(m) per node
     * Space: O(h*m) - recursion depth h, each level has a path string of length up to m */
    private void getCodewords(TNode t, String path, ArrayList<String> list){
        if(t != null){
            if(t.data != null){
                // found a leaf! this path is a complete codeword
                list.add(path);
            } else {
                // internal node - keep going. left first (0) then right (1) gives us lex order
                getCodewords(t.left, path + "0", list);
                getCodewords(t.right, path + "1", list);
            }
        }
    }

    /* Decode a binary string into symbols
     * Time: O(|s|) - walk through the input string once, following the tree
     * Space: O(k) - just storing the k symbols we decode */
    public ArrayList<String> decode(String s){
        ArrayList<String> result = new ArrayList<String>();
        TNode node = root;

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            // follow the tree based on each bit
            if(c == '0'){
                node = node.left;
            } else {
                node = node.right;
            }

            // did we hit a codeword?
            if(node.data != null){
                result.add(node.data);  // got one!
                node = root;  // reset to decode the next codeword
            }
        }

        return result;
    }

    /* Build the string representation in (symbol,codeword) format
     * Time: O(n*m + k^2) - tree traversal O(n*m), plus insertion sort behavior O(k^2) when building pairs
     * Space: O(k*m) - storing k symbols and k codewords, each of average length m */
    public String toString(){
        ArrayList<String> symbols = new ArrayList<String>();
        ArrayList<String> codewords = new ArrayList<String>();

        // gather all the pairs from the tree
        collectPairs(root, "", symbols, codewords);

        // now format them nicely
        String result = "";
        for(int i = 0; i < symbols.size(); i++){
            result += "(" + symbols.get(i) + "," + codewords.get(i) + ") ";
        }

        return result;
    }

    /* Helper to traverse tree and collect pairs in sorted order
     * Time: O(n*m + k^2*m) - visit each node, but sorting as we insert can be slow with k codewords
     * Space: O(h*m) - recursion depth is tree height h, plus the path string at each level */
    private void collectPairs(TNode t, String path, ArrayList<String> symbols, ArrayList<String> codewords){
        if(t != null){
            if(t.data != null){
                // found a codeword - figure out where it goes in sorted order
                int index = getInsertIndex(symbols, t.data);
                symbols.add(index, t.data);
                codewords.add(index, path);
            } else {
                // keep searching
                collectPairs(t.left, path + "0", symbols, codewords);
                collectPairs(t.right, path + "1", symbols, codewords);
            }
        }
    }

    /* Find where to insert this symbol to keep everything sorted by index
     * Time: O(k) - linear search through k existing symbols
     * Space: O(1) - just a couple of temp variables */
    private int getInsertIndex(ArrayList<String> symbols, String newSymbol){
        // pull out the number from the symbol (c0 -> 0, c1 -> 1, etc.)
        int newIndex = Integer.parseInt(newSymbol.substring(1));

        // find where this should go
        for(int i = 0; i < symbols.size(); i++){
            int currentIndex = Integer.parseInt(symbols.get(i).substring(1));
            if(newIndex < currentIndex){
                return i;  // found the spot
            }
        }

        // bigger than everything else, goes at the end
        return symbols.size();
    }

    /* Time: O(2^h) - need an array that's exponential in height, then fill in the n nodes we have
     * Space: O(2^h) - array size is 2^(h+1) which can get big for tall trees */
    public String[] toArray(){
        int h = height();
        int size = (int)Math.pow(2, h + 1);  // standard binary tree array size
        String[] arr = new String[size];

        // Java initializes to null, which is what we want for missing nodes
        toArray(root, "", arr, 1);  // start at index 1 (skip 0)

        return arr;
    }

    /* Recursively fill the array using index arithmetic for tree positions
    
     * Time: O(n*m) - visit each of the n actual nodes in the tree, store paths of length m
     * Space: O(h*m) - recursion depth h and path string at each level */
    private void toArray(TNode t, String path, String[] arr, int index){
        if(t != null){
            if(t.data != null){
                // leaf - store the actual codeword
                arr[index] = path;
            } else {
                // internal node - just mark it with "I"
                arr[index] = "I";
            }

            // standard binary tree indexing: left child is 2*i, right is 2*i+1
            toArray(t.left, path + "0", arr, 2 * index);
            toArray(t.right, path + "1", arr, 2 * index + 1);
        }
    }
}

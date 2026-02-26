// Ali Raza - 400338538
// Lab 1 Testing - Comprehensive Tests for BinTree Implementation
// McMaster University - COMP ENG 3SM4

import java.util.ArrayList;

public class Test {
    public static void main(String[] args){

        System.out.println("\n==============================");
        System.out.println("   TESTING SUITE - 400338538");
        System.out.println("================================\n");

        // First, let's test the basic empty tree constructor
        System.out.println(">>> Test 1: Empty Tree Constructor");
        System.out.println("Creating an empty BinTree...");
        BinTree emptyTree = new BinTree();
        System.out.print("Tree structure: ");
        emptyTree.printTree();
        int emptyCount = emptyTree.getNumberOfCodewords();
        System.out.println("\nCodeword count: " + emptyCount + " (should be 0)");
        boolean test1Pass = (emptyCount == 0);
        System.out.println("Pass: " + test1Pass);
        System.out.println();

        // Now test building a tree from an array - the main constructor
        System.out.println(">>> Test 2: Building Tree from Valid Codewords");
        String[] codes = {"10", "0", "111", "110"};
        System.out.println("Input codewords: 10, 0, 111, 110");
        BinTree mainTree = new BinTree(codes);
        System.out.print("Tree inorder: ");
        mainTree.printTree();
        int mainCount = mainTree.getNumberOfCodewords();
        System.out.println("\nTotal codewords: " + mainCount + " (expecting 4)");
        boolean test2Pass = (mainCount == 4);
        System.out.println("Pass: " + test2Pass);
        System.out.println();

        // What happens if someone tries to sneak in non-binary characters?
        System.out.println(">>> Test 3: Catching Non-Binary Input");
        String[] badInput1 = {"10", "0", "1a1", "110"};
        System.out.println("Trying to insert: 10, 0, 1a1, 110");
        System.out.println("(notice the 'a' in third codeword)");
        boolean test3Pass = false;
        try {
            new BinTree(badInput1);
            System.out.println("ERROR: Should have caught this!");
        } catch(IllegalArgumentException e) {
            System.out.println("Caught exception: " + e.getMessage());
            test3Pass = e.getMessage().equals("Invalid argument!");
        }
        System.out.println("Pass: " + test3Pass);
        System.out.println();

        // Testing prefix violations - this is important
        System.out.println(">>> Test 4: Detecting Prefix Violations");
        String[] badInput2 = {"10", "100", "111", "110"};
        System.out.println("Trying: 10, 100, 111, 110");
        System.out.println("(note: '10' is a prefix of '100')");
        boolean test4Pass = false;
        try {
            new BinTree(badInput2);
            System.out.println("ERROR: Missed the prefix violation!");
        } catch(IllegalArgumentException e) {
            System.out.println("Caught exception: " + e.getMessage());
            test4Pass = e.getMessage().equals("Prefix condition violated!");
        }
        System.out.println("Pass: " + test4Pass);
        System.out.println();

        // What about duplicates?
        System.out.println(">>> Test 5: Duplicate Codeword Detection");
        String[] badInput3 = {"10", "0", "10", "110"};
        System.out.println("Trying: 10, 0, 10, 110");
        System.out.println("(10 appears twice)");
        boolean test5Pass = false;
        try {
            new BinTree(badInput3);
            System.out.println("ERROR: Duplicates should not be allowed!");
        } catch(IllegalArgumentException e) {
            System.out.println("Caught exception: " + e.getMessage());
            test5Pass = e.getMessage().equals("Prefix condition violated!");
        }
        System.out.println("Pass: " + test5Pass);
        System.out.println();

        // Test the height calculation
        System.out.println(">>> Test 6: Tree Height Calculations");
        int emptyHeight = emptyTree.height();
        int mainHeight = mainTree.height();
        System.out.println("Empty tree height: " + emptyHeight + " (expect 0)");
        System.out.println("Main tree height: " + mainHeight + " (expect 3)");

        String[] shortCodes = {"0", "10", "11"};
        BinTree shortTree = new BinTree(shortCodes);
        int shortHeight = shortTree.height();
        System.out.println("Short tree {0, 10, 11} height: " + shortHeight + " (expect 2)");
        boolean test6Pass = (emptyHeight == 0 && mainHeight == 3 && shortHeight == 2);
        System.out.println("Pass: " + test6Pass);
        System.out.println();

        // Get the codewords back - should be in lex order
        System.out.println(">>> Test 7: Retrieving Codewords (Lexicographical Order)");
        ArrayList<String> retrievedCodes = mainTree.getCodewords();
        System.out.println("Codewords from tree: " + retrievedCodes);
        System.out.println("Should be: [0, 10, 110, 111]");
        boolean test7Pass = retrievedCodes.size() == 4 &&
                            retrievedCodes.get(0).equals("0") &&
                            retrievedCodes.get(1).equals("10") &&
                            retrievedCodes.get(2).equals("110") &&
                            retrievedCodes.get(3).equals("111");
        System.out.println("Pass: " + test7Pass);
        System.out.println();

        // Decoding binary strings
        System.out.println(">>> Test 8a: Decoding Binary Strings");
        String message1 = "11011110110000";
        System.out.println("Encoded message: " + message1);
        ArrayList<String> decoded1 = mainTree.decode(message1);
        System.out.println("Decoded symbols: " + decoded1);
        System.out.println("Expected: [c3, c2, c0, c3, c1, c1, c1]");
        boolean test8aPass = decoded1.size() == 7 &&
                             decoded1.get(0).equals("c3") &&
                             decoded1.get(1).equals("c2") &&
                             decoded1.get(2).equals("c0") &&
                             decoded1.get(3).equals("c3") &&
                             decoded1.get(4).equals("c1") &&
                             decoded1.get(5).equals("c1") &&
                             decoded1.get(6).equals("c1");
        System.out.println("Pass: " + test8aPass);
        System.out.println();

        System.out.println(">>> Test 8b: Another Decoding Test");
        String message2 = "0011001010111";
        System.out.println("Encoded message: " + message2);
        ArrayList<String> decoded2 = mainTree.decode(message2);
        System.out.println("Decoded symbols: " + decoded2);
        System.out.println("Expected: [c1, c1, c3, c1, c0, c0, c2]");
        boolean test8bPass = decoded2.size() == 7 &&
                             decoded2.get(0).equals("c1") &&
                             decoded2.get(1).equals("c1") &&
                             decoded2.get(2).equals("c3") &&
                             decoded2.get(3).equals("c1") &&
                             decoded2.get(4).equals("c0") &&
                             decoded2.get(5).equals("c0") &&
                             decoded2.get(6).equals("c2");
        System.out.println("Pass: " + test8bPass);
        System.out.println();

        // Testing toString - format matters here
        System.out.println(">>> Test 9: String Representation Format");
        String treeString = mainTree.toString();
        System.out.println("toString output: " + treeString);
        System.out.println("Expected format: (c0,10) (c1,0) (c2,111) (c3,110) ");
        boolean test9Pass = treeString.equals("(c0,10) (c1,0) (c2,111) (c3,110) ");
        System.out.println("Pass: " + test9Pass);
        System.out.println();

        System.out.println("Let's try with the shorter tree:");
        String shortString = shortTree.toString();
        System.out.println("Short tree toString: " + shortString);
        System.out.println("Expected: (c0,0) (c1,10) (c2,11) ");
        boolean shortStringPass = shortString.equals("(c0,0) (c1,10) (c2,11) ");
        System.out.println("Pass: " + shortStringPass);
        System.out.println();

        // The array representation
        System.out.println(">>> Test 10: Array Representation");
        String[] arrayForm = mainTree.toArray();
        System.out.println("Array size: " + arrayForm.length);
        System.out.println("Non-null entries:");
        for(int i = 0; i < arrayForm.length; i++){
            if(arrayForm[i] != null){
                System.out.println("  Position " + i + ": " + arrayForm[i]);
            }
        }
        System.out.println("\nExpected positions:");
        System.out.println("  Position 1: I (root)");
        System.out.println("  Position 2: 0");
        System.out.println("  Position 3: I");
        System.out.println("  Position 6: 10");
        System.out.println("  Position 7: I");
        System.out.println("  Position 14: 110");
        System.out.println("  Position 15: 111");
        boolean test10Pass = arrayForm[1].equals("I") &&
                             arrayForm[2].equals("0") &&
                             arrayForm[3].equals("I") &&
                             arrayForm[6].equals("10") &&
                             arrayForm[7].equals("I") &&
                             arrayForm[14].equals("110") &&
                             arrayForm[15].equals("111");
        System.out.println("Pass: " + test10Pass);
        System.out.println();

        // Edge case: single codeword tree
        System.out.println(">>> Test 11: Edge Case - Single Codeword");
        String[] singleCode = {"0"};
        BinTree singleTree = new BinTree(singleCode);
        System.out.print("Tree: ");
        singleTree.printTree();
        int singleHeight = singleTree.height();
        int singleCount = singleTree.getNumberOfCodewords();
        String singleString = singleTree.toString();
        System.out.println("\nHeight: " + singleHeight);
        System.out.println("Codeword count: " + singleCount);
        System.out.println("String form: " + singleString);
        boolean test11Pass = (singleHeight == 1) && (singleCount == 1) && singleString.equals("(c0,0) ");
        System.out.println("Pass: " + test11Pass);
        System.out.println();

        // More complex tree with longer paths
        System.out.println(">>> Test 12: Complex Tree - Deep Paths");
        String[] deepCodes = {"00000", "00001", "0001", "001", "01", "1"};
        System.out.println("Building tree with codes: 00000, 00001, 0001, 001, 01, 1");
        BinTree deepTree = new BinTree(deepCodes);
        System.out.print("Tree structure: ");
        deepTree.printTree();
        int deepHeight = deepTree.height();
        int deepCount = deepTree.getNumberOfCodewords();
        ArrayList<String> deepCodewords = deepTree.getCodewords();
        System.out.println("\nHeight: " + deepHeight + " (should be 5)");
        System.out.println("Codeword count: " + deepCount);
        System.out.println("Retrieved codes: " + deepCodewords);
        boolean test12Pass = (deepHeight == 5) && (deepCount == 6);
        System.out.println("Pass: " + test12Pass);
        System.out.println();

        // BONUS TESTS - optimize() functionality
        System.out.println("========================================");
        System.out.println("  BONUS: OPTIMIZE() METHOD TESTS");
        System.out.println("========================================");
        System.out.println();

        // Test 13: Tree that's already full - should stay unchanged
        System.out.println(">>> Test 13: Optimize Already-Full Tree");
        String[] fullCodes = {"0", "10", "11"};
        BinTree fullTree = new BinTree(fullCodes);
        System.out.println("Before optimization (already full): " + fullTree.toString());
        boolean beforeFull = fullTree.isFull();
        int beforeHeight = fullTree.height();
        fullTree.optimize();
        String afterOptimize = fullTree.toString();
        boolean afterFull = fullTree.isFull();
        int afterHeight = fullTree.height();
        System.out.println("After optimization: " + afterOptimize);
        System.out.println("Was full before: " + beforeFull + ", Is full after: " + afterFull);
        boolean test13Pass = beforeFull && afterFull &&
                             beforeHeight == afterHeight &&
                             afterOptimize.equals("(c0,0) (c1,10) (c2,11) ");
        System.out.println("Pass: " + test13Pass);
        System.out.println();

        // Test 14: Simple case - root with only right child
        System.out.println(">>> Test 14: Root with Only Right Child");
        // Manually build a non-full tree: root -> null, right -> leaf
        BinTree nonFullTree1 = new BinTree();
        nonFullTree1.root.right = new TNode("c0", null, null);
        System.out.print("Before: ");
        nonFullTree1.printTree();
        System.out.println(" (not full)");
        System.out.println("Tree is full: " + nonFullTree1.isFull());
        int heightBefore1 = nonFullTree1.height();
        nonFullTree1.optimize();
        System.out.print("After: ");
        nonFullTree1.printTree();
        System.out.println(" (should be just the leaf)");
        int heightAfter1 = nonFullTree1.height();
        boolean isFull1 = nonFullTree1.isFull();
        System.out.println("Tree is full: " + isFull1);
        System.out.println("Height reduced from " + heightBefore1 + " to " + heightAfter1);
        boolean test14Pass = isFull1 && heightAfter1 == 0;
        System.out.println("Pass: " + test14Pass);
        System.out.println();

        // Test 15: Root with only left child
        System.out.println(">>> Test 15: Root with Only Left Child");
        BinTree nonFullTree2 = new BinTree();
        nonFullTree2.root.left = new TNode("c0", null, null);
        System.out.print("Before: ");
        nonFullTree2.printTree();
        System.out.println(" (not full)");
        int heightBefore2 = nonFullTree2.height();
        nonFullTree2.optimize();
        System.out.print("After: ");
        nonFullTree2.printTree();
        System.out.println(" (should be just the leaf)");
        int heightAfter2 = nonFullTree2.height();
        boolean isFull2 = nonFullTree2.isFull();
        System.out.println("Tree is full: " + isFull2);
        System.out.println("Height reduced from " + heightBefore2 + " to " + heightAfter2);
        boolean test15Pass = isFull2 && heightAfter2 == 0;
        System.out.println("Pass: " + test15Pass);
        System.out.println();

        // Test 16: Complex non-full tree with multiple single-child nodes
        System.out.println(">>> Test 16: Complex Non-Full Tree");
        // Build a tree manually with several single-child internal nodes
        BinTree complexNonFull = new BinTree();
        complexNonFull.root.left = new TNode("c0", null, null);  // left leaf
        complexNonFull.root.right = new TNode(null, null, null);  // right internal
        complexNonFull.root.right.right = new TNode(null, null, null);  // another internal (only right)
        complexNonFull.root.right.right.left = new TNode("c1", null, null);
        complexNonFull.root.right.right.right = new TNode("c2", null, null);

        System.out.print("Before: ");
        complexNonFull.printTree();
        System.out.println();
        System.out.println("Tree is full: " + complexNonFull.isFull() + " (should be false)");
        int heightBefore3 = complexNonFull.height();
        System.out.println("Height before: " + heightBefore3);

        complexNonFull.optimize();

        System.out.print("After: ");
        complexNonFull.printTree();
        System.out.println();
        int heightAfter3 = complexNonFull.height();
        boolean isFull3 = complexNonFull.isFull();
        System.out.println("Tree is full: " + isFull3 + " (should be true)");
        System.out.println("Height after: " + heightAfter3 + " (should be reduced)");
        boolean test16Pass = isFull3 && heightAfter3 < heightBefore3;
        System.out.println("Pass: " + test16Pass);
        System.out.println();

        // Test 17: Verify codewords get shorter and remain valid
        System.out.println(">>> Test 17: Codeword Length Reduction");
        BinTree lengthTest = new BinTree();
        lengthTest.root.left = new TNode("c0", null, null);
        lengthTest.root.right = new TNode(null, null, null);
        lengthTest.root.right.right = new TNode("c1", null, null);  // codeword "11" (length 2)

        ArrayList<String> beforeCodes = lengthTest.getCodewords();
        System.out.println("Codewords before: " + beforeCodes);
        System.out.println("Lengths: c0=" + beforeCodes.get(0).length() + ", c1=" + beforeCodes.get(1).length());

        lengthTest.optimize();

        ArrayList<String> afterCodes = lengthTest.getCodewords();
        System.out.println("Codewords after: " + afterCodes);
        System.out.println("Lengths: c0=" + afterCodes.get(0).length() + ", c1=" + afterCodes.get(1).length());
        System.out.println("Tree is full: " + lengthTest.isFull());

        boolean test17Pass = lengthTest.isFull() &&
                             afterCodes.get(1).length() < beforeCodes.get(1).length();
        System.out.println("Pass: " + test17Pass);
        System.out.println();

        // Test 18: Verify decode still works after optimization
        System.out.println(">>> Test 18: Decoding After Optimization");
        BinTree decodeTest = new BinTree();
        decodeTest.root.left = new TNode(null, null, null);
        decodeTest.root.left.left = new TNode("c0", null, null);  // "00"
        decodeTest.root.left.right = new TNode("c1", null, null); // "01"
        decodeTest.root.right = new TNode(null, null, null);
        decodeTest.root.right.right = new TNode("c2", null, null); // "11"

        System.out.print("Before optimization: ");
        decodeTest.printTree();
        System.out.println();
        System.out.println("Codewords: " + decodeTest.getCodewords());
        System.out.println("Tree is full: " + decodeTest.isFull() + " (not full - right has only one child)");

        decodeTest.optimize();

        System.out.print("After optimization: ");
        decodeTest.printTree();
        System.out.println();
        ArrayList<String> optimizedCodes = decodeTest.getCodewords();
        System.out.println("Optimized codewords: " + optimizedCodes);
        System.out.println("Tree is full: " + decodeTest.isFull());

        // Try decoding with the new, shorter codewords
        String testMessage = "00" + "01" + "1" + "00" + "1";  // c0, c1, c2, c0, c2
        ArrayList<String> decoded = decodeTest.decode(testMessage);
        System.out.println("Encoded: " + testMessage);
        System.out.println("Decoded: " + decoded);
        System.out.println("Expected: [c0, c1, c2, c0, c2]");

        boolean test18Pass = decodeTest.isFull() &&
                             decoded.size() == 5 &&
                             decoded.get(0).equals("c0") &&
                             decoded.get(1).equals("c1") &&
                             decoded.get(2).equals("c2") &&
                             decoded.get(3).equals("c0") &&
                             decoded.get(4).equals("c2");
        System.out.println("Pass: " + test18Pass);
        System.out.println();

        // Final summary
        System.out.println("========================================");
        System.out.println("  ALL TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("========================================");
        System.out.println("\nTested:");
        System.out.println("- Empty tree construction");
        System.out.println("- Array-based construction");
        System.out.println("- Input validation (non-binary, prefix violations, duplicates)");
        System.out.println("- Height calculation");
        System.out.println("- Codeword retrieval in lexicographical order");
        System.out.println("- Binary string decoding");
        System.out.println("- String representation formatting");
        System.out.println("- Array conversion");
        System.out.println("- Edge cases (single codeword, deep trees)");
        System.out.println("- BONUS: Tree optimization (isFull, optimize)");
        System.out.println();
    }
}

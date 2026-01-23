# LAB 1 - Implementation Plan
## Prefix-free Codes Using Binary Trees

**Student:** Ali Raza
**Student Number:** 400338538
**Due Date:** January 24, 2026, 11:59 PM
**Worth:** 5% of course grade

---

## Progress Overview
- [x] Step 1: Create TNode.java
- [x] Step 2: Create BinTree.java Structure
- [x] Step 3: Implement Constructor 1
- [x] Step 4: Implement insertCodeword()
- [x] Step 5: Implement Constructor 2
- [x] Step 6: Implement printTree()
- [x] Step 7: Implement getNumberOfCodewords()
- [x] Step 8: Implement height()
- [x] Step 9: Implement getCodewords()
- [x] Step 10: Implement decode()
- [x] Step 11: Implement toString()
- [x] Step 12: Implement toArray()
- [x] Step 13: Add Complexity Analysis
- [x] Step 14: Create Test.java
- [ ] Step 15: Run Tests and Capture Output
- [ ] Step 16: Create Submission Files

---

## STEP 1: Create TNode.java ✅ COMPLETED

**EXACT SPECIFICATION FROM MANUAL (Page 2):**

```java
public class TNode {
    String data;
    TNode left;
    TNode right;

    TNode(String s, TNode l, TNode r){
        data=s;
        left=l;
        right=r;
    }
}
```

**Requirements:**
- NO modifications allowed
- Use EXACTLY as provided in manual
- `data`: stores alphabet symbol (e.g., "c0") for leaves, `null` for internal nodes
- `left`: left child pointer
- `right`: right child pointer

**Status:** ✅ File created at `/Users/aliraza/Documents/School/3SM4/LAB1/TNode.java`

---

## STEP 2: Create BinTree.java Structure

**EXACT REQUIREMENTS:**
- TWO private instance fields ONLY:
  - `private TNode root;`
  - `private int num;`
- ALL methods must be in the same package as TNode
- NO other public methods beyond those specified in manual

**Initial Structure:**
```java
public class BinTree {
    private TNode root;
    private int num;

    // Constructors and methods to follow
}
```

---

## STEP 3: Implement Constructor 1

**METHOD SIGNATURE:** `public BinTree()`

**EXACT BEHAVIOR:** Constructs a BinTree that contains only the root node

**IMPLEMENTATION:**
```java
public BinTree(){
    root = new TNode(null, null, null);
}
```

**REQUIREMENTS:**
- Creates a tree with only the root node
- Root has `data = null`, `left = null`, `right = null`
- `num` remains 0 (default initialization)

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(1)
- Space Complexity: O(1)

---

## STEP 4: Implement insertCodeword() - Private Helper

**METHOD SIGNATURE:** `private void insertCodeword(String symbol, String binary) throws IllegalArgumentException`

**EXACT REQUIREMENTS FROM MANUAL (Page 3):**
1. Reads characters in the binary string one by one
2. Follows the corresponding path in the tree (0 = left, 1 = right)
3. If tree ends before path is completed, add internal nodes (data = null)
4. Last node added is a leaf storing the symbol
5. **VALIDATION 1:** If any node on path has `data != null`, throw `IllegalArgumentException("Invalid argument!")`
6. **VALIDATION 2:** After traversing path, if current node has children, throw `IllegalArgumentException("Prefix condition violated!")`
7. Increment `num` by 1

**ALGORITHM:**
```java
private void insertCodeword(String symbol, String binary) throws IllegalArgumentException {
    TNode node = this.root;
    int n = binary.length();

    // Traverse the binary string
    for(int i = 0; i < n; i++){
        char c = binary.charAt(i);

        if(c == '0'){
            // Go left, create node if doesn't exist
            if(node.left == null){
                node.left = new TNode(null, null, null);
            }
            node = node.left;
        } else {
            // Go right, create node if doesn't exist
            if(node.right == null){
                node.right = new TNode(null, null, null);
            }
            node = node.right;
        }

        // Check if we hit an existing codeword (prefix violation)
        if(node.data != null){
            throw new IllegalArgumentException("Prefix condition violated!");
        }
    }

    // Check if current node has children (means existing codeword is prefix)
    if(node.left != null || node.right != null){
        throw new IllegalArgumentException("Prefix condition violated!");
    }

    // Store symbol and increment count
    node.data = symbol;
    num++;
}
```

**Template Reference:** See `/Resources/TemplateConstructor+insertCodeword.txt`

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(|binary|) where |binary| is length of binary string
- Space Complexity: O(|binary|) for new nodes created

---

## STEP 5: Implement Constructor 2

**METHOD SIGNATURE:** `public BinTree(String[] a) throws IllegalArgumentException`

**EXACT REQUIREMENTS FROM MANUAL (Page 2-3):**
1. Constructs a BinTree representing the prefix-free code stored in String array `a`
2. Each `a[i]` is a binary sequence (codeword) corresponding to symbol `"c" + i`
3. **VALIDATION 1:** If any element is not a binary string, throw `IllegalArgumentException("Invalid argument!")`
4. **VALIDATION 2:** If array contains duplicates or prefix violations, throw `IllegalArgumentException("Prefix condition violated!")`
5. May assume array is nonempty

**ALGORITHM:**
```java
public BinTree(String[] a) throws IllegalArgumentException {
    // Step 1: Validate all strings are binary
    int k = a.length;
    for(int i = 0; i < k; i++){
        int n = a[i].length();
        for(int j = 0; j < n; j++){
            char c = a[i].charAt(j);
            if(c != '0' && c != '1'){
                throw new IllegalArgumentException("Invalid argument!");
            }
        }
    }

    // Step 2: Create root
    this.root = new TNode(null, null, null);

    // Step 3: Insert each codeword
    for(int i = 0; i < k; i++){
        String symbol = "c" + i;
        insertCodeword(symbol, a[i]);
    }
}
```

**EXAMPLE:**
- Input: `["10", "0", "111", "110"]`
- Creates: c0→10, c1→0, c2→111, c3→110

**Template Reference:** See `/Resources/TemplateConstructor+insertCodeword.txt`

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(n*m) where n = array length, m = average codeword length
- Space Complexity: O(n*m) for tree nodes

---

## STEP 6: Implement printTree() Methods

**METHOD SIGNATURES:**
- `public void printTree()`
- `private void printTree(TNode t)`

**EXACT CODE FROM MANUAL (Page 3-4) - USE AS-IS:**

```java
public void printTree(){
    printTree(root);
}

private void printTree(TNode t){
    if(t != null){
        printTree(t.left);
        if(t.data == null)
            System.out.print("I ");
        else
            System.out.print(t.data + " ");
        printTree(t.right);
    }
}
```

**BEHAVIOR:**
- Performs inorder traversal (left, root, right)
- Prints "I " for internal nodes (data == null)
- Prints symbol + space for leaves (data != null)

**EXAMPLE OUTPUT:** `c1 I c0 I c3 I c2 `

**⚠️ CRITICAL:** Use EXACTLY as written - NO changes allowed

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(n) where n = number of nodes
- Space Complexity: O(h) where h = height (recursion stack)

---

## STEP 7: Implement getNumberOfCodewords()

**METHOD SIGNATURE:** `public int getNumberOfCodewords()`

**EXACT BEHAVIOR:** Returns the number of codewords stored in the tree

**IMPLEMENTATION:**
```java
public int getNumberOfCodewords(){
    return num;
}
```

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(1)
- Space Complexity: O(1)

---

## STEP 8: Implement height() Methods

**METHOD SIGNATURES:**
- `public int height()`
- `private int height(TNode t)` (recursive helper)

**EXACT BEHAVIOR:** Returns the height of the tree

**IMPLEMENTATION:**
```java
public int height(){
    return height(root);
}

private int height(TNode t){
    if(t == null){
        return -1;
    }
    int leftHeight = height(t.left);
    int rightHeight = height(t.right);
    return 1 + Math.max(leftHeight, rightHeight);
}
```

**HEIGHT DEFINITION:**
- Null/empty tree: -1
- Single node: 0
- Otherwise: 1 + max(left height, right height)

**ALLOWED:** `Math.max()` from java.lang.Math

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(n) where n = number of nodes
- Space Complexity: O(h) where h = height (recursion stack)

---

## STEP 9: Implement getCodewords()

**METHOD SIGNATURE:** `public ArrayList<String> getCodewords()`

**EXACT REQUIREMENTS FROM MANUAL (Page 4):**
- Returns `ArrayList<String>` storing codewords in **lexicographical order**
- Each item is a codeword (string of 0's and 1's)
- Use inorder/preorder/postorder traversal
- Track path from root to visited node as additional parameter
- When visiting leaf (data != null), insert codeword at end of list

**IMPLEMENTATION:**
```java
public ArrayList<String> getCodewords(){
    ArrayList<String> list = new ArrayList<String>();
    getCodewords(root, "", list);
    return list;
}

private void getCodewords(TNode t, String path, ArrayList<String> list){
    if(t != null){
        if(t.data != null){
            // Leaf node - add codeword
            list.add(path);
        } else {
            // Internal node - continue traversal
            getCodewords(t.left, path + "0", list);
            getCodewords(t.right, path + "1", list);
        }
    }
}
```

**LEXICOGRAPHICAL ORDER:** 0 < 1, so "0" < "10" < "110" < "111"

**ALLOWED:** ArrayList.add() method

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(n*m) where n = nodes, m = average path length
- Space Complexity: O(n*m) for storing all codewords

---

## STEP 10: Implement decode()

**METHOD SIGNATURE:** `public ArrayList<String> decode(String s)`

**EXACT REQUIREMENTS FROM MANUAL (Page 4):**
- Input: string `s` containing only 0's and 1's
- Output: ArrayList<String> of alphabet symbols in order
- Assumption: `s` is nonempty, valid, and can be divided into codewords

**ALGORITHM:**
```java
public ArrayList<String> decode(String s){
    ArrayList<String> result = new ArrayList<String>();
    TNode node = root;

    for(int i = 0; i < s.length(); i++){
        char c = s.charAt(i);

        // Traverse tree
        if(c == '0'){
            node = node.left;
        } else {
            node = node.right;
        }

        // Check if we reached a leaf
        if(node.data != null){
            result.add(node.data);
            node = root; // Reset to root for next codeword
        }
    }

    return result;
}
```

**EXAMPLE:**
- Input: "0011001010111"
- Divides: "0,0,110,0,10,10,111"
- Output: ["c1", "c1", "c3", "c1", "c0", "c0", "c2"]

**ALLOWED:** String.charAt(), String.length(), ArrayList.add()

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(|s|) where |s| = length of input string
- Space Complexity: O(k) where k = number of codewords decoded

---

## STEP 11: Implement toString()

**METHOD SIGNATURE:** `public String toString()`

**EXACT FORMAT FROM MANUAL (Page 4):** `"(c0,10) (c1,0) (c2,111) (c3,110) "`

**REQUIREMENTS:**
- Sequence of pairs (symbol, codeword)
- **INCREASING ORDER** of symbol index (c0, c1, c2, ...)
- Separated by spaces
- **ENDS WITH A SPACE**

**IMPLEMENTATION APPROACH:**
```java
public String toString(){
    ArrayList<String> symbols = new ArrayList<String>();
    ArrayList<String> codewords = new ArrayList<String>();

    // Collect (symbol, codeword) pairs
    collectPairs(root, "", symbols, codewords);

    // Build result string in order
    String result = "";
    for(int i = 0; i < symbols.size(); i++){
        result += "(" + symbols.get(i) + "," + codewords.get(i) + ") ";
    }

    return result;
}

private void collectPairs(TNode t, String path, ArrayList<String> symbols, ArrayList<String> codewords){
    if(t != null){
        if(t.data != null){
            // Leaf - store symbol and codeword
            // Need to insert in sorted order by symbol
            int index = getInsertIndex(symbols, t.data);
            symbols.add(index, t.data);
            codewords.add(index, path);
        } else {
            collectPairs(t.left, path + "0", symbols, codewords);
            collectPairs(t.right, path + "1", symbols, codewords);
        }
    }
}

private int getInsertIndex(ArrayList<String> symbols, String newSymbol){
    // Extract number from "c0", "c1", etc. and find insert position
    // ... implementation details
}
```

**ALLOWED:** String concatenation, ArrayList methods

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(n²) worst case for sorted insertion
- Space Complexity: O(n) for storing pairs

---

## STEP 12: Implement toArray()

**METHOD SIGNATURE:** `public String[] toArray()`

**EXACT REQUIREMENTS FROM MANUAL (Page 4):**
- Returns array representation using **COMP ENG 2SI3 convention**
- Index 0: stores `null`
- Missing tree nodes: store `null`
- Internal nodes: store `"I"`
- Leaf nodes: store the **codeword** (NOT the symbol!)

**ALGORITHM FROM MANUAL:**
1. Find height `h` of tree
2. Allocate array of size `2^(h+1)`, all elements = null
3. Perform tree traversal, track path from root
4. Path indicates array position
5. Store "I" for internal nodes, codeword for leaves

**IMPLEMENTATION:**
```java
public String[] toArray(){
    int h = height();
    int size = (int)Math.pow(2, h + 1);
    String[] arr = new String[size];

    // Fill array with null (already default)
    // Traverse and fill
    toArray(root, "", arr, 1);

    return arr;
}

private void toArray(TNode t, String path, String[] arr, int index){
    if(t != null){
        if(t.data != null){
            // Leaf - store codeword
            arr[index] = path;
        } else {
            // Internal - store "I"
            arr[index] = "I";
        }

        // Recurse left and right
        toArray(t.left, path + "0", arr, 2 * index);
        toArray(t.right, path + "1", arr, 2 * index + 1);
    }
}
```

**ARRAY INDEXING:**
- Root at index 1
- Left child of index i: 2*i
- Right child of index i: 2*i + 1

**ALLOWED:** Math.pow()

**COMPLEXITY COMMENT REQUIRED:**
- Time Complexity: O(2^h) where h = height
- Space Complexity: O(2^h) for array

---

## STEP 13: Add Complexity Analysis Comments

**REQUIREMENT:** For EVERY method, add comment with time and space complexity

**FORMAT:**
```java
// Time Complexity: O(?) - brief justification
// Space Complexity: O(?) - brief justification
public returnType methodName(...){
    ...
}
```

**EXAMPLE:**
```java
// Time Complexity: O(n) - visits each node once in inorder traversal
// Space Complexity: O(h) - recursion stack depth equals tree height
private void printTree(TNode t){
    if(t != null){
        printTree(t.left);
        if(t.data == null)
            System.out.print("I ");
        else
            System.out.print(t.data + " ");
        printTree(t.right);
    }
}
```

**METHODS REQUIRING COMPLEXITY ANALYSIS:**
1. Constructor 1: `BinTree()`
2. Constructor 2: `BinTree(String[] a)`
3. `insertCodeword(String symbol, String binary)`
4. `printTree()` (both public and private)
5. `getNumberOfCodewords()`
6. `height()` (both public and private)
7. `getCodewords()` (both public and private)
8. `decode(String s)`
9. `toString()` (and any helpers)
10. `toArray()` (and any helpers)

---

## STEP 14: Create Test.java Class

**EXACT REQUIREMENTS:**
- Class name: `Test`
- Same package as BinTree and TNode
- Provides **thorough testing** of ALL constructors and public methods

**REFERENCE:** See `/Resources/SampleTest1.txt` for test structure

**REQUIRED TEST CASES:**

### 1. Test BinTree() Constructor
```java
System.out.println("------ Testing Constructor 1 ------");
BinTree myTree = new BinTree();
myTree.printTree(); // Expected: "I "
System.out.println("num=" + myTree.getNumberOfCodewords()); // Expected: 0
```

### 2. Test insertCodeword() via Constructor
- Valid input
- Duplicate codeword
- Prefix violation (existing codeword is prefix)
- Prefix violation (new codeword is prefix)

### 3. Test BinTree(String[] a) Constructor
- Valid input: `["10", "0", "111", "110"]`
- Invalid binary string: `["10", "0", "1a1", "110"]`
- Duplicate codewords: `["10", "0", "10", "110"]`
- Prefix violation: `["10", "100", "111", "110"]`

### 4. Test printTree()
- Various tree structures
- Empty tree (just root)
- Single codeword
- Multiple codewords

### 5. Test getNumberOfCodewords()
- Empty tree: 0
- After inserting 1, 2, 4 codewords

### 6. Test height()
- Empty tree
- Single codeword
- Balanced tree
- Unbalanced tree

### 7. Test getCodewords()
- Verify lexicographical order
- Compare with expected codewords

### 8. Test decode()
- Various binary strings
- Verify correct symbol sequence

### 9. Test toString()
- Verify exact format: "(c0,10) (c1,0) (c2,111) (c3,110) "
- Check space at end
- Verify ordering

### 10. Test toArray()
- Verify array size
- Check index 0 is null
- Verify internal nodes are "I"
- Verify leaves store codewords

**STRUCTURE:**
```java
public class Test {
    public static void main(String[] args){
        // Test Constructor 1
        // Test insertCodeword
        // Test Constructor 2
        // Test printTree
        // Test getNumberOfCodewords
        // Test height
        // Test getCodewords
        // Test decode
        // Test toString
        // Test toArray
    }
}
```

---

## STEP 15: Run Tests and Capture Output

**ACTIONS:**
1. Compile all Java files: `javac TNode.java BinTree.java Test.java`
2. Run Test class: `java Test`
3. Capture ALL console output
4. Verify output matches expected results
5. Save output to text file

**COMMANDS:**
```bash
cd /Users/aliraza/Documents/School/3SM4/LAB1
javac TNode.java BinTree.java Test.java
java Test > test_output.txt
```

---

## STEP 16: Create Submission Files

**EXACT NAMING FORMAT:** `ClassName[YourName][StudentNumber].txt`

**YOUR DETAILS:**
- Name: Ali Raza → AliRaza (no spaces)
- Student Number: 400338538

**REQUIRED FILES:**

1. **BinTree Source Code:**
   - Filename: `BinTreeAliRaza400338538.txt`
   - Content: Complete BinTree.java source code

2. **Test Source Code:**
   - Filename: `Lab1TestAliRaza400338538.txt`
   - Content: Complete Test.java source code

3. **Test Output:**
   - Filename: `Lab1OutputAliRaza400338538.txt`
   - Content: All console output from running Test.java

**SUBMISSION:**
- Submit all 3 text files
- Ensure filenames match exactly

---

## CRITICAL RESTRICTIONS - DO NOT VIOLATE

### ❌ FORBIDDEN:
1. NO Java methods except: ArrayList, String, Math
2. NO additional public methods in BinTree beyond those specified
3. NO changes to TNode class
4. NO changes to provided printTree() code
5. NO deviations from exact error messages:
   - `"Invalid argument!"`
   - `"Prefix condition violated!"`
6. ONLY 2 private fields in BinTree: `root`, `num`
7. NO missing complexity analysis comments
8. NO incorrect output formats

### ✅ REQUIRED:
1. Exact error messages as specified
2. Complexity analysis for ALL methods
3. Exact toString format: `"(c0,10) (c1,0) ... "`
4. Exact toArray format: 2SI3 convention
5. Thorough testing of all methods
6. Correct file naming for submission

---

## BONUS (Optional +0.5%)

### Method: `public void optimize()`

**REQUIREMENTS:**
- Makes tree into full binary tree (every internal node has 2 children)
- Reduces codeword lengths while maintaining prefix-free property
- Must be efficient
- Requires complexity analysis
- Requires thorough testing
- Assumption: tree stores at least 2 codewords

**APPROACH:**
- Identify nodes with only one child
- Restructure tree to make it full
- Update codeword assignments

**ONLY IMPLEMENT AFTER COMPLETING ALL REQUIRED METHODS**

---

## Resources Available

1. **Manual:** `/Resources/lab1_2026_3SM4.pdf`
2. **Template:** `/Resources/TemplateConstructor+insertCodeword.txt`
3. **Sample Test:** `/Resources/SampleTest1.txt`
4. **Sample Output:** `/Resources/Output1.txt`

---

## Completion Checklist

- [x] Step 1: TNode.java created
- [x] Step 2: BinTree.java structure
- [x] Step 3: Constructor 1
- [x] Step 4: insertCodeword()
- [x] Step 5: Constructor 2
- [x] Step 6: printTree()
- [x] Step 7: getNumberOfCodewords()
- [x] Step 8: height()
- [x] Step 9: getCodewords()
- [x] Step 10: decode()
- [x] Step 11: toString()
- [x] Step 12: toArray()
- [x] Step 13: Complexity comments
- [x] Step 14: Test.java
- [x] Step 15: Run and capture output
- [ ] Step 16: Create submission files
- [ ] BONUS: optimize() (optional)

---

**Last Updated:** Step 15 completed - Tests run successfully, output saved to TestOutput.txt
**Next Step:** Step 16 - Create submission files with correct naming format

// Ali Raza - 400338538
// Lab 3: Red-Black Tree of Card Hands
// McMaster University - COMP ENG 3SM4

import java.util.Vector;

public class HandsRBT {

    private HandsRBTNode root;
    private static final boolean RED = false;
    private static final boolean BLACK = true;

    // This inline class is typically required in certain post-deletion correction cases
    private static class NilNode extends HandsRBTNode
    {
        private NilNode()
        {
            super(null);
            this.colour = BLACK;
        }
    }

    // Constructor: Instantiates an empty RBT
    public HandsRBT()
    {
        root = null;
    }

    private HandsRBTNode getRoot() {return root;}

    public boolean isEmpty() { return root==null; }

    private boolean isBlack(HandsRBTNode thisNode)
    {
        return (thisNode == null || thisNode.colour == BLACK);
    }

    // Required helper function for fixing RBT properties
    private HandsRBTNode getSibling(HandsRBTNode thisNode)
    {
        HandsRBTNode parent = thisNode.parent;

        if(thisNode == parent.left)         return parent.right;
        else if(thisNode == parent.right)   return parent.left;
        else
            throw new IllegalStateException("Parent and Children are Unrelated.");
    }

    // Required helper function for fixing RBT properties
    private HandsRBTNode getUncle(HandsRBTNode parent)
    {
        HandsRBTNode grandparent = parent.parent;
        if(grandparent.left == parent)
            return grandparent.right;
        else if(grandparent.right == parent)
            return grandparent.left;
        else
            throw new IllegalStateException("Provided Parent is NOT a child of the Grandparent");
    }


    // Activity 1 - Design Rotation Algorithm
    /////////////////////////////////////////////////

    // [TODO]: Required Helper Functions for RBT Rotation
    private void rotateLeft(HandsRBTNode thisNode)
    {
        // Left rotation: thisNode (N) rotates with its right child (R)
        // After rotation: R takes N's place, N becomes R's left child, R's old left (RL) becomes N's right child
        HandsRBTNode rightChild = thisNode.right; // R in the diagram

        // Step 1: R's left subtree (RL) becomes N's new right child
        thisNode.right = rightChild.left;
        if (rightChild.left != null)
            rightChild.left.parent = thisNode;

        // Step 2: R takes over N's position in the parent
        rightChild.parent = thisNode.parent;
        if (thisNode.parent == null)
            root = rightChild;                          // N was root, R is new root
        else if (thisNode == thisNode.parent.left)
            thisNode.parent.left = rightChild;          // N was left child
        else
            thisNode.parent.right = rightChild;         // N was right child

        // Step 3: N becomes R's new left child
        rightChild.left = thisNode;
        thisNode.parent = rightChild;
    }

    // [TODO]: Required Helper Functions for RBT Rotation
    private void rotateRight(HandsRBTNode thisNode)
    {
        // Right rotation: thisNode (N) rotates with its left child (L)
        // After rotation: L takes N's place, N becomes L's right child, L's old right (LR) becomes N's left child
        HandsRBTNode leftChild = thisNode.left; // L in the diagram

        // Step 1: L's right subtree (LR) becomes N's new left child
        thisNode.left = leftChild.right;
        if (leftChild.right != null)
            leftChild.right.parent = thisNode;

        // Step 2: L takes over N's position in the parent
        leftChild.parent = thisNode.parent;
        if (thisNode.parent == null)
            root = leftChild;                           // N was root, L is new root
        else if (thisNode == thisNode.parent.left)
            thisNode.parent.left = leftChild;           // N was left child
        else
            thisNode.parent.right = leftChild;          // N was right child

        // Step 3: N becomes L's new right child
        leftChild.right = thisNode;
        thisNode.parent = leftChild;
    }




    public HandsRBTNode findNode(Hands thisHand)
    {
        HandsRBTNode node = root;

        while(node != null)
        {
            if(node.myHand.isMyHandLarger(thisHand))
            {
                node = node.left;
            }
            else if(node.myHand.isMyHandSmaller(thisHand))
            {
                node = node.right;
            }
            else // Matched
            {
                return node;
            }
        }

        return null;

    }

    // Activity 2 - Design Insertion and Red-Violation Correction
    /////////////////////////////////////////////////////////////////////////

    // [TODO]: Implement the RBT insertion algorithm; if the input Hand is already in the RBT do not insert

    public void insert(Hands thisHand)
    {
        // Case 1 (NullRoot): tree is empty - just make this the root and colour it BLACK
        if (root == null)
        {
            root = new HandsRBTNode(thisHand);
            root.colour = BLACK;
            return;
        }

        // Standard BST traversal to find where the new node belongs
        HandsRBTNode current = root;
        HandsRBTNode parent = null;

        while (current != null)
        {
            // Already in the tree - per spec, do nothing
            if (thisHand.isMyHandEqual(current.myHand))
                return;

            parent = current;

            if (thisHand.isMyHandSmaller(current.myHand))
                current = current.left;     // go left for smaller hands
            else
                current = current.right;    // go right for larger hands
        }

        // Insert the new node coloured RED
        HandsRBTNode newNode = new HandsRBTNode(thisHand);
        newNode.colour = RED;
        newNode.parent = parent;

        if (thisHand.isMyHandSmaller(parent.myHand))
            parent.left = newNode;
        else
            parent.right = newNode;

        // Case 1 (BlackParent): parent is BLACK, no violation to fix
        // Case 2 (BlackParent): if parent is RED, we have a red-red violation - fix it
        if (parent.colour == RED)
            fixRedViolation(newNode);
    }

    // [TODO]: Implement the fixRedViolation algorithm

    private void fixRedViolation(HandsRBTNode thisNode)
    {
        // thisNode is node X from lecture notes; Uncle is node S from lecture notes; if Uncle is null, its colour is BLACK
        // Recall that there are 3 cases:
        // Case 3: Red Uncle - recolour and potentially recurse upward
        // Case 4: Black Uncle & Inner Grandchild - rotate parent, then fall into Case 5
        // Case 5: Black Uncle & Outer Grandchild - rotate grandparent and recolour

        HandsRBTNode parent = thisNode.parent;

        // Base: no violation if parent is BLACK or doesn't exist
        if (parent == null || parent.colour == BLACK)
            return;

        HandsRBTNode grandparent = parent.parent;

        // If parent is root (no grandparent), just ensure root is BLACK
        if (grandparent == null)
        {
            parent.colour = BLACK;
            return;
        }

        HandsRBTNode uncle = getUncle(parent);

        // Case 3 (Red Uncle): recolour parent and uncle BLACK, grandparent RED
        // Then check if the violation moved up to grandparent
        if (uncle != null && uncle.colour == RED)
        {
            parent.colour = BLACK;
            uncle.colour = BLACK;
            grandparent.colour = RED;

            // Root must always be BLACK - fix it if needed
            if (grandparent == root)
                grandparent.colour = BLACK;
            else if (grandparent.parent.colour == RED)
                fixRedViolation(grandparent); // violation may have propagated up

            return;
        }

        // Case 4 (Black Uncle, Inner Grandchild): need a double rotation
        // First rotate the parent to turn it into an outer grandchild, then fall into Case 5
        if (parent == grandparent.left && thisNode == parent.right)
        {
            // Left-Right case: rotate left around parent
            rotateLeft(parent);
            // After rotation, the original parent dropped down and thisNode rose up
            // Swap references so we can treat it as Left-Left for Case 5
            thisNode = parent;
            parent = thisNode.parent;
        }
        else if (parent == grandparent.right && thisNode == parent.left)
        {
            // Right-Left case: rotate right around parent
            rotateRight(parent);
            // Same swap trick as above
            thisNode = parent;
            parent = thisNode.parent;
        }

        // Case 5 (Black Uncle, Outer Grandchild): single rotation + recolour
        // At this point: parent is outer grandchild (Left-Left or Right-Right)
        if (parent == grandparent.left)
            rotateRight(grandparent);   // Left-Left: rotate right around grandparent
        else
            rotateLeft(grandparent);    // Right-Right: rotate left around grandparent

        // After rotation, parent is now at grandparent's old position
        parent.colour = BLACK;
        grandparent.colour = RED;
    }

    // Activity 3 - Delete All Hands from RBT with cards from the consumed hand
    /////////////////////////////////////////////////////////////////////////

    // Invoke this method to delete all the Nodes with Hands containing any card from the consumedHand
    // This method invokes deleteHandsWithCard() for all 5 cards in the consumedHand
    public void deleteInvalidHands(Hands consumedHand)
    {
        // Remove all hands that share any of the 5 cards in the consumed hand
        // A hand is invalid once one of its cards has been played
        for (int i = 0; i < 5; i++)
        {
            deleteHandsWithCard(consumedHand.getCard(i));
        }
    }

    // [TODO]: Implement this method that supports the card game activities. It deletes from RBT all hands with thisCard

    private void deleteHandsWithCard(Card thisCard)
    {
        // Step 1: Traverse the entire tree and collect all hands containing thisCard
        // We can't delete while traversing (it would corrupt the tree structure)
        // so gather them first into a list, then delete them one by one
        Vector<Hands> handsToDelete = new Vector<>();
        collectHandsWithCard(root, thisCard, handsToDelete);

        // Step 2: Delete each collected hand using the existing delete() method
        for (Hands h : handsToDelete)
        {
            delete(h);
        }
    }

    // Helper: recursively traverses the entire RBT (in-order) and collects
    // all hands that contain thisCard into the provided list
    private void collectHandsWithCard(HandsRBTNode node, Card thisCard, Vector<Hands> list)
    {
        if (node == null)
            return;

        // In-order traversal: left, current, right
        collectHandsWithCard(node.left, thisCard, list);

        if (node.myHand != null && node.myHand.hasCard(thisCard))
            list.add(node.myHand);

        collectHandsWithCard(node.right, thisCard, list);
    }

    // Deletion Method 1 - promote the smallest of the right tree
    private HandsRBTNode findMin(HandsRBTNode thisNode)
    {
        while(thisNode.left != null)
            thisNode = thisNode.left;

        return thisNode;
    }

    // Deletion Method 2 - promote the largest of the left tree
    private HandsRBTNode findMax(HandsRBTNode thisNode)
    {
        while(thisNode.right != null)
            thisNode = thisNode.right;

        return thisNode;
    }

    // Helper Function
    private void replaceParentsChild(HandsRBTNode parent, HandsRBTNode oldChild, HandsRBTNode newChild)
    {
        if(parent == null)  root = newChild;
        else if(parent.left == oldChild) parent.left = newChild;
        else if(parent.right == oldChild) parent.right = newChild;
        else throw new IllegalStateException("Parents and Children are unrelated...");

        if(newChild != null) newChild.parent = parent;
    }

    public void delete(Hands thisHand)
    {
        HandsRBTNode node = root;

        // First, search for node
        while( node != null )
        {
            if(node.myHand.isMyHandLarger(thisHand))
            {
                node = node.left;
            }
            else if(node.myHand.isMyHandSmaller(thisHand))
            {
                node = node.right;
            }
            else
            {
                break;  // equal
            }
        }
        // If not found, don't do anything
        if(node == null) return;

        // If node found, take the respective action
        HandsRBTNode promotedNode;
        boolean deletedNodeColour;

        //  Either the node has zero or one child
        if(node.left == null || node.right == null)
        {
            promotedNode = deleteNodeWithMaxOneChild(node);
            deletedNodeColour = node.colour;
        }
        //  Or the node has two children
        else
        {
            HandsRBTNode successorNode = findMin(node.right);   // OR, = findMax(node.left);
            node.myHand = successorNode.myHand;

            promotedNode = deleteNodeWithMaxOneChild(successorNode);
            deletedNodeColour = successorNode.colour;
        }

        // Finally, fix the RBT
        if(deletedNodeColour == BLACK)
        {
            PostDeleteRBTCorrection(promotedNode);

            if(promotedNode.getClass() == NilNode.class)
            {
                replaceParentsChild(promotedNode.parent, promotedNode, null);
            }
        }
    }

    private HandsRBTNode deleteNodeWithMaxOneChild(HandsRBTNode thisNode)
    {
        // Only left child
        if(thisNode.left != null)
        {
            replaceParentsChild(thisNode.parent, thisNode, thisNode.left);
            return thisNode.left;
        }
        // Only right child
        else if(thisNode.right != null)
        {
            replaceParentsChild(thisNode.parent, thisNode, thisNode.right);
            return thisNode.right;
        }
        // No child
        else
        {
            HandsRBTNode newChild = null;
            if(thisNode.colour == BLACK)
                newChild = new NilNode();

            replaceParentsChild(thisNode.parent, thisNode, newChild);
            return newChild;
        }


    }

    // 6 Cases to handle.  4 in this method, and 2 in its helper function
    private void PostDeleteRBTCorrection(HandsRBTNode thisNode)
    {
        // Case 1 - thisNode is root.
        if(thisNode == root) return;
        // may want to optionally colour the root node BLACK

        HandsRBTNode sibling = getSibling(thisNode);

        // Case 2 - Red Sibling
        if(sibling.colour == RED)
        {
            handleRedSibling(thisNode, sibling);
            sibling = getSibling(thisNode); // keep track of the new sibling for subsequent cases
        }

        // Case 3 + 4 - Black sibling with two black children
        if(isBlack(sibling.left) && isBlack(sibling.right))
        {
            sibling.colour = RED;

            // Case 3 - Black sibling with two black children + red parent
            if(thisNode.parent.colour == RED)
            {
                thisNode.parent.colour = BLACK;
            }
            // Case 4 - Black sibling with two black children + black parent
            else
            {
                PostDeleteRBTCorrection(thisNode.parent);
                // recursively correct the colour upwards
            }
        }

        // Case 5 + 6 - Black Sibling with at least One Child
        else
        {
            handleBlackSiblingWithMinOneRedChild(thisNode, sibling);
        }
    }

    private void handleRedSibling(HandsRBTNode thisNode, HandsRBTNode sibling)
    {
        // First, recolour
        sibling.colour = BLACK;
        thisNode.parent.colour = RED;

        // Then, rotate
        if(thisNode == thisNode.parent.left)
        {
            rotateLeft(thisNode.parent);
        }
        else
        {
            rotateRight(thisNode.parent);
        }
    }

    private void handleBlackSiblingWithMinOneRedChild(HandsRBTNode thisNode, HandsRBTNode sibling)
    {
        boolean isLeftChild = (thisNode == thisNode.parent.left);

        // Case 5: Black Sibling with min one red child + outer nephew is black
        if(isLeftChild && isBlack(sibling.right))
        {
            sibling.left.colour = BLACK;
            sibling.colour = RED;
            rotateRight(sibling);
            sibling = thisNode.parent.right;
        }
        else if(!isLeftChild && isBlack(sibling.left))
        {
            sibling.right.colour = BLACK;
            sibling.colour = RED;
            rotateLeft(sibling);
            sibling = thisNode.parent.left;
        }

        // Case 6: Black Sibling with min one red child + outer nephew is red
        sibling.colour = thisNode.parent.colour;
        thisNode.parent.colour = BLACK;

        if(isLeftChild)
        {
            sibling.right.colour = BLACK;
            rotateLeft(thisNode.parent);
        }
        else
        {
            sibling.left.colour = BLACK;
            rotateRight(thisNode.parent);
        }
    }

    public void printRBT()
    {
        // First, get the height of the tree
        int height = getHeight(root);

        // Then, print using leveled order
        for(int i = 1; i <= height; i++)
        {
            for(int j = 0; j < (height - i); j++)
            {
                System.out.print("  ");
            }
            printCurrentLevel(root, i);
            System.out.println(">>");
        }

    }

    private int getHeight(HandsRBTNode thisNode)
    {
        if(thisNode == null) return 0;

        int leftHeight = getHeight( thisNode.left );
        int rightHeight = getHeight( thisNode.right );

        if(leftHeight > rightHeight)
            return (leftHeight + 1);
        else
            return (rightHeight + 1);
    }

    private void printCurrentLevel(HandsRBTNode thisNode, int level)
    {
        if(thisNode == null)
        {
            System.out.print("==NIL==");
            return;
        }

        if(level == 1)
        {
            printNode(thisNode);
        }
        else if(level > 1)
        {
            printCurrentLevel(thisNode.left, level - 1);
            System.out.print("\t");
            printCurrentLevel(thisNode.right, level - 1);
            System.out.print("\t");
        }
    }

    private void printNode(HandsRBTNode thisNode)
    {
        if(thisNode.colour == BLACK)
            System.out.printf("(B)");
        else
            System.out.printf("(R)");
        thisNode.myHand.printMyHand();
        System.out.printf(" ");
    }




    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {
        testRotateLeft();
        testRotateRight();

        testInsertCase1NR();
        testInsertCase1BP();
        testInsertCase2A();
        testInsertCase2B();
        testInsertCase3();
        testInsertCase4L();
        testInsertCase4R();
        testInsertCase5L();
        testInsertCase5R();

        testCustomInsertCase4();
        testCustomInsertCase5();

        testDeleteHandsWithCard();
        testCustomDeleteHandsWithCard();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n",
                          totalPassCount,
                          totalTestCount);
        if(totalPassed)
        {
            System.out.println("All Tests Passed.");
            System.exit(0);
        }
        else
        {
            System.out.println("Tests Failed.");
            System.exit(-1);
        }
    }

    private static void testRotateLeft()
    {
        // Setup
        System.out.println("============testRotateLeft=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        Hands myHandsArray[] = new Hands[6];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));

        HandsRBTNode nodeArray[] = new HandsRBTNode[6];
        nodeArray[0] = new HandsRBTNode(myHandsArray[0]);
        nodeArray[1] = new HandsRBTNode(myHandsArray[1]);
        nodeArray[2] = new HandsRBTNode(myHandsArray[2]);
        nodeArray[3] = new HandsRBTNode(myHandsArray[3]);
        nodeArray[4] = new HandsRBTNode(myHandsArray[4]);
        nodeArray[5] = new HandsRBTNode(myHandsArray[5]);

        HandsRBTNode root = nodeArray[0];
        nodeArray[0].parent = null;
        nodeArray[0].left = nodeArray[1];
        nodeArray[0].right = null;

        nodeArray[1].parent = nodeArray[0];
        nodeArray[1].left = nodeArray[2];
        nodeArray[1].right = nodeArray[3];

        nodeArray[2].parent = nodeArray[1];
        nodeArray[2].left = null;
        nodeArray[2].right = null;

        nodeArray[3].parent = nodeArray[1];
        nodeArray[3].left = nodeArray[4];;
        nodeArray[3].right = nodeArray[5];

        nodeArray[4].parent = nodeArray[3];
        nodeArray[4].left = null;
        nodeArray[4].right = null;

        nodeArray[5].parent = nodeArray[3];
        nodeArray[5].left = null;
        nodeArray[5].right = null;

        HandsRBT dummyRBT = new HandsRBT();

        dummyRBT.rotateLeft(root.left);

        System.out.println(">> Test Root (Not Rotated)");
        passed &= assertEquals(root.myHand, nodeArray[0].myHand);
        System.out.println(">> Test Rotated SubRoot (root.left)");
        passed &= assertEquals(root.left.myHand, nodeArray[3].myHand);
        System.out.println(">> Test Rotated Left Child");
        passed &= assertEquals(root.left.left.myHand, nodeArray[1].myHand);
        System.out.println(">> Test Rotated Right Child");
        passed &= assertEquals(root.left.right.myHand, nodeArray[5].myHand);
        System.out.println(">> Test Rotated Left Grandchild of Left Child");
        passed &= assertEquals(root.left.left.left.myHand, nodeArray[2].myHand);
        System.out.println(">> Test Rotated Right Grandchild of Left Child");
        passed &= assertEquals(root.left.left.right.myHand, nodeArray[4].myHand);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testRotateRight()
    {
        // Setup
        System.out.println("============testRotateRight=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        Hands myHandsArray[] = new Hands[6];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));

        HandsRBTNode nodeArray[] = new HandsRBTNode[6];
        nodeArray[0] = new HandsRBTNode(myHandsArray[0]);
        nodeArray[1] = new HandsRBTNode(myHandsArray[1]);
        nodeArray[2] = new HandsRBTNode(myHandsArray[2]);
        nodeArray[3] = new HandsRBTNode(myHandsArray[3]);
        nodeArray[4] = new HandsRBTNode(myHandsArray[4]);
        nodeArray[5] = new HandsRBTNode(myHandsArray[5]);

        HandsRBTNode root = nodeArray[0];
        nodeArray[0].parent = null;
        nodeArray[0].left = nodeArray[1];
        nodeArray[0].right = null;

        nodeArray[1].parent = nodeArray[0];
        nodeArray[1].left = nodeArray[2];
        nodeArray[1].right = nodeArray[3];

        nodeArray[2].parent = nodeArray[1];
        nodeArray[2].left = nodeArray[4];
        nodeArray[2].right = nodeArray[5];

        nodeArray[3].parent = nodeArray[1];
        nodeArray[3].left = null;
        nodeArray[3].right = null;

        nodeArray[4].parent = nodeArray[2];
        nodeArray[4].left = null;
        nodeArray[4].right = null;

        nodeArray[5].parent = nodeArray[2];
        nodeArray[5].left = null;
        nodeArray[5].right = null;

        HandsRBT dummyRBT = new HandsRBT();

        dummyRBT.rotateRight(root.left);

        System.out.println(">> Test Root (Not Rotated)");
        passed &= assertEquals(root.myHand, nodeArray[0].myHand);
        System.out.println(">> Test Rotated SubRoot (root.left)");
        passed &= assertEquals(root.left.myHand, nodeArray[2].myHand);
        System.out.println(">> Test Rotated Left Child");
        passed &= assertEquals(root.left.left.myHand, nodeArray[4].myHand);
        System.out.println(">> Test Rotated Right Child");
        passed &= assertEquals(root.left.right.myHand, nodeArray[1].myHand);
        System.out.println(">> Test Rotated Left Grandchild of Right Child");
        passed &= assertEquals(root.left.right.left.myHand, nodeArray[5].myHand);
        System.out.println(">> Test Rotated Right Grandchild of Right Child");
        passed &= assertEquals(root.left.right.right.myHand, nodeArray[3].myHand);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }


    private static void testInsertCase1NR()
    {
        // Setup
        System.out.println("============testInsertCase1NR=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands thisHand = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        testRBT.insert(thisHand);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Node Insertion");
        passed &= assertEquals(testRoot.myHand, thisHand);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase1BP()
    {
        // Setup
        System.out.println("============testInsertCase1BP=============");
        boolean passed = true;
        totalTestCount++;

        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[3];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left, null);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase2A()
    {
        // Setup
        System.out.println("============testInsertCase2A=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[2];
        myHandsArray[0] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right, null);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase2B()
    {
        // Setup
        System.out.println("============testInsertCase2B=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[2];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left, null);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase3()
    {
        // Setup
        System.out.println("============testInsertCase3=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[4];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[3]);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase4L()
    {
        // Setup
        System.out.println("============testInsertCase4L=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(13, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(13, 'H'), new Card(13, 'H'));
        myHandsArray[4] = new Hands(new Card(14, 'S'), new Card(6, 'D'), new Card(6, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3
        testRBT.insert(myHandsArray[4]); // this will trigger Case 4L

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[3]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase4R()
    {
        // Setup
        System.out.println("============testInsertCase4R=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(7, 'S'), new Card(7, 'D'), new Card(7, 'H'), new Card(7, 'C'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 4R

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[3]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase5L()
    {
        // Setup
        System.out.println("============testInsertCase5L=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(6, 'D'), new Card(6, 'C'), new Card(12, 'H'), new Card(12, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5L

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[4]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testInsertCase5R()
    {
        // Setup
        System.out.println("============testInsertCase5R=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(11, 'S'), new Card(13, 'S'), new Card(14, 'S'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5R

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testCustomInsertCase4()
    {
        // Setup
        System.out.println("============testCustomInsertCase4=============");
        boolean passed = true;
        totalTestCount++;

        // Custom test for Case 4 (Black Uncle, Inner Grandchild) - triggering Case 4R
        // Setup: 3 nodes where the 3rd insertion triggers Right-Left (inner grandchild) rotation
        // [0] is the root (FH type), [1] goes RIGHT (FK > FH), [2] goes LEFT of [1] (FH < FK)
        // This gives: root [0], right child [1], and [2] as left child of [1] = inner grandchild
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[3];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H')); // FH: three 6s + pair 3s
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H')); // FK: four 6s
        myHandsArray[2] = new Hands(new Card(8, 'S'), new Card(8, 'H'), new Card(8, 'C'), new Card(5, 'D'), new Card(5, 'H')); // FH: three 8s + pair 5s (between [0] and [1])

        // [0] = FH(6-triple) → root, BLACK
        // [1] = FK(6-quad) > [0] → goes RIGHT, RED, no violation (parent=BLACK)
        // [2] = FH(8-triple): FH < FK → goes LEFT of [1]. Parent [1] is RED, uncle [0].left=null (BLACK) → Case 4R!
        // Case 4R: rotateRight([1]) → [2] rises to [1]'s spot, [1] becomes [2]'s right
        // Then Case 5R: rotateLeft([0]) → [2] becomes root, [0] is [2].left, [1] is [2].right
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]); // triggers Case 4R followed by Case 5R

        HandsRBTNode testRoot = testRBT.getRoot();

        // After Case 4R + Case 5R: [2](FH,8-triple) is new root (BLACK), [0](FH,6-triple) is left (RED), [1](FK,6-quad) is right (RED)
        System.out.println(">> Check New Root (was inner grandchild)");
        passed &= assertEquals(testRoot.myHand, myHandsArray[2]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child (original root, dropped down)");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[0]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED);
        System.out.println(">> Check Right Child (original right child)");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED);
        System.out.println(">> Check All Grandchildren are null");
        passed &= assertEquals(testRoot.left.left, null);
        passed &= assertEquals(testRoot.left.right, null);
        passed &= assertEquals(testRoot.right.left, null);
        passed &= assertEquals(testRoot.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testCustomInsertCase5()
    {
        // Setup
        System.out.println("============testCustomInsertCase5=============");
        boolean passed = true;
        totalTestCount++;

        // Custom test for Case 5 (Black Uncle, Outer Grandchild) - triggering Case 5L
        // Setup: 3 nodes where the 3rd insertion triggers Left-Left (outer grandchild) single rotation
        // [0] is root (FK), [1] goes LEFT (FH < FK), [2] goes LEFT again (S < FH < FK)
        // This gives: root [0], left child [1], and [2] as left child of [1] = outer grandchild
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[3];
        myHandsArray[0] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H')); // FK: four 6s
        myHandsArray[1] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H')); // FH: three 6s + pair 3s
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S')); // S: straight 2-6

        // [0] = FK(6-quad) → root, BLACK
        // [1] = FH(6-triple): FH < FK → goes LEFT, RED, no violation (parent=BLACK)
        // [2] = S(2-6): S < FH → goes LEFT of [1]. Parent [1] is RED, uncle [0].right=null (BLACK) → Case 5L!
        // Case 5L: rotateRight([0]) → [1] rises to root, [0] is [1].right, [2] stays [1].left
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]); // triggers Case 5L directly

        HandsRBTNode testRoot = testRBT.getRoot();

        // After Case 5L: [1](FH,6-triple) is new root (BLACK), [2](S,2-6) is left (RED), [0](FK,6-quad) is right (RED)
        System.out.println(">> Check New Root (was outer grandchild's parent)");
        passed &= assertEquals(testRoot.myHand, myHandsArray[1]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child (deepest outer grandchild)");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED);
        System.out.println(">> Check Right Child (original root, dropped)");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[0]);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED);
        System.out.println(">> Check All Grandchildren are null");
        passed &= assertEquals(testRoot.left.left, null);
        passed &= assertEquals(testRoot.left.right, null);
        passed &= assertEquals(testRoot.right.left, null);
        passed &= assertEquals(testRoot.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testDeleteHandsWithCard()
    {
        // Setup
        System.out.println("============testDeleteHandsWithCard=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(11, 'S'), new Card(13, 'S'), new Card(14, 'S'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5R

        testRBT.deleteHandsWithCard(new Card(6, 'H'));

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[3]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testCustomDeleteHandsWithCard()
    {
        // Setup
        System.out.println("============testCustomDeleteHandsWithCard=============");
        boolean passed = true;
        totalTestCount++;

        // Custom test: populate RBT with 5 hands, 3 containing Card(8,'H'), 2 without it
        // Then delete all hands with 8H and verify only the 2 clean hands remain
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];

        // 3 hands that contain Card(8, 'H')
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H')); // FH, no 8H (baseline hand)
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H')); // FK, no 8H (baseline)
        myHandsArray[2] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H')); // FK, has 8H
        myHandsArray[3] = new Hands(new Card(8, 'H'), new Card(8, 'D'), new Card(8, 'S'), new Card(5, 'D'), new Card(5, 'H')); // FH, has 8H
        myHandsArray[4] = new Hands(new Card(8, 'H'), new Card(8, 'D'), new Card(8, 'S'), new Card(3, 'D'), new Card(3, 'H')); // FH, has 8H

        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]);

        // Delete all hands containing Card(8, 'H') - should remove [2], [3], [4]
        testRBT.deleteHandsWithCard(new Card(8, 'H'));

        // Only [0](FH,6-triple) and [1](FK,6-quad) should remain
        // Verify: the 3 deleted hands are no longer in the tree
        System.out.println(">> Check deleted hand [2] is gone");
        passed &= assertEquals(testRBT.findNode(myHandsArray[2]), null);
        System.out.println(">> Check deleted hand [3] is gone");
        passed &= assertEquals(testRBT.findNode(myHandsArray[3]), null);
        System.out.println(">> Check deleted hand [4] is gone");
        passed &= assertEquals(testRBT.findNode(myHandsArray[4]), null);

        // Verify: the 2 remaining hands are still in the tree
        System.out.println(">> Check remaining hand [0] is still there");
        passed &= assertEquals(testRBT.findNode(myHandsArray[0]) != null, true);
        System.out.println(">> Check remaining hand [1] is still there");
        passed &= assertEquals(testRBT.findNode(myHandsArray[1]) != null, true);

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }



    // Assertions

    private static boolean assertEquals(Hands a, Hands b)
    {
        if(!a.isMyHandEqual(b))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: ");
            a.printMyHand();
            System.out.printf(", Actual: ");
            b.printMyHand();
            System.out.printf("\n");
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean a, boolean b)
    {
        if(a != b)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", a, b);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(HandsRBTNode a, HandsRBTNode b)
    {
        if(a != b)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", a, b);
            return false;
        }

        return true;
    }

}

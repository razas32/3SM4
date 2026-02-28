// Ali Raza - 400338538
// Lab 3: Model Code - Card Game Integration (RBT + MaxHeap)
// McMaster University - COMP ENG 3SM4

import java.util.*;

public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;
    public static Scanner myInputScanner;

    public static void main(String args[]) throws Exception
    {
        CardPool myCardPool;
        Card[] aiCards, myCards;
        Hands aiHand, myHand;

        // Using HandsMaxHeap for AI (completed in Lab 2) for better performance
        HandsMaxHeap aiHeap;
        HandsRBT myRBT;

        int aiPocketSize = POCKETSIZE, myPocketSize = POCKETSIZE;
        int aiScore = 0, playerScore = 0;

        myCardPool = new CardPool();
        myInputScanner = new Scanner(System.in);

        // ==============================================================
        // Step 1 - Initialization
        // Draw 25 cards for AI and 25 for Player from the shared card pool
        // Sort both pockets so hands are easier to analyze visualy
        // Generate all valid 5-card hands into the respective data structures
        // ==============================================================

        aiCards = myCardPool.getRandomCards(POCKETSIZE);
        myCards  = myCardPool.getRandomCards(POCKETSIZE);

        System.out.println("Drawing 25 cards for AI...");
        System.out.println("Drawing 25 cards for Player...");

        sortCards(aiCards);
        sortCards(myCards);

        System.out.println("Sorting AI pocket cards...");
        System.out.println("Sorting Player pocket cards...");

        // Player's valid hands go into a Red-Black Tree (Lab 3 focus)
        myRBT = new HandsRBT();
        System.out.println("Generating all valid Player hands into Red-Black Tree...");
        generateHandsIntoRBT(myCards, myRBT);

        // AI's valid hands go into a Max Heap (carried over from Lab 2)
        aiHeap = new HandsMaxHeap(60000);
        System.out.println("Generating all valid AI hands into Max Heap...");
        generateHandsIntoHeap(aiCards, aiHeap);

        // ==============================================================
        // Step 2 - Game Loop (exactly 5 rounds: 25 cards / 5 per hand)
        // Each round: show cards, player picks, AI picks, compare, score
        // ==============================================================

        int totalRounds = POCKETSIZE / 5;  // = 5

        for (int round = 1; round <= totalRounds; round++)
        {
            // ----------------------------------------------------------
            // Step 2-1: Print both pockets for strategy analysis
            // AI cards have no serial number; Player cards do (1-indexed)
            // ----------------------------------------------------------

            System.out.println("AI Pocket Cards:");
            for (int i = 0; i < aiPocketSize; i++)
                aiCards[i].printCard();
            System.out.println();

            System.out.printf("My Pocket Cards (Count: %d)\n", myPocketSize);
            for (int i = 0; i < myPocketSize; i++)
            {
                System.out.printf("[%d]", i + 1);
                myCards[i].printCard();
            }
            System.out.println();

            // Let player know if they're out of valid moves
            if (myRBT.isEmpty())
                System.out.println(" OUT OF HANDS!");

            // ----------------------------------------------------------
            // Step 2-2: Get the player's chosen hand
            // If the RBT is not empty and the chosen hand isn't in it,
            // keep asking - no passing allowed while valid moves exist
            // ----------------------------------------------------------

            Hands playerHand = null;

            do
            {
                playerHand = getUserHand(myCards);

                // Only reject the move if the RBT still has valid options
                if (!myRBT.isEmpty() && myRBT.findNode(playerHand) == null)
                {
                    System.out.println("Cannot Pass! You still have valid 5-card hands to make a move.");
                    playerHand = null;  // force the player to try again
                }
            }
            while (playerHand == null);

            // ----------------------------------------------------------
            // Step 2-3: Commit the player's hand and update state
            // Remove all hands that share any of the 5 played cards from RBT
            // Then shrink the pocket by removing the 5 consumed cards
            // ----------------------------------------------------------

            myHand = playerHand;

            // Delete all RBT nodes whose hands contain any of the played 5 cards
            myRBT.deleteInvalidHands(myHand);

            // Build new pocket without the 5 cards just played
            Card[] playedCards = new Card[5];
            for (int i = 0; i < 5; i++)
                playedCards[i] = myHand.getCard(i);

            Card[] newMyCards = new Card[myPocketSize - 5];
            int idx = 0;

            for (int i = 0; i < myPocketSize; i++)
            {
                boolean wasPlayed = false;
                for (int j = 0; j < 5; j++)
                {
                    if (myCards[i].isMyCardEqual(playedCards[j]))
                    {
                        wasPlayed = true;
                        break;
                    }
                }
                if (!wasPlayed)
                    newMyCards[idx++] = myCards[i];
            }

            myCards     = newMyCards;
            myPocketSize -= 5;

            // ----------------------------------------------------------
            // Step 2-4: AI makes the most aggressive move (Lab 2 logic)
            // If heap has valid hands, pull the max; otherwise pass
            // Then remove the 5 AI cards and regenerate the heap
            // ----------------------------------------------------------

            if (!aiHeap.isEmpty())
            {
                aiHand = aiHeap.removeMax();
            }
            else
            {
                // No valid hands left - AI plays any 5 remaining cards as a pass
                aiHand = new Hands(aiCards[0], aiCards[1], aiCards[2], aiCards[3], aiCards[4]);
            }

            // Remove the 5 AI cards from its pocket
            Card[] aiPlayedCards = new Card[5];
            for (int i = 0; i < 5; i++)
                aiPlayedCards[i] = aiHand.getCard(i);

            Card[] newAiCards = new Card[aiPocketSize - 5];
            idx = 0;

            for (int i = 0; i < aiPocketSize; i++)
            {
                boolean wasPlayed = false;
                for (int j = 0; j < 5; j++)
                {
                    if (aiCards[i].isMyCardEqual(aiPlayedCards[j]))
                    {
                        wasPlayed = true;
                        break;
                    }
                }
                if (!wasPlayed)
                    newAiCards[idx++] = aiCards[i];
            }

            aiCards      = newAiCards;
            aiPocketSize -= 5;

            // Rebuild AI heap from the remaining pocket cards
            aiHeap = new HandsMaxHeap(60000);
            generateHandsIntoHeap(aiCards, aiHeap);

            // ----------------------------------------------------------
            // Step 2-5: Determine winner of this round and update scores
            // Print both hands, compare, and award the point
            // ----------------------------------------------------------

            System.out.printf("My Hand: ");
            myHand.printMyHand();
            System.out.println();

            System.out.printf("AI Hand: ");
            aiHand.printMyHand();
            System.out.println();

            if (myHand.isMyHandLarger(aiHand))
            {
                playerScore++;
                System.out.println(" [RESULT] Player Wins This Round!");
            }
            else if (aiHand.isMyHandLarger(myHand))
            {
                aiScore++;
                System.out.println(" [RESULT] AI Wins This Round!");
            }
            else
            {
                System.out.println(" [RESULT] Draw This Round!");
            }
        }

        // ==============================================================
        // Step 3 - Report Final Game Results
        // ==============================================================

        System.out.println();
        System.out.println("  ==== Game Result ====");
        System.out.printf("  Player Score: %d\n", playerScore);
        System.out.printf("  AI Score: %d\n", aiScore);
        System.out.println();

        if (playerScore > aiScore)
            System.out.println("  << Player Won >>");
        else if (aiScore > playerScore)
            System.out.println("  << AI Won >>");
        else
            System.out.println("  << Draw >>");

        System.out.println();
        System.out.println("  ====================");

        myInputScanner.close();
    }

    // Populates all valid 5-card hands from the given card pocket into the BST
    // (provided for completeness - AI uses HandsMaxHeap instead)
    public static void generateHandsIntoBST(Card[] cards, HandsBST thisBST)
    {
        int n = cards.length;
        if (n < 5) return;

        for (int a = 0; a < n - 4; a++)
            for (int b = a + 1; b < n - 3; b++)
                for (int c = b + 1; c < n - 2; c++)
                    for (int d = c + 1; d < n - 1; d++)
                        for (int e = d + 1; e < n; e++)
                        {
                            Hands candidate = new Hands(cards[a], cards[b], cards[c], cards[d], cards[e]);
                            if (candidate.isAValidHand())
                                thisBST.insert(candidate);
                        }
    }

    // Populates all valid 5-card hands from the given card pocket into the RBT
    // Used for the Player's hand tracking structure
    public static void generateHandsIntoRBT(Card[] cards, HandsRBT thisRBT)
    {
        int n = cards.length;
        if (n < 5) return;

        // Try every C(n,5) combination of 5 cards from the pocket
        for (int a = 0; a < n - 4; a++)
            for (int b = a + 1; b < n - 3; b++)
                for (int c = b + 1; c < n - 2; c++)
                    for (int d = c + 1; d < n - 1; d++)
                        for (int e = d + 1; e < n; e++)
                        {
                            Hands candidate = new Hands(cards[a], cards[b], cards[c], cards[d], cards[e]);
                            if (candidate.isAValidHand())
                                thisRBT.insert(candidate);
                        }
    }

    // Populates all valid 5-card hands from the given card pocket into the MaxHeap
    // Used for the AI's agressive hand selection (Lab 2 strategy)
    private static void generateHandsIntoHeap(Card[] cards, HandsMaxHeap thisHeap)
    {
        int n = cards.length;
        if (n < 5) return;

        for (int a = 0; a < n - 4; a++)
            for (int b = a + 1; b < n - 3; b++)
                for (int c = b + 1; c < n - 2; c++)
                    for (int d = c + 1; d < n - 1; d++)
                        for (int e = d + 1; e < n; e++)
                        {
                            Hands candidate = new Hands(cards[a], cards[b], cards[c], cards[d], cards[e]);
                            if (candidate.isAValidHand())
                                thisHeap.insert(candidate);
                        }
    }

    // Sorts the card array in ascending order: by rank first, then by suite for ties
    // Using insertion sort (same as Lab 2)
    public static void sortCards(Card[] cards)
    {
        int j, size;
        Card temp;
        size = cards.length;

        for (int i = 1; i < size; i++)
        {
            temp = cards[i];
            for (j = i; j > 0 && cards[j - 1].isMyCardLarger(temp); j--)
                cards[j] = cards[j - 1];
            cards[j] = temp;
        }
    }

    // Allows the player to select 5 cards by serial number to form a hand
    // Serial numbers are 1-indexed; out-of-bounds input is clamped automatically
    public static Hands getUserHand(Card[] myCards)
    {
        int size = myCards.length;
        int[] mySelection = new int[5];
        boolean[] used = new boolean[size];
        // Use the shared scanner (initialized in main) - dont recreate it here
        // as that would reset the input buffer and lose already-buffered data

        System.out.println();
        for (int i = 0; i < 5; i++)
        {
            while (true)
            {
                System.out.printf("Card Choice #%d: ", i + 1);
                mySelection[i] = myInputScanner.nextInt() - 1;

                if (mySelection[i] >= size) mySelection[i] = size - 1;
                if (mySelection[i] < 0)     mySelection[i] = 0;

                if (used[mySelection[i]])
                {
                    System.out.println("You already picked that card. Pick a diffrent one.");
                }
                else
                {
                    used[mySelection[i]] = true;
                    break;
                }
            }
        }

        Hands newHand = new Hands(myCards[mySelection[0]],
                                   myCards[mySelection[1]],
                                   myCards[mySelection[2]],
                                   myCards[mySelection[3]],
                                   myCards[mySelection[4]]);

        return newHand;
    }

}

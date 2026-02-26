// Ali Raza - 400338538
// Lab 2: Aggressive AI Model Code - Card Game Integration
// McMaster University - COMP ENG 3SM4

public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;

    public static CardPool myCardPool;
    public static HandsMaxHeap myMaxHeap;

    public static Card[] myCards, tempCards;
    public static int pocketSize = POCKETSIZE;

    /* Generate all valid 5-card hands from pocket and put them in the heap
     * This is how the AI stays aggressive - once all valid hands are in the heap,
     * just keep calling removeMax to get the strongest one!
     * Time: O(C(n,5) * log k) where n = pocket size, k = valid hands
     * Space: O(C(n,5)) worst case but realistically way less */
    public static void generateHands(Card[] thisPocket)
    {
        int n = thisPocket.length;

        // need at least 5 cards to make a hand
        if(n < 5)
        {
            myMaxHeap = new HandsMaxHeap(1);
            return;
        }

        // allocate heap - worst case C(25,5) = 53,130 but most combos aren't valid hands
        // starting with 60k capacity to be safe
        myMaxHeap = new HandsMaxHeap(60000);

        // brute force: try all C(n,5) combinations with 5 nested loops
        // not the fastest approach but guaranteed to find everything
        for(int i = 0; i < n - 4; i++)
        {
            for(int j = i + 1; j < n - 3; j++)
            {
                for(int k = j + 1; k < n - 2; k++)
                {
                    for(int m = k + 1; m < n - 1; m++)
                    {
                        for(int p = m + 1; p < n; p++)
                        {
                            // try this combo of 5 cards
                            Hands candidateHand = new Hands(
                                thisPocket[i],
                                thisPocket[j],
                                thisPocket[k],
                                thisPocket[m],
                                thisPocket[p]
                            );

                            // if it's a valid poker hand (RF/SF/FK/FH/S), add it to heap
                            if(candidateHand.isAValidHand())
                            {
                                myMaxHeap.insert(candidateHand);
                            }
                        }
                    }
                }
            }
        }

        // done! heap has all valid hands with strongest at root
        System.out.println("Generated " + myMaxHeap.getSize() + " valid hands from " + n + " cards.");
    }

    // Sorts the array of Cards in ascending order: ascending order of ranks; cards of equal ranks are sorted in ascending order of suits
    public static void sortCards(Card[] cards)
    {
        int j;
        Card temp;        
        int size = cards.length;
        
        for(int i = 1; i < size; i++) 
        { 
            temp = cards[i];		
            for(j = i; j > 0 && cards[j-1].isMyCardLarger(temp); j--) 
                cards[j] = cards[j-1]; 
            cards[j] = temp;
        }  
    }

    public static void main(String args[]) throws Exception
    {
        myCardPool = new CardPool();        
        myCardPool.printPool();

        myCards = new Card[pocketSize];
        myCards = myCardPool.getRandomCards(POCKETSIZE);  
        sortCards(myCards);

        // print cards
        System.out.println("My Pocket Cards:");
        for(int j = 0; j < pocketSize; j++)
        {            
            myCards[j].printCard();
        }
        System.out.println();
        
        generateHands(myCards); // generates all valid hands from myCards and stores them in myMaxHeap
        myMaxHeap.printHeap(); // prints the contents of the initial heap

        // [Problem 3] Implementing Game Logic - Aggressive AI: Always Picks the Strongest Hand
        int moveNumber = 1;

        while(pocketSize > 4)
        {
            System.out.println("\n========================================");
            System.out.println("MOVE #" + moveNumber);
            System.out.println("========================================");

            // Step 1: Pick the most aggressive move
            Hands chosenHand;
            Card[] usedCards = new Card[5];  // remember which cards we use

            if(!myMaxHeap.isEmpty())
            {
                // heap has valid hands - grab the strongest!
                chosenHand = myMaxHeap.removeMax();
                System.out.println("Playing STRONGEST hand: ");
                chosenHand.printMyHand();
                System.out.println();

                // pull out the 5 cards in this hand
                for(int c = 0; c < 5; c++)
                {
                    usedCards[c] = chosenHand.getCard(c);
                }
            }
            else
            {
                // no valid hands left, make a pass move with any 5 cards
                System.out.println("No valid hands remaining - PASS MOVE with first 5 cards:");
                chosenHand = new Hands(myCards[0], myCards[1], myCards[2], myCards[3], myCards[4]);
                chosenHand.printMyHand();
                System.out.println();

                for(int c = 0; c < 5; c++)
                {
                    usedCards[c] = myCards[c];
                }
            }

            // Step 2: Remove used cards and update the heap
            // rebuild pocket without the 5 cards we just played
            Card[] newPocket = new Card[pocketSize - 5];
            int newIndex = 0;

            for(int i = 0; i < pocketSize; i++)
            {
                boolean cardWasUsed = false;

                // is myCards[i] one of the 5 we just used?
                for(int u = 0; u < 5; u++)
                {
                    if(myCards[i].isMyCardEqual(usedCards[u]))
                    {
                        cardWasUsed = true;
                        break;
                    }
                }

                // keep this card if we didn't use it
                if(!cardWasUsed)
                {
                    newPocket[newIndex] = myCards[i];
                    newIndex++;
                }
            }

            // switch to new pocket
            myCards = newPocket;
            pocketSize = pocketSize - 5;

            System.out.println("\nRemaining pocket cards (" + pocketSize + " cards):");
            for(int j = 0; j < pocketSize; j++)
            {
                myCards[j].printCard();
            }
            System.out.println();

            // update heap - throw out any hands that contain the used cards
            // extract all hands, filter out contaminated ones, rebuild
            HandsMaxHeap newHeap = new HandsMaxHeap(60000);

            while(!myMaxHeap.isEmpty())
            {
                Hands h = myMaxHeap.removeMax();
                boolean isContaminated = false;

                // does this hand have any of the cards we just used?
                for(int u = 0; u < 5; u++)
                {
                    if(h.hasCard(usedCards[u]))
                    {
                        isContaminated = true;
                        break;
                    }
                }

                // keep it only if it's clean
                if(!isContaminated)
                {
                    newHeap.insert(h);
                }
            }

            myMaxHeap = newHeap;

            System.out.println("Updated heap after removing contaminated hands:");
            myMaxHeap.printHeap();

            moveNumber++;
        }

        System.out.println("\n========================================");
        System.out.println("GAME OVER - All cards used!");
        System.out.println("Total moves played: " + (moveNumber - 1));
        System.out.println("========================================");
    }

}

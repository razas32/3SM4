# Lab 2 – Aggressive AI Card Game (Max Heap)
**Ali Raza – 400338538 | McMaster COMP ENG 3SM4**

---

## How to Run

```bash
# Compile all files (from the LAB2 directory)
javac Card.java CardPool.java Hands.java HandsMaxHeap.java ModelCode_CardGame.java

# Run the game
java ModelCode_CardGame

# Run the heap unit tests (built into HandsMaxHeap)
java HandsMaxHeap
```

Each run is random — 25 cards drawn from a 52-card deck.

---

## What the Program Does

An AI card player that is always **aggressive**: it always plays its strongest available 5-card poker hand.

**Flow:**
1. Draw 25 random cards ("the pocket")
2. Generate ALL valid 5-card hands from the pocket → store in a **Max Heap**
3. Loop 5 times (25 cards ÷ 5 per hand):
   - Pull the strongest hand off the heap (`removeMax`)
   - Remove those 5 cards from the pocket
   - Remove any heap hands that contained the used cards (heap update)
4. Print results after each move

---

## Hand Types (ordered weakest → strongest)

| Value | Type | Description |
|---|---|---|
| 0 | Straight (S) | 5 consecutive ranks, mixed suits |
| 1 | Full House (FH) | 3-of-a-kind + a pair |
| 2 | Four-of-a-Kind (FK) | 4 cards same rank |
| 3 | Straight Flush (SF) | 5 consecutive, same suit, no Ace |
| 4 | Royal Flush (RF) | Straight flush ending in Ace |

`-1` = not a valid hand. `5` = dummy (used at heap index 0).

---

## Class Breakdown

| File | Role |
|---|---|
| `Card.java` | rank (2–14) + suit (C/D/H/S), compare/print |
| `CardPool.java` | 52-card deck, random draw without replacement |
| `Hands.java` | 5-card hand, detect type, compare hands |
| `HandsMaxHeap.java` | Array-based max heap of `Hands` objects + tests |
| `ModelCode_CardGame.java` | Main driver: game loop |

---

## Algorithms & Complexity — With Code

### Array Indexing (1-based, index 0 = dummy)

```
parent(i)     = i / 2
leftChild(i)  = 2 * i
rightChild(i) = 2 * i + 1
```

This is why the array is size `capacity + 1` — index 0 is never used, just holds a dummy hand.

---

### `buildMaxHeap` — O(n) time, O(1) space
**`HandsMaxHeap.java:46`**

Turn an unsorted array into a valid max heap in one pass. Start at the **last internal node** (`size/2`) — nodes after that are leaves and are already valid 1-element heaps. Work backwards to the root, calling `downHeapify` on each.

```java
private void buildMaxHeap() {
    for (int i = size / 2; i >= 1; i--) {
        downHeapify(i);
    }
}
```

**Why O(n) not O(n log n)?** Most nodes are near the bottom and barely move. The math (sum of levels × work per level) works out to O(n). This is much better than inserting n elements one by one.

---

### `downHeapify` (percolate down) — O(log n) time, O(1) space
**`HandsMaxHeap.java:97`**

Push a node down until it's larger than both its children. Used after `removeMax` (when the last element is placed at root) and in `buildMaxHeap`.

```java
private void downHeapify(int index) {
    Hands temp = myHeap[index];          // save what we're sinking

    while (leftChild(index) <= size) {   // while a left child exists
        int left = leftChild(index);
        int right = rightChild(index);
        int largerChild = left;

        // pick the bigger child
        if (right <= size && myHeap[right].isMyHandLarger(myHeap[left]))
            largerChild = right;

        // if current node >= larger child, we're done
        if (temp.isMyHandLarger(myHeap[largerChild]) || temp.isMyHandEqual(myHeap[largerChild]))
            break;

        // pull child up, continue sinking
        myHeap[index] = myHeap[largerChild];
        index = largerChild;
    }
    myHeap[index] = temp;  // final resting place
}
```

---

### `heapifyUp` (percolate up) — O(log n) time, O(1) space
**`HandsMaxHeap.java:137`**

Bubble a newly inserted node up until its parent is larger. Used after every `insert`.

```java
private void heapifyUp(int index) {
    Hands temp = myHeap[index];

    while (index > 1 && myHeap[parent(index)].isMyHandSmaller(temp)) {
        myHeap[index] = myHeap[parent(index)];  // pull parent down
        index = parent(index);                   // move up
    }
    myHeap[index] = temp;
}
```

---

### `insert` — O(log n) amortized, O(n) on resize
**`HandsMaxHeap.java:162`**

Append the new hand at the end, then `heapifyUp`. If the array is full, double capacity first (amortized cost = O(1) per insert over many inserts).

```java
public void insert(Hands thisHand) {
    if (size >= capacity) {
        capacity = capacity * 2;
        Hands[] newHeap = new Hands[capacity + 1];
        for (int i = 0; i <= size; i++)
            newHeap[i] = myHeap[i];
        myHeap = newHeap;
    }
    size++;
    myHeap[size] = thisHand;
    heapifyUp(size);
}
```

---

### `removeMax` — O(log n) time, O(1) space
**`HandsMaxHeap.java:192`**

The root (`myHeap[1]`) is always the strongest hand. Grab it, move the last element to the root, shrink size, then `downHeapify` to restore order.

```java
public Hands removeMax() {
    if (size == 0) throw new RuntimeException("Heap is empty!");

    Hands maxHand = myHeap[1];      // always the max
    myHeap[1] = myHeap[size];       // move last to root
    size--;
    if (size > 0)
        downHeapify(1);             // sink it down
    return maxHand;
}
```

---

### `generateHands` — O(n^5 · log k) time, O(k) space
**`ModelCode_CardGame.java:20`**

Brute-force: 5 nested loops enumerate every C(n,5) combination of cards. Each is tested — if it's a valid hand, insert into the heap.

```java
public static void generateHands(Card[] thisPocket) {
    int n = thisPocket.length;
    myMaxHeap = new HandsMaxHeap(60000);

    for (int i = 0; i < n-4; i++)
      for (int j = i+1; j < n-3; j++)
        for (int k = j+1; k < n-2; k++)
          for (int m = k+1; m < n-1; m++)
            for (int p = m+1; p < n; p++) {
                Hands candidate = new Hands(
                    thisPocket[i], thisPocket[j], thisPocket[k],
                    thisPocket[m], thisPocket[p]
                );
                if (candidate.isAValidHand())
                    myMaxHeap.insert(candidate);
            }
}
```

For n=25: C(25,5) = **53,130 combinations** checked. Only ~400–500 are valid hands and get inserted.

---

### Heap update after each move — O(k log k) time, O(k) space
**`ModelCode_CardGame.java:188`**

After playing a hand, drain the entire heap. Keep only hands that share no card with the 5 just played. Rebuild.

```java
HandsMaxHeap newHeap = new HandsMaxHeap(60000);
while (!myMaxHeap.isEmpty()) {
    Hands h = myMaxHeap.removeMax();
    boolean contaminated = false;
    for (int u = 0; u < 5; u++) {
        if (h.hasCard(usedCards[u])) { contaminated = true; break; }
    }
    if (!contaminated) newHeap.insert(h);
}
myMaxHeap = newHeap;
```

---

### `heapSort` (static) — O(n log n) time, O(1) space
**`HandsMaxHeap.java:261`**

Sort an array in-place. Phase 1: build a max heap. Phase 2: repeatedly swap the root (max) to the end and re-heapify the shrinking heap.

```java
public static void heapSort(Hands[] myHands) {
    int n = myHands.length;

    // Phase 1: build max heap (0-indexed)
    for (int i = n/2 - 1; i >= 0; i--)
        heapifyDown(myHands, n, i);

    // Phase 2: extract max repeatedly
    for (int i = n-1; i > 0; i--) {
        Hands temp = myHands[0];   // swap max to end
        myHands[0] = myHands[i];
        myHands[i] = temp;
        heapifyDown(myHands, i, 0); // fix shrinking heap
    }
}
```

---

### `sortCards` / insertion sort — O(n²) time, O(1) space
**`ModelCode_CardGame.java:72`** and **`Hands.java:143`**

Used to sort pocket cards (n=25) and hand cards (n=5). For such small n it's effectively O(1).

```java
public static void sortCards(Card[] cards) {
    for (int i = 1; i < cards.length; i++) {
        Card temp = cards[i];
        int j = i;
        while (j > 0 && cards[j-1].isMyCardLarger(temp)) {
            cards[j] = cards[j-1];
            j--;
        }
        cards[j] = temp;
    }
}
```

---

## Complexity Summary

| Phase | Time | Space |
|---|---|---|
| Draw 25 cards | O(n) | O(n) |
| Sort pocket | O(n²) | O(1) |
| Generate all hands | O(n^5 · log k) | O(k) |
| Per move: play + update | O(k log k) | O(k) |
| **Total** | **O(n^5 + k log k)** | **O(k)** |

n=25, k≈400–500 valid hands. Dominated by initial generation (~480,000 ops in practice).

**Memory peak:** ~500 KB (mostly the pre-allocated 60,000-slot heap array).

---

## Demo Discussion — Alternatives

| Approach | Update Cost | Extract Max | Memory | Verdict |
|---|---|---|---|---|
| **My approach** (drain+rebuild) | O(k log k) | O(log k) | O(k) | Correct, clear |
| Linear array, scan for max | O(k) | O(k) | O(k) | Simpler but slower extraction |
| Regenerate each move | O(1) | O(n^5) | O(1) | Memory efficient, much slower |
| **Lazy deletion** (best!) | O(1) | O(log k) amortized | O(k) | Best all-around |

**Lazy deletion:** Keep a HashSet of used card IDs. On `removeMax`, keep popping until you find a clean hand. Update becomes O(1) instead of O(k log k) — biggest win possible with minimal code change.

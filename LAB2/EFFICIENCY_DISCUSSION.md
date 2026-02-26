# Lab 2 Efficiency Discussion - Ali Raza (400338538)

This document prepares me for the demo discussion about running time and memory usage of my implementation.

---

## 1. Running Time Analysis

### generateHands() - Initial Hand Generation
**Time Complexity: O(n^5 * log k)**
- I use 5 nested loops to generate all C(n,5) combinations of cards
- For n=25 cards: C(25,5) = 53,130 possible combinations
- For each combination, I create a Hands object and check if it's valid
- If valid, I insert it into the heap (O(log k) where k is current heap size)
- In practice, most combinations aren't valid hands, so I only insert ~400-500 hands
- **Actual time**: O(53,130 * log 500) ≈ O(478,000 operations)

**Why I chose this approach:**
- Brute force but guaranteed to find ALL valid hands
- Simple to implement and understand
- Only runs once at the start, so the cost is acceptable

### Game Loop - Playing Hands
**Per Move:**
1. **removeMax()**: O(log k) - grab the strongest hand
2. **Remove used cards from pocket**: O(n) - linear scan through pocket (n ≤ 25)
3. **Update heap**: O(k^2) - THIS IS THE BOTTLENECK!
   - Extract all k hands from heap: O(k log k)
   - Check each hand against 5 used cards: O(5k)
   - Rebuild heap with remaining hands: O(k log k)
   - **Total per move: O(k log k)**

**Total game time**: 5 moves × O(k log k) where k shrinks each move
- Move 1: k=416, Move 2: k≈300, Move 3: k≈200, etc.
- **Overall: O(k log k)** where k is initial heap size

### Overall Program Complexity
**Time: O(n^5 + k log k)** dominated by the initial hand generation

---

## 2. Memory Requirements

### Heap Storage
- **Worst case**: C(25,5) = 53,130 hands
- **Actual**: ~400-500 valid hands from 25 random cards
- Each Hand object: 5 Card references + metadata ≈ 40-50 bytes
- Each Card object: 2 fields (rank, suite) ≈ 8 bytes
- **Total heap memory**: ~500 hands × 50 bytes = **25 KB**

### Array Storage
- myHeap array: allocated with capacity 60,000 (generous buffer)
- Array overhead: 60,000 × 8 bytes (reference) = **480 KB**
- I could optimize this by starting smaller and expanding as needed

### Pocket Cards
- 25 cards initially, shrinks to 0
- Negligible memory: 25 × 8 bytes = **200 bytes**

### Total Memory: O(k) where k is number of valid hands
- **Peak usage: ~500 KB** (mostly the pre-allocated array)

---

## 3. Comparison with Alternative Approaches

### Alternative 1: Array with Linear Search (No Heap)
**How it works:**
- Store all valid hands in a simple array
- Each move: linear search for the strongest hand O(k)
- Remove hand and rebuild array O(k)

**Trade-offs:**
- ❌ **Slower per move**: O(k) vs O(log k) for removeMax
- ✅ **Same memory**: Still need to store all hands
- ✅ **Simpler code**: No heap logic needed
- **Verdict**: Worse! The heap's O(log k) extraction is much better than O(k) search

### Alternative 2: Regenerate Hands Each Move
**How it works:**
- Don't store any hands
- Each move: regenerate all valid hands from current pocket
- Find max, play it, remove cards, repeat

**Trade-offs:**
- ✅ **Memory efficient**: Only store 1 hand at a time! O(1) space
- ❌ **WAY slower**: Regenerate C(n,5) combinations every move
  - Move 1: C(25,5) = 53,130 checks
  - Move 2: C(20,5) = 15,504 checks
  - Move 3: C(15,5) = 3,003 checks
  - Total: ~72,000 checks vs. my 53,130 initial + 5 O(log k) extractions
- **Verdict**: Good for memory-constrained systems, but 5-10x slower overall

### Alternative 3: Lazy Deletion (Mark Invalid Instead of Rebuild)
**How it works:**
- Keep all hands in heap
- Each move: mark the 5 used cards as "consumed"
- During removeMax: skip hands that contain consumed cards
- No heap rebuild needed!

**Trade-offs:**
- ✅ **Much faster updates**: O(1) to mark cards vs O(k log k) to rebuild
- ❌ **Wastes space**: Dead hands stay in heap until extracted
- ❌ **Slower removeMax**: Might need to skip many invalid hands O(k) worst case
- **Verdict**: Better for many moves! I should have used this approach.

---

## 4. How I Could Improve Efficiency

### Improvement 1: Lazy Deletion (Best bang for buck!)
**What to do:**
- Add a HashSet of "used cards" as a static variable
- When I play a hand, add its 5 cards to usedCards set
- In removeMax(), keep popping until I find a hand with no used cards
- **Time savings**: Update step becomes O(1) instead of O(k log k)!
- **Trade-off**: removeMax becomes O(k) worst case, but amortized still fast

### Improvement 2: Use HashSet for Card Lookup
**Current approach:**
- Check if hand contains used card: 5 nested loops = O(25) per hand
- Update heap: O(k) hands × O(25) checks = O(25k)

**Better approach:**
- Store used cards in HashSet
- Check if hand contains used card: O(5) hash lookups
- Update heap: O(k) hands × O(5) = O(5k) ✅ 5x faster!

### Improvement 3: Prune During Generation
**Current approach:**
- Generate ALL C(25,5) = 53,130 combinations
- Check each for validity
- Lots of wasted work checking invalid combinations

**Better approach:**
- While generating, skip combinations that can't possibly be valid
- For example: if first 3 cards have different ranks, can't be 4-of-a-kind or full house
- Could reduce checks by 50-70% with smart pruning
- **Trade-off**: More complex code, harder to debug

### Improvement 4: Start with Smaller Heap Capacity
**Current**: Allocate 60,000 capacity immediately
**Better**: Start with 1,000, double when full
- **Memory savings**: ~450 KB → ~50 KB initially
- **Trade-off**: Might need 2-3 resize operations (minimal cost)

---

## 5. Final Thoughts - What I'd Do Differently

If I had more time, I would implement **Lazy Deletion** (#1) because:
1. Biggest performance win (O(k log k) → O(1) per update)
2. Simple to implement (just a HashSet and a while loop)
3. Matches real-world scenarios (don't rebuild data structures unnecessarily)

My current implementation prioritizes **correctness and clarity** over peak performance:
- ✅ Easy to understand and debug
- ✅ Guaranteed to always show the strongest available hand
- ✅ Clean heap invariant maintained at all times
- ✅ Works perfectly for the 25-card / 5-move scenario

For a production card game with 100s of moves, I'd definitely switch to lazy deletion!

---

**Summary Table:**

| Metric | My Implementation | With Lazy Deletion | Regenerate Each Move |
|--------|-------------------|-------------------|---------------------|
| Initial Gen | O(n^5 log k) | O(n^5 log k) | O(1) |
| Per Move | O(k log k) | O(1) | O(n^5) |
| Total Time | O(n^5 + k log k) | O(n^5) | O(n^5 × moves) |
| Memory | O(k) = 500KB | O(k) = 500KB | O(1) = 1KB |
| Code Complexity | Medium | Medium | Low |
| **Best for** | Clear logic | Many moves | Limited memory |

My choice was the **middle ground** - good performance, maintainable code, and room for optimization if needed!

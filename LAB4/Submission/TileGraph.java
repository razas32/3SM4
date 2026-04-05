// Ali Raza - 400338538
// Lab 4: Auto TileMap Navigation using Graph
// McMaster University - COMP ENG 3SM4

package Lab3Model;

import java.util.*;

public class TileGraph {

    // the graph is stored as an adjacency list — each Tile maps to the list of Tiles it connects to
    // using a HashMap gives us O(1) lookup per node, which is key for BFS/DFS performance
    private Map<Tile, LinkedList<Tile>> adjList;

    // empty graph — used in tests where we manually add vertices and edges
    public TileGraph()
    {
        adjList = new HashMap<Tile, LinkedList<Tile>>();
    }

    // this constructor is what the actual game uses — pass in the TileMap and it auto-builds the graph
    public TileGraph(TileMap mRef)
    {
        adjList = new HashMap<>();
        buildGraph(mRef);
    }

    // Time: O(1) avg - putIfAbsent is a single HashMap op
    // adding a vertex just means registering a Tile as a key in the map with an empty neighbor list
    // putIfAbsent makes sure we don't accidentally overwrite an existing entry
    private void addVertex(Tile thisTile)
    {
        adjList.putIfAbsent(thisTile, new LinkedList<Tile>());
    }

    // Time: O(deg(src)) - have to walk src's list to check for the duplicate
    // adding an edge means appending dst to src's neighbor list
    // we check for duplicates first so the same road doesn't get added twice
    private void addEdge(Tile src, Tile dst)
    {
        LinkedList<Tile> srcList = adjList.get(src);
        if (!srcList.contains(dst))
            srcList.add(dst);
    }

    // Time: O(1) avg - single HashMap lookup by key
    // just returns the adjacency list for a given tile — used internally by DFS and BFS
    private LinkedList<Tile> getAdjacentVertices(Tile thisTile)
    {
        return adjList.get(thisTile);
    }

    // Time: O(V + E) — each intersection is queued once, scan along each road once per direction
    // Space: O(V) for the queue and visited set, plus O(V + E) for the graph itself
    // builds the graph by doing a BFS over the TileMap itself
    // only intersections ('I') and destinations ('D') become vertices — roads are just how we find neighbors
    // from each intersection, we scan right and down until we hit a wall or the next intersection
    private void buildGraph(TileMap mRef)
    {
        Tile[][] map = mRef.getMapRef();

        // spec says start is always at [1][1]
        Tile startTile = map[1][1];
        addVertex(startTile);

        // use BFS to discover all reachable intersections from the start
        LinkedList<Tile> queue = new LinkedList<>();
        HashSet<Tile> visited = new HashSet<>();
        queue.add(startTile);
        visited.add(startTile);

        while (!queue.isEmpty())
        {
            Tile current = queue.poll();
            int cx = current.getX();
            int cy = current.getY();

            // scan right along the same row — stop at a wall, connect and stop at the next intersection
            for (int x = cx + 1; x < TileMap.BOARDSIZEX - 1; x++)
            {
                char t = map[cy][x].getTileType();
                if (t == '#') break;  // wall blocks the road

                if (t == 'I' || t == 'D')
                {
                    Tile neighbor = map[cy][x];
                    addVertex(neighbor);
                    addEdge(current, neighbor);
                    // only queue it if we haven't visited it yet — same as standard BFS
                    if (!visited.contains(neighbor)) { visited.add(neighbor); queue.add(neighbor); }
                    break;
                }
            }

            // scan down along the same column — exact same logic, just on the y-axis
            for (int y = cy + 1; y < TileMap.BOARDSIZEY - 1; y++)
            {
                char t = map[y][cx].getTileType();
                if (t == '#') break;

                if (t == 'I' || t == 'D')
                {
                    Tile neighbor = map[y][cx];
                    addVertex(neighbor);
                    addEdge(current, neighbor);
                    if (!visited.contains(neighbor)) { visited.add(neighbor); queue.add(neighbor); }
                    break;
                }
            }
        }
    }

    // Time: O(V + E) — visits every vertex and edge once
    // Space: O(V) for the stack and visited set
    // DFS — explores as deep as possible before backtracking
    // uses a stack: push neighbors, pop the top one next, go deep first
    public LinkedList<Tile> depthFirstTraversal(Tile start)
    {
        LinkedList<Tile> visitedList = new LinkedList<>();  // the result — order we visited nodes
        HashSet<Tile> visitedSet = new HashSet<>();         // fast lookup to avoid revisiting

        Stack<Tile> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty())
        {
            Tile current = stack.pop();

            // a node can be pushed multiple times before it's popped, so skip it if already visited
            if (visitedSet.contains(current)) continue;

            visitedSet.add(current);
            visitedList.add(current);

            // push neighbors in reverse so the first neighbor in the list gets processed first
            // this matches the expected traversal order in the tests
            LinkedList<Tile> neighbors = getAdjacentVertices(current);
            for (int i = neighbors.size() - 1; i >= 0; i--)
            {
                if (!visitedSet.contains(neighbors.get(i)))
                    stack.push(neighbors.get(i));
            }
        }

        return visitedList;
    }

    // Time: O(V + E) — standard BFS, each node and edge touched once
    // Space: O(V) for visited, parent map, and queue
    // BFS — explores level by level, guarantees the shortest path (fewest hops)
    // uses a queue: process nodes in the order they were discovered
    // also tracks each node's parent so we can reconstruct the path at the end
    public LinkedList<Tile> findShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> path = new LinkedList<>();

        HashSet<Tile> visited = new HashSet<>();
        HashMap<Tile, Tile> parent = new HashMap<>();  // parent[v] = the node we came from to reach v

        LinkedList<Tile> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        parent.put(start, null);  // start has no parent

        boolean found = false;

        while (!queue.isEmpty() && !found)
        {
            Tile current = queue.poll();  // dequeue front — BFS processes in order of discovery
            LinkedList<Tile> neighbors = getAdjacentVertices(current);

            // iterate in reverse to match expected BFS order in the tests
            for (int i = neighbors.size() - 1; i >= 0; i--)
            {
                Tile neighbor = neighbors.get(i);
                if (!visited.contains(neighbor))
                {
                    visited.add(neighbor);
                    parent.put(neighbor, current);  // remember how we got here
                    queue.add(neighbor);

                    // stop as soon as we find the destination — BFS guarantees this is the shortest
                    if (neighbor.isEqual(end)) { found = true; break; }
                }
            }
        }

        if (!found) return path;  // no path exists, return empty list

        // walk backwards from end to start using the parent map to reconstruct the path
        // result comes back reversed (end first) — that's the format Tracer expects
        Tile curr = end;
        while (curr != null) { path.add(curr); curr = parent.get(curr); }

        return path;
    }

    // Prints the entire graph: each vertex followed by its adjacency list
    public void printGraph()
    {
        Set<Tile> keySet = adjList.keySet();
        Collection<LinkedList<Tile>> valueLists = adjList.values();

        Iterator<Tile> keySetIter = keySet.iterator();
        Iterator<LinkedList<Tile>> valueListsIter = valueLists.iterator();
        int size = keySet.size();

        for (int i = 0; i < size; i++)
        {
            keySetIter.next().printTileCoord();
            System.out.printf(" >>\t");
            valueListsIter.next().forEach(e -> { e.printTileCoord(); System.out.printf(" : "); });
            System.out.println();
        }
    }



    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {
        testAddEdge1();
        testAddEdge2();
        testAddEdgeCustom();

        testGetAdjacentVertices1();
        testGetAdjacentVertices2();
        testGetAdjacentVerticesCustom();

        testDFT1();
        testDFT2();
        testDFTCustom();
        testFindShortestPath1();
        testFindShortestPath2();
        testFindShortestPathCustom();


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

    // Add Vertices and Edges
    // Add Vertices and Edges
    // Add Vertices and Edges

    private static void testAddVertex1()
    {
        // Setup
        System.out.println("============testAddVertex1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5)};

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        // Action
        for(int i = 0; i < 5; i++)
        {
            System.out.printf(">> Check Tile: ");
            tileArray[i].printTileCoord();
            System.out.println();

            passed &= assertEquals(true, testGraph.adjList.containsKey(tileArray[i]));
        }

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddVertex2()
    {
        // Setup
        System.out.println("============testAddVertex2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(16, 4, 'I', -5),
                              new Tile(16, 23, 'I', -5)};

        for(int i = 0; i < 10; i++)
            testGraph.addVertex(tileArray[i]);

        // Action
        for(int i = 0; i < 10; i++)
        {
            System.out.printf(">> Check Tile: ");
            tileArray[i].printTileCoord();
            System.out.println();

            passed &= assertEquals(true, testGraph.adjList.containsKey(tileArray[i]));        }

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddEdge1()
    {
        // Setup
        System.out.println("============testAddEdge1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5)};

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[3], tileArray[4]);


        // Action
        LinkedList<Tile> tempList;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(true, tempList.contains(tileArray[1]));
        passed &= assertEquals(true, tempList.contains(tileArray[2]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);
        passed &= assertEquals(true, tempList.contains(tileArray[3]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(true, tempList.isEmpty());


        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddEdge2()
    {
        // Setup
        System.out.println("============testAddEdge2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5)};

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[1], tileArray[5]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[2], tileArray[4]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);
        testGraph.addEdge(tileArray[5], tileArray[7]);
        testGraph.addEdge(tileArray[6], tileArray[7]);

        // Action
        LinkedList<Tile> tempList;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(true, tempList.contains(tileArray[1]));
        passed &= assertEquals(true, tempList.contains(tileArray[2]));
        passed &= assertEquals(true, tempList.contains(tileArray[3]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));
        passed &= assertEquals(true, tempList.contains(tileArray[5]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);
        passed &= assertEquals(true, tempList.contains(tileArray[3]));
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);
        passed &= assertEquals(true, tempList.contains(tileArray[6]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(true, tempList.contains(tileArray[5]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[5].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[5]);
        passed &= assertEquals(true, tempList.contains(tileArray[6]));
        passed &= assertEquals(true, tempList.contains(tileArray[7]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[6]);
        passed &= assertEquals(true, tempList.contains(tileArray[7]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[7].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[7]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddEdgeCustom()
    {
        // Setup
        System.out.println("============testAddEdgeCustom=============");
        boolean passed = true;
        totalTestCount++;

        // custom test: 3x3 grid of intersections + one disconnected node at bottom right
        // the key thing being tested here is the no-duplicate edge rule in addEdge
        // we intentionally add 0->1 twice and verify it only appears once in the adjacency list
        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0,  0,  'I', 0),   // 0
            new Tile(3,  0,  'I', 0),   // 1
            new Tile(6,  0,  'I', 0),   // 2
            new Tile(0,  3,  'I', 0),   // 3
            new Tile(3,  3,  'I', 0),   // 4
            new Tile(6,  3,  'I', 0),   // 5
            new Tile(0,  6,  'I', 0),   // 6
            new Tile(3,  6,  'I', 0),   // 7
            new Tile(6,  6,  'I', 0),   // 8
            new Tile(9,  9,  'I', 0)    // 9
        };

        for (int i = 0; i < 10; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[1], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[2], tileArray[5]);
        testGraph.addEdge(tileArray[3], tileArray[4]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[7]);
        testGraph.addEdge(tileArray[5], tileArray[8]);
        testGraph.addEdge(tileArray[6], tileArray[7]);
        testGraph.addEdge(tileArray[7], tileArray[8]);
        testGraph.addEdge(tileArray[8], tileArray[9]);
        testGraph.addEdge(tileArray[5], tileArray[9]);

        // add 0->1 again, shouldn't show up twice
        testGraph.addEdge(tileArray[0], tileArray[1]);

        // Action
        LinkedList<Tile> tempList;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(true, tempList.contains(tileArray[1]));
        passed &= assertEquals(true, tempList.contains(tileArray[3]));
        // count occurrences of [1] in [0]'s list — should be exactly 1
        int count = 0;
        for (Tile t : tempList) if (t.isEqual(tileArray[1])) count++;
        passed &= assertEquals(true, count == 1);

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(true, tempList.contains(tileArray[5]));
        passed &= assertEquals(true, tempList.contains(tileArray[7]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[8].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[8]);
        passed &= assertEquals(true, tempList.contains(tileArray[9]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[9].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[9]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }



    // Remove Vertices and Edges
    // Remove Vertices and Edges
    // Remove Vertices and Edges








    // Get Adjacent Vertices
       private static void testGetAdjacentVertices1()
    {
        // Setup
        System.out.println("============testGetNeighbours1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5)};

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[3], tileArray[4]);


        // Action
        LinkedList<Tile> tempList;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[0]);
        passed &= assertEquals(true, tempList.contains(tileArray[1]));
        passed &= assertEquals(true, tempList.contains(tileArray[2]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[1]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[2]);
        passed &= assertEquals(true, tempList.contains(tileArray[3]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[3]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[4]);
        passed &= assertEquals(true, tempList.isEmpty());



        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testGetAdjacentVertices2()
    {
        // Setup
        System.out.println("============testAddEdge2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5)};

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[1], tileArray[5]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[2], tileArray[4]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);
        testGraph.addEdge(tileArray[5], tileArray[7]);
        testGraph.addEdge(tileArray[6], tileArray[7]);

        // Action
        LinkedList<Tile> tempList;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[0]);
        passed &= assertEquals(true, tempList.contains(tileArray[1]));
        passed &= assertEquals(true, tempList.contains(tileArray[2]));
        passed &= assertEquals(true, tempList.contains(tileArray[3]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[1]);
        passed &= assertEquals(true, tempList.contains(tileArray[4]));
        passed &= assertEquals(true, tempList.contains(tileArray[5]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[2]);
        passed &= assertEquals(true, tempList.contains(tileArray[3]));
        passed &= assertEquals(true, tempList.contains(tileArray[4]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[3]);
        passed &= assertEquals(true, tempList.contains(tileArray[6]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[4]);
        passed &= assertEquals(true, tempList.contains(tileArray[5]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[5].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[5]);
        passed &= assertEquals(true, tempList.contains(tileArray[6]));
        passed &= assertEquals(true, tempList.contains(tileArray[7]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[6]);
        passed &= assertEquals(true, tempList.contains(tileArray[7]));

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[7].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[7]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }


    private static void testGetAdjacentVerticesCustom()
    {
        // Setup
        System.out.println("============testGetNeighboursCustom=============");
        boolean passed = true;
        totalTestCount++;

        // custom test: a chain with a branch off node 1
        // verifies getAdjacentVertices returns exactly the edges we added — no extras, no missing
        //
        //  0 -> 1 -> 2 -> 3
        //       |         |
        //       v         v
        //       4 -> 5 -> 6
        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0,  0,  'I', 0),   // 0
            new Tile(5,  0,  'I', 0),   // 1
            new Tile(10, 0,  'I', 0),   // 2
            new Tile(15, 0,  'I', 0),   // 3
            new Tile(5,  5,  'I', 0),   // 4
            new Tile(10, 5,  'I', 0),   // 5
            new Tile(15, 5,  'I', 0)    // 6
        };

        for (int i = 0; i < 7; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[1], tileArray[2]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);

        // Action
        LinkedList<Tile> tempList;

        // node 1 has two outgoing edges, make sure both show up
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[1]);
        passed &= assertEquals(true, tempList.contains(tileArray[2]));
        passed &= assertEquals(true, tempList.contains(tileArray[4]));
        passed &= assertEquals(true, tempList.size() == 2);

        // node 3 only goes to 6
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[3]);
        passed &= assertEquals(true, tempList.contains(tileArray[6]));
        passed &= assertEquals(true, tempList.size() == 1);

        // node 6 has no outgoing edges
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.getAdjacentVertices(tileArray[6]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }



    // Depth-First Traversal
    // Depth-First Traversal
    // Depth-First Traversal

    private static void testDFT1()
    {
        // Setup
        System.out.println("============testDFT1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5)};

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[3], tileArray[4]);


        // Action
        LinkedList<Tile> dftList = testGraph.depthFirstTraversal(tileArray[0]);

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(0).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[0], dftList.get(0));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(1).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[1], dftList.get(1));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(2).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[4], dftList.get(2));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(3).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[2], dftList.get(3));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(4).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[3], dftList.get(4));


        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testDFT2()
    {
        // Setup
        System.out.println("============testDFT2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5)};

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[1], tileArray[5]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[2], tileArray[4]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);
        testGraph.addEdge(tileArray[5], tileArray[7]);
        testGraph.addEdge(tileArray[6], tileArray[7]);

        // Action
        LinkedList<Tile> dftList = testGraph.depthFirstTraversal(tileArray[0]);

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(0).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[0], dftList.get(0));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(1).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[1], dftList.get(1));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(2).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[4], dftList.get(2));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(3).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[5], dftList.get(3));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(4).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[6], dftList.get(4));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(5).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[7], dftList.get(5));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(6).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[2], dftList.get(6));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(7).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[3], dftList.get(7));

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testDFTCustom()
    {
        // Setup
        System.out.println("============testDFTCustom=============");
        boolean passed = true;
        totalTestCount++;

        // custom test: 3x3 grid where all edges go right or down, plus one extra edge 2->8
        // the extra edge creates a second path to node 8 — DFS should still visit it only once
        // traced through by hand to figure out the correct DFT order before coding the assertions
        //
        //   0 -> 1 -> 2
        //   |    |    |  \
        //   v    v    v   v
        //   3 -> 4 -> 5   8
        //   |    |    |  /
        //   v    v    v
        //   6 -> 7 -> 8
        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0, 0, 'I', 0),  // 0
            new Tile(3, 0, 'I', 0),  // 1
            new Tile(6, 0, 'I', 0),  // 2
            new Tile(0, 3, 'I', 0),  // 3
            new Tile(3, 3, 'I', 0),  // 4
            new Tile(6, 3, 'I', 0),  // 5
            new Tile(0, 6, 'I', 0),  // 6
            new Tile(3, 6, 'I', 0),  // 7
            new Tile(6, 6, 'I', 0)   // 8
        };

        for (int i = 0; i < 9; i++)
            testGraph.addVertex(tileArray[i]);

        // right edges
        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[1], tileArray[2]);
        testGraph.addEdge(tileArray[3], tileArray[4]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[6], tileArray[7]);
        testGraph.addEdge(tileArray[7], tileArray[8]);
        // down edges
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[2], tileArray[5]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[7]);
        testGraph.addEdge(tileArray[5], tileArray[8]);

        testGraph.addEdge(tileArray[2], tileArray[8]);

        LinkedList<Tile> dftList = testGraph.depthFirstTraversal(tileArray[0]);

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(0).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[0], dftList.get(0));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(1).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[1], dftList.get(1));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(2).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[2], dftList.get(2));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(3).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[5], dftList.get(3));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(4).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[8], dftList.get(4));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(5).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[4], dftList.get(5));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(6).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[7], dftList.get(6));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(7).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[3], dftList.get(7));

        System.out.printf(">> Check DFT Resultant List: ");
        dftList.get(8).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[6], dftList.get(8));

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }



    // Find Shortest Path using Breadth-First Search
    // Find Shortest Path using Breadth-First Search
    // Find Shortest Path using Breadth-First Search

    private static void testFindShortestPath1()
    {
        // Setup
        System.out.println("============testFindShortestPath1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5)};

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[1], tileArray[5]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[2], tileArray[4]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);
        testGraph.addEdge(tileArray[5], tileArray[7]);
        testGraph.addEdge(tileArray[6], tileArray[7]);

        // Action
        LinkedList<Tile> pathList = testGraph.findShortestPath(tileArray[0], tileArray[7]);

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(0).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[7], pathList.get(0));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(1).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[6], pathList.get(1));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(2).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[3], pathList.get(2));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(3).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[0], pathList.get(3));

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testFindShortestPath2()
    {
        // Setup
        System.out.println("============testFindShortestPath2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5),
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5),
                              new Tile(10, 23, 'I', -5),
                              new Tile(5, 13, 'I', -5),
                              new Tile(10, 13, 'I', -5),
                              new Tile(16, 23, 'I', -5),
                              new Tile(16, 25, 'I', -5)};

        for(int i = 0; i < 12; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[1], tileArray[5]);
        testGraph.addEdge(tileArray[2], tileArray[3]);
        testGraph.addEdge(tileArray[2], tileArray[4]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[3], tileArray[8]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[6]);
        testGraph.addEdge(tileArray[5], tileArray[7]);
        testGraph.addEdge(tileArray[5], tileArray[9]);
        testGraph.addEdge(tileArray[6], tileArray[7]);
        testGraph.addEdge(tileArray[6], tileArray[10]);
        testGraph.addEdge(tileArray[7], tileArray[11]);
        testGraph.addEdge(tileArray[8], tileArray[6]);
        testGraph.addEdge(tileArray[8], tileArray[10]);
        testGraph.addEdge(tileArray[8], tileArray[9]);
        testGraph.addEdge(tileArray[9], tileArray[11]);
        testGraph.addEdge(tileArray[10], tileArray[11]);


        // Action
        LinkedList<Tile> pathList = testGraph.findShortestPath(tileArray[0], tileArray[11]);

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(0).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[11], pathList.get(0));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(1).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[9], pathList.get(1));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(2).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[8], pathList.get(2));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(3).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[3], pathList.get(3));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(4).printTileCoord();
        System.out.println();
        passed &= assertEquals(tileArray[0], pathList.get(4));

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testFindShortestPathCustom()
    {
        // Setup
        System.out.println("============testFindShortestPathCustom=============");
        boolean passed = true;
        totalTestCount++;

        // custom test: designed so there's one obvious shortest path (0->2->5->9, 3 hops)
        // and several longer alternates — verifies BFS picks the minimum hop count, not just any path
        // result comes back reversed as [9, 5, 2, 0] since we reconstruct by walking the parent chain
        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0,  0,  'I', 0),   // 0 - start
            new Tile(3,  0,  'I', 0),   // 1
            new Tile(6,  0,  'I', 0),   // 2
            new Tile(0,  4,  'I', 0),   // 3
            new Tile(3,  4,  'I', 0),   // 4
            new Tile(6,  4,  'I', 0),   // 5
            new Tile(0,  8,  'I', 0),   // 6
            new Tile(3,  8,  'I', 0),   // 7
            new Tile(6,  8,  'I', 0),   // 8
            new Tile(9,  4,  'I', 0)    // 9 - goal
        };

        for (int i = 0; i < 10; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[2]);
        testGraph.addEdge(tileArray[2], tileArray[5]);
        testGraph.addEdge(tileArray[5], tileArray[9]);

        // longer alternate routes
        testGraph.addEdge(tileArray[0], tileArray[1]);
        testGraph.addEdge(tileArray[1], tileArray[4]);
        testGraph.addEdge(tileArray[4], tileArray[5]);
        testGraph.addEdge(tileArray[0], tileArray[3]);
        testGraph.addEdge(tileArray[3], tileArray[6]);
        testGraph.addEdge(tileArray[6], tileArray[7]);
        testGraph.addEdge(tileArray[7], tileArray[8]);
        testGraph.addEdge(tileArray[8], tileArray[9]);
        testGraph.addEdge(tileArray[4], tileArray[7]);
        testGraph.addEdge(tileArray[1], tileArray[2]);

        // Action
        LinkedList<Tile> pathList = testGraph.findShortestPath(tileArray[0], tileArray[9]);

        // reversed: 9, 5, 2, 0
        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(0).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[9], pathList.get(0));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(1).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[5], pathList.get(1));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(2).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[2], pathList.get(2));

        System.out.printf(">> Check Shortest Path List (BFS): ");
        pathList.get(3).printTileCoord(); System.out.println();
        passed &= assertEquals(tileArray[0], pathList.get(3));

        // Tear Down
        totalPassed &= passed;
        if(passed)
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }





    private static boolean assertEquals(Tile expected, Tile actual)
    {
        if(!expected.isEqual(actual))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected:");
            expected.printTile();
            expected.printTileCoord();
            System.out.printf("\tActual:");
            actual.printTile();
            actual.printTileCoord();
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean expected, boolean actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", expected, actual);
            return false;
        }

        return true;
    }
}

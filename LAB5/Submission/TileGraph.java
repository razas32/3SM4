import java.util.*;

// Ali Raza - 400338538
// Lab 5: Weighted Shortest Path Algorithms on TileMap
// McMaster University - COMP ENG 3SM4

// This is the upgraded TileGraph from Lab 4 with weighted edges and directivity selection

class WeightedEdge {
    
    private Tile myTile;
    private int penalty;

    WeightedEdge(Tile thisTile, int pen)
    {
        myTile = thisTile;
        penalty = pen;
    }

    public Tile getTile() { return myTile; }
    public int getPenalty() { return penalty; }

    public boolean hasTile(Tile thisTile)
    {
        return myTile.equals(thisTile);  // shallow comparison by reference only
    }

    public boolean isEqual(WeightedEdge thisWEdge)
    {
        boolean result = myTile.isEqual(thisWEdge.getTile());
        result &= (penalty == thisWEdge.getPenalty());
        return result;
    }
}

public class TileGraph {
        
    private Map<Tile, LinkedList<WeightedEdge>> adjList;

    private static class TileDistance
    {
        private Tile myTile;
        private int distance;

        TileDistance(Tile thisTile, int dist)
        {
            myTile = thisTile;
            distance = dist;
        }
    }

    public TileGraph()
    {
        adjList = new HashMap<>();
    }

    public TileGraph(TileMap thisMap)
    {
        adjList = new HashMap<>();
        buildGraph(thisMap);
    }

    public void addVertex(Tile thisTile)
    {
        adjList.putIfAbsent(thisTile, new LinkedList<WeightedEdge>());
    }

    // Time: O(deg(src)) - scan src's list once to avoid duplicates
    // Space: O(1)
    // weighted version of addEdge from lab 4
    public void addEdge(Tile src, Tile dst, int penalty)
    {   
        LinkedList<WeightedEdge> srcList = adjList.get(src);
        if(srcList == null) return;

        WeightedEdge newEdge = new WeightedEdge(dst, penalty);
        for(WeightedEdge edge : srcList)
        {
            if(edge.isEqual(newEdge))
                return;
        }

        srcList.add(newEdge);
    }

    // Time: O(V + E) - standard BFS on the graph
    // Space: O(V) - queue, visited set, and parent map
    // returns the path with the smallest number of edges
    private LinkedList<Tile> UnweightedShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> path = new LinkedList<Tile>();  // front end to start
        HashSet<Tile> visited = new HashSet<>();
        HashMap<Tile, Tile> parent = new HashMap<>();
        LinkedList<Tile> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);
        parent.put(start, null);

        boolean found = start.isEqual(end);

        while(!queue.isEmpty() && !found)
        {
            Tile current = queue.poll();
            LinkedList<WeightedEdge> neighbors = adjList.get(current);
            if(neighbors == null) continue;

            // reverse order to keep the same path behaviour as lab 4
            for(int i = neighbors.size() - 1; i >= 0; i--)
            {
                Tile nextTile = neighbors.get(i).getTile();
                if(visited.contains(nextTile)) continue;

                visited.add(nextTile);
                parent.put(nextTile, current);
                queue.add(nextTile);

                if(nextTile.isEqual(end))
                {
                    found = true;
                    break;
                }
            }
        }

        if(!parent.containsKey(end)) return path;

        return buildPathFromParents(end, parent);
    }

    // Time: O((V + E) log V) - PQ operations dominate
    // Space: O(V) - distance map, parent map, and priority queue
    // dijkastra for path with lowest penalties
    private LinkedList<Tile> DijkstraShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        HashMap<Tile, Integer> distance = new HashMap<>();
        HashMap<Tile, Tile> parent = new HashMap<>();
        PriorityQueue<TileDistance> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.distance));

        for(Tile tile : adjList.keySet())
            distance.put(tile, Integer.MAX_VALUE);

        distance.put(start, 0);
        parent.put(start, null);
        pq.add(new TileDistance(start, 0));

        while(!pq.isEmpty())
        {
            TileDistance currentData = pq.poll();
            Tile current = currentData.myTile;

            if(currentData.distance != distance.get(current)) continue;
            if(current.isEqual(end)) break;

            LinkedList<WeightedEdge> neighbors = adjList.get(current);
            if(neighbors == null) continue;

            for(WeightedEdge edge : neighbors)
            {
                Tile nextTile = edge.getTile();
                int newDistance = distance.get(current) + edge.getPenalty();

                if(newDistance < distance.get(nextTile))
                {
                    distance.put(nextTile, newDistance);
                    parent.put(nextTile, current);
                    pq.add(new TileDistance(nextTile, newDistance));
                }
            }
        }

        if(!parent.containsKey(end)) return shortestPath;

        return buildPathFromParents(end, parent);
    }

    // Time: O(VE) - relax every edge up to V-1 times
    // Space: O(V) - distance map and parent map
    // Bellman-Ford Lowest Penalty Path - returns null if negative weight cycles are detected
    private LinkedList<Tile> BellmanShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        HashMap<Tile, Integer> distance = new HashMap<>();
        HashMap<Tile, Tile> parent = new HashMap<>();

        for(Tile tile : adjList.keySet())
            distance.put(tile, Integer.MAX_VALUE);

        distance.put(start, 0);
        parent.put(start, null);

        int vertexCount = adjList.size();

        for(int i = 0; i < vertexCount - 1; i++)
        {
            boolean updated = false;

            for(Tile src : adjList.keySet())
            {
                int srcDist = distance.get(src);
                if(srcDist == Integer.MAX_VALUE) continue;

                for(WeightedEdge edge : adjList.get(src))
                {
                    Tile dst = edge.getTile();
                    int newDistance = srcDist + edge.getPenalty();

                    if(newDistance < distance.get(dst))
                    {
                        distance.put(dst, newDistance);
                        parent.put(dst, src);
                        updated = true;
                    }
                }
            }

            if(!updated) break;
        }

        for(Tile src : adjList.keySet())
        {
            int srcDist = distance.get(src);
            if(srcDist == Integer.MAX_VALUE) continue;

            for(WeightedEdge edge : adjList.get(src))
            {
                Tile dst = edge.getTile();
                if(srcDist + edge.getPenalty() < distance.get(dst))
                {
                    System.out.println("Warning: Negative-weight cycle detected.");
                    return null;
                }
            }
        }

        if(!parent.containsKey(end)) return shortestPath;

        return buildPathFromParents(end, parent);
    }

    // Time: O(V + E) after topological sort
    // Space: O(V) - distance map, parent map, and topo list
    private LinkedList<Tile> DAGShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        LinkedList<Tile> sorted = topologicalSort();
        if(sorted == null) return null;

        HashMap<Tile, Integer> distance = new HashMap<>();
        HashMap<Tile, Tile> parent = new HashMap<>();

        for(Tile tile : adjList.keySet())
            distance.put(tile, Integer.MAX_VALUE);

        distance.put(start, 0);
        parent.put(start, null);

        for(Tile current : sorted)
        {
            int currentDistance = distance.get(current);
            if(currentDistance == Integer.MAX_VALUE) continue;

            for(WeightedEdge edge : adjList.get(current))
            {
                Tile nextTile = edge.getTile();
                int newDistance = currentDistance + edge.getPenalty();

                if(newDistance < distance.get(nextTile))
                {
                    distance.put(nextTile, newDistance);
                    parent.put(nextTile, current);
                }
            }
        }

        if(!parent.containsKey(end)) return shortestPath;

        return buildPathFromParents(end, parent);
    }
    
    // Time: O(V + E) - one indegree pass and one queue pass
    // Space: O(V) - indegree map and zero-indegree queue
    // Kahn's Topological Sorting
    private LinkedList<Tile> topologicalSort()  // helper method for DAG Shortest Path
    {
        LinkedList<Tile> sortedList = new LinkedList<Tile>();
        HashMap<Tile, Integer> indegree = new HashMap<>();
        LinkedList<Tile> queue = new LinkedList<>();

        for(Tile tile : adjList.keySet())
            indegree.put(tile, 0);

        for(Tile tile : adjList.keySet())
        {
            for(WeightedEdge edge : adjList.get(tile))
            {
                Tile dst = edge.getTile();
                indegree.put(dst, indegree.get(dst) + 1);
            }
        }

        for(Tile tile : adjList.keySet())
        {
            if(indegree.get(tile) == 0)
                queue.add(tile);
        }

        while(!queue.isEmpty())
        {
            Tile current = queue.poll();
            sortedList.add(current);

            for(WeightedEdge edge : adjList.get(current))
            {
                Tile dst = edge.getTile();
                indegree.put(dst, indegree.get(dst) - 1);
                if(indegree.get(dst) == 0)
                    queue.add(dst);
            }
        }

        if(sortedList.size() != adjList.size())
            return null;

        return sortedList;
    }

    public LinkedList<Tile> findShortestPath(Tile start, Tile end, int complexity)
    {
        switch(complexity)
        {
            default:
            case 0:
                return UnweightedShortestPath(start, end);  // Lab 4
             
            case 1:
                return DAGShortestPath(start, end);    // DAG
                
            case 2:
                return DijkstraShortestPath(start, end);    // Dijkstra

            case 3:
                return BellmanShortestPath(start, end);    // Bellman
        }
    }

    public void printGraph()
    {
        Set<Tile> keySet = adjList.keySet();
        Collection<LinkedList<WeightedEdge>> valueLists = adjList.values();      

        Iterator<Tile> keySetIter = keySet.iterator();  // so to iterate through map
        Iterator<LinkedList<WeightedEdge>> valueListsIter = valueLists.iterator();  // so to iterate through map
        int size = keySet.size();
        
        for(int i = 0; i < size; i++)
        {
            keySetIter.next().printTileCoord();
            System.out.printf(" >>\t");
            valueListsIter.next().forEach(e -> {e.getTile().printTileCoord(); System.out.printf(" : ");});
            System.out.println();
        }
    }

    // Time: O(V + E + R) - BFS over vertices, plus scan work across horizontal/vertical road segments
    // Space: O(V + E) - graph storage, queue, and visited set
    // build the graph from S outward just like lab 4, but now keep the net edge weight too
    private void buildGraph(TileMap mapRef)
    {
        Tile[][] map = mapRef.getMapRef();
        Tile startTile = map[1][1];

        addVertex(startTile);

        LinkedList<Tile> queue = new LinkedList<>();
        HashSet<Tile> visited = new HashSet<>();
        queue.add(startTile);
        visited.add(startTile);

        while(!queue.isEmpty())
        {
            Tile current = queue.poll();
            int currX = current.getX();
            int currY = current.getY();

            // scan right on the same row
            int penalty = 0;
            for(int x = currX + 1; x < TileMap.BOARDSIZEX - 1; x++)
            {
                char tileType = map[currY][x].getTileType();
                if(tileType == '#') break;

                if(tileType == 'I' || tileType == 'D')
                {
                    Tile nextTile = map[currY][x];
                    addVertex(nextTile);
                    addEdge(current, nextTile, penalty);

                    if(!visited.contains(nextTile))
                    {
                        visited.add(nextTile);
                        queue.add(nextTile);
                    }
                    break;
                }

                penalty += map[currY][x].getScorePenalty();
            }

            // scan down on the same column
            penalty = 0;
            for(int y = currY + 1; y < TileMap.BOARDSIZEY - 1; y++)
            {
                char tileType = map[y][currX].getTileType();
                if(tileType == '#') break;

                if(tileType == 'I' || tileType == 'D')
                {
                    Tile nextTile = map[y][currX];
                    addVertex(nextTile);
                    addEdge(current, nextTile, penalty);

                    if(!visited.contains(nextTile))
                    {
                        visited.add(nextTile);
                        queue.add(nextTile);
                    }
                    break;
                }

                penalty += map[y][currX].getScorePenalty();
            }
        }
    }

    // Time: O(V) - follow the parent chain once
    // Space: O(V) - returned path
    private LinkedList<Tile> buildPathFromParents(Tile end, HashMap<Tile, Tile> parent)
    {
        LinkedList<Tile> path = new LinkedList<Tile>();
        Tile current = end;

        while(current != null)
        {
            path.add(current);
            current = parent.get(current);
        }

        return path;
    }



    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {        
        testAddWeightedEdge1();
        testAddWeightedEdge2();
        testAddWeightedEdgeCustom();

        testTopologicalSort1();
        testTopologicalSort2();
        testTopologicalSortCustom();

        testShortestPathDAG1();
        testShortestPathDAG2();
        testShortestPathDAGCustom();

        testShortestPathDijkastra1();
        testShortestPathDijkastra2();
        testShortestPathDijkastraCustom();

        testShortestPathBellmanFord1();
        testShortestPathBellmanFord2();
        testNegativeCycleBellmanFord1();
        testNegativeCycleBellmanFord2();
        testShortestPathBellmanFordCustom();

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


    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)

    private static void testAddWeightedEdge1()
    {
        // Setup
        System.out.println("============testAddWeightedEdge1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<WeightedEdge> tempList;              
        boolean tempResult;
        
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);                
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
        }        

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);              
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);                      
        passed &= assertEquals(1, tempList.size());
        
        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
        }

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

    private static void testAddWeightedEdge2()
    {
        // Setup
        System.out.println("============testAddWeightedEdge2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<WeightedEdge> tempList;              
        boolean tempResult;
        
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);                
        passed &= assertEquals(3, tempList.size());      
        
        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) || 
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
        }        

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[6]))
                passed &= assertEquals(wEdge[6].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);              
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[7]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[7]))
                passed &= assertEquals(wEdge[7].getPenalty(), testWEdge.getPenalty());
            
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);                      
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[7]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[8]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[8]))
                passed &= assertEquals(wEdge[8].getPenalty(), testWEdge.getPenalty());
            
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[5].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[5]);                      
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[9]))
                passed &= assertEquals(wEdge[9].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[10]))
                passed &= assertEquals(wEdge[10].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[6]);                      
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[7]) ||
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[11]))
                passed &= assertEquals(wEdge[11].getPenalty(), testWEdge.getPenalty());
            
        } 

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
    
    private static void testAddWeightedEdgeCustom()
    {
        // Setup
        System.out.println("============testAddWeightedEdgeCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  {
            new Tile(0, 0, 'I', 0),
            new Tile(2, 0, 'I', 0),
            new Tile(4, 0, 'I', 0),
            new Tile(6, 0, 'I', 0),
            new Tile(2, 3, 'I', 0),
            new Tile(4, 3, 'I', 0),
            new Tile(6, 3, 'I', 0),
            new Tile(8, 3, 'I', 0)
        };

        for(int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1], 3);
        testGraph.addEdge(tileArray[0], tileArray[2], 6);
        testGraph.addEdge(tileArray[0], tileArray[3], 10);
        testGraph.addEdge(tileArray[1], tileArray[4], 2);
        testGraph.addEdge(tileArray[1], tileArray[5], 8);
        testGraph.addEdge(tileArray[2], tileArray[4], 1);
        testGraph.addEdge(tileArray[2], tileArray[5], 7);
        testGraph.addEdge(tileArray[3], tileArray[5], 2);
        testGraph.addEdge(tileArray[4], tileArray[6], 4);
        testGraph.addEdge(tileArray[4], tileArray[7], 9);
        testGraph.addEdge(tileArray[5], tileArray[6], 1);
        testGraph.addEdge(tileArray[6], tileArray[7], 2);

        // exact duplicate - should not be added
        testGraph.addEdge(tileArray[0], tileArray[1], 3);
        // same destination, different weight - should be added
        testGraph.addEdge(tileArray[0], tileArray[1], 9);

        LinkedList<WeightedEdge> tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(4, tempList.size());

        boolean foundPenalty3 = false;
        boolean foundPenalty9 = false;
        int countPenalty3 = 0;

        for(WeightedEdge edge : tempList)
        {
            if(edge.getTile().isEqual(tileArray[1]) && edge.getPenalty() == 3)
            {
                foundPenalty3 = true;
                countPenalty3++;
            }

            if(edge.getTile().isEqual(tileArray[1]) && edge.getPenalty() == 9)
                foundPenalty9 = true;
        }

        passed &= assertEquals(true, foundPenalty3);
        passed &= assertEquals(1, countPenalty3);
        passed &= assertEquals(true, foundPenalty9);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    // Topological Sort
    // Topological Sort
    // Topological Sort

    private static void testTopologicalSort1()
    {
        // Setup
        System.out.println("============testTopologicalSort1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if(sorted != null)
        {
            // Valid solutions:
            // 1: 0,1,2,3,4
            // 2: 0,2,1,3,4
            boolean solution1 = sorted.get(0).equals(tileArray[0]) &&
                            ((sorted.get(1).equals(tileArray[1]) && sorted.get(2).equals(tileArray[2])) ||
                                (sorted.get(1).equals(tileArray[2]) && sorted.get(2).equals(tileArray[1]))) &&
                            sorted.get(3).equals(tileArray[3]) &&
                            sorted.get(4).equals(tileArray[4]);
            
            passed &= solution1;
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testTopologicalSort2()
    {
        // Setup
        System.out.println("============testTopologicalSort2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if(sorted != null)
        {
            // Check if the solution is one of the valid topological sorts
            boolean validSolution = false;
            
            if (sorted.get(0).equals(tileArray[0])) {

                
                int[] indexMap = new int[tileArray.length];
                for (int i = 0; i < sorted.size(); i++) {
                    for (int j = 0; j < tileArray.length; j++) {
                        if (sorted.get(i).equals(tileArray[j])) {
                            indexMap[j] = i;
                            break;
                        }
                    }
                }
                
                // Check all dependencies
                validSolution = true;

                validSolution &= indexMap[1] > indexMap[0];

                validSolution &= indexMap[2] > indexMap[0];

                validSolution &= indexMap[3] > indexMap[0];
                validSolution &= indexMap[3] > indexMap[2];

                validSolution &= indexMap[4] > indexMap[1];
                validSolution &= indexMap[4] > indexMap[2];

                validSolution &= indexMap[5] > indexMap[1];

                validSolution &= indexMap[6] > indexMap[3];
                validSolution &= indexMap[6] > indexMap[5];

                validSolution &= indexMap[7] > indexMap[5];
                validSolution &= indexMap[7] > indexMap[6];
            }
            
            passed &= validSolution;
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testTopologicalSortCustom()
    {
        // Setup
        System.out.println("============testTopologicalSortCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  {
            new Tile(0, 0, 'I', 0),
            new Tile(2, 0, 'I', 0),
            new Tile(4, 0, 'I', 0),
            new Tile(6, 0, 'I', 0),
            new Tile(2, 3, 'I', 0),
            new Tile(4, 3, 'I', 0),
            new Tile(6, 3, 'I', 0),
            new Tile(8, 3, 'I', 0)
        };

        for(int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1], 3);
        testGraph.addEdge(tileArray[0], tileArray[2], 6);
        testGraph.addEdge(tileArray[0], tileArray[3], 10);
        testGraph.addEdge(tileArray[1], tileArray[4], 2);
        testGraph.addEdge(tileArray[1], tileArray[5], 8);
        testGraph.addEdge(tileArray[2], tileArray[4], 1);
        testGraph.addEdge(tileArray[2], tileArray[5], 7);
        testGraph.addEdge(tileArray[3], tileArray[5], 2);
        testGraph.addEdge(tileArray[4], tileArray[6], 4);
        testGraph.addEdge(tileArray[4], tileArray[7], 9);
        testGraph.addEdge(tileArray[5], tileArray[6], 1);
        testGraph.addEdge(tileArray[6], tileArray[7], 2);

        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if(sorted != null)
        {
            int[] indexMap = new int[tileArray.length];
            for(int i = 0; i < sorted.size(); i++)
            {
                for(int j = 0; j < tileArray.length; j++)
                {
                    if(sorted.get(i).equals(tileArray[j]))
                    {
                        indexMap[j] = i;
                        break;
                    }
                }
            }

            passed &= assertEquals(tileArray.length, sorted.size());
            passed &= assertEquals(true, indexMap[1] > indexMap[0]);
            passed &= assertEquals(true, indexMap[2] > indexMap[0]);
            passed &= assertEquals(true, indexMap[3] > indexMap[0]);
            passed &= assertEquals(true, indexMap[4] > indexMap[1]);
            passed &= assertEquals(true, indexMap[4] > indexMap[2]);
            passed &= assertEquals(true, indexMap[5] > indexMap[1]);
            passed &= assertEquals(true, indexMap[5] > indexMap[2]);
            passed &= assertEquals(true, indexMap[5] > indexMap[3]);
            passed &= assertEquals(true, indexMap[6] > indexMap[4]);
            passed &= assertEquals(true, indexMap[6] > indexMap[5]);
            passed &= assertEquals(true, indexMap[7] > indexMap[4]);
            passed &= assertEquals(true, indexMap[7] > indexMap[6]);
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort

    private static void testShortestPathDAG1()
    {
        // Setup
        System.out.println("============testShortestPathDAG1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[2], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDAG2()
    {
        // Setup
        System.out.println("============testShortestPathDAG2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[5], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));        
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDAGCustom()
    {
        // Setup
        System.out.println("============testShortestPathDAGCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  {
            new Tile(0, 0, 'I', 0),
            new Tile(2, 0, 'I', 0),
            new Tile(4, 0, 'I', 0),
            new Tile(6, 0, 'I', 0),
            new Tile(2, 3, 'I', 0),
            new Tile(4, 3, 'I', 0),
            new Tile(6, 3, 'I', 0),
            new Tile(8, 3, 'I', 0)
        };

        for(int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1], 4);
        testGraph.addEdge(tileArray[0], tileArray[2], 2);
        testGraph.addEdge(tileArray[0], tileArray[3], 7);
        testGraph.addEdge(tileArray[1], tileArray[4], 3);
        testGraph.addEdge(tileArray[1], tileArray[5], 6);
        testGraph.addEdge(tileArray[2], tileArray[4], -1);
        testGraph.addEdge(tileArray[2], tileArray[5], 5);
        testGraph.addEdge(tileArray[3], tileArray[5], 1);
        testGraph.addEdge(tileArray[4], tileArray[6], 2);
        testGraph.addEdge(tileArray[4], tileArray[7], 8);
        testGraph.addEdge(tileArray[5], tileArray[6], -2);
        testGraph.addEdge(tileArray[6], tileArray[7], 1);

        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[2], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph

    private static void testShortestPathDijkastra1()
    {
        // Setup
        System.out.println("============testShortestPathDijkastra1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 6), //0
            new WeightedEdge(tileArray[4], 5), //1
            new WeightedEdge(tileArray[3], 1), //1
            new WeightedEdge(tileArray[3], 10), //2
            new WeightedEdge(tileArray[4], 7)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[1], path.get(1));
            passed &= assertEquals(tileArray[0], path.get(2));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDijkastra2()
    {
        // Setup
        System.out.println("============testShortestPathDijkastra2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], 7), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], 2), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[7], 6), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[7]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[3], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));   
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDijkastraCustom()
    {
        // Setup
        System.out.println("============testShortestPathDijkastraCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  {
            new Tile(0, 0, 'I', 0),
            new Tile(2, 0, 'I', 0),
            new Tile(4, 0, 'I', 0),
            new Tile(6, 0, 'I', 0),
            new Tile(2, 3, 'I', 0),
            new Tile(4, 3, 'I', 0),
            new Tile(6, 3, 'I', 0),
            new Tile(8, 3, 'I', 0)
        };

        for(int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1], 3);
        testGraph.addEdge(tileArray[0], tileArray[2], 6);
        testGraph.addEdge(tileArray[0], tileArray[3], 10);
        testGraph.addEdge(tileArray[1], tileArray[4], 2);
        testGraph.addEdge(tileArray[1], tileArray[5], 8);
        testGraph.addEdge(tileArray[2], tileArray[4], 1);
        testGraph.addEdge(tileArray[2], tileArray[5], 7);
        testGraph.addEdge(tileArray[3], tileArray[5], 2);
        testGraph.addEdge(tileArray[4], tileArray[6], 4);
        testGraph.addEdge(tileArray[4], tileArray[7], 9);
        testGraph.addEdge(tileArray[5], tileArray[6], 1);
        testGraph.addEdge(tileArray[6], tileArray[7], 2);

        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph

    private static void testShortestPathBellmanFord1()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 2), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[3], -4), //1
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], -1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[1], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        }
        else
        {
            passed = false;
        }   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathBellmanFord2()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFord2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], -2), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], -4), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[7], -1), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[5], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));   
            passed &= assertEquals(tileArray[0], path.get(4));   
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testNegativeCycleBellmanFord1()
    {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 2), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[3], -4), //1
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], -1),  //3
            new WeightedEdge(tileArray[1], -4)  //4
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[6].getTile(), wEdge[6].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);        
        passed &= assertEquals(true, path == null);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testNegativeCycleBellmanFord2()
    {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord2=============");
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

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], -2), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], -4), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[1], 2), //5  
            new WeightedEdge(tileArray[7], -1), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[11].getTile(), wEdge[11].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[12].getTile(), wEdge[12].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);
        passed &= assertEquals(true, path == null);
        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathBellmanFordCustom()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFordCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  {
            new Tile(0, 0, 'I', 0),
            new Tile(2, 0, 'I', 0),
            new Tile(4, 0, 'I', 0),
            new Tile(6, 0, 'I', 0),
            new Tile(2, 3, 'I', 0),
            new Tile(4, 3, 'I', 0),
            new Tile(6, 3, 'I', 0),
            new Tile(8, 3, 'I', 0)
        };

        for(int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], tileArray[1], 4);
        testGraph.addEdge(tileArray[0], tileArray[2], 2);
        testGraph.addEdge(tileArray[0], tileArray[3], 7);
        testGraph.addEdge(tileArray[1], tileArray[4], 3);
        testGraph.addEdge(tileArray[1], tileArray[5], 6);
        testGraph.addEdge(tileArray[2], tileArray[4], -1);
        testGraph.addEdge(tileArray[2], tileArray[5], 5);
        testGraph.addEdge(tileArray[3], tileArray[5], 1);
        testGraph.addEdge(tileArray[4], tileArray[6], 2);
        testGraph.addEdge(tileArray[4], tileArray[7], 8);
        testGraph.addEdge(tileArray[5], tileArray[6], -2);
        testGraph.addEdge(tileArray[6], tileArray[7], 1);

        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[2], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    


    ////// ASSERTIONS //////
    ////// ASSERTIONS //////
    ////// ASSERTIONS //////

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

    private static boolean assertEquals(int expected, int actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", expected, actual);
            return false;
        }

        return true;
    }
}

import java.util.*;

public class EmergencyAmbulanceOptimization {

    // Travel Cost Matrix (Adjacency Matrix)
    static int[][] costMatrix = {
        {0, 15, 25, 35},
        {15, 0, 30, 28},
        {25, 30, 0, 20},
        {35, 28, 20, 0}
    };

    // Location names
    static String[] locations = {
        "Hospital",
        "Emergency Location B",
        "Emergency Location C",
        "Emergency Location D"
    };

    // ==========================================
    // GREEDY ROUTE OPTIMIZATION (Nearest Neighbour)
    // ==========================================
    public static String greedyEAROP(int[][] dist) {
        int n = dist.length;
        boolean[] visited = new boolean[n];
        StringBuilder path = new StringBuilder();
        int totalCost = 0;
        int current = 0;
        visited[0] = true;
        path.append(locations[0]);

        for (int step = 1; step < n; step++) {
            int next = -1;
            int minEdge = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && dist[current][j] < minEdge) {
                    minEdge = dist[current][j];
                    next = j;
                }
            }
            visited[next] = true;
            totalCost += minEdge;
            path.append(" -> ").append(locations[next]);
            current = next;
        }

        totalCost += dist[current][0];
        path.append(" -> ").append(locations[0]);

        return "Greedy Ambulance Route: " + path + " | Total Cost: " + totalCost;
    }

    // ==========================================
    // DYNAMIC PROGRAMMING (Held-Karp)
    // ==========================================
    public static String dynamicProgrammingEAROP(int[][] dist) {
        int n = dist.length;
        int VISITED_ALL = (1 << n) - 1;
        int[][] memo = new int[n][1 << n];
        String[][] nextCity = new String[n][1 << n];

        for (int[] row : memo) Arrays.fill(row, -1);

        int minCost = dynamicProgrammingEAROPHelper(0, 1, dist, memo, VISITED_ALL, nextCity);

        StringBuilder path = new StringBuilder(locations[0]);
        int pos = 0, mask = 1;
        while (mask != VISITED_ALL) {
            int next = Integer.parseInt(nextCity[pos][mask]);
            path.append(" -> ").append(locations[next]);
            mask |= (1 << next);
            pos = next;
        }
        path.append(" -> ").append(locations[0]);

        return "Dynamic Programming Ambulance Route: " + path + " | Total Cost: " + minCost;
    }

    private static int dynamicProgrammingEAROPHelper(
            int pos, int mask, int[][] dist, int[][] memo, int VISITED_ALL, String[][] nextCity) {

        if (mask == VISITED_ALL) return dist[pos][0];
        if (memo[pos][mask] != -1) return memo[pos][mask];

        int minCost = Integer.MAX_VALUE;
        int bestNext = -1;

        for (int city = 0; city < dist.length; city++) {
            if ((mask & (1 << city)) == 0) {
                int newCost = dist[pos][city]
                        + dynamicProgrammingEAROPHelper(city, mask | (1 << city),
                                dist, memo, VISITED_ALL, nextCity);
                if (newCost < minCost) {
                    minCost = newCost;
                    bestNext = city;
                }
            }
        }
        memo[pos][mask] = minCost;
        nextCity[pos][mask] = String.valueOf(bestNext);
        return minCost;
    }

    // ==========================================
    // BACKTRACKING ROUTE OPTIMIZATION
    // ==========================================
    private static int btBestCost;
    private static List<Integer> btBestPath;

    public static String backtrackingEAROP(int[][] dist) {
        int n = dist.length;
        boolean[] visited = new boolean[n];
        visited[0] = true;
        btBestCost = Integer.MAX_VALUE;
        btBestPath = new ArrayList<>();
        btBestPath.add(0);
        List<Integer> currentPath = new ArrayList<>();
        currentPath.add(0);

        earpBacktracking(0, dist, visited, n, 1, 0, currentPath);

        StringBuilder resultPath = new StringBuilder();
        for (int i = 0; i < btBestPath.size(); i++) {
            if (i > 0) resultPath.append(" -> ");
            resultPath.append(locations[btBestPath.get(i)]);
        }
        resultPath.append(" -> ").append(locations[0]);

        return "Backtracking Ambulance Route: " + resultPath + " | Total Cost: " + btBestCost;
    }

    private static int earpBacktracking(int pos, int[][] dist, boolean[] visited,
                                         int n, int count, int cost, List<Integer> currentPath) {
        if (count == n) {
            int total = cost + dist[pos][0];
            if (total < btBestCost) {
                btBestCost = total;
                btBestPath = new ArrayList<>(currentPath);
            }
            return total;
        }

        int minCost = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                visited[i] = true;
                currentPath.add(i);
                int newCost = earpBacktracking(i, dist, visited, n, count + 1,
                        cost + dist[pos][i], currentPath);
                if (newCost < minCost) minCost = newCost;
                currentPath.remove(currentPath.size() - 1);
                visited[i] = false;
            }
        }
        return minCost;
    }

    // ==========================================
    // DIVIDE AND CONQUER ROUTE OPTIMIZATION
    // ==========================================
    private static int dcBestCost;
    private static List<Integer> dcBestPath;

    public static String divideAndConquerEAROP(int[][] dist) {
        int n = dist.length;
        boolean[] visited = new boolean[n];
        visited[0] = true;
        dcBestCost = Integer.MAX_VALUE;
        dcBestPath = new ArrayList<>();
        dcBestPath.add(0);
        List<Integer> currentPath = new ArrayList<>();
        currentPath.add(0);

        divideAndConquerHelper(0, visited, 0, dist, n, currentPath);

        StringBuilder resultPath = new StringBuilder();
        for (int i = 0; i < dcBestPath.size(); i++) {
            if (i > 0) resultPath.append(" -> ");
            resultPath.append(locations[dcBestPath.get(i)]);
        }
        resultPath.append(" -> ").append(locations[0]);

        return "Divide & Conquer Ambulance Route: " + resultPath + " | Total Cost: " + dcBestCost;
    }

    private static int divideAndConquerHelper(int pos, boolean[] visited,
                                               int currentCost, int[][] dist,
                                               int n, List<Integer> currentPath) {
        if (allVisited(visited)) {
            int total = currentCost + dist[pos][0];
            if (total < dcBestCost) {
                dcBestCost = total;
                dcBestPath = new ArrayList<>(currentPath);
            }
            return total;
        }

        int minCost = Integer.MAX_VALUE;
        int mid = n / 2;

        // First half of remaining cities
        for (int i = 1; i <= mid; i++) {
            if (!visited[i]) {
                visited[i] = true;
                currentPath.add(i);
                int cost = divideAndConquerHelper(i, visited,
                        currentCost + dist[pos][i], dist, n, currentPath);
                if (cost < minCost) minCost = cost;
                currentPath.remove(currentPath.size() - 1);
                visited[i] = false;
            }
        }
        // Second half of remaining cities
        for (int i = mid + 1; i < n; i++) {
            if (!visited[i]) {
                visited[i] = true;
                currentPath.add(i);
                int cost = divideAndConquerHelper(i, visited,
                        currentCost + dist[pos][i], dist, n, currentPath);
                if (cost < minCost) minCost = cost;
                currentPath.remove(currentPath.size() - 1);
                visited[i] = false;
            }
        }
        return minCost;
    }

    private static boolean allVisited(boolean[] visited) {
        for (boolean v : visited) if (!v) return false;
        return true;
    }

    // ==========================================
    // INSERTION SORT
    // ==========================================
    public static String insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
        return Arrays.toString(arr);
    }

    // ==========================================
    // BINARY SEARCH
    // ==========================================
    public static String binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) return String.valueOf(mid);
            else if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return "-1";
    }

    // ==========================================
    // MIN-HEAP
    // ==========================================
    static class MinHeap {
        private PriorityQueue<Integer> heap = new PriorityQueue<>();

        public void insert(int value) { heap.offer(value); }

        public int extractMin() {
            if (heap.isEmpty()) throw new NoSuchElementException("Heap is empty");
            return heap.poll();
        }
    }

    // ==========================================
    // SPLAY TREE
    // ==========================================
    static class SplayTree {
        private class Node {
            int value;
            Node left, right, parent;
            Node(int value) { this.value = value; }
        }

        private Node root;

        public void insert(int value) {
            if (root == null) { root = new Node(value); return; }
            Node current = root, parent = null;
            while (current != null) {
                parent = current;
                if (value < current.value) current = current.left;
                else if (value > current.value) current = current.right;
                else { splay(current); return; }
            }
            Node newNode = new Node(value);
            newNode.parent = parent;
            if (value < parent.value) parent.left = newNode;
            else parent.right = newNode;
            splay(newNode);
        }

        public boolean search(int value) {
            Node current = root, last = null;
            while (current != null) {
                last = current;
                if (value < current.value) current = current.left;
                else if (value > current.value) current = current.right;
                else { splay(current); return true; }
            }
            if (last != null) splay(last);
            return false;
        }

        private void splay(Node node) {
            while (node.parent != null) {
                Node parent = node.parent;
                Node grandparent = parent.parent;
                if (grandparent == null) {
                    if (node == parent.left) rotateRight(parent);
                    else rotateLeft(parent);
                } else if (node == parent.left && parent == grandparent.left) {
                    rotateRight(grandparent); rotateRight(parent);
                } else if (node == parent.right && parent == grandparent.right) {
                    rotateLeft(grandparent); rotateLeft(parent);
                } else if (node == parent.right && parent == grandparent.left) {
                    rotateLeft(parent); rotateRight(grandparent);
                } else {
                    rotateRight(parent); rotateLeft(grandparent);
                }
            }
            root = node;
        }

        private void rotateLeft(Node node) {
            Node right = node.right;
            node.right = right.left;
            if (right.left != null) right.left.parent = node;
            right.parent = node.parent;
            if (node.parent == null) root = right;
            else if (node == node.parent.left) node.parent.left = right;
            else node.parent.right = right;
            right.left = node;
            node.parent = right;
        }

        private void rotateRight(Node node) {
            Node left = node.left;
            node.left = left.right;
            if (left.right != null) left.right.parent = node;
            left.parent = node.parent;
            if (node.parent == null) root = left;
            else if (node == node.parent.right) node.parent.right = left;
            else node.parent.left = left;
            left.right = node;
            node.parent = left;
        }
    }

    // ==========================================
    // DRIVER METHOD
    // ==========================================
    public static void main(String[] args) {
        System.out.println(greedyEAROP(costMatrix));
        System.out.println("");
        System.out.println(dynamicProgrammingEAROP(costMatrix));
        System.out.println("");
        System.out.println(backtrackingEAROP(costMatrix));
        System.out.println("");
        System.out.println(divideAndConquerEAROP(costMatrix));
        System.out.println("");

        int[] arr = {8, 3, 5, 1, 9, 2};
        insertionSort(arr);
        System.out.println("Sorted Emergency Response Times: " + Arrays.toString(arr));
        System.out.println("");
        System.out.println("Binary Search (Response Time 5 found at index): " + binarySearch(arr, 5));
        System.out.println("");

        MinHeap heap = new MinHeap();
        heap.insert(10);
        heap.insert(3);
        heap.insert(15);
        System.out.println("Min-Heap Extract Minimum Priority Value: " + heap.extractMin());
        System.out.println("");

        SplayTree tree = new SplayTree();
        tree.insert(20);
        tree.insert(10);
        tree.insert(30);
        System.out.println("Splay Tree Search (Emergency Case 10 found): " + tree.search(10));
    }
}
package org.example;

class Node {
    int data;
    Node left, right;

    public Node(int value) {
        data = value;
        left = right = null;
    }
}

class BinarySearchTree {
    Node root;

    // Eleman ekleme
    public void insert(int value) {
        root = insertRec(root, value);
    }

    private Node insertRec(Node root, int value) {
        if (root == null) {
            root = new Node(value);
            return root;
        }
        if (value < root.data)
            root.left = insertRec(root.left, value);
        else if (value > root.data)
            root.right = insertRec(root.right, value);
        return root;
    }

    // Eleman arama
    public boolean search(int value) {
        return searchRec(root, value);
    }

    private boolean searchRec(Node root, int value) {
        if (root == null) return false;
        if (root.data == value) return true;
        return value < root.data ? searchRec(root.left, value) : searchRec(root.right, value);
    }

    // Eleman silme
    public void delete(int value) {
        root = deleteRec(root, value);
    }

    private Node deleteRec(Node root, int value) {
        if (root == null) return root;

        if (value < root.data)
            root.left = deleteRec(root.left, value);
        else if (value > root.data)
            root.right = deleteRec(root.right, value);
        else {
            // Tek çocuk veya hiç çocuk yoksa
            if (root.left == null)
                return root.right;
            else if (root.right == null)
                return root.left;

            // İki çocuk varsa: inorder successor ile değiştir
            root.data = minValue(root.right);
            root.right = deleteRec(root.right, root.data);
        }
        return root;
    }

    private int minValue(Node root) {
        int min = root.data;
        while (root.left != null) {
            root = root.left;
            min = root.data;
        }
        return min;
    }

    // Inorder (LNR)
    public void inorder() {
        inorderRec(root);
        System.out.println();
    }

    private void inorderRec(Node root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.data + " ");
            inorderRec(root.right);
        }
    }

    // Preorder (NLR)
    public void preorder() {
        preorderRec(root);
        System.out.println();
    }

    private void preorderRec(Node root) {
        if (root != null) {
            System.out.print(root.data + " ");
            preorderRec(root.left);
            preorderRec(root.right);
        }
    }

    // Postorder (LRN)
    public void postorder() {
        postorderRec(root);
        System.out.println();
    }

    private void postorderRec(Node root) {
        if (root != null) {
            postorderRec(root.left);
            postorderRec(root.right);
            System.out.print(root.data + " ");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        BinarySearchTree tree = new BinarySearchTree();

        int[] values = {8, 1, 3, 6, 7, 10, 14, 4, 9};
        for (int val : values)
            tree.insert(val);

        System.out.print("Inorder: ");
        tree.inorder();

        System.out.print("Preorder: ");
        tree.preorder();

        System.out.print("Postorder: ");
        tree.postorder();

        // Arama
        System.out.println("9 var mı? " + tree.search(9));
        System.out.println("5 var mı? " + tree.search(5));

        // Silme
        tree.delete(10);
        System.out.print("10 silindikten sonra Inorder: ");
        tree.inorder();
    }
}

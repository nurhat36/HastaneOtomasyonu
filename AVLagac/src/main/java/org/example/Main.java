package org.example;

// AVL Ağacı Düğüm Sınıfı
class Node {
    int key, height;
    Node left, right;

    Node(int d) {
        key = d;
        height = 1;
    }
}

public class Main {
    Node root;

    // Düğüm yüksekliğini döndür
    int height(Node N) {
        if (N == null)
            return 0;
        return N.height;
    }

    // Maksimum değeri döndür
    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // Sağ dönüş işlemi
    Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // Döndürme işlemi
        x.right = y;
        y.left = T2;

        // Yükseklikleri güncelle
        y.height = max(height(y.left), height(y.right)) + 1;
        x.height = max(height(x.left), height(x.right)) + 1;

        // Yeni kök
        return x;
    }

    // Sol dönüş işlemi
    Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // Döndürme işlemi
        y.left = x;
        x.right = T2;

        // Yükseklikleri güncelle
        x.height = max(height(x.left), height(x.right)) + 1;
        y.height = max(height(y.left), height(y.right)) + 1;

        // Yeni kök
        return y;
    }

    // Denge faktörü hesapla
    int getBalance(Node N) {
        if (N == null)
            return 0;
        return height(N.left) - height(N.right);
    }

    // AVL ağacına eleman ekle
    Node insert(Node node, int key) {
        // Normal BST ekleme
        if (node == null)
            return (new Node(key));

        if (key < node.key)
            node.left = insert(node.left, key);
        else if (key > node.key)
            node.right = insert(node.right, key);
        else // Aynı anahtarlar kabul edilmez
            return node;

        // Yüksekliği güncelle
        node.height = 1 + max(height(node.left), height(node.right));

        // Denge faktörünü al
        int balance = getBalance(node);

        // 4 Denge Durumu

        // Sol Sol
        if (balance > 1 && key < node.left.key)
            return rightRotate(node);

        // Sağ Sağ
        if (balance < -1 && key > node.right.key)
            return leftRotate(node);

        // Sol Sağ
        if (balance > 1 && key > node.left.key) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Sağ Sol
        if (balance < -1 && key < node.right.key) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        // Düğüm döndürülmeye gerek yok
        return node;
    }

    // Ağacı ön ek sırada yazdır
    void preOrder(Node node) {
        if (node != null) {
            System.out.print(node.key + " ");
            preOrder(node.left);
            preOrder(node.right);
        }
    }

    // Ana program
    public static void main(String[] args) {
        Main tree = new Main();

        tree.root = tree.insert(tree.root, 10);
        tree.root = tree.insert(tree.root, 20);
        tree.root = tree.insert(tree.root, 30);
        tree.root = tree.insert(tree.root, 40);
        tree.root = tree.insert(tree.root, 50);
        tree.root = tree.insert(tree.root, 25);

        System.out.println("Ön ek sıralama (PreOrder): ");
        tree.preOrder(tree.root);
    }
}


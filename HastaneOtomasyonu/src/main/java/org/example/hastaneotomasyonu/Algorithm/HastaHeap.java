package org.example.hastaneotomasyonu.Algorithm;

import org.example.hastaneotomasyonu.models.Hasta;
import java.util.LinkedList;
import java.util.Queue;

public class HastaHeap {
    private Node root;
    private int size;

    // Node sınıfı
    public class Node {
        Hasta data;
        Node left;
        Node right;
        Node parent;

        Node(Hasta data) {
            this.data = data;
        }

        public Hasta getData() {
            return data;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public Node getParent() {
            return parent;
        }
    }

    // Constructor
    public HastaHeap() {
        this.root = null;
        this.size = 0;
    }

    // Root'u döndür
    public Node getRoot() {
        return root;
    }

    // Heap'in boş olup olmadığını kontrol et
    public boolean bosMu() {
        return size == 0;
    }

    // Heap'in boyutunu döndür
    public int boyut() {
        return size;
    }

    // Kök öğeyi (root) döndür
    public Hasta peek() {
        return root != null ? root.data : null;
    }

    // Hasta ekle
    public void ekle(Hasta hasta) {
        Node newNode = new Node(hasta);

        if (root == null) {
            root = newNode;
        } else {
            Queue<Node> queue = new LinkedList<>();
            queue.add(root);

            // En son boş yer bulunur ve hasta eklenir
            while (!queue.isEmpty()) {
                Node current = queue.poll();

                if (current.left == null) {
                    current.left = newNode;
                    newNode.parent = current;
                    break;
                } else if (current.right == null) {
                    current.right = newNode;
                    newNode.parent = current;
                    break;
                } else {
                    queue.add(current.left);
                    queue.add(current.right);
                }
            }

            // Yukarı taşıma (heap property)
            yukariTasima(newNode);
        }

        size++;
    }

    // Kök öğeyi çıkart (max heap olduğu için root çıkar)
    public Hasta cikar() {
        if (root == null) return null;

        Hasta maxValue = root.data;

        if (size == 1) {
            root = null;
            size--;
            return maxValue;
        }

        // En son düğüm alınır ve root ile takas yapılır
        Node lastNode = getLastNode();
        root.data = lastNode.data;

        // Last node silinir
        if (lastNode.parent.right == lastNode) {
            lastNode.parent.right = null;
        } else {
            lastNode.parent.left = null;
        }

        size--;
        // Aşağı taşıma (heap property)
        asagiTasima(root);

        return maxValue;
    }

    // Yukarı taşıma (heap property)
    private void yukariTasima(Node node) {
        while (node.parent != null && node.data.compareTo(node.parent.data) > 0) {
            swap(node, node.parent);
            node = node.parent;
        }
    }

    // Aşağı taşıma (heap property)
    private void asagiTasima(Node node) {
        while (node != null) {
            Node maxChild = null;

            if (node.left != null && node.right != null) {
                maxChild = (node.left.data.compareTo(node.right.data) > 0) ? node.left : node.right;
            } else if (node.left != null) {
                maxChild = node.left;
            } else if (node.right != null) {
                maxChild = node.right;
            }

            if (maxChild != null && maxChild.data.compareTo(node.data) > 0) {
                swap(node, maxChild);
                node = maxChild;
            } else {
                break;
            }
        }
    }

    // İki düğümün verilerini takas yapma
    private void swap(Node a, Node b) {
        Hasta temp = a.data;
        a.data = b.data;
        b.data = temp;
    }

    // En son eklenen düğümü bulma
    private Node getLastNode() {
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        Node last = null;

        while (!queue.isEmpty()) {
            last = queue.poll();
            if (last.left != null) queue.add(last.left);
            if (last.right != null) queue.add(last.right);
        }

        return last;
    }

    // En yüksek öncelikli ikinci hastayı bulma
    public Hasta peekNext() {
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        Hasta first = root != null ? root.data : null;
        Hasta second = null;

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current != root) {
                if (second == null || current.data.getOncelikPuani() > second.getOncelikPuani()) {
                    second = current.data;
                }
            }

            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }

        return second;
    }

    // Tüm hastaları döndür
    public Hasta[] getTumHastalar() {
        Hasta[] hastalar = new Hasta[size];
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        int i = 0;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            hastalar[i++] = current.data;

            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }

        return hastalar;
    }
}

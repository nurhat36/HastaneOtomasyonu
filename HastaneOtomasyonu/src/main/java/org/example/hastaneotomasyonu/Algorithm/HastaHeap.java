package org.example.hastaneotomasyonu.Algorithm;

import org.example.hastaneotomasyonu.Controller.HelloController;
import org.example.hastaneotomasyonu.models.Hasta;

import java.util.*;

public class HastaHeap {
    private Node root;
    private Node lastNode;
    private int size;

    public class Node {
        Hasta data;
        Node left, right, parent;
        Node next; // For linked list structure

        Node(Hasta data) {
            this.data = data;
        }

        public Hasta getData() { return data; }
        public Node getLeft() { return left; }
        public Node getRight() { return right; }
        public Node getParent() { return parent; }
    }

    public HastaHeap() {
        root = null;
        lastNode = null;
        size = 0;
    }

    public Node getRoot() { return root; }
    public boolean bosMu() { return size == 0; }
    public int boyut() { return size; }
    public Hasta peek() { return root != null ? root.data : null; }

    private Node findParentForNewNode() {
        if (root == null) return null;

        // Find parent using level order traversal
        CustomQueue<Node> queue = new CustomQueue<>();
        queue.enqueue(root);

        while (!queue.isEmpty()) {
            Node current = queue.dequeue();

            if (current.left == null || current.right == null) {
                return current;
            }

            queue.enqueue(current.left);
            queue.enqueue(current.right);
        }
        return null;
    }

    private void yukariTasima(Node node) {
        while (node.parent != null && node.data.compareTo(node.parent.data) > 0) {
            swap(node, node.parent);
            node = node.parent;
        }
    }

    public Hasta cikar() {
        if (root == null) return null;

        Hasta maxValue = root.data;

        if (size == 1) {
            root = null;
            lastNode = null;
            size--;
            return maxValue;
        }

        // Move last node's data to root
        root.data = lastNode.data;

        // Remove the last node
        if (lastNode.parent != null) {
            if (lastNode.parent.left == lastNode) {
                lastNode.parent.left = null;
            } else {
                lastNode.parent.right = null;
            }
        }

        // Update lastNode
        updateLastNode();

        size--;
        asagiTasima(root);

        guncelleMuayeneSaatleri();
        return maxValue;
    }

    private void updateLastNode() {
        if (size <= 1) {
            lastNode = root;
            return;
        }

        CustomQueue<Node> queue = new CustomQueue<>();
        queue.enqueue(root);
        Node current = null;

        while (!queue.isEmpty()) {
            current = queue.dequeue();
            if (current.left != null) queue.enqueue(current.left);
            if (current.right != null) queue.enqueue(current.right);
        }

        lastNode = current;
    }

    private void asagiTasima(Node node) {
        while (node != null) {
            Node maxChild = getMaxChild(node);

            if (maxChild != null && maxChild.data.compareTo(node.data) > 0) {
                swap(node, maxChild);
                node = maxChild;
            } else {
                break;
            }
        }
    }

    private Node getMaxChild(Node node) {
        if (node == null) return null;

        Node maxChild = null;
        if (node.left != null) {
            maxChild = node.left;
        }

        if (node.right != null) {
            if (maxChild == null || node.right.data.compareTo(maxChild.data) > 0) {
                maxChild = node.right;
            }
        }

        return maxChild;
    }

    private void swap(Node a, Node b) {
        Hasta temp = a.data;
        a.data = b.data;
        b.data = temp;
    }

    private void guncelleMuayeneSaatleri() {
        if (root == null) return;

        // Get all patients in heap order using level-order traversal
        List<Hasta> heapOrder = new ArrayList<>();
        CustomQueue<Node> queue = new CustomQueue<>();
        if (root != null) queue.enqueue(root);

        while (!queue.isEmpty()) {
            Node current = queue.dequeue();
            heapOrder.add(current.data);
            if (current.left != null) queue.enqueue(current.left);
            if (current.right != null) queue.enqueue(current.right);
        }

        // Sort by priority (highest first) for scheduling
        List<Hasta> sortedPatients = new ArrayList<>(heapOrder);
        sortedPatients.sort((h1, h2) -> Integer.compare(h2.getOncelikPuani(), h1.getOncelikPuani()));

        // Calculate examination times
        double currentTime = HelloController.muayeneBitisSaati;
        if (currentTime == 0) {
            currentTime = 9.00;
        }

        for (Hasta hasta : sortedPatients) {
            double registrationTime = hasta.hastaKayitSaati;

            if (currentTime < registrationTime) {
                currentTime = registrationTime;
            }

            hasta.setMuayeneSaati(currentTime);
            currentTime = saatTopla(currentTime, hasta.muayeneSuresi);
        }
    }

    public static double saatTopla(double saatDouble, int dakikaEkle) {
        int saat = (int) saatDouble;
        int dakika = (int) Math.round((saatDouble - saat) * 100);

        dakika += dakikaEkle;

        saat += dakika / 60;
        dakika = dakika % 60;

        return saat + (dakika / 100.0);
    }

    public void ekle(Hasta hasta) {
        Node newNode = new Node(hasta);
        size++;

        if (root == null) {
            root = newNode;
            lastNode = newNode;
        } else {
            Node parent = findParentForNewNode();
            if (parent.left == null) {
                parent.left = newNode;
            } else {
                parent.right = newNode;
            }
            newNode.parent = parent;
            lastNode = newNode;

            yukariTasima(newNode);
        }

        guncelleMuayeneSaatleri();
    }

    public Hasta peekNext() {
        if (size <= 1) return null;

        if (root.left != null && root.right != null) {
            return root.left.data.compareTo(root.right.data) > 0 ?
                    root.left.data : root.right.data;
        } else if (root.left != null) {
            return root.left.data;
        } else {
            return root.right != null ? root.right.data : null;
        }
    }

    public Hasta[] getTumHastalar() {
        Hasta[] hastalar = new Hasta[size];
        if (root == null) return hastalar;

        // Get all patients in level order
        List<Hasta> allPatients = new ArrayList<>();
        CustomQueue<Node> queue = new CustomQueue<>();
        queue.enqueue(root);

        while (!queue.isEmpty()) {
            Node current = queue.dequeue();
            allPatients.add(current.data);
            if (current.left != null) queue.enqueue(current.left);
            if (current.right != null) queue.enqueue(current.right);
        }

        // Sort by priority (highest first)
        allPatients.sort((h1, h2) -> Integer.compare(h2.getOncelikPuani(), h1.getOncelikPuani()));

        return allPatients.toArray(hastalar);
    }

    public boolean isValid() {
        return isValid(root);
    }

    private boolean isValid(Node node) {
        if (node == null) return true;

        boolean leftValid = true;
        boolean rightValid = true;

        if (node.left != null) {
            if (node.data.compareTo(node.left.data) < 0) return false;
            leftValid = isValid(node.left);
        }

        if (node.right != null) {
            if (node.data.compareTo(node.right.data) < 0) return false;
            rightValid = isValid(node.right);
        }

        return leftValid && rightValid;
    }
}
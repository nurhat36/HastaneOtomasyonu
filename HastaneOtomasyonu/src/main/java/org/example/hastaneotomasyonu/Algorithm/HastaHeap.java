package org.example.hastaneotomasyonu.Algorithm;

import org.example.hastaneotomasyonu.models.Hasta;

import java.util.*;

public class HastaHeap {
    private Node root;
    private int size;

    public class Node {
        Hasta data;
        Node left, right, parent;

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
        size = 0;
    }

    public Node getRoot() { return root; }
    public boolean bosMu() { return size == 0; }
    public int boyut() { return size; }
    public Hasta peek() { return root != null ? root.data : null; }

    private Node findParentForNewNode() {
        if (root == null) return null;

        int temp = size + 1;
        String path = Integer.toBinaryString(temp).substring(1);

        Node current = root;
        for (char c : path.toCharArray()) {
            if (c == '0') {
                if (current.left == null) break;
                current = current.left;
            } else {
                if (current.right == null) break;
                current = current.right;
            }
        }
        return current;
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
            size--;
            return maxValue;
        }

        Node lastNode = getLastNode();

        // Kök ile son düğümün verisini değiştir
        root.data = lastNode.data;

        // Son düğümü kaldır
        if (lastNode.parent != null) {
            if (lastNode.parent.left == lastNode) {
                lastNode.parent.left = null;
            } else {
                lastNode.parent.right = null;
            }
        }

        size--;
        asagiTasima(root);


        return maxValue;
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

    private Node getLastNode() {
        if (root == null) return null;

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

    private void guncelleMuayeneSaatleri() {
        if (root == null) return;

        // Get all patients in the heap (not in order)
        List<Hasta> allPatients = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        if (root != null) queue.add(root);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            allPatients.add(current.data);
            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }

        // Sort by priority (highest first)
        allPatients.sort((h1, h2) -> Integer.compare(h2.getOncelikPuani(), h1.getOncelikPuani()));

        // Calculate examination times
        int currentTime = 9 * 60; // Start at 09:00

        for (Hasta hasta : allPatients) {
            int registrationTime = saatDoubleToDakika(hasta.hastaKayitSaati);

            // Doctor can't see patient before registration time
            if (currentTime < registrationTime) {
                currentTime = registrationTime;
            }

            hasta.setMuayeneSaati(dakikaToSaatDouble(currentTime));
            currentTime += hasta.getMuayeneSuresi();
        }
    }

    public void ekle(Hasta hasta) {
        Node newNode = new Node(hasta);
        size++;

        if (root == null) {
            root = newNode;
        } else {
            Node parent = findParentForNewNode();
            if (parent.left == null) {
                parent.left = newNode;
            } else {
                parent.right = newNode;
            }
            newNode.parent = parent;

            yukariTasima(newNode);
        }

        guncelleMuayeneSaatleri();
    }

    private double dakikaToSaatDouble(int dakika) {
        int saat = dakika / 60;
        int kalanDakika = dakika % 60;
        return saat + (kalanDakika / 100.0);
    }

    private int saatDoubleToDakika(double saatDouble) {
        int saat = (int) saatDouble;
        int dakika = (int) Math.round((saatDouble - saat) * 100);
        return saat * 60 + dakika;
    }

    public Hasta peekNext() {
        if (size <= 1) return null;

        if (root.left != null && root.right != null) {
            return root.left.data.compareTo(root.right.data) > 0 ?
                    root.left.data : root.right.data;
        } else if (root.left != null) {
            return root.left.data;
        } else {
            return root.right.data;
        }
    }

    public Hasta[] getTumHastalar() {
        Hasta[] hastalar = new Hasta[size];
        if (root == null) return hastalar;

        // Get all patients (not in order)
        List<Hasta> allPatients = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            allPatients.add(current.data);
            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
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
package org.example;

public class Main {


        static class TreeNode {
            int val;
            TreeNode left, right;

            TreeNode(int val) {
                this.val = val;
            }
        }

        public static void main(String[] args) {
            // Ağacı elle oluşturuyoruz
            TreeNode root = new TreeNode(8);
            root.left = new TreeNode(4);
            root.right = new TreeNode(12);

            root.left.left = new TreeNode(2);
            root.left.right = new TreeNode(6);
            root.right.left = new TreeNode(10);
            root.right.right = new TreeNode(14);

            root.left.left.left = new TreeNode(1);
            root.left.left.right = new TreeNode(3);
            root.left.right.left = new TreeNode(5);
            root.left.right.right = new TreeNode(7);

            root.right.left.left = new TreeNode(9);
            root.right.left.right = new TreeNode(11);
            root.right.right.left = new TreeNode(13);
            root.right.right.right = new TreeNode(15);


            System.out.println("\n== (B) konumundaki çıktı ==");
            traverseWithB(root);
            

        }

        // A: Sol alt ağaçtan önce yazdır
        static void traverseWithA(TreeNode v) {
            if (v.left != null) {
                System.out.print(v.val + " "); // A
                traverseWithA(v.left);
            }

            if (v.right != null) {
                traverseWithA(v.right);
            }
        }

        // B: Sol alt ağaçtan sonra yazdır
        static void traverseWithB(TreeNode v) {
            if (v.left != null) {
                traverseWithB(v.left);
                System.out.print(v.val + " "); // B
            }

            if (v.right != null) {
                System.out.print(v.val + " ");
                traverseWithB(v.right);

            }
        }

        // C: Sağ alt ağaçtan önce yazdır
        static void traverseWithC(TreeNode v) {
            if (v.left != null) {
                traverseWithC(v.left);
            }

            if (v.right != null) {
                System.out.print(v.val + " "); // C
                traverseWithC(v.right);
            }
        }

        // D: Sağ alt ağaçtan sonra yazdır
        static void traverseWithD(TreeNode v) {
            if (v.left != null) {
                traverseWithD(v.left);
            }

            if (v.right != null) {
                traverseWithD(v.right);
                System.out.print(v.val + " "); // D
            }
        }
    }

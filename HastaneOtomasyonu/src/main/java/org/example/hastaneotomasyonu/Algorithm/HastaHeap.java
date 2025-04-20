package org.example.hastaneotomasyonu.Algorithm;

import org.example.hastaneotomasyonu.models.Hasta;

import java.util.Arrays;

public class HastaHeap {
    public Hasta[] heap;
    private int size;
    private int capacity;

    public HastaHeap(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.heap = new Hasta[capacity];
    }

    public void ekle(Hasta hasta) {
        if (size == capacity) {
            System.out.println("Heap dolu!");
            return;
        }

        heap[size] = hasta;
        yukariTasima(size);
        size++;
    }

    public Hasta cikar() {
        if (size == 0) return null;

        Hasta root = heap[0];
        heap[0] = heap[size - 1];
        size--;
        asagiTasima(0);

        return root;
    }

    private void yukariTasima(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap[index].compareTo(heap[parent]) > 0) {
                swap(index, parent);
                index = parent;
            } else break;
        }
    }

    private void asagiTasima(int index) {
        while (index < size) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && heap[left].compareTo(heap[largest]) > 0) largest = left;
            if (right < size && heap[right].compareTo(heap[largest]) > 0) largest = right;

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else break;
        }
    }

    private void swap(int i, int j) {
        Hasta temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public boolean bosMu() {
        return size == 0;
    }

    public int boyut() {
        return size;
    }

    public Hasta[] getTumHastalar() {
        return Arrays.copyOf(heap, size);
    }
}
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
    public Hasta peekNext() {
        if (size <= 1) {
            return null;
        }
        // root hariç en yüksek öncelikli 2. hastayı bul
        int nextIndex = 1;
        if (size > 2 && heap[2].getOncelikPuani() > heap[1].getOncelikPuani()) {
            nextIndex = 2;
        }
        return heap[nextIndex];
    }


    public void ekle(Hasta hasta) {
        if (size == capacity) {
            System.out.println("Heap dolu!");
            kapasiteyiArtir();
        }

        heap[size] = hasta;
        yukariTasima(size);
        size++;
    }
    private void kapasiteyiArtir() {
        capacity = capacity * 2;
        heap = Arrays.copyOf(heap, capacity);
    }


    public Hasta cikar() {
        if (size == 0) return null;

        Hasta root = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;  // önemli
        size--;
        asagiTasima(0);

        return root;
    }
    public Hasta peek() {
        return size == 0 ? null : heap[0];
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
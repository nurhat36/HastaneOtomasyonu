package org.example.hastaneotomasyonu.Services;

import org.example.hastaneotomasyonu.Algorithm.HastaHeap;
import org.example.hastaneotomasyonu.Controller.HelloController;

public class GlobalHeapService {
    private static HastaHeap heap = HelloController.HastaHeap;

    public static HastaHeap getHeap() {
        return heap;
    }

    public static void setHeap(HastaHeap newHeap) {
        heap = newHeap;
    }
}
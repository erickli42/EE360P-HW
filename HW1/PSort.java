//UT-EID=eyl283, rch2777


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{
    final int[] A;
    final int begin;
    final int end;

    PSort(int[] A, int begin, int end) {
        this.A = A;
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (begin < end) {
            if (end - begin <= 16) {
                insertionSort();
            }
            else {
                // partition array
                int pIdx = partition();

                PSort p1 = new PSort(A, begin, pIdx);
                p1.fork();
                PSort p2 = new PSort(A, pIdx + 1, end);
                p2.compute();
                p1.join();
            }
        }
    }

    public int partition() {
        // Use last element as pivot (index end is exclusive)
        int pVal = A[end-1];
        int pIdx = begin-1;

        for (int i = begin; i < end-1; i++) {
            if (A[i] < pVal) {
                pIdx++;
                int temp = A[i];
                A[i] = A[pIdx];
                A[pIdx] = temp;
            }
        }

        pIdx++;
        A[end-1] = A[pIdx];
        A[pIdx] = pVal;
        return pIdx;
    }

    public void insertionSort() {
        for (int i = begin+1; i < end; i++) {
            int val = A[i];
            int j = i-1;

            while (j>=0 && val < A[j]) {
                A[j+1] = A[j];
                j--;
            }
            A[j+1]=val;
        }
    }

    public static void parallelSort(int[] A, int begin, int end){
        int processors = Runtime.getRuntime().availableProcessors();
        PSort p = new PSort(A, begin, end);
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(p);
    }
}
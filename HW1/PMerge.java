//UT-EID=eyl283, rch2777


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Runnable{
    int[] A;
    int[] B;
    int[] C;
    int idx;
    int dupePrio;

    PMerge(int[] A, int[] B, int[] C, int idx, int dupePrio) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.idx = idx;
        this.dupePrio = dupePrio;
    }

    @Override
    public void run() {
        if (B.length > 0) {
            int idx2 = binarySearch(B, A[idx]);
            int aReverse = A.length - 1 - idx;
            int bReverse = B.length - idx2;
            C[aReverse + bReverse] = A[idx];
        } else {
            int aReverse = A.length - 1 - idx;
            C[aReverse] = A[idx];
        }
    }

    public int binarySearch(int[] arr, int x) {
        int begin = 0;
        int end = arr.length-1;
        while (begin < end) {
            int mid = (begin+end) / 2;
            if (arr[mid] < x) {
                begin = mid + 1;
            } else if (arr[mid] > x) {
                end = mid - 1;
            } else {
                return mid + dupePrio;
            }
        }

        if (arr[begin] < x) {
            return begin+1;
        } else if (arr[begin] > x) {
            return begin;
        } else {
            return begin + dupePrio;
        }
    }

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < A.length; i++) {
            PMerge p = new PMerge(A, B, C, i, 0);
            executor.execute(p);
        }

        for (int i = 0; i < B.length; i++) {
            PMerge p = new PMerge(B, A, C, i, 1);
            executor.execute(p);
        }

        executor.shutdown();
        try {
            boolean success = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
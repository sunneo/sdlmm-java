package sunneo.sdlmm.exams;

import java.util.Scanner;

public class HanoiTowerGame {
    public static boolean checkHanoiEnd(int[] c) {
        int len = c.length;
        int maxValue = len;
        int[] cloneC = new int[len];
        for (int i = 0; i < c.length; ++i) {
            cloneC[i] = maxValue;
            --maxValue;
        }
        for (int i = 0; i < c.length; ++i) {
            if (c[i] != cloneC[i]) {
                return false;
            }
        }
        return true;
    }

    public static void printOneHanoi(int[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.printf("%d ", a[i]);
        }
        System.out.println();
    }

    public static void printHanoiOneByOne(int[]... arrays) {
        for (int i = 0; i < arrays.length; ++i) {
            int[] array = arrays[i];
            printOneHanoi(array);
        }
    }

    public static void printHanoiTower(int[] a, int[] b, int[] c) {
        printHanoiOneByOne(a, b, c);
    }

    public static int[] selectTower(int selection, int[] a, int[] b, int[] c) {
        switch (selection) {
            case 0:
                return a;
            case 1:
                return b;
            case 2:
                return c;
        }
        return null;
    }

    public static int getTopIndex(int[] tower) {
        for (int i = tower.length - 1; i >= 0; --i) {
            if (tower[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    public static void moveHanoi(int from, int to, int[] a, int[] b, int[] c) {
        int[] fromArray = selectTower(from, a, b, c);
        int[] toArray = selectTower(to, a, b, c);
        int fromTopIdx = getTopIndex(fromArray);
        if (fromTopIdx == -1) {
            return;
        }
        int topValue = fromArray[fromTopIdx];
        int toTopIdx = getTopIndex(toArray);
        if (toTopIdx != -1) {
            if (topValue > toArray[toTopIdx]) {
                return;
            }
            fromArray[fromTopIdx] = 0;
            toArray[toTopIdx + 1] = topValue;
        }
        else {
            fromArray[fromTopIdx] = 0;
            toArray[toTopIdx + 1] = topValue;
        }
    }

    public static void main(String[] argv) {
        int[] a = new int[] { 5, 4, 3, 2, 1 };
        int[] b = new int[] { 0, 0, 0, 0, 0 };
        int[] c = new int[] { 0, 0, 0, 0, 0 };
        Scanner sc = new Scanner(System.in);
        System.out.println("�e����");
        while (!checkHanoiEnd(c)) {
            printHanoiTower(a, b, c);
            System.out.println("�п�J�n�h�����W�l[0-2]");
            int n1 = sc.nextInt();
            if (n1 < 0 || n1 > 2) {
                System.out.println("��J��~");
                continue;
            }
            System.out.println("�п�J�h����h[0-2]");
            int n2 = sc.nextInt();
            if (n2 < 0 || n2 > 2) {
                System.out.println("��J��~");
                continue;
            }
            moveHanoi(n1, n2, a, b, c);
        }
    }
}

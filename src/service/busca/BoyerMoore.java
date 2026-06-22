package service.busca;

public class BoyerMoore {

    private static final int ALFABETO = 256;

    private int[] badChar(String pattern) {

        int[] bad = new int[ALFABETO];

        for (int i = 0; i < ALFABETO; i++) {
            bad[i] = -1;
        }

        for (int i = 0; i < pattern.length(); i++) {
            bad[pattern.charAt(i)] = i;
        }

        return bad;
    }

    public boolean contains(String text, String pattern) {

        if (text == null || pattern == null) return false;

        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        if (pattern.length() > text.length()) return false;

        int[] bad = badChar(pattern);

        int shift = 0;

        while (shift <= text.length() - pattern.length()) {

            int j = pattern.length() - 1;

            while (j >= 0 &&
                   pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) return true;

            char c = text.charAt(shift + j);

            int last = (c < 256) ? bad[c] : -1;

            shift += Math.max(1, j - last);
        }

        return false;
    }
}

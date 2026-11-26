package com.liftcode.liftcode_server.common.util;

import java.math.BigInteger;

/**
 * Base-62 fractions in (0, 1), stored without their common radix point.
 */
public final class FractionalIndex {

    private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // Two base digits are always retained: a0 < a0V < a1.
    private static final int MIN_WIDTH = 2;
    private static final BigInteger BASE = BigInteger.valueOf(DIGITS.length());

    private FractionalIndex() {
    }

    /**
     * Null bounds denote the beginning or end of the list.
     */
    public static String between(String left, String right) {
        if (left != null && right != null && left.compareTo(right) >= 0) {
            throw new IllegalArgumentException("Expected ordered, distinct bounds");
        }
        int width = Math.max(MIN_WIDTH, Math.max(left == null ? 0 : left.length(), right == null ? 0 : right.length()));
        BigInteger low = left == null ? BigInteger.ZERO : scaledValue(left, width);
        BigInteger high = right == null ? BASE.pow(width) : scaledValue(right, width);
        if (high.subtract(low).equals(BigInteger.ONE)) {
            low = low.multiply(BASE);
            high = high.multiply(BASE);
            width++;
        }
        BigInteger middle = low.add(high).shiftRight(1);
        char[] digits = new char[width];
        for (int i = width - 1; i >= 0; i--) {
            BigInteger[] parts = middle.divideAndRemainder(BASE);
            digits[i] = DIGITS.charAt(parts[1].intValueExact());
            middle = parts[0];
        }
        while (width > MIN_WIDTH && digits[width - 1] == '0') width--;
        return new String(digits, 0, width);
    }

    private static BigInteger scaledValue(String key, int width) {
        if (key.length() < MIN_WIDTH || key.equals("00") || (key.length() > MIN_WIDTH && key.endsWith("0"))) {
            throw new IllegalArgumentException("Expected a canonical fractional index");
        }
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < key.length(); i++) {
            int digit = DIGITS.indexOf(key.charAt(i));
            if (digit < 0) throw new IllegalArgumentException("Invalid fractional index digit");
            value = value.multiply(BASE).add(BigInteger.valueOf(digit));
        }
        return value.multiply(BASE.pow(width - key.length()));
    }
}

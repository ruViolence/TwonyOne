package ru.violence.twonyone.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.Random;

@UtilityClass
public class Utils {
    public final Random RANDOM = new SecureRandom();

    public int calculateAmountWithFee(int amount, double feePercentage) {
        int feeAmount = (int) (amount * feePercentage / 100.0); // Calculate fee amount
        return amount - feeAmount; // Total amount with fee subtracted
    }
}

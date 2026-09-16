package budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Calculates currency targets while keeping their sum equal to income. */
public final class FiftyThirtyTwentyMethod implements BudgetMethod {
    @Override
    public BudgetTargets calculate(BigDecimal monthlyIncome) {
        Objects.requireNonNull(monthlyIncome, "monthlyIncome");
        if (monthlyIncome.signum() < 0) {
            throw new IllegalArgumentException("Monthly income cannot be negative.");
        }
        final BigDecimal income;
        try {
            income = monthlyIncome.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Enter income in whole cents.", ex);
        }
        BigDecimal needs = income.multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal wants = income.multiply(new BigDecimal("0.30"))
                .setScale(2, RoundingMode.HALF_UP);
        // Assign the remaining cents to goals to avoid creating or losing money.
        BigDecimal goals = income.subtract(needs).subtract(wants);
        return new BudgetTargets(needs, wants, goals);
    }
}

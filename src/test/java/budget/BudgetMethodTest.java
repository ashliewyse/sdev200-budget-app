package budget;

import java.math.BigDecimal;

/** Run without a separate test framework: java -cp build budget.BudgetMethodTest. */
public final class BudgetMethodTest {
    private BudgetMethodTest() { }

    private static void equal(String expected, BigDecimal actual) {
        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("Expected " + expected + "; received " + actual);
        }
    }

    public static void main(String[] args) {
        BudgetMethod method = new FiftyThirtyTwentyMethod();
        BudgetTargets regular = method.calculate(new BigDecimal("3000.00"));
        equal("1500.00", regular.get(BudgetCategory.NEEDS));
        equal("900.00", regular.get(BudgetCategory.WANTS));
        equal("600.00", regular.get(BudgetCategory.GOALS));
        for (String income : new String[] {"0.00", "0.01", "0.02", "0.03", "1000.01", "4250.50"}) {
            BudgetTargets targets = method.calculate(new BigDecimal(income));
            equal(income, targets.total());
            for (BudgetCategory category : BudgetCategory.values()) {
                if (targets.get(category).signum() < 0 || targets.get(category).scale() != 2) {
                    throw new AssertionError("Invalid currency target for " + income);
                }
            }
        }
        for (String invalid : new String[] {"-1.00", "12.345"}) {
            try {
                method.calculate(new BigDecimal(invalid));
                throw new AssertionError("Accepted invalid income: " + invalid);
            } catch (IllegalArgumentException expected) {
                // The calculator must reject negative amounts and fractional cents.
            }
        }
        System.out.println("PASS: Known allocations, cents, zero income, and invalid input.");
    }
}

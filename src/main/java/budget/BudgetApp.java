package budget;

import java.math.BigDecimal;

/** Module 3 demonstration; the JSP interface will be added later. */
public final class BudgetApp {
    private BudgetApp() { }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java -cp build budget.BudgetApp [monthlyIncome]");
            System.exit(1);
        }
        try {
            BigDecimal income = new BigDecimal(args.length == 0 ? "3000.00" : args[0]);
            BudgetMethod method = new FiftyThirtyTwentyMethod();
            BudgetTargets targets = method.calculate(income);
            System.out.println("50/30/20 BUDGET TARGETS");
            System.out.println("Monthly take-home income: $" + income.setScale(2).toPlainString());
            for (BudgetCategory category : BudgetCategory.values()) {
                System.out.println(category + ": $" + targets.get(category).toPlainString());
            }
            System.out.println("Total allocated: $" + targets.total().toPlainString());
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid income: " + ex.getMessage());
            System.exit(1);
        }
    }
}

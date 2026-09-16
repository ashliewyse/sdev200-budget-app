package budget;

import java.math.BigDecimal;

/** A budgeting method that divides monthly take-home income into targets. */
public interface BudgetMethod {
    BudgetTargets calculate(BigDecimal monthlyIncome);
}

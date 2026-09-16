package budget;

import java.math.BigDecimal;
import java.util.Objects;

/** Immutable result of the budget calculation. */
public final class BudgetTargets {
    private final BigDecimal needs;
    private final BigDecimal wants;
    private final BigDecimal goals;

    public BudgetTargets(BigDecimal needs, BigDecimal wants, BigDecimal goals) {
        this.needs = Objects.requireNonNull(needs, "needs");
        this.wants = Objects.requireNonNull(wants, "wants");
        this.goals = Objects.requireNonNull(goals, "goals");
        if (needs.signum() < 0 || wants.signum() < 0 || goals.signum() < 0) {
            throw new IllegalArgumentException("Targets cannot be negative.");
        }
    }

    public BigDecimal get(BudgetCategory category) {
        Objects.requireNonNull(category, "category");
        switch (category) {
            case NEEDS: return needs;
            case WANTS: return wants;
            case GOALS: return goals;
            default: throw new IllegalArgumentException("Unknown category.");
        }
    }

    public BigDecimal total() {
        return needs.add(wants).add(goals);
    }
}

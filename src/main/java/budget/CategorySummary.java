package budget;
import java.math.BigDecimal;
/** Read-only category figures for the JSP dashboard. */
public final class CategorySummary {
    private final BudgetCategory category;
    private final BigDecimal target, actual, remaining;
    private final int progressPercent;
    public CategorySummary(BudgetCategory category, BigDecimal target, BigDecimal actual, BigDecimal remaining, int progressPercent) {
        this.category = category; this.target = target; this.actual = actual; this.remaining = remaining; this.progressPercent = progressPercent;
    }
    public BudgetCategory getCategory() { return category; }
    public BigDecimal getTarget() { return target; }
    public BigDecimal getActual() { return actual; }
    public BigDecimal getRemaining() { return remaining; }
    public int getProgressPercent() { return progressPercent; }
}

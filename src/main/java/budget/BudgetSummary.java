package budget;
import java.math.BigDecimal;
import java.util.List;
/** Dashboard model assembled from saved state and the chosen budget method. */
public final class BudgetSummary {
    private final UserProfile profile;
    private final MonthlyBudget budget;
    private final List<CategorySummary> categories;
    private final BigDecimal transactionTotal, unassigned;
    public BudgetSummary(UserProfile profile, MonthlyBudget budget, List<CategorySummary> categories, BigDecimal transactionTotal, BigDecimal unassigned) {
        this.profile=profile; this.budget=budget; this.categories=List.copyOf(categories); this.transactionTotal=transactionTotal; this.unassigned=unassigned;
    }
    public UserProfile getProfile() { return profile; }
    public MonthlyBudget getBudget() { return budget; }
    public List<CategorySummary> getCategories() { return categories; }
    public BigDecimal getTransactionTotal() { return transactionTotal; }
    public BigDecimal getUnassigned() { return unassigned; }
}

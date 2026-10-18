package budget;
import java.math.BigDecimal;
import java.time.LocalDate;
/** One expense or contribution toward goals. */
public final class Transaction {
    private final String id;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final BudgetCategory category;
    public Transaction(String id, LocalDate date, String description, BigDecimal amount, BudgetCategory category) {
        this.id = Identifiers.require(id);
        if (date == null || date.getYear() < 1 || date.getYear() > 9999) throw new IllegalArgumentException("Enter a valid date.");
        if (description == null || description.trim().isEmpty() || description.trim().length() > 120) {
            throw new IllegalArgumentException("Description must contain 1 to 120 characters.");
        }
        if (category == null) throw new IllegalArgumentException("Choose a category.");
        this.date = date;
        this.description = description.trim();
        this.amount = Money.amount(amount, true);
        this.category = category;
    }
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public BudgetCategory getCategory() { return category; }
}

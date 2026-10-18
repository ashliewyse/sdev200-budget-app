package budget;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
/** Immutable monthly state, owned by exactly one profile. */
public final class MonthlyBudget {
    private final String profileId;
    private final YearMonth month;
    private final BigDecimal income;
    private final List<Transaction> transactions;
    public MonthlyBudget(String profileId, YearMonth month, BigDecimal income, List<Transaction> transactions) {
        this.profileId = Identifiers.require(profileId);
        if (month == null || month.getYear() < 1 || month.getYear() > 9999) throw new IllegalArgumentException("Choose a valid month.");
        this.month = month;
        this.income = Money.amount(income, false);
        if (transactions == null || transactions.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("Entries cannot be empty.");
        Set<String> ids = new HashSet<>();
        for (Transaction entry : transactions) {
            if (!YearMonth.from(entry.getDate()).equals(month)) throw new IllegalArgumentException("Entry date must be within the selected month.");
            if (!ids.add(entry.getId())) throw new IllegalArgumentException("Entry identifiers must be unique.");
        }
        this.transactions = List.copyOf(transactions);
    }
    public String getProfileId() { return profileId; }
    public YearMonth getMonth() { return month; }
    public BigDecimal getIncome() { return income; }
    public List<Transaction> getTransactions() { return transactions; }
}

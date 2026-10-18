package budget;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BudgetDomainTest {
    private final String profileId = UUID.randomUUID().toString();
    @Test void moneyUsesWholeCentsAndLimitsInput() {
        assertEquals(new BigDecimal("12.30"), Money.parse("12.30", true));
        assertEquals(new BigDecimal("0.00"), Money.parse("0", false));
        for (String bad : new String[]{"", "-1", "+1", "1e9", "12.345", "1000000000", "NaN"}) {
            assertThrows(IllegalArgumentException.class, () -> Money.parse(bad, false), bad);
        }
        assertThrows(IllegalArgumentException.class, () -> Money.parse("0", true));
    }
    @Test void transactionsValidateAndPreserveUnicode() {
        Transaction entry = new Transaction(UUID.randomUUID().toString(), LocalDate.of(2026,10,17), "  Café  ", new BigDecimal("3.5"), BudgetCategory.WANTS);
        assertEquals("Café", entry.getDescription());
        assertEquals(new BigDecimal("3.50"), entry.getAmount());
        assertThrows(IllegalArgumentException.class, () -> new Transaction("../x", LocalDate.now(), "Rent", BigDecimal.ONE, BudgetCategory.NEEDS));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(UUID.randomUUID().toString(), LocalDate.now(), " ", BigDecimal.ONE, BudgetCategory.NEEDS));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(UUID.randomUUID().toString(), LocalDate.now(), "x".repeat(121), BigDecimal.ONE, BudgetCategory.NEEDS));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(UUID.randomUUID().toString(), null, "Rent", BigDecimal.ONE, BudgetCategory.NEEDS));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(UUID.randomUUID().toString(), LocalDate.now(), "Rent", BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(UUID.randomUUID().toString(), LocalDate.now(), "Rent", BigDecimal.ZERO, BudgetCategory.NEEDS));
    }
    @Test void monthlyBudgetProtectsItsEntriesAndMonth() {
        List<Transaction> input = new ArrayList<>();
        MonthlyBudget budget = new MonthlyBudget(profileId, YearMonth.of(2026,10), BigDecimal.ZERO, input);
        input.add(new Transaction(UUID.randomUUID().toString(), LocalDate.of(2026,10,1), "Rent", BigDecimal.ONE, BudgetCategory.NEEDS));
        assertTrue(budget.getTransactions().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> budget.getTransactions().add(input.get(0)));
        assertThrows(IllegalArgumentException.class, () -> new MonthlyBudget(profileId, YearMonth.of(2026,11), BigDecimal.ZERO, input));
        assertThrows(IllegalArgumentException.class, () -> new MonthlyBudget(profileId, YearMonth.of(2026,10), new BigDecimal("-1"), List.of()));
    }
    @Test void profileNamesAreTrimmedAndBounded() {
        assertEquals("Zoë", new UserProfile(profileId, " Zoë ").getDisplayName());
        assertThrows(IllegalArgumentException.class, () -> new UserProfile("../bad", "Demo"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(profileId, ""));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(profileId, "x".repeat(41)));
    }
}

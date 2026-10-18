package budget;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FiftyThirtyTwentyMethodTest {
    private final BudgetMethod method = new FiftyThirtyTwentyMethod();
    @Test void knownAllocations() {
        BudgetTargets targets = method.calculate(new BigDecimal("4250.50"));
        assertEquals(new BigDecimal("2125.25"), targets.get(BudgetCategory.NEEDS));
        assertEquals(new BigDecimal("1275.15"), targets.get(BudgetCategory.WANTS));
        assertEquals(new BigDecimal("850.10"), targets.get(BudgetCategory.GOALS));
        assertEquals(new BigDecimal("1500.00"), method.calculate(new BigDecimal("3000")).get(BudgetCategory.NEEDS));
    }
    @Test void tinyAmountsAndZeroConserveCents() {
        for (String value : new String[]{"0", "0.01", "0.02", "0.03", "1000.01"}) {
            BudgetTargets targets = method.calculate(new BigDecimal(value));
            assertEquals(new BigDecimal(value).setScale(2), targets.total());
            for (BudgetCategory category : BudgetCategory.values()) {
                assertTrue(targets.get(category).signum() >= 0);
                assertEquals(2, targets.get(category).scale());
            }
        }
    }
    @Test void invalidIncomeRejected() {
        assertThrows(IllegalArgumentException.class, () -> method.calculate(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> method.calculate(new BigDecimal("12.345")));
    }
}

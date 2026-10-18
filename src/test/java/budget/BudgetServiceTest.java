package budget;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class BudgetServiceTest {
    @TempDir Path root;
    BudgetService service; UserProfile a,b;
    final YearMonth october=YearMonth.of(2026,10), november=YearMonth.of(2026,11);
    @BeforeEach void setup() {
        service=new BudgetService(new FileBudgetRepository(root,new ObjectMapper().findAndRegisterModules()),new FiftyThirtyTwentyMethod());
        a=service.createProfile("Demo Ashlie"); b=service.createProfile("Demo Morgan");
    }
    @Test void totalsOverspendingAndGoalsUseSavedEntries() {
        service.saveIncome(a.getId(),october,"3000");
        service.addTransaction(a.getId(),october,"2026-10-01","Rent","1550","NEEDS");
        service.addTransaction(a.getId(),october,"2026-10-02","Fun","100","WANTS");
        service.addTransaction(a.getId(),october,"2026-10-03","Savings","200","GOALS");
        var result=service.summary(a.getId(),october);
        assertEquals("-50.00",result.getCategories().get(0).getRemaining().toPlainString());
        assertEquals("800.00",result.getCategories().get(1).getRemaining().toPlainString());
        assertEquals("400.00",result.getCategories().get(2).getRemaining().toPlainString());
        assertEquals(100,result.getCategories().get(0).getProgressPercent());
        assertEquals(33,result.getCategories().get(2).getProgressPercent());
        assertEquals("1150.00",result.getUnassigned().toPlainString());
        assertEquals("0.00",service.summary(b.getId(),october).getBudget().getIncome().toPlainString());
        assertTrue(service.summary(a.getId(),november).getBudget().getTransactions().isEmpty());
    }
    @Test void deletionOnlyAffectsSelectedProfileAndMonth() {
        service.addTransaction(a.getId(),october,"2026-10-17","Café","3.50","WANTS");
        String id=service.summary(a.getId(),october).getBudget().getTransactions().get(0).getId();
        assertThrows(IllegalArgumentException.class, () -> service.deleteTransaction(b.getId(),october,id));
        assertThrows(IllegalArgumentException.class, () -> service.deleteTransaction(a.getId(),november,id));
        assertEquals(1,service.summary(a.getId(),october).getBudget().getTransactions().size());
        service.deleteTransaction(a.getId(),october,id);
        assertTrue(service.summary(a.getId(),october).getBudget().getTransactions().isEmpty());
    }
    @Test void invalidEntriesDoNotChangeSavedState() {
        for (String bad:List.of("0","-1","12.345","1000000000","1e8")) {
            assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"2026-10-17","Entry",bad,"NEEDS"));
        }
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"2026-11-01","Entry","1","NEEDS"));
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"not-a-date","Entry","1","NEEDS"));
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"2026-10-17","","1","NEEDS"));
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"2026-10-17","x".repeat(121),"1","NEEDS"));
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(a.getId(),october,"2026-10-17","Entry","1","INVALID"));
        assertTrue(service.summary(a.getId(),october).getBudget().getTransactions().isEmpty());
    }
    @Test void zeroTargetsDoNotDivideByZero() {
        service.addTransaction(a.getId(),october,"2026-10-17","Savings","5","GOALS");
        assertEquals(100,service.summary(a.getId(),october).getCategories().get(2).getProgressPercent());
    }
    @Test void concurrentEntriesAreNotLost() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            List<Callable<Void>> actions=List.of(
                ()->{service.addTransaction(a.getId(),october,"2026-10-01","First","10","NEEDS");return null;},
                ()->{service.addTransaction(a.getId(),october,"2026-10-02","Second","20","NEEDS");return null;});
            for (Future<Void> result:pool.invokeAll(actions)) result.get(5,TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
        assertEquals(2,service.summary(a.getId(),october).getBudget().getTransactions().size());
        assertEquals("30.00",service.summary(a.getId(),october).getTransactionTotal().toPlainString());
    }
}

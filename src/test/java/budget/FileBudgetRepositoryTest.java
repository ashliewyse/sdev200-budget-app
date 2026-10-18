package budget;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class FileBudgetRepositoryTest {
    @TempDir Path root;
    private FileBudgetRepository repository() { return new FileBudgetRepository(root, new ObjectMapper().findAndRegisterModules()); }
    private Transaction rent() { return new Transaction(UUID.randomUUID().toString(),LocalDate.of(2026,10,1),"Rent",new BigDecimal("1000"),BudgetCategory.NEEDS); }
    @Test void profilesAndMonthsReloadIndependently() {
        var first = repository();
        var a = first.createProfile(" Demo Ashlie ");
        var b = first.createProfile("Demo Morgan");
        assertEquals("Demo Ashlie", a.getDisplayName());
        first.saveBudget(new MonthlyBudget(a.getId(),YearMonth.of(2026,10),new BigDecimal("3000"),List.of(rent())));
        first.saveBudget(new MonthlyBudget(a.getId(),YearMonth.of(2026,11),new BigDecimal("4000"),List.of()));
        first.saveBudget(new MonthlyBudget(b.getId(),YearMonth.of(2026,10),new BigDecimal("5000"),List.of()));
        var second = repository();
        assertEquals(2, second.listProfiles().size());
        assertEquals("1000.00", second.loadBudget(a.getId(),YearMonth.of(2026,10)).getTransactions().get(0).getAmount().toPlainString());
        assertEquals("4000.00", second.loadBudget(a.getId(),YearMonth.of(2026,11)).getIncome().toPlainString());
        assertEquals("5000.00", second.loadBudget(b.getId(),YearMonth.of(2026,10)).getIncome().toPlainString());
        assertEquals("0.00", second.loadBudget(b.getId(),YearMonth.of(2026,12)).getIncome().toPlainString());
    }
    @Test void invalidAndUnknownProfilesCannotBecomePaths() {
        var repo=repository();
        repo.createProfile("Demo");
        assertThrows(IllegalArgumentException.class, () -> repo.findProfile("../outside"));
        assertThrows(IllegalArgumentException.class, () -> repo.loadBudget(UUID.randomUUID().toString(),YearMonth.of(2026,10)));
        assertThrows(IllegalArgumentException.class, () -> repo.createProfile(" demo "));
    }
    @Test void malformedBudgetsAreNotReplacedWithEmptyData() throws Exception {
        var repo=repository(); var a=repo.createProfile("Demo");
        repo.saveBudget(new MonthlyBudget(a.getId(),YearMonth.of(2026,10),BigDecimal.TEN,List.of()));
        Path file=root.resolve(a.getId()).resolve("2026-10.json");
        Files.writeString(file,"{broken budget");
        assertThrows(BudgetStorageException.class, () -> repo.loadBudget(a.getId(),YearMonth.of(2026,10)));
        assertEquals("{broken budget",Files.readString(file));
        Files.writeString(root.resolve("profiles.json"),"{broken profiles");
        assertThrows(BudgetStorageException.class, () -> repo.createProfile("Another"));
        assertEquals("{broken profiles",Files.readString(root.resolve("profiles.json")));
    }
    @Test void inconsistentOwnershipOrMonthIsRejected() throws Exception {
        var repo=repository(); var a=repo.createProfile("A"); var b=repo.createProfile("B");
        repo.saveBudget(new MonthlyBudget(a.getId(),YearMonth.of(2026,10),BigDecimal.TEN,List.of()));
        Path file=root.resolve(a.getId()).resolve("2026-10.json");
        String original=Files.readString(file);
        Files.writeString(file,original.replace(a.getId(),b.getId()));
        assertThrows(BudgetStorageException.class, () -> repo.loadBudget(a.getId(),YearMonth.of(2026,10)));
        Files.writeString(file,original.replace("2026-10","2026-11"));
        assertThrows(BudgetStorageException.class, () -> repo.loadBudget(a.getId(),YearMonth.of(2026,10)));
    }
    @Test void inaccessibleRootPreservesExistingFile() throws Exception {
        Path blocked=root.resolve("blocked"); Files.writeString(blocked,"preserve me");
        var repo=new FileBudgetRepository(blocked,new ObjectMapper().findAndRegisterModules());
        assertThrows(BudgetStorageException.class, () -> repo.createProfile("Demo"));
        assertEquals("preserve me",Files.readString(blocked));
    }

    @Test void failedReplacementPreservesExistingBudgetAndCleansTemporaryFile() throws Exception {
        var original=repository(); var profile=original.createProfile("Demo");
        var month=YearMonth.of(2026,10);
        original.saveBudget(new MonthlyBudget(profile.getId(),month,new BigDecimal("3000"),List.of(rent())));
        Path file=root.resolve(profile.getId()).resolve("2026-10.json");
        byte[] before=Files.readAllBytes(file);
        var module=new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addSerializer(MonthlyBudget.class,new com.fasterxml.jackson.databind.JsonSerializer<MonthlyBudget>() {
            @Override public void serialize(MonthlyBudget value,com.fasterxml.jackson.core.JsonGenerator generator,
                    com.fasterxml.jackson.databind.SerializerProvider provider) throws java.io.IOException {
                generator.writeStartObject(); generator.writeStringField("partial","incomplete");
                throw new java.io.IOException("Controlled serialization failure");
            }
        });
        var failing=new FileBudgetRepository(root,new ObjectMapper().findAndRegisterModules().registerModule(module));
        assertThrows(BudgetStorageException.class,()->failing.saveBudget(new MonthlyBudget(profile.getId(),month,BigDecimal.ONE,List.of())));
        assertArrayEquals(before,Files.readAllBytes(file));
        try(var files=Files.list(file.getParent())) { assertFalse(files.anyMatch(p->p.getFileName().toString().endsWith(".tmp"))); }
        assertEquals(new BigDecimal("3000.00"),original.loadBudget(profile.getId(),month).getIncome());
    }
}

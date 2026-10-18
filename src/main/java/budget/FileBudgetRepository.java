package budget;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.*;
/** One JSON budget per profile/month. Updates replace a completed sibling file. */
public final class FileBudgetRepository {
    private final Path root;
    private final ObjectMapper mapper;
    public FileBudgetRepository(Path root, ObjectMapper mapper) {
        this.root=Objects.requireNonNull(root).toAbsolutePath().normalize();
        this.mapper=mapper.copy().findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }
    public synchronized List<UserProfile> listProfiles() {
        try {
            String json=Files.readString(root.resolve("profiles.json"));
            List<UserProfile> profiles=mapper.readValue(json,new TypeReference<List<UserProfile>>(){});
            if (profiles==null) throw new IllegalArgumentException("Invalid profiles.");
            Set<String> ids=new HashSet<>(), names=new HashSet<>();
            for (UserProfile profile:profiles) {
                if (profile==null || !ids.add(profile.getId()) || !names.add(profile.getDisplayName().toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Duplicate profiles.");
                }
            }
            return List.copyOf(profiles);
        } catch (NoSuchFileException ex) { return List.of(); }
        catch (IOException | IllegalArgumentException ex) {
            throw new BudgetStorageException("Saved profiles could not be read. Existing data has not been reset.",ex);
        }
    }
    public synchronized UserProfile createProfile(String name) {
        UserProfile profile=new UserProfile(UUID.randomUUID().toString(),name);
        List<UserProfile> profiles=new ArrayList<>(listProfiles());
        if (profiles.stream().anyMatch(p->p.getDisplayName().equalsIgnoreCase(profile.getDisplayName()))) {
            throw new IllegalArgumentException("That profile name already exists. Choose it below or use a different name.");
        }
        profiles.add(profile);
        write(root.resolve("profiles.json"),profiles);
        return profile;
    }
    public synchronized Optional<UserProfile> findProfile(String id) {
        Identifiers.require(id);
        return listProfiles().stream().filter(p->p.getId().equals(id)).findFirst();
    }
    private void requireProfile(String id) {
        if (findProfile(id).isEmpty()) throw new IllegalArgumentException("That profile is no longer available. Choose another profile.");
    }
    private Path budgetPath(String id,YearMonth month) {
        Identifiers.require(id);
        if (month==null || month.getYear()<1 || month.getYear()>9999) throw new IllegalArgumentException("Choose a valid month.");
        return root.resolve(id).resolve(month+".json");
    }
    public synchronized MonthlyBudget loadBudget(String id,YearMonth month) {
        requireProfile(id);
        Path path=budgetPath(id,month);
        try {
            MonthlyBudget budget=mapper.readValue(Files.readString(path),MonthlyBudget.class);
            if (budget==null || !budget.getProfileId().equals(id) || !budget.getMonth().equals(month)) {
                throw new IllegalArgumentException("Budget ownership or month is inconsistent.");
            }
            return budget;
        } catch (NoSuchFileException ex) { return new MonthlyBudget(id,month,BigDecimal.ZERO,List.of()); }
        catch (IOException | IllegalArgumentException ex) {
            throw new BudgetStorageException("This month's saved budget could not be read. It has not been overwritten.",ex);
        }
    }
    public synchronized void saveBudget(MonthlyBudget budget) {
        loadBudget(budget.getProfileId(),budget.getMonth());
        write(budgetPath(budget.getProfileId(),budget.getMonth()),budget);
    }
    private void write(Path path,Object value) {
        Path temporary=null;
        try {
            Files.createDirectories(path.getParent());
            temporary=Files.createTempFile(path.getParent(),"budget-",".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(),value);
            try { Files.move(temporary,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ex) {
            throw new BudgetStorageException("Your changes could not be saved. Check the data folder and try again.",ex);
        } finally {
            if (temporary!=null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Preserve the original save error. */ }
            }
        }
    }
}

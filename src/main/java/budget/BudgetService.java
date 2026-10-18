package budget;
import java.math.*;
import java.time.*;
import java.util.*;
/** Coordinates validation and serializes read-modify-write operations. */
public final class BudgetService {
    private final FileBudgetRepository repository;
    private final BudgetMethod method;
    public BudgetService(FileBudgetRepository repository,BudgetMethod method) { this.repository=repository;this.method=method; }
    public synchronized List<UserProfile> profiles() { return repository.listProfiles(); }
    public synchronized UserProfile createProfile(String name) { return repository.createProfile(name); }
    public synchronized UserProfile requireProfile(String id) {
        return repository.findProfile(id).orElseThrow(()->new IllegalArgumentException("Choose an available profile."));
    }
    public synchronized BudgetSummary summary(String id,YearMonth month) {
        UserProfile profile=requireProfile(id);
        MonthlyBudget budget=repository.loadBudget(id,month);
        BudgetTargets targets=method.calculate(budget.getIncome());
        List<CategorySummary> categories=new ArrayList<>();
        BigDecimal total=new BigDecimal("0.00");
        for (BudgetCategory category:BudgetCategory.values()) {
            BigDecimal actual=budget.getTransactions().stream().filter(t->t.getCategory()==category)
                    .map(Transaction::getAmount).reduce(new BigDecimal("0.00"),BigDecimal::add);
            BigDecimal target=targets.get(category);
            int progress=actual.signum()==0 ? 0 : (target.signum()==0 || actual.compareTo(target)>=0 ? 100 :
                    actual.multiply(new BigDecimal("100")).divide(target,0,RoundingMode.HALF_UP).intValue());
            categories.add(new CategorySummary(category,target,actual,target.subtract(actual),progress));
            total=total.add(actual);
        }
        return new BudgetSummary(profile,budget,categories,total,budget.getIncome().subtract(total));
    }
    public synchronized void saveIncome(String id,YearMonth month,String rawIncome) {
        BigDecimal income=Money.parse(rawIncome,false);
        MonthlyBudget old=repository.loadBudget(id,month);
        repository.saveBudget(new MonthlyBudget(id,month,income,old.getTransactions()));
    }
    public synchronized void addTransaction(String id,YearMonth month,String rawDate,String description,String rawAmount,String rawCategory) {
        LocalDate date;
        try { date=LocalDate.parse(rawDate==null?"":rawDate); }
        catch (DateTimeException ex) { throw new IllegalArgumentException("Enter a valid entry date."); }
        BudgetCategory category;
        try { category=BudgetCategory.valueOf(rawCategory==null?"":rawCategory); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Choose needs, wants, or goals."); }
        Transaction entry=new Transaction(UUID.randomUUID().toString(),date,description,Money.parse(rawAmount,true),category);
        MonthlyBudget old=repository.loadBudget(id,month);
        List<Transaction> entries=new ArrayList<>(old.getTransactions());
        entries.add(entry);
        repository.saveBudget(new MonthlyBudget(id,month,old.getIncome(),entries));
    }
    public synchronized void deleteTransaction(String id,YearMonth month,String transactionId) {
        Identifiers.require(transactionId);
        MonthlyBudget old=repository.loadBudget(id,month);
        List<Transaction> entries=new ArrayList<>(old.getTransactions());
        if (!entries.removeIf(t->t.getId().equals(transactionId))) throw new IllegalArgumentException("That entry is not available in this profile and month.");
        repository.saveBudget(new MonthlyBudget(id,month,old.getIncome(),entries));
    }
}

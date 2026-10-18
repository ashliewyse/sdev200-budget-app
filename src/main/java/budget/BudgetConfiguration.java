package budget;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration
public class BudgetConfiguration {
    @Bean public BudgetMethod budgetMethod() { return new FiftyThirtyTwentyMethod(); }
    @Bean public FileBudgetRepository budgetRepository(@Value("${budget.data-dir:./data}") String directory,ObjectMapper mapper) {
        return new FileBudgetRepository(Path.of(directory),mapper);
    }
    @Bean public BudgetService budgetService(FileBudgetRepository repository,BudgetMethod method) { return new BudgetService(repository,method); }
}

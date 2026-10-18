package budget;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/** Starts the same application as a runnable WAR or in a servlet container. */
@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {
    public static void main(String[] args) { SpringApplication.run(BudgetApplication.class, args); }
    @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BudgetApplication.class);
    }
}

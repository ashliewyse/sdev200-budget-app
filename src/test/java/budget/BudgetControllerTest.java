package budget;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
class BudgetControllerTest {
    @TempDir static Path dataRoot;
    @DynamicPropertySource static void storage(DynamicPropertyRegistry registry) { registry.add("budget.data-dir",()->dataRoot.toString()); }
    @Autowired MockMvc mvc;
    @Autowired BudgetService service;
    MockHttpSession session; UserProfile profile;
    @BeforeEach void setup() {
        profile=service.createProfile("Demo "+UUID.randomUUID().toString().substring(0,8));
        session=new MockHttpSession(); session.setAttribute("activeProfileId",profile.getId()); session.setAttribute("csrfToken","test-token");
    }
    @Test void anonymousAndStaleProfilesReturnToSelection() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profiles"));
        session.setAttribute("activeProfileId",UUID.randomUUID().toString());
        mvc.perform(get("/").session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profiles"));
    }
    @Test void validFormsSaveThenRedirect() throws Exception {
        mvc.perform(post("/income").session(session).param("_csrf","test-token").param("month","2026-10").param("income","3000"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/?month=2026-10"));
        mvc.perform(post("/transactions").session(session).param("_csrf","test-token").param("month","2026-10").param("date","2026-10-17")
                .param("description","Groceries").param("amount","55.25").param("category","NEEDS")).andExpect(status().is3xxRedirection());
        var budget=service.summary(profile.getId(),YearMonth.of(2026,10)).getBudget();
        assertEquals(new BigDecimal("3000.00"),budget.getIncome());
        assertEquals(new BigDecimal("55.25"),budget.getTransactions().get(0).getAmount());
        mvc.perform(post("/transactions/"+budget.getTransactions().get(0).getId()+"/delete").session(session)
                .param("_csrf","test-token").param("month","2026-10")).andExpect(status().is3xxRedirection());
        assertTrue(service.summary(profile.getId(),YearMonth.of(2026,10)).getBudget().getTransactions().isEmpty());
    }
    @Test void invalidAmountRetainsFormInputWithoutChangingBudget() throws Exception {
        mvc.perform(post("/transactions").session(session).param("_csrf","test-token").param("month","2026-10").param("date","2026-10-17")
                .param("description","Keep this description").param("amount","12.345").param("category","WANTS"))
                .andExpect(status().isBadRequest()).andExpect(view().name("dashboard"))
                .andExpect(model().attribute("descriptionInput","Keep this description")).andExpect(model().attribute("amountInput","12.345"));
        assertTrue(service.summary(profile.getId(),YearMonth.of(2026,10)).getBudget().getTransactions().isEmpty());
        mvc.perform(post("/income").session(session).param("_csrf","test-token").param("month","2026-10").param("income","-5"))
                .andExpect(status().isBadRequest()).andExpect(model().attribute("incomeInput","-5"));
    }
    @Test void missingOrIncorrectCsrfCannotMutate() throws Exception {
        mvc.perform(post("/income").session(session).param("month","2026-10").param("income","3000")).andExpect(status().isForbidden());
        mvc.perform(post("/income").session(session).param("_csrf","wrong").param("month","2026-10").param("income","3000")).andExpect(status().isForbidden());
        assertEquals(new BigDecimal("0.00"),service.summary(profile.getId(),YearMonth.of(2026,10)).getBudget().getIncome());
    }
    @Test void postedProfileIdCannotRedirectIncomeToAnotherProfile() throws Exception {
        var other=service.createProfile("Other "+UUID.randomUUID().toString().substring(0,8));
        mvc.perform(post("/income").session(session).param("_csrf","test-token").param("month","2026-10").param("income","900")
                .param("profileId",other.getId())).andExpect(status().is3xxRedirection());
        assertEquals(new BigDecimal("0.00"),service.summary(other.getId(),YearMonth.of(2026,10)).getBudget().getIncome());
        assertEquals(new BigDecimal("900.00"),service.summary(profile.getId(),YearMonth.of(2026,10)).getBudget().getIncome());
    }
    @Test void createAndSelectProfilesUseExplicitForms() throws Exception {
        mvc.perform(post("/profiles").session(session).param("_csrf","test-token").param("displayName","New "+UUID.randomUUID().toString().substring(0,8)))
                .andExpect(status().is3xxRedirection());
        assertNotEquals(profile.getId(),session.getAttribute("activeProfileId"));
        mvc.perform(post("/profiles/select").session(session).param("_csrf","test-token").param("profileId",profile.getId()))
                .andExpect(status().is3xxRedirection());
        assertEquals(profile.getId(),session.getAttribute("activeProfileId"));
    }
    @Test void corruptedBudgetShowsStorageErrorInsteadOfEmptyBudget() throws Exception {
        Path dir=dataRoot.resolve(profile.getId()); Files.createDirectories(dir); Path file=dir.resolve("2026-10.json");
        Files.writeString(file,"{bad");
        mvc.perform(get("/").session(session).param("month","2026-10")).andExpect(status().isServiceUnavailable())
                .andExpect(view().name("error")).andExpect(model().attributeExists("message"));
        assertEquals("{bad",Files.readString(file));
    }
    @Test void failedStoragePreservesSubmittedValues() throws Exception {
        Path dir=dataRoot.resolve(profile.getId()); Files.createDirectories(dir);
        Files.writeString(dir.resolve("2026-10.json"),"{bad");
        var result=mvc.perform(post("/transactions").session(session).param("_csrf","test-token").param("month","2026-10")
                .param("date","2026-10-17").param("description","Unsaved groceries").param("amount","22.50").param("category","NEEDS"))
                .andExpect(status().isServiceUnavailable()).andExpect(view().name("error"))
                .andExpect(model().attributeExists("submittedValues")).andReturn();
        assertEquals("Unsaved groceries",((Map<?,?>)result.getModelAndView().getModel().get("submittedValues")).get("Description"));
    }
    @Test void invalidMonthProducesReadableBadRequest() throws Exception {
        mvc.perform(get("/").session(session).param("month","2026-13")).andExpect(status().isBadRequest())
                .andExpect(view().name("error")).andExpect(model().attributeExists("message"));
    }
}

package budget;
import jakarta.servlet.http.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/** HTTP forms use the session's profile, never a posted storage path. */
@Controller
public class BudgetController {
    private final BudgetService service;
    public BudgetController(BudgetService service) { this.service=service; }
    private String active(HttpSession session) {
        Object id=session.getAttribute("activeProfileId");
        if (id instanceof String text) {
            try { service.requireProfile(text);return text; }
            catch (IllegalArgumentException ex) { session.removeAttribute("activeProfileId"); }
        }
        return null;
    }
    private String activeForForm(HttpSession session,String formProfileId) {
        String id=active(session);
        if (id!=null && !id.equals(formProfileId)) {
            throw new IllegalArgumentException("The selected profile changed after this form was opened. Reopen the planner and retry your change.");
        }
        return id;
    }
    private static YearMonth month(String raw) {
        if (raw==null || !raw.matches("[0-9]{4}-[0-9]{2}")) throw new IllegalArgumentException("Choose a valid month.");
        try {
            YearMonth month=YearMonth.parse(raw);
            if (month.getYear()<1) throw new DateTimeException("Invalid year.");return month;
        } catch (DateTimeException ex) { throw new IllegalArgumentException("Choose a valid month."); }
    }
    private String profiles(Model model) {
        model.addAttribute("profiles",service.profiles());model.addAttribute("pageTitle","Choose a profile");
        if (!model.containsAttribute("displayNameInput")) model.addAttribute("displayNameInput","");return "profiles";
    }
    @GetMapping("/profiles") public String profilesPage(Model model) { return profiles(model); }
    @PostMapping("/profiles") public String createProfile(@RequestParam(defaultValue="") String displayName,HttpSession session,
            Model model,HttpServletResponse response,RedirectAttributes flash) {
        try {
            UserProfile profile=service.createProfile(displayName);session.setAttribute("activeProfileId",profile.getId());
            flash.addFlashAttribute("success","Profile created. Start with your monthly take-home income.");return "redirect:/";
        } catch (IllegalArgumentException ex) {
            response.setStatus(400);model.addAttribute("error",ex.getMessage());model.addAttribute("displayNameInput",displayName);return profiles(model);
        }
    }
    @PostMapping("/profiles/select") public String selectProfile(@RequestParam(defaultValue="") String profileId,HttpSession session,RedirectAttributes flash) {
        service.requireProfile(profileId);session.setAttribute("activeProfileId",profileId);flash.addFlashAttribute("success","Profile selected.");return "redirect:/";
    }
    @GetMapping("/") public String dashboard(@RequestParam(defaultValue="") String month,HttpSession session,Model model) {
        String id=active(session);if (id==null) return "redirect:/profiles";
        String remembered=(String)session.getAttribute("selectedMonth");
        YearMonth selected=month.isBlank()?(remembered==null?YearMonth.now():month(remembered)):month(month);
        return dashboard(id,selected,session,model);
    }
    private String dashboard(String id,YearMonth selected,HttpSession session,Model model) {
        BudgetSummary summary=service.summary(id,selected);
        model.addAttribute("summary",summary);model.addAttribute("month",selected.toString());
        model.addAttribute("monthStart",selected.atDay(1));model.addAttribute("monthEnd",selected.atEndOfMonth());model.addAttribute("pageTitle","Monthly planner");
        session.setAttribute("selectedMonth",selected.toString());
        if (!model.containsAttribute("incomeInput")) model.addAttribute("incomeInput",summary.getBudget().getIncome().toPlainString());
        if (!model.containsAttribute("dateInput")) model.addAttribute("dateInput",YearMonth.from(LocalDate.now()).equals(selected)?LocalDate.now():selected.atDay(1));
        if (!model.containsAttribute("descriptionInput")) model.addAttribute("descriptionInput","");
        if (!model.containsAttribute("amountInput")) model.addAttribute("amountInput","");
        if (!model.containsAttribute("categoryInput")) model.addAttribute("categoryInput","NEEDS");return "dashboard";
    }
    @PostMapping("/income") public String income(@RequestParam(defaultValue="") String formProfileId,@RequestParam(defaultValue="") String month,@RequestParam(defaultValue="") String income,
            HttpSession session,Model model,HttpServletResponse response,RedirectAttributes flash) {
        String id=activeForForm(session,formProfileId);if (id==null) return "redirect:/profiles";YearMonth selected=month(month);
        try { service.saveIncome(id,selected,income);flash.addFlashAttribute("success","Monthly income saved.");return "redirect:/?month="+selected; }
        catch (IllegalArgumentException ex) {
            response.setStatus(400);model.addAttribute("error",ex.getMessage());model.addAttribute("incomeInput",income);return dashboard(id,selected,session,model);
        }
    }
    @PostMapping("/transactions") public String add(@RequestParam(defaultValue="") String formProfileId,@RequestParam(defaultValue="") String month,@RequestParam(defaultValue="") String date,
            @RequestParam(defaultValue="") String description,@RequestParam(defaultValue="") String amount,@RequestParam(defaultValue="") String category,
            HttpSession session,Model model,HttpServletResponse response,RedirectAttributes flash) {
        String id=activeForForm(session,formProfileId);if (id==null) return "redirect:/profiles";YearMonth selected=month(month);
        try { service.addTransaction(id,selected,date,description,amount,category);flash.addFlashAttribute("success","Entry saved.");return "redirect:/?month="+selected; }
        catch (IllegalArgumentException ex) {
            response.setStatus(400);model.addAttribute("error",ex.getMessage());model.addAttribute("dateInput",date);
            model.addAttribute("descriptionInput",description);model.addAttribute("amountInput",amount);model.addAttribute("categoryInput",category);
            return dashboard(id,selected,session,model);
        }
    }
    @PostMapping("/transactions/{id}/delete") public String delete(@PathVariable String id,@RequestParam(defaultValue="") String formProfileId,@RequestParam(defaultValue="") String month,
            HttpSession session,RedirectAttributes flash) {
        String profile=activeForForm(session,formProfileId);if (profile==null) return "redirect:/profiles";YearMonth selected=month(month);
        service.deleteTransaction(profile,selected,id);flash.addFlashAttribute("success","Entry deleted.");return "redirect:/?month="+selected;
    }
    @GetMapping("/help") public String help(Model model) { model.addAttribute("pageTitle","Help");return "help"; }
    @ExceptionHandler(IllegalArgumentException.class)
    public String invalid(IllegalArgumentException ex,Model model,HttpServletResponse response) {
        response.setStatus(400);model.addAttribute("message",ex.getMessage());model.addAttribute("pageTitle","Check your request");return "error";
    }
    @ExceptionHandler(BudgetStorageException.class)
    public String storage(BudgetStorageException ex,Model model,HttpServletResponse response,HttpServletRequest request) {
        response.setStatus(503);model.addAttribute("message",ex.getMessage());model.addAttribute("pageTitle","Unable to save or load");
        Map<String,String> submitted=new LinkedHashMap<>();
        String[][] fields={{"month","Month"},{"income","Income"},{"date","Date"},{"description","Description"},{"amount","Amount"},{"category","Category"},{"displayName","Profile name"}};
        for (String[] field:fields) if (request.getParameter(field[0])!=null) submitted.put(field[1],request.getParameter(field[0]));
        model.addAttribute("submittedValues",submitted);return "error";
    }
}

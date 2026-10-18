# Application architecture

The final app extends the existing Java calculator. Ordinary Java classes handle the budget independently of JSP rendering.

## Data flow

A browser form is checked by CsrfInterceptor. BudgetController selects the active profile from the session and parses the month. BudgetService validates a change, loads its MonthlyBudget, creates a new immutable value, and saves it through FileBudgetRepository. Successful POSTs redirect to a GET, preventing a normal page refresh from resubmitting the form.

The dashboard uses BudgetSummary and CategorySummary. The JSPs render values with JSTL c:out and format money with fmt:formatNumber. Each JSP/fragment declares UTF-8 for its own source file.

## Budget calculations

BudgetMethod defines calculate(BigDecimal). FiftyThirtyTwentyMethod implements it. BudgetCategory identifies NEEDS, WANTS, and GOALS. BudgetTargets holds the result.

The service totals saved entries by enum category. Remaining target = category target minus recorded amount. Unassigned income = income minus all entries. Goals use contribution-specific wording. A zero target is handled without dividing by zero.

## Persistence

Profile metadata is in profiles.json; each generated UUID has separate YYYY-MM.json files. Constructors validate deserialized state, including amount precision, IDs, entry month, and duplicate entry IDs. The repository checks that the file's owner/month match the requested budget.

Only a genuinely absent month file is treated as a new budget. Invalid existing files cause BudgetStorageException. Saves write a sibling temporary file and replace the target after serialization completes. Atomic moves are used where supported. Service mutations are synchronized to avoid lost updates inside one running application.

## Web boundaries

GET routes display pages. POST routes create/select a profile, update income, add an entry, or delete an entry. Income and transaction handlers do not use a posted profile ID as the storage owner. Session CSRF tokens guard mutations. JSP output escaping prevents descriptions from becoming executable markup.

This local course app has profile separation, not authentication. It is bound to loopback by default. One running instance should use a data directory; it is not a multi-server or shared-host deployment.

## Build and tests

Java 17 and Maven build an executable WAR with embedded Tomcat/Jasper and JSP resources. JUnit tests use temporary data folders. JspRenderingTest starts the real server and exercises HTML forms with cookies, not only controller view names. The delivered WAR is checked separately from the source directory.

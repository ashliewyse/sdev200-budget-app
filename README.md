# 50/30/20 Budget Planner — SDEV200 Final

A Java web budgeting application by Ashlie Wyse. It continues the Module 3 calculator and Module 6 project proposal with a JSP interface, transactions, monthly comparisons, and separate saved local profiles.

Repository: https://github.com/ashliewyse/sdev200-budget-app

## Requirements

- Java **17 or later** (a JDK is needed to build).
- Maven **3.6.3 or later**, or the Maven support in IntelliJ IDEA.
- Internet access for the first Maven dependency download.
- A browser. The application listens only on **127.0.0.1** by default.

The build pins Spring Boot 3.5.16 and uses its embedded Tomcat/Jasper support. This is a **WAR**, so the packaged app includes the JSP pages and can run with Java directly.

## Build, test, and run

Open a terminal in the project folder:

~~~powershell
java -version
mvn test
mvn package
java -jar target/sdev200-budget-app.war --budget.data-dir=./data
~~~

Then open **http://127.0.0.1:8085/**. Stop the application with Ctrl+C in the terminal that started it. Use the same data directory each time to reopen saved profiles.

If port 8085 is occupied:

~~~powershell
java -jar target/sdev200-budget-app.war --server.port=8086 --budget.data-dir=./data
~~~

Open http://127.0.0.1:8086/ for that run. You can also give an absolute data-directory path. Relative paths are relative to the terminal's working directory, not the WAR's location.

In IntelliJ, open this folder as a Maven project, select a Java 17+ JDK, allow dependencies to finish loading, and run BudgetApplication. The packaged WAR is the verified delivery format.

## How to use it

1. Create a local profile or open an existing one.
2. Choose a planning month and click **Open month**.
3. Enter monthly take-home income and choose **Save income**.
4. Add dated expenses or goals contributions with **Save entry**.
5. Compare the three category cards with your monthly targets.
6. Use **Delete** to remove a mistaken entry, then add its correction.
7. Use **Profiles** to switch people and **Help** for category explanations.

If you change profiles in another tab, reopen older planner forms before saving. The app rejects a form whose displayed profile no longer matches the selected profile.

Only a successful save message confirms a change. Validation errors retain the entered fields. A storage-error page displays submitted values so they can be copied before returning to the planner; it does not claim a save succeeded. Unreadable saved files produce an error rather than an empty replacement.

### The selected budgeting method

- **Needs: 50%** — housing, groceries, utilities, and minimum debt payments.
- **Wants: 30%** — optional purchases, entertainment, and dining out.
- **Goals: 20%** — savings or extra debt repayments. Goals are contributions, not spending.

Currency uses BigDecimal. Needs and wants round to cents; goals receives the remaining cents so the targets always equal the original income. An uneven amount may shift a penny to goals. Minimum debt payments and extra repayments should not be entered twice.

Amounts accept at most two decimal places, from zero through 999999999.99 for income; entries must be greater than zero. Each entry's date must belong to the selected month. Profile names contain 1–40 characters and descriptions 1–120 characters.

Cards show actual totals, remaining target amounts, overspending for needs/wants, and progress toward goals. The progress bar stops at 100%; numeric totals still show the complete amount. "Income not yet recorded in entries" is income minus all recorded expenses and goals contributions.

## Saved data and limitations

By default, data is stored under ./data:

~~~text
data/
  profiles.json
  <generated-profile-UUID>/
    2026-10.json
    2026-11.json
~~~

Each budget belongs to one profile and one calendar month. UUIDs, not names, are used in storage paths. The app validates loaded records as well as form input and replaces saved files only after a complete new file has been written. In-process updates are serialized.

Back up the **entire data folder while the app is stopped**. Do not edit JSON while the app is running. If a file becomes invalid, retain a copy and restore a valid backup rather than deleting it blindly. Run one app instance against a given data folder.

Profiles are **not password-protected accounts**. Anyone who can access this local app can select a profile. Browser tabs share the selected profile through the browser session. The planner does not connect to a bank. Demonstrations and screenshots use fictional data. Saved user data and generated build output are excluded from Git.

## Code map

| File | Responsibility |
| --- | --- |
| BudgetApplication.java | Spring Boot entry point and WAR initialization |
| BudgetMethod.java | Required calculation interface |
| FiftyThirtyTwentyMethod.java | 50/30/20 calculation using BigDecimal |
| BudgetCategory.java | Required NEEDS / WANTS / GOALS enum |
| BudgetTargets.java | Immutable category targets |
| UserProfile.java | Validated local profile |
| Transaction.java | Validated dated entry |
| MonthlyBudget.java | Immutable profile/month state |
| Money.java / Identifiers.java | Money limits and canonical identifiers |
| FileBudgetRepository.java | Per-profile/month JSON persistence |
| BudgetService.java | Validation, totals, and serialized changes |
| BudgetSummary.java / CategorySummary.java | Dashboard view models |
| BudgetController.java | Form routes and session profile selection |
| CsrfInterceptor.java | Session token checks on state-changing forms |
| WebConfiguration.java / BudgetConfiguration.java | Web and application wiring |
| src/main/webapp/WEB-INF/jsp/ | Actual JSP profile, dashboard, help, and error pages |
| src/main/resources/static/app.css | Responsive styling |

There are more than five ordinary Java classes, in addition to the interface and enum. The original BudgetApp console demonstration remains available in the source.

See [architecture notes](docs/architecture.md) and the [walkthrough script](docs/code-walkthrough-script.txt).

## Verification

Run mvn test for calculation, validation, profile/month isolation, reload, malformed-file protection, deletion, concurrency, MVC form behavior, CSRF, Unicode, escaped markup, and actual embedded-server JSP rendering. The runnable WAR is also checked in the browser, including a process restart.

## Recording

The accompanying final walkthrough is **computer narrated**, using a generic installed Microsoft Zira Desktop voice, with a real screen capture of this app and its source. It does not use or impersonate the author's voice. The script is included in docs/code-walkthrough-script.txt. The video is a separate final-assignment deliverable.

## References

- [Spring Boot requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Boot JSP/WAR support](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html)
- [Jakarta Server Pages specification](https://jakarta.ee/specifications/pages/3.1/jakarta-server-pages-spec-3.1)
- The Module 3 report selected the 50/30/20 method; this final retains that project choice.

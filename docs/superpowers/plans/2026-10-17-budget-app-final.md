# Budgeting App Final Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Complete the existing SDEV200 50/30/20 budgeting app and prepare its GitHub link and a clearly computer-narrated code walkthrough without submitting to Canvas.

**Architecture:** Extend the existing calculator with a small Spring Boot MVC app and JSP views. Store separate monthly budgets for generated local profile IDs in JSON files. Keep calculation, persistence, application service, and web code separate.

**Tech Stack:** Java 17, Maven, Spring Boot 3.5.16, embedded Tomcat/Jasper, Jakarta JSTL, Jackson with Java time support, JUnit 5, Spring MVC tests; Windows Microsoft Zira Desktop narration and existing FFmpeg encoder.

**Spec:** ../specs/2026-10-16-budget-app-final-design.md (approved October 17, 2026; user chose computer narration).

## Global Constraints

- Finish Ashlie Wyse's existing 50/30/20 Java budgeting project for the SDEV200 Module 8 final.
- Do not submit anything to Canvas.
- Preserve the original starter and continue in a separate working copy.
- Use more than five ordinary Java classes, plus the required interface and enum.
- Use BigDecimal. Keep income and transactions separate for every profile and month.
- Run on loopback for a local course demonstration. Exclude saved user data from GitHub. Use fictional demo records.
- Profile selection is not password authentication.
- Use runnable WAR packaging and verify the packaged app.
- Preserve repository history at https://github.com/ashliewyse/sdev200-budget-app.
- User explicitly chose computer narration. Use a generic installed voice; label the recording "Computer-narrated code walkthrough."
- The existing projectless workspace is not a Git checkout. At execution, clone the public repository into work/budget-final/sdev200-budget-app as an isolated checkout; do not alter the Desktop starter.
- For commands below, MVN means the installed IntelliJ Maven mvn.cmd, with JAVA_HOME set to the installed Java 17 JDK. Record resolved versions in verification notes.

## Review Focus

1. A crafted profile ID or transaction ID must not escape its profile directory or affect another profile; test with traversal strings and another profile's entry.
2. Invalid or very large amount input must return a readable validation error and preserve entered values; cap accepted input at 999999999.99 with at most two decimal places.
3. Malformed JSON, missing profiles, or write failures must not silently reset saved budgets; test that existing bytes remain intact.
4. Repeated submissions and concurrent mutations must not lose existing transactions; serialize writes and use post/redirect/get after successful changes.
5. Unicode names and HTML-looking descriptions must display as text, remain valid after reload, and not execute markup in the JSP pages.

## File Structure and Shared Contracts

All Java files use package budget under src/main/java/budget. Keep the original BudgetApp console entry point, BudgetMethod interface, BudgetCategory enum, BudgetTargets, and FiftyThirtyTwentyMethod. Add focused ordinary classes with conventional getters so JSP expression language can read them.

- UserProfile(String id, String displayName): getId(), getDisplayName(); generated UUID ID.
- Transaction(String id, LocalDate date, String description, BigDecimal amount, BudgetCategory category): getters; immutable value.
- MonthlyBudget(String profileId, YearMonth month, BigDecimal income, List<Transaction> transactions): getters; immutable copied list.
- CategorySummary(BudgetCategory category, BigDecimal target, BigDecimal actual, BigDecimal remaining, int progressPercent): getters; goals has contribution-specific display wording.
- BudgetSummary(UserProfile profile, MonthlyBudget budget, List<CategorySummary> categories, BigDecimal transactionTotal, BigDecimal unassigned): getters.
- Money.parse(String raw, boolean positive): BigDecimal; whole cents, max 999999999.99, reject blank/sign/exponent/extra decimals.
- FileBudgetRepository(Path root, ObjectMapper mapper): listProfiles(): List<UserProfile>; createProfile(String): UserProfile; findProfile(String): Optional<UserProfile>; loadBudget(String, YearMonth): MonthlyBudget; saveBudget(MonthlyBudget): void.
- BudgetService(FileBudgetRepository repository, BudgetMethod method): profiles(): List<UserProfile>; createProfile(String displayName): UserProfile; requireProfile(String profileId): UserProfile; summary(String profileId, YearMonth month): BudgetSummary; saveIncome(String profileId, YearMonth month, String rawIncome): void; addTransaction(String profileId, YearMonth month, String date, String description, String amount, String category): void; deleteTransaction(String profileId, YearMonth month, String transactionId): void.
- BudgetController: form routes described in Task 3.
- BudgetConfiguration: beans for repository and calculation interface.
- CsrfInterceptor: session token creation/POST validation. One token per session; compare the submitted token before every mutation.

Repository layout: data/profiles.json holds profile metadata; data/<UUID>/<YYYY-MM>.json holds one budget. Only generated/validated IDs and canonical month names enter paths. A missing budget file means an empty month; a malformed existing file is an error. Validate all loaded records as well as new input.

## Task 1: Reproducible Build and Budget Domain

**Files:** Add pom.xml; src/main/java/budget/{BudgetApplication,Money,UserProfile,Transaction,MonthlyBudget,CategorySummary,BudgetSummary}.java; src/main/resources/application.properties; src/test/java/budget/{FiftyThirtyTwentyMethodTest,BudgetDomainTest}.java. Keep existing source and the original smoke test.

**Consumes:** Existing BudgetMethod.calculate(BigDecimal), BudgetTargets.get(BudgetCategory), BudgetTargets.total().
**Produces:** The immutable domain contracts above and a Maven WAR build.

- [ ] Clone the existing repository into the new execution directory; inspect repository instructions/status and compare starter source. Record baseline commit and run the original javac/BudgetMethodTest check into a separate build directory. Preserve existing history.
- [ ] Add Maven build configuration with Spring Boot 3.5.16 parent, Java 17, WAR packaging, web/test starters, Jasper, Jakarta JSTL API/implementation, and Boot repackage plugin. Use BOM-managed versions where available, explicit versions otherwise. Bind the app to 127.0.0.1:8085; configure budget.data-dir with ./data default.
- [ ] Write JUnit tests first: 3000.00 -> 1500.00/900.00/600.00; 4250.50 -> 2125.25/1275.15/850.10; 0 and 0.01/0.02/0.03 conserve every cent; invalid negative/fractional-cent inputs are rejected.
- [ ] Add failing domain tests: positive transaction amounts, zero income, invalid dates/categories, defensive transaction-list copies, and Unicode descriptions. Money.parse("1e9", false), Money.parse("12.345", false), and Money.parse("1000000000", false) throw; Money.parse("12.30", true) returns 12.30.
- [ ] Run MVN -Dtest=FiftyThirtyTwentyMethodTest,BudgetDomainTest test and confirm the new behavior fails before implementation.
- [ ] Implement the domain contracts. Trim display names (1–40 characters) and descriptions (1–120 characters). Give profiles/transactions UUIDs. Keep 50/30/20 rounding behavior from the starter.
- [ ] Rerun focused tests, then MVN test; require zero failures/errors. Commit only explicit source/build/test paths.

## Task 2: Per-Profile Persistence and Budget Service

**Files:** Add src/main/java/budget/{FileBudgetRepository,BudgetService,BudgetConfiguration,BudgetStorageException}.java and src/test/java/budget/{FileBudgetRepositoryTest,BudgetServiceTest}.java.

**Consumes:** Domain contracts from Task 1 and a configured Jackson mapper with Java time support.
**Produces:** Repository and service contracts in the shared-contract section.

- [ ] Write failing repository tests using temporary directories. Create two profiles and two months, save different income/transactions, construct a second repository instance, and require exact independent reloads.
- [ ] Test traversal profile IDs; unknown profiles; case-insensitive duplicate display names; malformed JSON; unexpected ownership/month inside a file; and a file where a directory is expected. Verify a failed load/save does not replace pre-existing bytes.
- [ ] Write service tests: 3000 income, needs 1550 -> remaining -50; wants 100 -> remaining 800; goals contribution 200 -> remaining 400. Delete one entry without changing another profile/month. Reject out-of-month dates, invalid categories, blank/long descriptions, and another profile's transaction ID.
- [ ] Run MVN -Dtest=FileBudgetRepositoryTest,BudgetServiceTest test and verify expected failures.
- [ ] Implement synchronized read/modify/write service operations. Write UTF-8 JSON to a temporary sibling and move into place atomically, using safe replacement fallback only if atomic move is unsupported. Clean up only that owned temporary file on failure.
- [ ] Treat missing month files as empty budgets, but surface malformed/unreadable files through BudgetStorageException. Never return success before storage finishes. Duplicate display names (trimmed, case-insensitive) get a readable validation error.
- [ ] Calculate category actuals/remaining and goals progress. Cap visual progress between 0 and 100 while showing full numeric actual/remaining values; zero targets produce no divide-by-zero.
- [ ] Test concurrent additions with separate transaction IDs; both persist. Rerun all tests and commit explicit paths.

## Task 3: JSP Interface and Form Flow

**Files:** Add src/main/java/budget/{BudgetController,CsrfInterceptor,WebConfiguration}.java; src/main/webapp/WEB-INF/jsp/{profiles,dashboard,help,error}.jsp; src/main/resources/static/app.css; src/test/java/budget/{BudgetControllerTest,JspRenderingTest}.java.

**Consumes:** BudgetService contracts and session keys activeProfileId, csrfToken.
**Produces:** Working JSP pages, guarded mutation forms, and accessible feedback.

Routes:
- GET /profiles: list/create/select profiles.
- POST /profiles (displayName, _csrf): create and select; POST /profiles/select (profileId, _csrf): validate and select.
- GET /?month=YYYY-MM: dashboard for active profile, redirect to /profiles if none.
- POST /income (month, income, _csrf): save.
- POST /transactions (month, date, description, amount, category, _csrf): add.
- POST /transactions/{id}/delete (month, _csrf): delete only in selected profile/month.
- GET /help: describe app, categories, profile selection, storage, and limitations.

- [ ] Write MVC tests first for profile redirects/selections, successful income/transaction redirects, validation errors with retained inputs, bad/missing CSRF rejection, stale profile IDs, and failed storage messages without stack traces.
- [ ] Write a real-server JSP rendering test (not only a view-name assertion) for profile/dashboard/help HTML. A description containing <script> must render escaped; Unicode must survive reload.
- [ ] Run MVN -Dtest=BudgetControllerTest,JspRenderingTest test and confirm expected missing-flow failures.
- [ ] Implement controller validation/parsing, session-scoped profile selection, canonical YYYY-MM input, and session CSRF checks. Never trust a profile ID posted on transaction/income forms.
- [ ] Create JSPs with JSTL c:out, visible labels, keyboard focus, an error summary, safe numeric/month/date inputs, and post/redirect/get success messages. Use needs/wants "spent" and goals "contributed." Provide profile and month switchers and clear transaction deletion controls.
- [ ] Style a readable responsive dashboard with 3 category cards, income, entry form, transaction list, and help navigation. Keep framework/implementation details out of ordinary app use.
- [ ] Rerun full tests and MVN package. Launch the WAR from a directory other than the project with an explicit temporary data path and verify JSPs compile/render. Commit explicit web/config/test files.

## Task 4: Complete-Flow Verification, Review, and Help

**Files:** Update README.md and .gitignore; add docs/architecture.md and docs/code-walkthrough-script.txt. Save local evidence under outputs/Budget-App-Final and work/budget-final/evidence, outside the source upload list.

**Consumes:** Packaged runnable app and tested UI.
**Produces:** Reviewed app, accurate help, source ZIP, verification record.

- [ ] Run the complete test suite and package build; record test count, failures, Java/Maven/Boot versions, and WAR SHA-256.
- [ ] Use the actual browser against the packaged app. Create fictional profiles Demo Ashlie and Demo Morgan. Exercise October/November, income, all 3 categories, overspending, validation, deletion, profile switching, and help. Stop/restart the same app and verify saved values; inspect logs.
- [ ] Check malformed-file/write-error behavior in isolated test data only; preserve demo/user data. Capture readable real UI screenshots for local verification.
- [ ] Request independent whole-project review against the approved spec and rubric. Address meaningful findings and rerun affected checks; do not claim readiness before evidence passes.
- [ ] Update README with Java 17 and Maven 3.6.3+ prerequisites, build/test/run commands, executable WAR instructions, data location/backup, fake-data demonstration, 50/30/20 semantics, local-profile limitations, help, class map, and recording disclosure.
- [ ] Verify .gitignore excludes data, target, IDE settings, logs, capture intermediates, and narration audio. Build a portable source ZIP with source/docs/build files, not generated user data. Record the included-file manifest and SHA-256. Commit reviewed source/help.

## Task 5: Real Screen Recording and GitHub Publication

**Files:** Add local recording helpers under work/budget-final/recording. Final files: outputs/Budget-App-Final/Budget-App-Code-Walkthrough-Computer-Narrated.mp4, Recording-Script.txt, Verification.txt, GitHub-Link.txt, and sdev200-budget-app-final.zip.

**Consumes:** Reviewed final app/source and the generic Microsoft Zira Desktop voice.
**Produces:** Verified MP4 and final repository link; Canvas remains unsubmitted.

- [ ] Write a concise ~3–5-minute script that names the selected method and explains actual class/interface/enum code, JSP/controller flow, validation, JSON per-user/month storage, tests, and a real demo. Opening text/audio: "Computer-narrated code walkthrough." Do not imply the computer voice is Ashlie's own speech.
- [ ] Generate WAV segments with installed System.Speech/Microsoft Zira Desktop; measure durations. Capture the real app and actual source in the browser/editor, keeping the pointer out of central content. Match scene actions to narration segments; no fabricated test results or simulated app output.
- [ ] Adapt the previously verified FFmpeg gdigrab capture workflow. Inspect the current screen before choosing the capture rectangle. Mux the actual capture with synthesized WAV and H.264/AAC; preserve audio.
- [ ] Check MP4 duration, resolution, audio stream, playback, and representative start/middle/end frames. Review narration alignment and ensure no credentials, unrelated tabs, private data, or distracting cursor cover the content. Re-record if the actual demonstration is missing.
- [ ] Publish the tested source/help to the existing repository after reviewing the exact diff and secret/generated-data exclusions. Use normal fast-forward history; no force push. If push is unavailable, use authenticated GitHub file tools/browser upload while preserving existing commits and checking current content SHAs.
- [ ] Fetch published source/help and compare to local manifests; verify the default-branch README describes the finished app. Record final commit and https://github.com/ashliewyse/sdev200-budget-app in GitHub-Link.txt.
- [ ] Deliver clickable local ZIP/recording/script and the GitHub link. State that the recording uses computer narration and that Canvas has not been submitted. Do not click any Canvas Submit button.

## Self-Review and Execution Handoff

Spec coverage: Tasks 1–3 implement all app requirements, Task 4 checks the packaged app and help, Task 5 creates/verifies recording and publication. Review Focus 1–3 are pinned to Tasks 1–3 tests; concurrent writes and Unicode/escaped content are explicitly tested. All cross-task names use the shared contracts above.

Recommended execution: Native — implement the coupled tasks in this chat, then use a fresh independent reviewer for the complete app. Research and verification can run independently where they do not share UI or writable files. Subagent-driven execution is also available with separate review at each task.

Approved October 17, 2026: build here with one final independent review. Computer narration approved; Canvas must remain unsubmitted.



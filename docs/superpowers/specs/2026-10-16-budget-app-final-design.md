# SDEV200 budgeting app final — proposed design

Date: October 16, 2026

## Intended result

Finish Ashlie Wyse's existing 50/30/20 Java budgeting project for the SDEV200 Module 8 final. Prepare a tested app, complete source and help in the existing GitHub repository, and a real screen recording explaining the code and demonstrating the app. Do not submit anything to Canvas.

The Module 3 source and graded Module 6 progress report establish the selected method and unfinished features. Preserve the original starter and continue in a separate working copy.

## Approach

Recommended: extend the starter with Spring Boot MVC, JSP pages, and local JSON files. This follows the Module 6 proposal and works with the installed Java 17 toolchain.

Alternative: a database and account authentication would provide stronger account management but add setup and code beyond the earlier proposal. The recommended app uses selected local profiles with separate saved data. Profile selection is not password authentication.

Use a supported Spring Boot 3.5 patch release and runnable WAR packaging for embedded Tomcat/JSP. Provide Maven build/run instructions and verify the packaged app.

## App behavior

- Create/select profiles with generated identifiers; never use a display name as a storage path. Keep the active profile in the browser session.
- Select a calendar month and save monthly take-home income in dollars and cents.
- Reuse BudgetMethod, BudgetCategory, BudgetTargets, and FiftyThirtyTwentyMethod for 50% needs, 30% wants, and remaining cents for savings or extra debt repayments (goals).
- Add dated transactions with a description, positive amount, and category. Treat goals as contributions, not spending.
- Display targets, category totals, remaining amounts, overspending, and goals progress. Explain minimum debt payments versus extra repayments.
- Delete an individual transaction through an explicit user action. Keep income and transactions separate for every profile and month.
- Persist across application restarts and show helpful validation/file errors while preserving entered form values.
- Provide JSP dashboard, profile selection, and help pages with accessible forms, escaped user text, and fictional demo data.

## Components

Use more than five ordinary Java classes, plus the required interface and enum:

| Component | Responsibility |
| --- | --- |
| BudgetApplication | Start the Spring Boot application |
| BudgetMethod / FiftyThirtyTwentyMethod | Define and calculate targets |
| BudgetCategory | Needs, wants, and goals enum |
| BudgetTargets | Store targets |
| UserProfile | Identify a local profile |
| Transaction | Store a dated categorized entry |
| MonthlyBudget | Hold monthly income and transactions |
| FileBudgetRepository | Save/load profile and month data with atomic replacement |
| BudgetService | Validate changes, summarize totals, coordinate storage |
| BudgetController | Handle forms and supply JSP models |

Use BigDecimal. Reject negative income, nonpositive transactions, fractions of a cent, invalid dates/months/categories, and blank/overly long descriptions. Require transaction dates to match the selected month. Guard state-changing forms with a session CSRF token. Serialize application updates and never overwrite malformed/unreadable saved data with an empty budget.

Run on loopback for a local course demonstration. Exclude saved user data from GitHub. Use fictional demo records. Local profiles do not provide access control for a shared computer.

## Verification and deliverables

1. Automated tests for rounding, validation, category/month totals, profile/month isolation, persistence, deletion, file errors, and web forms.
2. Launch the packaged WAR and exercise real JSP pages using two fictional profiles and two months. Check income, transactions, overspending, deletion, invalid input, and persistence across restart. Check logs.
3. Update https://github.com/ashliewyse/sdev200-budget-app with complete source and README help for prerequisites, build/run/test, profiles, categories, storage, limitations, and code structure. Preserve repository history; exclude saved data/build caches.
4. Record the actual app and source screens while explaining classes, interface, enum, JSP, validation, and user-data persistence. Prepare a narration script. No length is assigned; target approximately 3–5 minutes. The user's personal-voice or clearly labeled computer-narration choice determines the audio workflow; do not impersonate their voice.
5. Save a portable source ZIP, recording, and verification notes. Verify the remote GitHub files and MP4 playback.
6. Leave Canvas unsubmitted. The assignment requires the GitHub URL using Website URL and the recording in another submission.

## Evidence and references

- Starter: C:\Users\wysea\Desktop\Mod3_Final_Project_Wyse_A\sdev200-budget-app.
- Module 6 report: C:\Users\wysea\Documents\Codex\2026-09-29\go\outputs\Java-Mod6-Progress\Mod6_Progress_Wyse_A.docx.
- [Canvas final](https://ivylearn.ivytech.edu/courses/1422263/assignments/24564718?module_item_id=61363462).
- [Course overview](https://ivylearn.ivytech.edu/courses/1422263/pages/final-project-overview).
- [Spring Boot requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Spring Boot JSP support](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html).

## Review status

Approved by the user on October 17, 2026. The user selected computer narration. Written implementation plan prepared for review; application implementation has not begun. This workspace is not a Git checkout; commit the design with the isolated project checkout during execution.


# SDEV 200 Budget App

A Java budgeting project based on the 50/30/20 method: 50% of monthly take-home income for needs, 30% for wants, and 20% for savings or extra debt repayment.

## Current progress

The Module 3 starter calculates category targets and checks input amounts. It includes a budgeting interface, an enum for the categories, and a console demonstration. It does not yet have transaction entry, persistent user data, or a JSP interface.

## Run the starter

Install or use Java 17 or later. From this folder, run the following commands in PowerShell:

```powershell
New-Item -ItemType Directory -Path build -Force | Out-Null
javac -d build src/main/java/budget/*.java src/test/java/budget/*.java
java -cp build budget.BudgetApp
java -cp build budget.BudgetApp 4250.50
java -cp build budget.BudgetMethodTest
```

The default example uses fictional monthly take-home income of $3,000 and displays targets of $1,500, $900, and $600. No bank accounts are connected.

## Current structure

| File | Purpose |
| --- | --- |
| `BudgetCategory.java` | Enum identifying NEEDS, WANTS, and GOALS |
| `BudgetMethod.java` | Interface defining a budgeting calculation |
| `FiftyThirtyTwentyMethod.java` | Implements the 50/30/20 allocation |
| `BudgetTargets.java` | Stores the calculated targets |
| `BudgetApp.java` | Console demonstration |
| `BudgetMethodTest.java` | Checks expected targets, cents, zero income, and invalid input |

Currency uses `BigDecimal`. Input must be nonnegative and expressible in whole cents. Needs and wants are rounded to the nearest cent; goals receives the remainder so the targets always add up to the original income. This can change the goals percentage by a penny for small or uneven amounts.

Minimum required debt payments belong under needs. Additional debt repayments belong under goals. The same payment should not be entered in both categories.

## Planned development

- Add `UserProfile`, `Transaction`, `MonthlyBudget`, and `FileBudgetRepository` classes. Together with the calculation and target classes, these will exceed the final project's five-class requirement.
- Record and categorize transactions, including contributions toward goals.
- Compare needs and wants spending with their targets, and show progress toward the goals target.
- Save and reload each user's data, with input validation and file error handling.
- Add the required JSP interface after Module 4 introduces JSP.
- Test transaction totals, overspending, rounding, and saving/loading data.
- Prepare the Module 6 progress report and Module 8 code walkthrough recording.

## Research

- Erin Talks Money (2024): https://www.youtube.com/watch?v=EcpvkD2nY_k
- Consumer Financial Protection Bureau (2022), *Analyzing budgets*: https://files.consumerfinance.gov/f/documents/cfpb_building_block_activities_analyzing-budgets_guide.pdf
- Chase, *What is the 50/30/20 budget rule?*: https://www.chase.com/personal/banking/education/budgeting-saving/50-20-30-budget-rule

The report is maintained separately from this source-code folder.

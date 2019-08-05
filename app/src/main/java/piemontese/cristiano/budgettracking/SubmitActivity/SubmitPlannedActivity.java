package piemontese.cristiano.budgettracking.SubmitActivity;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpensePlannedPeriodic;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpensePlannedPeriodicSQLiteHelper;
import piemontese.cristiano.budgettracking.R;

public class SubmitPlannedActivity extends SubmitPeriodicPlannedCommonActivity {

    @Override
    protected void setLayout() {
        setContentView(R.layout.submit_planned);
    }

    @Override
    protected void initializeSubmitterForUpdate(Expense expense) {
        this.reminder = expense.reminder;
    }

    @Override
    protected void activitySpecificInitialization() {
        db = new ExpensePlannedPeriodicSQLiteHelper(getApplicationContext());
        setMinDate();
    }

    @Override
    protected Expense createNewExpense(int id, double amount, String date, String description, String category) {
        // this is a one time planned expense so next deadline is the date
        return new ExpensePlannedPeriodic(id, date, amount, description, category, SubmitPlannedActivity.this.reminder, "one time", date);
    }
}

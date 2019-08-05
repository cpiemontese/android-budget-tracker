package piemontese.cristiano.budgettracking.SubmitActivity;

import android.widget.RadioGroup;

import java.util.Calendar;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpensePlannedPeriodic;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpensePlannedPeriodicSQLiteHelper;
import piemontese.cristiano.budgettracking.R;

public class SubmitPeriodicActivity extends SubmitPeriodicPlannedCommonActivity {
    final String[] periodicities = { "yearly", "monthly", "weekly" };
    protected String currentPeriodicity = "yearly";

    @Override
    protected void setLayout() {
        setContentView(R.layout.submit_periodic);
    }

    @Override
    protected void initializeSubmitterForUpdate(Expense expense) {
        this.currentPeriodicity = expense.periodicity;
        this.reminder = expense.reminder;

        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
        switch (this.currentPeriodicity) {
            case "yearly":
                rg.check(R.id.yearlyRadioButton);
                break;
            case "monthly":
                rg.check(R.id.monthlyRadioButton);
                break;
            case "weekly":
                rg.check(R.id.weeklyRadioButton);
        }
    }

    @Override
    protected void activitySpecificInitialization() {
        db = new ExpensePlannedPeriodicSQLiteHelper(getApplicationContext());

        setMinDate();

        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
        rg.check(R.id.yearlyRadioButton);
    }

    @Override
    protected void setOnClickListeners() {
        setSubmitExpenseOnClickListener();
        setRemainderOnClickListener();
        setRadioButtonsOnClickListener();
    }

    @Override
    protected Expense createNewExpense(int id, double amount, String date, String description, String category) {
        // calculate next deadline:
        String nextDeadlineDate;
        Calendar c = Calendar.getInstance();
        String[] dateParts = date.split("/");
        // we set calendar to the date of our deadline
        c.set(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[0]));

        switch (this.currentPeriodicity) {
            case "yearly":
                c.add(Calendar.YEAR, 1);  // we go to next year
                break;
            case "monthly":
                c.add(Calendar.MONTH, 1);
                break;
            case "weekly":
                c.add(Calendar.DAY_OF_MONTH, 7);
        }
        nextDeadlineDate = String.format("%d/%d/%d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));

        return new ExpensePlannedPeriodic(id, date, amount, description, category, this.reminder, this.currentPeriodicity, nextDeadlineDate);
    }

    protected void setRadioButtonsOnClickListener() {
        ((RadioGroup) findViewById(R.id.radioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SubmitPeriodicActivity.this.currentPeriodicity = periodicities[group.indexOfChild(findViewById(checkedId))];
            }
        });
    }
}

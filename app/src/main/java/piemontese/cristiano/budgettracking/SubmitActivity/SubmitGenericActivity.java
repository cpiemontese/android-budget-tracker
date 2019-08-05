package piemontese.cristiano.budgettracking.SubmitActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseSQLiteHelper;
import piemontese.cristiano.budgettracking.R;
import piemontese.cristiano.budgettracking.SharedPreferencesWrapper;

/**
 * Every method/logic here is common to SubmitDailyActivity and SubmitPlannedActivity.
 * Everything that's not common is either added or overridden in their definition.
 */

public abstract class SubmitGenericActivity extends Activity {
    protected SharedPreferencesWrapper spWrapper;
    protected ExpenseSQLiteHelper db;
    protected boolean wasStartedForUpdate;
    protected double oldAmount;
    protected int expenseToUpdateId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setLayout();
        initializeCommonVariables();
        activitySpecificInitialization();

        // we check if this activity was started to modify an expense or to create a new one
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        wasStartedForUpdate = extras != null;
        if (wasStartedForUpdate) {

            Expense expenseToUpdate = (Expense) extras.get("expense");
            expenseToUpdateId = expenseToUpdate.id;

            assert expenseToUpdate != null;
            ((TextView) findViewById(R.id.amount)).setText(Double.toString(expenseToUpdate.amount));
            ((TextView) findViewById(R.id.category)).setText(expenseToUpdate.category);
            ((TextView) findViewById(R.id.description)).setText(expenseToUpdate.description);
            String[] dateParts = expenseToUpdate.date.split("/");
            ((DatePicker) findViewById(R.id.datePicker)).updateDate(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[0]));

            oldAmount = expenseToUpdate.amount;

            initializeSubmitterForUpdate(expenseToUpdate);
        }

        setOnClickListeners();
    }

    protected void initializeCommonVariables() {
        oldAmount = 0.0;
        spWrapper = new SharedPreferencesWrapper(getApplicationContext());
    }

    protected abstract void initializeSubmitterForUpdate(Expense expense);

    protected abstract void activitySpecificInitialization();

    protected void setOnClickListeners() {
        setSubmitExpenseOnClickListener();
    }

    protected void setSubmitExpenseOnClickListener() {
        final Button submitExpenseBtn = (Button) findViewById(R.id.submitBtn);
        submitExpenseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();

                // amount is required
                Double amount;
                String amountText = ((EditText) findViewById(R.id.amount)).getText().toString();
                if (checkEmptyStringAndReport(amountText, Integer.toString(R.string.no_amount_err))) {
                    return;
                } else {
                    amount = Double.parseDouble(amountText);
                }

                // date is required - date might be handled differently so we call getDate()
                String date = getDate();
                if (checkEmptyStringAndReport(date, Integer.toString(R.string.no_date_err))) return;

                String category = ((EditText) findViewById(R.id.category)).getText().toString();
                if (Objects.equals(category, context.getString(R.string.categoryEditTextDefault)))
                    category = "";

                String description = ((EditText) findViewById(R.id.description)).getText().toString();
                if (Objects.equals(description, context.getString(R.string.descriptionEditTextDefault)))
                    description = "";

                int newExpenseId;
                if (SubmitGenericActivity.this.wasStartedForUpdate)
                    newExpenseId = SubmitGenericActivity.this.expenseToUpdateId;
                else
                    newExpenseId = SubmitGenericActivity.this.db.getTotalExpenses() + 1;

                Expense newExpense = createNewExpense(newExpenseId, amount, date, description, category);

                if (!SubmitGenericActivity.this.isPlannedOrPeriodic()) {
                    SubmitGenericActivity.this.spWrapper.updateTotal(oldAmount);    // we add the old amount back
                    SubmitGenericActivity.this.spWrapper.updateTotal(-amount);      // we remove the new amount
                }

                if (SubmitGenericActivity.this.wasStartedForUpdate) {
                    SubmitGenericActivity.this.db.updateExpense(newExpense);

                    Toast.makeText(context, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    SubmitGenericActivity.this.db.addExpense(newExpense);
                    Toast.makeText(context, "Expense submitted successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected boolean checkEmptyStringAndReport(String stringToCheck, String errorMsg) {
        if (stringToCheck.isEmpty()) {
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            return true;
        } else return false;
    }

    protected abstract Expense createNewExpense(int id, double amount, String date, String description, String category);

    protected abstract void setLayout();

    protected abstract String getDate();

    protected boolean isPlannedOrPeriodic() {
        return false;
    }
}

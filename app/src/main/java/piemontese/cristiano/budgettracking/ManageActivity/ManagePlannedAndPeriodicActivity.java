package piemontese.cristiano.budgettracking.ManageActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;

import java.io.FileNotFoundException;
import java.util.List;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpensePlannedPeriodicSQLiteHelper;
import piemontese.cristiano.budgettracking.R;
import piemontese.cristiano.budgettracking.SharedPreferencesWrapper;
import piemontese.cristiano.budgettracking.SubmitActivity.SubmitPeriodicActivity;
import piemontese.cristiano.budgettracking.SubmitActivity.SubmitPlannedActivity;

public class ManagePlannedAndPeriodicActivity extends ManageGenericActivity {
    // we override items because "Show location" won't be used in this activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.manage_plannedperiodic);

        db = new ExpensePlannedPeriodicSQLiteHelper(getApplicationContext());
        spWrapper = new SharedPreferencesWrapper(getApplicationContext());
        items = new CharSequence[]{ "Modify expense", "Remove expense" };

        findViewById(R.id.createPDFBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ManagePlannedAndPeriodicActivity.this.createPdf();
                    Toast.makeText(getApplicationContext(), "Document saved in pdf_reports", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.addExpenseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] expenseChoices = {"Add planned expense", "Add periodic expense"};

                final AlertDialog.Builder expenseChoiceBuilder = new AlertDialog.Builder(ManagePlannedAndPeriodicActivity.this);
                expenseChoiceBuilder.setTitle("Choose expense type");
                expenseChoiceBuilder.setSingleChoiceItems(expenseChoices, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent addExpenseIntent = new Intent(ManagePlannedAndPeriodicActivity.this, SubmitPlannedActivity.class);
                                ManagePlannedAndPeriodicActivity.this.startActivity(addExpenseIntent);
                                break;
                            case 1:
                                addExpenseIntent = new Intent(ManagePlannedAndPeriodicActivity.this, SubmitPeriodicActivity.class);
                                ManagePlannedAndPeriodicActivity.this.startActivity(addExpenseIntent);
                        }
                        dialog.dismiss();
                    }
                });
                expenseChoiceBuilder.setCancelable(true);
                expenseChoiceBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                expenseChoiceBuilder.show();
            }
        });

        updateList();
    }

    @Override
    protected void modifyExpense(Expense expenseToModify) {
        Intent modifyExpenseIntent;
        switch (expenseToModify.periodicity) {
            case "one time":
                modifyExpenseIntent = new Intent(ManagePlannedAndPeriodicActivity.this, SubmitPlannedActivity.class);
                break;
            default:
                modifyExpenseIntent = new Intent(ManagePlannedAndPeriodicActivity.this, SubmitPeriodicActivity.class);
        }

        modifyExpenseIntent.putExtra("modified expense id", expenseToModify.id);
        modifyExpenseIntent.putExtra("expense", expenseToModify);
        startActivity(modifyExpenseIntent);
    }

    @Override
    protected void removeExpense(Expense expenseToRemove) {
        ManagePlannedAndPeriodicActivity.this.db.deleteExpense(expenseToRemove.id);
        updateList();
    }

    @Override
    protected void showLocation(Expense expenseToShow) {
        // nothing to do
    }

    @Override
    protected List<Expense> getExpenses() {
        return db.getExpenses();            // get all planned/periodic expenses
    }

    @Override
    protected RelativeLayout setupExpenseEntryParent(Expense expense) {
        RelativeLayout expenseEntryParent = (RelativeLayout) getLayoutInflater().inflate(R.layout.expenses_list_entry_plannedperiodic, null);

        TextView amountTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemAmount);
        TextView deadlineStartTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemStartDeadline);
        TextView nextDeadlineTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemNextDeadline);
        TextView reminderTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemReminder);
        TextView descriptionTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemDescription);
        TextView categoryTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemCategory);

        amountTextView.setText("Amount: " + Double.toString(expense.amount) + "€");
        deadlineStartTextView.setText("Starting date: ".concat(expense.date));

        switch (expense.periodicity) {
            case "one time":
                deadlineStartTextView.setText("Planned date: " + expense.date);
                break;
            default:
                deadlineStartTextView.setText("Starting date: " + expense.date);
                nextDeadlineTextView.setText("Next deadline: " + expense.nextDeadlineDate);
        }

        reminderTextView.setText("Reminder: you will be reminded " + expense.reminder);

        if (!expense.description.isEmpty())
            descriptionTextView.setText("Description: ".concat(expense.description));

        if (!expense.category.isEmpty())
            categoryTextView.setText("Category: ".concat(expense.category));

        return expenseEntryParent;
    }

    @Override
    protected String createPDFTitle() {
        return "Report of planned and periodic expenses";
    }

    @Override
    protected String createPDFTag() {
        return "pp";
    }
}

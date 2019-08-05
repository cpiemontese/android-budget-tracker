package piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpensePlannedPeriodic;

public class ExpensePlannedPeriodicSQLiteHelper extends ExpenseSQLiteHelper {

    public ExpensePlannedPeriodicSQLiteHelper(Context context) {
        super(context);
    }

   @Override
    protected ContentValues createExpenseCV(Expense expense) {
        ContentValues values = new ContentValues();
        values.put(COL_DATE, formatDate(expense.date));
        values.put(COL_AMOUNT, expense.amount);
        values.put(COL_DESCRIPTION, expense.description);
        values.put(COL_CATEGORY, expense.category);
        values.put(COL_REMINDER, expense.reminder);
        values.put(COL_PERIODICITY, expense.periodicity);
        values.put(COL_NEXTDEADLINEDATE, formatDate(expense.nextDeadlineDate));
        return values;
    }

    @Override
    public List<Expense> getExpenses(int condition) {
        return null;
    }

    // Read
    @Override
    public List<Expense> getExpenses() {
        LinkedList<Expense> expenses = new LinkedList<>();
        String query = String.format("SELECT * FROM %s ORDER BY %s DESC", TABLE_PLANNEDPERIODIC, COL_DATE);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ExpensePlannedPeriodic expense;
        if (cursor.moveToFirst()) do {
            expense = new ExpensePlannedPeriodic(
                    Integer.parseInt(cursor.getString(0)),
                    formatDate(cursor.getString(1)),
                    Double.parseDouble(cursor.getString(2)),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    formatDate(cursor.getString(7))
            );
            expenses.add(expense);
        } while (cursor.moveToNext());

        cursor.close();

        return expenses;
    }

    @Override
    protected String getTableName() {
        return TABLE_PLANNEDPERIODIC;
    }
}

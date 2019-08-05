package piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpenseDaily;


/**
 * This helper will manage the daily expenses database
 */

public class ExpenseDailySQLiteHelper extends ExpenseSQLiteHelper {

    // condition codes
    protected static final int GET_ALL = 0;
    protected static final int GET_LAST_WEEK = 1;
    protected static final int GET_LAST_MONTH = 2;
    protected static final int ORDER_ASC = 3;


    public ExpenseDailySQLiteHelper(Context context) {
        super(context);
    }

    @Override
    protected ContentValues createExpenseCV(Expense expense) {
        ContentValues values = new ContentValues();
        values.put(COL_DATE, formatDate(expense.date));
        values.put(COL_AMOUNT, expense.amount);
        values.put(COL_DESCRIPTION, expense.description);
        values.put(COL_CATEGORY, expense.category);
        values.put(COL_LATITUDE, expense.latitude);
        values.put(COL_LONGITUDE, expense.longitude);
        return values;
    }

    @Override
    public List<Expense> getExpenses() {
        return null;
    }

    // Read
    @Override
    public List<Expense> getExpenses(int condition) {
        LinkedList<Expense> expenses = new LinkedList<>();
        String query = String.format("SELECT * FROM %s ", TABLE_DAILY);
        String orderByDate = String.format("ORDER BY %s DESC", COL_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String whereTemplate = "WHERE %s >= \"%s\" AND %s <= \"%s\" ";

        switch (condition) {
            case GET_LAST_WEEK:
                int dayOfWeekOffset = calendar.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeekOffset == 0)
                    dayOfWeekOffset = 7;  // sunday = 0, monday = 1, ..., saturday = 6 --> sunday for us is 7th

                calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeekOffset - 6);   // we go to last week's first day

                String firstDayOfLastWeek = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                if (firstDayOfLastWeek.length() == 1)
                    firstDayOfLastWeek = "0" + firstDayOfLastWeek;

                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);
                String lastWeekStartDate = String.format("%d/%d/%s", year, month, firstDayOfLastWeek);

                calendar.add(Calendar.DAY_OF_MONTH, 6);                         // we go to last day of week
                String lastDayOfLastWeek = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                if (lastDayOfLastWeek.length() == 1)
                    lastDayOfLastWeek = "0" + lastDayOfLastWeek;

                month = calendar.get(Calendar.MONTH) + 1;
                year = calendar.get(Calendar.YEAR);
                String lastWeekEndDate = String.format("%d/%d/%s", year, month, lastDayOfLastWeek);

                query += String.format(whereTemplate, COL_DATE, lastWeekStartDate, COL_DATE, lastWeekEndDate);
                break;
            case GET_LAST_MONTH:
                calendar.add(Calendar.MONTH, -1);   // we go to last month
                int lastDayOfLastMonth = calendar.getMaximum(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH) + 1;
                year = calendar.get(Calendar.YEAR);

                String lastMonthStartDate = String.format("%d/%d/01", year, month);
                String lastMonthEndDate = String.format("%d/%d/%d", year, month, lastDayOfLastMonth);

                query += String.format(whereTemplate, COL_DATE, lastMonthStartDate, COL_DATE, lastMonthEndDate);
                break;
            case GET_ALL:
                break;
            case ORDER_ASC:
                orderByDate = String.format("ORDER BY %s ASC", COL_DATE);
        }
        query = query + orderByDate;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ExpenseDaily expense;
        if (cursor.moveToFirst()) do {
            expense = new ExpenseDaily(Integer.parseInt(cursor.getString(0)),
                    formatDate(cursor.getString(1)),
                    Double.parseDouble(cursor.getString(2)),
                    cursor.getString(3),
                    cursor.getString(4),
                    Double.parseDouble(cursor.getString(5)),
                    Double.parseDouble(cursor.getString(6)));
            expenses.add(expense);
        } while (cursor.moveToNext());



        cursor.close();

        return expenses;
    }

    @Override
    protected String getTableName() {
        return TABLE_DAILY;
    }
}

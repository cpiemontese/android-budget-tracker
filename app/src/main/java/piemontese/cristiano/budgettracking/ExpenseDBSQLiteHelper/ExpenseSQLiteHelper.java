package piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import piemontese.cristiano.budgettracking.Expense.Expense;

public abstract class ExpenseSQLiteHelper extends SQLiteOpenHelper {
    protected static final int DATABASE_VERSION = 1;
    protected static final String DATABASE_NAME = "ExpenseDB";
    protected static final String TABLE_DAILY = "daily_expenses";
    protected static final String TABLE_PLANNEDPERIODIC = "planned_periodic_expenses";

    // common expenses values
    protected static final String COL_ID = "id";
    protected static final String COL_DATE = "date";
    protected static final String COL_AMOUNT = "amount";
    protected static final String COL_CATEGORY = "category";
    protected static final String COL_DESCRIPTION = "description";

    // daily expenses values
    protected static final String COL_LATITUDE = "latitude";
    protected static final String COL_LONGITUDE = "longitude";

    // planned/periodic expenses values
    protected static final String COL_REMINDER = "reminder";
    protected static final String COL_PERIODICITY = "periodicity";
    protected static final String COL_NEXTDEADLINEDATE = "next_deadline_date";


    public ExpenseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // we create the two tables that will be used
        db.execSQL(
                "CREATE TABLE " + TABLE_DAILY + "(" +
                        COL_ID + " integer primary key, " +
                        COL_DATE + " text not null, " +         // formatted as YYYY/MM/DD
                        COL_AMOUNT + " double not null, " +
                        COL_DESCRIPTION + " text, " +
                        COL_CATEGORY + " text, " +
                        COL_LATITUDE + " double, " +
                        COL_LONGITUDE + " double );"
        );
        db.execSQL(
                "CREATE TABLE " + TABLE_PLANNEDPERIODIC + "(" +
                        COL_ID + " integer primary key, " +
                        COL_DATE + " text not null, " +                 // formatted as YYYY/MM/DD
                        COL_AMOUNT + " double not null, " +
                        COL_DESCRIPTION + " text, " +
                        COL_CATEGORY + " text, " +
                        COL_REMINDER + " text not null," +
                        COL_PERIODICITY + " text not null, " +
                        COL_NEXTDEADLINEDATE + " text not null );"     // formatted as YYYY/MM/DD
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANNEDPERIODIC);
        this.onCreate(db);
    }


    protected abstract ContentValues createExpenseCV(Expense expense);

    // NOTE: if date is DD-MM-YYYY it becomes YYYY-MM-DD and vice versa
    protected String formatDate(String unformattedDate) {
        String[] dateParts = unformattedDate.split("/");
        String day = dateParts[0];
        if (day.length() == 1)
            day = "0" + day;

        return String.format("%s/%s/%s", dateParts[2], dateParts[1], day);
    }

    /* CRUD core - start */
    // Create - common
    public void addExpense(Expense expense){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createExpenseCV(expense);
        db.insert(
                getTableName(),
                null,
                values
        );
        db.close();
    }

    // Read - specific
    public abstract List<Expense> getExpenses();
    public abstract List<Expense> getExpenses(int condition);

    // Update - common
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createExpenseCV(expense);

        int numberOfUpdates = db.update(
                getTableName(),                                     // table
                values,                                             // column/value
                COL_ID + " =?",                                     // select rows based on their id
                new String[] { Integer.toString(expense.id) }
        );     // select if id is expense.id
        db.close();
        return numberOfUpdates;
    }

    // Delete
    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                getTableName(),
                COL_ID + " = ?",
                new String[] { Integer.toString(id) }
        );
        db.close();
    }
    /* CRUD core - end */

    public void deleteAllExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("DELETE FROM %s", getTableName()));
    }

    public int getTotalExpenses() {
        int totalExpenses = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format("SELECT COUNT(*) FROM %s", getTableName()), null);

        if (cursor.moveToFirst()) {
            totalExpenses = Integer.parseInt((cursor.getString(0)));
        }

        cursor.close();
        db.close();
        return totalExpenses;
    }

    protected abstract String getTableName();
}

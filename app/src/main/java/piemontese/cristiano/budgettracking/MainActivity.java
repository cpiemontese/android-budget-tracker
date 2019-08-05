package piemontese.cristiano.budgettracking;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpenseDaily;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseDailySQLiteHelper;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpensePlannedPeriodicSQLiteHelper;
import piemontese.cristiano.budgettracking.ManageActivity.ManageDailyActivity;
import piemontese.cristiano.budgettracking.ManageActivity.ManagePlannedAndPeriodicActivity;


public class MainActivity extends Activity {
    protected boolean didNotifyTheUser;
    protected SharedPreferencesWrapper spWrapper;
    protected ExpenseDailySQLiteHelper dbDaily;
    protected ExpensePlannedPeriodicSQLiteHelper dbPlannedPeriodic;

    final static String GROUP_EXPIRED = "expired";
    final static String GROUP_REMINDER = "reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        didNotifyTheUser = false;
        spWrapper = new SharedPreferencesWrapper(getApplicationContext());
        dbDaily = new ExpenseDailySQLiteHelper(getApplicationContext());
        dbPlannedPeriodic = new ExpensePlannedPeriodicSQLiteHelper(getApplicationContext());

        findViewById(R.id.browseExpensesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent browseExpensesIntent = new Intent(MainActivity.this, ManageDailyActivity.class);
                MainActivity.this.startActivity(browseExpensesIntent);
            }
        });

        findViewById(R.id.managePlannedPeriodicBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent managePlannedPeriodicIntent = new Intent(MainActivity.this, ManagePlannedAndPeriodicActivity.class);
                MainActivity.this.startActivity(managePlannedPeriodicIntent);
            }
        });

        findViewById(R.id.statisticsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent expenseStatisticsIntent = new Intent(MainActivity.this, ExpenseStatisticsActivity.class);
                MainActivity.this.startActivity(expenseStatisticsIntent);
            }
        });

        findViewById(R.id.addIncome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder addIncomeDialog = new AlertDialog.Builder(MainActivity.this);
                addIncomeDialog.setCancelable(true);
                addIncomeDialog.setMessage("Add new income");
                addIncomeDialog.setView(R.layout.submit_income);
                addIncomeDialog.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText amount = (EditText) ((AlertDialog) dialog).findViewById(R.id.amount);
                        String text = amount.getText().toString();
                        if (Objects.equals(text, getString(R.string.default_income_string)))
                            Toast.makeText(
                                    MainActivity.this,
                                    "You must insert some value!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        else {
                            spWrapper.updateTotal(Double.parseDouble(text));
                            Toast.makeText(
                                    MainActivity.this,
                                    String.format("Amount added, new budget is: %s", Double.toString(spWrapper.getTotal())),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });

                addIncomeDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                addIncomeDialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (didNotifyTheUser)
            return;

        List<Expense> expenses = dbPlannedPeriodic.getExpenses();

        Expense expense;
        String title;
        List<String> groupExpired = new LinkedList<>(), groupReminder = new LinkedList<>();

        // we set calendar to today at 00:00:00
        Calendar currentDayCalendar = Calendar.getInstance();
        int currentDay = currentDayCalendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = currentDayCalendar.get(Calendar.MONTH) + 1;
        int currentYear = currentDayCalendar.get(Calendar.YEAR);

        int nextDeadlineDay, nextDeadlineMonth, nextDeadlineYear, reminderDay, reminderMonth, reminderYear;
        Calendar nextDeadlineCalendar = Calendar.getInstance();
        for (int i = 0 ; i < expenses.size() ; i++) {
            expense = expenses.get(i);

            String[] dateParts = expense.nextDeadlineDate.split("/");

            nextDeadlineDay = Integer.parseInt(dateParts[0]);
            nextDeadlineMonth = Integer.parseInt(dateParts[1]);
            nextDeadlineYear = Integer.parseInt(dateParts[2]);

            nextDeadlineCalendar.set(nextDeadlineYear, nextDeadlineMonth, nextDeadlineDay);
            nextDeadlineCalendar.add(Calendar.DAY_OF_MONTH, -getNumFromReminder(expense.reminder));
            reminderDay = nextDeadlineCalendar.get(Calendar.DAY_OF_MONTH);
            reminderMonth = nextDeadlineCalendar.get(Calendar.MONTH);
            reminderYear = nextDeadlineCalendar.get(Calendar.YEAR);

            if (currentDay == nextDeadlineDay && currentMonth == nextDeadlineMonth && currentYear == nextDeadlineYear) {
                spWrapper.updateTotal(-expense.amount);
                title = String.format("%s€ payment carried out", Double.toString(expense.amount));

                if (Objects.equals(expense.periodicity, "one time")) {
                    // we won't have to bill the expense again
                    dbPlannedPeriodic.deleteExpense(expense.id);

                    // we create a new daily expense entry
                    ExpenseDaily newExpense = new ExpenseDaily(
                            dbDaily.getTotalExpenses() + 1,
                            expense.date,
                            expense.amount,
                            expense.description,
                            expense.category,
                            -1.0,
                            -1.0
                    );
                    dbDaily.addExpense(newExpense);
                } else {
                    switch (expense.periodicity) {
                        case "yearly":
                            nextDeadlineCalendar.add(Calendar.YEAR, 1);
                            break;
                        case "monthly":
                            nextDeadlineCalendar.add(Calendar.MONTH, 1);
                            break;
                        case "weekly":
                            nextDeadlineCalendar.add(Calendar.DAY_OF_MONTH, 7);
                    }
                    // we reset the calendar
                    nextDeadlineCalendar.set(nextDeadlineYear, nextDeadlineMonth, nextDeadlineDay);
                    expense.nextDeadlineDate = String.format(
                            "%d/%d/%d",
                            nextDeadlineCalendar.get(Calendar.DAY_OF_MONTH),
                            nextDeadlineCalendar.get(Calendar.MONTH) + 1,
                            nextDeadlineCalendar.get(Calendar.YEAR)
                    );
                    dbPlannedPeriodic.updateExpense(expense);
                }
                groupExpired.add(title);
            } else if (currentDay == reminderDay && currentMonth == reminderMonth && currentYear == reminderYear) {
                int daysTill = getNumFromReminder(expense.reminder);
                if (daysTill > 1)
                    title = String.format("%d days till %s€ payment", daysTill, Double.toString(expense.amount));
                else
                    title = String.format("1 day till %s€ payment", Double.toString(expense.amount));

                groupReminder.add(title);
            }
        }
        notifyUser(groupExpired, GROUP_EXPIRED, 0);
        notifyUser(groupReminder, GROUP_REMINDER, 1);
        didNotifyTheUser = true;
    }

    protected int getNumFromReminder(String reminder) {
        switch (reminder) {
            case "1 day before":
                return 1;
            case "2 days before":
                return 2;
            case "7 days before":
                return 7;
        } return 0;
    }

    protected void notifyUser(List<String> texts, String group, int id) {

        int size = texts.size();
        if (size == 0)
            return;

        Intent manageIntent = new Intent(this, ManagePlannedAndPeriodicActivity.class);
        PendingIntent clickIntent =
                PendingIntent.getActivity(
                        this,
                        id,
                        manageIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        String title = "";
        NotificationCompat.InboxStyle myInboxStyle = new NotificationCompat.InboxStyle();
        switch (group) {
            case GROUP_EXPIRED:
                if (size == 1)
                    title = "1 payment deadline expired";
                else
                    title = String.format("%d payment deadlines expired", size);

                myInboxStyle.setSummaryText(String.format("Your total is now %s", Double.toString(spWrapper.getTotal())));
                break;
            case GROUP_REMINDER:
                if (size == 1)
                    title = "You got 1 pending payment";
                else
                    title = String.format("You got %d pending payments", size);
        } myInboxStyle.setBigContentTitle(title);

        for (int i = 0 ; i < size ; i++) {
            myInboxStyle.addLine(texts.get(i));
            Log.d("adding", texts.get(i));
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.alert_light_frame)
                .setStyle(myInboxStyle)
                .setContentTitle(title)
                .setGroup(group)
                .setGroupSummary(true)
                .setContentIntent(clickIntent)
                .setAutoCancel(true)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}
package piemontese.cristiano.budgettracking.Expense;

public class ExpensePlannedPeriodic  extends Expense {

    public ExpensePlannedPeriodic(
            int id, String date, double amount, String description,
            String category, String reminder, String periodicity, String nextDeadlineDate) {
        super(id, date, amount, description, category);
        this.reminder = reminder;
        this.periodicity = periodicity;
        this.nextDeadlineDate = nextDeadlineDate;
    }
}

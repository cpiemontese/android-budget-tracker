package piemontese.cristiano.budgettracking.Expense;

public class ExpenseDaily extends Expense {

    public ExpenseDaily(int id, String date, double amount, String description, String category, double lat, double lng) {
        super(id, date, amount, description, category);
        this.latitude = lat;
        this.longitude = lng;
    }
}

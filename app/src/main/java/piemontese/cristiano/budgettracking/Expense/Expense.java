package piemontese.cristiano.budgettracking.Expense;

import java.io.Serializable;

/**
 * Generic Expense class
 */

public class Expense implements Serializable {
    public int id;
    public double amount;
    public String date;
    public String description;
    public String category;

    public double latitude;
    public double longitude;

    public String reminder;
    public String periodicity;
    public String nextDeadlineDate;

    public Expense(int id, String date, double amount, String description, String category) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }
}

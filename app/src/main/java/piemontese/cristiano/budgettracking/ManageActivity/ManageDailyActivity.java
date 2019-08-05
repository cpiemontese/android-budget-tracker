package piemontese.cristiano.budgettracking.ManageActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseDailySQLiteHelper;
import piemontese.cristiano.budgettracking.R;
import piemontese.cristiano.budgettracking.SharedPreferencesWrapper;
import piemontese.cristiano.budgettracking.SubmitActivity.SubmitDailyActivity;

public class ManageDailyActivity extends ManageGenericActivity {
    private int spinnerChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.manage_daily);

        db = new ExpenseDailySQLiteHelper(getApplicationContext());
        spWrapper = new SharedPreferencesWrapper(getApplicationContext());
        spinnerChoice = 0;

        findViewById(R.id.createPDFBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ManageDailyActivity.this.createPdf();
                    Toast.makeText(getApplicationContext(), "Document saved in pdf_reports", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.addExpenseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addExpenseIntent = new Intent(ManageDailyActivity.this, SubmitDailyActivity.class);
                ManageDailyActivity.this.startActivity(addExpenseIntent);
            }
        });

        setupSpinner();
    }

    @Override
    protected void modifyExpense(Expense expenseToModify) {
        Intent modifyExpenseIntent = new Intent(ManageDailyActivity.this, SubmitDailyActivity.class);
        modifyExpenseIntent.putExtra("modified expense id", expenseToModify.id);
        modifyExpenseIntent.putExtra("expense", expenseToModify);
        startActivity(modifyExpenseIntent);
    }

    @Override
    protected void removeExpense(Expense expenseToRemove) {
        ManageDailyActivity.this.db.deleteExpense(expenseToRemove.id);
        spWrapper.updateTotal(expenseToRemove.amount);
        updateList();
    }

    @Override
    protected void showLocation(Expense expenseToShow) {
        if (expenseToShow.latitude == -1.0 && expenseToShow.longitude == -1.0) {
            Toast.makeText(getApplicationContext(), "No location provided", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("geo:" + expenseToShow.latitude + "," + expenseToShow.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    protected List<Expense> getExpenses() {
        return db.getExpenses(this.spinnerChoice);
    }

    @Override
    protected RelativeLayout setupExpenseEntryParent(Expense expense) {
        RelativeLayout expenseEntryParent = (RelativeLayout) getLayoutInflater().inflate(R.layout.expenses_list_entry_daily, null);

        TextView amountTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemAmount);
        TextView dateTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemDate);
        TextView descriptionTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemDescription);
        TextView categoryTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemCategory);
        TextView locationTextView = (TextView) expenseEntryParent.findViewById(R.id.expenseListItemLocation);

        amountTextView.setText("Amount: " + Double.toString(expense.amount) + "€");
        dateTextView.setText("Date: ".concat(expense.date));

        if (!expense.description.isEmpty())
            descriptionTextView.setText("Description: ".concat(expense.description));

        if (!expense.category.isEmpty())
            categoryTextView.setText("Category: ".concat(expense.category));

        Geocoder gc = new Geocoder(getApplicationContext());
        List<Address> addresses = null;
        try {
            addresses = gc.getFromLocation(expense.latitude, expense.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert addresses != null;
        if (addresses.size() != 0) {
            String locationAddress = "";
            Address address = addresses.get(0);
            for (int j = 0 ; j < address.getMaxAddressLineIndex() ; j++)
                locationAddress = locationAddress.concat(address.getAddressLine(j));

            locationTextView.setText(getString(R.string.browseExpensesBoughtAt).concat(locationAddress));
        }

        return expenseEntryParent;
    }

    @Override
    protected String createPDFTitle() {
        switch(spinnerChoice) {
            case 0:                 // i.e. all expenses
                return "Report of all expenses";
            case 1:                 // i.e. last week
                return "Report of last week's expenses";
            case 2:
                return "Report of last month's expenses";
        } return "";
    }

    @Override
    protected String createPDFTag() {
        switch(spinnerChoice) {
            case 0:
                return "all";
            case 1:
                return "lw";
            case 2:
                return "lm";
        } return "";
    }

    protected void setupSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.orderTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.order_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ManageDailyActivity.this.spinnerChoice = position;
                ManageDailyActivity.this.updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }
}

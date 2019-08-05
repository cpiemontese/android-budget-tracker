package piemontese.cristiano.budgettracking.ManageActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseSQLiteHelper;
import piemontese.cristiano.budgettracking.R;
import piemontese.cristiano.budgettracking.SharedPreferencesWrapper;

public abstract class ManageGenericActivity extends Activity implements View.OnLongClickListener {
    protected ExpenseSQLiteHelper db;
    protected SharedPreferencesWrapper spWrapper;
    protected List<Expense> expensesList;
    protected CharSequence[] items = { "Show location", "Modify expense", "Remove expense" };

    @Override
    public boolean onLongClick(View v)  {
        LinearLayout parent = (LinearLayout) v.getParent();
        int index = parent.indexOfChild(v);
        final Expense clickedViewExpense = expensesList.get(index);

        AlertDialog.Builder builder = new AlertDialog.Builder(ManageGenericActivity.this);
        builder.setTitle("Choose an action");
        builder.setItems(this.items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedItem = (String) items[item];
                switch (selectedItem) {
                    case "Modify expense":
                        ManageGenericActivity.this.modifyExpense(clickedViewExpense);
                        break;
                    case "Remove expense":
                        ManageGenericActivity.this.removeExpense(clickedViewExpense);
                        break;
                    case "Show location":
                        ManageGenericActivity.this.showLocation(clickedViewExpense);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        updateList();
    }

    protected abstract void modifyExpense(Expense expenseToModify);

    protected abstract void removeExpense(Expense expenseToRemove);

    protected abstract void showLocation(Expense expenseToShow);

    protected void updateList() {
        expensesList = getExpenses();

        // we update the remaining budget TextView
        TextView remainingBudgetTextView = (TextView) findViewById(R.id.remainingBudgetTextView);

        double total = spWrapper.getTotal();
        remainingBudgetTextView.setText(getString(R.string.browseExpensesRemaningBudget).concat(" " + Double.toString(total) + "€"));

        LinearLayout parentLinearLayout = (LinearLayout) findViewById(R.id.expensesListContainer);
        parentLinearLayout.removeAllViews();    // we need to remove the data we previously added

        Expense currentExpense;
        for (int i = 0; i < expensesList.size(); i++) {
            currentExpense = expensesList.get(i);

            RelativeLayout expenseEntryParent = setupExpenseEntryParent(currentExpense);
            parentLinearLayout.addView(expenseEntryParent);
            expenseEntryParent.setOnLongClickListener(ManageGenericActivity.this);
        }
    }

    protected abstract List<Expense> getExpenses();

    protected abstract RelativeLayout setupExpenseEntryParent(Expense expense);

    protected void createPdf() throws FileNotFoundException, DocumentException {
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), "pdf_reports");
        if (!pdfFolder.exists())
            pdfFolder.mkdir();

        // we add a date and a selection to the name to differentiate reports
        String pdfTitle = createPDFTitle();
        String pdfTag = createPDFTag();

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        String date = String.format("%d_%d_%d", day, month, year);

        // we create the new file and we obtain a stream from it
        File newPdfFile = new File(String.format("%s/%s_%s_report.pdf", pdfFolder, date, pdfTag));
        OutputStream output = new FileOutputStream(newPdfFile);

        // we create a new document, open it, fill it with the expenses and then we close it and send a toast to the user
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();

        Paragraph title = new Paragraph(pdfTitle);
        title.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(((TextView) findViewById(R.id.remainingBudgetTextView)).getText().toString()));
        document.add(new Paragraph("\n"));

        LinearLayout expensesListContainer = (LinearLayout) findViewById(R.id.expensesListContainer);
        for (int i = 0 ; i < expensesListContainer.getChildCount() ; i++) {
            RelativeLayout expenseEntryParent = (RelativeLayout) expensesListContainer.getChildAt(i);
            document.add(new Paragraph(((TextView) expenseEntryParent.findViewById(R.id.expenseListItemAmount)).getText().toString()));
            document.add(new Paragraph(((TextView) expenseEntryParent.findViewById(R.id.expenseListItemDate)).getText().toString()));
            document.add(new Paragraph(((TextView) expenseEntryParent.findViewById(R.id.expenseListItemCategory)).getText().toString()));
            document.add(new Paragraph(((TextView) expenseEntryParent.findViewById(R.id.expenseListItemDescription)).getText().toString()));
            document.add(new Paragraph(((TextView) expenseEntryParent.findViewById(R.id.expenseListItemLocation)).getText().toString()));
            document.add(new Paragraph("\n"));
        }

        //Step 5: Close the document
        document.close();
    }

    protected abstract String createPDFTitle();

    protected abstract String createPDFTag();
}

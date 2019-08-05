package piemontese.cristiano.budgettracking;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseDailySQLiteHelper;


public class ExpenseStatisticsActivity extends Activity {
    protected final static int DAY = 0;
    protected final static int WEEK = 1;
    protected final static int MONTH = 2;
    protected final static int YEAR = 3;

    protected final static int GENERAL = 0;

    GraphView graph;
    StaticLabelsFormatter staticLabelsFormatter;

    List<Expense> expenses;
    protected int currentPeriod = DAY;
    protected int currentCategory = GENERAL;
    protected ExpenseDailySQLiteHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.expense_statistics);

        graph = (GraphView) findViewById(R.id.expenseGraph);
        staticLabelsFormatter = new StaticLabelsFormatter(graph);

        db = new ExpenseDailySQLiteHelper(getApplicationContext());
        expenses = db.getExpenses(3);   // get all daily expenses
        setupSpinner();

        String date = "";
        Expense expense;
        int size = expenses.size();
        double max = 0.0, avg = 0.0;
        for (int i = 0 ; i < size ; i++) {
            expense = expenses.get(i);
            avg += expense.amount;

            if (expense.amount > max) {
                max = expense.amount;
                date = expense.date;
            }
        }
        if (avg != 0.0)
            ((TextView) findViewById(R.id.avgAmount)).setText(Double.toString(avg/size).substring(0, 7) + "€");

        if (max != 0.0)
            ((TextView) findViewById(R.id.highestAmount)).setText(String.format("%s€ on %s", Double.toString(max), date));
    }

    protected void setupSpinner() {
        Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == GENERAL)
                    findViewById(R.id.periodSpinner).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.periodSpinner).setVisibility(View.GONE);

                ExpenseStatisticsActivity.this.currentCategory = position;
                updateStatisticsGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        Spinner periodSpinner = (Spinner) findViewById(R.id.periodSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.periods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ExpenseStatisticsActivity.this.currentPeriod = position;
                updateStatisticsGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void updateStatisticsGraph() {
        int i;
        String key = "", errKey;
        Map<String, Double> quantitiesDict = new LinkedHashMap<>();
        List<String> keys = new LinkedList<>();

        graph.removeAllSeries();

        for (i = 0 ; i < expenses.size() ; i++) {
            if (currentCategory == GENERAL) {
                switch (currentPeriod) {
                    case DAY:
                        key = expenses.get(i).date;
                        break;
                    case WEEK:
                        key = getKey(expenses.get(i).date, "week");
                        break;
                    case MONTH:
                        key = getKey(expenses.get(i).date, "month");
                        break;
                    case YEAR:
                        key = getKey(expenses.get(i).date, "year");
                } errKey = "No date";
            } else {
                key = expenses.get(i).category;
                errKey = "N/A";
            }

            if(key.isEmpty())
                key = errKey;

            if (!keys.contains(key))
                keys.add(key);

            if (quantitiesDict.containsKey(key))
                quantitiesDict.put(key, quantitiesDict.get(key) + expenses.get(i).amount);
            else
                quantitiesDict.put(key, expenses.get(i).amount);
        }

        if (keys.size() < 2) {
            Toast.makeText(ExpenseStatisticsActivity.this, "Not enough data to plot", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] keysArray = new String[keys.size()];
        DataPoint[] dataPoints = new DataPoint[keys.size()];
        for (i = 0 ; i < keys.size() ; i++) {
            if (currentCategory == GENERAL && currentPeriod == DAY)
                keysArray[i] = "";
            else
                keysArray[i] = keys.get(i);
            dataPoints[i] = new DataPoint(i, quantitiesDict.get(keys.get(i)));
        }

        staticLabelsFormatter.setHorizontalLabels(keysArray);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        if (currentCategory == GENERAL) {
            LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(dataPoints);
            graph.addSeries(lineSeries);
        } else {
            BarGraphSeries<DataPoint> barSeries = new BarGraphSeries<>(dataPoints);
            graph.addSeries(barSeries);

            barSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.rgb((int) data.getX()*255/2, (int) Math.abs(data.getY()*255/2), 100);
                }
            });

            barSeries.setSpacing(65);
            barSeries.setDrawValuesOnTop(true);
            barSeries.setValuesOnTopColor(Color.BLACK);
        }
    }

    protected String getKey(String date, String type) {
        String key = "";
        String[] dateParts = date.split("/");
        switch (type) {
            case "week":
                Calendar c = Calendar.getInstance();
                int day = Integer.parseInt(dateParts[0]);
                c.set(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]), day);
                int firstDay = c.getFirstDayOfWeek();
                while (firstDay + 7 <= day)
                    firstDay += 7;

                key = String.format("%d/%s -\n %d/%s", firstDay, dateParts[1], firstDay + 7, dateParts[1]);
                break;
            case "month":
                key = String.format("%s/%d", dateParts[1], Integer.parseInt(dateParts[2]) - 2000);
                break;
            case "year":
                key = dateParts[2];
        } return key;
    }
}

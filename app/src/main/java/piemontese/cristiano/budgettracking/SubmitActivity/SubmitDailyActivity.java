package piemontese.cristiano.budgettracking.SubmitActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import piemontese.cristiano.budgettracking.Expense.Expense;
import piemontese.cristiano.budgettracking.Expense.ExpenseDaily;
import piemontese.cristiano.budgettracking.ExpenseDBSQLiteHelper.ExpenseDailySQLiteHelper;
import piemontese.cristiano.budgettracking.R;

public class SubmitDailyActivity extends SubmitGenericActivity {
    private final int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder placePickerIntentBuilder;
    protected LatLng coordinates;

    protected void setLayout() {
        setContentView(R.layout.submit_daily);
    }

    @Override
    protected void initializeSubmitterForUpdate(Expense expense) {
        coordinates = new LatLng(expense.latitude, expense.longitude);
    }

    @Override
    protected void activitySpecificInitialization() {
        coordinates = new LatLng(-1.0, -1.0);
        placePickerIntentBuilder = new PlacePicker.IntentBuilder();
        db = new ExpenseDailySQLiteHelper(getApplicationContext());
    }

    @Override
    protected void setOnClickListeners() {
        setSubmitExpenseOnClickListener();
        setPlacePickerOnClickListener();
    }

    @Override
    protected Expense createNewExpense(int id, double amount, String date, String description, String category) {
        return new ExpenseDaily(
                id,
                date,
                amount,
                description,
                category,
                SubmitDailyActivity.this.coordinates.latitude,
                SubmitDailyActivity.this.coordinates.longitude
        );
    }

    protected void setPlacePickerOnClickListener() {
        Button placePickerBtn = (Button) findViewById(R.id.placePicker);
        placePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(placePickerIntentBuilder.build(SubmitDailyActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.google_play_not_available_errormsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected String getDate() {
        DatePicker dp = ((DatePicker) findViewById(R.id.datePicker));
        return String.format("%d/%d/%d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                SubmitDailyActivity.this.coordinates = place.getLatLng();
                Log.d("lat", Double.toString(place.getLatLng().latitude));
                Log.d("lng", Double.toString(place.getLatLng().longitude));
                String toastMsg = String.format("You picked: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
}

package piemontese.cristiano.budgettracking.SubmitActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

import piemontese.cristiano.budgettracking.R;

/**
 * Activity with common functionalities between SubmitPlanned and SubmitPeriodic
 */

public abstract class SubmitPeriodicPlannedCommonActivity extends SubmitGenericActivity {
    protected String reminder = "1 day before";

    @Override
    protected void setOnClickListeners() {
        setSubmitExpenseOnClickListener();
        setRemainderOnClickListener();
    }

    protected void setRemainderOnClickListener() {
        findViewById(R.id.setRemainderBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"1 day before", "2 days before", "7 days before"};

                AlertDialog.Builder chooseRemainderDialogBuilder = new AlertDialog.Builder(SubmitPeriodicPlannedCommonActivity.this);
                chooseRemainderDialogBuilder.setSingleChoiceItems(items, 0, null)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                SubmitPeriodicPlannedCommonActivity.this.reminder =
                                        items[((AlertDialog) dialog).getListView().getCheckedItemPosition()];
                            }
                        }).show();
            }
        });
    }

    @Override
    protected boolean isPlannedOrPeriodic() {
        return true;
    }

    @Override
    protected String getDate() {
        DatePicker dp = ((DatePicker) findViewById(R.id.datePicker));
        return String.format("%d/%d/%d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear());
    }

    protected void setMinDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
        dp.setMinDate(c.getTimeInMillis());
    }
}

package piemontese.cristiano.budgettracking;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesWrapper {
    Context spContext;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public SharedPreferencesWrapper(Context context) {
        spContext = context;
        sp = context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public double getTotal() {
        return Double.longBitsToDouble(sp.getLong(spContext.getString(R.string.shared_preference_total_key), Double.doubleToLongBits(0.0))); // default budget value is 0.0
    }

    public double updateTotal(double amount) {
        double oldTotal = getTotal();
        double newTotal = oldTotal + amount;

        editor.putLong(spContext.getString(R.string.shared_preference_total_key), Double.doubleToRawLongBits(newTotal));
        editor.clear().apply();

        return newTotal;
    }

}

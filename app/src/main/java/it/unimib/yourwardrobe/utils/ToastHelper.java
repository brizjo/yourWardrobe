package it.unimib.yourwardrobe.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    private static Toast currentToast;

    // Metodo statico accessibile da ovunque
    public static void show(Context context, String message, boolean isLong) {
        if (context == null) return;

        // Se c'è un toast precedente visibile, cancellalo
        if (currentToast != null) {
            currentToast.cancel();
        }

        // Crea e mostra il nuovo toast
        int duration = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        currentToast = Toast.makeText(context, message, duration);
        currentToast.show();
    }
}


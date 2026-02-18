package it.unimib.yourwardrobe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.view.View;
import android.widget.TextView;

public class NetworkBannerHelper {

    public static void observe(Context context, TextView banner) {
        if (banner == null) return;

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return;

        // Controlla subito lo stato attuale
        boolean connected = isConnected(cm);
        banner.post(() -> banner.setVisibility(connected ? View.GONE : View.VISIBLE));

        // Osserva i cambiamenti futuri
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                banner.post(() -> banner.setVisibility(View.GONE));
            }

            @Override
            public void onLost(Network network) {
                banner.post(() -> banner.setVisibility(View.VISIBLE));
            }
        });
    }

    private static boolean isConnected(ConnectivityManager cm) {
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }
}
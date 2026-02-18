package it.unimib.yourwardrobe.ui.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.view.View;
import android.widget.TextView;

public class NetworkBannerHelper {

    private static final long BANNER_VISIBLE_MS = 15_000;
    private static final long FADE_IN_MS = 300;
    private static final long FADE_OUT_MS = 500;

    // Runnable salvato per poterlo cancellare se la connessione torna prima dei 15s
    private static Runnable pendingHide;

    public static void observe(Context context, TextView banner) {
        if (banner == null) return;

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        // Controlla subito lo stato attuale
        boolean connected = isConnected(cm);
        banner.post(() -> {
            if (connected) hideBanner(banner);
            else showBanner(banner);
        });

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                banner.post(() -> hideBanner(banner));
            }

            @Override
            public void onLost(Network network) {
                banner.post(() -> showBanner(banner));
            }
        });
    }

    // -------------------------------------------------------------------------

    private static void showBanner(TextView banner) {
        // Cancella fade-out e auto-hide pendenti
        banner.animate().cancel();
        if (pendingHide != null) {
            banner.removeCallbacks(pendingHide);
            pendingHide = null;
        }

        banner.setAlpha(0f);
        banner.setVisibility(View.VISIBLE);
        banner.animate()
                .alpha(1f)
                .setDuration(FADE_IN_MS)
                .setListener(null)
                .start();

        // Auto-hide dopo 15 secondi
        pendingHide = () -> hideBanner(banner);
        banner.postDelayed(pendingHide, BANNER_VISIBLE_MS);
    }

    private static void hideBanner(TextView banner) {
        // Cancella auto-hide pendente se nascosto prima (es. connessione tornata)
        if (pendingHide != null) {
            banner.removeCallbacks(pendingHide);
            pendingHide = null;
        }

        banner.animate().cancel();
        banner.animate()
                .alpha(0f)
                .setDuration(FADE_OUT_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        banner.setVisibility(View.GONE);
                    }
                })
                .start();
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
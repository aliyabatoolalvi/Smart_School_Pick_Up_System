package com.finallab.smartschoolpickupsystem;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
public class Utilities {

    // Check if the device is connected to the internet
    public static boolean isNetworkConnected(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }

    // Show a Snackbar when there's no internet connection
    public static void showNotConnectedSnack(View view, Context context) {
        Snackbar snackbar = Snackbar.make(view, "No internet connection", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Open Settings", v -> context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)));
        snackbar.setActionTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        snackbar.show();
    }

    // Show an error message with a Snackbar
    public static void showErrorSnack(View view, Context context, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", v -> {});
        snackbar.setActionTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        snackbar.show();
    }
}

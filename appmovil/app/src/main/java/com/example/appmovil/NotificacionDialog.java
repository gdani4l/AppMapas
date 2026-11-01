package com.example.appmovil;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class NotificacionDialog {

    public static void mostrar(
            Context context,
            String titulo,
            String mensaje,
            boolean esExito,
            Runnable alAceptar
    ) {
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.dialog_notificacion, null);

        TextView tvIcono = vista.findViewById(R.id.tvIconoNotificacion);
        TextView tvTitulo = vista.findViewById(R.id.tvTituloNotificacion);
        TextView tvMensaje = vista.findViewById(R.id.tvMensajeNotificacion);
        MaterialButton btnAceptar = vista.findViewById(
                R.id.btnAceptarNotificacion
        );

        tvIcono.setText(esExito ? "✓" : "!");
        tvIcono.setTextColor(Color.parseColor(
                esExito ? "#0D9488" : "#DC2626"
        ));

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(vista)
                .setCancelable(false)
                .create();

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();

            if (alAceptar != null) {
                alAceptar.run();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }
    }
}
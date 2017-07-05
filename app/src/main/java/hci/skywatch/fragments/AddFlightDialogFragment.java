package hci.skywatch.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hci.skywatch.R;

public class AddFlightDialogFragment extends DialogFragment {

    private OnFlightAddedListener onFlightAddedListener;

    public interface OnFlightAddedListener {
        void onFlightAdded(String airline, String flightNumber);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                onFlightAddedListener = (OnFlightAddedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement onFlightAddedListener");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.fragment_dialog_add_flight, null);
        builder.setView(rootView);
        builder.setMessage(R.string.add_flight_dialog_title);
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // empty, overridden below in onResume
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();

        EditText airline = (EditText) rootView.findViewById(R.id.airline);
        if (airline != null) {
            airline.requestFocus();
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null) {
            final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText airline = (EditText) dialog.findViewById(R.id.airline);
                    EditText flightNumber = (EditText) dialog.findViewById(R.id.flight_number);
                    if (airline != null && flightNumber != null) {
                        Editable airlineText = airline.getText();
                        Editable flightNumberText = flightNumber.getText();
                        // if airline or flight number is empty, show error msg
                        if (airlineText.toString().equals("")) {
                            Toast.makeText(getContext(), R.string.empty_airline, Toast.LENGTH_SHORT).show();
                            airline.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, null), PorterDuff.Mode.SRC_ATOP);
                            return;
                        }
                        if (flightNumberText.toString().equals("")) {
                            Toast.makeText(getContext(), R.string.empty_flight_number, Toast.LENGTH_SHORT).show();
                            flightNumber.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, null), PorterDuff.Mode.SRC_ATOP);
                            return;
                        }

                        onFlightAddedListener.onFlightAdded(airlineText.toString(), flightNumberText.toString());
                        dialog.dismiss();
                    }
                }
            });

        }
    }

}

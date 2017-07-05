package hci.skywatch.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hci.skywatch.R;

public class SearchDealsDialogFragment extends DialogFragment {

    private OnSearchListener listener;

    public interface OnSearchListener {
        void onSearch(String departure);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                listener = (SearchDealsDialogFragment.OnSearchListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement onSearch");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.fragment_dialog_search_deals, null);
        builder.setView(rootView);
        builder.setMessage(R.string.search_deals_dialog_msg);
        builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();

        EditText location = (EditText) rootView.findViewById(R.id.location);
        if (location != null) {
            location.requestFocus();
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
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText location = (EditText) dialog.findViewById(R.id.location);
                    if (location != null) {
                        String result = location.getText().toString();
                        if (result.equals("")) {
                            Toast.makeText(getContext(), R.string.empty_departure_location, Toast.LENGTH_SHORT).show();
                            location.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, null), PorterDuff.Mode.SRC_ATOP);
                            return;
                        }

                        listener.onSearch(result.toUpperCase());
                        dialog.dismiss();
                    }
                }
            });
        }
    }
}

package hci.skywatch.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import hci.skywatch.R;
import hci.skywatch.model.Airline;
import hci.skywatch.model.Flight;
import hci.skywatch.model.FlightStatus;

/**
 * ViewHolder capable of presenting two states: "normal" and "undo" state.
 * Source: http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary
 */
public class FlightViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    NetworkImageView airlineLogoImageView;
    TextView nameTextView;
    TextView fromToTextView;
    TextView dateTextView;
    TextView statusTextView;
    Button undoButton;
    TextView deleted;

    private ImageLoader imageLoader;
    private Callback callback;

    public interface Callback {
        void onItemClick(int position, boolean longClick);
    }

    public FlightViewHolder(View view, ImageLoader imageLoader, Callback callback) {
        super(view);
        airlineLogoImageView = (NetworkImageView) itemView.findViewById(R.id.airline_logo);
        nameTextView = (TextView) itemView.findViewById(R.id.flight_name);
        fromToTextView = (TextView) itemView.findViewById(R.id.from_to);
        dateTextView = (TextView) itemView.findViewById(R.id.flight_date);
        statusTextView = (TextView) itemView.findViewById(R.id.flight_status);
        undoButton = (Button) itemView.findViewById(R.id.undo_button);
        deleted = (TextView) itemView.findViewById(R.id.action);

        this.imageLoader = imageLoader;
        this.callback = callback;
    }

    //show/hide all items but the undo button
    public void setVisibilityOfItems(int visibility) {
        if (airlineLogoImageView != null) airlineLogoImageView.setVisibility(visibility);
        if (nameTextView != null) nameTextView.setVisibility(visibility);
        if (fromToTextView != null) fromToTextView.setVisibility(visibility);
        if (dateTextView != null) dateTextView.setVisibility(visibility);
        if (statusTextView != null) statusTextView.setVisibility(visibility);
    }

    public void setVisibleItem(Flight flight) {
        Airline airline = flight.getAirline();
        if (airlineLogoImageView != null) {
            airlineLogoImageView.setDefaultImageResId(R.mipmap.ic_launcher);
            airlineLogoImageView.setImageUrl(airline.getLogoUrl(), imageLoader);
        }
        if (nameTextView != null)
            nameTextView.setText(airline.getId() + "" + flight.getNumber());
        if (fromToTextView != null) fromToTextView.setText(flight.getFromTo());
        if (dateTextView != null)
            dateTextView.setText(flight.getDeparture().getScheduledTime());
        if (statusTextView != null) {
            FlightStatus status = FlightStatus.getStatusById(flight.getStatus());
            statusTextView.setText(status.stringResourceId);
            //statusTextView.setBackgroundColor(status.color);
            statusTextView.setTextColor(status.color);
        }
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        callback.onItemClick(position, false);
    }

    @Override
    public boolean onLongClick(View v) {
        int position = getAdapterPosition();
        callback.onItemClick(position, true);
        return true;
    }
}

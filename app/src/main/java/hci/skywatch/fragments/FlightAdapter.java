package hci.skywatch.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hci.skywatch.MainActivity;
import hci.skywatch.R;
import hci.skywatch.model.Airline;
import hci.skywatch.model.Flight;
import hci.skywatch.model.FlightStatus;
import hci.skywatch.network.DataBase;
import hci.skywatch.network.RequestManager;

/**
 * RecyclerView adapter enabling undo on a swiped away item.
 * Source: http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary
 */
public class FlightAdapter extends RecyclerView.Adapter implements Filterable {

    private static ImageLoader imageLoader;

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    private List<Flight> items;
    private List<Flight> itemsPendingRemoval;
    private List<Flight> mFilteredList;    // list used for searching

    private boolean undoOn = true; // is undo on, you can turn it on with the setUndoOn method

    private Handler handler = new Handler(); // handler for running delayed runnables
    private HashMap<Flight, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    private OnClickListener listener;

    private OnFlightRemovedListener onFlightRemovedListener;

    public Flight getFlightAt(int position) {
        Flight flight;
        if (mFilteredList != items) {
            flight = mFilteredList.get(position);
        } else {
            flight = items.get(position);
        }

        return flight;
    }

    public interface OnFlightRemovedListener {
        public void onFlightRemoved(Flight flight);
    }

    public FlightAdapter(Context context, @NonNull List<Flight> items, OnClickListener listener) {
        this.items = items;
        this.mFilteredList = items;
        this.listener = listener;
        itemsPendingRemoval = new ArrayList<>();

        if (context instanceof Activity) {
            try {
                onFlightRemovedListener = (OnFlightRemovedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnFlightRemovedListener");
            }
        }
        imageLoader = RequestManager.getInstance(context).getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (MainActivity.detailsView) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.details_view_flight, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_flight, viewGroup, false);
        }

        final FlightViewHolder viewHolder = new FlightAdapter.FlightViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                if (MainActivity.dualPane) {
                    notifyItemChanged(selectedPosition);
                }
                selectedPosition = position;
                if (MainActivity.dualPane) {
                    notifyItemChanged(selectedPosition);
                }
                listener.onClick(position);
            }
        });

        return viewHolder;
    }

    private static int selectedPosition = RecyclerView.NO_POSITION;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        FlightViewHolder viewHolder = (FlightViewHolder) holder;
        final Flight item = mFilteredList.get(position);

        if (itemsPendingRemoval.contains(item)) {
            // we need to show the "undo" state of the row
            viewHolder.itemView.setBackgroundColor(Color.parseColor(MyFlightsFragment.color_red));
            viewHolder.setVisibilityOfItems(View.GONE);
            viewHolder.undoButton.setVisibility(View.VISIBLE);
            viewHolder.deleted.setVisibility(View.VISIBLE);
            viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                    pendingRunnables.remove(item);
                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(item);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(items.indexOf(item));
                }
            });
        } else {
            // we need to show the "normal" state
            viewHolder.setVisibilityOfItems(View.VISIBLE);
            viewHolder.setVisibleItem(item);
            viewHolder.deleted.setVisibility(View.GONE);
            viewHolder.undoButton.setVisibility(View.GONE);
            viewHolder.undoButton.setOnClickListener(null);

            if (MainActivity.dualPane) {
                if (selectedPosition == position) {
                    viewHolder.itemView.setBackgroundColor(MyFlightsFragment.HIGHLIGHTED_COLOR);
                } else {
                    viewHolder.itemView.setBackgroundColor(Color.WHITE);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    public void setUndoOn(boolean undoOn) {
        this.undoOn = undoOn;
    }

    public boolean isUndoOn() {
        return undoOn;
    }

    public void pendingRemoval(int position) {
        final Flight item = items.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(items.indexOf(item));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        Flight item = items.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (items.contains(item)) {
            Flight flight = items.remove(position);
            DataBase.getInstance().removeFlight(flight);
            onFlightRemovedListener.onFlightRemoved(flight);
            mFilteredList = items;
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        Flight item = items.get(position);
        return itemsPendingRemoval.contains(item);
    }

    public interface OnClickListener {
        void onClick(int position);
    }

    /**
     * ViewHolder capable of presenting two states: "normal" and "undo" state.
     * Source: http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary
     */
    public static class FlightViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView airlineLogoImageView;
        TextView nameTextView;
        TextView fromToTextView;
        TextView dateTextView;
        TextView statusTextView;
        Button undoButton;
        TextView deleted;

        public FlightViewHolder(View view) {
            super(view);
            airlineLogoImageView = (NetworkImageView) itemView.findViewById(R.id.airline_logo);
            nameTextView = (TextView) itemView.findViewById(R.id.flight_name);
            fromToTextView = (TextView) itemView.findViewById(R.id.from_to);
            dateTextView = (TextView) itemView.findViewById(R.id.flight_date);
            statusTextView = (TextView) itemView.findViewById(R.id.flight_status);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            deleted = (TextView) itemView.findViewById(R.id.action);
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
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mFilteredList = items;
                } else {
                    ArrayList<Flight> filteredList = new ArrayList<>();
                    charString = charString.toUpperCase();

                    for (Flight flight : items) {
                        if (flight.getName().contains(charString) || flight.getDeparture().getAirport().getId().contains(charString)) {
                            filteredList.add(flight);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Flight>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
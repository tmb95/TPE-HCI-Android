package hci.skywatch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import hci.skywatch.MainActivity;
import hci.skywatch.R;
import hci.skywatch.fragments.MyFlightsFragment;
import hci.skywatch.model.Flight;
import hci.skywatch.network.RequestManager;

public class FlightSearchAdapter extends SearchAdapter<Flight> {
    private FlightViewHolder.Callback callback;
    private ImageLoader imageLoader;

    public FlightSearchAdapter(Context context, List<Flight> itemList, FlightViewHolder.Callback callback) {
        super(itemList);
        this.callback = callback;
        imageLoader = RequestManager.getInstance(context).getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (MyFlightsFragment.detailsView) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.details_view_flight, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_flight, viewGroup, false);
        }

        final FlightViewHolder viewHolder = new FlightViewHolder(view, imageLoader);
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
                callback.onItemClick(position, false);
            }
        });
        viewHolder.itemView.setLongClickable(true);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = viewHolder.getAdapterPosition();
                callback.onItemClick(position, true);
                return true;
            }
        });

        return viewHolder;
    }

    private static int selectedPosition = RecyclerView.NO_POSITION;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        FlightViewHolder viewHolder = (FlightViewHolder) holder;
        final Flight item = filteredList.get(position);

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

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public Flight getFlightAt(int position) {
        Flight flight;
        if (filteredList != itemList) {
            flight = filteredList.get(position);
        } else {
            flight = itemList.get(position);
        }

        return flight;
    }
}

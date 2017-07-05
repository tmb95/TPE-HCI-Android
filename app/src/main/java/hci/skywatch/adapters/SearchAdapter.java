package hci.skywatch.adapters;

import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchAdapter<T extends SearchAdapter.FilterableItem> extends RecyclerView.Adapter implements Filterable {

    protected List<T> itemList;
    protected List<T> filteredList;

    public interface FilterableItem {
        boolean contains(String query);
    }

    public SearchAdapter(List<T> itemList) {
        this.itemList = new ArrayList<>(itemList);
        this.filteredList = itemList;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredList = itemList;
                } else {
                    ArrayList<T> list = new ArrayList<>();
                    charString = charString.toUpperCase();

                    for (T item : itemList) {
                        if (item.contains(charString)) {
                            list.add(item);
                        }
                    }

                    filteredList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (ArrayList<T>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Reset search with new items, its the same as creating a new adapter.
     * IDK what its better
     */
    public void setItems(List<T> items) {
        itemList = new ArrayList<>(items);
        filteredList = itemList;
    }

}

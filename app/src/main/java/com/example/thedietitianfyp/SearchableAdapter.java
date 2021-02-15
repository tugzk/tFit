package com.example.thedietitianfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private List<String> originalData ;
    private List<String> filteredFood = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();


    public SearchableAdapter(Context context, List<String> data) {
        this.filteredFood = data;
        this.originalData = data;

        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredFood.size();
    }

    public Object getItem(int position) {
        return filteredFood.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.search_food_list, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
//            holder.list = (ListView) convertView.findViewById(R.id.listViewFoods);
//            holder.brand = (TextView) convertView.findViewById(R.id.textViewSub);
            holder.text = (TextView) convertView.findViewById(R.id.textViewSearchName);
            // Bind the data efficiently with the holder.
            convertView.setTag(holder);

        } else {
            // Get the ViewHolder back to get fast access to the TextView
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.text.setText(filteredFood.get(position));

        return convertView;
    }

    static class ViewHolder {
        TextView text;
        TextView brand;
        ListView list;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredFood = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
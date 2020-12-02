package hzhl.net.hlcall.adapter;


import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    private final SparseBooleanArray mSelectedItems;
    private boolean mIsEditionEnabled = false;

    protected SelectableAdapter() {
        mSelectedItems = new SparseBooleanArray();
    }

    public boolean isEditionEnabled() {
        return mIsEditionEnabled;
    }

    public void enableEdition(boolean set) {
        mIsEditionEnabled = set;

        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Indicates if the item at position position is selected
     *
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    protected boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            mSelectedItems.put(i, true);
            notifyDataSetChanged();
        }
    }

    public void deselectAll() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public abstract Object getItem(int position);
}

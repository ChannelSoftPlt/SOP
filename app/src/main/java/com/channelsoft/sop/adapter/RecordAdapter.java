package com.channelsoft.sop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.channelsoft.sop.BR;
import com.channelsoft.sop.R;
import com.channelsoft.sop.databinding.ItemLoadingLayoutBinding;
import com.channelsoft.sop.databinding.RecordListItemBinding;
import com.channelsoft.sop.object.RecordObject;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private OrderAdapterCallBack callBack;
    private ArrayList<RecordObject> recordObjectArrayList;
    private Context context;

    public RecordAdapter(Context context, ArrayList<RecordObject> recordObjectArrayList, OrderAdapterCallBack callBack) {
        this.context = context;
        this.recordObjectArrayList = recordObjectArrayList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                binding = DataBindingUtil.inflate(inflater, R.layout.record_list_item, parent, false);
                return new MyViewHolder((RecordListItemBinding) binding);
            case VIEW_TYPE_LOADING:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_loading_layout, parent, false);
                return new MyViewHolder((ItemLoadingLayoutBinding) binding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RecordObject object = recordObjectArrayList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_NORMAL:
                holder.bind(object, position);
                break;
            case VIEW_TYPE_LOADING:
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == recordObjectArrayList.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return recordObjectArrayList.size();
    }

    public void addLoading() {
        isLoaderVisible = true;
        recordObjectArrayList.add(new RecordObject());
        notifyItemInserted(recordObjectArrayList.size() - 1);
    }

    public void addItems(List<RecordObject> items) {
        recordObjectArrayList.addAll(items);
        notifyDataSetChanged();
    }

    public void removeLoading() {
        try {
            isLoaderVisible = false;
            int position = recordObjectArrayList.size() - 1;
            RecordObject item = getItem(position);
            if (item != null) {
                recordObjectArrayList.remove(position);
                notifyItemRemoved(position);
            }
        } catch (Exception e) {
        }

    }

    public void clear() {
        removeLoading();
        recordObjectArrayList.clear();
        notifyDataSetChanged();
    }

    private RecordObject getItem(int position) {
        return recordObjectArrayList.get(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private RecordListItemBinding recordListItemBinding;
        private ItemLoadingLayoutBinding itemLoadingLayoutBinding;

        MyViewHolder(RecordListItemBinding binding) {
            super(binding.getRoot());
            this.recordListItemBinding = binding;
        }

        MyViewHolder(ItemLoadingLayoutBinding binding) {
            super(binding.getRoot());
            this.itemLoadingLayoutBinding = binding;
        }

        public void bind(final RecordObject obj, final int position) {
            recordListItemBinding.setVariable(BR.obj, obj);
            recordListItemBinding.executePendingBindings();

            recordListItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.view(obj);
                }
            });

        }
    }

    public interface OrderAdapterCallBack {
        void view(RecordObject recordObject);
    }
}
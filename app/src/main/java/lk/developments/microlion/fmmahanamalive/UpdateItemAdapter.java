package lk.developments.microlion.fmmahanamalive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpdateItemAdapter extends RecyclerView.Adapter<UpdateItemAdapter.MyViewHolder> {

    private List<UpdateItem> updateItemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, desc;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txtUITitle);
            desc = view.findViewById(R.id.txtUIDesc);
        }
    }


    public UpdateItemAdapter(List<UpdateItem> updateItemList) {
        this.updateItemList = updateItemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.update_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UpdateItem updateItem = updateItemList.get(position);
        holder.title.setText(updateItem.getTitle());
        holder.desc.setText(updateItem.getDesc());
    }

    @Override
    public int getItemCount() {
        return updateItemList.size();
    }
}

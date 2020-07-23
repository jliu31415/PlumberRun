package jliu.plumberrun;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> levelNames;

    RecyclerViewAdapter(Context context, ArrayList<String> levelNames) {
        this.context = context;
        this.levelNames = levelNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.levelName.setText(levelNames.get(position));
//        Glide.with(context)
//                .load(.get(position))
//                .into(holder.);
    }

    @Override
    public int getItemCount() {
        return levelNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView levelName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            levelName = itemView.findViewById(R.id.cardName);
        }
    }
}

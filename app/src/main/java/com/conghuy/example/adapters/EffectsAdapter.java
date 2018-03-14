package com.conghuy.example.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.conghuy.example.R;
import com.conghuy.example.classs.Const;
import com.conghuy.example.dtos.Effects;
import com.conghuy.example.interfaces.EffectCallBack;

import java.util.List;

/**
 * Created by maidinh on 05-Oct-17.
 */

public class EffectsAdapter extends RecyclerView.Adapter<EffectsAdapter.MyViewHolder> {
    private Context context;
    private List<Effects> effectsList;
    private EffectCallBack callBack;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public RelativeLayout root;

        public MyViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tvName);
            root = (RelativeLayout) view.findViewById(R.id.root);
        }

        public void handler(final Effects effects) {
            root.setBackgroundColor(effects.flag ? Const.getColor(context, R.color.colorChooseEffect) : Const.getColor(context, R.color.colorBackgroundEffect));
            tvName.setText(effects.parameters);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!effects.flag){
                        callBack.onComplete(effects.parameters);

                        // notifyDataSetChanged
                        for (Effects dto : effectsList) {
                            dto.flag = false;
                        }
                        effects.flag = true;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }


    public EffectsAdapter(Context context, List<Effects> effectsList, EffectCallBack callBack) {
        this.context = context;
        this.effectsList = effectsList;
        this.callBack = callBack;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.effects_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Effects effects = effectsList.get(position);
        holder.handler(effects);
    }

    @Override
    public int getItemCount() {
        return effectsList.size();
    }
}
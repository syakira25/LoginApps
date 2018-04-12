package com.example.jameedean.loginapps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jameedean.loginapps.R;
import com.example.jameedean.loginapps.model.AgencyModel;

import java.util.ArrayList;

/**
 * Created by JameeDean on 12/11/2017.
 */

public class AgencyAdapter extends RecyclerView.Adapter<AgencyAdapter.AgencyViewHolder>  {

    private Context mContext;
    private ArrayList<AgencyModel> mData;

    private OnItemClick mListener;

    public AgencyAdapter(Context context, AgencyAdapter.OnItemClick listener) {
        mContext = context;
        mData = new ArrayList<>();

        mListener = listener;
    }

    @Override
    public AgencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_holder_agency, parent, false);
        return new AgencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AgencyViewHolder holder, int position) {

        AgencyModel model = mData.get(position);

        holder.name_agency.setText(model.getName());
        holder.email_agency.setText(model.getEmail());
        // set description as log
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addData(AgencyModel model) {
        mData.add(model);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();

        notifyDataSetChanged();
    }

    public AgencyModel getItem(int position) {
        return mData.get(position);
    }

    public interface OnItemClick {
        void onClick(int pos);
    }

    class AgencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name_agency;
        private TextView email_agency;

        AgencyViewHolder(View itemView) {
            super(itemView);

            name_agency = itemView.findViewById(R.id.tv_name);
            email_agency = itemView.findViewById(R.id.tv_email);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(getAdapterPosition());
        }
    }
}

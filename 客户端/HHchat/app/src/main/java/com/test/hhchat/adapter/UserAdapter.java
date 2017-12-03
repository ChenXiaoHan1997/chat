package com.test.hhchat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.hhchat.Constants;
import com.test.hhchat.R;
import com.test.hhchat.activity.MainActivity;
import com.test.hhchat.model.HHUser;

import java.util.List;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<HHUser> hhUsers;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View userView;
        TextView tvPhone;
        TextView tvNickname;
        TextView tvState;

        public ViewHolder(View view) {
            super(view);
            userView = view;
            tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            tvNickname = (TextView) view.findViewById(R.id.tv_nickname);
            tvState = (TextView) view.findViewById(R.id.tv_state);
        }
    }

    public UserAdapter(List<HHUser> hhUsers) {
        this.hhUsers = hhUsers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                HHUser hhUser = hhUsers.get(position);
                ((MainActivity) view.getContext()).startChat(hhUser);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HHUser hhUser = hhUsers.get(position);
        holder.tvPhone.setText(hhUser.getPhone());
        holder.tvNickname.setText(hhUser.getNickname());
        if (hhUser.isOnline()) {
            holder.tvState.setText("在线");
            holder.tvState.setBackgroundColor(Constants.COLOR_ONLINE);
        } else {
            holder.tvState.setText("离线");
            holder.tvState.setBackgroundColor(Constants.COLOR_OFFLINE);
        }
    }

    @Override
    public int getItemCount() {
        return hhUsers.size();
    }
}

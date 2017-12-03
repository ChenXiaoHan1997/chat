package com.test.hhchat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.test.hhchat.R;
import com.test.hhchat.model.HHMessage;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<HHMessage> hhMessages;
    private String myPhone;
    private String myNickname;
    private String otherPhone;
    private String otherNickname;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftBallon;
        LinearLayout rightBallon;
        TextView tvTime;
        TextView tvOtherContent;
        TextView tvMyContent;

        public ViewHolder(View view) {
            super(view);
            leftBallon = (LinearLayout) view.findViewById(R.id.left_ballon);
            rightBallon = (LinearLayout) view.findViewById(R.id.right_ballon);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            tvOtherContent = (TextView) view.findViewById(R.id.tv_other_content);
            tvMyContent = (TextView) view.findViewById(R.id.tv_my_content);
        }
    }

    public MessageAdapter(List<HHMessage> hhMessages, String myPhone, String myNickname, String otherPhone, String otherNickname) {
        this.hhMessages = hhMessages;
        this.myPhone = myPhone;
        this.myNickname = myNickname;
        this.otherPhone = otherPhone;
        this.otherNickname = otherNickname;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HHMessage hhMessage = hhMessages.get(position);
        if (hhMessage.getFromPhone().equals(myPhone) && hhMessage.getTargetPhone().equals(otherPhone)) {
            holder.rightBallon.setVisibility(View.VISIBLE);
            holder.leftBallon.setVisibility(View.GONE);
            holder.tvMyContent.setText(hhMessage.getContent());
        } else if (hhMessage.getFromPhone().equals(otherPhone) && hhMessage.getTargetPhone().equals(myPhone)) {
            holder.leftBallon.setVisibility(View.VISIBLE);
            holder.rightBallon.setVisibility(View.GONE);
            holder.tvOtherContent.setText(hhMessage.getContent());
        }
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(hhMessage.getTime());
        holder.tvTime.setText(time);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return hhMessages.size();
    }
}

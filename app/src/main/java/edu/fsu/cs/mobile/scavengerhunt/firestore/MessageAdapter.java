package edu.fsu.cs.mobile.scavengerhunt.firestore;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.fsu.cs.mobile.scavengerhunt.R;


// https://github.com/MahmoudAlyuDeen/FirebaseIM/tree/3fafaa07495e161840afc7c547c3d1b83538e770
public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder> {
    public static final String TAG = MessageAdapter.class.getCanonicalName();

    public MessageAdapter(@NonNull FirestoreRecyclerOptions<Message> options, ArrayList<Message> mList) {
        super(options);
        this.mList = mList;
    }

    List<Message> mList;

    public void setItems(List<Message> items) {
        this.mList = items;
    }


    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
        holder.setUser(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_child_friends, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onDataChanged() {
        // If there are no chat messages, show a view that invites the user to add a message.
        Log.d(TAG, "Data changed?");
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvItemFriendName;
        TextView tvItemFriendUid;
        Message user;

        MessageViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvItemFriendUid = itemView.findViewById(R.id.adapter_child_friend_uid);
            tvItemFriendName = itemView.findViewById(R.id.adapter_child_friend_name);
        }


        void setUser(Message user) {
            this.user = user;
            tvItemFriendName.setText(user.getFrom());
            tvItemFriendUid.setText(new Date(user.getTimestamp()).toString());

        }


        @Override
        public void onClick(View v) {
        }
    }
}


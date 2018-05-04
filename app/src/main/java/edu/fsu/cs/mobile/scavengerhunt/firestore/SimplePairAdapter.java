package edu.fsu.cs.mobile.scavengerhunt.firestore;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.fsu.cs.mobile.scavengerhunt.R;


// https://github.com/MahmoudAlyuDeen/FirebaseIM/tree/3fafaa07495e161840afc7c547c3d1b83538e770
public class SimplePairAdapter extends RecyclerView.Adapter<SimplePairAdapter.MessageViewHolder> {
    public static final String TAG = SimplePairAdapter.class.getCanonicalName();

    public SimplePairAdapter(ArrayList<Pair<String, String>> mList, onUserClickFriend Listener) {
        super();
        this.mList = mList;
        this.mListener = Listener;
    }

    List<Pair<String, String>> mList;
    onUserClickFriend mListener;

    public void setItems(List<Pair<String, String>> items) {
        List<Pair<String, String>> list = new ArrayList<>();
        list.addAll(items);
        this.mList = list;
    }

    public interface onUserClickFriend {
        void onItemClick(Pair<String, String> item);
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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.setUser(mList.get(position));
        holder.setCallBack(mListener);

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvItemFriendName;
        TextView tvItemFriendUid;
        onUserClickFriend callBack;
        Pair<String, String> user;

        MessageViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvItemFriendUid = itemView.findViewById(R.id.adapter_child_friend_uid);
            tvItemFriendName = itemView.findViewById(R.id.adapter_child_friend_name);
        }


        void setUser(Pair<String, String> user) {
            this.user = user;
            tvItemFriendName.setText(user.first);
            tvItemFriendUid.setText(user.second);

        }

        public void setCallBack(onUserClickFriend callBack) {
            this.callBack = callBack;
        }

        @Override
        public void onClick(View v) {
            callBack.onItemClick(user);
        }
    }
}


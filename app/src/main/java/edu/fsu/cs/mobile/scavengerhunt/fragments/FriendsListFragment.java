package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.fsu.cs.mobile.scavengerhunt.Firestore.PlainAdapter;
import edu.fsu.cs.mobile.scavengerhunt.R;

import static android.view.animation.Animation.INFINITE;

public class FriendsListFragment extends Fragment implements PlainAdapter.onUserClickFriend {
    EditText etUid, etMessage;
    RecyclerView usersRecycler;
    TextView tvReceiver;
    FirebaseAuth mAuth;

    private static final CollectionReference Friend_ListCollection = FirebaseFirestore.getInstance().collection("friends");
    private static Query sChatQuery;
    public static final String FRAGMENT_TAG = "Friends List";
    public static final String TAG = "Friends";
    public PlainAdapter adapter = newAdapter();
    private String userId;
    private TextView tvEmptyList;
    private LottieAnimationView lottieAnimation;
    private boolean emptyListFlag = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        mAuth = FirebaseAuth.getInstance();
        etUid = view.findViewById(R.id.user_id_edit);
        userId = mAuth.getCurrentUser().getUid();
        etUid.setText(userId);
        /* friends/id/pendingFriends         */
        sChatQuery = Friend_ListCollection.document(userId).collection("pendingFriends");
        sChatQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "There was an error is updating the UI");
                    return;
                }

                List<DocumentSnapshot> DocSnaps = queryDocumentSnapshots.getDocuments();
                List<Pair<String, String>> newItemSet = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : DocSnaps) {
                    String friend_path = "new_friend";
                    if (documentSnapshot.contains(friend_path)) {
                        if (emptyListFlag) {
                            toggleEmptyListState(false);
                        }
                    } else {
                        toggleEmptyListState(true);
                    }
                    String id = documentSnapshot.getId();
                    String userName = String.valueOf(documentSnapshot.get(friend_path));
                    Pair<String, String> newPair = new Pair<>(id, userName);
                    newItemSet.add(newPair);
                }

                if (DocSnaps.size() == 0) {
                    toggleEmptyListState(true);
                    return;
                }
                adapter.setItems(newItemSet);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "UI Updated!");
            }
        });
        tvReceiver = view.findViewById(R.id.RecieverIndicatorText);
        usersRecycler = view.findViewById(R.id.friends_recycler);
        initializeUsersRecycler();

        tvEmptyList = view.findViewById(R.id.empty_list_text);
        lottieAnimation = view.findViewById(R.id.lottieAnimationView);

        toggleEmptyListState(false);

        return view;
    }

    private void toggleEmptyListState(boolean beginAnimation) {
        emptyListFlag = beginAnimation;
        int visiblity = (beginAnimation ? View.VISIBLE : View.INVISIBLE);
        int visiblity2 = (beginAnimation ? View.INVISIBLE : View.VISIBLE);
        tvEmptyList.setVisibility(visiblity);
        lottieAnimation.setVisibility(visiblity);
        usersRecycler.setVisibility(visiblity2);
        // If we are going to an empty state start the animation
        if (beginAnimation) {
            // https://editor.lottiefiles.com/?src=https://www.lottiefiles.com/download_public/872
            lottieAnimation.setAnimation("empty_list.json");
            lottieAnimation.setRepeatMode(INFINITE);
            lottieAnimation.playAnimation();
        }

    }

    private void initializeUsersRecycler() {

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                usersRecycler.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        usersRecycler.setAdapter(adapter);
        usersRecycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
    }

    protected PlainAdapter newAdapter() {
        return new PlainAdapter(new ArrayList<Pair<String, String>>(), this);
    }

    @Override
    public void onItemClick(Pair<String, String> item) {
        Toast.makeText(getActivity().getApplicationContext(), item.first + "____" + item.second, Toast.LENGTH_SHORT).show();
    }
}

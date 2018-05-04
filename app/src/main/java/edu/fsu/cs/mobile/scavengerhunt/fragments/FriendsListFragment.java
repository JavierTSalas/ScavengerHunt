package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.firestore.SimplePairAdapter;

import static android.view.animation.Animation.INFINITE;

public class FriendsListFragment extends Fragment implements SimplePairAdapter.onUserClickFriend {
    EditText etUid, etMessage;
    RecyclerView usersRecycler;
    TextView tvReceiver;
    FirebaseAuth mAuth;

    private static final CollectionReference Friend_ListCollection = FirebaseFirestore.getInstance().collection("friends");
    private static Query sChatQuery;
    public static final String FRAGMENT_TAG = "Friends List";
    public static final String TAG = "Friends";
    public SimplePairAdapter adapter = newAdapter();
    private String userId;
    private TextView tvEmptyList;
    private LottieAnimationView lottieAnimation;
    private boolean emptyListFlag = false;
    private Button bAdd;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        mAuth = FirebaseAuth.getInstance();
        etUid = view.findViewById(R.id.friend_id_text);
        userId = mAuth.getCurrentUser().getUid();
        etUid.setText(userId);
        tvReceiver = view.findViewById(R.id.RecieverIndicatorText);
        usersRecycler = view.findViewById(R.id.friends_recycler);
        tvEmptyList = view.findViewById(R.id.empty_list_text);
        lottieAnimation = view.findViewById(R.id.lottieAnimationView);
        etMessage = view.findViewById(R.id.friend_message_edit);
        bAdd = view.findViewById(R.id.friend_add_button);
        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {

                    String new_friend = etMessage.getText().toString();
                    Map<String, String> myData = new HashMap<String, String>() {
                    };
                    myData.put("new_friend", new_friend);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("friends").document(userId).set(myData, SetOptions.merge());


                }
            }
        });
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

        initializeUsersRecycler();
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

    protected SimplePairAdapter newAdapter() {
        return new SimplePairAdapter(new ArrayList<Pair<String, String>>(), this);
    }

    @Override
    public void onItemClick(Pair<String, String> item) {
        inflateUserFragment(item.first);
    }

    private void inflateUserFragment(String userId) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FriendChatFragment fragment = FriendChatFragment.newInstance(userId);
        FragmentTransaction trans = manager.beginTransaction();
        trans.replace(R.id.frame, fragment, FriendChatFragment.FRAGMENT_TAG);
        trans.addToBackStack(FriendChatFragment.FRAGMENT_TAG);
        trans.commit();
    }
}

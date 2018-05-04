package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.firestore.Message;
import edu.fsu.cs.mobile.scavengerhunt.firestore.SimplePairAdapter;

import static android.view.animation.Animation.INFINITE;

public class FriendChatFragment extends Fragment {
    private static final String mRecipientID_KEY = "Recipient_Id";
    private static String mRecipientId, mOwnerId;
    EditText etMessage;
    RecyclerView messageRecycler;
    TextView tvReceiver;
    FirebaseAuth mAuth;

    private static final CollectionReference MessagesCollection = FirebaseFirestore.getInstance().collection("messages");
    private static Query sChatQuery;
    public static final String FRAGMENT_TAG = "Messages_List";
    public static final String TAG = "Friends";
    public SimplePairAdapter adapter = newAdapter();
    private String userId;
    private TextView tvEmptyList;
    private LottieAnimationView lottieAnimation;
    private boolean emptyListFlag = false;
    private Button bConfirm;

    /**
     * Public constructor for creating new fragments
     *
     * @param recipientId id of the receiver
     * @return Fragment with data attached
     */
    public static FriendChatFragment newInstance(String recipientId) {
        Bundle bundle = new Bundle();
        bundle.putString(mRecipientID_KEY, recipientId);

        FriendChatFragment fragment = new FriendChatFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Closes the keyboard for the user - this might have already been done
     */
    public void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
            Log.d(TAG, e.getMessage() + " user's keyboard was already closed.");
        }
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            mRecipientId = bundle.getString(mRecipientID_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messaging, container, false);
        hideSoftKeyboard();
        readBundle(getArguments());
        mAuth = FirebaseAuth.getInstance();
        bConfirm = view.findViewById(R.id.message_send_button);
        tvReceiver = view.findViewById(R.id.friend_id_text);
        tvEmptyList = view.findViewById(R.id.empty_list_text);
        messageRecycler = view.findViewById(R.id.messages_recycler);
        lottieAnimation = view.findViewById(R.id.lottieAnimationView);
        etMessage = view.findViewById(R.id.friend_message_edit);
        mOwnerId = mAuth.getCurrentUser().getUid();
        tvReceiver.setText(mRecipientId);
        initializeQuery();

        initializeUsersRecycler();


        toggleEmptyListState(true);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message message = new Message(new Date().getTime(), etMessage.getText().toString(), mOwnerId, mRecipientId);
                //saveMessageToFirestore(message);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // This should give us unique ids ?
                // I really don't know but it looks pretty random so it' good enough for now
                String docId = String.valueOf(new Date(message.getTimestamp()).hashCode() + message.getFrom().hashCode());
                db.collection("messages").document(getChatPath()).collection("messages").document(docId).set(message);

                // This might be a source of error due to a race condition from setting and initializing
                // TODO: NOT USE A WHILE LOOP BUT IT'S ONLY A DEMO SO YOLO
                while (sChatQuery == null) // If it wasn't initialized in the first place, i.e no messages then it thew a NullPointerException so
                {
                    initializeQuery();// we need to re-assign it
                }

            }
        });


        return view;
    }

    private void initializeQuery() {

        try {
            {
                sChatQuery = MessagesCollection.document(getChatPath()).collection("messages");

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
                            if (documentSnapshot.contains("from")) {
                                if (emptyListFlag) {
                                    toggleEmptyListState(false);
                                }
                            } else {
                                toggleEmptyListState(true);
                            }
                            String body = String.valueOf(documentSnapshot.get("body"));
                            String userName = String.valueOf(documentSnapshot.get("from"));
                            Pair<String, String> newPair = new Pair<>(body, userName);
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

            }
        } catch (NullPointerException e) {
            Log.d(TAG, "No path found e=" + e.getMessage());
            toggleEmptyListState(true);
        }

    }

    private void toggleEmptyListState(boolean beginAnimation) {
        emptyListFlag = beginAnimation;
        int visiblity = (beginAnimation ? View.VISIBLE : View.INVISIBLE);
        int visiblity2 = (beginAnimation ? View.INVISIBLE : View.VISIBLE);
        tvEmptyList.setVisibility(visiblity);
        lottieAnimation.setVisibility(visiblity);
        messageRecycler.setVisibility(visiblity2);
        // If we are going to an empty state start the animation
        if (beginAnimation) {
            // https://editor.lottiefiles.com/?src=https://www.lottiefiles.com/download_public/43
            lottieAnimation.setAnimation("empty_messages.json");
            lottieAnimation.setRepeatMode(INFINITE);
            lottieAnimation.playAnimation();
        }

    }

    private void initializeUsersRecycler() {

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messageRecycler.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        messageRecycler.setAdapter(adapter);
        messageRecycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
    }

    protected SimplePairAdapter newAdapter() {
        return new SimplePairAdapter(new ArrayList<Pair<String, String>>(), new SimplePairAdapter.onUserClickFriend() {
            @Override
            public void onItemClick(Pair<String, String> item) {

            }
        });
    }


    /*
     *      the value {@code 0} if the argument string is equal to
     *      this string; a value less than {@code 0} if this string
     *      is lexicographically less than the string argument; and a
     *      value greater than {@code 0} if this string is
     *      lexicographically greater than the string argument.
     *
     *      path = messages/room/id1_id2
     *      where id1 and id2 are in lexicographic order small_large
     */

    public String getChatPath() {
        int StrComp = mOwnerId.compareTo(mRecipientId);
        if (StrComp == 0) {
            // Equal strings this should never happen but we should handle this case at some point
            return null;
        } else if (StrComp < 0) {
            // mOwnerId < mRecipientId
            return mOwnerId + "_" + mRecipientId;
        } else {
            // mOwnerId > mRecipientId
            return mRecipientId + "_" + mOwnerId;

        }
    }
}

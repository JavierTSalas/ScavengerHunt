package edu.fsu.cs.mobile.scavengerhunt.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// Source :  https://stackoverflow.com/questions/37671380/what-is-fcm-token-in-firebase
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = MyInstanceIDListenerService.class.getCanonicalName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String device_id = FirebaseInstanceId.getInstance().getId();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        if (mAuth.getCurrentUser() != null)
            sendRegistrationToServer(refreshedToken, device_id, mAuth.getCurrentUser().getUid());
    }

    private void sendRegistrationToServer(final String refreshedToken, final String device_id, final String uid) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("FCM_tokens").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> map = task.getResult().getData();
                            // Used for making sure that we are working with the same device
                            // Meaning that client_tokens[0] is from device_ids[0]
                            boolean shouldUpdate = true;
                            ArrayList<String> client_tokens_list = new ArrayList<>();
                            ArrayList<String> device_ids_list = new ArrayList<>();
                            if (map != null) {
                                // Search to see if our client ID is already logged
                                if (map.containsKey("client_tokens")) {
                                    // Get the information from the document
                                    client_tokens_list = (ArrayList<String>) map.get("client_tokens");
                                    if (client_tokens_list.contains(refreshedToken)) {
                                        shouldUpdate = false;
                                    } else {
                                        shouldUpdate = true;
                                        // Get the device_id list so we can append to it
                                        device_ids_list = (ArrayList<String>) map.get("device_ids");
                                        client_tokens_list.remove("");
                                        device_ids_list.remove("");
                                        client_tokens_list.add(refreshedToken);
                                        device_ids_list.add(device_id);
                                    }
                                }

                                // If our client is not already in the database
                                if (shouldUpdate) {
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("client_tokens", client_tokens_list);
                                    user.put("device_ids", device_ids_list);

                                    // Add a new document with a generated ID
                                    db.collection("FCM_tokens").document(uid).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Successfully added new token=" + refreshedToken + " for device id=" + device_id);
                                                }
                                            });

                                }

                            }
                            Log.d(TAG, task.getResult().getId());

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());

                        }
                    }
                });

    }
}

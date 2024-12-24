package com.example.wanderSync.model.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotesManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public NotesManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    public void addNoteToTravelLog(String location, String currentUserId, String locationId,
                                   Note note, OnCompleteListener<Void> listener) {
        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                .whereEqualTo("documentId", locationId)
                .whereArrayContains("associatedUsers", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // reference to the travel log document
                        DocumentReference travelLogRef = firestore
                                .collection("travelLogs").document(locationId);

                        // add the new note
                        travelLogRef.update("notes", FieldValue.arrayUnion(note))
                                .addOnCompleteListener(listener);
                    } else {
                        // no travel log match
                        listener.onComplete(Tasks.forResult(null));
                    }
                });
    }

    public LiveData<List<Note>> getNotesForTravelLog(String location, String currentUserId,
                                                     String documentId) {
        MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();

        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                .whereEqualTo("documentId", documentId)
                .whereArrayContains("associatedUsers", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // fetch the notes field
                        List<Map<String, Object>> notesData = (List<Map<String, Object>>) document
                                .get("notes");

                        if (notesData != null && !notesData.isEmpty()) {
                            // get unique user IDs from the notes
                            Set<String> userIds = new HashSet<>();
                            for (Map<String, Object> noteData : notesData) {
                                String userId = (String) noteData.get("userId");
                                if (userId != null) {
                                    userIds.add(userId);
                                }
                            }

                            // query users collection to get emails for each user ID
                            firestore.collection("users")
                                    .whereIn("userId", new ArrayList<>(userIds))
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        Map<String, String> userIdToEmailMap = new HashMap<>();
                                        for (DocumentSnapshot userDoc : querySnapshot) {
                                            String userId = userDoc.getString("userId");
                                            String email = userDoc.getString("email");
                                            if (userId != null && email != null) {
                                                userIdToEmailMap.put(userId, email);
                                            }
                                        }

                                        List<Note> notes = new ArrayList<>();
                                        for (Map<String, Object> noteData : notesData) {
                                            String noteContent = (String) noteData
                                                    .get("noteContent");
                                            String userId = (String) noteData.get("userId");
                                            String email = userIdToEmailMap.get(userId);

                                            // add email to the Note object
                                            if (email != null) {
                                                Note note = new Note(noteContent, email);
                                                notes.add(note);
                                            }
                                        }

                                        // TODO: this doesn't actually work as intended. need to fix
                                        // Sort notes by timestamp (descending order)
                                        Collections.sort(notes, (note1, note2) -> Long
                                                .compare(note2.getTimestampMillis(),
                                                        note1.getTimestampMillis()));

                                        notesLiveData.setValue(notes);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("Firestore", "Failed to fetch user emails", e);
                                        notesLiveData.setValue(new ArrayList<>());
                                    });
                        } else {
                            Log.d("Firestore", "No notes found in the document");
                            notesLiveData.setValue(new ArrayList<>());
                        }
                    } else {
                        Log.d("Firestore", "Query failed or no matching travel log found "
                                + "for: " + location);
                        notesLiveData.setValue(new ArrayList<>());
                    }
                });

        return notesLiveData;
    }
}

package com.example.taskease;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class DBManager {

    private static final String DB_URL =
            "https://taskease-d8588-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static DBManager instance;
    private final DatabaseReference tasksRef;

    public interface Callback { void onResult(boolean success, String message); }
    public interface TasksListener { void onTasks(List<TaskItem> tasks); }

    private DBManager() {
        tasksRef = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");
    }

    // this is to mark a task as completed anf save the time it toojk
    public void completeTask(String id, int actualMinutes, Callback cb){
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("actualMinutes", actualMinutes);
        updates.put("completedAt", System.currentTimeMillis());
        tasksRef.child(id).updateChildren(updates).addOnSuccessListener(v -> cb.onResult(true, "Task completed"))
                .addOnFailureListener(e -> cb.onResult(false, e.getMessage()));
    }
    public static DBManager getInstance() {
        if (instance == null) instance = new DBManager();
        return instance;
    }

    public void addTask(TaskItem task, Callback cb) {
        String id = tasksRef.push().getKey();
        task.id = id;
        tasksRef.child(id).setValue(task)
                .addOnSuccessListener(v -> cb.onResult(true, "Task saved"))
                .addOnFailureListener(e -> cb.onResult(false, e.getMessage()));
    }

    public void listenTasks(TasksListener listener) {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TaskItem> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskItem t = child.getValue(TaskItem.class);
                    if (t != null){
                        t.id = child.getKey();
                        list.add(t);
                    }
                }
                listener.onTasks(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
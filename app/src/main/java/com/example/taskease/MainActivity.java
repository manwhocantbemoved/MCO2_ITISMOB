package com.example.taskease;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // this is for remembering if the user is currently Settled or Moving
    private boolean isSettled = false;

    // this is for the adapter that shows the task cards in the Dashboard list
    private TaskAdapter adapter;

    // this is for holding every task loaded from Firebase
    private List<TaskItem> allTasks = new ArrayList<>();

    // this is for remembering which filter button is active (all, short, high)
    private String currentFilter = "all";

    // this is for the free time window (in minutes) used to pick tasks that fit
    private static final int FREE_WINDOW_MIN = 35;

    // NEW: this is for testing: when true, "settled" takes 20 seconds no matter the sensitivity setting
    private static final boolean TEST_MODE = false;

    // NEW: this is for reading the values saved by the Settings screen
    private SharedPreferences prefs;

    // this is for the location permission popup and what happens after the user answers
    private final ActivityResultLauncher<String[]> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                Toast.makeText(this,
                        granted ? "Location enabled" : "Location is needed for movement detection",
                        Toast.LENGTH_SHORT).show();
                if (granted) startLocationUpdates();
                askNotificationPermission();
            });

    // this is for the Google location client and the callback that receives each new location
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;

    // this is for starting location updates every 5 seconds (only if permission is granted)
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        boolean fine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) return;

        LocationRequest request =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    // this is for starting location updates whenever the Dashboard is on screen
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    // this is for stopping location updates when the user leaves the Dashboard
    @Override
    protected void onPause() {
        super.onPause();
        fusedClient.removeLocationUpdates(locationCallback);
        anchorLocation = null;
    }

    // this is for the settled radius: stay within 50 meters to count as staying still
    private static final float SETTLED_RADIUS_M = 50;

    // this is for remembering the spot (and the time) where the user started staying still
    private Location anchorLocation;
    private long anchorTime;

    // NEW: this is for how long the user must stay still before being Settled (Sensitivity Settings)
    private long settledAfterMs() {
        if (TEST_MODE) return 20000;
        int minutes = prefs.getInt("sensitivity_min", 15);
        return minutes * 60 * 1000L;
    }

    // NEW: this is for checking if the current time falls inside Quiet Hours
    private boolean inQuietHours() {
        if (!prefs.getBoolean("quiet_enabled", false)) return false;
        int start = prefs.getInt("quiet_start_min", 22 * 60);
        int end = prefs.getInt("quiet_end_min", 7 * 60);
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (start <= end) return now >= start && now < end;
        return now >= start || now < end; // overnight, like 10 PM to 7 AM
    }

    // NEW: this is for checking if the user is inside a blacklisted place (Location Blacklist)
    private boolean inBlacklistedPlace(Location loc) {
        Set<String> places = prefs.getStringSet("blacklist_places", new HashSet<>());
        for (String p : places) {
            String[] parts = p.split(",");
            if (parts.length < 2) continue;
            try {
                float[] result = new float[1];
                Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                        Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()), result);
                if (result[0] <= 100) return true; // within 100 meters
            } catch (NumberFormatException ignored) { }
        }
        return false;
    }

    // this is for deciding Settled or Moving from each new location
    private void handleLocation(Location loc) {
        long now = System.currentTimeMillis();

        // NEW: this is for ignoring tracking inside blacklisted places
        if (inBlacklistedPlace(loc)) {
            anchorLocation = null;
            return;
        }

        // this is for the very first location: just remember it as the starting spot
        if (anchorLocation == null) {
            anchorLocation = loc;
            anchorTime = now;
            return;
        }

        // this is for when the user moved far away: restart the timer and go back to Moving
        if (loc.distanceTo(anchorLocation) > SETTLED_RADIUS_M) {
            anchorLocation = loc;
            anchorTime = now;
            if (isSettled) {
                isSettled = false;
                updateStatus();
            }
            return;
        }

        // CHANGED: this is for becoming Settled using the sensitivity setting, and staying quiet in Quiet Hours
        if (!isSettled && now - anchorTime >= settledAfterMs()) {
            isSettled = true;
            updateStatus();
            if (!inQuietHours()) {
                showSettledDialog();
                showSettledNotification();
            }
        }
    }

    // this is for the notification channel name (Android needs a channel to show notifications)
    private static final String CHANNEL_ID = "settled_channel";

    // this is for creating the notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Settled alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Tells you when you've settled and have time for a task");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    // this is for asking the user to allow notifications (needed on Android 13 and newer)
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // this is for showing the "You're settled" notification, which opens the Dashboard when tapped
    @SuppressLint("MissingPermission")
    private void showSettledNotification() {
        // this is for skipping the notification if the user didn't allow it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // this is for what happens when the user taps the notification (open the Dashboard)
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // this is for the notification's title and text, including how many tasks fit
        int count = getFittingTasks().size();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("You're settled! " + FREE_WINDOW_MIN + " mins free")
                .setContentText(count + " tasks fit your window. Tap to pick one.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        // this is for actually showing the notification
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    // this is for setting up the whole Dashboard screen when it opens
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // NEW: this is for opening the settings the Settings screen saves
        prefs = getSharedPreferences("taskease_settings", MODE_PRIVATE);

        // this is for creating the notification channel
        createNotificationChannel();

        // this is for setting up the location client and what to do with each new location
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    Log.d("LocationTest", "Lat " + loc.getLatitude() + ", Lng " + loc.getLongitude());
                    handleLocation(loc);
                }
            }
        };

        // this is for keeping the screen clear of the status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // this is for the Simulate button that switches between Moving and Settled by hand
        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnToggleStatus.setOnClickListener(v -> {
            isSettled = !isSettled;
            updateStatus();
            if (isSettled) {
                showSettledDialog();
                showSettledNotification();
            }
        });
        updateStatus();

        // this is for the + button that opens the Create Task screen
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateTaskActivity.class)));

        // this is for the sideways task list on the Dashboard
        RecyclerView rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // this is for what happens when a task card's Start button is tapped (open Focus with that task)
        adapter = new TaskAdapter(task -> {
            Intent i = new Intent(MainActivity.this, FocusActivity.class);
            i.putExtra("task_name", task.name);
            i.putExtra("task_duration", task.duration);
            i.putExtra("task_id", task.id);
            startActivity(i);
        });
        rvTasks.setAdapter(adapter);

        // this is for loading tasks from Firebase and updating the list whenever they change
        DBManager.getInstance().listenTasks(tasks -> {
            allTasks = tasks;
            refreshList();
        });

        // this is for the three filter buttons (All, Under 30m, High)
        findViewById(R.id.btnFilterAll).setOnClickListener(v -> {
            currentFilter = "all";
            refreshList();
        });
        findViewById(R.id.btnFilterShort).setOnClickListener(v -> {
            currentFilter = "short";
            refreshList();
        });
        findViewById(R.id.btnFilterHigh).setOnClickListener(v -> {
            currentFilter = "high";
            refreshList();
        });

        // this is for the bottom navigation bar that opens the other screens
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus)
                startActivity(new Intent(MainActivity.this, FocusActivity.class));
            else if (id == R.id.nav_profile)
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            else if (id == R.id.nav_automate)
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(MainActivity.this, InsightsActivity.class));
            return true;
        });

        // this is for asking for location permission first, or notification permission if location is already allowed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            askNotificationPermission();
        }
    }

    // this is for the notification permission popup
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    // this is for picking the tasks that fit inside the free time window
    private List<TaskItem> getFittingTasks() {
        List<TaskItem> fitting = new ArrayList<>();
        for (TaskItem t : allTasks) {
            if ("completed".equals(t.status))
                continue;
            if (t.duration <= FREE_WINDOW_MIN)
                fitting.add(t);
        }
        return fitting;
    }

    // this is for showing only the tasks that match the active filter (and hiding completed ones)
    private void refreshList() {
        List<TaskItem> shown = new ArrayList<>();
        for (TaskItem t : allTasks) {
            if ("completed".equals(t.status)) continue;
            if (currentFilter.equals("short") && t.duration > 30) continue;
            if (currentFilter.equals("high") && !"High".equals(t.priority)) continue;
            shown.add(t);
        }
        adapter.setTasks(shown);
    }

    // this is for updating the status card text and the Simulate button text
    private void updateStatus() {
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvStatusMessage = findViewById(R.id.tvStatusMessage);
        TextView tvWindow = findViewById(R.id.tvWindow);
        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);

        // this is for the Settled version of the status card
        if (isSettled) {
            tvStatus.setText("Settled - 18m");
            tvStatusMessage.setText("You are currently settled. We found "
                    + getFittingTasks().size() + " tasks that fit your window.");
            tvWindow.setText(FREE_WINDOW_MIN + " mins window open");
            btnToggleStatus.setText("Simulate: I'm moving");
        }
        // this is for the Moving version of the status card
        else {
            tvStatus.setText("Moving");
            tvStatusMessage.setText("You are on the move. We'll suggest tasks when you settle.");
            tvWindow.setText("No free window yet");
            btnToggleStatus.setText("Simulate: I'm settled");
        }
    }

    // this is for the popup that lists the tasks that fit when the user becomes Settled
    private void showSettledDialog() {
        List<TaskItem> fitting = getFittingTasks();
        // this is for the case where no saved task fits the free window
        if (fitting.isEmpty()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("You're settled! " + FREE_WINDOW_MIN + " mins free")
                    .setMessage("No saved tasks fit this window. Tap + to add one.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // this is for turning each fitting task into a line of text for the popup
        String[] labels = new String[fitting.size()];
        for (int i = 0; i < fitting.size(); i++) {
            TaskItem t = fitting.get(i);
            labels[i] = t.name + " (" + t.duration + " min, " + t.priority + ")";
        }

        // this is for showing the popup, and opening Focus with the task the user taps
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("You're settled! " + FREE_WINDOW_MIN + " mins free")
                .setItems(labels, (dialog, which) -> {
                    TaskItem picked = fitting.get(which);
                    Intent i = new Intent(MainActivity.this, FocusActivity.class);
                    i.putExtra("task_name", picked.name);
                    i.putExtra("task_duration", picked.duration);
                    i.putExtra("task_id", picked.id);
                    startActivity(i);
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }
}
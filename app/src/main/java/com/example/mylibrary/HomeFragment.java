package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.BookContract.BookEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements SensorEventListener {

    private BookDbHelper dbHelper;
    private BookAdapter adapter;
    private RecyclerView recyclerView;

    private String currentKeyword = "";
    private int currentStatusFilter = -1;
    private String currentSortOrder = "time";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new BookDbHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_home);
        SearchView searchView = view.findViewById(R.id.search_view);
        Toolbar toolbar = view.findViewById(R.id.toolbar_home);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter();

        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        if (toolbar != null) {
            toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { currentKeyword = query; loadBooks(); return true; }
            @Override public boolean onQueryTextChange(String newText) { currentKeyword = newText; loadBooks(); return true; }
        });

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            acceleration = 0.00f;
            currentAcceleration = SensorManager.GRAVITY_EARTH;
            lastAcceleration = SensorManager.GRAVITY_EARTH;
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > 12) {
                acceleration = 0;
                performShakeAction();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void performShakeAction() {
        Toast.makeText(requireContext(), R.string.shake_detected, Toast.LENGTH_SHORT).show();
        List<Book> currentBooks = new ArrayList<>(adapter.getBooks());
        if (!currentBooks.isEmpty()) {
            Collections.shuffle(currentBooks);
            adapter.setBooks(currentBooks);
        }
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_time) currentSortOrder = "time";
        else if (id == R.id.sort_rating) currentSortOrder = "rating";
        else if (id == R.id.sort_status) currentSortOrder = "status";
        else if (id == R.id.filter_all) currentStatusFilter = -1;
        else if (id == R.id.filter_todo) currentStatusFilter = 0;
        else if (id == R.id.filter_reading) currentStatusFilter = 1;
        else if (id == R.id.filter_read) currentStatusFilter = 2;
        loadBooks();
        return true;
    }

    private void loadBooks() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long currentUserId = prefs.getLong("current_user_id", -1);

        List<Book> books = new ArrayList<>();
        Cursor cursor = dbHelper.queryBooks(currentUserId, currentKeyword, currentStatusFilter, currentSortOrder);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));
            int status = 0; try { status = cursor.getInt(cursor.getColumnIndexOrThrow("status")); } catch (Exception e) {}
            String filePath = ""; try { filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path")); } catch (Exception e) {}
            books.add(new Book(id, title, author, rating, imageUri, status, filePath));
        }
        cursor.close();
        adapter.setBooks(books);
    }
}
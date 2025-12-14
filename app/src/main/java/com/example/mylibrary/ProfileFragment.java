package com.example.mylibrary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ProfileFragment extends Fragment {

    private BookDbHelper dbHelper;
    private TextView tvUsername, tvFavCount, tvReviewCount;
    private ImageView ivAvatar;
    private View layoutFav, layoutReview;
    private Button btnEditProfile;

    private TextView tvLocationInfo;
    private Button btnGetLocation, btnOpenMap;
    private LocationManager locationManager;
    private double currentLat = 0, currentLon = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new BookDbHelper(requireContext());
        tvUsername = view.findViewById(R.id.tv_username);
        tvFavCount = view.findViewById(R.id.tv_fav_count);
        tvReviewCount = view.findViewById(R.id.tv_review_count);
        ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        layoutFav = view.findViewById(R.id.layout_fav_click);
        layoutReview = view.findViewById(R.id.layout_review_click);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        tvLocationInfo = view.findViewById(R.id.tv_location_info);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnOpenMap = view.findViewById(R.id.btn_open_map);
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        btnGetLocation.setOnClickListener(v -> checkLocationPermission());
        btnOpenMap.setOnClickListener(v -> openMap());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        if (layoutFav != null) layoutFav.setOnClickListener(v -> startActivity(new Intent(requireContext(), MyFavoritesActivity.class)));
        if (layoutReview != null) layoutReview.setOnClickListener(v -> startActivity(new Intent(requireContext(), MyReviewsActivity.class)));

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(requireContext(), UserManageActivity.class)));

        return view;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            Toast.makeText(requireContext(), R.string.location_permission_needed, Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                tvLocationInfo.setText(R.string.getting_location);

                Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnown == null) lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnown != null) {
                    updateLocationUI(lastKnown);
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvLocationInfo.setText(getString(R.string.unknown_error));
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateLocationUI(location);
            locationManager.removeUpdates(this);
        }
        @Override public void onProviderEnabled(@NonNull String provider) {}
        @Override public void onProviderDisabled(@NonNull String provider) {}
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private void updateLocationUI(Location location) {
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        tvLocationInfo.setText(String.format(getString(R.string.location_format), currentLon, currentLat));
    }

    private void openMap() {
        if (currentLat == 0 && currentLon == 0) {
            Toast.makeText(requireContext(), R.string.location_first, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("geo:" + currentLat + "," + currentLon + "?z=15&q=" + currentLat + "," + currentLon + "(My Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.map_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);

        Cursor cursor = dbHelper.getUser(userId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String avatarUri = cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri"));

            tvUsername.setText(name);
            if (avatarUri != null && !avatarUri.isEmpty()) {
                Glide.with(this)
                        .load(avatarUri)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
        cursor.close();

        int favCount = dbHelper.getFavoriteCount(userId);
        int reviewCount = dbHelper.getReviewCount(userId);

        tvFavCount.setText(String.valueOf(favCount));
        tvReviewCount.setText(String.valueOf(reviewCount));
    }
}
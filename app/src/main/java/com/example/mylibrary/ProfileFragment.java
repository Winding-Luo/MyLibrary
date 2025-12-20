package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ProfileFragment extends Fragment {

    private BookDbHelper dbHelper;
    private TextView tvUsername, tvFavCount, tvReviewCount;
    private ImageView ivAvatar;
    private View layoutFav, layoutReview;
    private Button btnEditProfile;

    // 移除了 Location 相关的变量

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

        // 移除了获取位置和打开地图按钮的初始化与监听器

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

    // 移除了 checkLocationPermission, onRequestPermissionsResult, getLocation, updateLocationUI, openMap 方法

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
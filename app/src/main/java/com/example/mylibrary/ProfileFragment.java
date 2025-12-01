package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private BookDbHelper dbHelper;
    private TextView tvUsername, tvFavCount, tvReviewCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new BookDbHelper(requireContext());
        tvUsername = view.findViewById(R.id.tv_username);
        tvFavCount = view.findViewById(R.id.tv_fav_count);
        tvReviewCount = view.findViewById(R.id.tv_review_count);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // 注销逻辑
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        // 获取当前用户ID (这里假设你之前保存了 username, 如果没保存，可以只显示 ID 或重新查询)
        // 为了简单，我们只从 Prefs 读 ID，用户名如果没存，暂时显示 "User"
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);

        // 注意：这里最好在 LoginActivity 登录成功时顺便把 username 也存进去
        // 现在我们暂时用 ID 举例，或者你可以去 LoginActivity 加一行 prefs.edit().putString("username", user).apply();
        String username = prefs.getString("username", "Library User");
        tvUsername.setText(username);

        // 加载统计数据
        int favCount = dbHelper.getFavoriteCount(userId);
        int reviewCount = dbHelper.getReviewCount(userId);

        tvFavCount.setText(String.valueOf(favCount));
        tvReviewCount.setText(String.valueOf(reviewCount));
    }
}
package com.example.im_chat.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.im_chat.R;

import me.yokeyword.fragmentation.SupportFragment;

/**
 *   user头像布局
 * @auther songjihu
 * @since 2020/2/13 15:45
 */
public class AvatarFragment extends SupportFragment {

    public static AvatarFragment newInstance() {

        Bundle args = new Bundle();

        AvatarFragment fragment = new AvatarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third_avatar, container, false);
        return view;
    }
}

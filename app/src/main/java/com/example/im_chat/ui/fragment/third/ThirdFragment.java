package com.example.im_chat.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.im_chat.R;
import com.example.im_chat.base.BaseMainFragment;


/**
 *   第三个主要fragment
 * @auther songjihu
 * @since 2020/2/13 16:39
 */
public class ThirdFragment extends BaseMainFragment {
    private Toolbar mToolbar;
    private View mView;

    public static ThirdFragment newInstance() {

        Bundle args = new Bundle();

        ThirdFragment fragment = new ThirdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_third, container, false);
        return mView;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        if (findChildFragment(AvatarFragment.class) == null) {
            loadFragment();
        }

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.me);
    }

    private void loadFragment() {
        loadRootFragment(R.id.fl_fourth_container_upper, AvatarFragment.newInstance());
        loadRootFragment(R.id.fl_fourth_container_lower, MeFragment.newInstance());
    }

    public void onBackToFirstFragment() {
        _mBackToFirstListener.onBackToFirstFragment();
    }
}

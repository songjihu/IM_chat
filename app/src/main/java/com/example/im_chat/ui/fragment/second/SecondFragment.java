package com.example.im_chat.ui.fragment.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.im_chat.R;
import com.example.im_chat.base.BaseMainFragment;
import com.example.im_chat.ui.fragment.first.FirstHomeFragmentChat;


public class SecondFragment extends BaseMainFragment {

    public static SecondFragment newInstance() {

        Bundle args = new Bundle();

        SecondFragment fragment = new SecondFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

        if (findChildFragment(FirstHomeFragmentChat.class) == null) {
            // ShopFragment是flow包里的
            loadRootFragment(R.id.fl_third_container, SecondHomeFragmentChat.newInstance());
        }
    }
}

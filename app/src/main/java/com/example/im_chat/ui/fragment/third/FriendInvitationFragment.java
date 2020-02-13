package com.example.im_chat.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import com.example.im_chat.R;

import me.yokeyword.fragmentation.SupportFragment;


/**
 *   处理好友请求的fragment
 * @auther songjihu
 * @since 2020/2/13 15:57
 */
public class FriendInvitationFragment extends SupportFragment {
    private Toolbar mToolbar;

    public static FriendInvitationFragment newInstance() {

        Bundle args = new Bundle();

        FriendInvitationFragment fragment = new FriendInvitationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third_friend_invitation, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbarSettings);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mActivity.onBackPressed();
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}

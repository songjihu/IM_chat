package com.example.im_chat.ui.fragment.third.filefunction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.im_chat.R;
import com.example.im_chat.ui.fragment.third.ThirdFragment;

import me.yokeyword.fragmentation.SupportFragment;


/**
 *   用户个人信息fragment
 * @auther songjihu
 * @since 2020/2/13 15:46
 */
public class FileFragment extends SupportFragment {
    private TextView mTvBtnSettings;

    public static FileFragment newInstance() {

        Bundle args = new Bundle();

        FileFragment fragment = new FileFragment();
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
        mTvBtnSettings = (TextView) view.findViewById(R.id.tv_btn_file_receive);
        mTvBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(FriendFileFragment.newInstance());
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        // 这里实际项目中推荐使用 EventBus接耦
        ((ThirdFragment)getParentFragment()).onBackToFirstFragment();
        return true;
    }
}

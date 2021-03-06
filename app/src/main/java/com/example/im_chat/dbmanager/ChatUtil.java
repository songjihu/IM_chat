package com.example.im_chat.dbmanager;


import com.example.im_chat.db.ChatMessageDao;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;

import java.util.List;

/**
 *   数据库操作
 * @auther songjihu
 * @since 2020/2/2 13:23
 */
public class ChatUtil {

    //获取数据库
   private ChatMessageDao chatDao ;

   private DaoSession daoSession;

    public ChatUtil(){
        //chatDao = GreenDaoManager.getInstance().getSession().getChatMessageDao();
    }


    /**
     *  插入一条数据，插入成功返回true
     * @param msg
     * @return
     */
    public boolean insertMsg(ChatMessage msg){

        boolean flag;
        flag = chatDao.insert(msg)==1?true:false ;//成功插入返回1

        return flag;
        //return true;
    }

    /**
     * 按msg_id删除数据
     * @param msg_id
     */
    public void deleteMsg(String msg_id){
        //chatDao.deleteByKey(msg_id);
    }

    /**
     * 删除所有chat表里面的数据
     */
    public void deleteAllMessagess(){
        chatDao.deleteAll();
    }


    /**
     * 更新msg的一条数据
     * @param msg
     */
    public void updateChatmsg(ChatMessage msg){
        chatDao.update(msg);
    }


    /**
     *  获取最近100条聊天记录
     *  传入分组id
     *  这里面的sql是自己写的
     * @return
     */

    public List<ChatMessage> querryMassages(){

        List<ChatMessage> msgList = chatDao.loadAll();
/*
        daoSession = GreenDaoManager.getInstance().getSession();
        List<ChatMessage>  msgList = new ArrayList<ChatMessage>();
        String sql = "select * from ChatMessage where ";//自定义sql，获取全部表内容
        Cursor cursor = daoSession.getDatabase().rawQuery(sql,null);
        ChatMessage msg;
        while (cursor.moveToNext()){
            msg = new ChatMessage();
            msg.setMsgId(cursor.getString(0));     //获取数据库的第一列
            msg.setMsgContent(cursor.getString(1));//第二列
            msg.setFrom(cursor.getInt(2));         //第三列
            msg.setMsgTimetag(cursor.getString(3));//第四列
            msgList.add(msg);
        }
*/
       return msgList;
    }




}

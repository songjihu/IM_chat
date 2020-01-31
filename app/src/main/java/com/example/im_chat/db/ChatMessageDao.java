package com.example.im_chat.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.example.im_chat.adapter.ChatMessage;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "CHAT_MESSAGE".
*/
public class ChatMessageDao extends AbstractDao<ChatMessage, Long> {

    public static final String TABLENAME = "CHAT_MESSAGE";

    /**
     * Properties of entity ChatMessage.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property MsgId = new Property(0, Long.class, "msgId", true, "_id");
        public final static Property Msg = new Property(1, String.class, "msg", false, "MSG");
    }


    public ChatMessageDao(DaoConfig config) {
        super(config);
    }
    
    public ChatMessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CHAT_MESSAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: msgId
                "\"MSG\" TEXT NOT NULL );"); // 1: msg
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CHAT_MESSAGE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ChatMessage entity) {
        stmt.clearBindings();
 
        Long msgId = entity.getMsgId();
        if (msgId != null) {
            stmt.bindLong(1, msgId);
        }
        stmt.bindString(2, entity.getMsg());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ChatMessage entity) {
        stmt.clearBindings();
 
        Long msgId = entity.getMsgId();
        if (msgId != null) {
            stmt.bindLong(1, msgId);
        }
        stmt.bindString(2, entity.getMsg());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ChatMessage readEntity(Cursor cursor, int offset) {
        ChatMessage entity = new ChatMessage( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // msgId
            cursor.getString(offset + 1) // msg
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ChatMessage entity, int offset) {
        entity.setMsgId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMsg(cursor.getString(offset + 1));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ChatMessage entity, long rowId) {
        entity.setMsgId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ChatMessage entity) {
        if(entity != null) {
            return entity.getMsgId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ChatMessage entity) {
        return entity.getMsgId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
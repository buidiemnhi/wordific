package com.evilgeniustechnologies.Wordrific.daomodel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table USER.
*/
public class UserDao extends AbstractDao<User, Long> {

    public static final String TABLENAME = "USER";

    /**
     * Properties of entity User.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ObjectId = new Property(1, String.class, "objectId", false, "OBJECT_ID");
        public final static Property UpdatedAt = new Property(2, java.util.Date.class, "updatedAt", false, "UPDATED_AT");
        public final static Property Username = new Property(3, String.class, "username", false, "USERNAME");
        public final static Property AvatarUrl = new Property(4, String.class, "avatarUrl", false, "AVATAR_URL");
        public final static Property BestScore = new Property(5, Integer.class, "bestScore", false, "BEST_SCORE");
        public final static Property Email = new Property(6, String.class, "email", false, "EMAIL");
        public final static Property FirstName = new Property(7, String.class, "firstName", false, "FIRST_NAME");
        public final static Property LastName = new Property(8, String.class, "lastName", false, "LAST_NAME");
        public final static Property NextSetToBuy = new Property(9, Integer.class, "nextSetToBuy", false, "NEXT_SET_TO_BUY");
    };

    private DaoSession daoSession;


    public UserDao(DaoConfig config) {
        super(config);
    }
    
    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'USER' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'OBJECT_ID' TEXT NOT NULL ," + // 1: objectId
                "'UPDATED_AT' INTEGER NOT NULL ," + // 2: updatedAt
                "'USERNAME' TEXT," + // 3: username
                "'AVATAR_URL' TEXT," + // 4: avatarUrl
                "'BEST_SCORE' INTEGER," + // 5: bestScore
                "'EMAIL' TEXT," + // 6: email
                "'FIRST_NAME' TEXT," + // 7: firstName
                "'LAST_NAME' TEXT," + // 8: lastName
                "'NEXT_SET_TO_BUY' INTEGER);"); // 9: nextSetToBuy
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'USER'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getObjectId());
        stmt.bindLong(3, entity.getUpdatedAt().getTime());
 
        String username = entity.getUsername();
        if (username != null) {
            stmt.bindString(4, username);
        }
 
        String avatarUrl = entity.getAvatarUrl();
        if (avatarUrl != null) {
            stmt.bindString(5, avatarUrl);
        }
 
        Integer bestScore = entity.getBestScore();
        if (bestScore != null) {
            stmt.bindLong(6, bestScore);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(7, email);
        }
 
        String firstName = entity.getFirstName();
        if (firstName != null) {
            stmt.bindString(8, firstName);
        }
 
        String lastName = entity.getLastName();
        if (lastName != null) {
            stmt.bindString(9, lastName);
        }
 
        Integer nextSetToBuy = entity.getNextSetToBuy();
        if (nextSetToBuy != null) {
            stmt.bindLong(10, nextSetToBuy);
        }
    }

    @Override
    protected void attachEntity(User entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public User readEntity(Cursor cursor, int offset) {
        User entity = new User( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // objectId
            new java.util.Date(cursor.getLong(offset + 2)), // updatedAt
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // username
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // avatarUrl
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // bestScore
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // email
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // firstName
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // lastName
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9) // nextSetToBuy
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setObjectId(cursor.getString(offset + 1));
        entity.setUpdatedAt(new java.util.Date(cursor.getLong(offset + 2)));
        entity.setUsername(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAvatarUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBestScore(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setEmail(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setFirstName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setLastName(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setNextSetToBuy(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(User entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(User entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
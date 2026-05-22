package com.artswipe.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.artswipe.data.local.model.ArtworkEntity;
import com.artswipe.data.local.model.SwipeRecordEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ArtworkDao_Impl implements ArtworkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ArtworkEntity> __insertionAdapterOfArtworkEntity;

  private final EntityInsertionAdapter<SwipeRecordEntity> __insertionAdapterOfSwipeRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteArtwork;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSwipeRecordForArtwork;

  private final SharedSQLiteStatement __preparedStmtOfClearAllSwipeRecords;

  private final SharedSQLiteStatement __preparedStmtOfReshuffleQueue;

  public ArtworkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfArtworkEntity = new EntityInsertionAdapter<ArtworkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `artworks` (`id`,`source`,`title`,`artist`,`year`,`imageUrl`,`styleMovement`,`medium`,`description`,`department`,`sourceUrl`,`randomOrder`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ArtworkEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSource());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getArtist());
        if (entity.getYear() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getYear());
        }
        statement.bindString(6, entity.getImageUrl());
        statement.bindString(7, entity.getStyleMovement());
        if (entity.getMedium() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMedium());
        }
        statement.bindString(9, entity.getDescription());
        if (entity.getDepartment() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDepartment());
        }
        if (entity.getSourceUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getSourceUrl());
        }
        statement.bindDouble(12, entity.getRandomOrder());
      }
    };
    this.__insertionAdapterOfSwipeRecordEntity = new EntityInsertionAdapter<SwipeRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `swipe_records` (`id`,`userId`,`artworkId`,`liked`,`timestamp`,`styleMovement`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SwipeRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getArtworkId());
        final int _tmp = entity.getLiked() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        statement.bindString(6, entity.getStyleMovement());
      }
    };
    this.__preparedStmtOfDeleteArtwork = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM artworks WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM artworks";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSwipeRecordForArtwork = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM swipe_records WHERE artworkId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllSwipeRecords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM swipe_records";
        return _query;
      }
    };
    this.__preparedStmtOfReshuffleQueue = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE artworks SET randomOrder = ABS(RANDOM()) % 1000000 / 1000000.0";
        return _query;
      }
    };
  }

  @Override
  public Object insertArtworks(final List<ArtworkEntity> artworks,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfArtworkEntity.insert(artworks);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSwipeRecord(final SwipeRecordEntity swipeRecord,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSwipeRecordEntity.insert(swipeRecord);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteArtwork(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteArtwork.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteArtwork.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSwipeRecordForArtwork(final String artworkId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSwipeRecordForArtwork.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, artworkId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSwipeRecordForArtwork.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllSwipeRecords(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllSwipeRecords.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllSwipeRecords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object reshuffleQueue(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfReshuffleQueue.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfReshuffleQueue.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ArtworkEntity>> getAllArtworks() {
    final String _sql = "SELECT * FROM artworks";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"artworks"}, new Callable<List<ArtworkEntity>>() {
      @Override
      @NonNull
      public List<ArtworkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final int _cursorIndexOfMedium = CursorUtil.getColumnIndexOrThrow(_cursor, "medium");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfRandomOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "randomOrder");
          final List<ArtworkEntity> _result = new ArrayList<ArtworkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtworkEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            final String _tmpMedium;
            if (_cursor.isNull(_cursorIndexOfMedium)) {
              _tmpMedium = null;
            } else {
              _tmpMedium = _cursor.getString(_cursorIndexOfMedium);
            }
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDepartment;
            if (_cursor.isNull(_cursorIndexOfDepartment)) {
              _tmpDepartment = null;
            } else {
              _tmpDepartment = _cursor.getString(_cursorIndexOfDepartment);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final float _tmpRandomOrder;
            _tmpRandomOrder = _cursor.getFloat(_cursorIndexOfRandomOrder);
            _item = new ArtworkEntity(_tmpId,_tmpSource,_tmpTitle,_tmpArtist,_tmpYear,_tmpImageUrl,_tmpStyleMovement,_tmpMedium,_tmpDescription,_tmpDepartment,_tmpSourceUrl,_tmpRandomOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getArtworkById(final String id,
      final Continuation<? super ArtworkEntity> $completion) {
    final String _sql = "SELECT * FROM artworks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ArtworkEntity>() {
      @Override
      @Nullable
      public ArtworkEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final int _cursorIndexOfMedium = CursorUtil.getColumnIndexOrThrow(_cursor, "medium");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfRandomOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "randomOrder");
          final ArtworkEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            final String _tmpMedium;
            if (_cursor.isNull(_cursorIndexOfMedium)) {
              _tmpMedium = null;
            } else {
              _tmpMedium = _cursor.getString(_cursorIndexOfMedium);
            }
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDepartment;
            if (_cursor.isNull(_cursorIndexOfDepartment)) {
              _tmpDepartment = null;
            } else {
              _tmpDepartment = _cursor.getString(_cursorIndexOfDepartment);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final float _tmpRandomOrder;
            _tmpRandomOrder = _cursor.getFloat(_cursorIndexOfRandomOrder);
            _result = new ArtworkEntity(_tmpId,_tmpSource,_tmpTitle,_tmpArtist,_tmpYear,_tmpImageUrl,_tmpStyleMovement,_tmpMedium,_tmpDescription,_tmpDepartment,_tmpSourceUrl,_tmpRandomOrder);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SwipeRecordEntity>> getAllSwipeRecords() {
    final String _sql = "SELECT * FROM swipe_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"swipe_records"}, new Callable<List<SwipeRecordEntity>>() {
      @Override
      @NonNull
      public List<SwipeRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfArtworkId = CursorUtil.getColumnIndexOrThrow(_cursor, "artworkId");
          final int _cursorIndexOfLiked = CursorUtil.getColumnIndexOrThrow(_cursor, "liked");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final List<SwipeRecordEntity> _result = new ArrayList<SwipeRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SwipeRecordEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpArtworkId;
            _tmpArtworkId = _cursor.getString(_cursorIndexOfArtworkId);
            final boolean _tmpLiked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLiked);
            _tmpLiked = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            _item = new SwipeRecordEntity(_tmpId,_tmpUserId,_tmpArtworkId,_tmpLiked,_tmpTimestamp,_tmpStyleMovement);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLatestSwipeForArtwork(final String artworkId,
      final Continuation<? super SwipeRecordEntity> $completion) {
    final String _sql = "SELECT * FROM swipe_records WHERE artworkId = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, artworkId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SwipeRecordEntity>() {
      @Override
      @Nullable
      public SwipeRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfArtworkId = CursorUtil.getColumnIndexOrThrow(_cursor, "artworkId");
          final int _cursorIndexOfLiked = CursorUtil.getColumnIndexOrThrow(_cursor, "liked");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final SwipeRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpArtworkId;
            _tmpArtworkId = _cursor.getString(_cursorIndexOfArtworkId);
            final boolean _tmpLiked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLiked);
            _tmpLiked = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            _result = new SwipeRecordEntity(_tmpId,_tmpUserId,_tmpArtworkId,_tmpLiked,_tmpTimestamp,_tmpStyleMovement);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ArtworkEntity>> getUnswipedArtworks() {
    final String _sql = "SELECT * FROM artworks WHERE id NOT IN (SELECT artworkId FROM swipe_records) ORDER BY randomOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"artworks",
        "swipe_records"}, new Callable<List<ArtworkEntity>>() {
      @Override
      @NonNull
      public List<ArtworkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final int _cursorIndexOfMedium = CursorUtil.getColumnIndexOrThrow(_cursor, "medium");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfRandomOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "randomOrder");
          final List<ArtworkEntity> _result = new ArrayList<ArtworkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtworkEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            final String _tmpMedium;
            if (_cursor.isNull(_cursorIndexOfMedium)) {
              _tmpMedium = null;
            } else {
              _tmpMedium = _cursor.getString(_cursorIndexOfMedium);
            }
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDepartment;
            if (_cursor.isNull(_cursorIndexOfDepartment)) {
              _tmpDepartment = null;
            } else {
              _tmpDepartment = _cursor.getString(_cursorIndexOfDepartment);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final float _tmpRandomOrder;
            _tmpRandomOrder = _cursor.getFloat(_cursorIndexOfRandomOrder);
            _item = new ArtworkEntity(_tmpId,_tmpSource,_tmpTitle,_tmpArtist,_tmpYear,_tmpImageUrl,_tmpStyleMovement,_tmpMedium,_tmpDescription,_tmpDepartment,_tmpSourceUrl,_tmpRandomOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ArtworkEntity>> getLikedArtworks() {
    final String _sql = "SELECT * FROM artworks WHERE id IN (SELECT artworkId FROM swipe_records WHERE liked = 1)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"artworks",
        "swipe_records"}, new Callable<List<ArtworkEntity>>() {
      @Override
      @NonNull
      public List<ArtworkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStyleMovement = CursorUtil.getColumnIndexOrThrow(_cursor, "styleMovement");
          final int _cursorIndexOfMedium = CursorUtil.getColumnIndexOrThrow(_cursor, "medium");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfRandomOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "randomOrder");
          final List<ArtworkEntity> _result = new ArrayList<ArtworkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtworkEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpStyleMovement;
            _tmpStyleMovement = _cursor.getString(_cursorIndexOfStyleMovement);
            final String _tmpMedium;
            if (_cursor.isNull(_cursorIndexOfMedium)) {
              _tmpMedium = null;
            } else {
              _tmpMedium = _cursor.getString(_cursorIndexOfMedium);
            }
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDepartment;
            if (_cursor.isNull(_cursorIndexOfDepartment)) {
              _tmpDepartment = null;
            } else {
              _tmpDepartment = _cursor.getString(_cursorIndexOfDepartment);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final float _tmpRandomOrder;
            _tmpRandomOrder = _cursor.getFloat(_cursorIndexOfRandomOrder);
            _item = new ArtworkEntity(_tmpId,_tmpSource,_tmpTitle,_tmpArtist,_tmpYear,_tmpImageUrl,_tmpStyleMovement,_tmpMedium,_tmpDescription,_tmpDepartment,_tmpSourceUrl,_tmpRandomOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

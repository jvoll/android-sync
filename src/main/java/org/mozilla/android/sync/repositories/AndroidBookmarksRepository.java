package org.mozilla.android.sync.repositories;

import java.util.ArrayList;

import org.mozilla.android.sync.repositories.domain.BookmarkRecord;
import org.mozilla.android.sync.repositories.domain.Record;

import android.content.Context;
import android.database.Cursor;

public class AndroidBookmarksRepository implements Repository {

  BookmarksDatabaseHelper dbHelper;
  
  public AndroidBookmarksRepository(Context context) {
    dbHelper = new BookmarksDatabaseHelper(context);
  }
  
  // guids since method and thread
  public void guidsSince(long timestamp, RepositoryCallbackReceiver receiver) {
    GuidsSinceThread thread = new GuidsSinceThread(timestamp, receiver, dbHelper);
    thread.start();
  }
  
  class GuidsSinceThread extends Thread {

    long timestamp;
    RepositoryCallbackReceiver callbackReceiver;
    BookmarksDatabaseHelper dbHelper;
    
    public GuidsSinceThread(long timestamp, RepositoryCallbackReceiver callbackReceiver, BookmarksDatabaseHelper dbHelper) {
      this.timestamp = timestamp;
      this.callbackReceiver = callbackReceiver;
      this.dbHelper = dbHelper;
      // start(); ???? do we need this if we call start externally?
    }
    
    public void run() {
      //Display info about this particular thread
      System.out.println(Thread.currentThread().getName());
      
      Cursor cur = dbHelper.getGUIDSSince(timestamp);
      int index = cur.getColumnIndex(BookmarksDatabaseHelper.COL_GUID);
     
      ArrayList<String> guids = new ArrayList<String>();
      
      while (cur.isAfterLast() == false) {
        guids.add(cur.getString(index));
        cur.moveToNext();
      }
      
      callbackReceiver.guidsSinceCallback(RepoStatusCode.DONE, (String[]) guids.toArray());
      
    }
  }

  // Fetch since method and thread
  public void fetchSince(long timestamp, RepositoryCallbackReceiver receiver) {
    FetchSinceThread thread = new FetchSinceThread(timestamp, receiver);
    thread.start();
  }
  
  class FetchSinceThread extends Thread {
    
    long timestamp;
    RepositoryCallbackReceiver callbackReceiver;
    
    public FetchSinceThread(long timestamp, RepositoryCallbackReceiver callbackReceiver ) {
      this.timestamp = timestamp;
      this.callbackReceiver = callbackReceiver;
      // start(); ???? do we need this if we call start externally?
    }
    
    public void run() {
      //Display info about this particular thread
      System.out.println(Thread.currentThread().getName());
      
      Cursor cur = dbHelper.fetchSince(timestamp);
      ArrayList<BookmarkRecord> records = new ArrayList<BookmarkRecord>();
      while (cur.isAfterLast() == false) {
        records.add(getRecord(cur));
        cur.moveToNext();
      }
      
      callbackReceiver.fetchSinceCallback(RepoStatusCode.DONE, (BookmarkRecord[]) records.toArray());
      
    }
  }

  // Fetch method and thread
  public void fetch(String[] guids, RepositoryCallbackReceiver receiver) {
    FetchThread thread = new FetchThread(guids, receiver);
    thread.start();
  }
  
  class FetchThread extends Thread {
    String[] guids;
    RepositoryCallbackReceiver callbackReceiver;
    
    public FetchThread(String[] guids, RepositoryCallbackReceiver callbackReceiver) {
      this.guids = guids;
      this.callbackReceiver = callbackReceiver;
    }
    
    public void run() {
      //Display info about this particular thread
      System.out.println(Thread.currentThread().getName());
      
      Cursor cur = dbHelper.fetch(guids);
      ArrayList<BookmarkRecord> records = new ArrayList<BookmarkRecord>();
      while (cur.isAfterLast() == false) {
        records.add(getRecord(cur));
        cur.moveToNext();
      }
      
      callbackReceiver.fetchSinceCallback(RepoStatusCode.DONE, (BookmarkRecord[]) records.toArray());
    }
  }

  // Not doing store async, starting a thread is more overhead
  // than just doing the operation here (and old code doesn't
  // use async here)
  // return the id of the inserted value
  public long store(Record record) {
    long id = dbHelper.insertBookmark((BookmarkRecord) record);
    return id;
  }

  // Wipe method and thread
  // Right now doing this async probably seems silly,
  // but I imagine it might be worth it once the implmentation
  // of this is complete (plus I'm sticking with past conventions)
  public void wipe(RepositoryCallbackReceiver receiver) {
    WipeThread thread = new WipeThread(receiver);
    thread.start();
  }
  
  class WipeThread extends Thread {
    
    RepositoryCallbackReceiver callbackReceiver;
    
    public WipeThread(RepositoryCallbackReceiver callbackReciever) {
      this.callbackReceiver = callbackReciever;
    }
    
    public void run() {
      //Display info about this particular thread
      System.out.println(Thread.currentThread().getName());
      dbHelper.wipe();
      callbackReceiver.wipeCallback(RepoStatusCode.DONE);
    }
  }

  public void begin(RepositoryCallbackReceiver receiver) {
    // TODO Auto-generated method stub
    
  }

  public void finish(RepositoryCallbackReceiver receiver) {
    // TODO Auto-generated method stub
    
  }
  
  // Create a BookmarkRecord object from a cursor on a row with a Bookmark in it
  public static BookmarkRecord getRecord(Cursor cur) {
    
    BookmarkRecord rec = new BookmarkRecord();
    rec.setId(getStringValue(cur, BookmarksDatabaseHelper.COL_ID));
    rec.setGuid(getStringValue(cur, BookmarksDatabaseHelper.COL_GUID));
    rec.setAndroidId(getStringValue(cur, BookmarksDatabaseHelper.COL_ANDROID_ID));
    rec.setTitle(getStringValue(cur, BookmarksDatabaseHelper.COL_TITLE));
    rec.setBmkUri(getStringValue(cur, BookmarksDatabaseHelper.COL_BMK_URI));
    rec.setDescription(getStringValue(cur, BookmarksDatabaseHelper.COL_DESCRIP));
    rec.setLoadInSidebar(cur.getInt(cur.getColumnIndex(BookmarksDatabaseHelper.COL_LOAD_IN_SIDEBAR)) == 1 ? true: false );
    rec.setTags(getStringValue(cur, BookmarksDatabaseHelper.COL_TAGS));
    rec.setKeyword(getStringValue(cur, BookmarksDatabaseHelper.COL_KEYWORD));
    rec.setParentId(getStringValue(cur, BookmarksDatabaseHelper.COL_PARENT_ID));
    rec.setParentName(getStringValue(cur, BookmarksDatabaseHelper.COL_PARENT_NAME));
    rec.setType(getStringValue(cur, BookmarksDatabaseHelper.COL_TYPE));
    rec.setGeneratorUri(getStringValue(cur, BookmarksDatabaseHelper.COL_GENERATOR_URI));
    rec.setStaticTitle(getStringValue(cur, BookmarksDatabaseHelper.COL_STATIC_TITLE));
    rec.setFolderName(getStringValue(cur, BookmarksDatabaseHelper.COL_FOLDER_NAME));
    rec.setQueryId(getStringValue(cur, BookmarksDatabaseHelper.COL_QUERY_ID));
    rec.setSiteUri(getStringValue(cur, BookmarksDatabaseHelper.COL_SITE_URI));
    rec.setFeedUri(getStringValue(cur, BookmarksDatabaseHelper.COL_FEED_URI));
    rec.setPos(getStringValue(cur, BookmarksDatabaseHelper.COL_POS));
    rec.setChildren(getStringValue(cur, BookmarksDatabaseHelper.COL_CHILDREN));
    return rec;
  }
  
  private static String getStringValue(Cursor cur, String columnName) {
    return cur.getString(cur.getColumnIndex(columnName));
  }

}

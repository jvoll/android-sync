package org.mozilla.android.sync.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.android.sync.MainActivity;
import org.mozilla.android.sync.repositories.BookmarksRepository;
import org.mozilla.android.sync.repositories.BookmarksRepositorySession;
import org.mozilla.android.sync.repositories.CollectionType;
import org.mozilla.android.sync.repositories.RepoStatusCode;
import org.mozilla.android.sync.repositories.Utils;
import org.mozilla.android.sync.repositories.domain.BookmarkRecord;
import org.mozilla.android.sync.repositories.domain.Record;
import org.mozilla.android.sync.test.CallbackResult.CallType;

import android.content.Context;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TestAndroidBookmarksRepo {

  private BookmarksRepositorySession session;
  private BookmarksSessionTestWrapper testWrapper;
  
  @Before
  public void setUp() {
    
    // Create a testWrapper instance
    setTestWrapper(new BookmarksSessionTestWrapper());
    
    // Create the session used by tests
    BookmarksRepository repo = new BookmarksRepository(CollectionType.Bookmarks);
    Context context = new MainActivity().getApplicationContext();
    CallbackResult result = testWrapper.doCreateSessionSync(repo, context);
    
    // Check that we got a valid session back
    assertEquals(result.getStatusCode(), RepoStatusCode.DONE);
    assertEquals(result.getCallType(), CallType.CREATE_SESSION);
    assert(result.getSession() != null);
    
    // Set the session
    setSession((BookmarksRepositorySession) result.getSession());
    
  }
  
  @Test
  public void testStore() {
    
    // Create a record to store
    CallbackResult result = getTestWrapper().doStoreSync(session, createBookmark1());
    
    System.out.println("Stored a record and got back id: " + result.getRowId());
    
    assertEquals(CallType.STORE, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
  }
  
  @Test
  public void testFetchAll() {
    
    // Create a record and store it
    CallbackResult result = testWrapper.doStoreSync(session, createBookmark1());
    System.out.println("Stored record with id: " + result.getRowId());
    
    // Create a second record and store it 
    result = testWrapper.doStoreSync(session, createBookmark2());
    System.out.println("Stored record with id: " + result.getRowId());
    
    // Get records
    result = testWrapper.doFetchAllSync(session);
    
    System.out.println("Number of records returned: " + result.getRecords().length);
    
    assertEquals(CallType.FETCH_ALL, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
    
    // TODO: Do something to check that we got some records here. Need to perform a setup
    // function first to make sure there are records in there to be got.
  }
  
  @Test
  public void testGuidsSince() {
    
    // Create a record and store it
    CallbackResult result = testWrapper.doStoreSync(session, createBookmark1());
    System.out.println("Stored record with id: " + result.getRowId());
    
    // Get records
    result = testWrapper.doGuidsSinceSync(session, (System.currentTimeMillis() - 100000000)/1000);
    
    System.out.println("Number of records returned: " + result.getGuids().length);
    
    assertEquals(CallType.GUIDS_SINCE, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
    
    // TODO: Do something to check that we got some records here. Need to perform a setup
    // function first to make sure there are records in there to be got.
  }
  
  // TODO Test for guids since where there are none
  
  // TODO Test for guids since where some should come back and some shouldn't
  
  @Test
  public void testFetchRecordForGuid() {
    // Create two records and store them
    BookmarkRecord record = createBookmark1();
    String guid = record.getGuid();
    CallbackResult result = testWrapper.doStoreSync(session, record);
    System.out.println("Stored record with id: " + result.getRowId());
    result = testWrapper.doStoreSync(session, createBookmark2());
    System.out.println("Stored record with id: " + result.getRowId());
    
    // Fetch record with guid from above and ensure we only get back one record
    result = testWrapper.doFetchSync(session, new String[] { guid });
    System.out.println("Number of records returned: " + result.getRecords().length);
    
    assertEquals(CallType.FETCH, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
    
    // Check that only one record was returned and that it is the correct one
    Record[] returnedRecords = result.getRecords();
    assertEquals(1, returnedRecords.length);
    BookmarkRecord fetched = (BookmarkRecord) returnedRecords[0];
    assertEquals(guid, fetched.getGuid());
    assertEquals(record.getBmkUri(), fetched.getBmkUri());
    assertEquals(record.getDescription(), fetched.getDescription());
    assertEquals(record.getTitle(), fetched.getTitle());
    
  }
  
  // TODO Test for retrieving multiple guids
  
  @Test
  public void testFetchSince() {
    // Create two records and store them
    BookmarkRecord record = createBookmark1();
    CallbackResult result = testWrapper.doStoreSync(session, record);
    System.out.println("Stored record with id: " + result.getRowId());
    result = testWrapper.doStoreSync(session, createBookmark2());
    System.out.println("Stored record with id: " + result.getRowId());
    
    // Fetch record with guid from above and ensure we only get back one record
    result = testWrapper.doFetchSinceSync(session, (System.currentTimeMillis() - 10000000)/1000);
    System.out.println("Number of records returned: " + result.getRecords().length);
    
    assertEquals(CallType.FETCH_SINCE, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
    
    // Check that both records were returned
    Record[] returnedRecords = result.getRecords();
    assertEquals(2, returnedRecords.length);
    
  }
  
  // TODO Test for retrieving multiple guids
  
  // Helpers
  private static BookmarkRecord createBookmark1() {
    BookmarkRecord record = new BookmarkRecord();
    record.setBmkUri("http://foo.bar.com");
    record.setDescription("This is a description for foo.bar.com");
    record.setType("bookmark");
    record.setTitle("Foo!!!");
    record.setGuid(Utils.generateGuid());
    return record;
  }
  
  private static BookmarkRecord createBookmark2() {
    BookmarkRecord record = new BookmarkRecord();
    record.setBmkUri("http://boo.bar.com");
    record.setDescription("Describe boo.bar.com");
    record.setType("bookmark");
    record.setTitle("boo!!!");
    record.setGuid(Utils.generateGuid());
    return record;
  }
  
  // Accessors and mutators
  public BookmarksRepositorySession getSession() {
    return session;
  }

  public void setSession(BookmarksRepositorySession session) {
    this.session = session;
  }

  public BookmarksSessionTestWrapper getTestWrapper() {
    return testWrapper;
  }

  public void setTestWrapper(BookmarksSessionTestWrapper testWrapper) {
    this.testWrapper = testWrapper;
  }
  
}
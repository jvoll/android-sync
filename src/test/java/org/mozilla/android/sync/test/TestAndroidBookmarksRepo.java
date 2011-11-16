package org.mozilla.android.sync.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.android.sync.MainActivity;
import org.mozilla.android.sync.repositories.BookmarksRepository;
import org.mozilla.android.sync.repositories.BookmarksRepositorySession;
import org.mozilla.android.sync.repositories.CollectionType;
import org.mozilla.android.sync.repositories.RepoStatusCode;
import org.mozilla.android.sync.repositories.Repository;
import org.mozilla.android.sync.repositories.SyncCallbackReceiver;
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
  private static String parentId;
  private static String parentName = "Menu";
  
  @BeforeClass
  public static void oneTimeSetUp() {
    parentId = Utils.generateGuid();
  }
  
  @Before
  public void setUp() {
    
    // Create a testWrapper instance
    setTestWrapper(new BookmarksSessionTestWrapper());
    
    // Create the session used by tests
    BookmarksRepository repo = (BookmarksRepository) Repository.makeRepository(CollectionType.Bookmarks);
    Context context = new MainActivity().getApplicationContext();
    CallbackResult result = testWrapper.doCreateSessionSync(repo, context);
    
    // Check that we got a valid session back
    assertEquals(result.getStatusCode(), RepoStatusCode.DONE);
    assertEquals(result.getCallType(), CallType.CREATE_SESSION);
    assert(result.getSession() != null);
    
    // Set the session
    setSession((BookmarksRepositorySession) result.getSession());
    
  }
  
  /*
   * Tests for createSession
   */
  @Test
  public void testCreateSessionNullContext() {
    BookmarksRepository repo = (BookmarksRepository) Repository.makeRepository(CollectionType.Bookmarks);
    CallbackResult result = testWrapper.doCreateSessionSync(repo, null);
    assertEquals(RepoStatusCode.NULL_CONTEXT, result.getStatusCode());
  }
  
  /*
   * Tests for store
   * 
   * Test storing a record for each different type of Bookmark record
   */
  @Test
  public void testStoreBookmark() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createBookmark1());
    verifyStoreResult(result);
  }
  
  @Test
  public void testStoreMicrosummary() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createMicrosummary());
    verifyStoreResult(result);
  }
  
  @Test
  public void testStoreQuery() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createQuery());
    verifyStoreResult(result);
  }
  
  @Test
  public void testStoreFolder() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createFolder());
    verifyStoreResult(result);
  }
  
  @Test
  public void testStoreLivemark() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createLivemark());
    verifyStoreResult(result);
  }
  
  @Test
  public void testStoreSeparator() {
    CallbackResult result = getTestWrapper().doStoreSync(session, createSeparator());
    verifyStoreResult(result);
  }
  
  // TODO not sure whether it is worth leaving this in, probably never actually need
  // this call for anything. I wrote it for testing and existing clients don't use it.
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
  
  /*
   * Helpers for creating bookmark records of different types
   */
  private static BookmarkRecord createBookmark1() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setTitle("Foo!!!");
    record.setBmkUri("http://foo.bar.com");
    record.setDescription("This is a description for foo.bar.com");
    record.setLoadInSidebar(true);
    record.setTags("[\"tag1\", \"tag2\", \"tag3\"]");
    record.setKeyword("fooooozzzzz");
    record.setParentId(parentId);
    record.setParentName(parentName);
    record.setType("bookmark");
    return record;
  }
  
  private static BookmarkRecord createBookmark2() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setTitle("Bar???");
    record.setBmkUri("http://bar.foo.com");
    record.setDescription("This is a description for Bar???");
    record.setLoadInSidebar(false);
    record.setTags("[\"tag1\", \"tag2\"]");
    record.setKeyword("keywordzzz");
    record.setParentId(parentId);
    record.setParentName(parentName);
    record.setType("bookmark");
    return record;
  }
  
  private static BookmarkRecord createMicrosummary() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setGeneratorUri("http://generatoruri.com");
    record.setStaticTitle("Static Microsummary Title");
    record.setTitle("Microsummary 1");
    record.setBmkUri("www.bmkuri.com");
    record.setDescription("microsummary description");
    record.setLoadInSidebar(false);
    record.setTags("[\"tag1\", \"tag2\"]");
    record.setKeyword("keywordzzz");
    record.setParentId(parentId);
    record.setParentName(parentName);
    record.setType("microsummary");
    return record;
  }
  
  private static BookmarkRecord createQuery() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setFolderName("Query Folder Name");
    record.setQueryId("OptionalQueryId");
    record.setTitle("Query 1");
    record.setBmkUri("http://www.query.com");
    record.setDescription("Query 1 description");
    record.setLoadInSidebar(true);
    record.setTags("[]");
    record.setKeyword("queryKeyword");
    record.setParentId(parentId);
    record.setParentName(parentName);
    record.setType("query");
    return record;
  }
  
  private static BookmarkRecord createFolder() {
    // Make this the Menu folder since each DB will
    // have at least this folder
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(parentId);
    record.setTitle(parentName);
    // No parent since this is the menu folder
    record.setParentId("");
    record.setParentName("");
    // TODO verify how we want to store these string arrays
    // pretty sure I verified that this is actually how other clients do it, but double check
    record.setChildren("[\"" + Utils.generateGuid() + "\", \"" + Utils.generateGuid() + "\"]");
    record.setType("folder");
    return record;
  }
  
  private static BookmarkRecord createLivemark() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setSiteUri("http://site.uri.com");
    record.setFeedUri("http://rss.site.uri.com");
    record.setTitle("Livemark title");
    record.setParentId(parentId);
    record.setParentName(parentName);
    // TODO verify how we want to store these string arrays
    // pretty sure I verified that this is actually how other clients do it, but double check
    record.setChildren("[\"" + Utils.generateGuid() + "\", \"" + Utils.generateGuid() + "\"]");
    record.setType("livemark");
    return record;
  }
  
  private static BookmarkRecord createSeparator() {
    BookmarkRecord record = new BookmarkRecord();
    record.setGuid(Utils.generateGuid());
    record.setPos("3");
    record.setParentId(parentId);
    record.setParentName(parentName);
    record.setType("separator");
    return record;
  }
  
  /*
   * Other helpers
   */
  private void verifyStoreResult(CallbackResult result) {
    assert(result.getRowId() != CallbackResult.DEFAULT_ROW_ID);
    assertEquals(CallType.STORE, result.getCallType());
    assertEquals(RepoStatusCode.DONE, result.getStatusCode());
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
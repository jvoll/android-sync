package org.mozilla.android.sync.repositories;

import android.content.Context;

public class BookmarksRepository extends Repository {

  public BookmarksRepository(CollectionType collection) {
    super(collection);
  }

  // TODO it is annoying to have to pass the context around to get access to the DB...is there anywhere
  // else I can get this from rather than passing it around?
  @Override
  public void createSession(SyncCallbackReceiver callbackMechanism, Context context) {
    BookmarksRepositorySession session = new BookmarksRepositorySession(this, callbackMechanism, context);
    callbackMechanism.sessionCallback(RepoStatusCode.DONE, session);
  }

}

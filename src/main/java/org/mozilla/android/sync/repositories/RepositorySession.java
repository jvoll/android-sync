package org.mozilla.android.sync.repositories;

public interface RepositorySession {
  
  // TODO: haven't really plugged the notion of sessions in yet,
  // this will come later. For now, just working on actually
  // being able to get/store records.
  public void guidsSince(long timestamp, RepositoryCallbackReceiver receiver);
  
}

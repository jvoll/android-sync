package org.mozilla.android.sync.repositories;

import org.mozilla.android.sync.repositories.domain.Record;


public interface Repository {
  
  public void guidsSince(long timestamp, RepositoryCallbackReceiver receiver);
  public void fetchSince(long timestamp, RepositoryCallbackReceiver receiver);
  public void fetch(String[] guids, RepositoryCallbackReceiver receiver);
  public long store(Record record);
  public void wipe(RepositoryCallbackReceiver receiver);
  public void begin(RepositoryCallbackReceiver receiver);
  public void finish(RepositoryCallbackReceiver receiver);
  
  
}

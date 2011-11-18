/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Android Sync Client.
 *
 * The Initial Developer of the Original Code is
 * the Mozilla Foundation.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jason Voll <jvoll@mozilla.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.android.sync.repositories;

import android.content.Context;

public class BookmarksRepository extends Repository {

  // protected constructor to force use of Repository static factory method makeRepository
  protected BookmarksRepository() { }
  
  // TODO it is annoying to have to pass the context around to get access to the DB...is there anywhere
  // else I can get this from rather than passing it around?
  
  // TODO this needs to happen in a thread :S
  public void createSession(Context context, SyncCallbackReceiver callbackMechanism,long lastSyncTimestamp) {
    CreateSessionThread thread = new CreateSessionThread(context, callbackMechanism, lastSyncTimestamp);
    thread.start();
  }
  
  class CreateSessionThread extends Thread {

    private Context context;
    private SyncCallbackReceiver callbackMechanism;
    private long lastSyncTimestamp;
    
    public CreateSessionThread(Context context, SyncCallbackReceiver callbackMechanism,
        long lastSyncTimestamp) {
      this.context = context;
      this.callbackMechanism = callbackMechanism;
      this.lastSyncTimestamp = lastSyncTimestamp;
    }
    
    public void run() {
      if (context == null) {
        callbackMechanism.sessionCallback(RepoStatusCode.NULL_CONTEXT, null);
        return;
      }
      BookmarksRepositorySession session = new BookmarksRepositorySession(BookmarksRepository.this, callbackMechanism, context, lastSyncTimestamp);
      callbackMechanism.sessionCallback(RepoStatusCode.DONE, session);
    }
  }

}

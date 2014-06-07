/*
 * Copyright 2014 KiwiAndroidDev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kiwiandroiddev.gmailradiodriveextension.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.kiwiandroiddev.radiodrive.api.IAnnouncement;
import com.kiwiandroiddev.radiodrive.api.IAnnouncementManager;

/**
 * Base class for announcements. The simplest possible announcement would
 * just override play() to return a message to be read out when appropriate.
 *
 * Manifest meta-data documentation here...
 *
 * Created by matt on 20/04/14.
 */
public class RadioDriveAnnouncement extends Service {

    public static final String TAG = "RadioDriveExtension";

    /**
     * The {@link android.content.Intent} action representing a RadioDrive extension. This service should
     * declare an <code>&lt;intent-filter&gt;</code> for this action in order to register with
     * RadioDrive.
     */
    public static final String ACTION_EXTENSION = "com.kiwiandroiddev.radiodrive.Extension";

    /**
     * The permission that RadioDrive extensions should require callers to have before providing
     * any status updates. Permission checks are implemented automatically by the base class.
     */
    public static final String PERMISSION_READ_EXTENSION_DATA
            = "com.kiwiandroiddev.radiodrive.permission.READ_EXTENSION_DATA";

    private boolean mInitialized = false;
    private IAnnouncementManager mHost;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IAnnouncement.Stub mBinder = new IAnnouncement.Stub() {
        @Override
        public void onInitialize(IAnnouncementManager manager) {
            mHost = manager;

            if (!mInitialized) {
                RadioDriveAnnouncement.this.onInitialize();
                mInitialized = true;
            }
        }

        @Override
        public void onRemove() {
            RadioDriveAnnouncement.this.onRemove();
        }

        @Override
        public void onRealtimeAnnouncementPermissionChange(boolean permitted) {
            RadioDriveAnnouncement.this.onRealtimeAnnouncementPermissionChange(permitted);
        }

        @Override
        public String play() {
            return RadioDriveAnnouncement.this.play();
        }
    };

    public String play() {
        return null;
    }

    public void onRealtimeAnnouncementPermissionChange(boolean permitted) {

    }

    public void onRemove() {

    }

    public void onInitialize() {

    }

    public void requestRealtimeAnnouncement(String message, boolean shouldPause) {
        try {
            mHost.requestRealtimeAnnouncement(message, shouldPause);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn\'t request realtime announcement", e);
        }
    }

    public boolean realtimeAnnouncementsPermitted() {
        try {
            return mHost.realtimeAnnouncementsPermitted();
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn\'t query realtime announcement permitted status", e);
            return false;
        }
    }
}

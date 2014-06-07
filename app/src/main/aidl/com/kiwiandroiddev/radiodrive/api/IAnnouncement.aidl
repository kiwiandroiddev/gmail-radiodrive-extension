package com.kiwiandroiddev.radiodrive.api;

import com.kiwiandroiddev.radiodrive.api.IAnnouncementManager;

interface IAnnouncement {
    oneway void onInitialize(IAnnouncementManager manager);
    oneway void onRemove();
    oneway void onRealtimeAnnouncementPermissionChange(boolean permitted);
    String play();
}
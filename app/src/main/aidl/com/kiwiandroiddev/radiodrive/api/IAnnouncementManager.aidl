package com.kiwiandroiddev.radiodrive.api;

interface IAnnouncementManager {
    oneway void requestRealtimeAnnouncement(String message,
                                            boolean shouldPause);

    boolean realtimeAnnouncementsPermitted();
}
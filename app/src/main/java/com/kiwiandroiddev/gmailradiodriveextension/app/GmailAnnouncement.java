/*
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads unread message count for Gmail accounts set up on device.
 *
 * Adapted from DashClock Gmail extension source code (GmailExtension.java):
 *
 * https://code.google.com/p/dashclock/
 *
 * Created by matt on 7/06/14.
 */
public class GmailAnnouncement extends RadioDriveAnnouncement {

    public static final String TAG = "GmailAnnouncement";

    public static final String PREF_ACCOUNTS = "pref_gmail_accounts";
    public static final String PREF_LABEL = "pref_gmail_label";

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    private static final String SECTIONED_INBOX_CANONICAL_NAME_PREFIX = "^sq_ig_i_";
    private static final String SECTIONED_INBOX_CANONICAL_NAME_PERSONAL = "^sq_ig_i_personal";

    static String[] getAllAccountNames(Context context) {
        final Account[] accounts = AccountManager.get(context).getAccountsByType(
                ACCOUNT_TYPE_GOOGLE);
        final String[] accountNames = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            accountNames[i] = accounts[i].name;
        }
        return accountNames;
    }

    private Set<String> getSelectedAccounts() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String[] accounts = getAllAccountNames(this);
        Set<String> allAccountsSet = new HashSet<String>();
        allAccountsSet.addAll(Arrays.asList(accounts));
        return sp.getStringSet(PREF_ACCOUNTS, allAccountsSet);
    }

    @Override
    public void onInitialize() {
        Log.d(TAG, "onInitialize() called");
    }

    @Override
    public String play() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String labelCanonical = sp.getString(PREF_LABEL, "i");
        Set<String> selectedAccounts = getSelectedAccounts();

        if ("i".equals(labelCanonical)) {
            labelCanonical = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX;
        } else if ("p".equals(labelCanonical)) {
            labelCanonical = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_PRIORITY_INBOX;
        }

        int unread = 0;
        List<Pair<String, Integer>> unreadPerAccount = new ArrayList<Pair<String, Integer>>();
        String lastUnreadLabelUri = null;

        for (String account : selectedAccounts) {
            Cursor cursor = tryOpenLabelsCursor(account);
            if (cursor == null || cursor.isAfterLast()) {
                Log.d(TAG, "No Gmail inbox information found for account.");
                if (cursor != null) {
                    cursor.close();
                }
                continue;
            }

            int accountUnread = 0;

            while (cursor.moveToNext()) {
                int thisUnread = cursor.getInt(LabelsQuery.NUM_UNREAD_CONVERSATIONS);
                String thisCanonicalName = cursor.getString(LabelsQuery.CANONICAL_NAME);
                if (labelCanonical.equals(thisCanonicalName)) {
                    accountUnread = thisUnread;
                    if (thisUnread > 0) {
                        lastUnreadLabelUri = cursor.getString(LabelsQuery.URI);
                    }
                    break;
                } else if (!TextUtils.isEmpty(thisCanonicalName)
                        && thisCanonicalName.startsWith(SECTIONED_INBOX_CANONICAL_NAME_PREFIX)) {
                    accountUnread += thisUnread;
                    if (thisUnread > 0
                            && SECTIONED_INBOX_CANONICAL_NAME_PERSONAL.equals(thisCanonicalName)) {
                        lastUnreadLabelUri = cursor.getString(LabelsQuery.URI);
                    }
                }
            }

            if (accountUnread > 0) {
                unreadPerAccount.add(new Pair<String, Integer>(account, accountUnread));
                unread += accountUnread;
            }

            cursor.close();
        }

        // STUB
        Log.d(TAG, "getSelectedAccounts() = " + getSelectedAccounts());
        for (Pair<String, Integer> unreadForAccount : unreadPerAccount) {
            Log.d(TAG, unreadForAccount.first + " = " + unreadForAccount.second);
        }

        return "You have " + unread + " unread Gmail messages.";
    }

    @Override
    public void onRemove() {
        Log.d(TAG, "onRemove() called");
    }

    private Cursor tryOpenLabelsCursor(String account) {
        try {
            return getContentResolver().query(
                    GmailContract.Labels.getLabelsUri(account),
                    LabelsQuery.PROJECTION,
                    null, // NOTE: the Labels API doesn't allow selections here
                    null,
                    null);

        } catch (Exception e) {
            // From developer console: "Permission Denial: opening provider com.google.android.gsf..
            // From developer console: "SQLiteException: no such table: labels"
            // From developer console: "NullPointerException"
            Log.e(TAG, "Error opening Gmail labels", e);
            return null;
        }
    }

    private interface LabelsQuery {
        String[] PROJECTION = {
                GmailContract.Labels.NUM_UNREAD_CONVERSATIONS,
                GmailContract.Labels.URI,
                GmailContract.Labels.CANONICAL_NAME,
        };

        int NUM_UNREAD_CONVERSATIONS = 0;
        int URI = 1;
        int CANONICAL_NAME = 2;
    }
}

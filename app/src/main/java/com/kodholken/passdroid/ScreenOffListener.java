package com.kodholken.passdroid;

import android.content.BroadcastReceiver;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.content.ClipboardManager;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ScreenOffListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean clear = PreferenceManager.getDefaultSharedPreferences(context).
                                          getBoolean("clear_clipboard", true);

        if (clear) {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            
            String clipboardPassword = Session.getInstance().getClipboardPassword();
            String clipboardData = null;
            if (cm.hasPrimaryClip() && cm.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                ClipData.Item item = cm.getPrimaryClip().getItemAt(0);
                clipboardData = item.getText().toString();
            }
            // We only clear the clipboard when the content equals the last
            // copied password.
            if (clipboardData != null && clipboardData.equals(clipboardPassword)) {
                ClipData data = ClipData.newPlainText("", "");
                cm.setPrimaryClip(data);
                Session.getInstance().setClipboardPassword(null);
            }

        }
    }
}

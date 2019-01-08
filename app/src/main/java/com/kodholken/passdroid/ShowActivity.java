/*    
    This file is part of the Passdroid password management software.
    
    Copyright (C) 2009-2012  Magnus Eriksson <eriksson.mag@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.kodholken.passdroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kodholken.passdroid.db.PasswordData;

/**
 * Activity that displays a password entry and let the user choose to edit or
 * delete the entry.
 */
public class ShowActivity extends AppCompatTimeoutActivity {
    private long passwordId;
    private String system;
    private String username;
    private String password;
    private String note;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        passwordId = extras.getLong("id");
        system = extras.getString("system");
        username = extras.getString("username");
        password = extras.getString("password");
        note = extras.getString("note");
        url = extras.getString("url");

        this.setContentView(R.layout.show);

        TextView usernameView = findViewById(R.id.username);
        TextView usernameHeaderView = findViewById(R.id.username_title);

        // Do not show the username entry if it does not exist
        if (username != null && username.length() > 0) {
            usernameView.setText(username);
        } else {
            usernameView.setVisibility(View.GONE);
            usernameHeaderView.setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.password)).setText(password);

        if (note != null && note.length() > 0) {
            findViewById(R.id.note_title).setVisibility(View.VISIBLE);
            TextView text = findViewById(R.id.note);
            text.setText(note);
            text.setVisibility(View.VISIBLE);
        }
        
        if (url != null && url.length() > 0) {
            findViewById(R.id.url_title).setVisibility(View.VISIBLE);
            TextView text = findViewById(R.id.url);
            text.setText(url);
            text.setVisibility(View.VISIBLE);
        }
        
        setupClipboardAction();
        
        getSupportActionBar().setTitle(system);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItemCompat.setShowAsAction(menu.add("Edit")
            .setIcon(R.drawable.ic_menu_edit),MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        MenuItemCompat.setShowAsAction(menu.add("Delete")
            .setIcon(R.drawable.ic_menu_delete),MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // User press home back button
            finish();
            overridePendingTransition(0, 0);
            return true;
        }
        
        String option = item.getTitle().toString();
        
        if (option.equals("Edit")) {
            editPassword();
            return true;
        } else if (option.equals("Delete")) {
            confirmDelete();
            return true;
        }
        
        return false;
    }

    /**
     * Set up a clickable password. When clicked the user will be prompted to
     * choose if the password should be copied to the clipboard.
     */
    private void setupClipboardAction() {
        final TextView passwordView = findViewById(R.id.password);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Copy to clipboard");
        alertDialog.setMessage("Copy the password to clipboard?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordView.getText().toString();

                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("passdroid", password);
                clipboard.setPrimaryClip(clip);

                Session.getInstance().setClipboardPassword(password);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        passwordView.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.show();
                    }
                }
        );
    }

    private void editPassword() {
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra("id", passwordId);
        i.putExtra("system", system);
        i.putExtra("username", username);
        i.putExtra("password", password);
        i.putExtra("note", note);
        i.putExtra("url", url);

        startActivity(i);
        finish();
    }

    private void confirmDelete() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Confirm delete");
        alertDialog.setMessage("Are your sure you want to delete "+system+"?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int which) {
                deletePassword();
                Session.getInstance().setNeedReload(true);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    private void deletePassword() {
        PasswordData passwordData = new PasswordData(this);
        SQLiteDatabase db = passwordData.getWritableDatabase();

        try {
            db.delete("data", "id=" + passwordId, null);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        db.close();

        finish();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
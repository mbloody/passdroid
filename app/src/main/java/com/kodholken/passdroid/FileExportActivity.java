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

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class FileExportActivity extends AppCompatTimeoutActivity {
    private Button cancelButton;
    private Button exportButton;
    private CheckBox encryptFileCheckbox;
    private EditText exportFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.file_export);

        exportFilename = (EditText) this.findViewById(R.id.filename);
        String filename = "/passdroid_db.xml";
        if (Environment.getExternalStorageDirectory() != null) {
            filename = Environment.getExternalStorageDirectory() + "/passdroid_db.xml";
        }
        exportFilename.setText(filename);

        cancelButton = (Button) this.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        exportButton = (Button) this.findViewById(R.id.export_button);
        exportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doExport();
            }
        });
        
        encryptFileCheckbox = (CheckBox) findViewById(R.id.encrypt_export);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode != 1) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            Log.i("passdroid", "onRequestPermissionsResult Permission: " + permissions[i] + "Result: " + grantResults[i]);
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("passdroid", "WRITE_EXTERNAL_STORAGE permission has now been granted.");
            }
        }
    }

    private void doExport() {
        FileExporter exporter = new FileExporter(exportFilename.getText().toString(),
                                                 Utils.getVersion(this));
        try {
            if (encryptFileCheckbox.isChecked()) {
                exporter.exportEncrypted(
                                Session.getInstance().getKey(),
                                PasswordModel.getInstance(this).getPasswords());
                showDialog("Success", getString(R.string.export_successful, exportFilename.getText().toString()));
            } else {
                exporter.exportCleartext(
                        PasswordModel.getInstance(this).getPasswords());
                showDialog("Success", getString(R.string.export_cleartext_successful, exportFilename.getText().toString()));
            }
        } catch (ExportException e) {
            Utils.alertDialog(this, "Failure", "Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog;

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            } }); 
        alertDialog.show();
    }
}

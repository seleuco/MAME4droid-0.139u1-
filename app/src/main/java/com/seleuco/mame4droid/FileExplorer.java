/*
 * This file is part of MAME4droid.
 *
 * Copyright (C) 2015 David Valdeita (Seleuco)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Linking MAME4droid statically or dynamically with other modules is
 * making a combined work based on MAME4droid. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * In addition, as a special exception, the copyright holders of MAME4droid
 * give you permission to combine MAME4droid with free software programs
 * or libraries that are released under the GNU LGPL and with code included
 * in the standard release of MAME under the MAME License (or modified
 * versions of such code, with unchanged license). You may copy and
 * distribute such a system following the terms of the GNU GPL for MAME4droid
 * and the licenses of the other code concerned, provided that you include
 * the source code of that other code when and as the GNU GPL requires
 * distribution of source code.
 *
 * Note that people who make modified versions of MAME4idroid are not
 * obligated to grant this special exception for their modified versions; it
 * is their choice whether to do so. The GNU General Public License
 * gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version
 * which carries forward this exception.
 *
 * MAME4droid is dual-licensed: Alternatively, you can license MAME4droid
 * under a MAME license, as set out in http://mamedev.org/
 */

package com.seleuco.mame4droid;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.seleuco.mame4droid.helpers.DialogHelper;
import com.seleuco.mame4droid.R;

public class FileExplorer {

    // Stores names of traversed directories
    ArrayList<String> traversed = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = false;

    public File getPath() {
        return path;
    }

    private static final String TAG = "FE_PATH";

    private Item[] fileList;
    private File path = new File(
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "");
    private String chosenFile;
    private String downFile;
    private Boolean hasZero = false;

    ListAdapter adapter;

    protected MAME4droid mm = null;

    public FileExplorer(MAME4droid mm) {
        this.mm = mm;
    }

    private void loadFileList() {

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (/* sel.isFile() || */sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            //String[] fList = path.list();

            if (fList == null)
                fList = new String[0];

            if (downFile != null) {
                boolean f = false;
                for (int i = 0; i < fList.length && !f; i++)
                    f = downFile.equals(fList[i]);
                if (!f) {
                    String[] fListNew = new String[fList.length + 1];
                    for (int i = 0; i < fList.length; i++)
                        fListNew[i] = fList[i];
                    fListNew[fListNew.length - 1] = downFile;
                    fList = fListNew;
                }
            }

            if (hasZero && path != null && path.getAbsolutePath().equals("/storage/emulated")) {
                boolean f = false;
                for (int i = 0; i < fList.length && !f; i++)
                    f = "0".equals(fList[i]);
                if (!f) {
                    String[] fListNew = new String[fList.length + 1];
                    for (int i = 0; i < fList.length; i++)
                        fListNew[i] = fList[i];
                    fListNew[fListNew.length - 1] = "0";
                    fList = fListNew;
                }
            }

            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }

        if(fileList!=null) { //El path existe
            adapter = new ArrayAdapter<Item>(mm,
                    /*android.R.layout.select_dialog_item*/android.R.layout.simple_list_item_1, android.R.id.text1,
                    fileList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // creates view
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view
                            .findViewById(android.R.id.text1);

                    // put the image on the text view
                    textView.setCompoundDrawablesWithIntrinsicBounds(
                            fileList[position].icon, 0, 0, 0);

                    // add margin between image and text (support various screen
                    // densities)
                    int dp5 = (int) (5 * mm.getResources().getDisplayMetrics().density + 0.5f);
                    textView.setCompoundDrawablePadding(dp5);

                    return view;
                }
            };
        }
    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    public Dialog create() {

        //firstLvl = false;
        if (!firstLvl && traversed.isEmpty()) {
            String[] files = path.getPath().split("/");
            for (int i = 0; i < files.length; i++) {
                if (files[i].trim().length() != 0)
                    traversed.add(files[i]);
            }
        }

        if (path.getAbsolutePath().equals("/storage/emulated/0"))
            hasZero = true;

        loadFileList();

        Dialog dialog = null;
        AlertDialog.Builder builder = new Builder(mm);

        if (fileList == null) {
            Log.e(TAG, "No files loaded");
            dialog = builder.create();
            return dialog;
        }

        builder.setTitle("Selected: " + path.getPath());
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chosenFile = fileList[which].file;
                File sel = new File(path + "/" + chosenFile);
                if (sel.isDirectory()) {
                    firstLvl = false;

                    downFile = null;

                    // Adds chosen directory to list
                    traversed.add(chosenFile);
                    fileList = null;
                    path = new File(sel + "");

                    mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                    mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                    Log.d(TAG, path.getAbsolutePath());

                }
                // Checks if 'up' was clicked
                else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                    // present directory removed from list
                    String s = traversed.get(traversed.size() - 1);

                    downFile = s;

                    // path modified to exclude present directory
                    File pathTmp = new File(path.toString().substring(0,
                            path.toString().lastIndexOf(s)));

                    if (pathTmp.isDirectory()) {
                        path = pathTmp;
                        fileList = null;

                        traversed.remove(traversed.size() - 1);

                        // if there are no more directories in the list, then
                        // its the first level
                        if (traversed.isEmpty()) {
                            firstLvl = true;
                        }

                        mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                        mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                        Log.d(TAG, path.getAbsolutePath());
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(mm);
                        alert.setCancelable(false);

                        alert.setTitle("The directory is not readable!");

                        alert.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                                mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                            }
                        });

                        alert.show();
                    }


                }
                // File picked
                else {

                }
            }
        });

        builder.setNeutralButton("Manual",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        final EditText edittext = new EditText(mm);
                        AlertDialog.Builder alert = new AlertDialog.Builder(mm);

                        alert.setCancelable(false);

                        alert.setTitle("Custom ROM path");
                        alert.setMessage("Set your custom ROM path directly if it is not accesible to file browser. Ensure is readable and writable!");

                        edittext.setText(path.getAbsolutePath());
                        edittext.setSelection(edittext.getText().length());

                        alert.setView(edittext);

                        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DialogHelper.savedDialog = DialogHelper.DIALOG_NONE;
                                mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                                mm.getPrefsHelper().setROMsDIR(edittext.getText().toString());
                                mm.getMainHelper().ensureInstallationDIR(mm.getMainHelper().getInstallationDIR());
                                mm.runMAME4droid();
                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                                mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                            }
                        });

                        alert.show();
                    }
                });

        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                        DialogHelper.savedDialog = DialogHelper.DIALOG_NONE;
                        mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                        mm.getPrefsHelper().setROMsDIR(path.getAbsolutePath());
                        mm.getMainHelper().ensureInstallationDIR(mm.getMainHelper().getInstallationDIR());
                        mm.runMAME4droid();
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogHelper.savedDialog = DialogHelper.DIALOG_NONE;
                        mm.removeDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
                        if(mm.getPrefsHelper().getROMsDIR()==null)
                            mm.getPrefsHelper().setROMsDIR("");
                        mm.getMainHelper().ensureInstallationDIR(mm.getMainHelper().getInstallationDIR());
                        mm.runMAME4droid();
                    }
                });

        builder.setCancelable(false);
        dialog = builder.show();
        return dialog;
    }

}
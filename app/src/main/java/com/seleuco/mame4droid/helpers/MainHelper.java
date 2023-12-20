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

package com.seleuco.mame4droid.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.seleuco.mame4droid.R;
import com.seleuco.mame4droid.WebHelpActivity;
import com.seleuco.mame4droid.input.ControlCustomizer;
import com.seleuco.mame4droid.input.InputHandler;
import com.seleuco.mame4droid.prefs.GameFilterPrefs;
import com.seleuco.mame4droid.prefs.UserPreferences;
import com.seleuco.mame4droid.views.IEmuView;
import com.seleuco.mame4droid.views.InputView;

public class MainHelper {

    final static public int SUBACTIVITY_USER_PREFS = 1;
    final static public int SUBACTIVITY_HELP = 2;
    final static public int BUFFER_SIZE = 1024 * 48;

    // final static public String MAGIC_FILE = "dont-delete-00005.bin";

    final public static int DEVICE_GENEREIC = 1;
    final public static int DEVICE_OUYA = 2;
    final public static int DEVICE_SHIELD = 3;
    final public static int DEVICE_JXDS7800 = 4;
    final public static int DEVICE_AGAMEPAD2 = 5;
    final public static int DEVICE_ANDROIDTV = 5;

    final public static int INSTALLATION_DIR_UNDEFINED = 1;
    final public static int INSTALLATION_DIR_FILES_DIR = 2;
    final public static int INSTALLATION_DIR_LEGACY = 3;
    final public static int INSTALLATION_DIR_MEDIA_FOLDER = 4;

    protected int installationDirType = INSTALLATION_DIR_UNDEFINED;

    protected boolean createdInstallationDir = false;

    protected int deviceDetected = DEVICE_GENEREIC;

    protected int oldInMAME = 0;

    final public static int REQUEST_CODE_OPEN_DIRECTORY = 33;

    public int getDeviceDetected() {
        return deviceDetected;
    }

    protected MAME4droid mm = null;

    public MainHelper(MAME4droid value) {
        mm = value;
    }

    public void setInstallationDirType(int installationDirType) {
        this.installationDirType = installationDirType;
    }

    public int getInstallationDirType() {
        return installationDirType;
    }

    public boolean isCreatedInstallationDir() {
        return createdInstallationDir;
    }

    public String getLibDir() {
        String cache_dir, lib_dir;
        try {
            // cache_dir = mm.getCacheDir().getCanonicalPath();
            // lib_dir = cache_dir.replace("cache", "lib");
            lib_dir = mm.getApplicationInfo().nativeLibraryDir;
        } catch (Exception e) {
            e.printStackTrace();
            lib_dir = "/data/data/com.seleuco.mame4droid/lib";
        }
        return lib_dir;
    }

    public String getInstallationDIR() {
        String res_dir = null;

        if (mm.getPrefsHelper().getInstallationDIR() != null)
            return mm.getPrefsHelper().getInstallationDIR();

        // android.os.Debug.waitForDebugger();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (getInstallationDirType() == INSTALLATION_DIR_FILES_DIR)
                res_dir = mm.getExternalFilesDir(null).getAbsolutePath() + "/";
            else if (getInstallationDirType() == INSTALLATION_DIR_LEGACY)
                res_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MAME4droid/";
            else if (getInstallationDirType() == INSTALLATION_DIR_MEDIA_FOLDER)
            {
                File[] dirs = mm.getExternalMediaDirs();
                for(File d: dirs)
                {
                    if(d != null) {
                        res_dir = d.getAbsolutePath() + "/";
                        break;
                    }
                }
            }
        }
        if (res_dir == null)
            res_dir = mm.getFilesDir().getAbsolutePath() + "/";

        // res_dir =
        // mm.getExternalFilesDir(null).getAbsolutePath()+"/MAME4droid/";
        // File[] f = mm.getExternalFilesDirs(null);
        // res_dir = f[f.length-1].getAbsolutePath();

        mm.getPrefsHelper().setInstallationDIR(res_dir);

        return res_dir;
    }

    public boolean ensureInstallationDIR(String dir) {

        if (!dir.endsWith("/"))
            dir += "/";

        File res_dir = new File(dir);

        boolean created = false;

        if (res_dir.exists() == false) {
            if (!res_dir.mkdirs()) {
                mm.getDialogHelper().setErrorMsg(
                        "Can't find/create: '" + dir + "' Is it writeable?.\nReverting...");
                mm.showDialog(DialogHelper.DIALOG_ERROR_WRITING);
                return false;
            } else {
                created = true;
            }
        }

        String str_sav_dir = dir + "saves/";
        File sav_dir = new File(str_sav_dir);
        if (sav_dir.exists() == false) {
            if (!sav_dir.mkdirs()) {
                mm.getDialogHelper().setErrorMsg(
                        "Can't find/create: '" + str_sav_dir + "' Is it writeable?.\nReverting...");
                mm.showDialog(DialogHelper.DIALOG_ERROR_WRITING);
                return false;
            } else {
                created = true;
            }
        }

        createdInstallationDir = created;

        mm.getPrefsHelper().setOldInstallationDIR(dir);

        return true;
    }

    protected boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists())
            throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public void removeFiles() {
        try {
            if (mm.getPrefsHelper().isDefaultData()) {
                String dir = mm.getMainHelper().getInstallationDIR();

                File f1 = new File(dir + File.separator + "cfg/");
                File f2 = new File(dir + File.separator + "nvram/");

                deleteRecursive(f1);
                deleteRecursive(f2);

                Toast.makeText(mm, "Deleted MAME cfg and NVRAM files...",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(mm, "Failed deleting:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void copyFiles() {

        try {

            String roms_dir = mm.getMainHelper().getInstallationDIR();

            File fm = new File(roms_dir + File.separator + "saves/"
                    + "dont-delete-" + getVersion() + ".bin");
            if (fm.exists())
                return;

            fm.mkdirs();
            fm.createNewFile();

            // Create a ZipInputStream to read the zip file
            BufferedOutputStream dest = null;
            InputStream fis = mm.getResources().openRawResource(R.raw.files);
            ZipInputStream zis = new ZipInputStream(

                    new BufferedInputStream(fis));
            // Loop over all of the entries in the zip file
/*
InputStream is = new InputStream(untrustedFileName);
ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
while((ZipEntry ze = zis.getNextEntry()) != null) {
  File f = new File(DIR, ze.getName());
  String canonicalPath = f.getCanonicalPath();
  if (!canonicalPath.startsWith(DIR)) {
    // SecurityException
  }
  // Finish unzipping…
}
 */
            String zip_dir = new File(roms_dir).getCanonicalPath();
            int count;
            byte data[] = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File f = new File(zip_dir, entry.getName());
                    String canonicalPath = f.getCanonicalPath();
                    if (!canonicalPath.startsWith(zip_dir)) {
                        throw new SecurityException("Error zip!!!!");
                    }
                    String destination = zip_dir;
                    String destFN = destination + File.separator + entry.getName();
                    // Write the file to the file system
                    FileOutputStream fos = new FileOutputStream(destFN);
                    dest = new BufferedOutputStream(fos, BUFFER_SIZE);
                    while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } else {
                    File f = new File(zip_dir+ File.separator
                            + entry.getName());
                    f.mkdirs();
                }

            }
            zis.close();

            String dir = this.getInstallationDIR();
            if (!dir.endsWith("/")) dir += "/";
            String rompath = mm.getPrefsHelper().getROMsDIR() != null && mm.getPrefsHelper().getROMsDIR() != "" ? mm
                    .getPrefsHelper().getROMsDIR() : dir + "roms";
            String msg =
                    "Created or updated: '"
                            + dir
                            + "' to store save states, cfg files and MAME assets.\n\nNote, copy or move your zipped ROMs under '"
                            + rompath
                            + "' directory!\n\nMAME4droid 0.139 uses only 0.139 MAME romset.";
            if (Build.VERSION.SDK_INT >= 29 && mm.getPrefsHelper().getSAF_Uri()!=null)
                msg += "\n\nTIP: You can enable a setting to store save states under roms folder, so they will not be deleted when uninstalling MAME4droid. Look at MAME4droid option in settings.";
            mm.getDialogHelper()
                    .setInfoMsg(msg);
            mm.showDialog(DialogHelper.DIALOG_INFO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getscrOrientation() {
        Display getOrient = mm.getWindowManager().getDefaultDisplay();
        // int orientation = getOrient.getOrientation();

        int orientation = mm.getResources().getConfiguration().orientation;

        // Sometimes you may get undefined orientation Value is 0
        // simple logic solves the problem compare the screen
        // X,Y Co-ordinates and determine the Orientation in such cases
        if (orientation == Configuration.ORIENTATION_UNDEFINED) {

            Configuration config = mm.getResources().getConfiguration();
            orientation = config.orientation;

            if (orientation == Configuration.ORIENTATION_UNDEFINED) {
                // if emu_height and widht of screen are equal then
                // it is square orientation
                if (getOrient.getWidth() == getOrient.getHeight()) {
                    orientation = Configuration.ORIENTATION_SQUARE;
                } else { // if widht is less than emu_height than it is portrait
                    if (getOrient.getWidth() < getOrient.getHeight()) {
                        orientation = Configuration.ORIENTATION_PORTRAIT;
                    } else { // if it is not any of the above it will defineitly
                        // be landscape
                        orientation = Configuration.ORIENTATION_LANDSCAPE;
                    }
                }
            }
        }
        return orientation; // return values 1 is portrait and 2 is Landscape
        // Mode
    }

    public void reload() {

        if (true)
            return;
        System.out.println("RELOAD!!!!!");

        Intent intent = mm.getIntent();
        System.out.println("RELOAD intent:" + intent.getAction());

        mm.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mm.finish();

        mm.overridePendingTransition(0, 0);
        mm.startActivity(intent);
        mm.overridePendingTransition(0, 0);
    }

    public void updateOverlayFilter() {

        String value = PrefsHelper.PREF_OVERLAY_NONE;

        if (getscrOrientation() == Configuration.ORIENTATION_PORTRAIT)
            value = mm.getPrefsHelper().getPortraitOverlayFilterValue();
        else
            value = mm.getPrefsHelper().getLandscapeOverlayFilterValue();

        if (!Emulator.getOverlayFilterValue().equals(value)) {
            Emulator.setOverlayFilterValue(value);
            Emulator.setFilterBitmap(null);
            if (!value.equals(PrefsHelper.PREF_OVERLAY_NONE)) {
                String fileName = mm.getMainHelper().getInstallationDIR()
                        + File.separator + "overlays" + File.separator + value;

                Bitmap bmp = BitmapFactory.decodeFile(fileName);
                Emulator.setFilterBitmap(bmp);
            }
        } else {
            Emulator.setOverlayFilterValue(value);
        }
    }

    public void updateVideoRender() {

        if (Emulator.getVideoRenderMode() != mm.getPrefsHelper()
                .getVideoRenderMode()) {
            Emulator.setVideoRenderMode(mm.getPrefsHelper()
                    .getVideoRenderMode());
        } else {
            Emulator.setVideoRenderMode(mm.getPrefsHelper()
                    .getVideoRenderMode());
        }
    }

    public void setBorder() {

        if (true)
            return;

        int size = mm.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;

        if ((size == Configuration.SCREENLAYOUT_SIZE_LARGE || size == Configuration.SCREENLAYOUT_SIZE_XLARGE)
                && mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            LayoutParams lp = (LayoutParams) mm.getEmuView().getLayoutParams();
            View v = mm.findViewById(R.id.EmulatorFrame);
            if (mm.getPrefsHelper().isPortraitTouchController()) {
                v.setBackgroundDrawable(mm.getResources().getDrawable(
                        R.drawable.border_view));
                lp.setMargins(15, 15, 15, 15);
            } else {
                v.setBackgroundDrawable(null);
                v.setBackgroundColor(mm.getResources().getColor(
                        R.color.emu_back_color));
                lp.setMargins(0, 0, 0, 0);
            }
        }
    }

    public void updateEmuValues() {

        PrefsHelper prefsHelper = mm.getPrefsHelper();

        Emulator.setValue(Emulator.FPS_SHOWED_KEY,
                prefsHelper.isFPSShowed() ? 1 : 0);
        Emulator.setValue(Emulator.INFOWARN_KEY,
                prefsHelper.isShowInfoWarnings() ? 1 : 0);

        Emulator.setValue(Emulator.IDLE_WAIT, prefsHelper.isIdleWait() ? 1 : 0);
        Emulator.setValue(Emulator.THROTTLE, prefsHelper.isThrottle() ? 1 : 0);
        Emulator.setValue(Emulator.AUTOSAVE, prefsHelper.isAutosave() ? 1 : 0);
        Emulator.setValue(Emulator.CHEAT, prefsHelper.isCheat() ? 1 : 0);

        Emulator.setValue(Emulator.FRAME_SKIP_VALUE,
                prefsHelper.getFrameSkipValue());

        Emulator.setValue(Emulator.EMU_AUTO_RESOLUTION,
                prefsHelper.isAutoSwitchRes() ? 1 : 0);
        Emulator.setValue(Emulator.EMU_RESOLUTION,
                prefsHelper.getEmulatedResolution());
        Emulator.setValue(Emulator.FORCE_PXASPECT,
                prefsHelper.getForcedPixelAspect());

        Emulator.setValue(Emulator.DOUBLE_BUFFER, mm.getPrefsHelper()
                .isDoubleBuffer() ? 1 : 0);
        Emulator.setValue(Emulator.PXASP1, mm.getPrefsHelper()
                .isPlayerXasPlayer1() ? 1 : 0);
        Emulator.setValue(Emulator.SAVELOAD_COMBO, mm.getPrefsHelper()
                .isSaveLoadCombo() ? 1 : 0);

        Emulator.setValue(Emulator.AUTOFIRE, mm.getPrefsHelper()
                .getAutofireValue());

        Emulator.setValue(Emulator.HISCORE, mm.getPrefsHelper().isHiscore() ? 1
                : 0);

        Emulator.setValue(Emulator.VBEAN2X, mm.getPrefsHelper()
                .isVectorBeam2x() ? 1 : 0);
        Emulator.setValue(Emulator.VANTIALIAS, mm.getPrefsHelper()
                .isVectorAntialias() ? 1 : 0);
        Emulator.setValue(Emulator.VFLICKER, mm.getPrefsHelper()
                .isVectorFlicker() ? 1 : 0);

        Emulator.setValue(Emulator.NETPLAY_DELAY, mm.getPrefsHelper()
                .getNetplayDelay());

        Emulator.setValue(
                Emulator.RENDER_RGB,
                mm.getPrefsHelper().isRenderRGB()
                        && mm.getPrefsHelper().getVideoRenderMode() != PrefsHelper.PREF_RENDER_SW ? 1
                        : 0);

        Emulator.setValue(
                Emulator.SAVESATES_IN_ROM_PATH,
                mm.getPrefsHelper().areSavesInRomPath() ? 1 : 0);

        Emulator.setValue(Emulator.IMAGE_EFFECT, mm.getPrefsHelper()
                .getImageEffectValue());
        Emulator.setValue(Emulator.MOUSE,
                mm.getPrefsHelper().isMouseEnabled() ? 1 : 0);

        Emulator.setValue(Emulator.REFRESH, mm.getPrefsHelper().getRefresh());

        GameFilterPrefs gfp = mm.getPrefsHelper().getGameFilterPrefs();
        boolean dirty = gfp.readValues();
        gfp.sendValues();
        if (dirty) {
            if (!Emulator.isInMAME())
                Emulator.setValue(Emulator.RESET_FILTER, 1);
            Emulator.setValue(Emulator.LAST_GAME_SELECTED, 0);
        }

        Emulator.setValue(Emulator.EMU_SPEED, mm.getPrefsHelper()
                .getEmulatedSpeed());
        Emulator.setValue(Emulator.VSYNC, mm.getPrefsHelper().getVSync());

        Emulator.setValue(Emulator.SOUND_ENGINE, mm.getPrefsHelper()
                .getSoundEngine() > 2 ? 2 : 1);

        AudioManager am = (AudioManager) mm
                .getSystemService(Context.AUDIO_SERVICE);
        int sfr = 512;//10ms a 48000khz

        if (mm.getPrefsHelper().getSoundEngine() == PrefsHelper.PREF_SNDENG_OPENSL_LOW) {
            try {
                sfr = Integer
                        .valueOf(
                                am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER))
                        .intValue();
                System.out.println("PROPERTY_OUTPUT_FRAMES_PER_BUFFER:" + sfr);
            } catch (Throwable e) {
            }
        }

        Emulator.setValue(Emulator.SOUND_DEVICE_FRAMES, sfr);

        int sr = 44100;

        try {
            sr = Integer.valueOf(
                    am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE))
                    .intValue();
            System.out.println("PROPERTY_OUTPUT_SAMPLE_RATE:" + sr);
        } catch (Throwable e) {
        }

        Context context = mm.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("sound_rate", false)) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("sound_rate", true);
            if (sr == 48000)//sino defecto 44100
                edit.putString(PrefsHelper.PREF_GLOBAL_SOUND, sr + "");
            edit.commit();
        }

        if (mm.getPrefsHelper().getSoundEngine() == PrefsHelper.PREF_SNDENG_OPENSL)
            sr = mm.getPrefsHelper().getSoundValue();
		/*
		else is PrefsHelper.PREF_SNDENG_OPENSL_LOW fixed at PROPERTY_OUTPUT_SAMPLE_RATE
		 */

        Emulator.setValue(Emulator.SOUND_VALUE, prefsHelper.getSoundValue());
        Emulator.setValue(Emulator.SOUND_DEVICE_SR, sr);

        Emulator.setValueStr(Emulator.BIOS, mm.getPrefsHelper().getCustomBIOS());
    }

    public void updateMAME4droid() {

        if (Emulator.isRestartNeeded()) {
            mm.showDialog(DialogHelper.DIALOG_EMU_RESTART);
            return;
        }

        // updateVideoRender();
        Emulator.setVideoRenderMode(mm.getPrefsHelper().getVideoRenderMode());

        updateOverlayFilter();

        if (Emulator.isPortraitFull() != mm.getPrefsHelper()
                .isPortraitFullscreen())
            mm.inflateViews();

        View emuView = mm.getEmuView();

        InputView inputView = mm.getInputView();
        InputHandler inputHandler = mm.getInputHandler();
        PrefsHelper prefsHelper = mm.getPrefsHelper();

        String definedKeys = prefsHelper.getDefinedKeys();
        final String[] keys = definedKeys.split(":");
        for (int i = 0; i < keys.length; i++)
            InputHandler.keyMapping[i] = Integer.valueOf(keys[i]).intValue();

        Emulator.setDebug(prefsHelper.isDebugEnabled());
        Emulator.setThreadedSound(!prefsHelper.isSoundSync());

        updateEmuValues();

        setBorder();

        if (prefsHelper.isTiltSensor())
            inputHandler.getTiltSensor().enable();
        else
            inputHandler.getTiltSensor().disable();

        inputHandler.setTrackballSensitivity(prefsHelper
                .getTrackballSensitivity());
        inputHandler.setTrackballEnabled(!prefsHelper.isTrackballNoMove());

        int state = mm.getInputHandler().getInputHandlerState();

        if (this.getscrOrientation() == Configuration.ORIENTATION_PORTRAIT) {

            ((IEmuView) emuView).setScaleType(prefsHelper
                    .getPortraitScaleMode());

            Emulator.setFrameFiltering(prefsHelper.isPortraitBitmapFiltering());

            if (state == InputHandler.STATE_SHOWING_CONTROLLER
                    && !prefsHelper.isPortraitTouchController())
                // {reload();return;}
                inputHandler.changeState();

            if (state == InputHandler.STATE_SHOWING_NONE
                    && prefsHelper.isPortraitTouchController())
                // {reload();return;}
                inputHandler.changeState();

            state = mm.getInputHandler().getInputHandlerState();

            if (state == InputHandler.STATE_SHOWING_NONE) {
                inputView.setVisibility(View.GONE);
            } else {
                inputView.setVisibility(View.VISIBLE);
            }

            if (state == InputHandler.STATE_SHOWING_CONTROLLER) {
                if (Emulator.isPortraitFull()) {
                    inputView.bringToFront();
                    inputHandler
                            .readControllerValues(R.raw.controller_portrait_full);
                } else {
                    inputView.setImageDrawable(mm.getResources().getDrawable(
                            R.drawable.back_portrait));
                    inputHandler
                            .readControllerValues(R.raw.controller_portrait);
                }
            }

            if (ControlCustomizer.isEnabled() && !Emulator.isPortraitFull()) {
                ControlCustomizer.setEnabled(false);
                mm.getDialogHelper()
                        .setInfoMsg(
                                "Control layout customization is only allowed in fullscreen mode");
                mm.showDialog(DialogHelper.DIALOG_INFO);
            }
        } else {
            ((IEmuView) emuView).setScaleType(mm.getPrefsHelper()
                    .getLandscapeScaleMode());

            Emulator.setFrameFiltering(mm.getPrefsHelper()
                    .isLandscapeBitmapFiltering());

            if (state == InputHandler.STATE_SHOWING_CONTROLLER
                    && !prefsHelper.isLandscapeTouchController())
                // {reload();return;}
                inputHandler.changeState();

            if (state == InputHandler.STATE_SHOWING_NONE
                    && prefsHelper.isLandscapeTouchController())
                // {reload();return;}
                inputHandler.changeState();

            state = mm.getInputHandler().getInputHandlerState();

            inputView.bringToFront();

            if (state == InputHandler.STATE_SHOWING_NONE) {
                inputView.setVisibility(View.GONE);
            } else {
                inputView.setVisibility(View.VISIBLE);
            }

            if (state == InputHandler.STATE_SHOWING_CONTROLLER) {
                inputView.setImageDrawable(null);

                Display dp = mm.getWindowManager().getDefaultDisplay();

                float w = dp.getWidth();
                float h = dp.getHeight();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Point pt = new Point();
                    dp.getRealSize(pt);
                    w = pt.x;
                    h = pt.y;
                } else {
                    try {
                        Method mGetRawW;
                        mGetRawW = Display.class.getMethod("getRawWidth");
                        Method mGetRawH = Display.class
                                .getMethod("getRawHeight");
                        w = (Integer) mGetRawW.invoke(dp);
                        h = (Integer) mGetRawH.invoke(dp);
                    } catch (Exception e) {
                    }
                }

                if (h == 0)
                    h = 1;
/*
https://stackoverflow.com/questions/7199492/what-are-the-aspect-ratios-for-all-android-phone-and-tablet-devices

asus rog       --> 2160x1080 18:9
oneplus 6      --> 2280x1080 19:9   6.28
oneplus 7      --> 2340x1080 19.5:9 6.21
oneplus 8 	   --> 2400x1080 20.9   6.55
oneplus 8 pro  --> 3168x1440 19.8:9 6.78
galaxy sde	   --> 2560x1600 16:10


19.8:9 -> 2.2
20/9   -> 2,22222
19.5:9 -> 2,16666
19/9  -> 2,11111
18/9  -> 2
16/9   -> 1,7
5/3   -> 1,6666
4/3    -> 1,3
 */
                // System.out.println("--->>> "+w+" "+h+ " "+w/h+ " "+ (float)(16.0/9.0));
                float ar = w / h;
                if (ar >= (float) (18.0 / 9.0)) {
                    System.out.println("--->>> ULTRA WIDE");
                    inputHandler.readControllerValues(R.raw.controller_landscape_19_9);
                } else if (ar >= (float) (16.0 / 9.0) && ar < (float) (18.0 / 9.0)) {
                    System.out.println("--->>> WIDE");
                    inputHandler.readControllerValues(R.raw.controller_landscape_16_9);
                } else { //5 : 3
                    System.out.println("--->>> NORMAL");
                    inputHandler.readControllerValues(R.raw.controller_landscape);
                }
            }
        }

        if (Emulator.getValue(Emulator.IN_MAME) == 1
                && (Emulator.getValue(Emulator.IN_MENU) == 0 || oldInMAME == 0)
                && ((mm.getPrefsHelper().isLightgun() && mm.getInputHandler()
                .getInputHandlerState() != InputHandler.STATE_SHOWING_NONE) || mm
                .getPrefsHelper().isTiltSensor())) {
            CharSequence text = mm.getPrefsHelper().isTiltSensor() ? "Tilt sensor is enabled!"
                    : "Touch lightgun is enabled!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(mm, text, duration);
            toast.show();
        }

        oldInMAME = Emulator.getValue(Emulator.IN_MAME);

        if (state != InputHandler.STATE_SHOWING_CONTROLLER
                && ControlCustomizer.isEnabled()) {
            ControlCustomizer.setEnabled(false);
            mm.getDialogHelper()
                    .setInfoMsg(
                            "Control layout customization is only allowed when touch controller is visible");
            mm.showDialog(DialogHelper.DIALOG_INFO);
        }

        if (ControlCustomizer.isEnabled()) {
            // mm.getEmuView().setVisibility(View.INVISIBLE);
            // mm.getInputView().requestFocus();
        }

        int op = inputHandler.getOpacity();
        if (op != -1 && (state == InputHandler.STATE_SHOWING_CONTROLLER))
            inputView.setAlpha(op);

        inputView.requestLayout();

        emuView.requestLayout();

        inputView.invalidate();
        emuView.invalidate();
    }

    public void showSettings() {
        if (!Emulator.isEmulating()) return;
        Intent i = new Intent(mm, UserPreferences.class);
        mm.startActivityForResult(i, MainHelper.SUBACTIVITY_USER_PREFS);
    }

    public void showHelp() {
        // Intent i2 = new Intent(mm, HelpActivity.class);
        // mm.startActivityForResult(i2, MainHelper.SUBACTIVITY_HELP);
        if (mm.getMainHelper().isAndroidTV()) {
            mm.getDialogHelper()
                    .setInfoMsg(
                            "When MAME4droid is first run, it will create a folder structure for you on the internal memory of your Android device. This folder contains all the other folders MAME uses as well as some basic configuration files."
                                    + "Since MAME4droid does not come with game ROM files, you will need to copy them to the selected or 'Android/data/com.seleuco.mame4droid/files/roms' folder (" +
                                    "the one that applies) yourself. These should be properly named, ZIPped MAME v1.39u1 ROMs files with the filenames in all lower case.\n\n Important: You should define or map your Android TV game controller on 'options/settings/input/External controller/define Keys' to avoid this help screen constantly showing if the controller is not auto detected.\n\n"
                                    + "Controls: Buttons A,B,C,D,E,F on the controller map to buttons Button MAME 1 to 6 buttons."
                                    + "Coin button inserts coin/adds credit.START button starts 1P game.START+UP starts 2P game. START+RIGHT starts 3P game. START+DOWN starts 4P game.SELECT+UP inserts 2P credits. SELECT+RIGHT inserts 3P credits. SELECT+DOWN inserts 4P credits."
                                    + "R1 + START loads a save state. L1 + START saves a save state. START + SELECT when gaming accesses the game's MAME menu (dip switches, etc)...");
            mm.showDialog(DialogHelper.DIALOG_INFO);
        } else {

            Intent i = new Intent(mm, WebHelpActivity.class);
            i.putExtra("INSTALLATION_PATH", mm.getMainHelper()
                    .getInstallationDIR());
            try {
                mm.startActivityForResult(i, MainHelper.SUBACTIVITY_HELP);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
			/*
			mm.getDialogHelper()
			.setInfoMsg(
					"When MAME4droid is first run, it will create a folder structure for you on the internal memory of your Android device. This folder contains all the other folders MAME uses as well as some basic configuration files."
							+ "Since MAME4droid does not come with game ROM files, you will need to copy them to the '/sdcard/MAME4droid/roms' or 'Android/data/com.seleuco.mame4droid/files/roms' folder (" +
							"the one that applies) yourself. These should be properly named, ZIPped MAME v1.39u1 ROMs files with the filenames in all lower case.\n\n"
							+ "Controls: Buttons A,B,C,D,E,F on the controller map to buttons Button MAME 1 to 6 buttons."
							+ "Coin button inserts coin/adds credit.START button starts 1P game.START+UP starts 2P game. START+RIGHT starts 3P game. START+DOWN starts 4P game.SELECT+UP inserts 2P credits. SELECT+RIGHT inserts 3P credits. SELECT+DOWN inserts 4P credits."
							+ "R1 + START loads a save state. L1 + START saves a save state. START + SELECT when gaming accesses the game's MAME menu (dip switches, etc)...");
	         mm.showDialog(DialogHelper.DIALOG_INFO);
	         */
        }
    }

    public void activityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == SUBACTIVITY_USER_PREFS) {
            updateMAME4droid();
        }


        if (android.os.Build.VERSION.SDK_INT >= 21 && requestCode == MainHelper.REQUEST_CODE_OPEN_DIRECTORY &&
                resultCode == Activity.RESULT_OK && intent != null) {
            final ContentResolver resolver = mm.getContentResolver();
            final Uri uri = intent.getData();
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            resolver.takePersistableUriPermission(uri, takeFlags);
            final Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));

            mm.getSAFHelper().setURI(dirUri.toString());

            System.out.println("SAF ROMS dirUri:" + dirUri.getPath());

            String romsPath = mm.getSAFHelper().pathFromDocumentUri(uri);
            if (romsPath == null)
                romsPath = "/Your_Selected_Folder";

            mm.getMainHelper().setInstallationDirType(MainHelper.INSTALLATION_DIR_FILES_DIR);
            mm.getPrefsHelper().setROMsDIR(romsPath);
            mm.getPrefsHelper().setSAF_Uri(uri.toString());
            mm.getPrefsHelper().setIsNotMigrated(false);
            mm.runMAME4droid();
        }
    }

    public ArrayList<Integer> measureWindow(int widthMeasureSpec,
                                            int heightMeasureSpec, int scaleType) {

        int widthSize = 1;
        int heightSize = 1;

        if (!Emulator.isInMAME() && !(scaleType == PrefsHelper.PREF_STRETCH))
            scaleType = PrefsHelper.PREF_SCALE;

        if (scaleType == PrefsHelper.PREF_STRETCH)// FILL ALL
        {
            widthSize = MeasureSpec.getSize(widthMeasureSpec);
            heightSize = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            int emu_w = Emulator.getEmulatedVisWidth();
            int emu_h = Emulator.getEmulatedVisHeight();

            if (scaleType == PrefsHelper.PREF_SCALE_INTEGER) {
                int e = mm.getPrefsHelper().getImageEffectValue() + 1;
                emu_w = emu_w / e;
                emu_h = emu_h / e;

                int ax = (MeasureSpec.getSize(widthMeasureSpec) / emu_w);
                int ay = (MeasureSpec.getSize(heightMeasureSpec) / emu_h);

                int xx = Math.min(ax, ay);

                if (xx == 0)
                    xx = 1;

                emu_w = emu_w * xx;
                emu_h = emu_h * xx;
            } else if (scaleType == PrefsHelper.PREF_SCALE_INTEGER_BEYOND) {
                int e = mm.getPrefsHelper().getImageEffectValue() + 1;
                emu_w = emu_w / e;
                emu_h = emu_h / e;

                int ax = (MeasureSpec.getSize(widthMeasureSpec) / emu_w);
                int ay = (MeasureSpec.getSize(heightMeasureSpec) / emu_h);

                ax++;
                ay++;
                int xx = Math.min(ax, ay);

                if (xx == 0)
                    xx = 1;

                emu_w = emu_w * xx;
                emu_h = emu_h * xx;
            } else if (scaleType == PrefsHelper.PREF_15X) {
                emu_w = (int) (emu_w * 1.5f);
                emu_h = (int) (emu_h * 1.5f);
            } else if (scaleType == PrefsHelper.PREF_20X) {
                emu_w = emu_w * 2;
                emu_h = emu_h * 2;
            } else if (scaleType == PrefsHelper.PREF_25X) {
                emu_w = (int) (emu_w * 2.5f);
                emu_h = (int) (emu_h * 2.5f);
            } else if (scaleType == PrefsHelper.PREF_3X) {
                emu_w = (int) (emu_w * 3.0f);
                emu_h = (int) (emu_h * 3.0f);
            } else if (scaleType == PrefsHelper.PREF_35X) {
                emu_w = (int) (emu_w * 3.5f);
                emu_h = (int) (emu_h * 3.5f);
            } else if (scaleType == PrefsHelper.PREF_4X) {
                emu_w = (int) (emu_w * 4.0f);
                emu_h = (int) (emu_h * 4.0f);
            } else if (scaleType == PrefsHelper.PREF_45X) {
                emu_w = (int) (emu_w * 4.5f);
                emu_h = (int) (emu_h * 4.5f);
            } else if (scaleType == PrefsHelper.PREF_5X) {
                emu_w = (int) (emu_w * 5.0f);
                emu_h = (int) (emu_h * 5.0f);
            }

            if (scaleType == PrefsHelper.PREF_55X) {
                emu_w = (int) (emu_w * 5.5f);
                emu_h = (int) (emu_h * 5.5f);
            }

            if (scaleType == PrefsHelper.PREF_6X) {
                emu_w = (int) (emu_w * 6.0f);
                emu_h = (int) (emu_h * 6.0f);
            }

            int w = emu_w;
            int h = emu_h;

            if (scaleType == PrefsHelper.PREF_SCALE
                    || scaleType == PrefsHelper.PREF_STRETCH
                    || !Emulator.isInMAME()
                    || !mm.getPrefsHelper().isScaleBeyondBoundaries()) {
                widthSize = MeasureSpec.getSize(widthMeasureSpec);
                heightSize = MeasureSpec.getSize(heightMeasureSpec);

                if (mm.getPrefsHelper().isOverscan()) {
                    widthSize *= 0.93;
                    heightSize *= 0.93;
                }

            } else {
                widthSize = emu_w;
                heightSize = emu_h;
            }

            if (heightSize == 0)
                heightSize = 1;
            if (widthSize == 0)
                widthSize = 1;

            float scale = 1.0f;

            if (scaleType == PrefsHelper.PREF_SCALE)
                scale = Math.min((float) widthSize / (float) w,
                        (float) heightSize / (float) h);

            w = (int) (w * scale);
            h = (int) (h * scale);

            float desiredAspect = (float) emu_w / (float) emu_h;

            widthSize = Math.min(w, widthSize);
            heightSize = Math.min(h, heightSize);

            if (heightSize == 0)
                heightSize = 1;
            if (widthSize == 0)
                widthSize = 1;

            float actualAspect = (float) (widthSize / heightSize);

            if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                boolean done = false;

                // Try adjusting emu_width to be proportional to emu_height
                int newWidth = (int) (desiredAspect * heightSize);

                if (newWidth <= widthSize) {
                    widthSize = newWidth;
                    done = true;
                }

                // Try adjusting emu_height to be proportional to emu_width
                if (!done) {
                    int newHeight = (int) (widthSize / desiredAspect);
                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
            }
        }

        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(Integer.valueOf(widthSize));
        l.add(Integer.valueOf(heightSize));
        return l;
    }

    public void detectDevice() {

        boolean ouya = android.os.Build.MODEL.equals("OUYA Console");
        boolean shield = android.os.Build.MODEL.equals("SHIELD");
        boolean S7800 = android.os.Build.MODEL.equals("S7800");
        boolean GP2 = android.os.Build.MODEL.equals("ARCHOS GAMEPAD2");

        if (ouya) {
            Context context = mm.getApplicationContext();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("ouya_2", false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("ouya_2", true);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
                        false);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_BITMAP_FILTERING,
                        true);
                edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE,
                        PrefsHelper.PREF_NAVBAR_VISIBLE + "");
                edit.putString(PrefsHelper.PREF_GLOBAL_RESOLUTION, "11");
                edit.putString(
                        PrefsHelper.PREF_AUTOMAP_OPTIONS,
                        PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED
                                + "");

                // edit.putString("", "");
                edit.commit();
            }
            deviceDetected = DEVICE_OUYA;
        } else if (shield) {
            Context context = mm.getApplicationContext();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("shield_3", false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("shield_3", true);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
                        false);
                edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE,
                        PrefsHelper.PREF_NAVBAR_VISIBLE + "");
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_BITMAP_FILTERING,
                        true);
                edit.putString(PrefsHelper.PREF_GLOBAL_RESOLUTION, "14");
                edit.commit();
            }
            deviceDetected = DEVICE_SHIELD;
        } else if (S7800) {
            Context context = mm.getApplicationContext();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("S7800", false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("S7800", true);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
                        false);
                edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE,
                        PrefsHelper.PREF_NAVBAR_VISIBLE + "");
                edit.commit();
            }
            deviceDetected = DEVICE_JXDS7800;
        }

        if (GP2) {
            Context context = mm.getApplicationContext();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("GAMEPAD2", false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("GAMEPAD2", true);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
                        false);
                // edit.putString(PrefsHelper.PREF_AUTOMAP_OPTIONS,PrefsHelper.PREF_AUTOMAP_L1R1_AS_EXITMENU_L2R2_AS_L1R1+"");
                edit.commit();
            }
            deviceDetected = DEVICE_AGAMEPAD2;
        } else if (isAndroidTV()) {
            Context context = mm.getApplicationContext();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("androidtv", false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("androidtv", true);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
                        false);
                edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_BITMAP_FILTERING,
                        true);

                edit.putString(PrefsHelper.PREF_GLOBAL_RESOLUTION, "14");

                // edit.putString("", "");
                edit.commit();
            }
            deviceDetected = DEVICE_ANDROIDTV;
        }
    }

    public void restartApp() {

        if (Build.VERSION.SDK_INT < 30) {
            Intent oldintent = mm.getIntent();
            // System.out.println("OLD INTENT:"+oldintent.getAction());
            int flags = oldintent.getFlags();

            if(Build.VERSION.SDK_INT >= 33)//para que no saque error el UI
               flags |=  PendingIntent.FLAG_IMMUTABLE;//67108864; //FLAG_IMMUTABLE

            PendingIntent intent = PendingIntent.getActivity(mm.getBaseContext(),
                    0, new Intent(oldintent), flags);
            AlarmManager manager = (AlarmManager) mm
                    .getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, System.currentTimeMillis() + 250, intent);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void checkNewViewIntent(Intent intent) {//TODO
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && Emulator.isEmulating()) {
            Uri uri = intent.getData();
            java.io.File f = new java.io.File(uri.getPath());
            String name = f.getName();
            String romName = Emulator.getValueStr(Emulator.ROM_NAME);
            // System.out.print("Intent view: "+name + " "+ romName);
            if (/*romName != null && */name.equals(romName))
                return;
            mm.setIntent(intent);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    restartApp();
                }
            }).start();
        }
    }

    public String getVersion() {
        String version = "???";
        try {
            version = mm.getPackageManager().getPackageInfo(
                    mm.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public boolean isAndroidTV() {
        try {
            android.app.UiModeManager uiModeManager = (android.app.UiModeManager) mm
                    .getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION)
                return true;
            else
                return false;
        } catch (Throwable e) {
            return false;
        }
    }

    //@SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = mm.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(i);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public boolean copyFile(InputStream input, String path, String fileName) {
        boolean error = false;
        try {
            File file = new File(path, fileName);
            try (OutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
        } catch (Exception e) {
            error = true;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return error;
    }

    public void gotoNewMAME() {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = "com.seleuco.mame4d2024";
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = mm.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                mm.startActivity(rateIntent);
                marketFound = true;
                break;
            }
        }
    }

}

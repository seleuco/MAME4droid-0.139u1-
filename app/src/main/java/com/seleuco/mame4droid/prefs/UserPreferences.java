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

package com.seleuco.mame4droid.prefs;

import java.io.File;
import java.io.FilenameFilter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.R;
import com.seleuco.mame4droid.helpers.PrefsHelper;
import com.seleuco.mame4droid.input.ControlCustomizer;
import com.seleuco.mame4droid.input.InputHandler;
import com.seleuco.mame4droid.input.InputHandlerExt;

public class UserPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private SharedPreferences settings;

	protected ListPreference mPrefGlobalVideoRenderMode;
	protected ListPreference mPrefResolution;
	protected ListPreference mPrefSpeed;	
	protected ListPreference mPrefPortraitMode;
    protected ListPreference mPrefLandsMode;
	protected ListPreference mPrefPortraitOverlay;
    protected ListPreference mPrefLandsOverlay;
    protected ListPreference mPrefControllerType;
    protected ListPreference mPrefExtInput;
    protected ListPreference mPrefAutomap;
    protected ListPreference mPrefAnalogDZ;
    protected ListPreference mPrefGamepadDZ;
    protected ListPreference mPrefTiltDZ;
    protected ListPreference mPrefTiltNeutral;
    protected ListPreference mPrefFrameSkip;
    protected ListPreference mPrefSound;
    protected ListPreference mPrefStickType;
    protected ListPreference mPrefNumButtons;
    protected ListPreference mPrefSizeButtons;
    protected ListPreference mPrefSizeStick;    
    protected ListPreference mPrefVideoThPr;
    protected ListPreference mPrefMainThPr;
    protected ListPreference mPrefSoundEngine;
    protected ListPreference mPrefAutofire;
    protected ListPreference mPrefVSync;
    protected ListPreference mPrefFilterCat;
    protected ListPreference mPrefFilterDrvSrc;
    protected ListPreference mPrefFilterManuf;
    protected ListPreference mPrefFilterYGTE;
    protected ListPreference mPrefFilterYLTE;  
    protected EditTextPreference mPrefFilterkeyword;
    //protected ListPreference mPrefOverlayInt; 
    protected ListPreference mPrefForcPX;
    protected ListPreference mPrefNetplayDelay;
    protected EditTextPreference mPrefNetplayPort;
    protected ListPreference mPrefNavbar;
    protected ListPreference mPrefImageEffect;
    protected EditTextPreference mPrefInstPath;
    protected ListPreference mPrefLightgun;
    protected ListPreference mPrefBios;    
    protected EditTextPreference mPrefRefresh;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
				
		addPreferencesFromResource(R.xml.userpreferences);
						
		settings = PreferenceManager.getDefaultSharedPreferences(this);
				
		mPrefGlobalVideoRenderMode = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_VIDEO_RENDER_MODE);
		mPrefResolution = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_RESOLUTION);
		mPrefSpeed = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_SPEED);
        mPrefPortraitMode = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_PORTRAIT_SCALING_MODE);
        mPrefLandsMode = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_LANDSCAPE_SCALING_MODE);
        
        mPrefPortraitOverlay = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_PORTRAIT_OVERLAY);
        populateOverlayList(mPrefPortraitOverlay);
        
        mPrefLandsOverlay = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_LANDSCAPE_OVERLAY);  
        populateOverlayList(mPrefLandsOverlay);
        
        mPrefControllerType = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_CONTROLLER_TYPE);
        mPrefExtInput = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_INPUT_EXTERNAL);
        mPrefAutomap = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_AUTOMAP_OPTIONS);
        mPrefAnalogDZ = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_ANALOG_DZ); 
        mPrefGamepadDZ = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GAMEPAD_DZ);
        mPrefTiltDZ = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_TILT_DZ);
        mPrefTiltNeutral = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_TILT_NEUTRAL);
        mPrefFrameSkip = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_FRAMESKIP);
        mPrefSound = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_SOUND);
        mPrefStickType = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_STICK_TYPE);
        mPrefNumButtons = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_NUMBUTTONS);
        mPrefSizeButtons = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_BUTTONS_SIZE);    
        mPrefSizeStick = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_STICK_SIZE);    
        mPrefVideoThPr = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_VIDEO_THREAD_PRIORITY);
        mPrefMainThPr = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_MAIN_THREAD_PRIORITY);
        mPrefSoundEngine = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_SOUND_ENGINE);
        mPrefAutofire = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_AUTOFIRE);
        mPrefVSync = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_VSYNC);
        		
		mPrefFilterCat = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_CATEGORY);	
		populateFilterList(Emulator.FILTER_NUM_CATEGORIES,Emulator.FILTER_CATEGORIES_ARRAY,mPrefFilterCat);
		
		mPrefFilterDrvSrc = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_DRVSRC);
		populateFilterList(Emulator.FILTER_NUM_DRIVERS_SRC,Emulator.FILTER_DRIVERS_SRC_ARRAY,mPrefFilterDrvSrc);
        
		mPrefFilterManuf = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_MANUF);
		populateFilterList(Emulator.FILTER_NUM_MANUFACTURERS,Emulator.FILTER_MANUFACTURERS_ARRAY,mPrefFilterManuf);
		
		mPrefFilterYGTE = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_YGTE);
		populateFilterList(Emulator.FILTER_NUM_YEARS,Emulator.FILTER_YEARS_ARRAY,mPrefFilterYGTE);
		mPrefFilterYLTE = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_YLTE);
		populateFilterList(Emulator.FILTER_NUM_YEARS,Emulator.FILTER_YEARS_ARRAY,mPrefFilterYLTE);
		
		mPrefFilterkeyword = (EditTextPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_FILTER_KEYWORD);
		
		//mPrefOverlayInt = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_OVERLAY_INTENSITY);
		mPrefForcPX = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_FORCE_PXASPECT);
		
		mPrefNetplayDelay = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_NETPLAY_DELAY);
		mPrefNetplayPort = (EditTextPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_NETPLAY_PORT);
		mPrefNavbar = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE);
		mPrefImageEffect = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_IMAGE_EFFECT);
		mPrefInstPath = (EditTextPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_INSTALLATION_DIR);
		mPrefLightgun = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_LIGHTGUN);
		mPrefBios = (ListPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_BIOS);
		mPrefRefresh = (EditTextPreference)getPreferenceScreen().findPreference(PrefsHelper.PREF_GLOBAL_REFRESH);
	}
	
	protected void populateFilterList(int key1, int key2, ListPreference lp){
        int i = 0;
		int n = 0; 
		CharSequence[] cs = null;
		CharSequence[] csv = null;
		
		n = Emulator.getValue(key1);
		if(n==-1)return;
		cs = new String[n+1];
		csv = new String[n+1];
		cs[0] = "All";
		csv[0] = "-1";
		while(i<n)
		{			
			i++;
			cs[i] =Emulator.getValueStr(key2,i-1);
			csv[i] = (i-1)+"";		
		}
		lp.setEntries(cs);
		lp.setEntryValues(csv);	
	}
	
	protected void populateOverlayList(ListPreference lp){
		
		CharSequence[] cs = null;
		CharSequence[] csv = null;

		String romDir = getPreferenceScreen().getSharedPreferences().getString(
				PrefsHelper.PREF_INSTALLATION_DIR, "");
		
		romDir += File.separator + "overlays";

		File path = new File(romDir);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				return sel.isFile() && !sel.isHidden()
						&& filename.toLowerCase().endsWith(".png");
			}
		};

		String[] fList = null;
		
		if(path.exists())
		    fList = path.list(filter);
		
		if (fList == null)
			fList = new String[0];

		int n = fList.length;

		cs = new String[n + 1];
		csv = new String[n + 1];

		cs[0] = "None";
		csv[0] = PrefsHelper.PREF_OVERLAY_NONE;

		int i = 0;
		while (i < n) {
			File f = new File(fList[i]);
			i++;
			csv[i] = f.getName();
			cs[i] = f.getName().toLowerCase().replace(".png", "");
		}
		lp.setEntries(cs);
		lp.setEntryValues(csv);
	}
	
	  @Override
	    protected void onResume() {
	        super.onResume();
	        boolean enable;
	        // Setup the initial values
	        //mCheckBoxPreference.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting");
	        mPrefGlobalVideoRenderMode.setSummary("Current value is '" + mPrefGlobalVideoRenderMode.getEntry()+"'");
			enable = Integer.valueOf(mPrefGlobalVideoRenderMode.getValue()).intValue() ==PrefsHelper.PREF_RENDER_GL;
			getPreferenceScreen().findPreference(PrefsHelper.PREF_FORCE_ALTGLPATH).setEnabled(enable);
			getPreferenceScreen().findPreference(PrefsHelper.PREF_RENDER_RGB).setEnabled(enable);
	        
	        mPrefResolution.setSummary("Current value is '" + mPrefResolution.getEntry()+"'");
	        mPrefSpeed.setSummary("Current value is '" + mPrefSpeed.getEntry()+"'");
	        mPrefPortraitMode.setSummary("Current value is '" + mPrefPortraitMode.getEntry()+"'"); 
	        mPrefLandsMode.setSummary("Current value is '" + mPrefLandsMode.getEntry()+"'"); 
	        mPrefPortraitOverlay.setSummary("Current value is '" + mPrefPortraitOverlay.getEntry()+"'"); 
	        mPrefLandsOverlay.setSummary("Current value is '" + mPrefLandsOverlay.getEntry()+"'"); 	        
	        mPrefControllerType.setSummary("Current value is '" + mPrefControllerType.getEntry()+"'");
	        mPrefExtInput.setSummary("Current value is '" + mPrefExtInput.getEntry()+"'");
	        
	        enable = Integer.valueOf(mPrefExtInput.getValue()).intValue() == PrefsHelper.PREF_INPUT_USB_AUTO;
	        getPreferenceScreen().findPreference(PrefsHelper.PREF_AUTOMAP_OPTIONS).setEnabled(enable);
	        getPreferenceScreen().findPreference(PrefsHelper.PREF_DISABLE_RIGHT_STICK).setEnabled(enable);
	        
	        mPrefAutomap.setSummary("Current value is '" + mPrefAutomap.getEntry()+"'");
	        mPrefAnalogDZ.setSummary("Current value is '" + mPrefAnalogDZ.getEntry()+"'");
	        mPrefGamepadDZ.setSummary("Current value is '" + mPrefGamepadDZ.getEntry()+"'");
	        mPrefTiltDZ.setSummary("Current value is '" + mPrefTiltDZ.getEntry()+"'");
	        mPrefTiltNeutral.setSummary("Current value is '" + mPrefTiltNeutral.getEntry()+"'");
	        mPrefFrameSkip.setSummary("Current value is '" + mPrefFrameSkip.getEntry()+"'");
	        mPrefSound.setSummary("Current value is '" + mPrefSound.getEntry()+"'");
	        mPrefStickType.setSummary("Current value is '" + mPrefStickType.getEntry()+"'");
	        mPrefNumButtons.setSummary("Current value is '" + mPrefNumButtons.getEntry()+"'");
	        mPrefSizeButtons.setSummary("Current value is '" + mPrefSizeButtons.getEntry()+"'");
	        mPrefSizeStick.setSummary("Current value is '" + mPrefSizeStick.getEntry()+"'");
	        mPrefVideoThPr.setSummary("Current value is '" + mPrefVideoThPr.getEntry()+"'");
	        mPrefMainThPr.setSummary("Current value is '" + mPrefMainThPr.getEntry()+"'");
	        mPrefSoundEngine.setSummary("Current value is '" + mPrefSoundEngine.getEntry()+"'");
	        mPrefAutofire.setSummary("Current value is '" + mPrefAutofire.getEntry()+"'");
	        mPrefVSync.setSummary("Current value is '" + mPrefVSync.getEntry()+"'");
	        mPrefFilterCat.setSummary("Current value is '" + mPrefFilterCat.getEntry()+"'");
	        mPrefFilterDrvSrc.setSummary("Current value is '" + mPrefFilterDrvSrc.getEntry()+"'");
	        mPrefFilterManuf.setSummary("Current value is '" + mPrefFilterManuf.getEntry()+"'");
	        mPrefFilterYGTE.setSummary("Current value is '" + mPrefFilterYGTE.getEntry()+"'");
	        mPrefFilterYLTE.setSummary("Current value is '" + mPrefFilterYLTE.getEntry()+"'");   
	        mPrefFilterkeyword.setSummary("Current value is '" + mPrefFilterkeyword.getText()+"'"); 
	        //mPrefOverlayInt.setSummary("Current value is '" + mPrefOverlayInt.getEntry()+"'"); 
	        mPrefForcPX.setSummary("Current value is '" + mPrefForcPX.getEntry()+"'");
	        mPrefNetplayDelay.setSummary("Current value is '" + mPrefNetplayDelay.getEntry()+"'");
	        mPrefNetplayPort.setSummary("Current value is '" + mPrefNetplayPort.getText()+"'"); 
	        mPrefNavbar.setSummary("Current value is '" + mPrefNavbar.getEntry()+"'");
	        mPrefImageEffect.setSummary("Current value is '" + mPrefImageEffect.getEntry()+"'");
			mPrefInstPath.setSummary("Current value is '" + mPrefInstPath.getText()+"'");
			mPrefLightgun.setSummary("Current value is '" + mPrefLightgun.getEntry()+"'");
			mPrefBios.setSummary("Current value is '" + mPrefBios.getEntry()+"'");
			mPrefRefresh.setSummary("Current value is '" + mPrefRefresh.getText()+"'"); 
			 
	        // Set up a listener whenever a key changes            
	        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	        
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();

	        // Unregister the listener whenever a key changes            
	        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	    }
	    
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	        // Let's do something a preference values changes
	    	/*
	        if (key.equals(KEY_CHECKBOX_PREFERENCE)) {
	          mCheckBoxPreference.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting");
	        }
	        else*/ 
	        if (key.equals(PrefsHelper.PREF_PORTRAIT_SCALING_MODE)) 
	        {
	            mPrefPortraitMode.setSummary("Current value is '" + mPrefPortraitMode.getEntry()+"'"); 
	        }
	        else if(key.equals(PrefsHelper.PREF_LANDSCAPE_SCALING_MODE))
	        {
	        	mPrefLandsMode.setSummary("Current value is '" + mPrefLandsMode.getEntry()+"'");	
	        }
	        if (key.equals(PrefsHelper.PREF_PORTRAIT_OVERLAY)) 
	        {
	        	mPrefPortraitOverlay.setSummary("Current value is '" + mPrefPortraitOverlay.getEntry()+"'"); 
	        }
	        else if(key.equals(PrefsHelper.PREF_LANDSCAPE_OVERLAY))
	        {
	        	mPrefLandsOverlay.setSummary("Current value is '" + mPrefLandsOverlay.getEntry()+"'");	
	        }	        
	        else if(key.equals(PrefsHelper.PREF_CONTROLLER_TYPE))
	        {	
	            mPrefControllerType.setSummary("Current values is '" + mPrefControllerType.getEntry()+"'");
	        }
	        else if(key.equals(PrefsHelper.PREF_GLOBAL_VIDEO_RENDER_MODE))
	        {		        	
				mPrefGlobalVideoRenderMode.setSummary("Current value is '" + mPrefGlobalVideoRenderMode.getEntry()+"'"); 
				boolean enable = Integer.valueOf(mPrefGlobalVideoRenderMode.getValue()).intValue() ==PrefsHelper.PREF_RENDER_GL;
				getPreferenceScreen().findPreference(PrefsHelper.PREF_FORCE_ALTGLPATH).setEnabled(enable);
				getPreferenceScreen().findPreference(PrefsHelper.PREF_RENDER_RGB).setEnabled(enable);
	        }
	        else if(key.equals(PrefsHelper.PREF_GLOBAL_RESOLUTION))
	        {	
	        	mPrefResolution.setSummary("Current value is '" + mPrefResolution.getEntry()+"'"); 
	        }	
	        else if(key.equals(PrefsHelper.PREF_GLOBAL_SPEED))
	        {	
	        	mPrefSpeed.setSummary("Current value is '" + mPrefSpeed.getEntry()+"'"); 
	        }	        
	        else if(key.equals(PrefsHelper.PREF_INPUT_EXTERNAL))
	        {	
	        	try{InputHandlerExt.resetAutodetected();}catch(Throwable e){};
	        	mPrefExtInput.setSummary("Current value is '" + mPrefExtInput.getEntry()+"'"); 
		        boolean enable = Integer.valueOf(mPrefExtInput.getValue()).intValue() ==PrefsHelper.PREF_INPUT_USB_AUTO;
		        getPreferenceScreen().findPreference(PrefsHelper.PREF_AUTOMAP_OPTIONS).setEnabled(enable);
		        getPreferenceScreen().findPreference(PrefsHelper.PREF_DISABLE_RIGHT_STICK).setEnabled(enable);
	        }
	        else if(key.equals(PrefsHelper.PREF_AUTOMAP_OPTIONS))
	        {	
	        	try{InputHandlerExt.resetAutodetected();}catch(Throwable e){};
	        	mPrefAutomap.setSummary("Current value is '" + mPrefAutomap.getEntry()+"'"); 
	        }	        
	        else if(key.equals(PrefsHelper.PREF_ANALOG_DZ))
	        {	
	        	mPrefAnalogDZ.setSummary("Current value is '" + mPrefAnalogDZ.getEntry()+"'"); 
	        }
	        else if(key.equals(PrefsHelper.PREF_GAMEPAD_DZ))
	        {	
	        	mPrefGamepadDZ.setSummary("Current value is '" + mPrefGamepadDZ.getEntry()+"'"); 
	        }	        
	        else if(key.equals(PrefsHelper.PREF_TILT_DZ))
	        {	
	        	mPrefTiltDZ.setSummary("Current value is '" + mPrefTiltDZ.getEntry()+"'"); 
	        }
	        else if(key.equals(PrefsHelper.PREF_TILT_NEUTRAL))
	        {	
	        	mPrefTiltNeutral.setSummary("Current value is '" + mPrefTiltNeutral.getEntry()+"'"); 
	        }	        
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_FRAMESKIP))
		    {	
		    	mPrefFrameSkip.setSummary("Current value is '" + mPrefFrameSkip.getEntry()+"'"); 
		    }
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_SOUND))
		    {	
		    	mPrefSound.setSummary("Current value is '" + mPrefSound.getEntry()+"'"); 
	        }
		    else if(key.equals(PrefsHelper.PREF_STICK_TYPE))
		    {	
		    	mPrefStickType.setSummary("Current value is '" + mPrefStickType.getEntry()+"'"); 
		    }
		    else if(key.equals(PrefsHelper.PREF_NUMBUTTONS))
		    {	
		    	mPrefNumButtons.setSummary("Current value is '" + mPrefNumButtons.getEntry()+"'"); 
		    }
		    else if(key.equals(PrefsHelper.PREF_BUTTONS_SIZE))
		    {	
		    	mPrefSizeButtons.setSummary("Current value is '" + mPrefSizeButtons.getEntry()+"'"); 
		    }
		    else if(key.equals(PrefsHelper.PREF_STICK_SIZE))
		    {	
		    	mPrefSizeStick.setSummary("Current value is '" + mPrefSizeStick.getEntry()+"'"); 
		    }	        
		    else if(key.equals(PrefsHelper.PREF_VIDEO_THREAD_PRIORITY))
		    {	        
	            mPrefVideoThPr.setSummary("Current value is '" + mPrefVideoThPr.getEntry()+"'");
		    }
			else if(key.equals(PrefsHelper.PREF_MAIN_THREAD_PRIORITY))
			{    
	            mPrefMainThPr.setSummary("Current value is '" + mPrefMainThPr.getEntry()+"'");
			}
		    else if(key.equals(PrefsHelper.PREF_SOUND_ENGINE))
		    {
	            mPrefSoundEngine.setSummary("Current value is '" + mPrefSoundEngine.getEntry()+"'");
		    }    
		    else if(key.equals(PrefsHelper.PREF_AUTOFIRE))
		    {
		    	mPrefAutofire.setSummary("Current value is '" + mPrefAutofire.getEntry()+"'");
		    }  	
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_VSYNC))
		    {
		    	mPrefVSync.setSummary("Current value is '" + mPrefVSync.getEntry()+"'");
		    } 	  
		    else if(key.equals(PrefsHelper.PREF_FILTER_CATEGORY))
		    {
		    	mPrefFilterCat.setSummary("Current value is '" + mPrefFilterCat.getEntry()+"'");
		    }
		    else if(key.equals(PrefsHelper.PREF_FILTER_DRVSRC))
		    {
		    	mPrefFilterDrvSrc.setSummary("Current value is '" + mPrefFilterDrvSrc.getEntry()+"'");
		    } 
		    else if(key.equals(PrefsHelper.PREF_FILTER_MANUF))
		    {
		    	mPrefFilterManuf.setSummary("Current value is '" + mPrefFilterManuf.getEntry()+"'");
		    } 	 
		    else if(key.equals(PrefsHelper.PREF_FILTER_YGTE))
		    {
		    	mPrefFilterYGTE.setSummary("Current value is '" + mPrefFilterYGTE.getEntry()+"'");
		    } 
		    else if(key.equals(PrefsHelper.PREF_FILTER_YLTE))
		    {
		    	mPrefFilterYLTE.setSummary("Current value is '" + mPrefFilterYLTE.getEntry()+"'");
		    } 
		    else if(key.equals(PrefsHelper.PREF_FILTER_KEYWORD))
		    {
		    	mPrefFilterkeyword.setSummary("Current value is '" + mPrefFilterkeyword.getText()+"'");
		    } 	
	        /*
		    else if(key.equals(PrefsHelper.PREF_OVERLAY_INTENSITY))
		    {
		    	mPrefOverlayInt.setSummary("Current value is '" + mPrefOverlayInt.getEntry()+"'");
		    	Emulator.setOverlayFilterValue(PrefsHelper.PREF_OVERLAY_NONE);//forces reload
		    } 
		    */	  
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_FORCE_PXASPECT))
		    {
		    	mPrefForcPX.setSummary("Current value is '" + mPrefForcPX.getEntry()+"'");
		    }
		    else if(key.equals(PrefsHelper.PREF_NETPLAY_PORT))
		    {
		    	mPrefNetplayPort.setSummary("Current value is '" + mPrefNetplayPort.getText()+"'");
		    } 	        
		    else if(key.equals(PrefsHelper.PREF_NETPLAY_DELAY))
		    {
		    	mPrefNetplayDelay.setSummary("Current value is '" + mPrefNetplayDelay.getEntry()+"'");
		    } 	  
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE))
		    {
		    	mPrefNavbar.setSummary("Current value is '" + mPrefNavbar.getEntry()+"'");
		    } 	        
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_IMAGE_EFFECT))
		    {
		    	mPrefImageEffect.setSummary("Current value is '" + mPrefImageEffect.getEntry()+"'");
		    } 
		    else if(key.equals(PrefsHelper.PREF_INSTALLATION_DIR))
		    {
		    	mPrefInstPath.setSummary("Current value is '" + mPrefInstPath.getText()+"'");
		    }
		    else if(key.equals(PrefsHelper.PREF_LIGHTGUN))
		    {
		    	mPrefLightgun.setSummary("Current value is '" + mPrefLightgun.getEntry()+"'");
		    }
		    else if(key.equals(PrefsHelper.PREF_BIOS))
		    {
		    	mPrefBios.setSummary("Current value is '" + mPrefBios.getEntry()+"'");
		    }
		    else if(key.equals(PrefsHelper.PREF_GLOBAL_REFRESH))
		    {
		    	mPrefRefresh.setSummary("Current value is '" + mPrefRefresh.getText()+"'");
		    } 	        
	    }

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				Preference pref) {
			
			if (pref.getKey().equals("defineKeys")) {
				startActivityForResult(new Intent(this, DefineKeys.class), 1);
			}
			else if (pref.getKey().equals("changeRomPath")) {
				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    	builder.setMessage("Are you sure? (app restart needed)")
		    	       .setCancelable(false)
		    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    					SharedPreferences.Editor editor =  settings.edit();
		    					editor.putString(PrefsHelper.PREF_ROMsDIR, null);
		    					editor.commit();
		    					Emulator.setNeedRestart(true);
		    	                //android.os.Process.killProcess(android.os.Process.myPid());  
		    	           }
		    	       })
		    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                dialog.cancel();
		    	           }
		    	       });
			    	Dialog dialog = builder.create();
			    	dialog.show();				
			}
			else if (pref.getKey().equals("defaultsKeys")) {

				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    	builder.setMessage("Are you sure to restore?")
		    	       .setCancelable(false)
		    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    					SharedPreferences.Editor editor =  settings.edit();
		    					
		    					StringBuffer definedKeysStr = new StringBuffer(); 
		    					
		    					for(int i=0; i< InputHandler.defaultKeyMapping.length;i++)
		    					{	
		    						InputHandler.keyMapping[i] = InputHandler.defaultKeyMapping[i];
		    						definedKeysStr.append(InputHandler.defaultKeyMapping[i]+":");
		    					}
		    					editor.putString(PrefsHelper.PREF_DEFINED_KEYS, definedKeysStr.toString());
		    					editor.commit();
		    					//finish();
		    				
		    	           }
		    	       })
		    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                dialog.cancel();
		    	           }
		    	       });
			    	Dialog dialog = builder.create();
			    	dialog.show();
			}
			else if (pref.getKey().equals("customControlLayout")) {
				ControlCustomizer.setEnabled(true);
				finish();
			}
			else if (pref.getKey().equals("defaultControlLayout")) {

				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    	builder.setMessage("Are you sure to restore?")
		    	       .setCancelable(false)
		    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    					SharedPreferences.Editor editor =  settings.edit();
		    					editor.putString(PrefsHelper.PREF_DEFINED_CONTROL_LAYOUT, null);
		    					editor.putString(PrefsHelper.PREF_DEFINED_CONTROL_LAYOUT_P, null);
		    					editor.commit();
		    	           }
		    	       })
		    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                dialog.cancel();
		    	           }
		    	       });
			    	Dialog dialog = builder.create();
			    	dialog.show();
			}	
			else if (pref.getKey().equals("restoreFilters")) {

				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    	builder.setMessage("Are you sure to restore?")
		    	       .setCancelable(false)
		    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	        	SharedPreferences.Editor editor =  settings.edit();
		    	       		editor.putBoolean(PrefsHelper.PREF_FILTER_FAVORITES, false);
		    	    		editor.putBoolean(PrefsHelper.PREF_FILTER_CLONES, false);		
		    	    		editor.putBoolean(PrefsHelper.PREF_FILTER_NOTWORKING, false);
		    	    		editor.putString(PrefsHelper.PREF_FILTER_YGTE, "-1");
		    	    		editor.putString(PrefsHelper.PREF_FILTER_YLTE, "-1");
		    	    		editor.putString(PrefsHelper.PREF_FILTER_MANUF, "-1");
		    	    		editor.putString(PrefsHelper.PREF_FILTER_CATEGORY,"-1");
		    	    		editor.putString(PrefsHelper.PREF_FILTER_DRVSRC, "-1");	
		    	    		editor.putString(PrefsHelper.PREF_FILTER_KEYWORD, "");			
		    	    		editor.commit();
		    	    		finish();
		    	    		startActivity(getIntent());
		    	           }
		    	       })
		    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                dialog.cancel();
		    	           }
		    	       });
			    	Dialog dialog = builder.create();
			    	dialog.show();
			}	
			else if (pref.getKey().equals("defaultData")) {

				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    	builder.setMessage("Are you sure to restore? This will remove all your MAME cfg and nvram files. This is useful to restore games to defaults to fixup mame key mappings or netplay problems.")
		    	       .setCancelable(false)
		    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	        	SharedPreferences.Editor editor =  settings.edit();
		    	       		editor.putBoolean(PrefsHelper.PREF_MAME_DEFAULTS, true);		
		    	    		editor.commit();
		    	    		Emulator.setNeedRestart(true);
		    	           }
		    	       })
		    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                dialog.cancel();
		    	           }
		    	       });
			    	Dialog dialog = builder.create();
			    	dialog.show();
			}
			
			return super.onPreferenceTreeClick(preferenceScreen, pref);
		}
	    
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			
			if (resultCode == RESULT_OK && requestCode == 0) {
				setResult(RESULT_OK, data);
			} 
			else if (requestCode == 1) {
				SharedPreferences.Editor editor =  settings.edit();
					
				StringBuffer definedKeysStr = new StringBuffer(); 
				
				for(int i=0; i< InputHandler.keyMapping.length;i++)
					definedKeysStr.append(InputHandler.keyMapping[i]+":");
				
				editor.putString(PrefsHelper.PREF_DEFINED_KEYS, definedKeysStr.toString());
				editor.commit();
				return;
			}
			finish();
		}
}

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

import android.content.SharedPreferences;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.seleuco.mame4droid.helpers.PrefsHelper;

public class GameFilterPrefs {

    protected int favorites = 0;
    protected int clones = 0;
    protected int not_working = 0;
    protected int gte_year = -1;
    protected int lte_year = -1;
    protected int manufacturer = -1;
    protected int category = -1;
    protected int drvsrc = -1;
    protected String keyword = "";

    protected MAME4droid mm = null;

    public GameFilterPrefs(MAME4droid value) {
        mm = value;
    }

    public boolean readValues() {
        boolean b = false;
        int value = 0;
        String str = null;

        SharedPreferences sp = mm.getPrefsHelper().getSharedPreferences();

        value = sp.getBoolean(PrefsHelper.PREF_FILTER_FAVORITES, false) ? 1 : 0;
        b = value != favorites || b;
        favorites = value;
        value = sp.getBoolean(PrefsHelper.PREF_FILTER_CLONES, false) ? 1 : 0;
        b = value != clones || b;
        clones = value;
        value = sp.getBoolean(PrefsHelper.PREF_FILTER_NOTWORKING, false) ? 1 : 0;
        b = value != not_working || b;
        not_working = value;
        value = Integer.valueOf(sp.getString(PrefsHelper.PREF_FILTER_YGTE, "-1")).intValue();
        b = value != gte_year || b;
        gte_year = value;
        value = Integer.valueOf(sp.getString(PrefsHelper.PREF_FILTER_YLTE, "-1")).intValue();
        b = value != lte_year || b;
        lte_year = value;
        value = Integer.valueOf(sp.getString(PrefsHelper.PREF_FILTER_MANUF, "-1")).intValue();
        b = value != manufacturer || b;
        manufacturer = value;
        value = Integer.valueOf(sp.getString(PrefsHelper.PREF_FILTER_CATEGORY, "-1")).intValue();
        b = value != category || b;
        category = value;
        value = Integer.valueOf(sp.getString(PrefsHelper.PREF_FILTER_DRVSRC, "-1")).intValue();
        b = value != drvsrc || b;
        drvsrc = value;
        str = sp.getString(PrefsHelper.PREF_FILTER_KEYWORD, "");
        b = !str.equals(keyword) || b;
        keyword = str;
        return b;
    }

    public void sendValues() {
        Emulator.setValue(Emulator.FILTER_FAVORITES, favorites);
        Emulator.setValue(Emulator.FILTER_CLONES, clones);
        Emulator.setValue(Emulator.FILTER_NOTWORKING, not_working);
        Emulator.setValue(Emulator.FILTER_GTE_YEAR, gte_year);
        Emulator.setValue(Emulator.FILTER_LTE_YEAR, lte_year);
        Emulator.setValue(Emulator.FILTER_MANUFACTURER, manufacturer);
        Emulator.setValue(Emulator.FILTER_CATEGORY, category);
        Emulator.setValue(Emulator.FILTER_DRVSRC, drvsrc);
        Emulator.setValueStr(Emulator.FILTER_KEYWORD, keyword);
    }

}

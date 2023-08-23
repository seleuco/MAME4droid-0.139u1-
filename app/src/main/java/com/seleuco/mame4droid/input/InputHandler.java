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

package com.seleuco.mame4droid.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.seleuco.mame4droid.helpers.DialogHelper;
import com.seleuco.mame4droid.helpers.PrefsHelper;

public class InputHandler implements OnTouchListener, OnKeyListener, IController {

    protected AnalogStick stick = new AnalogStick();
    protected TiltSensor tiltSensor = new TiltSensor();
    protected ControlCustomizer controlCustomizer = new ControlCustomizer();

    public TiltSensor getTiltSensor() {
        return tiltSensor;
    }

    public ControlCustomizer getControlCustomizer() {
        return controlCustomizer;
    }

    final byte vibrate_time = 1;//16;

    protected static final int[] emulatorInputValues = {
            UP_VALUE,
            DOWN_VALUE,
            LEFT_VALUE,
            RIGHT_VALUE,
            A_VALUE,
            B_VALUE,
            C_VALUE,
            D_VALUE,
            E_VALUE,
            F_VALUE,
            COIN_VALUE,
            START_VALUE,
            EXIT_VALUE,
            OPTION_VALUE
            ///
    };

    public static int[] defaultKeyMapping = {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            //////
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            //////
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            //////
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
    };

    public static int[] keyMapping = new int[emulatorInputValues.length * 4];

    protected int ax = 0;
    protected int ay = 0;
    protected float dx = 1;
    protected float dy = 1;

    protected ArrayList<InputValue> values = new ArrayList<InputValue>();

    protected int[] pad_data = new int[4];

    //protected int newtouch;
    //protected int oldtouch;
    //protected boolean touchstate;

    protected int[] touchContrData = new int[20];
    protected InputValue[] touchKeyData = new InputValue[20];

    protected static int[] newtouches = new int[20];
    protected static int[] oldtouches = new int[20];
    protected static boolean[] touchstates = new boolean[20];

    private boolean up_icade = false;
    private boolean down_icade = false;
    private boolean left_icade = false;
    private boolean right_icade = false;

    protected int trackballSensitivity = 30;
    protected boolean trackballEnabled = true;

    protected int lightgun_pid = -1;

    protected boolean isMouseEnabled = false;

    /////////////////

    final public static int STATE_SHOWING_CONTROLLER = 1;
    final public static int STATE_SHOWING_NONE = 3;

    protected int state = STATE_SHOWING_CONTROLLER;

    final public static int TYPE_MAIN_RECT = 1;
    final public static int TYPE_STICK_RECT = 2;
    final public static int TYPE_BUTTON_RECT = 3;
    final public static int TYPE_STICK_IMG = 4;
    final public static int TYPE_BUTTON_IMG = 5;
    final public static int TYPE_SWITCH = 6;
    final public static int TYPE_OPACITY = 7;
    final public static int TYPE_ANALOG_RECT = 8;


    protected int stick_state;

    public int getStick_state() {
        return stick_state;
    }

    protected int old_stick_state;

    protected int btnStates[] = new int[NUM_BUTTONS];

    public int[] getBtnStates() {
        return btnStates;
    }

    protected int old_btnStates[] = new int[NUM_BUTTONS];

    protected MAME4droid mm = null;

    protected boolean iCade = false;

    //protected Timer timer = new Timer();

    protected Handler handler = new Handler();

    protected Object lock = new Object();

    protected Runnable finishTrackBallMove = new Runnable() {
        //@Override
        public void run() {
            //synchronized(lock){
            //System.out.println("---> INIT C");
            //System.out.println("+CLEAR Set Pad "+pad_data+ " new:"+newtrack+" old:"+oldtrack);
            //pad_data &= ~oldtrack;
            pad_data[0] &= ~UP_VALUE;
            pad_data[0] &= ~DOWN_VALUE;
            pad_data[0] &= ~LEFT_VALUE;
            pad_data[0] &= ~RIGHT_VALUE;
            Emulator.setPadData(0, pad_data[0]);

            //System.out.println("++CLEAR Set Pad "+pad_data+ " new:"+newtrack+" old:"+oldtrack);
            //System.out.println("---> CLEAR");
            //}
        }
    };

    public InputHandler(MAME4droid value) {

        mm = value;

        fakeID = mm.getPrefsHelper().isFakeID();

        stick.setMAME4droid(mm);
        tiltSensor.setMAME4droid(mm);
        controlCustomizer.setMAME4droid(mm);

        if (mm == null) return;

        if (mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            state = mm.getPrefsHelper().isLandscapeTouchController() ? STATE_SHOWING_CONTROLLER : STATE_SHOWING_NONE;
        } else {
            state = mm.getPrefsHelper().isPortraitTouchController() ? STATE_SHOWING_CONTROLLER : STATE_SHOWING_NONE;
        }

        stick_state = old_stick_state = STICK_NONE;
        for (int i = 0; i < NUM_BUTTONS; i++)
            btnStates[i] = old_btnStates[i] = BTN_NO_PRESS_STATE;

        resetInput();

    }

    public static Boolean fakeID = false;
    public static Boolean hasMethodControllerNumber = false;

    public static int getGamePadId(InputDevice id) {
        int iDeviceId = 0;
        int iControllerNumber = 0;

        try {
            if (!fakeID)
                iDeviceId = id.getId();
            else
                iDeviceId = 0;
        } catch (Exception e) {
        }

        if (hasMethodControllerNumber && !fakeID) { // upper android 4.4
            try {
                Method method = id.getClass().getMethod("getControllerNumber");
                if (method != null) {
                    iControllerNumber = ((Integer) method.invoke(id)).intValue();
                }
            } catch (Exception e) {
            }
            if (iControllerNumber > 0)
                iDeviceId = iControllerNumber;
        }
        return iDeviceId;
    }

    public static int makeKeyCodeWithDeviceID(InputDevice id, int iKeyCode) {
        int padid = 0;
        try {
            padid = getGamePadId(id);
        } catch (Exception e) {
        }

        return makeKeyCodeWithDeviceID(padid, iKeyCode);
    }

    public static int makeKeyCodeWithDeviceID(int iDeviceId, int iKeyCode) {
        int iRet = 0;

        //iRet = ((iDeviceId * 1000) + iKeyCode);//type 1

        //type 2
        iRet = iDeviceId;
        iRet = iRet << 16;
        iRet |= iKeyCode;
        //type 2 end

        return iRet;
    }

    public static void getInfoFromKeyCodeWithDeviceID(int iKeyCode, int[] iArrRet) {
        int iDeviceIdRet = 0;
        int iKeyCodeRet = 0;

        //type 1
		/*iDeviceIdRet = iKeyCode / 1000;
		iKeyCodeRet = iKeyCode % 1000;
		*/
        //type 1 end

        //type 2
        iDeviceIdRet = iKeyCode >> 16;
        iKeyCodeRet = iKeyCode & 0xFFFF;
        //type 2 end

        iArrRet[0] = iDeviceIdRet;
        iArrRet[1] = iKeyCodeRet;
    }

    public static int getDeviceIdFromKeyCodeWithDeviceID(int iKeyCode) {
        //return  iKeyCode / 1000; //type 1
        return iKeyCode >> 16; //type 2
    }

    public static int getKeyCodeFromKeyCodeWithDeviceID(int iKeyCode) {
        //return  iKeyCode % 1000;  //type 1
        return iKeyCode & 0xFFFF; //type 2
    }

    public void resetInput() {
        for (int i = 0; i < 4 * 3; i++) {
            try {
                if (i < 4) {
                    pad_data[i] = 0;
                    Emulator.setPadData(i, pad_data[i]);
                }
                Emulator.setAnalogData(i, 0, 0);
            } catch (Throwable e) {
            }
        }
    }

    public int setInputHandlerState(int value) {
        return state = value;
    }

    public int getInputHandlerState() {
        return state;
    }

    public boolean isMouseEnabled() {
        return isMouseEnabled;
    }

    public void changeState() {
        if (state == STATE_SHOWING_CONTROLLER) {
            resetInput();
            state = STATE_SHOWING_NONE;
        } else {
            state = STATE_SHOWING_CONTROLLER;
        }
    }

    public void setTrackballSensitivity(int trackballSensitivity) {
        this.trackballSensitivity = trackballSensitivity;
    }

    public void setTrackballEnabled(boolean trackballEnabled) {
        this.trackballEnabled = trackballEnabled;
    }

    public void setFixFactor(int ax, int ay, float dx, float dy) {
        this.ax = ax;
        this.ay = ay;
        this.dx = dx;
        this.dy = dy;
        fixControllerCoords(values);
    }

    protected boolean setPadData(int i, KeyEvent event, int data) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN)
            pad_data[i] |= data;
        else if (action == KeyEvent.ACTION_UP)
            pad_data[i] &= ~data;
        return true;
    }

    protected boolean handlePADKey(int value, KeyEvent event) {

        int v = emulatorInputValues[value % emulatorInputValues.length];

        if (v == EXIT_VALUE) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (Emulator.isInMenu()) {
                    Emulator.setValue(Emulator.EXIT_GAME_KEY, 1);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Emulator.setValue(Emulator.EXIT_GAME_KEY, 0);
                } else if (!Emulator.isInMAME()) {
                    mm.showDialog(DialogHelper.DIALOG_EXIT);
                } else {
                    if (mm.getPrefsHelper().isWarnOnExit())
                        mm.showDialog(DialogHelper.DIALOG_EXIT_GAME);
                    else {
                        Emulator.setValue(Emulator.EXIT_GAME_KEY, 1);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        Emulator.setValue(Emulator.EXIT_GAME_KEY, 0);
                    }
                }
            }
        } else if (v == OPTION_VALUE) {
            if (event.getAction() == KeyEvent.ACTION_UP)
                mm.showDialog(DialogHelper.DIALOG_OPTIONS);
        } else {
            int i = value / emulatorInputValues.length;
            setPadData(i, event, v);
            fixTiltCoin();
            Emulator.setPadData(i, pad_data[i]);
        }

        return true;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //Log.d("TECLA", "onKeyDown=" + keyCode + " " + event.getAction() + " " + event.getDisplayLabel() + " " + event.getUnicodeChar() + " " + event.getNumber());

        if (ControlCustomizer.isEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mm.showDialog(DialogHelper.DIALOG_FINISH_CUSTOM_LAYOUT);
            }
            return true;
        }

        if (mm.getPrefsHelper().getInputExternal() == PrefsHelper.PREF_INPUT_ICADE || mm.getPrefsHelper().getInputExternal() == PrefsHelper.PREF_INPUT_ICP) {
            this.handleIcade(event);
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            handlePADKey(12, event);
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            handlePADKey(13, event);
            return true;
        }

        if ((event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_START || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_SELECT) && !Emulator.isInMAME() && mm.getMainHelper().isAndroidTV()) {
            handlePADKey(13, event);
            return true;
        }

        int value = -1;
        for (int i = 0; i < keyMapping.length; i++) {
            //if(keyMapping[i]==keyCode)
            if (keyMapping[i] == makeKeyCodeWithDeviceID(event.getDevice(), keyCode))
                value = i;
        }

        //if(value >=0 && value <=13)
        if (value != -1)
            if (handlePADKey(value, event)) return true;

        return false;
    }

    public void handleVirtualKey(int action) {

        pad_data[0] |= action;
        fixTiltCoin();
        Emulator.setPadData(0, pad_data[0]);

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pad_data[0] &= ~action;
        Emulator.setPadData(0, pad_data[0]);

    }

    //debug method
    public ArrayList<InputValue> getAllInputData() {
        if (state == STATE_SHOWING_CONTROLLER)
            return values;
        else
            return null;
    }

    public Rect getMainRect() {
        if (values == null)
            return null;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getType() == TYPE_MAIN_RECT)
                return values.get(i).getOrigRect();
        }
        return null;
    }

    protected void handleImageStates(boolean onlyStick) {

        PrefsHelper pH = mm.getPrefsHelper();

        if (!pH.isAnimatedInput() && !pH.isVibrate())
            return;

        switch ((int) pad_data[0] & (UP_VALUE | DOWN_VALUE | LEFT_VALUE | RIGHT_VALUE)) {
            case UP_VALUE:
                stick_state = STICK_UP;
                break;
            case DOWN_VALUE:
                stick_state = STICK_DOWN;
                break;
            case LEFT_VALUE:
                stick_state = STICK_LEFT;
                break;
            case RIGHT_VALUE:
                stick_state = STICK_RIGHT;
                break;

            case UP_VALUE | LEFT_VALUE:
                stick_state = STICK_UP_LEFT;
                break;
            case UP_VALUE | RIGHT_VALUE:
                stick_state = STICK_UP_RIGHT;
                break;
            case DOWN_VALUE | LEFT_VALUE:
                stick_state = STICK_DOWN_LEFT;
                break;
            case DOWN_VALUE | RIGHT_VALUE:
                stick_state = STICK_DOWN_RIGHT;
                break;

            default:
                stick_state = STICK_NONE;
        }

        for (int j = 0; j < values.size(); j++) {
            InputValue iv = values.get(j);
            if (iv.getType() == TYPE_STICK_IMG && pH.getControllerType() == PrefsHelper.PREF_DIGITAL_DPAD) {
                if (stick_state != old_stick_state) {
                    if (pH.isAnimatedInput()) {
                        //System.out.println("CAMBIA STICK! "+stick_state+" != "+old_stick_state+" "+iv.getRect()+ " "+iv.getOrigRect()+" "+values.size()+"  POS:"+j+ " "+onlyStick+ " "+this);
                        mm.getInputView().invalidate(iv.getRect());
                    }
                    if (pH.isVibrate()) {
                        try {
                            Vibrator v = (Vibrator) mm.getSystemService(Context.VIBRATOR_SERVICE);
                            if (v != null) v.vibrate(vibrate_time);
                        } catch (Exception e) {
                        }
                    }
                    old_stick_state = stick_state;
                }
            } else if (iv.getType() == TYPE_ANALOG_RECT && pH.getControllerType() != PrefsHelper.PREF_DIGITAL_DPAD) {
                if (stick_state != old_stick_state) {
                    if (pH.isAnimatedInput() && (pH.getControllerType() == PrefsHelper.PREF_ANALOG_FAST || pH.getControllerType() == PrefsHelper.PREF_DIGITAL_STICK ||
                            (mm.getPrefsHelper().getControllerType() == PrefsHelper.PREF_ANALOG_PRETTY && tiltSensor.isEnabled()))) {
                        if (pH.isDebugEnabled())
                            mm.getInputView().invalidate();
                        else
                            mm.getInputView().invalidate(iv.getRect());
                    }
                    if (pH.isVibrate()) {
                        try {
                            Vibrator v = (Vibrator) mm.getSystemService(Context.VIBRATOR_SERVICE);
                            if (v != null) v.vibrate(vibrate_time);
                        } catch (Exception e) {
                        }
                    }
                    old_stick_state = stick_state;
                }
            } else if (iv.getType() == TYPE_BUTTON_IMG && !onlyStick) {
                int i = iv.getValue();

                btnStates[i] = (pad_data[0] & getButtonValue(i, false)) != 0 ? BTN_PRESS_STATE : BTN_NO_PRESS_STATE;

                if (btnStates[iv.getValue()] != old_btnStates[iv.getValue()]) {
                    if (pH.isAnimatedInput())
                        mm.getInputView().invalidate(iv.getRect());
                    if (pH.isVibrate()) {
                        try {
                            Vibrator v = (Vibrator) mm.getSystemService(Context.VIBRATOR_SERVICE);
                            if (v != null) v.vibrate(15);
                        } catch (Exception e) {
                        }
                    }
                    old_btnStates[iv.getValue()] = btnStates[iv.getValue()];
                }
            }
        }
    }

    protected void fixTiltCoin() {
        if (tiltSensor.isEnabled() && ((pad_data[0] & IController.COIN_VALUE) != 0 || (pad_data[0] & IController.START_VALUE) != 0)) {
            pad_data[0] &= ~InputHandler.LEFT_VALUE;
            pad_data[0] &= ~InputHandler.RIGHT_VALUE;
            pad_data[0] &= ~InputHandler.UP_VALUE;
            pad_data[0] &= ~InputHandler.DOWN_VALUE;
            Emulator.setAnalogData(0, 0, 0);
        }
    }

    protected boolean handleLightgun(View v, MotionEvent event) {
        int pid = 0;
        int action = event.getAction();
        int actionEvent = action & MotionEvent.ACTION_MASK;

        try {
            int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            pid = event.getPointerId(pointerIndex);
        } catch (Throwable e) {
            pid = (action & MotionEvent.ACTION_POINTER_ID_SHIFT) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        }

        if (actionEvent == MotionEvent.ACTION_UP ||
                actionEvent == MotionEvent.ACTION_POINTER_UP ||
                actionEvent == MotionEvent.ACTION_CANCEL) {
            if (pid == lightgun_pid) {
                lightgun_pid = -1;
                //Emulator.setAnalogData(4, 0, 0);
                pad_data[0] &= ~A_VALUE;
                pad_data[0] &= ~B_VALUE;
            } else {
                pad_data[0] &= ~B_VALUE;
            }
            Emulator.setPadData(0, pad_data[0]);
        } else {
            for (int i = 0; i < event.getPointerCount(); i++) {

                int pointerId = event.getPointerId(i);

                final int location[] = {0, 0};
                v.getLocationOnScreen(location);
                int x = (int) event.getX(i) + location[0];
                int y = (int) event.getY(i) + location[1];

                //System.out.println("x:"+event.getX(i)+" y:"+event.getY(i)+" nx:"+x+" ny:"+y+" l0:"+location[0]+" l1:"+location[1]);

                mm.getEmuView().getLocationOnScreen(location);
                x -= location[0];
                y -= location[1];

                float xf = (float) (x - mm.getEmuView().getWidth() / 2) / (float) (mm.getEmuView().getWidth() / 2);
                float yf = (float) (y - mm.getEmuView().getHeight() / 2) / (float) (mm.getEmuView().getHeight() / 2);

                //System.out.println("nx2:"+x+" ny2:"+y+" l0:"+location[0]+" l1:"+location[1]+" xf:"+xf+" yf:"+yf);

                if (lightgun_pid == -1)
                    lightgun_pid = pointerId;


                if (lightgun_pid == pointerId) {
                    if (mm.getPrefsHelper().isBottomReload()) {
                        if (yf > 0.90)
                            yf = 1.1f;
                    }

                    if (!tiltSensor.isEnabled())
                        Emulator.setAnalogData(4, xf, -yf);

                    if ((pad_data[0] & B_VALUE) == 0)
                        pad_data[0] |= A_VALUE;
                } else {
                    pad_data[0] &= ~A_VALUE;
                    pad_data[0] |= B_VALUE;
                }
            }
            Emulator.setPadData(0, pad_data[0]);
        }
        return true;
    }

    protected boolean handleTouchController(MotionEvent event) {

        int action = event.getAction();
        int actionEvent = action & MotionEvent.ACTION_MASK;

        int pid = 0;

        try {
            int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            pid = event.getPointerId(pointerIndex);
        } catch (Throwable e) {
            pid = (action & MotionEvent.ACTION_POINTER_ID_SHIFT) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        }

        //dumpEvent(event);

        for (int i = 0; i < 10; i++) {
            touchstates[i] = false;
            oldtouches[i] = newtouches[i];
        }

        for (int i = 0; i < event.getPointerCount(); i++) {

            int actionPointerId = event.getPointerId(i);

            int x = (int) event.getX(i);
            int y = (int) event.getY(i);

            if (actionPointerId == getAnalogStick().getMotionPid())
                continue;

            if (actionEvent == MotionEvent.ACTION_UP
                    || (actionEvent == MotionEvent.ACTION_POINTER_UP && actionPointerId == pid)
                    || actionEvent == MotionEvent.ACTION_CANCEL) {
                //nada
            } else {
                //int id = i;
                int id = actionPointerId;
                if (id > touchstates.length)
                    continue;//strange but i have this error on my development console
                touchstates[id] = true;
                //newtouches[id] = 0;

                for (int j = 0; j < values.size(); j++) {
                    InputValue iv = values.get(j);

                    if (iv.getRect().contains(x, y)) {

                        //Log.d("touch","HIT "+iv.getType()+" "+iv.getRect()+ " "+iv.getOrigRect());

                        if (iv.getType() == TYPE_BUTTON_RECT || iv.getType() == TYPE_STICK_RECT) {

                            switch (actionEvent) {

                                case MotionEvent.ACTION_DOWN:
                                case MotionEvent.ACTION_POINTER_DOWN:
                                case MotionEvent.ACTION_MOVE:

                                    boolean b =
                                            !mm.getPrefsHelper().isLightgun()
                                                    || (mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT && !mm.getPrefsHelper().isPortraitFullscreen())
                                                    || Emulator.isInMenu() || !Emulator.isInMAME() ||
                                                    iv.getValue() == BTN_EXIT || iv.getValue() == BTN_OPTION || iv.getValue() == BTN_COIN || iv.getValue() == BTN_START;

                                    if (iv.getType() == TYPE_BUTTON_RECT && b) {

                                        if ((iv.getValue() == BTN_COIN || iv.getValue() == BTN_EXIT) && stick_state != STICK_NONE &&
                                                mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT &&
                                                !tiltSensor.isEnabled()
                                        )
                                            continue;

                                        newtouches[id] |= getButtonValue(iv.getValue(), true);

                                        if (iv.getValue() == BTN_EXIT && actionEvent != MotionEvent.ACTION_MOVE) {
                                            if (Emulator.isInMenu()) {
                                                Emulator.setValue(Emulator.EXIT_GAME_KEY, 1);
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                }
                                                Emulator.setValue(Emulator.EXIT_GAME_KEY, 0);
                                            } else if (!Emulator.isInMAME())
                                                mm.showDialog(DialogHelper.DIALOG_EXIT);
                                            else
                                                mm.showDialog(DialogHelper.DIALOG_EXIT_GAME);
                                        } else if (iv.getValue() == BTN_OPTION) {
                                            mm.showDialog(DialogHelper.DIALOG_OPTIONS);
                                        }
                                    } else if (mm.getPrefsHelper().getControllerType() == PrefsHelper.PREF_DIGITAL_DPAD
                                            && !((tiltSensor.isEnabled() || (mm.getPrefsHelper().isLightgun() && !(mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT && !mm.getPrefsHelper().isPortraitFullscreen())))
                                            && Emulator.isInMAME() && !Emulator.isInMenu())) {
                                        newtouches[id] = getStickValue(iv.getValue());
                                    }

                                    if (oldtouches[id] != newtouches[id])
                                        pad_data[0] &= ~(oldtouches[id]);

                                    pad_data[0] |= newtouches[id];
                            }

                            if (mm.getPrefsHelper().isBplusX() && (iv.getValue() == BTN_A || iv.getValue() == BTN_B))
                                break;

                        }
                    }
                }
            }
        }

        for (int i = 0; i < touchstates.length; i++) {
            if (!touchstates[i] && newtouches[i] != 0) {
                boolean really = true;

                for (int j = 0; j < 10 && really; j++) {
                    if (j == i)
                        continue;
                    really = (newtouches[j] & newtouches[i]) == 0;//try to fix something buggy touch screens
                }

                if (really) {
                    pad_data[0] &= ~(newtouches[i]);
                }

                newtouches[i] = 0;
                oldtouches[i] = 0;
            }
        }

        handleImageStates(false);

        fixTiltCoin();

        Emulator.setPadData(0, pad_data[0]);
        return true;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //Log.d("touch",event.getRawX()+" "+event.getX()+" "+event.getRawY()+" "+event.getY());
        if (mm == null /*|| mm.getMainHelper()==null*/) return false;

        if (v == mm.getEmuView() && mm.getPrefsHelper().isLightgun() && state != STATE_SHOWING_NONE && Emulator.isInMAME() && !Emulator.isInMenu()) {
            handleLightgun(v, event);
            return true;

        } else if (v == mm.getInputView()) {
            if (ControlCustomizer.isEnabled()) {
                controlCustomizer.handleMotion(event);
                return true;
            }

            if (mm.getPrefsHelper().getControllerType() != PrefsHelper.PREF_DIGITAL_DPAD && !(tiltSensor.isEnabled() && Emulator.isInMAME() && !Emulator.isInMenu()))
                pad_data[0] = stick.handleMotion(event, pad_data[0]);

            if (mm.getPrefsHelper().isLightgun() && Emulator.isInMAME() && !Emulator.isInMenu() &&
                    !(mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT && !mm.getPrefsHelper().isPortraitFullscreen())
            )
                handleLightgun(v, event);

            handleTouchController(event);

            return true;
        } else {
            if ((mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT && state != STATE_SHOWING_NONE)
                    ||
                    (mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_LANDSCAPE && state != STATE_SHOWING_NONE)) {
                if (mm.getPrefsHelper().isLightgun() && Emulator.isInMAME() && !Emulator.isInMenu()) {
                    handleLightgun(v, event);
                    return true;
                }
                return false;
            }

            mm.showDialog(DialogHelper.DIALOG_FULLSCREEN);
            return true;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {

        int gap = 0;

        if (!trackballEnabled) return false;

        int action = event.getAction();

        if (action == MotionEvent.ACTION_MOVE /*&& trackballEnabled*/) {

            int newtrack = 0;

            final float x = event.getX();
            final float y = event.getY();

            //float d = Math.max(Math.abs(x), Math.abs(y));

            // System.out.println("x: "+x+" y:"+y);

            if (y < -gap) {
                newtrack |= UP_VALUE;
                // System.out.println("Up");
            } else if (y > gap) {
                newtrack |= DOWN_VALUE;
                // System.out.println("Down");
            }

            if (x < -gap) {
                newtrack |= LEFT_VALUE;
                // System.out.println("left_icade");
            } else if (x > gap) {
                newtrack |= RIGHT_VALUE;
                // System.out.println("right_icade");
            }

            // System.out.println("Set Pad "+pad_data+
            // " new:"+newtrack+" old:"+oldtrack);

            handler.removeCallbacks(finishTrackBallMove);
            handler.postDelayed(finishTrackBallMove, (int) (/* 50 * d */150 * trackballSensitivity));// TODO

            if (newtrack != 0) {
                pad_data[0] &= ~UP_VALUE;
                pad_data[0] &= ~DOWN_VALUE;
                pad_data[0] &= ~LEFT_VALUE;
                pad_data[0] &= ~RIGHT_VALUE;
                pad_data[0] |= newtrack;
            }

        } else if (action == MotionEvent.ACTION_DOWN) {
            pad_data[0] |= A_VALUE;

        } else if (action == MotionEvent.ACTION_UP) {
            pad_data[0] &= ~A_VALUE;
        }

        fixTiltCoin();
        Emulator.setPadData(0, pad_data[0]);

        return true;
    }

    protected void fixControllerCoords(ArrayList<InputValue> values) {

        if (values != null) {
            for (int i = 0; i < values.size(); i++) {

                values.get(i).setFixData(dx, dy, ax, ay);

                if (values.get(i).getType() == TYPE_ANALOG_RECT)
                    stick.setStickArea(values.get(i).getRect());
            }
        }
    }

    protected void setButtonsSizes(ArrayList<InputValue> values) {

        if (mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT && !Emulator.isPortraitFull())
            return;

        int sz = 0;
        switch (mm.getPrefsHelper().getButtonsSize()) {
            case 1:
                sz = -30;
                break;
            case 2:
                sz = -20;
                break;
            case 3:
                sz = 0;
                break;
            case 4:
                sz = 20;
                break;
            case 5:
                sz = 30;
                break;
        }
        int sz2 = 0;
        switch (mm.getPrefsHelper().getStickSize()) {
            case 1:
                sz2 = -30;
                break;
            case 2:
                sz2 = -20;
                break;
            case 3:
                sz2 = 0;
                break;
            case 4:
                sz2 = 20;
                break;
            case 5:
                sz2 = 30;
                break;
        }
        if (values == null || (sz == 0 && sz2 == 0))
            return;

        for (int j = 0; j < values.size(); j++) {

            InputValue iv = values.get(j);
            if (iv.getType() == InputHandler.TYPE_BUTTON_IMG
                    || iv.getType() == InputHandler.TYPE_BUTTON_RECT) {
                if (iv.getValue() != BTN_EXIT && iv.getValue() != BTN_OPTION && iv.getValue() != BTN_START && iv.getValue() != BTN_COIN)
                    iv.setSize(0, 0, sz, sz);
            } else if (iv.getType() == InputHandler.TYPE_STICK_IMG) {
                iv.setSize(0, 0, sz2, sz2);
            } else if (iv.getType() == InputHandler.TYPE_STICK_RECT) {
                switch (iv.getValue()) {
                    case 1:
                        iv.setSize(0, 0, 0, 0);
                        break;//upleft
                    case 2:
                        iv.setSize(0, 0, sz2, 0);
                        break;//up
                    case 3:
                        iv.setSize(sz2, 0, sz2, 0);
                        break;//upright
                    case 4:
                        iv.setSize(0, 0, sz2 / 2, sz2);
                        break;//left
                    case 5:
                        iv.setSize(sz2 / 2, 0, sz2, sz2);
                        break;//right
                    case 6:
                        iv.setSize(0, sz2, 0, sz2);
                        break;//downleft
                    case 7:
                        iv.setSize(0, sz2, sz2, sz2);
                        break;     //down
                    case 8:
                        iv.setSize(sz, sz2, sz2, sz2);
                        break;//downright
                    default:
                        iv.setSize(0, 0, sz2, sz2);
                }
            } else if (iv.getType() == InputHandler.TYPE_ANALOG_RECT) {
                iv.setSize(0, 0, sz2, sz2);
                mm.getInputHandler().getAnalogStick().setStickArea(iv.getRect());
            }
        }
    }

    public AnalogStick getAnalogStick() {
        return stick;
    }

    public int getOpacity() {

        ArrayList<InputValue> data = null;
        if (state == STATE_SHOWING_CONTROLLER)
            data = values;
        else
            return -1;

        for (InputValue v : data) {
            if (v.getType() == TYPE_OPACITY)
                return v.getValue();
        }
        return -1;
    }

    public void readControllerValues(int v) {
        readInputValues(v, values);
        fixControllerCoords(values);
        setButtonsSizes(values);
        if (controlCustomizer != null)
            controlCustomizer.readDefinedControlLayout();
    }

    protected void readInputValues(int id, ArrayList<InputValue> values) {
        System.out.println("readInputValues");
        InputStream is = mm.getResources().openRawResource(id);

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        InputValue iv = null;
        values.clear();

        //int i=0;
        try {
            String s = br.readLine();
            while (s != null) {
                int[] data = new int[10];
                if (s.trim().startsWith("//")) {
                    s = br.readLine();
                    continue;
                }
                StringTokenizer st = new StringTokenizer(s, ",");
                int j = 0;
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int k = token.indexOf("/");
                    if (k != -1) {
                        token = token.substring(0, k);
                    }

                    token = token.trim();
                    if (token.equals(""))
                        break;
                    data[j] = Integer.parseInt(token);
                    j++;
                    if (k != -1) break;
                }

                //values.
                if (j != 0) {
                    iv = new InputValue(data, mm);
                    values.add(iv);
                }
                s = br.readLine();//i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getStickValue(int i) {
        int ways = mm.getPrefsHelper().getStickWays();
        if (ways == -1) ways = Emulator.getValue(Emulator.NUMWAYS);
        boolean b = Emulator.isInMAME() && !Emulator.isInMenu();

        if (ways == 2 && b) {
            switch (i) {
                case 1:
                    return LEFT_VALUE;
                case 3:
                    return RIGHT_VALUE;
                case 4:
                    return LEFT_VALUE;
                case 5:
                    return RIGHT_VALUE;
                case 6:
                    return LEFT_VALUE;
                case 8:
                    return RIGHT_VALUE;
            }
        } else if (ways == 4 /*&& b*/ || !b) {
            switch (i) {
                case 1:
                    return LEFT_VALUE;
                case 2:
                    return UP_VALUE;
                case 3:
                    return RIGHT_VALUE;
                case 4:
                    return LEFT_VALUE;
                case 5:
                    return RIGHT_VALUE;
                case 6:
                    return LEFT_VALUE;
                case 7:
                    return DOWN_VALUE;
                case 8:
                    return RIGHT_VALUE;
            }
        } else {
            switch (i) {
                case 1:
                    return UP_VALUE | LEFT_VALUE;
                case 2:
                    return UP_VALUE;
                case 3:
                    return UP_VALUE | RIGHT_VALUE;
                case 4:
                    return LEFT_VALUE;
                case 5:
                    return RIGHT_VALUE;
                case 6:
                    return DOWN_VALUE | LEFT_VALUE;
                case 7:
                    return DOWN_VALUE;
                case 8:
                    return DOWN_VALUE | RIGHT_VALUE;
            }
        }
        return 0;
    }

    int getButtonValue(int i, boolean b) {
        switch (i) {
            case 0:
                return D_VALUE;
            case 1:
                if (mm.getPrefsHelper().isBplusX() && b) {
                    return B_VALUE | A_VALUE | C_VALUE; //El A lo pongo para que salte la animación
                } else {
                    return C_VALUE;
                }
            case 2:
                return A_VALUE;
            case 3:
                return B_VALUE;

            case 4:
                return E_VALUE;
            case 5:
                return F_VALUE;
            case 6:
                return EXIT_VALUE;
            case 7:
                return OPTION_VALUE;

            case 8:
                return COIN_VALUE;
            case 9:
                return START_VALUE;

            case 10:
                return B_VALUE | C_VALUE;
            case 11://TODO
                if (mm.getPrefsHelper().isBplusX() && mm.getPrefsHelper().getNumButtons() >= 3) {
                    return B_VALUE | A_VALUE;
                } else
                    return 0;

            case 12:
                return D_VALUE | C_VALUE;
            case 13:
                return D_VALUE | A_VALUE;
        }
        return 0;
    }

    protected void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    //action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
                    (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        //if(action != MotionEvent.ACTION_MOVE)
        Log.d("touch", sb.toString());
    }

    protected void handleIcade(KeyEvent event) {

        int action = event.getAction();
        if (action != KeyEvent.ACTION_DOWN)
            return;

        int ways = mm.getPrefsHelper().getStickWays();
        if (ways == -1) ways = Emulator.getValue(Emulator.NUMWAYS);
        boolean b = Emulator.isInMAME() && !Emulator.isInMenu();

        int keyCode = event.getKeyCode();

        boolean bCadeLayout = mm.getPrefsHelper().getInputExternal() == PrefsHelper.PREF_INPUT_ICADE;

        long old_pad_data = pad_data[0];

        switch (keyCode) {
            // joystick up_icade
            case KeyEvent.KEYCODE_W:
                if (ways == 4 /*&& b*/ || !b) {
                    pad_data[0] &= ~LEFT_VALUE;
                    pad_data[0] &= ~RIGHT_VALUE;
                }
                if (!(ways == 2 && b))
                    pad_data[0] |= UP_VALUE;
                up_icade = true;
                break;
            case KeyEvent.KEYCODE_E:
                if (ways == 4 && b) {
                    if (left_icade) pad_data[0] |= LEFT_VALUE;
                    if (right_icade) pad_data[0] |= RIGHT_VALUE;
                }
                pad_data[0] &= ~UP_VALUE;
                up_icade = false;
                break;

            // joystick down_icade
            case KeyEvent.KEYCODE_X:
                if (ways == 4 /*&& b*/ || !b) {
                    pad_data[0] &= ~LEFT_VALUE;
                    pad_data[0] &= ~RIGHT_VALUE;
                }
                if (!(ways == 2 && b))
                    pad_data[0] |= DOWN_VALUE;
                down_icade = true;
                break;
            case KeyEvent.KEYCODE_Z:
                if (ways == 4 && b) {
                    if (left_icade) pad_data[0] |= LEFT_VALUE;
                    if (right_icade) pad_data[0] |= RIGHT_VALUE;
                }
                pad_data[0] &= ~DOWN_VALUE;
                down_icade = false;
                break;

            // joystick right_icade
            case KeyEvent.KEYCODE_D:
                if (ways == 4 /*&& b*/ || !b) {
                    pad_data[0] &= ~UP_VALUE;
                    pad_data[0] &= ~DOWN_VALUE;
                }
                pad_data[0] |= RIGHT_VALUE;
                right_icade = true;
                break;
            case KeyEvent.KEYCODE_C:
                if (ways == 4 /*&& b*/ || !b) {
                    if (up_icade) pad_data[0] |= UP_VALUE;
                    if (down_icade) pad_data[0] |= DOWN_VALUE;
                }
                pad_data[0] &= ~RIGHT_VALUE;
                right_icade = false;
                break;

            // joystick left_icade
            case KeyEvent.KEYCODE_A:
                if (ways == 4 /*&& b*/ || !b) {
                    pad_data[0] &= ~UP_VALUE;
                    pad_data[0] &= ~DOWN_VALUE;
                }
                pad_data[0] |= LEFT_VALUE;
                left_icade = true;
                break;
            case KeyEvent.KEYCODE_Q:
                if (ways == 4 /*&& b*/ || !b) {
                    if (up_icade) pad_data[0] |= UP_VALUE;
                    if (down_icade) pad_data[0] |= DOWN_VALUE;
                }
                pad_data[0] &= ~LEFT_VALUE;
                left_icade = false;
                break;

            // Y / UP
            case KeyEvent.KEYCODE_I:
                pad_data[0] |= D_VALUE;
                break;
            case KeyEvent.KEYCODE_M:
                pad_data[0] &= ~D_VALUE;
                break;

            // X / DOWN
            case KeyEvent.KEYCODE_L:
                pad_data[0] |= B_VALUE;
                break;
            case KeyEvent.KEYCODE_V:
                pad_data[0] &= ~B_VALUE;
                break;

            // A / LEFT
            case KeyEvent.KEYCODE_K:
                pad_data[0] |= C_VALUE;
                break;
            case KeyEvent.KEYCODE_P:
                pad_data[0] &= ~C_VALUE;
                break;

            // B / RIGHT
            case KeyEvent.KEYCODE_O:
                pad_data[0] |= A_VALUE;
                break;
            case KeyEvent.KEYCODE_G:
                pad_data[0] &= ~A_VALUE;
                break;

            // SELECT / COIN
            case KeyEvent.KEYCODE_Y:
                pad_data[0] |= COIN_VALUE;
                break;
            case KeyEvent.KEYCODE_T:
                pad_data[0] &= ~COIN_VALUE;
                break;

            // START
            case KeyEvent.KEYCODE_U:
                if (bCadeLayout) {
                    pad_data[0] |= E_VALUE;
                } else {
                    pad_data[0] |= START_VALUE;
                }
                break;
            case KeyEvent.KEYCODE_F:
                if (bCadeLayout) {
                    pad_data[0] &= ~E_VALUE;
                } else {
                    pad_data[0] &= ~START_VALUE;
                }
                break;

            //
            case KeyEvent.KEYCODE_H:
                if (bCadeLayout) {
                    pad_data[0] |= START_VALUE;
                } else {
                    pad_data[0] |= E_VALUE;
                }
                break;
            case KeyEvent.KEYCODE_R:
                if (bCadeLayout) {
                    pad_data[0] &= ~START_VALUE;
                } else {
                    pad_data[0] &= ~E_VALUE;
                }
                break;

            //
            case KeyEvent.KEYCODE_J:
                pad_data[0] |= F_VALUE;
                break;
            case KeyEvent.KEYCODE_N:
                pad_data[0] &= ~F_VALUE;
                break;
        }
        if (!iCade && old_pad_data == 0 && pad_data[0] != 0) {
            iCade = true;
            mm.getMainHelper().updateMAME4droid();
        }

        fixTiltCoin();
        Emulator.setPadData(0, pad_data[0]);
    }

    public void setInputListeners() {

        mm.getEmuView().setOnKeyListener(this);
        mm.getEmuView().setOnTouchListener(this);

        mm.getInputView().setOnTouchListener(this);
        mm.getInputView().setOnKeyListener(this);

        //mm.findViewById(R.id.EmulatorFrame).setOnTouchListener(this);
        //mm.findViewById(R.id.EmulatorFrame).setOnKeyListener(this);
    }

    public void unsetInputListeners() {
        if (mm == null)
            return;
        if (mm.getInputView() == null)
            return;
        if (mm.getEmuView() == null)
            return;

        mm.getEmuView().setOnKeyListener(null);
        mm.getEmuView().setOnTouchListener(null);

        mm.getInputView().setOnTouchListener(null);
        mm.getInputView().setOnKeyListener(null);
    }

    public boolean isControllerDevice() {
        return iCade;
    }

    public void resume() {
    }

    public boolean genericMotion(MotionEvent event) {
        return false;
    }

}

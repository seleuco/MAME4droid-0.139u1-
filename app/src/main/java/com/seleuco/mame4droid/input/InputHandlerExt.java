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

//import static com.seleuco.mame4droid.input.InputHandlerExt.resetAutodetected;

import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Build;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.widget.Toast;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.seleuco.mame4droid.helpers.DialogHelper;
import com.seleuco.mame4droid.helpers.MainHelper;
import com.seleuco.mame4droid.helpers.PrefsHelper;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class InputHandlerExt extends InputHandler implements OnGenericMotionListener {

    static protected int MAX_DEVICES = 4;
    static protected int MAX_KEYS = 250;

    protected float MY_PI = 3.14159265f;
    protected int oldinput[] = new int[MAX_DEVICES], newinput[] = new int[MAX_DEVICES];

    public static int deviceIDs[] = new int[MAX_DEVICES];
    //public  static int id = 0;

    protected int[][] deviceMappings = new int[MAX_KEYS][MAX_DEVICES];

    protected static SparseIntArray banDev = new SparseIntArray(50);

    protected boolean gf_NVMouseExtensions;

    public static void resetAutodetected() {
        //id = 0;
        for (int i = 0; i < deviceIDs.length; i++)
            deviceIDs[i] = -1;
        banDev.clear();
    }

    public InputHandlerExt(MAME4droid value) {
        super(value);

        //vemos dispositivos!

        int ids[] = InputDevice.getDeviceIds();
        for (int i = 0; i < ids.length; i++) {
            InputDevice id = InputDevice.getDevice(ids[i]);
            if(id!=null) {
                System.out.println("name: " + id.getName());
                System.out.println(id.toString());

                try {
                    Method method = id.getClass().getMethod("getControllerNumber");
                    if (method != null) {
                        method.invoke(id);
                        hasMethodControllerNumber = true;
                    }
                } catch (Exception e) {
                }
            }
        }

        resetAutodetected();

        gf_NVMouseExtensions = false;
        if (mm.getPrefsHelper().isMouseEnabled()) {
            try {
                Wrap_NVMouseExtensions.checkAvailable();
                gf_NVMouseExtensions = true;
            } catch (Throwable t) {
            }
        }

    }

    final public float rad2degree(float r) {
        return ((r * 180.0f) / MY_PI);
    }

    protected float processAxis(InputDevice.MotionRange range, float axisvalue) {
        float absaxisvalue = Math.abs(axisvalue);
        float deadzone = range.getFlat();
        //System.out.println("deadzone: "+deadzone);
        //deadzone = Math.max(deadzone, 0.2f);
        if (absaxisvalue <= deadzone) {
            return 0.0f;
        }
        float nomralizedvalue;
        if (axisvalue < 0.0f) {
            nomralizedvalue = absaxisvalue / range.getMin();
        } else {
            nomralizedvalue = absaxisvalue / range.getMax();
        }

        return nomralizedvalue;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //System.out.println("touch: "+event.getRawX()+" "+event.getX()+" "+event.getRawY()+" "+event.getY());
        if (mm == null) return false;

        if (gf_NVMouseExtensions) {
            boolean isMouse = event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE;
            if (isMouse) {
                if (isMouseEnabled) {
                    if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                        float flRelativeX = event.getAxisValue(Wrap_NVMouseExtensions.getAxisRelativeX(), 0);
                        float flRelativeY = event.getAxisValue(Wrap_NVMouseExtensions.getAxisRelativeY(), 0);

                        Emulator.setAnalogData(8, flRelativeX, flRelativeY);
                    }

                    int pressedButtons = event.getButtonState();

                    if ((pressedButtons & MotionEvent.BUTTON_PRIMARY) != 0) {
                        pad_data[0] |= A_VALUE;
                    } else {
                        pad_data[0] &= ~A_VALUE;
                    }
                    if ((pressedButtons & MotionEvent.BUTTON_SECONDARY) != 0) {
                        pad_data[0] |= B_VALUE;
                    } else {
                        pad_data[0] &= ~B_VALUE;
                    }
                    if ((pressedButtons & MotionEvent.BUTTON_TERTIARY) != 0) {
                        pad_data[0] |= C_VALUE;
                    } else {
                        pad_data[0] &= ~C_VALUE;
                    }
                    Emulator.setPadData(0, pad_data[0]);

                }
                return true;
            }
        }
        return super.onTouch(view, event);
    }


    public boolean genericMotion(MotionEvent event) {

        if (gf_NVMouseExtensions) {
            if (Emulator.isInMAME()) {
                if ((event.getSource() & InputDevice.SOURCE_MOUSE) != 0) {
                    if (!isMouseEnabled) {
                        isMouseEnabled = true;
                        CharSequence text = "Mouse is enabled!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(mm, text, duration);
                        toast.show();

                        mm.getMainHelper().updateMAME4droid();
                        resetInput();
                    }

                    //String strOutput = "";
                    if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
                        //strOutput += "Mouse Move: (" + event.getRawX() + ", " + event.getRawY() + ")\n";
                        float flRelativeX = event.getAxisValue(Wrap_NVMouseExtensions.getAxisRelativeX(), 0);
                        float flRelativeY = event.getAxisValue(Wrap_NVMouseExtensions.getAxisRelativeY(), 0);

                        //strOutput += "Relative: (" + flRelativeX + ",  " + flRelativeY + ")\n";
                        //System.out.println(strOutput);
                        Emulator.setAnalogData(8, flRelativeX, flRelativeY);
                    }

                    return true;
                }
            }
        }

        if (mm.getPrefsHelper().getInputExternal() != PrefsHelper.PREF_INPUT_USB_AUTO) {
            return false;
        }

        if (((event.getSource() & (InputDevice.SOURCE_CLASS_JOYSTICK | InputDevice.SOURCE_GAMEPAD)) == 0)
                || (event.getAction() != MotionEvent.ACTION_MOVE)) {
            return false;
        }
        int historySize = event.getHistorySize();
        for (int i = 0; i < historySize; i++) {
            processJoystickInput(event, i);
        }

        return processJoystickInput(event, -1);
    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        return false;
    }

    final public float getAxisValue(int axis, MotionEvent event, int historyPos) {
        float value = 0.0f;
        InputDevice device = event.getDevice();
        if (device != null) {
            InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
            if (range != null) {
                float axisValue;
                if (historyPos >= 0) {
                    axisValue = event.getHistoricalAxisValue(axis, historyPos);
                } else {
                    axisValue = event.getAxisValue(axis);
                }
                value = this.processAxis(range, axisValue);
                //System.out.print("x: "+x);
            }
        }
        return value;
    }

    final public float getAngle(float x, float y) {
        float ang = rad2degree((float) Math.atan(y / x));
        ang -= 90.0f;
        if (x < 0.0f)
            ang -= 180.0f;
        ang = Math.abs(ang);
        return ang;
    }

    final public float getMagnitude(float x, float y) {
        return (float) Math.sqrt((x * x) + (y * y));
    }

    protected boolean processJoystickInput(MotionEvent event, int historyPos) {

        int ways = mm.getPrefsHelper().getStickWays();
        if (ways == -1) ways = Emulator.getValue(Emulator.NUMWAYS);
        boolean b = Emulator.isInMAME() && !Emulator.isInMenu();

        int dev = getDevice(event.getDevice(), false);

        int iDeviceId = 0;
        try {
            iDeviceId = getGamePadId(event.getDevice());
        } catch (Exception e) {
        }

        if (dev == -1) { //no autodetectado
            for (int i = 0; i < MAX_DEVICES; i++) {
                if (iDeviceId == getDeviceIdFromKeyCodeWithDeviceID(keyMapping[i * emulatorInputValues.length])) // select each devices input settings first item UP_VALUE dpad setting not applicable seperate
                {
                    dev = i;
                    break;
                }
            }
        }

        int joy = dev != -1 ? dev : 0;

        newinput[joy] = 0;

        float deadZone = 0.2f;

        switch (mm.getPrefsHelper().getGamepadDZ()) {
            case 1:
                deadZone = 0.01f;
                break;
            case 2:
                deadZone = 0.15f;
                break;
            case 3:
                deadZone = 0.2f;
                break;
            case 4:
                deadZone = 0.3f;
                break;
            case 5:
                deadZone = 0.5f;
                break;
        }

        //System.out.println("DEAD ZONE IS "+deadZone);

        float x = 0.0f;
        float y = 0.0f;
        float mag = 0.0f;

        for (int i = 0; i < 2; i++) {
            if (i == 0 && tiltSensor.isEnabled() && Emulator.isInMAME() && !Emulator.isInMenu())
                continue;

            if (i == 0) {
                x = getAxisValue(MotionEvent.AXIS_X, event, historyPos);
                y = getAxisValue(MotionEvent.AXIS_Y, event, historyPos);
            } else {
                x = getAxisValue(MotionEvent.AXIS_HAT_X, event, historyPos);
                y = getAxisValue(MotionEvent.AXIS_HAT_Y, event, historyPos);
            }

            mag = getMagnitude(x, y);

            if (mag >= deadZone) {
                if (dev == -1) {
                    dev = getDevice(event.getDevice(), true);
                    if (dev != -1) {
                        joy = dev;
                        newinput[joy] = 0;
                    }
                }

                if (i == 0) {
                    Emulator.setAnalogData(joy, x, y * -1.0f);
                    if (Emulator.isInMAME())
                        continue;
                }

                float v = getAngle(x, y);

                if (ways == 2 && b) {
                    if (v < 180) {
                        newinput[joy] |= RIGHT_VALUE;
                    } else if (v >= 180) {
                        newinput[joy] |= LEFT_VALUE;
                    }
                } else if (ways == 4 || !b) {
                    if (v >= 315 || v < 45) {
                        newinput[joy] |= DOWN_VALUE;
                    } else if (v >= 45 && v < 135) {
                        newinput[joy] |= RIGHT_VALUE;
                    } else if (v >= 135 && v < 225) {
                        newinput[joy] |= UP_VALUE;
                    } else if (v >= 225 && v < 315) {
                        newinput[joy] |= LEFT_VALUE;
                    }
                } else {
                    if (v >= 330 || v < 30) {
                        newinput[joy] |= DOWN_VALUE;
                    } else if (v >= 30 && v < 60) {
                        newinput[joy] |= DOWN_VALUE;
                        newinput[joy] |= RIGHT_VALUE;
                    } else if (v >= 60 && v < 120) {
                        newinput[joy] |= RIGHT_VALUE;
                    } else if (v >= 120 && v < 150) {

                        newinput[joy] |= RIGHT_VALUE;
                        newinput[joy] |= UP_VALUE;
                    } else if (v >= 150 && v < 210) {
                        newinput[joy] |= UP_VALUE;
                    } else if (v >= 210 && v < 240) {
                        newinput[joy] |= UP_VALUE;
                        newinput[joy] |= LEFT_VALUE;
                    } else if (v >= 240 && v < 300) {
                        newinput[joy] |= LEFT_VALUE;
                    } else if (v >= 300 && v < 330) {
                        newinput[joy] |= LEFT_VALUE;
                        newinput[joy] |= DOWN_VALUE;
                    }
                }
            } else {
                if (i == 0) {
                    Emulator.setAnalogData(joy, 0, 0);
                }
            }
        }

        if (!mm.getPrefsHelper().isDisabledRightStick() && Emulator.isInMAME()) {

            x = getAxisValue(MotionEvent.AXIS_Z, event, historyPos);
            y = getAxisValue(MotionEvent.AXIS_RZ, event, historyPos) * -1;

            if ((x != 0 || y != 0) && mm.getPrefsHelper().isShieldControllerAsMouse()) {
                if (event.getDevice().getName().indexOf("NVIDIA Controller") != -1)
                    return false;
            }

            mag = getMagnitude(x, y);

            if (mag >= deadZone) {

                float v = getAngle(x, y);

                if (v >= 330 || v < 30) {
                    newinput[joy] |= D_VALUE;
                } else if (v >= 30 && v < 60) {
                    newinput[joy] |= D_VALUE;
                    newinput[joy] |= A_VALUE;
                } else if (v >= 60 && v < 120) {
                    newinput[joy] |= A_VALUE;
                } else if (v >= 120 && v < 150) {
                    newinput[joy] |= A_VALUE;
                    newinput[joy] |= B_VALUE;
                } else if (v >= 150 && v < 210) {
                    newinput[joy] |= B_VALUE;
                } else if (v >= 210 && v < 240) {
                    newinput[joy] |= B_VALUE;
                    newinput[joy] |= C_VALUE;
                } else if (v >= 240 && v < 300) {
                    newinput[joy] |= C_VALUE;
                } else if (v >= 300 && v < 330) {
                    newinput[joy] |= C_VALUE;
                    newinput[joy] |= D_VALUE;
                }
            }
        }

        x = getAxisValue(MotionEvent.AXIS_LTRIGGER, event, historyPos);
        //System.out.println("x:"+x);
        if (x >= 0.35f) {
            if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_THUMBS_DISABLED_L2R2_AS_COINSTART)
                newinput[joy] |= COIN_VALUE;
            else if (mm.getPrefsHelper().getAutomapOptions() != PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED)
                newinput[joy] |= E_VALUE;
        }
        y = getAxisValue(MotionEvent.AXIS_RTRIGGER, event, historyPos);
        //System.out.println("y:"+y);
        if (y >= 0.35f) {
            if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_THUMBS_DISABLED_L2R2_AS_COINSTART)
                newinput[joy] |= START_VALUE;
            else if (mm.getPrefsHelper().getAutomapOptions() != PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED)
                newinput[joy] |= F_VALUE;
        }

        pad_data[joy] &= ~(oldinput[joy] & ~newinput[joy]);
        pad_data[joy] |= newinput[joy];

        fixTiltCoin();

        Emulator.setPadData(joy, pad_data[joy]);

        oldinput[joy] = newinput[joy];

        return true;
    }

    public boolean onKey(View vw, int keyCode, KeyEvent event) {

        if (mm.getPrefsHelper().getInputExternal() != PrefsHelper.PREF_INPUT_USB_AUTO) {
            return super.onKey(vw, keyCode, event);
        }

        if (ControlCustomizer.isEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mm.showDialog(DialogHelper.DIALOG_FINISH_CUSTOM_LAYOUT);
            }
            return true;
        }

        int dev = getDevice(event.getDevice(), true);

        //System.out.println(event.getDevice().getName()+" "+dev+" "+" "+event.getKeyCode());
        //System.out.println("IME:"+Settings.Secure.getString(mm.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD));

        if (dev == -1) {
			
			/*
			if(mm.getMainHelper().getDeviceDetected() == MainHelper.DEVICE_SHIELD && event.getKeyCode()==KeyEvent.KEYCODE_BACK)
			{
				handlePADKey(12, event);
				return true;
			}
			*/
            return super.onKey(vw, keyCode, event);
        }

        int v = deviceMappings[event.getKeyCode()][dev];

        if (v != -1) {
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
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (event.getDevice().getName().indexOf("OUYA") != -1)
                        mm.showDialog(DialogHelper.DIALOG_FULLSCREEN);
                    else
                        mm.showDialog(DialogHelper.DIALOG_OPTIONS);
                }
            } else {
                int action = event.getAction();
                if (action == KeyEvent.ACTION_DOWN) {
                    pad_data[dev] |= v;
                } else if (action == KeyEvent.ACTION_UP)
                    pad_data[dev] &= ~v;

                fixTiltCoin();

                Emulator.setPadData(dev, pad_data[dev]);
            }
            return true;
        }

        return false;
    }

    public void setInputListeners() {

        super.setInputListeners();

        //mm.getEmuView().setOnGenericMotionListener(this);
        //mm.getInputView().setOnGenericMotionListener(this);
    }

    public void unsetInputListeners() {

        super.unsetInputListeners();

        if (mm == null)
            return;
        if (mm.getInputView() == null)
            return;
        if (mm.getEmuView() == null)
            return;

        //mm.getEmuView().setOnGenericMotionListener(null);
        //mm.getInputView().setOnGenericMotionListener(null);
    }

    protected int getDevice(InputDevice device, boolean detect) {

        if (mm.getPrefsHelper().getInputExternal() != PrefsHelper.PREF_INPUT_USB_AUTO)
            return -1;

        if (device == null)
            return -1;
        //dav
        if (device.getId() == -1)
            return -1;
        ///
        for (int i = 0; i < MAX_DEVICES; i++) {
            if (deviceIDs[i] == device.getId())
                return i;
        }

        //clean dissconected devices
        int ids[] = InputDevice.getDeviceIds();
        for (int i = 0; i < MAX_DEVICES; i++) {
            boolean found = false;
            for (int j = 0; j < ids.length && !found; j++) {
                found = deviceIDs[i] == ids[j];
            }
            if (!found) {
                deviceIDs[i] = -1;
                banDev.clear();
            }
        }

        if (detect)
            return detectDevice(device);
        else
            return -1;
    }

    protected void mapDPAD(int id) {
        deviceMappings[KeyEvent.KEYCODE_DPAD_UP][id] = UP_VALUE;
        deviceMappings[KeyEvent.KEYCODE_DPAD_DOWN][id] = DOWN_VALUE;
        deviceMappings[KeyEvent.KEYCODE_DPAD_LEFT][id] = LEFT_VALUE;
        deviceMappings[KeyEvent.KEYCODE_DPAD_RIGHT][id] = RIGHT_VALUE;
    }

    protected void mapL1R1(int id) {

        if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_L1R1_AS_EXITMENU_L2R2_AS_L1R1) {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = EXIT_VALUE;
        } else if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_L1R1_AS_COINSTART_L2R2_AS_L1R1) {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = START_VALUE;
        } else {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;
        }
    }

    protected void mapL2R2(int id) {

        if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_THUMBS_DISABLED_L2R2_AS_COINSTART) {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = START_VALUE;
        } else if (mm.getPrefsHelper().getAutomapOptions() != PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED) {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;
        }
    }


    protected void mapTHUMBS(int id) {

        if (mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_AS_L1R2 ||
                mm.getPrefsHelper().getAutomapOptions() == PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED
        ) {
            deviceMappings[KeyEvent.KEYCODE_BUTTON_THUMBL][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_THUMBR][id] = START_VALUE;
        }
    }

    protected void mapSelectStart(int id) {
        deviceMappings[KeyEvent.KEYCODE_BUTTON_SELECT][id] = EXIT_VALUE;
        deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = OPTION_VALUE;
    }


    protected int detectDevice(InputDevice device) {

        boolean detected = false;

        int id = -1;
        for (int i = 0; i < MAX_DEVICES && id == -1; i++) {
            if (deviceIDs[i] == -1)
                id = i;
        }

        if (id == -1)
            return -1;

        if (device == null || banDev == null)
            return -1;

        if (banDev.get(device.getId()) == 1)
            return -1;

        final String name = device.getName();

        if (Emulator.isDebug()) {
            mm.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mm, "Detected input device: " + name, Toast.LENGTH_LONG).show();
                }
            });
        }

        CharSequence desc = "";

        if (name.indexOf("PLAYSTATION(R)3") != -1 || name.indexOf("Dualshock3") != -1
                || name.indexOf("Sixaxis") != -1 || name.indexOf("Gasia,Co") != -1
        ) {

            //deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = Y_VALUE;
            //deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = X_VALUE;
            //deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = A_VALUE;
            //deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = B_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapDPAD(id);
            mapL1R1(id);
            mapTHUMBS(id);
            mapSelectStart(id);

            //deviceMappings[KeyEvent.KEYCODE_BACK][id] = SELECT_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BACK][id] = EXIT_VALUE;

            desc = "Sixaxis";

            detected = true;
        } else if (name.indexOf("Gamepad 0") != -1 || name.indexOf("Gamepad 1") != -1 //Sixaxis Controller
                || name.indexOf("Gamepad 1") != -1 || name.indexOf("Gamepad 2") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapDPAD(id);
            mapL1R1(id);
            mapTHUMBS(id);
            mapSelectStart(id);

            desc = "Gamepad";

            detected = true;
        } else if (name.indexOf("nvidia_joypad") != -1 || name.indexOf("NVIDIA Controller") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapL1R1(id);
            mapTHUMBS(id);

            //deviceMappings[KeyEvent.KEYCODE_BACK][id] = SELECT_VALUE;
            //deviceMappings[KeyEvent.KEYCODE_BUTTON_START] [id]= START_VALUE;


            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BACK][id] = EXIT_VALUE;

            detected = true;

            desc = "NVIDIA Shield";
        } else if (name.indexOf("ipega Extending") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapL1R1(id);
            mapTHUMBS(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_SELECT][id] = EXIT_VALUE;

            detected = true;

            desc = "Ipega Extending Game";
        }
        //else if (name.indexOf("X-Box 360")!=-1 || name.indexOf("X-Box")!=-1
        //		   || name.indexOf("Xbox 360 Wireless Receiver")!=-1 || name.indexOf("Xbox Wireless")!=-1 ){
        else if (name.indexOf("X-Box") != -1 || name.indexOf("Xbox") != -1) {


            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapDPAD(id);
            mapL1R1(id);
            mapTHUMBS(id);
            mapSelectStart(id);

            deviceMappings[KeyEvent.KEYCODE_BACK][id] = EXIT_VALUE;

            desc = "XBox";

            detected = true;
        } else if (name.indexOf("Logitech") != -1 && name.indexOf("Dual Action") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = A_VALUE;

            mapL1R1(id);
            mapTHUMBS(id);
            mapSelectStart(id);

            desc = "Dual Action";

            detected = true;
        } else if (name.indexOf("Logitech") != -1 && name.indexOf("RumblePad 2") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_10][id] = START_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_11][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_12][id] = EXIT_VALUE;

            desc = "Rumblepad 2";

            detected = true;

        } else if (name.indexOf("Logitech") != -1 && name.indexOf("Precision") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_10][id] = START_VALUE;

            desc = "Logitech Precision";

            detected = true;
        } else if (name.indexOf("TTT THT Arcade console 2P USB Play") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = F_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = START_VALUE;

            desc = "TTT THT Arcade";

            detected = true;
        } else if (name.indexOf("TOMMO NEOGEOX Arcade Stick") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_C][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = START_VALUE;

            desc = "TOMMO Neogeo X Arcade";

            detected = true;
        } else if (name.indexOf("Onlive Wireless Controller") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BACK][id] = START_VALUE;

            desc = "Onlive Wireless";

            detected = true;
        } else if (name.indexOf("MadCatz") != -1 && name.indexOf("PC USB Wired Stick") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_C][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Z][id] = E_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = START_VALUE;

            desc = "Madcatz PC USB Stick";

            detected = true;
        } else if (name.indexOf("Logicool") != -1 && name.indexOf("RumblePad 2") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_C][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = C_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Z][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = START_VALUE;

            desc = "Logicool Rumblepad 2";

            detected = true;
        } else if (name.indexOf("Zeemote") != -1 && name.indexOf("Steelseries free") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_MODE][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = START_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;

            desc = "Zeemote Steelseries";

            detected = true;
        } else if (name.indexOf("HuiJia  USB GamePad") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_10][id] = START_VALUE;

            desc = "Huijia USB SNES";

            detected = true;
        } else if (name.indexOf("Smartjoy Family Super Smartjoy 2") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = START_VALUE;

            desc = "Super Smartjoy";

            detected = true;
        } else if (name.indexOf("Jess Tech Dual Analog Rumble Pad") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_11][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_12][id] = START_VALUE;

            //desc = "Super Smartjoy";

            detected = true;
        } else if (name.indexOf("Microsoft") != -1 && name.indexOf("Dual Strike") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = OPTION_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = START_VALUE;

            desc = "MS Dual Strike";

            detected = true;
        } else if (name.indexOf("Microsoft") != -1 && name.indexOf("SideWinder") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Z][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_C][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_11][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_12][id] = START_VALUE;

            desc = "MS Sidewinder";

            detected = true;
        } else if (name.indexOf("WiseGroup") != -1 &&
                (name.indexOf("JC-PS102U") != -1 || name.indexOf("TigerGame") != -1) ||
                name.indexOf("Game Controller Adapter") != -1 || name.indexOf("Dual USB Joypad") != -1 ||
                name.indexOf("Twin USB Joystick") != -1
        ) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_13][id] = UP_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_15][id] = DOWN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_16][id] = LEFT_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_14][id] = RIGHT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = A_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_10][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_9][id] = START_VALUE;

            desc = "PlayStation2";

            detected = true;
        } else if (name.indexOf("MOGA") != -1 || name.indexOf("Moga") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L1][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_SELECT][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = START_VALUE;

            desc = "MOGA";

            detected = true;
        } else if (name.indexOf("OUYA Game Controller") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;

            deviceMappings[KeyEvent.KEYCODE_MENU][id] = OPTION_VALUE;

            mapL1R1(id);
            //mapL2R2(id);
            mapTHUMBS(id);

            desc = "OUYA";

            detected = true;
        } else if (name.indexOf("DragonRise") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_2][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_3][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_4][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_1][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_5][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_6][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_7][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_8][id] = START_VALUE;

            desc = "DragonRise";

            detected = true;
        } else if (name.indexOf("Thrustmaster T Mini") != -1) {

            mapDPAD(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = D_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_C][id] = A_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Z][id] = F_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R1][id] = EXIT_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = START_VALUE;

            desc = "Thrustmaster T Mini";

            detected = true;
        } else if (name.indexOf("ADC joystick") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = F_VALUE;

            mapDPAD(id);
            mapL1R1(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_SELECT][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = START_VALUE;

            desc = "JXD S7800";
            detected = true;
        } else if (name.indexOf("Green Throttle Atlas") != -1) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapDPAD(id);
            mapL1R1(id);
            mapTHUMBS(id);
            mapSelectStart(id);

            deviceMappings[KeyEvent.KEYCODE_BACK][id] = EXIT_VALUE;

            desc = "Green Throttle";
            detected = true;
        } else if (name.indexOf("joy_key") != -1 && mm.getMainHelper().getDeviceDetected() == MainHelper.DEVICE_AGAMEPAD2) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            deviceMappings[KeyEvent.KEYCODE_BUTTON_L2][id] = E_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_R2][id] = F_VALUE;

            mapDPAD(id);
            mapL1R1(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_SELECT][id] = COIN_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = START_VALUE;

            desc = "Archos Gamepad 2";
            detected = true;
        } else if (name.indexOf("NYKO PLAYPAD") != -1 ||
                (name.indexOf("Broadcom Bluetooth HID") != -1 && mm.getMainHelper().getDeviceDetected() == MainHelper.DEVICE_SHIELD)) {

            deviceMappings[KeyEvent.KEYCODE_BUTTON_A][id] = B_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_B][id] = A_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_X][id] = C_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BUTTON_Y][id] = D_VALUE;

            mapL1R1(id);
            mapTHUMBS(id);

            deviceMappings[KeyEvent.KEYCODE_BUTTON_START][id] = OPTION_VALUE;
            deviceMappings[KeyEvent.KEYCODE_BACK][id] = EXIT_VALUE;

            detected = true;

            desc = "NYKO PLAYPAD";
        }

        //JOYPAD_B = X_VALUE
        //JOYPAD_Y = A_VALUE
        //JOYPAD_A = B_VALUE
        //JOYPAD_X = Y_VALUE

        if (detected) {
            System.out.println("Controller detected: " + device.getName());
            deviceIDs[id] = device.getId();
            id++;
            if (id == 1)
                mm.getMainHelper().updateMAME4droid();

            CharSequence text = "Detected " + desc + " controller as P" + id;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(mm, text, duration);
            toast.show();

            return id - 1;
        } else {
            banDev.append(device.getId(), 1);
        }

        return -1;
    }

    public boolean isControllerDevice() {
        if (isMouseEnabled && !Emulator.isInMAME())
            isMouseEnabled = false;
        int numDevs = 0;
        for (int i = 0; i < MAX_DEVICES; i++) {
            if (deviceIDs[i] != -1)
                numDevs++;
        }
        return numDevs != 0 || iCade || (isMouseEnabled && !Emulator.isInMenu());
    }

    protected void setMouseVisibility(boolean fVisibility) {
        if (!gf_NVMouseExtensions) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;

        InputManager inputManager = (InputManager) mm.getSystemService(Context.INPUT_SERVICE);

        Wrap_NVMouseExtensions.setCursorVisibility(inputManager, fVisibility);
    }

    public void resume() {
        super.resume();
        setMouseVisibility(false);
    }
}

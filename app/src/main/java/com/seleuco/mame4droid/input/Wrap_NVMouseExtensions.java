package com.seleuco.mame4droid.input;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.hardware.input.InputManager;
import android.util.Log;
import android.view.MotionEvent;

class NVMouseExtensionException extends RuntimeException
{
	private static final String LOGCAT_NVMOUSEEXT_TAG = "NvMouseExt";
	private static final long serialVersionUID = 2115360613743138916L;

	public NVMouseExtensionException(int err)
	{
		String strError = "Unknown";
		switch (err)
		{
			case (Wrap_NVMouseExtensions.ERR_NVMOUSEEXT_NOMETHOD):   { strError = "setCursorVisibility method doesn't exist";	break;	}
			case (Wrap_NVMouseExtensions.ERR_NVMOUSEEXT_ACCESSEX_X): { strError = "AXIS_RELATIVE_X not accessable";						break;	}
			case (Wrap_NVMouseExtensions.ERR_NVMOUSEEXT_ACCESSEX_Y): { strError = "AXIS_RELATIVE_Y not accessable";						break;	}
			case (Wrap_NVMouseExtensions.ERR_NVMOUSEEXT_NOFIELD_X):  { strError = "AXIS_RELATIVE_X field doesn't exist";			break;	}
			case (Wrap_NVMouseExtensions.ERR_NVMOUSEEXT_NOFIELD_Y):  { strError = "AXIS_RELATIVE_Y field doesn't exist";			break;	}
		}
		
		Log.d(LOGCAT_NVMOUSEEXT_TAG, "**** NvMouseExt Error - " + strError);
	};
}

class Wrap_NVMouseExtensions
{
	public static final int ERR_NVMOUSEEXT_NOMETHOD		= 0;
	public static final int ERR_NVMOUSEEXT_ACCESSEX_X	= 1;
	public static final int ERR_NVMOUSEEXT_ACCESSEX_Y	= 2;
	public static final int ERR_NVMOUSEEXT_NOFIELD_X	= 3;
	public static final int ERR_NVMOUSEEXT_NOFIELD_Y	= 4;
	
	private static Method mInputManager_setCursorVisibility;
	private static int nMotionEvent_AXIS_RELATIVE_X	= 0;
	private static int nMotionEvent_AXIS_RELATIVE_Y	= 0;
	
	static
	{
		try { mInputManager_setCursorVisibility = InputManager.class.getMethod("setCursorVisibility", boolean.class);	}
		catch (NoSuchMethodException ex) { throw new NVMouseExtensionException(ERR_NVMOUSEEXT_NOMETHOD);	}
		
		try
		{
			Field fieldMotionEvent_AXIS_RELATIVE_X = MotionEvent.class.getField("AXIS_RELATIVE_X");
			try { nMotionEvent_AXIS_RELATIVE_X = (Integer) fieldMotionEvent_AXIS_RELATIVE_X.get(null);	}
			catch (IllegalAccessException iae) { throw new NVMouseExtensionException(ERR_NVMOUSEEXT_ACCESSEX_X);	}
		}
		catch (NoSuchFieldException ex) { throw new NVMouseExtensionException(ERR_NVMOUSEEXT_NOFIELD_X);	}

		try
		{
			Field fieldMotionEvent_AXIS_RELATIVE_Y = MotionEvent.class.getField("AXIS_RELATIVE_Y");
			try { nMotionEvent_AXIS_RELATIVE_Y = (Integer) fieldMotionEvent_AXIS_RELATIVE_Y.get(null);	}
			catch (IllegalAccessException iae) { throw new NVMouseExtensionException(ERR_NVMOUSEEXT_ACCESSEX_Y);	}
		}
		catch (NoSuchFieldException ex) { throw new NVMouseExtensionException(ERR_NVMOUSEEXT_NOFIELD_Y);	}
	}
	
	public static void checkAvailable () { /* force initialization above */ };
	

	public static boolean setCursorVisibility(InputManager im, boolean fVisibility)
	{
		try
		{
			mInputManager_setCursorVisibility.invoke(im, fVisibility);
		}
		catch (InvocationTargetException ite)	{ return false; }
		catch (IllegalAccessException iae)		{ return false; }

		return true;
	}
	
	public static int getAxisRelativeX()	{ return (nMotionEvent_AXIS_RELATIVE_X);	};
	public static int getAxisRelativeY()	{ return (nMotionEvent_AXIS_RELATIVE_Y);	};
}

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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.seleuco.mame4droid.input.ControlCustomizer;

public class DialogHelper {
	
	public static int savedDialog = DialogHelper.DIALOG_NONE;

	public final static int DIALOG_NONE = -1;
	public final static int DIALOG_EXIT = 1;
	public final static int DIALOG_ERROR_WRITING = 2;
	public final static int DIALOG_INFO = 3;
	public final static int DIALOG_EXIT_GAME = 4;
	public final static int DIALOG_OPTIONS = 5;
	public final static int DIALOG_FULLSCREEN = 7;
	public final static int DIALOG_LOAD_FILE_EXPLORER = 8;
	public final static int DIALOG_ROMs_DIR = 9;
	public final static int DIALOG_FINISH_CUSTOM_LAYOUT = 10;
	public final static int DIALOG_EMU_RESTART = 11;
	public final static int DIALOG_NO_PERMISSIONS = 12;
	public final static int DIALOG_NEW_ROMs_DIR = 13;
	
	protected MAME4droid mm = null;
	
	static protected String errorMsg;
	static protected String infoMsg;
	
	public void setErrorMsg(String errorMsg) {
		DialogHelper.errorMsg = errorMsg;
	}

	public void setInfoMsg(String infoMsg) {
		DialogHelper.infoMsg = infoMsg;
	}
		
	public DialogHelper(MAME4droid value){
		mm = value;
	}
	
	public Dialog createDialog(int id) {
		
		if(id==DialogHelper.DIALOG_LOAD_FILE_EXPLORER)
		{	
		   return mm.getFileExplore().create();
		}	
		
	    Dialog dialog;
	    AlertDialog.Builder builder = new AlertDialog.Builder(mm);
	    switch(id) {
	    case DIALOG_FINISH_CUSTOM_LAYOUT:
	    	
	    	builder.setMessage("Do you want to save changes?")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   DialogHelper.savedDialog = DIALOG_NONE;  
	    	        	   mm.removeDialog(DIALOG_FINISH_CUSTOM_LAYOUT);
	    				   ControlCustomizer.setEnabled(false);
	    				   mm.getInputHandler().getControlCustomizer().saveDefinedControlLayout();
	    				   mm.getEmuView().setVisibility(View.VISIBLE);
	    				   mm.getEmuView().requestFocus();
	    				   Emulator.resume();
	    				   mm.getInputView().invalidate();	    				   
	    	           }
	    	       })
	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   DialogHelper.savedDialog = DIALOG_NONE;  
	    	        	   mm.removeDialog(DIALOG_FINISH_CUSTOM_LAYOUT);
	    				   ControlCustomizer.setEnabled(false);
	    				   mm.getInputHandler().getControlCustomizer().discardDefinedControlLayout();
	    				   mm.getEmuView().setVisibility(View.VISIBLE);
	    				   mm.getEmuView().requestFocus();
	    				   Emulator.resume();
	    				   mm.getInputView().invalidate();
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;       
	    case DIALOG_ROMs_DIR:
	    	
	    	builder.setMessage("Do you want to use default ROMs path? (recomended)")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   DialogHelper.savedDialog = DIALOG_NONE;
	    	        	   mm.removeDialog(DIALOG_ROMs_DIR);
						   mm.getMainHelper().setInstallationDirType(MainHelper.INSTALLATION_DIR_OLD);
	    	        	   if(mm.getMainHelper().ensureInstallationDIR(mm.getMainHelper().getInstallationDIR()))
	    	        	   {	    	
	    	        		  mm.getPrefsHelper().setROMsDIR("");
	    	        	      mm.runMAME4droid();
	    	        	   }	    	        	   
	    	           }
	    	       })
	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   DialogHelper.savedDialog = DIALOG_NONE;	    	        	   
	    	        	   mm.removeDialog(DIALOG_ROMs_DIR);
	    	               mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    case DIALOG_NEW_ROMs_DIR:

			builder.setMessage(
					"Since Android 10 Google has added scoped storage."+
					"That means applications can no longer read anywhere on the SD card, only in their own directories.\n\n"+
					"I have used a compatibility flag that allows applications to work as they did before, but it can stop working at any time.\n\n" +
					"That is why now the application will install files and create the roms directory within '/sdcard/Android/data/com.seleuco.mame4droid/files' instead old '/sdcard/MAME4droid/' path.\n\n"+
					"This has the disadvantage that when you uninstall the application, all files, such as roms or save states, will be deleted.The good, I no longer need permission to read from your SD.\n\n"+
					"However, as long as it works, you can continue to use the old way.\n\n"+
					"which one do you want?"
			)
					.setCancelable(false)
					.setPositiveButton("New Way (Recommended)", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							DialogHelper.savedDialog = DIALOG_NONE;
							mm.removeDialog(DIALOG_NEW_ROMs_DIR);
							mm.getMainHelper().setInstallationDirType(MainHelper.INSTALLATION_DIR_NEW);
							if(mm.getMainHelper().ensureInstallationDIR(mm.getMainHelper().getInstallationDIR()))
							{
								mm.getPrefsHelper().setROMsDIR("");
								mm.runMAME4droid();
							}
						}
						})
					.setNegativeButton("Old Way", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								DialogHelper.savedDialog = DIALOG_NONE;
								mm.removeDialog(DIALOG_NEW_ROMs_DIR);
								mm.getPrefsHelper().setOldInstallation(true);
								mm.recreate();
							}
						});
			dialog = builder.create();
			break;
	    case DIALOG_EXIT:
	    	
	    	builder.setMessage("Are you sure you want to exit?")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                //System.exit(0);
						   if(android.os.Build.VERSION.SDK_INT >= 21) {
							   mm.finishAndRemoveTask();
						   }
						   else
						   	   mm.finish();
						   android.os.Process.killProcess(android.os.Process.myPid());
	    	           }
	    	       })
	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	     Emulator.resume();
	    	        	     DialogHelper.savedDialog = DIALOG_NONE;
	    	        	     mm.removeDialog(DIALOG_EXIT);
	    	                //dialog.cancel();	    	                
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    case DIALOG_ERROR_WRITING:
	    	builder.setMessage("Error")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                //System.exit(0);
	    	               DialogHelper.savedDialog = DIALOG_NONE;
	    	               mm.removeDialog(DIALOG_ERROR_WRITING);
	    	               mm.getMainHelper().restartApp();
	    	        	   //mm.showDialog(DialogHelper.DIALOG_LOAD_FILE_EXPLORER);
	    	           }
	    	       });

	    	 dialog = builder.create();
	         break;
	    case DIALOG_INFO:
	    	builder.setMessage("Info")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                DialogHelper.savedDialog = DIALOG_NONE;
	    	                Emulator.resume();
	    	                mm.removeDialog(DIALOG_INFO);
	    	           }
	    	       });

	    	 dialog = builder.create();
	         break;
	    case DIALOG_EXIT_GAME:	    	
	    	builder.setMessage("Are you sure you want to exit game?")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                DialogHelper.savedDialog = DIALOG_NONE;
	    	                Emulator.resume();
	    		        	Emulator.setValue(Emulator.EXIT_GAME_KEY, 1);		    	
	    			    	try {
	    						Thread.sleep(100);
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
	    					Emulator.setValue(Emulator.EXIT_GAME_KEY, 0);	    	                
	    					mm.removeDialog(DIALOG_EXIT_GAME);
	    	           }
	    	       })
	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   Emulator.resume();
	    	        	   DialogHelper.savedDialog = DIALOG_NONE;
	    	        	   mm.removeDialog(DIALOG_EXIT_GAME);
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    case DIALOG_OPTIONS:
	    case DIALOG_FULLSCREEN:	
	    	final CharSequence[] items1 = {"Load State", "Save State","Help","Settings", "Netplay" /*"Support",*/};	    	
	    	final CharSequence[] items2 = {"Help","Settings", "Netplay"/*"Support"*/};	
	    	final CharSequence[] items3 = {"Exit","Load State", "Save State","Help","Settings", "Netplay"/*"Support",*/};	    	
	    	final CharSequence[] items4 = {"Exit","Help","Settings", "Netplay" /*"Support"*/};	
	    	
	    	final int a = id == DIALOG_FULLSCREEN ? 0 : 1;
	    	final int b = Emulator.isInMAME() ? 0 : 2;
	    	
	    	if(a == 1)
	    	   builder.setTitle("Choose an option from the menu.");
	    	
	    	builder.setCancelable(true);
	    	builder.setItems(Emulator.isInMAME() ? (id==DIALOG_OPTIONS?items1:items3) : (id==DIALOG_OPTIONS?items2:items4), new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	
    	          if(item == 0 && a==0)
    	          {
					if(Emulator.isInMenu())
					{
		    		    Emulator.setValue(Emulator.EXIT_GAME_KEY, 1);
		    		    Emulator.resume();
	    			    try {Thread.sleep(100);} catch (InterruptedException e) {}
	    			    Emulator.setValue(Emulator.EXIT_GAME_KEY, 0);
					}										 
					else if(!Emulator.isInMAME())
					    mm.showDialog(DialogHelper.DIALOG_EXIT);
				    else
				    	mm.showDialog(DialogHelper.DIALOG_EXIT_GAME);
    	          } else if (item == 1-a &&  b==0){ Emulator.setValue(Emulator.LOADSTATE, 1);Emulator.resume();
    	          } else if (item == 2-a &&  b==0){ Emulator.setValue(Emulator.SAVESTATE, 1);Emulator.resume();		    	        
    	          } else if (item == 3-a-b){ mm.getMainHelper().showHelp();
    	          } else if (item == 4-a-b){ mm.getMainHelper().showSettings();
    	          } else if (item == 5-a-b){
    	        	  //mm.showDialog(DialogHelper.DIALOG_THANKS);
    	              //mm.getNetPlay().showView();
    	        	  mm.getNetPlay().createDialog();
    	              
    	          }
		    	    
  	        	  DialogHelper.savedDialog = DIALOG_NONE;
  	        	  mm.removeDialog(DIALOG_OPTIONS);
  	        	  mm.removeDialog(DIALOG_FULLSCREEN);
	    	    }	    	 
	    	});
	    	builder.setOnCancelListener(new  DialogInterface.OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
  	        	    DialogHelper.savedDialog = DIALOG_NONE;
  	        	    Emulator.resume();
  	        	    if(a!=0)
  	        	       mm.removeDialog(DIALOG_OPTIONS);
  	        	    else
  	        	       mm.removeDialog(DIALOG_FULLSCREEN);
				}
			});
	    	dialog = builder.create();
	        break;
	    case DIALOG_EMU_RESTART: 	
	    	builder.setTitle("Restart needed!")
	    	       .setMessage("MAME4droid needs to restart for the changes to take effect.")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
                          mm.getMainHelper().restartApp(); 
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;	        
	    case DIALOG_NO_PERMISSIONS: 	
	    	builder.setTitle("No permissions!")
	    	       .setMessage("You don't have permission to read from external storage. Please, allow storage permission on Android applications settings.")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   android.os.Process.killProcess(android.os.Process.myPid());
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;	        
	    default:
	        dialog = null;
	    }
	    /*
	    if(dialog!=null)
	    {
	    	dialog.setCanceledOnTouchOutside(false);
	    }*/
	    return dialog;

	}

	public void prepareDialog(int id, Dialog dialog) {
		
		if(id==DIALOG_ERROR_WRITING)
		{
			((AlertDialog)dialog).setMessage(errorMsg);
			DialogHelper.savedDialog = DIALOG_ERROR_WRITING;
		}
		else if(id==DIALOG_INFO)
		{
			((AlertDialog)dialog).setMessage(infoMsg);
	    	Emulator.pause();
	        DialogHelper.savedDialog = DIALOG_INFO;
		}
	    else if(id==DIALOG_EXIT)
		{
	    	Emulator.pause();
	        DialogHelper.savedDialog = DIALOG_EXIT;
		}		
	    else if(id==DIALOG_EXIT_GAME)
		{
	    	Emulator.pause();
	        DialogHelper.savedDialog = DIALOG_EXIT_GAME;
		}
	    else if(id==DIALOG_OPTIONS)
		{
	    	Emulator.pause();
	    	DialogHelper.savedDialog = DIALOG_OPTIONS;
		}
	    else if(id==DIALOG_FULLSCREEN)
		{
	    	Emulator.pause();
	    	DialogHelper.savedDialog = DIALOG_FULLSCREEN;
		}
	    else if(id==DIALOG_ROMs_DIR)
		{
	    	DialogHelper.savedDialog = DIALOG_ROMs_DIR;
		}
		else if(id==DIALOG_NEW_ROMs_DIR)
		{
			DialogHelper.savedDialog = DIALOG_NEW_ROMs_DIR;
		}
	    else if(id==DIALOG_LOAD_FILE_EXPLORER)
		{
	    	DialogHelper.savedDialog = DIALOG_LOAD_FILE_EXPLORER;
		}
	    else if(id==DIALOG_FINISH_CUSTOM_LAYOUT)
		{
	    	DialogHelper.savedDialog = DIALOG_FINISH_CUSTOM_LAYOUT;
		}
	    else if(id==DIALOG_EMU_RESTART)
		{
	    	Emulator.pause();
		}		    		
	    else if(id==DIALOG_NO_PERMISSIONS)
		{
	    	DialogHelper.savedDialog = DIALOG_NO_PERMISSIONS;
		}		
		
	}
        
	public void removeDialogs() {
		if(savedDialog==DIALOG_FINISH_CUSTOM_LAYOUT)
		{
		    mm.removeDialog(DIALOG_FINISH_CUSTOM_LAYOUT);
			DialogHelper.savedDialog = DIALOG_NONE;  
		}
	}
	
	public void showMessage(String message, DialogInterface.OnClickListener okListener) {
	    new AlertDialog.Builder(mm)
	            .setMessage(message)
	            .setCancelable(false)
	            .setPositiveButton("OK", okListener)
	            .create()
	            .show();
	}
	
}

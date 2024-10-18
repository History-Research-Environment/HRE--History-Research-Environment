package hre.bila;
/***************************************************************************
 * Help - Specification 06.14 Help 2019-11-29
 * v0.00.0015 2019-12-07 initial implementation of Oracle Help (D Ferguson)
 * v0.01.0025 2021-01-29 fix file separator error (D Ferguson)
 * v0.01.0027 2022-09-04 Added English(UK) support (D Ferguson)
 ***************************************************************************/

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import hre.gui.HGlobal;
import oracle.help.CSHManager;
import oracle.help.Help;
import oracle.help.library.Book;
import oracle.help.library.helpset.HelpSet;

/**
 * HRE Help system driver
 * @author D Ferguson
 * @version v0.01.0025
 * @since 2019-12-07
 */

public class HB0614Help {
	// This method uses Oracle Help for Java to access HRE helpsets and indexes, etc
	// It relies on 5 Oracle Help provided JAR files which must be in the classpath
	// For (poor) documentation, see the Guide provided with the OHJ JDK v12.2.1

	private static Help hreHelp;
	private static Book hreBook;
	private static CSHManager contextManager;
	private static String helpsetPath;

/**
 * Build the Help book from the help files on the helpsetPath
 */
	@SuppressWarnings("deprecation")
	// NOTE on deprecated method of creating hreBook:
	// The helpsetPath contains the full path to the hre.hs file.
	// The final URL passed to HelpSet needs to look like (in Windows)
	// the string: file:/C:/Program Files/HRE/app/Help/en/hre.hs
	//
	// But the constructor URL(String) is now deprecated.
	// However, the replacement method requires working from a path to a URI to the URL.
	// However, this converts any blanks in the path (as in "Program Files") to %20
	// which the Oracle Help code cannot handle.
	// Alternatively, if we attempt to use the other HelpSet constructor as described in
	// https://download.oracle.com/otn_hosted_doc/jdeveloper/1012/ohguide/ohjpg_add.html
	// by using code like:
	//     Class bookClass = MethodHandles.lookup().lookupClass();
	//     URL forHelpSet = bookClass.getResource("HB0614Help.class");
	//     hreBook = new HelpSet(forHelpSet, "hre.js");
	// then this also includes the %20 instead of blank.

	public static void setupHelp() {
		// Build full path to helpset file - use 'en' suffix if nativeLanguage is actually 'gb'
		String langCode = HGlobal.nativeLanguage;
		if (langCode.equals("gb")) langCode = "en";
		helpsetPath = HGlobal.helpPath + langCode + File.separator + "hre.hs";

       try {
    	  hreBook = new HelpSet(new URL(helpsetPath));	// deprecated
	    }
	    catch (Exception e)  {
	    	JOptionPane.showMessageDialog(null, e.getMessage() + " View Stack Trace", "HelpSet setup error", JOptionPane.ERROR_MESSAGE);
	    	e.printStackTrace();
	    }

        // Create the Help Object
	    hreHelp = new Help(false, false);			// set to not combine books, default label info
		hreHelp.addBook(hreBook);
		}

/**
 * Show the full Help Navigator window
 * @param winInst is the Window Instance of the calling method
 */
	public static void fullHelp(Window winInst)
	{
		Help.registerClientWindow(winInst);  		// register the window to set modality correctly
	    hreHelp.showNavigatorWindow();				// displays the Help Navigator window
	    Help.unregisterClientWindow(winInst);
	}

/**
 * Context-sensitive Help - show only the requested Help Topic
 * @param winInst 		is the Window Instance of the calling method
 * @param helpCaller 	is the component initiating the context-sensitive help call
 * @param topicID 		is the target of the context-sensitive topic to be displayed
 */
	public static void contextHelp(Window winInst, Component helpCaller, String topicID)
	{
		Help.registerClientWindow(winInst); 		// register the window to set modality correctly
		contextManager = new CSHManager(hreHelp);
	    contextManager.addComponent(helpCaller, hreBook, topicID);
	    contextManager.showHelpForComponent(helpCaller);	// displays only the required help topic
	    Help.unregisterClientWindow(winInst);
	    }

}  // End of HG0514

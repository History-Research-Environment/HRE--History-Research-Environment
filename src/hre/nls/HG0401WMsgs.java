package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0401WMsgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0401W-en"; //$NON-NLS-1$
	public static String Text_0;			// Window title
	public static String Text_1;			// email tooltip
	public static String Text_11;			// name prompt
	public static String Text_12;			// email prompt
	public static String Text_2;			// name tooltip
	public static String Text_21;			// name re-enter prompt
	public static String Text_22;			// your name
	public static String Text_23;			// email re-enter prompt
	public static String Text_24;			// your email
	public static String Text_25;			// project msg 1
	public static String Text_26;			// project msg 2
	public static String Text_27;			// select
	public static String Text_28;			// close
	public static String Text_29;			// error msg
	public static String Text_30;			// error pane title
	public static String Text_31;			// error msg
	public static String Text_32;			// unknown
	public static String Text_4;			// HRE heading
	public static String Text_5;			// build
	public static String Text_6;			// release
	public static String Text_7;			// save
	public static String Text_8;			// do not show again
	public static String Text_9;			// initial new user prompt
	
	
	public HG0401WMsgs(String guiLanguage) {		
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0401W-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0401WMsgs.class);
	}
}

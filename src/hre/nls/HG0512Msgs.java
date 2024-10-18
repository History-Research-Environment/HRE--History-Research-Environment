package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0512Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0512-en"; //$NON-NLS-1$

	public static String Text_0;
	public static String Text_1;
	public static String Text_2;	
	public static String Text_3;
	public static String Text_4;
	public static String Text_5;
	public static String Text_6;
	public static String Text_7;
	public static String Text_8;
	public static String Text_9;
	
	public static String Text_10;
	public static String Text_11;

	public static String Text_13;
	public static String Text_14;
	public static String Text_15;
	public static String Text_16;
	public static String Text_17;
	public static String Text_18;

	public static String Text_20;

	public HG0512Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0512-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0512Msgs.class);			
	}

}
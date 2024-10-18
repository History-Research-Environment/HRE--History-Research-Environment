package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0540Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0540-en"; //$NON-NLS-1$
	public static String Text_0;
	public static String Text_5;
	public static String Text_9;
	public static String Text_12;
	public static String Text_15;
	public static String Text_18;
	public static String Text_21;
	public static String Text_27;
	public static String Text_28;
	public static String Text_30;
	public static String Text_31;
	public static String Text_33;
	public static String Text_34;

	public HG0540Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0540-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0540Msgs.class);	
	}
}

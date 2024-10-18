package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0575Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0575-en"; //$NON-NLS-1$
	public static String Text_6;
	public static String Text_7;
	public static String Text_12;
	public static String Text_17;
	public static String Text_21;
	public static String Text_27;
	public static String Text_28;
	public static String Text_29;
	public static String Text_31;
	public static String Text_32;
	public static String Text_35;
	public static String Text_36;
	public static String Text_41;
	public static String Text_42;

	public HG0575Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0575-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0575Msgs.class);
	}

}

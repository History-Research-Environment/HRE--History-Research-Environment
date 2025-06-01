package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0620Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0620-en"; //$NON-NLS-1$

	public static String Text_4;
	public static String Text_9;
	public static String Text_10;
	public static String Text_13;
	public static String Text_15;
	public static String Text_19;
	public static String Text_20;
	public static String Text_22;
	public static String Text_26;

	public HG0620Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0620-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0620Msgs.class);
	}
}

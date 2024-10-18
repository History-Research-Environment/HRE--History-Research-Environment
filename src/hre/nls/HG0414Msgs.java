package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0414Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0414-en"; //$NON-NLS-1$
	public static String Text_11;
	public static String Text_12;
	public static String Text_3;
	public static String Text_9;
	
	public HG0414Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0414-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0414Msgs.class);
	}
}

package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0416Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0416-en"; //$NON-NLS-1$
	public static String Text_10;
	public static String Text_2;
	public static String Text_6;
	public static String Text_9;
	
	
	public HG0416Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0416-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0416Msgs.class);
	}

}

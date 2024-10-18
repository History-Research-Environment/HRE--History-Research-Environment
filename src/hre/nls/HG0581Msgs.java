package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0581Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0581-en"; //$NON-NLS-1$
	public static String Text_3;
	public static String Text_7;

	public static String Text_12;
	public static String Text_14;
	
	public HG0581Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0581-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0581Msgs.class);					
	}
}

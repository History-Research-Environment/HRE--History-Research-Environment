package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0450Msgs extends NLS {
	private static String BUNDLE_NAME =  "hre.nls.HG0450-en"; //$NON-NLS-1$
	
	public static String Text_0;
	public static String Text_1;
	public static String Text_2;
	public static String Text_3;
	
	public HG0450Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0450-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0450Msgs.class);
	}

}

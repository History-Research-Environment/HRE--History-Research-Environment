package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0577Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0577-en"; //$NON-NLS-1$
	public static String Text_0;
	public static String Text_10;
	public static String Text_12;
	public static String Text_2;
	
	
	public HG0577Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0577-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0577Msgs.class);
	}

}

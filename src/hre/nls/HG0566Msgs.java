package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0566Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0566-en"; //$NON-NLS-1$



	public HG0566Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0566-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0566Msgs.class);
	}

}

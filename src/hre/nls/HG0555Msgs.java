package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0555Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0555-en"; //$NON-NLS-1$



	public HG0555Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0555-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0555Msgs.class);
	}

}

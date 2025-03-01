package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HG0565Msgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HG0565-en"; //$NON-NLS-1$



	public HG0565Msgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HG0565-" + guiLanguage;  //$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HG0565Msgs.class);
	}

}

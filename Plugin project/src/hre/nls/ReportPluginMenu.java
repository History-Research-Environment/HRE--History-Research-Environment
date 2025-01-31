package hre.nls;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ReportPluginMenu {
	private static String BUNDLE_NAME = "hre.nls.ReportPluginMenu-no"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public ReportPluginMenu(String nationalLanguage) {
		if (nationalLanguage.equals("en")) BUNDLE_NAME = "hre.nls.ReportPluginMenu-en";
		if (nationalLanguage.equals("no")) BUNDLE_NAME = "hre.nls.ReportPluginMenu-no";
		if (nationalLanguage.equals("de")) BUNDLE_NAME = "hre.nls.ReportPluginMenu-de";
		if (nationalLanguage.equals("fr")) BUNDLE_NAME = "hre.nls.ReportPluginMenu-fr";
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}

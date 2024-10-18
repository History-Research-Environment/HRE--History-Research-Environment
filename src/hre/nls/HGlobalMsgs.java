package hre.nls;

import org.eclipse.osgi.util.NLS;

public class HGlobalMsgs extends NLS {
	private static String BUNDLE_NAME = "hre.nls.HGlobal-en"; //$NON-NLS-1$
	
	public static String Text_52;
	public static String Text_53;
	public static String Text_54;
	public static String Text_55;
	public static String Text_56;
	public static String Text_57;
	public static String Text_58;
	public static String Text_67;
	public static String Text_68;
	public static String Text_71;
	public static String Text_73;
	public static String Text_75;
	public static String Text_77;
	public static String Text_79;
	public static String Text_81;
	public static String Text_83;
	public static String Text_85;
	public static String Text_91;
	public static String Text_92;
	public static String Text_93;
	public static String Text_94;
	public static String Text_96;
	public static String Text_97;
	public static String Text_98;
	public static String Text_99;
	public static String Text_100;
	public static String Text_102;
	public static String Text_103;
	public static String Text_104;	
	
	public HGlobalMsgs(String guiLanguage) {
		// set the Bundle Name to the requested language
		BUNDLE_NAME = "hre.nls.HGlobal-" + guiLanguage;	//$NON-NLS-1$
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HGlobalMsgs.class);
	}

}

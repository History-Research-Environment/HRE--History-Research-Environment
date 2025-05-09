package hre.pf4j.ext;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import hre.bila.HBBusinessLayer;
import hre.gui.HG0401HREMain;

@Extension
public interface ReportMenuExtensionPoint extends ExtensionPoint {

    String className();
    void buildMenu(JMenu menuItem);
	void setMainPane(JDesktopPane mainPane);
	void setBusinessLayerPointer(HBBusinessLayer[] pointBusinessLayers);
	void setMainPointer(HG0401HREMain mainFrame);
	void setNationalLanguage(String nativeLanguage);
	void setDDLversion(String dbaseVersion);
	void setDEBUG(boolean DEBUG);

}

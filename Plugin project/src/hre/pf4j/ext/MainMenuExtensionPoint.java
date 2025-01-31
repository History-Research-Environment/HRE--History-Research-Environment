package hre.pf4j.ext;
import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import hre.gui.HG0401HREMain;

@Extension
public interface MainMenuExtensionPoint extends ExtensionPoint {

    String className();
	void buildMenuBar(JMenuBar menuBar);
	void setMainPane(JDesktopPane mainPane);
	void setMainPointer(HG0401HREMain mainFrame);
	void setNationalLanguage(String nativeLanguage);

}

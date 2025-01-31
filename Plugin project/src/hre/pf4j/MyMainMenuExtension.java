package hre.pf4j;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.pf4j.Extension;

import hre.gui.HG0401HREMain;
import hre.pf4j.ext.MainMenuExtensionPoint;

/**
 * 
 * @author NTo
 *
 */
@Extension
public class MyMainMenuExtension implements MainMenuExtensionPoint {
	private String nationalLanguage ="en";
    public void buildMenuBar(JMenuBar menuBar) {
        JMenu exampleMenu = new JMenu("Example");
        exampleMenu.add(new JMenuItem("Hello World"));
        menuBar.add(exampleMenu);
    }   
    
/**
 * String className()
 */
	@Override
	public String className() {
		return "My menu";
	}

/**
 * setMainPane(JDesktopPane mainPane)
 * @param mainPane pointer to main pnael in HRE
 */
	@Override
	public void setMainPane(JDesktopPane mainPane) {}
	
/**
 * setNationalLanguage(String nls)	
 * @param nls National Language select string
 */
	public void setNationalLanguage(String nls) {
		nationalLanguage = nls;
		System.out.println("  Plugin setting national language: " + nationalLanguage);
	}

	//@Override
	//public void setBusinessLayerPointer(HBBusinessLayer[] pointBusinessLayer) {}

	@Override
	public void setMainPointer(HG0401HREMain mainFrame) {}
}

package hre.pf4j;
/**
 * class FirstMainMenuExtension implements MainMenuExtensionPoint
 */
import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

import org.pf4j.Extension;

import hre.bila.HBBusinessLayer;
import hre.bila.HBProjectOpenData;
import hre.gui.HG0401HREMain;
import hre.pf4j.ext.MainMenuExtensionPoint;

/**
 * class FirstMainMenuExtension implements MainMenuExtensionPoint, ActionListener
 * @author NTo
 *
 */
@Extension(ordinal = 1)
public class MainMenuExtension implements MainMenuExtensionPoint, ActionListener {
	JDesktopPane mainPane;
	HG0401HREMain mainFrame;
	HBBusinessLayer[] pointBusinessLayer;
	JMenuItem plugInMenuItemA, plugInMenuItemB;
	private String nationalLanguage ="en";
	
/**
 * 	
 */
	@Override
	public void setMainPane(JDesktopPane mainPane) {
		this.mainPane = mainPane;
	}	
	
/**
 * setNationalLanguage(String nls)	
 * @param nls National Language select string
 */
	public void setNationalLanguage(String nls) {
		nationalLanguage = nls;
		System.out.println("  Plugin setting main national language: " + nationalLanguage);
	}
	
/**
 * 	String className()
 */
	public String className() {
		return getClass().getName();
	}
	
/**
 * void setBusinessLayerPointer(HBBusinessLayer[] pointBusinessLayer)	
 
	@Override
	public void setBusinessLayerPointer(HBBusinessLayer[] pointBusinessLayers) {
		this.pointBusinessLayer = pointBusinessLayers;	
	}
*/	
/**
 * void setMainPointer(HG0401HREMain mainFrame)
 */
	@Override
	public void setMainPointer(HG0401HREMain mainFrame) {
		this.mainFrame = mainFrame;		
	}
	
/**
 * void buildMenuBar(JMenuBar menuBar)
 */
    public void buildMenuBar(JMenuBar menuBar) {
        JMenu menu = new JMenu("  Project ++ ");
        plugInMenuItemA = new JMenuItem("  Open Project");
        plugInMenuItemA.addActionListener(this);
        menu.add(plugInMenuItemA);
        plugInMenuItemB = new JMenuItem("  Not implemented");
        plugInMenuItemB.addActionListener(this);
        menu.add(plugInMenuItemB);
        menuBar.add(menu);
    }

/**
 * void actionPerformed(ActionEvent actionEvent)
 */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == plugInMenuItemA) {
			String windowTitle = "Open Project Data - Plugin";
			//System.out.println(windowTitle);
			JDialog dialogWindow = createPluginWindow(windowTitle,100);
			JTextArea textArea = new JTextArea();
			dialogWindow.add(textArea);
			HBProjectOpenData selectedProject = mainFrame.getSelectedOpenProject();
			String[] projectData = selectedProject.getProjectData();
			textArea.append("  Selected Open Project data:" + "\n");
			textArea.append("  ---------------------------" + "\n");
			for (int i = 0; i < projectData.length; i++)
				textArea.append("  " + projectData[i] + "\n");
			dialogWindow.setVisible(true);
		}
		
		if (actionEvent.getSource() == plugInMenuItemB) {
			String windowTitle = "Not Implemented Plugin";
			//System.out.println(windowTitle);
			JDialog dialogWindow = createPluginWindow(windowTitle,50);
			JTextArea textArea = new JTextArea();
			textArea.append("  Not implemented!" + "\n");
			dialogWindow.add(textArea);
			dialogWindow.setVisible(true);
		}
    	
    }
    
 /**
  * JDialog createPluginWindow(String windowTitle)   
  * @param windowTitle
  * @return
  */
	private JDialog createPluginWindow(String windowTitle, int vert) {
		// Create and show a dialog with the menu bar.
		JDialog dialog = new JDialog();
		JMenuBar dialogMenu = new JMenuBar();
		dialog.setTitle(windowTitle);
		dialog.setSize(450,300);
		dialog.setJMenuBar(dialogMenu);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		Point xymainPane = new Point(50,50);
		if (mainPane != null) xymainPane = mainPane.getLocationOnScreen();     		
		dialog.setLocation(xymainPane.x + 20, xymainPane.y + vert);  
				
		return dialog;
	}
} // End FirstMainMenuExtension

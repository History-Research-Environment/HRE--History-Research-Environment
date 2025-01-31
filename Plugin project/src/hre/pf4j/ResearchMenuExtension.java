package hre.pf4j;

/**
 * SecondMainMenuExtension implements MainMenuExtensionPoint
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

import org.pf4j.Extension;

import hre.bila.HBBusinessLayer;
import hre.gui.HG0401HREMain;
import hre.pf4j.ext.ResearchMenuExtensionPoint;


@Extension(ordinal = 2)
public class ResearchMenuExtension implements ActionListener, ResearchMenuExtensionPoint {
	JDesktopPane mainPane;
	HG0401HREMain mainFrame;
	HBBusinessLayer[] pointBusinessLayer;
	JMenuItem plugInMenuItemA, plugInMenuItemB;
	private String nationalLanguage ="en";
	
/**
 * 	void setMainPane(JDesktopPane mainPane)
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
		System.out.println("  Plugin setting research national language: " + nationalLanguage);
	}
	
/**
 * 	String className()
 */
	public String className() {
		return getClass().getName();
	}
	
/**
 * void setBusinessLayerPointer(HBBusinessLayer[] pointBusLayer)	

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
	@Override	
	public void buildMenu(JMenu researchItem) {
        JMenu menu = new JMenu("Research Plugin ++");
        plugInMenuItemA = new JMenuItem("Research A");
        menu.add(plugInMenuItemA);
        plugInMenuItemA.addActionListener(this);
        plugInMenuItemB = new JMenuItem("Research B");
        menu.add(plugInMenuItemB);
        plugInMenuItemB.addActionListener(this);
        researchItem.add(menu); 
    }
	
/**
 * 	void actionPerformed(ActionEvent actionEvent) 
 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == plugInMenuItemA) {
			String windowTitle = "Research plugin A";
			//System.out.println(windowTitle);
			createPluginWindow(windowTitle,50);
		}
		
		if (actionEvent.getSource() == plugInMenuItemB) {
			String windowTitle = "Research plugin B";
			//System.out.println(windowTitle);
			createPluginWindow(windowTitle,50);
		}		
	}

/**
 * JDialog createPluginWindow(String windowTitle)
 * @param windowTitle
 * @return
 */
	private JDialog createPluginWindow(String windowTitle,int vert) {
		// Create and show a dialog with the menu bar.
		JMenuBar dialogMenu = new JMenuBar();
		JDialog dialog = new JDialog();
		dialog.setTitle(windowTitle);
		dialog.setSize(450,300);
		dialog.setJMenuBar(dialogMenu);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		Point xymainPane = mainPane.getLocationOnScreen();     		
		dialog.setLocation(xymainPane.x + 40, xymainPane.y + vert);  
		dialog.setVisible(true);
		return dialog;
	}


} // End SecondMainMenuExtension

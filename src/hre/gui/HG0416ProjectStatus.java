package hre.gui;
/**********************************************************************************
 * Project Status - Specification 04.16
 * v0.00.0021 2020-04-20 initial draft (D Ferguson)
 * v0.00.0022 2020-05-25 close panel on mouse exit (D Ferguson)
 * v0.01.0025 2020-11-18 update Main Status Bar (D Ferguson)
 * 			  2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * v0.01.0027 2022-05-21 clean out Person Recents list if project chnages (D Ferguson)
 * v0.03.0031 2024-10-01 Organize imports (D Ferguson)
 * v0.04.0032 2026-01-03 Logged catch block actions (D Ferguson)
 **********************************************************************************/

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.nls.HG0416Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Status display/change
 * @author D Ferguson
 * @version v0.03.0032
 * @since 2020-04-20
 */

public class HG0416ProjectStatus extends JDialog {

	private static final long serialVersionUID = 001L;
	private static JPanel contents;
	private String selectedProject = ""; //$NON-NLS-1$

/**
 * Create the Dialog.
 */
	public HG0416ProjectStatus() {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));  //$NON-NLS-1$
		setTitle(HG0416Msgs.Text_2);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Heading with default text
		JLabel lbl_Heading = new JLabel(HG0416Msgs.Text_6);		// There are no Projects open at the moment
		contents.add(lbl_Heading, "cell 0 0, wrap"); //$NON-NLS-1$

		// Button group for project radio buttons
		ButtonGroup buttonGroup = new ButtonGroup();

/**
* Create Action Listeners
**/
		// If mouse exits the panel, then close it
		contents.addMouseListener(new MouseAdapter() {
		    public void mouseExited(MouseEvent e) {
		    	// if mouse is over any contained component DON'T exit
		    	if(!contents.contains(e.getPoint())) {
		    		HGlobal.statusWindowOpen = false;
					dispose();
		        	}
		    }
    	});

		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	HGlobal.statusWindowOpen = false;
				dispose();
		    }
		});

		// Toggle button listener - reset selected (Active) Project when user toggles a button
		ActionListener listener = actionEvent -> {
			// Get the clicked project name
        		selectedProject = actionEvent.getActionCommand();
        	// Reset the current project
        		HG0401HREMain.mainFrame.setStatusProject(selectedProject);
        	// Clean out the Persons Recently Used list
        		HG0401HREMain.mainFrame.removeRecents();
        	// Record the selectedProject in the Status bar
	            try {
	            	HG0401HREMain.mainFrame.setSelectedOpenProject(HGlobalCode.pointOpenProjectByName(selectedProject));
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0416 error resetting project " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
		};

/*****************
 * INITIALISE CODE
 ****************/
      // if no open projects, we're done
        if (HGlobal.numOpenProjects == 0) pack();
      // if Projects are open, display their names in JRadioButtons
      // with current selectedProject's button set as Active
		if (HGlobal.numOpenProjects > 0) {
			if (HGlobal.numOpenProjects == 1)
					lbl_Heading.setText(HG0416Msgs.Text_9);		// There is only one Project open:
				else lbl_Heading.setText(HG0416Msgs.Text_10);	// Click to change the Active Project
		    for (int i=0; i<HGlobal.openProjects.size(); i++){
		        String projName = HGlobal.openProjects.get(i).getProjectData()[0];	// project name
		        JRadioButton button = new JRadioButton(projName);
		        selectedProject = HG0401HREMain.mainFrame.getSelectedOpenProject().getProjectName();
				if (projName.contentEquals(selectedProject)) {
							button.setSelected(true);
							button.setFocusPainted(true);
							}
					else {button.setSelected(false);
						  button.setFocusPainted(false);
					}
				button.addActionListener(listener);
		        buttonGroup.add(button);
		        contents.add(button, "wrap"); //$NON-NLS-1$
		    }
			pack();
		}

	}	// End HG0416 constructor

}	// End HG0416ProjectStatus

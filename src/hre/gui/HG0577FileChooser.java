package hre.gui;
/****************************************************************************
 * File Chooser - Specification 05.77 GUI_FileChooser 2019-04-13
 * v0.00.0007 2019-04-13 initial code (D Ferguson)
 * v0.00.0016 2019-12-14 increased with of window (N.Tolleshaug)
 * v0.00.0016 2019-12-20 implemented alternative constructor (N.Tolleshaug)
 * v0.01.0025 2021-01-29 fix file separator issue (D Ferguson)
 * v0.03.0031 2024-10-01 organize imports (D Ferguson)
 *****************************************************************************/

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import hre.nls.HG0577Msgs;

/**
 * File Chooser
 * @author D Ferguson
 * @version v0.01.0025
 * @since 2019-04-13
 */

public class HG0577FileChooser extends JDialog {

	private static final long serialVersionUID = 001L;
	private final JPanel contentPanel = new JPanel();
	private String userDir;

/**
 * Optional HG0577FileChooser constructor
 * @param String[] fileChooserParams
 */
	public HG0577FileChooser (String[] fileChooserParams) {
    	this (fileChooserParams[0],fileChooserParams[1],fileChooserParams[2],
    		fileChooserParams[3],fileChooserParams[4],Integer.parseInt(fileChooserParams[5]));
	}

/**
 * Default HG0577FileChooser constructor
 * @param dialogType	the type of Action required (Open, Save, Select, Delete)
 * @param fileType		the file filter literal
 * @param fileExt		the file filter extension (e.g .db, .hrep, .zip, etc)
 * @param fileName		if a default required (e.g, for New Project)
 * @param folderStart	folder to start search in (default to User Home directory)
 * @param mode			1=FILES_ONLY, 2=DIRECTORIES_ONLY
 */
	public HG0577FileChooser (String dialogType, String fileType, String fileExt, String fileName, String folderStart, int mode) {

		setResizable(false);
		// Clear filename and path fields
		HGlobal.chosenFilename = " "; //$NON-NLS-1$
    	HGlobal.chosenFolder = " "; //$NON-NLS-1$

		setTitle(HG0577Msgs.Text_2);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 700, 450);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// File chooser construct
		// Set start folder, if passed in parameter-5, else default to user's home directory, plus /hre/
		if (folderStart == null) { userDir = System.getProperty("user.home") + File.separator + "hre" + File.separator; } //$NON-NLS-1$ //$NON-NLS-2$
			else { userDir = folderStart; }
		JFileChooser fileChooser = new JFileChooser(userDir);
		fileChooser.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);   //$NON-NLS-1$
		// Set default filename, if passed as parameter-4
		if (fileName != null) fileChooser.setSelectedFile(new File(fileName));

		// set the fileChooser type based on parameter-1
		if (dialogType == "Open") fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);   //$NON-NLS-1$
		if (dialogType == "Save") fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);   //$NON-NLS-1$
		if (dialogType == "Select") { //$NON-NLS-1$
			fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
									 fileChooser.setApproveButtonText(HG0577Msgs.Text_0);
									 fileChooser.setApproveButtonToolTipText(HG0577Msgs.Text_10);
		}
		if (dialogType == "Delete") { //$NON-NLS-1$
			fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
									 fileChooser.setApproveButtonText(HG0577Msgs.Text_12);
		}

		fileChooser.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		// Set file types filter from parameter-2, 3
		FileNameExtensionFilter filter = new FileNameExtensionFilter(fileType, fileExt);
		fileChooser.setFileFilter(filter);

		// set file/folder mode from parameter-6
		if ( mode == 1) fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			else fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		class MyFileView extends FileView {
		      @Override
			public Icon getIcon(File f)
		      { FileSystemView view=FileSystemView.getFileSystemView();
		            return view.getSystemIcon(f);
		      } }
		fileChooser.setFileView(new MyFileView());	  	// ensures file icons are System type
		contentPanel.add(fileChooser);

		// LISTENER FOR FILECHOOSER ACTIONS
		fileChooser.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 001L;
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
					if (dialogType == "Open" & fileExt == "zip") {                             // special case of call from HG0406 //$NON-NLS-1$ //$NON-NLS-2$
				    	   HGlobal.fromExtFile = fileChooser.getSelectedFile().getName();      // get filename
				    	   HGlobal.fromExtFolder = fileChooser.getSelectedFile().getParent();  // get path
				    } else {HGlobal.chosenFilename = fileChooser.getSelectedFile().getName();    					// normal case - get filename
						  	  if (mode == 1) HGlobal.chosenFolder = fileChooser.getSelectedFile().getParent();  	// normal case - get path
						  	  	else HGlobal.chosenFolder = fileChooser.getSelectedFile().getPath();				// Directory case - get folder name
					}
		       }
		        else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
		        	if (fileExt == "zip") {         // special case of call from HG0406 //$NON-NLS-1$
				    	   HGlobal.fromExtFile = "";       //$NON-NLS-1$
				    	   HGlobal.fromExtFolder = "";   //$NON-NLS-1$
			    	    }
		        	else {  HGlobal.chosenFilename = "";	// normal case //$NON-NLS-1$
			    			HGlobal.chosenFolder = ""; //$NON-NLS-1$
		        		}
		        	}
				dispose(); }
		});

		// LISTENER FOR CLICKING 'X" ON MAIN SCREEN
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	if (fileExt == "zip") {                            // special case of call from HG0406 //$NON-NLS-1$
			    	   HGlobal.fromExtFile = "";       //$NON-NLS-1$
			    	   HGlobal.fromExtFolder = "";    //$NON-NLS-1$
			    	}
			    else { HGlobal.chosenFilename = "";    // normal case //$NON-NLS-1$
		    			HGlobal.chosenFolder = "";   //$NON-NLS-1$
		    		}
		    }
		});
	}
}

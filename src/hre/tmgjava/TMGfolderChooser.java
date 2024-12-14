package hre.tmgjava;
/****************************************************************************
 * File Chooser - Specification 05.77 GUI_FileChooser 2019-04-13
 * v0.00.0007 2019-04-13 initial HG0577FileChooser code (D Ferguson)
 * v0.00.0016 2019-12-14 increased with of window (N.Tolleshaug)
 *			  2019-12-20 implemented alternative constructor (N.Tolleshaug)
 * v0.01.0027 2022-01-30 set JFileChooser to fast start mode (D Ferguson)
 * v0.03.0031 2024-11-25 Convert "\\" usage to File.separator (D Ferguson)
 *****************************************************************************/

import java.awt.BorderLayout;
import java.awt.Font;
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

/**
 * File Chooser
 * @author D Ferguson
 * @version v0.01.0027
 * @since 2019-04-13
 */

public class TMGfolderChooser extends JDialog {

	private static final long serialVersionUID = 001L;
	private final JPanel contentPanel = new JPanel();
	private String userDir;

/**
 * Optional FileChooser constructor
 * @param String[] fileChooserParams
 * @wbp.parser.constructor
 */
	public TMGfolderChooser (String[] fileChooserParams) {
    	this (fileChooserParams[0],fileChooserParams[1],fileChooserParams[2],
    		  fileChooserParams[3],fileChooserParams[4],Integer.parseInt(fileChooserParams[5]));
	}

/**
 * Default FileChooser constructor
 * @param dialogType	the type of Action required (Open, Save, Select, Delete)
 * @param fileType		the file filter literal
 * @param fileExt		the file filter extension (e.g .db, .hrep, .zip, etc)
 * @param fileName		if a default required (e.g, for New Project)
 * @param folderStart	folder to start search in (default to User Home directory)
 * @param mode			1=FILES_ONLY, 2=DIRECTORIES_ONLY
 */
	public TMGfolderChooser (String dialogType, String fileType, String fileExt, String fileName, String folderStart, int mode) {

		setResizable(false);
		// Clear filename and path fields
		TMGglobal.chosenFilename = " ";
    	TMGglobal.chosenFolder = " ";

		setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
		setTitle("TMG Folder Chooser");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(0, 0, 750, 450);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// File chooser construct
		// Set start folder, if passed in parameter-5, else default to user's home directory, plus /hre/
		if (folderStart == null) { userDir = System.getProperty("user.home");
								   userDir = userDir + File.separator +"hre"+ File.separator;
								 }
			else userDir = folderStart;
		JFileChooser fileChooser = new JFileChooser(userDir);

		// Setup FileChooser in fast start mode
		fileChooser.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);

		// Set default filename, if passed as parameter-4
		if (fileName != null) fileChooser.setSelectedFile(new File(fileName));

		// set the fileChooser type based on parameter-1
		if (dialogType == "Open") fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		if (dialogType == "Save") fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		if (dialogType == "Select") {
				fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
				fileChooser.setApproveButtonText("Select");
				fileChooser.setApproveButtonToolTipText("Select database");
		}
		if (dialogType == "Delete") {
				fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
				fileChooser.setApproveButtonText("Delete");
		}

		fileChooser.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		fileChooser.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 15));

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
		fileChooser.setFileView(new MyFileView());	  			// ensures file icons are System type
		contentPanel.add(fileChooser);

		// LISTENER FOR FILECHOOSER ACTIONS
		fileChooser.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 001L;
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
					if (dialogType == "Open" & fileExt == "zip") {                             // special case of call from HG0406
				    	   TMGglobal.fromExtFile = fileChooser.getSelectedFile().getName();      // get filename
				    	   TMGglobal.fromExtFolder = fileChooser.getSelectedFile().getParent();  // get path
				    } else {TMGglobal.chosenFilename = fileChooser.getSelectedFile().getName();    					// normal case - get filename
						  	  if (mode == 1) TMGglobal.chosenFolder = fileChooser.getSelectedFile().getParent();  	// normal case - get path
						  	  	else TMGglobal.chosenFolder = fileChooser.getSelectedFile().getPath();				// Directory case - get folder name
					}
		       }
		        else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
		        	if (fileExt == "zip") {                             						      // special case of call from HG0406
				    	   TMGglobal.fromExtFile = "";
				    	   TMGglobal.fromExtFolder = "";
			    	    }
		        	else {  TMGglobal.chosenFilename = "";						                  // normal case
			    			TMGglobal.chosenFolder = "";
		        		}
		        	}
				dispose(); }
		});

		// LISTENER FOR CLICKING 'X" ON MAIN SCREEN
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	if (fileExt == "zip") {                            // special case of call from HG0406
			    	   TMGglobal.fromExtFile = "";
			    	   TMGglobal.fromExtFolder = "";
			    	}
			    else { TMGglobal.chosenFilename = "";         // normal case
		    			TMGglobal.chosenFolder = "";
		    		}
		    }
		});
	}
}

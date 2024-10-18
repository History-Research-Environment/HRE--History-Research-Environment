package hre.gui;
/***********************************************************************************
 * Project Delete code - Specification 04.10 GUI_ProjectDelete 2020-02-28
 * v0.00.0005 2019-02-22 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 fix close code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for userProjects being ArrayList (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0016 2019-12-12 added pointer to ProjectHandler in constructor (N. Tolleshaug)
 * v0.00.0016 2019-12-20 added call to deleteProjectAction in ProjectHandler.
 * v0.00.0017 2020-01-14 updated call to HG0414 (D. Ferguson)
 *            2020-01-15 change format of displayed known projects; use ProjHandler code
 * v0.00.0018 2020-02-01 implemented deletion code (N. Tolleshaug)
 * v0,00.0021 2020-04-11 delete projects at bottom of Project menu (D Ferguson)
 * v0.00.0022 2020-05-24 fix duplicated deletion log messages with wrong ID (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messages moved to GUI (N. Tolleshaug)
 * 			  2021-04-06 NLS all text strings (D Ferguson)
 * v0.01.0026 2021-09-17 apply tag codes to screen control buttons (D Ferguson)
 * 			  2021-09-29 add code to pass server name to ProjectHandler (D Ferguson)
 * v0.01.0027 2021-10-25 delete Server entry if delete last project on server (D Ferguson)
 *			  2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.03.0031 2024-10-01 Organize imports (D Ferguson)
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HB0744UserAUX;
import hre.bila.HBException;
import hre.bila.HBProjectHandler;
import hre.nls.HG0410Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Open
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-02-22
 */

public class HG0410ProjectDelete extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "41000"; //$NON-NLS-1$
	HG0410ProjectDelete pointProjectDelete = this;
 	private JPanel contents;

	private JTable table_Projects;

/**
 * Collect JTable for output by SuperDialog
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Projects;
	}

/**
 * Create the Dialog.
 */
	public HG0410ProjectDelete(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "deleteproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0410 Project Delete");} //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0410Msgs.Text_3);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][grow]", "[]10[grow]10[]10[]10[]10[]20[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_ProjectList = new JLabel(HG0410Msgs.Text_8);
		lbl_ProjectList.setFont(lbl_ProjectList.getFont().deriveFont(lbl_ProjectList.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_ProjectList, "cell 0 0"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 1 2, grow"); //$NON-NLS-1$

		JLabel lbl_SelectedProject = new JLabel(HG0410Msgs.Text_11);
		lbl_SelectedProject.setFont(lbl_SelectedProject.getFont().deriveFont(lbl_SelectedProject.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_SelectedProject, "cell 0 2, alignx right"); //$NON-NLS-1$

		JLabel lbl_ProjectName = new JLabel(HG0410Msgs.Text_13);
		contents.add(lbl_ProjectName, "cell 0 3, alignx right"); //$NON-NLS-1$
		JLabel lbl_ProjectNameData = new JLabel();
		contents.add(lbl_ProjectNameData, "cell 1 3"); //$NON-NLS-1$

		JLabel lbl_ProjectFile = new JLabel(HG0410Msgs.Text_16);
		contents.add(lbl_ProjectFile, "cell 0 4, alignx right"); //$NON-NLS-1$
		JLabel lbl_ProjectFileData = new JLabel();
		contents.add(lbl_ProjectFileData, "cell 1 4"); //$NON-NLS-1$

		JLabel lbl_Location = new JLabel(HG0410Msgs.Text_19);
		contents.add(lbl_Location, "cell 0 5, alignx right"); //$NON-NLS-1$
		JLabel lbl_LocationData = new JLabel();
		contents.add(lbl_LocationData, "cell 1 5"); //$NON-NLS-1$

		JButton btn_Summary = new JButton(HG0410Msgs.Text_22);
		btn_Summary.setToolTipText(HG0410Msgs.Text_23);
		contents.add(btn_Summary, "cell 0 6"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0410Msgs.Text_25);
		btn_Cancel.setToolTipText(HG0410Msgs.Text_26);
		contents.add(btn_Cancel, "cell 1 6, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Delete = new JButton(HG0410Msgs.Text_28);
		btn_Delete.setEnabled(false);
		btn_Delete.setToolTipText(HG0410Msgs.Text_29);
		contents.add(btn_Delete, "cell 1 6, align right, gapx 20, tag ok"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user, set TableModel and load data
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_Projects.setModel(new DefaultTableModel(
				pointProHand.getUserProjectArray2D(),
				HGlobalCode.projectTableHeader()));

		// Set the ability to sort on columns and presort column 0
		table_Projects.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_Projects.getModel());
		table_Projects.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);

		// Set tooltips, column widths and formats
		table_Projects.getTableHeader().setToolTipText(HG0410Msgs.Text_31);
		table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
		table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_Projects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader pHeader = table_Projects.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer rendererFromHeader = table_Projects.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel cellSelectionModel = table_Projects.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table_Projects.setPreferredScrollableViewportSize(new Dimension(530, 100));
		scrollPane.setViewportView(table_Projects);

		pack();

/***
 * CREATE ACTION BUTTON LISTENERS
 **/
		btn_Summary.addActionListener(new ActionListener() {
			//Displays properties of the Selected Project
			@Override
			public void actionPerformed(ActionEvent arg0) {
                int[] selectedRow = table_Projects.getSelectedRows();
                String [][] summaryData = null;
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
					try {
						summaryData = pointProHand.getSummaryUserProjectAction(selectedProjectName);
					} catch (HBException hbe) {
						JOptionPane.showMessageDialog(null, HG0410Msgs.Text_32
								+  hbe.getMessage(), HG0410Msgs.Text_33,JOptionPane.ERROR_MESSAGE);
					}
				}
				if (summaryData != null) {
					HG0414ProjectSummary summscreen = new HG0414ProjectSummary(summaryData);
					summscreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = btn_Summary.getLocationOnScreen();     // Gets Summary button location on screen
					summscreen.setLocation(xy.x + 50, xy.y);     	  // Sets screen top-left corner relative to that
					summscreen.setVisible(true);
				} else if (HGlobal.DEBUG) System.out.println("Summary data is null"); //$NON-NLS-1$
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg) {							// only show Cancel message if setting true
					if (JOptionPane.showConfirmDialog(btn_Cancel, HG0410Msgs.Text_35, HG0410Msgs.Text_36,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								if (HGlobal.writeLogs) {
									HB0711Logging.logWrite("Action: cancelling HG0410 Project Delete"); //$NON-NLS-1$
								}
								dispose();
							}  // yes option - return to main menu
				} else {
					if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0410 Project Delete");} //$NON-NLS-1$
					  dispose();
				}
			}
		});

		// Listener for clicking 'X on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);

					// If server name in projects table is ' Local', then set the server name to this PC
					String selectedServerName = (String) table_Projects.getValueAt(selectedRow[i], 2);
					if (selectedServerName == HGlobalCode.getLocalText()) selectedServerName = HGlobal.thisComputer;

					// Delete the project
					int errorCode = pointProHand.deleteProjectAction(pointProjectDelete, selectedProjectName, selectedServerName);
					if (errorCode == 0) {
						if (HGlobal.DEBUG) userInfoDeleteProject(errorCode, selectedProjectName);

					// Remove project from the Project menu project list
						pointProHand.mainFrame.removeProjMenuItem(selectedProjectName.trim());

					// Remove Server from userServers if this is last project on that server (except thisComputer)
						if (selectedServerName.trim() != HGlobal.thisComputer) {
							int countProjects = 0;
							for (int j = 0; j < HGlobal.userProjects.size(); j++) {
								if (HGlobal.userProjects.get(j)[3].equals(selectedServerName.trim())) countProjects++;
							}
						// If the counter is 0, there are no projects on this server, so delete the Server entry
							if (countProjects == 0) {
								for (int k = 0; k < HGlobal.userServers.size(); k++) {
									if (HGlobal.userServers.get(k)[0].equals(selectedServerName.trim()))
										HGlobal.userServers.remove(k);
								}
						// Update userAUX with new info
								HB0744UserAUX.writeUserAUXfile();
							}
						}

					// Do logging
						if (HGlobal.DEBUG) System.out.println("Delete and Dispose: " + selectedProjectName); //$NON-NLS-1$
						if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: delete project " + selectedProjectName +" from HG0410 Project Delete"); //$NON-NLS-1$ //$NON-NLS-2$
						dispose();

					} else if (errorCode == 1) {
						userInfoDeleteProject(errorCode, selectedProjectName);
					} else if (errorCode == 2) {
						userInfoDeleteProject(errorCode, selectedProjectName);
					} else if (errorCode == 3) {
						userInfoDeleteProject(errorCode, selectedProjectName);
					}
				}
			}
		});

		//Project Table selection - any selection from this table replaces any pre-selection
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String selectedProject = null;
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					//get the Project name irrespective of what field was selected in the table
					selectedProject = (String) table_Projects.getValueAt(selectedRow[i], 0);	// Project always in col 0

					//put the Project name, Filename, Folder name into their label places
					lbl_ProjectNameData.setText(selectedProject);
					lbl_ProjectNameData.setVisible(true);
					lbl_ProjectFileData.setText((String)table_Projects.getValueAt(selectedRow[i], 1));
					lbl_ProjectFileData.setVisible(true);
					lbl_LocationData.setText((String)table_Projects.getValueAt(selectedRow[i], 2));
					lbl_LocationData.setVisible(true);
					btn_Delete.setEnabled(true);
				}
			}
		});

		// Detect double-click on table_Projects row -> then delete this project
		table_Projects.addMouseListener(new MouseAdapter() {
		    @Override
			public void mousePressed(MouseEvent mouseEvent) {
		    	table_Projects =(JTable) mouseEvent.getSource();
		        if (mouseEvent.getClickCount() == 2
		        		&& table_Projects.getSelectedRow() != -1)   btn_Delete.doClick();
		    }
		});
	} // End HG0410ProjectDelete constructor

/**
 * Keep database file
 * @param deleteFilePath
 * @return
 */
	public boolean keepDatabaseFile(String deleteFilePath) {
		return JOptionPane.showConfirmDialog(null, HG0410Msgs.Text_42 + deleteFilePath, HG0410Msgs.Text_43,
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION;
	}	// End keepDatabaseFile

/**
 * GUI user messages
 * @param errorCode
 * @param projectName
 */
	private void userInfoDeleteProject(int errorCode, String projectName) {
		String errorMess = ""; //$NON-NLS-1$
		String errorTitle = HG0410Msgs.Text_45;
		if (errorCode == 0) {
			errorMess = HG0410Msgs.Text_46;
			JOptionPane.showMessageDialog(null, errorMess + projectName, errorTitle,
					JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode > 0) {
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("Action: failed HG0410 Project Delete");	 //$NON-NLS-1$
			if (errorCode == 1)  errorMess = HG0410Msgs.Text_48 + projectName + HG0410Msgs.Text_49;
			if (errorCode == 2)  errorMess = HG0410Msgs.Text_50 + projectName + HG0410Msgs.Text_51;
			if (errorCode == 3)  errorMess = HG0410Msgs.Text_52 + projectName;
			JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoDeleteProject

}	//End of HG0410ProjectDelete
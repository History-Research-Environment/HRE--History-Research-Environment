package hre.gui;
/***********************************************************************************
 * Project Rename - Specification 04.09 GUI_ProjectRename 2020-02-28
 * v0.00.0002 2019-02-10 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-22 fix close cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 setup server/project ArrayLists properly (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2020-01-14 updated call to HG0414 (D. Ferguson)
 * v0.00.0018 2020-02-10 implement rename code (N. Tolleshaug)
 * v0.00.0019 2020-02-28 fix logging of rename/exit actions (D.Ferguson)
 * v0.00.0020 2020-03-18 fix errors in DocumentListener code (D. Ferguson)
 * v0.00.0021 2020-04-11 delete old/add new project names to Project menu (D Ferguson)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-09 All JOptionPane messages moved to GUI (N. Tolleshaug)
 * 			  2021-04-06 NLS all text strings (D Ferguson)
 * v0.01.0026 2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 * 			  2021-09-27 Removed code for server login; disallowed remote rename (D Ferguson)
 * v0.01.0027 2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.03.0031 2024-10-01 Organize imports (D Ferguson)
 * 			  2024-11-30 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * v0.04.0032 2026-01-02 Logged catch block actions (D Ferguson)
 ************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectHandler;
import hre.nls.HG0409Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Rename
 * @author R Thompson
 * @version v0.03.0032
 * @since 2019-02-10
 */

public class HG0409ProjectRename extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "40900"; //$NON-NLS-1$
	private JPanel contents;
	private JTextField txtEnterNewProject;
	private String newProjectName = ""; //$NON-NLS-1$
	private String selectedProject = ""; //$NON-NLS-1$

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
	public HG0409ProjectRename(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "renameproject";	 //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0409 Project Rename");	 //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0409Msgs.Text_5);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][grow]", "[]10[grow]10[]10[]10[]10[]20[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
	    toolBar.add(Box.createHorizontalGlue());
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_From = new JLabel(HG0409Msgs.Text_10);
		lbl_From.setFont(lbl_From.getFont().deriveFont(lbl_From.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_From, "cell 0 0"); //$NON-NLS-1$

		JLabel lbl_OpenProjects = new JLabel(HG0409Msgs.Text_12);
		contents.add(lbl_OpenProjects, "cell 0 1 2"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 2 2, grow"); //$NON-NLS-1$

		JButton btn_Summary = new JButton(HG0409Msgs.Text_15);
		btn_Summary.setToolTipText(HG0409Msgs.Text_16);
		btn_Summary.setEnabled(false);				//Enable after project selection
		contents.add(btn_Summary, "cell 0 3"); //$NON-NLS-1$

		JLabel lbl_SelectProject = new JLabel(HG0409Msgs.Text_18);
		lbl_SelectProject.setVisible(false);
		contents.add(lbl_SelectProject, "cell 1 3"); //$NON-NLS-1$
		JLabel lbl_ProjectNameData = new JLabel();
		contents.add(lbl_ProjectNameData, "cell 1 3, gapx 10");	 //$NON-NLS-1$

		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setForeground(UIManager.getColor("scrollbar")); //$NON-NLS-1$
		separator.setPreferredSize(new Dimension(0, 3));
		contents.add(separator, "cell 0 4 2, growx"); //$NON-NLS-1$

		JLabel lbl_To = new JLabel(HG0409Msgs.Text_23);
		lbl_To.setFont(lbl_To.getFont().deriveFont(lbl_To.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_To, "cell 0 5"); //$NON-NLS-1$

		JLabel lbl_NewName = new JLabel(HG0409Msgs.Text_25);
		contents.add(lbl_NewName, "cell 0 6");			 //$NON-NLS-1$
		txtEnterNewProject = new JTextField(HG0409Msgs.Text_27);
		contents.add(txtEnterNewProject, "cell 1 6, growx");				 //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0409Msgs.Text_29);
		btn_Cancel.setToolTipText(HG0409Msgs.Text_30);
		contents.add(btn_Cancel, "cell 1 7, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Rename = new JButton(HG0409Msgs.Text_32);
		btn_Rename.setToolTipText(HG0409Msgs.Text_33);
		btn_Rename.setEnabled(false);					//Enable after project selection
		contents.add(btn_Rename, "cell 1 7, align right, gapx 20, tag ok"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user and set TableModel
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_Projects.setModel(new DefaultTableModel(
				pointProHand.getUserProjectArray2D(),
				HGlobalCode.projectTableHeader()));

		table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
		table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_Projects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Set the ability to sort on columns and presort column 0
		table_Projects.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_Projects.getModel());
		table_Projects.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);

		// Set tooltips, column widths and header format
		table_Projects.getTableHeader().setToolTipText(HG0409Msgs.Text_35);
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
 * CREATE ACTION LISTENERS
***/
		//buttons
		btn_Summary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
                int[] selectedRow = table_Projects.getSelectedRows();
                String [][] summaryData = null;
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
					try {
						summaryData = pointProHand.getSummaryUserProjectAction(selectedProjectName);
					} catch (HBException hbe) {
						JOptionPane.showMessageDialog(contents, HG0409Msgs.Text_36
								+  hbe.getMessage(), HG0409Msgs.Text_37,JOptionPane.ERROR_MESSAGE);
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0409 project summary error " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
				if (summaryData != null) {
					HG0414ProjectSummary summscreen = new HG0414ProjectSummary(summaryData);
					summscreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = btn_Summary.getLocationOnScreen();          	  // Gets Summary button location on screen
					summscreen.setLocation(xy.x + 50, xy.y);     			  // Sets screen top-left corner relative to that
					summscreen.setVisible(true);
				} else if (HGlobal.writeLogs) HB0711Logging.logWrite("Result: in HG0409 Summary data is null"); //$NON-NLS-1$
			}
		});

		btn_Rename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: renaming project "+ selectedProject+" to "+newProjectName+" in HG0409 Project Rename"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int errorCode = pointProHand.renameProjectAction(selectedProject , newProjectName);
				//if (pointProHand.renameProjectAction(selectedProject , newProjectName))  {
				if (errorCode == 0) {
					// if successful, remove old name from the Project menu project list and add new one
					pointProHand.mainFrame.removeProjMenuItem(selectedProject);
					pointProHand.mainFrame.addProjMenuItem(newProjectName);
					// Display confirmation message
					userInfoRenameProject(errorCode, newProjectName);
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0409 Project Rename");		 //$NON-NLS-1$
					if (HGlobal.DEBUG) userInfoRenameProject(errorCode, newProjectName);
					dispose();
				} else if (errorCode == 1) {
                	userInfoRenameProject(errorCode, newProjectName);
                } else if (errorCode == 2) {
                	userInfoRenameProject(errorCode, selectedProject);
                } else if (errorCode == 3) {
                	userInfoRenameProject(errorCode, selectedProject);
                }
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0409Msgs.Text_43, HG0409Msgs.Text_44,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0409 Project Rename"); //$NON-NLS-1$
							dispose();	}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0409 Project Rename"); //$NON-NLS-1$
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

		// Project Table selection
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				//String selectedProject = null;
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					// if selected row server name is not ' Local' (column 2) it is remote, so do not allow
						if ((String) table_Projects.getValueAt(selectedRow[i], 2) != HGlobalCode.getLocalText()) {
							lbl_SelectProject.setVisible(false);
							lbl_ProjectNameData.setText(HG0409Msgs.Text_45);	// Cannot Rename Remote Proj
							lbl_ProjectNameData.setForeground(Color.RED);
							return;
						}
					// Otherwise, put the Project name (column 0) into the 'ProjectName label'
						selectedProject = (String) table_Projects.getValueAt(selectedRow[i], 0);
						lbl_SelectProject.setVisible(true);
						lbl_ProjectNameData.setText(selectedProject);
						lbl_ProjectNameData.setForeground(Color.BLACK);
					// and enable the Summary button
						btn_Summary.setEnabled(true);
				}
			}
		});

		// Clean out project prompt when Textfield gets focus
		txtEnterNewProject.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				txtEnterNewProject.setText("");  //$NON-NLS-1$
				txtEnterNewProject.setBackground(Color.YELLOW);
				}
			});
		// Listener for entry of new Project Name
		DocumentListener docListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) {
		        updateFieldState();
		    }
		    @Override
		    public void removeUpdate(DocumentEvent e) {
		    	updateFieldState();
		    }
		    @Override
		    public void changedUpdate(DocumentEvent e) {
		        updateFieldState();
		    }
		    protected void updateFieldState() {
		     	newProjectName = txtEnterNewProject.getText().trim();   // remove any leading/trailing blanks then cleanout silly characters
		    	newProjectName = newProjectName.replaceAll("[\\/|\\\\|\\*|\\:|\\;|\\^|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",""); //$NON-NLS-1$ //$NON-NLS-2$
		    	if (newProjectName.length() == 0 ) {
		    		btn_Rename.setEnabled(false);
		    		txtEnterNewProject.setBackground(Color.YELLOW);
		    		}
		    		else if (newProjectName.equals(selectedProject))  {
		    				JOptionPane.showMessageDialog(btn_Rename, HG0409Msgs.Text_50, HG0409Msgs.Text_51,JOptionPane.WARNING_MESSAGE);
		    				btn_Rename.setEnabled(false);
		    				txtEnterNewProject.setBackground(Color.YELLOW);
		    				}
		    			else {btn_Rename.setEnabled(true); 						 //turn on Rename button
		    				  txtEnterNewProject.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
		    				  txtEnterNewProject.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
		    				  }
		    		}
		    };
		    txtEnterNewProject.getDocument().addDocumentListener(docListen);

	}	// End of HG0409 constructor

/**
 * GUI user messages
 * @param errorCode
 * @param projectName
 */
	private void userInfoRenameProject(int errorCode, String projectName) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0409Msgs.Text_55;
		if (errorCode == 0) {
			errorMessage = HG0409Msgs.Text_56;
			JOptionPane.showMessageDialog(contents, errorMessage + projectName, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode > 0) {
			if (errorCode == 1)  errorMessage = HG0409Msgs.Text_57 + projectName + HG0409Msgs.Text_58;
			if (errorCode == 2)  errorMessage = HG0409Msgs.Text_59 + projectName + HG0409Msgs.Text_60;
			if (errorCode == 3)  errorMessage = HG0409Msgs.Text_61 +  projectName + HG0409Msgs.Text_62;
			JOptionPane.showMessageDialog(contents, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoRenameProject

}	// End of HG0409ProjectRename

package hre.gui;
/*********************************************************************************************
 * Project Remote definition - add a Remote Project to UserAUX
 * v0.01.0027 2021-10-03 First version (D Ferguson)
 *		 	  2021-10-25 Create IP addr/port in userServers table (D Ferguson)
 *			  2021-12-13 Display known remote projects and New/Edit action buttons (D Ferguson)
 *			  2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.03.0030 2023-09-28 Implement full NLS (D Ferguson)
 * v0.03.0031 2024-10-01 Clean whitespaces (D Ferguson)
 ********************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import hre.bila.HB0744UserAUX;
import hre.bila.HBProjectHandler;
import hre.nls.HG0418Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Remote definition
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2021-10-03
 */

public class HG0418ProjectRemote extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "41800";     //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_ProjectRem, txt_FilenameRem, txt_FolderRem, txt_ServerRem, txt_IPRem, txt_Port;
	private String rmtProjectName = ""; 	//$NON-NLS-1$
	private String rmtFilename = ""; 		//$NON-NLS-1$
	private String rmtFolderName = ""; 		//$NON-NLS-1$
	private String rmtServerName = ""; 		//$NON-NLS-1$
	private String rmtTCPIP = ""; 			//$NON-NLS-1$
	private String rmtPort = ""; 			//$NON-NLS-1$
	private String savedIPaddr = ""; 		//$NON-NLS-1$
	private String savedPort = ""; 			//$NON-NLS-1$
	private boolean rmtProjectOK = false;
	private boolean rmtFilenameOK = false;
	private boolean rmtFolderOK = false;
	private boolean rmtServerOK = false;
	private boolean rmtTCPIPOK = false;
	private boolean rmtPortOK = false;
	private boolean serverExists = false;
	private boolean projectExists = false;
	boolean haveNotSaved = true;
	boolean inServerCode = false;
	boolean definingNew = false;

	private JTable table_RmtProj;
	private String selectedProject = ""; //$NON-NLS-1$

/**
 * Create the Dialog.
 */
	public HG0418ProjectRemote(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "rmtproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0418ProjectRemote"); //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0418Msgs.Text_0);		// Remote Project Definition
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");  //$NON-NLS-1$

		// Define initial action buttons
		JButton btn_Select = new JButton(HG0418Msgs.Text_1);		// Edit a remote project
		contents.add(btn_Select, "hidemode 3, cell 0 0, alignx center");  //$NON-NLS-1$

		JLabel lbl_OR = new JLabel(HG0418Msgs.Text_2);				// ' -  OR - '
		contents.add(lbl_OR, "hidemode 3, cell 0 0, alignx center, gapx 20");  //$NON-NLS-1$

		JButton btn_New = new JButton(HG0418Msgs.Text_3);		// Define a New Remote Project
		btn_New.setToolTipText(HG0418Msgs.Text_3);
		contents.add(btn_New, "hidemode 3, cell 0 0, alignx center, gapx 20"); //$NON-NLS-1$

		// Define the alternate first row labels
		JLabel lbl_Known = new JLabel(HG0418Msgs.Text_5);		// Select a Remote Project to Edit
		lbl_Known.setFont(lbl_Known.getFont().deriveFont(lbl_Known.getFont().getStyle() | Font.BOLD));
		lbl_Known.setVisible(false);
		contents.add(lbl_Known, "cell 0 0, alignx left, hidemode 3");  //$NON-NLS-1$

		JLabel lbl_Enter = new JLabel(HG0418Msgs.Text_6);		// Enter New Remote Project Details
		lbl_Enter.setFont(lbl_Enter.getFont().deriveFont(lbl_Enter.getFont().getStyle() | Font.BOLD));
		lbl_Enter.setVisible(false);
		contents.add(lbl_Enter, "cell 0 0, alignx left, hidemode 3");  //$NON-NLS-1$

		// Define an area for remote projects table
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVisible(false);
		contents.add(scrollPane, "cell 0 1, hidemode 3"); //$NON-NLS-1$

		// Define a bordered panel for Project info
		JPanel projPanel = new JPanel();
		projPanel.setVisible(false);
		projPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				new Color(255, 255, 255), new Color(160, 160, 160)), HG0418Msgs.Text_7, 		// Remote Project Data
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contents.add(projPanel, "cell 0 2, hidemode 3");		//$NON-NLS-1$
		projPanel.setLayout(new MigLayout("insets 10", "[200]10[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_ProjectName = new JLabel(HG0418Msgs.Text_8);		// Remote Project's Name
		projPanel.add(lbl_ProjectName, "cell 0 0, alignx right");   //$NON-NLS-1$
		txt_ProjectRem = new JTextField();
		txt_ProjectRem.setToolTipText(HG0418Msgs.Text_9);			// Enter the new Project Name
		txt_ProjectRem.setText(HG0418Msgs.Text_10);					// \ --- Enter the new Project Name ---
		txt_ProjectRem.setColumns(30);
		projPanel.add(txt_ProjectRem, "cell 1 0"); //$NON-NLS-1$

		JLabel lbl_FilenameRem = new JLabel(HG0418Msgs.Text_11);	// Remote Project's Filename
		projPanel.add(lbl_FilenameRem, "cell 0 1, alignx right");   //$NON-NLS-1$
		txt_FilenameRem = new JTextField();
		txt_FilenameRem.setText(HG0418Msgs.Text_12);				// \ --- Enter the Remote Project Filename ---
		txt_FilenameRem.setColumns(30);
		projPanel.add(txt_FilenameRem, "cell 1 1"); //$NON-NLS-1$

		JLabel lbl_FolderRem = new JLabel(HG0418Msgs.Text_13);		// Remote Project's Folder Location
		projPanel.add(lbl_FolderRem, "cell 0 2, alignx right"); 	//$NON-NLS-1$
		txt_FolderRem = new JTextField();
		txt_FolderRem.setText(HG0418Msgs.Text_14);					// \ --- Enter the Remote Project Folder Location ---
		txt_FolderRem.setColumns(30);
		projPanel.add(txt_FolderRem, "cell 1 2"); //$NON-NLS-1$

		// Define a bordered panel for the Server info
		JPanel servPanel = new JPanel();
		servPanel.setVisible(false);
		servPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				new Color(255, 255, 255), new Color(160, 160, 160)), HG0418Msgs.Text_15,	// Remote Server Data
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contents.add(servPanel, "cell 0 3, hidemode 3");	//$NON-NLS-1$
		servPanel.setLayout(new MigLayout("insets 10", "[200]10[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_ServerRem = new JLabel(HG0418Msgs.Text_16);		// Remote Server Name
		servPanel.add(lbl_ServerRem, "cell 0 0, alignx right"); 	//$NON-NLS-1$
		txt_ServerRem = new JTextField();
		txt_ServerRem.setText(HG0418Msgs.Text_17);					// \ --- Enter the Remote Server Name ---
		txt_ServerRem.setColumns(30);
		servPanel.add(txt_ServerRem, "cell 1 0"); 	//$NON-NLS-1$
		String promptIPaddr = HG0418Msgs.Text_18;					// Remote Server IP Address
		JLabel lbl_IPRem = new JLabel(promptIPaddr);
		servPanel.add(lbl_IPRem, "cell 0 1, alignx right"); 		//$NON-NLS-1$
		txt_IPRem = new JTextField();
		txt_IPRem.setText(HG0418Msgs.Text_19);						// \ --- Enter the Server IP Address (N.N.N.N) ---
		txt_IPRem.setColumns(30);
		servPanel.add(txt_IPRem, "cell 1 1"); //$NON-NLS-1$

		String promptPort = HG0418Msgs.Text_20;				// Remote Server Port Number
		JLabel lbl_Port = new JLabel(promptPort);
		servPanel.add(lbl_Port, "cell 0 2, alignx right"); //$NON-NLS-1$
		txt_Port = new JTextField();
		txt_Port.setText(HG0418Msgs.Text_21);				// \ --- Enter the Server Port Number ---
		txt_Port.setColumns(30);
		servPanel.add(txt_Port, "cell 1 2"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0418Msgs.Text_22);	// Cancel
		btn_Cancel.setToolTipText(HG0418Msgs.Text_23);			// Cancel the remote project definition
		contents.add(btn_Cancel, "cell 0 4, align right, gapx 20, tag cancel, hidemode 3"); //$NON-NLS-1$

		JButton btn_Save = new JButton(HG0418Msgs.Text_24);		// Save
		btn_Save.setVisible(false);
		btn_Save.setEnabled(false);
		btn_Save.setToolTipText(HG0418Msgs.Text_25);			// Save the remote project definition
		contents.add(btn_Save, "cell 0 4, align right, gapx 20, tag ok, hidemode 3"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user, set TableModel, and load data
		table_RmtProj = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {return false;}
										};
		table_RmtProj.setModel(new DefaultTableModel(
								getRemoteProjects(),
								HGlobalCode.rmtprojTableHeader()));
		// Set the ability to sort on columns and presort column 0 (Project Name)
		table_RmtProj.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_RmtProj.getModel());
		table_RmtProj.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);

		// Set tooltips, column widths and formats
		table_RmtProj.getTableHeader().setToolTipText(HG0418Msgs.Text_26);	// Click to sort; Click again to sort in reverse order
		table_RmtProj.getColumnModel().getColumn(0).setMinWidth(150);
		table_RmtProj.getColumnModel().getColumn(0).setPreferredWidth(185);
		table_RmtProj.getColumnModel().getColumn(1).setMinWidth(150);
		table_RmtProj.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_RmtProj.getColumnModel().getColumn(2).setMinWidth(100);
		table_RmtProj.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_RmtProj.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader pHeader = table_RmtProj.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer rendererFromHeader = table_RmtProj.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel cellSelectionModel = table_RmtProj.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table_RmtProj.setPreferredScrollableViewportSize(new Dimension(515, 50));
		scrollPane.setViewportView(table_RmtProj);

		pack();

/***
 * Create Document Listeners
 **/
		// DOCLISTENER for Project Name entry
		txt_ProjectRem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				// Clear out prompt text 1st time field gets focus
				if (txt_ProjectRem.getText().contains("---")) txt_ProjectRem.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_ProjectRem.setBackground(Color.YELLOW);
				txt_ProjectRem.setForeground(Color.BLACK);}
			});
		// Listener for entry of new Project Name
		DocumentListener projListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
            	rmtProjectName = txt_ProjectRem.getText().trim();    // remove any leading/trailing blanks
            	if (rmtProjectName.length() == 0 ) {
            		txt_ProjectRem.setBackground(Color.YELLOW);
            		txt_ProjectRem.setForeground(Color.BLACK);
            		rmtProjectOK = false;
            		}
            	else {rmtProjectOK = true;
            		  txt_ProjectRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_ProjectRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// If all fields valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK & rmtPortOK)
            		btn_Save.setEnabled(true);
            } };
        txt_ProjectRem.getDocument().addDocumentListener(projListen);

		// DOCLISTENER for Filename entry
		txt_FilenameRem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg1) {
				// Clear out prompt text 1st time field gets focus
				if (txt_FilenameRem.getText().contains("---")) txt_FilenameRem.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_FilenameRem.setBackground(Color.YELLOW);
				txt_FilenameRem.setForeground(Color.BLACK);}
			});
		// Listener for entry of new Filename
		DocumentListener fileListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
            	rmtFilename = txt_FilenameRem.getText().trim();
            	if (rmtFilename.length() == 0 ) {
            		txt_FilenameRem.setBackground(Color.YELLOW);
            		txt_FilenameRem.setForeground(Color.BLACK);
            		rmtFilenameOK = false;
            		}
            	else {rmtFilenameOK = true;	// assume OK for now
            		  txt_FilenameRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_FilenameRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// If all fields valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK & rmtPortOK)
            		btn_Save.setEnabled(true);
            } };
        txt_FilenameRem.getDocument().addDocumentListener(fileListen);

		// DOCLISTENER for Folder entry
		txt_FolderRem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg2) {
				// Clear out prompt text 1st time field gets focus
				if (txt_FolderRem.getText().contains("---")) txt_FolderRem.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_FolderRem.setBackground(Color.YELLOW);
				txt_FolderRem.setForeground(Color.BLACK);}
			});
		// Listener for entry of new Folder Name
		DocumentListener folderListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
            	rmtFolderName = txt_FolderRem.getText().trim();
            	if (rmtFolderName.length() == 0 ) {
            		txt_FolderRem.setBackground(Color.YELLOW);
            		txt_FolderRem.setForeground(Color.BLACK);
            		rmtFolderOK = false;
            		}
            	else {rmtFolderOK = true;	// assume OK for now
            		  txt_FolderRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_FolderRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// If all fields valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK)
            		btn_Save.setEnabled(true);
            } };
            txt_FolderRem.getDocument().addDocumentListener(folderListen);

		// DOCLISTENER for Server entry
		txt_ServerRem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg3) {
				// Clear out prompt text 1st time field gets focus
				if (txt_ServerRem.getText().contains("---")) txt_ServerRem.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_ServerRem.setBackground(Color.YELLOW);
				txt_ServerRem.setForeground(Color.BLACK);}
			});
		// Listener for entry of new Server Name
		DocumentListener serverListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
            	inServerCode = true;
            	rmtServerName = txt_ServerRem.getText().trim();    // remove any leading/trailing blanks
            	if (rmtServerName.length() == 0 ) {
            		txt_ServerRem.setBackground(Color.YELLOW);
            		txt_ServerRem.setForeground(Color.BLACK);
            		rmtServerOK = false;
            		}
            	else {rmtServerOK = true;
            		  txt_ServerRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_ServerRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// Save the IPaddr and Port fields in case we need to reinstate them
            	if (haveNotSaved) {
            		savedIPaddr = txt_IPRem.getText();
            		savedPort = txt_Port.getText();
            		haveNotSaved = false;
            	}
            	// Now check if entered ServerName already exists in server table - if so,
            	// get the existing IP addr and port and use them.
            	for (int i = 0; i < HGlobal.userServers.size(); i++) {
					if (HGlobal.userServers.get(i)[0].trim().equals(rmtServerName.trim())) {
						lbl_IPRem.setText(HG0418Msgs.Text_27);			// Confirm Server IP Address
						txt_IPRem.setText(HGlobal.userServers.get(i)[1]);
						rmtTCPIP = HGlobal.userServers.get(i)[1];
						rmtTCPIPOK = true;
						lbl_Port.setText(HG0418Msgs.Text_28);			// Confirm Server Port Number
						txt_Port.setText(HGlobal.userServers.get(i)[2]);
						rmtPort = HGlobal.userServers.get(i)[2];
						rmtPortOK = true;
					}
					// but user may have entered a server name that initially matched an existing server,
					// then kept typing a different (longer) name. In that case we need to reset the Labels
					// and restore the text values to what they were before we replaced them in the above IF code.
					else {
						lbl_IPRem.setText(promptIPaddr);
						txt_IPRem.setText(savedIPaddr);
						lbl_Port.setText(promptPort);
						txt_Port.setText(savedPort);
					}
				}
            	// If all fields valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK & rmtPortOK)
            		btn_Save.setEnabled(true);
            	inServerCode = false;
            } };
            txt_ServerRem.getDocument().addDocumentListener(serverListen);

		// DOCLISTENER for TCP/IP entry
		txt_IPRem.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg4) {
				// If we're here because Server code has changed the TCP field, then exit
				if (inServerCode) return;
				// Otherwise clear out prompt text 1st time field gets focus
				if (txt_IPRem.getText().contains("---")) txt_IPRem.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_IPRem.setBackground(Color.YELLOW);
				txt_IPRem.setForeground(Color.BLACK);
				}
			});
		// Listener for entry of new TCP/IP data
		DocumentListener tcpListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
            	if (inServerCode) return;
            	rmtTCPIP = txt_IPRem.getText().trim(); // entry has to be at least 7 chars (n.n.n.n)
            	if (rmtTCPIP.length() < 7 ) {
            		txt_IPRem.setBackground(Color.YELLOW);
            		txt_IPRem.setForeground(Color.BLACK);
            		rmtTCPIPOK = false;
            		}
            	else {rmtTCPIPOK = true;	// set true for now, subject to final test
            		  txt_IPRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_IPRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// If all fields appear valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK & rmtPortOK)
            		btn_Save.setEnabled(true);
            } };
            txt_IPRem.getDocument().addDocumentListener(tcpListen);


		// DOCLISTENER for Port entry
		txt_Port.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg4) {
				// If we're here because Server code has changed the Port field, then exit
				if (inServerCode) return;
				// Otherwise clear out prompt text 1st time field gets focus
				if (txt_Port.getText().contains("---")) txt_Port.setText("");  //$NON-NLS-1$ //$NON-NLS-2$
				txt_Port.setBackground(Color.YELLOW);
				txt_Port.setForeground(Color.BLACK);}
			});
		// Listener for entry of new Port number data
		DocumentListener portListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFieldState(); }
            public void removeUpdate(DocumentEvent e) { updateFieldState(); }
            public void changedUpdate(DocumentEvent e) { updateFieldState(); }
            protected void updateFieldState() {
               	if (inServerCode) return;
            	rmtPort = txt_Port.getText();
            	if (rmtPort.length() < 3 ) {
            		txt_Port.setBackground(Color.YELLOW);
            		txt_Port.setForeground(Color.BLACK);
            		rmtPortOK = false;
            		}
            	else {rmtPortOK = true;		// assume OK for now, subject to final check
            		  txt_Port.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_Port.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            	}
            	// If all fields appear valid, enable Save button
            	if (rmtProjectOK & rmtFilenameOK & rmtFolderOK & rmtServerOK & rmtTCPIPOK & rmtPortOK)
            		btn_Save.setEnabled(true);
            } };
            txt_Port.getDocument().addDocumentListener(portListen);

/**
 * Implement Action Listeners
 */
        // For button to select a project to edit
    	// Remove initial buttons and show required panels/buttons
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Select.setVisible(false);
				lbl_OR.setVisible(false);
				btn_New.setVisible(false);
				lbl_Known.setVisible(true);
				scrollPane.setVisible(true);
				btn_Save.setVisible(true);
				definingNew = false;

				pack();
			}
		});

        // For button to define a new project from scratch
		// Remove initial buttons and show required panels/buttons
		btn_New.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Select.setVisible(false);
				lbl_OR.setVisible(false);
				btn_New.setVisible(false);
				lbl_Enter.setVisible(true);
				projPanel.setVisible(true);
				servPanel.setVisible(true);
				btn_Save.setVisible(true);
				definingNew = true;

				pack();
			}
		});

        // For Save button
        btn_Save.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
        	// Check if project name already used on same server; this is an error if we were defining a New project
			for (int i = 0; i < HGlobal.userProjects.size(); i++) {
				if (HGlobal.userProjects.get(i)[0].trim().equals(rmtProjectName.trim())
						& HGlobal.userProjects.get(i)[3].trim().equals(rmtServerName.trim())) {
					if (definingNew) {
						rmtProjectOK = false;
						txt_ProjectRem.setBackground(Color.YELLOW);
						txt_ProjectRem.setForeground(Color.BLACK);
            			JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_29,		// Project already defined on this server \n Please re-enter
            				  HG0418Msgs.Text_30, JOptionPane.ERROR_MESSAGE);			// Project Name Check
            		  }
					  break;
				}
			}

			// Do full test of validity of filename string
        	rmtFilename = txt_FilenameRem.getText().trim();
        	if (isValid(rmtFilename) & rmtFilename.length() > 0) {
        		 rmtFilenameOK = true;
        		 txt_FilenameRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
        		 txt_FilenameRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
        		}
        	else {rmtFilenameOK = false;
        		  txt_FilenameRem.setBackground(Color.YELLOW);
        		  txt_FilenameRem.setForeground(Color.BLACK);
        		  JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_31,		// Invalid Filename, please re-enter
        				  HG0418Msgs.Text_32, JOptionPane.ERROR_MESSAGE);			// Remote Filename
        	}

			// Do full test of validity of folder string
        	rmtFolderName = txt_FolderRem.getText().trim();
        	if (isValid(rmtFolderName) & rmtFolderName.length() > 0) {
        		 rmtFolderOK = true;
        		 txt_FolderRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
        		 txt_FolderRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
        		}
        	else {rmtFolderOK = false;
        		  txt_FolderRem.setBackground(Color.YELLOW);
        		  txt_FolderRem.setForeground(Color.BLACK);
        		  JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_33,		// Invalid Folder name, please re-enter
        				  HG0418Msgs.Text_34, JOptionPane.ERROR_MESSAGE);			// Remote Folder Name
        	}

           	// Do full test of validity of entered TCP/IP string
        	rmtTCPIP = txt_IPRem.getText().trim();
        	if (!HGlobalCode.isIPValid(rmtTCPIP)) {
        		txt_IPRem.setBackground(Color.YELLOW);
        		txt_IPRem.setForeground(Color.BLACK);
        		rmtTCPIPOK = false;
        		JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_35,		// Invalid TCP/IP address, please re-enter
        				HG0418Msgs.Text_36, JOptionPane.ERROR_MESSAGE);			// TCP/IP Address
        		}
				else {rmtTCPIPOK = true;
					  txt_IPRem.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
					  txt_IPRem.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
					  }

           	// Do full test of validity of entered Port number string
        	rmtPort = txt_Port.getText().trim();
        	if (!isValidPort(rmtPort)) {
        		txt_Port.setBackground(Color.YELLOW);
        		txt_Port.setForeground(Color.BLACK);
        		rmtPortOK = false;
        		JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_37,		// Invalid Port Number, please re-enter
        				HG0418Msgs.Text_38, JOptionPane.ERROR_MESSAGE);			// Port Number
        		}
				else {rmtPortOK = true;
					  txt_Port.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
					  txt_Port.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
					  }

        	// Only proceed if all fields have been validated correctly
        	if (!rmtProjectOK | !rmtFilenameOK | !rmtFolderOK | !rmtServerOK | !rmtTCPIPOK | !rmtPortOK) return;

        	// Create new Project arraylist
        	String[] newProjectData = {
            		rmtProjectName,
            		rmtFilename,
            		rmtFolderName,
            		rmtServerName,
            		pointProHand.currentTime("yyyy-MM-dd / HH:mm:ss"),  //$NON-NLS-1$
            		HGlobal.defDatabaseEngine,
            		HG0418Msgs.Text_39};	// unknown
        	// Check if Project exists in known Project list - if it does, update it; if not, add it.
        	// (if we were supposed to be defining a New project, that error would already have been flagged)
			for (int i = 0; i < HGlobal.userProjects.size(); i++) {
				if (HGlobal.userProjects.get(i)[0].trim().equals(rmtProjectName.trim())) {
					projectExists = true;
					HGlobal.userProjects.get(i)[1] = rmtFilename;
					HGlobal.userProjects.get(i)[2] = rmtFolderName;
					HGlobal.userProjects.get(i)[3] = rmtServerName;
					break;
				}
			}
			if (!projectExists) HGlobal.userProjects.add(newProjectData);

			// Create new Server arraylist
			String[] newServerData = {
            		rmtServerName,
            		rmtTCPIP,
            		rmtPort};
			// Check if Server exists in known Server list - if it does, update it; if not, add it
			for (int i = 0; i < HGlobal.userServers.size(); i++) {
				if (HGlobal.userServers.get(i)[0].trim().equals(rmtServerName.trim())) {
					serverExists = true;
					HGlobal.userServers.get(i)[1] = rmtTCPIP;
					HGlobal.userServers.get(i)[2] = rmtPort;
					break;
				}
			}
			if (!serverExists) 	HGlobal.userServers.add(newServerData);

        	// Update userAUX with new info and exit
			HB0744UserAUX.writeUserAUXfile();
			JOptionPane.showMessageDialog(btn_Save, HG0418Msgs.Text_40,		// Remote Project definition accepted
    				HG0418Msgs.Text_41, JOptionPane.INFORMATION_MESSAGE);	// Define Remote Project
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: writing new project data and exit from HG0418ProjectRemote"); //$NON-NLS-1$
			dispose();
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)								// only show Cancel message if setting true
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0418Msgs.Text_42,		// Cancel definition of Remote Project?
							HG0418Msgs.Text_43, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {		// Cancel Remote Project Process
								if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0418ProjectRemote"); //$NON-NLS-1$
								dispose();
								}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0418ProjectRemote"); //$NON-NLS-1$
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

/**
 * 	Remote Project Table selection
 */
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// show the edit panels
				projPanel.setVisible(true);
				servPanel.setVisible(true);
				// take data from selected row into edit panels
				int[] selectedRow = table_RmtProj.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					// get the Project name irrespective of what field was selected in the table row
					selectedProject = (String) table_RmtProj.getValueAt(selectedRow[i], 0);	// Project always in col 0
				}
				// Get the project data and put it into the GUI
				// HGlobal.userProjects is: Projectname, Filename, Folder, Servername, Lastclosed, DBtype, LastBackup
				txt_ProjectRem.setText(selectedProject.trim());
				rmtProjectName = selectedProject.trim();
				for (int j = 0; j < HGlobal.userProjects.size(); j++) {
					String[] projRow = HGlobal.userProjects.get(j);
					if (rmtProjectName.equals(projRow[0])) {
						rmtFilename = projRow[1];
						txt_FilenameRem.setText(rmtFilename);
						rmtFolderName = projRow[2];
						txt_FolderRem.setText(rmtFolderName);
						rmtServerName = projRow[3];
						txt_ServerRem.setText(rmtServerName);
						break;
					}
				}
				// Get the server data and put it into the GUI
				// HGlobal.userServers is: Servername, TCP/IP address, Port number
				for (int k = 0; k < HGlobal.userProjects.size(); k++) {
					String[] servRow = HGlobal.userServers.get(k);
					if (rmtServerName.equals(servRow[0])) {
						rmtTCPIP = servRow[1];
						txt_IPRem.setText(rmtTCPIP);
						rmtPort = servRow[2];
						txt_Port.setText(rmtPort);
						break;
					}
				}
				pack();
			}
		});

	}	// End HG0418 constructor

/**
 * Get String[][] from userProjects and populate JTable with only Rmt projects
 * @return String[][]
 */
	private Object[][] getRemoteProjects() {
		// Count the number of remote projects in HGlobal.userProjects
		int keep = 0;
		for (int i = 0; i < HGlobal.userProjects.size(); i++) {
			String[] remRow = HGlobal.userProjects.get(i);
			if (!remRow[3].trim().equals(HGlobal.thisComputer.trim())) keep = keep + 1;
		}
		// Set new 2d Array to the size of found rmt projects
		String[][] remProjects = new String[keep][];
		int k = 0;
		for (int i = 0; i < HGlobal.userProjects.size(); i++) {
			String[] remRow = HGlobal.userProjects.get(i);
			// select only those projects NOT on this PC
			if (!remRow[3].trim().equals(HGlobal.thisComputer.trim())) {
	        	remProjects[k] = new String[] {remRow[0],	//project name
									remRow[1],	//file name
									remRow[3]};	//server name
	        	k++;
			}
		}
		return remProjects;
	}

/**
 * Checks if a string is a valid folder or filename (all OS's)
 * @param toCheck
 */
    private static boolean isValid(String toCheck) {
    	// check for validity
        try {
            Paths.get(toCheck);
        	} catch (InvalidPathException | NullPointerException ex) {
              return false;
        	}
        // As Paths is an incomplete check in Windows, also check following -
        if (HGlobal.osType.contains("win")) {					//$NON-NLS-1$
        	if (toCheck.matches(".*[;*?<>|].*")) return false;	//$NON-NLS-1$
        }
        return true;
    }	// End isValid

/**
 * Checks if the Port string is valid (numeric, > 1023, < 65535)
 * @param strNum
 */
    public static boolean isValidPort(String strNum) {
    	double dd;
        if (strNum == null) return false;
        try {
            dd = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        if (dd < 1024 | dd > 65535) return false;
        return true;
    }	// End isValidPort

}	// End HG0418ProjectRemote

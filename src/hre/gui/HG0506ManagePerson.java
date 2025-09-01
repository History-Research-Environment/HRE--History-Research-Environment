package hre.gui;
/************************************************************************************************
 * Manage Person - Specification 05.06 GUI_EntityEdit 2018-06-12
 * v0.01.0023 2020-08-09 first draft (D Ferguson)
 * 		      2020-09-07 HG0540Reminder store implemented (N. Tolleshaug)
 * 			  2020-09-30 fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2021-02-07 add Associates panel (D Ferguson)
 *            2021-02-14 add dummy Flag data to populate Flag area (D Ferguson)
 * v0.01.0026 2021-09-16 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2022-03-13 Added Name Style combobox and Copy/Del/Renum buttons (D Ferguson)
 * 			  2022-03-22 Add Other Names edit option and tab/screen (D Ferguson)
 * 			  2022-03-22 HG0506ManagePerson extends HG0451SuperIntFrame (N. Tolleshaug)
 * 			  2022-03-25 Loaded data for Event/Assocs/Name/Parents/Images;
 * 						 fix entry of Person # to edit; modify layout (D Ferguson)
 * 			  2022-03-30 Table cellEditable settings correct; action double-clicks (D Ferguson)
 * 			  2022-04-12 Pass person sex to updateRecentPerson list (D Ferguson)
 * 			  2022-06-03 Add 2nd right-click menu for Events (D Ferguson)
 * 			  2022-09-23 Added code to drive HG0509 to edit Person Name (D Ferguson)
 * 			  2022-09-26 Added code to drive HG0509 from Other Names table (D Ferguson)
 * v0.01.0028 2023-02-12 Add field for person's Sex (D Ferguson)
 * 			  2023-03-01 Add field for current Name Style (D Ferguson)
 * 			  2023-03-03 Add code to handle text exhibits and format media card (D Ferguson)
 * 			  2023-03-10 Add Caption handling into media card (N. Tolleshaug)
 * 			  2023-03-13 Reset all name table if updated "BEST_NAMES_RPID (N. Tolleshaug)
 * 			  2023-03-19 persName only responds to dbl-click, opens All Names card (D Ferguson)
 * 			  2023-03-23 If only Primary name, show HG0509 on persName dbl-click (D Ferguson)
 * v0.03.0030 2023-05-29 Add import/display of flag data (N.Tolleshaug)
 * 			  2023-06-06 Revised display and added code for flag change handling (D Ferguson)
 * 			  2023-07-02 Activated witness / children events on/off (N. Tolleshaug)
 * 			  2023-07-07 Present #marriages, #children, Reference (N. Tolleshaug)
 * 		  	  2023-07-11 Perform shading on witnessed/child events (D Ferguson)
 * 			  2023-07-13 Implement translated table headings (D Ferguson)
 * 			  2023-07-22 Update flag value for Person Manager (N. Tolleshaug)
 * 			  2023-08-05 NLS all code (D Ferguson)
 * v0.03.0030 2023-09-08 Add icons for missing image/text media (D Ferguson)
 * 			  2023-09-10 Activated delete person button (N. Tolleshaug)
 * 			  2023-09-13 Add scrolling to media card (D Ferguson)
 * 			  2023-09-14 Dispose after successful delete person(N. Tolleshaug)
 * 			  2023-09-20 Fix Parents table minimum height issue (D Ferguson)
 * 			  2023-10-03 Pass correct Sex value to Main menu Recent Person list (D Ferguson)
 * 			  2023-10-03 Add buttons for adding media to media card (D Ferguson)
 * 			  2023-10-04 Only add to Recent Person list if we are the current project (D Ferguson)
 * v0.03.0031 2023-12-09 Implement F4; revise all event edit GUI positions (D Ferguson)
 * 			  2023-12-30 ERROR fix Activate Person Manager after delete person (N Tolleshaug)
 * 			  2024-01-01 Add deletion from recents person list on person delete (D Ferguson)
 * 			  2024-03-01 Add parents table right-click popupmenu (D Ferguson)
 * 			  2024-03-05 Add confirmation prompts for delete actions (D Ferguson)
 * 			  2024-03-16 Add minimal PersonSelector for Add Parent (D Ferguson)
 * 			  2024-03-22 New handling of date, location and memo (N. Tolleshaug)
 * 			  2024-04-01 Added activation of delete partner (N. Tolleshaug)
 * 			  2024-04-24 Add Surety fields (D Ferguson)
 * 			  2024-04-25 Add filters to Associates table (D Ferguson)
 * 			  2024-04-28 Move Copy/Delete/Renum buttons to toolbar; add Partner radio-button (D Ferguson)
 * 			  2024-04-28 Added activation of partner table (N. Tolleshaug)
 * 			  2024-06-14 Add popup menu to 'All Names' for Name add/edit/del actions (D Ferguson)
 * 			  2024-08-18 Update NLS for all new entries (D Ferguson)
 * 			  2024-08-29 Modify Add Partner prompt to cover all cases (D Ferguson)
 * 			  2024-10-03 Modified popup action edit partner for error message (N. Tolleshaug)
 * 			  2024-10-05 Modified add partner event and reset PS (N. Tolleshaug)
 * 			  2024-10-06 NLS cleanup (D Ferguson)
 * 			  2024-10-29 Modified public static final String screenID = "50600"; (N. Tolleshaug)
 * 			  2024-11-10 Add confirmation prompts for delete of partner, parent (D Ferguson)
 * 			  2024-11-15 Removed 'null' positioning of all JOptionPane msgs (D Ferguson)
 * v0.04.0032 2025-01-11 Rearranged processing of event and roles (N. Tolleshaug)
 * 			  2025-03-17 Removed person Surety; adjust Parent Surety column size (D Ferguson)
 * 		 	  2025-03-20 Modify top of screen to enable primary Image to be shown (D Ferguson)
 * 			  2025-04-25 Fix parent teble popup menu structure; correct picPanel size (D Ferguson)
 * 			  2025-04-26 Setup NLS of null parent table data (D Ferguson)
 * 			  2025-06-30 Make all double-click actions consistent (D Ferguson)
 *			  2025-07-13 Handle passing of sexCode through to HG0547 (D Ferguson)
 ***********************************************************************************************
 * NOTES for incomplete functionality:
 * NOTE06 need listener and code for handling Notepads
 * NOTE07 need listener and code for handling DNA data
 * NOTE09 need to load new audio/video media or delete existing
 * NOTE15 need copy/renumber actions added
 *********************************************************************************************/

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.NumberFormatter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBMediaHandler;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0506Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Person
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2020-08-09
 */
public class HG0506ManagePerson extends HG0451SuperIntFrame {
	private static final long serialVersionUID = 001L;
	public static final String screenID = "50600";	//$NON-NLS-1$
	private String className;
	protected HBPersonHandler pointPersonHandler;
    protected HBProjectOpenData pointOpenProject;
    protected HBWhereWhenHandler pointHBWhereWhenHandler;
	protected JInternalFrame personManagerFrame = this;
	private JPanel contents;

	private JRadioButton radio_Names;
	private int rowClicked;
	public Long personPID;

	private String sexCode = "";	//$NON-NLS-1$
	private String sexValues;
	private String sexMaleValue;
	private String sexFemValue;
	private boolean closeAfterDelete = false;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	JCheckBox chkbox_Filter;
	JComboBox<String> comboBox_Subset;
	JTable tableAssocs;
    JScrollPane assocScrollPane;
	JTable tablePartners;
	JTable tableParents;
    JScrollPane partnerScrollPane;

/**
 * Objects holding card/panel data
 */
	String[] eventTableHeader;
	Object[][] objEventData;
	Object[][] objPartnerData;
	Object[][] objAssocData;
	Object[][] objNameData;
	Object[][] objAllFlagData;
	Object[][] objReqFlagData;

	String[] tableNameHeader;
	Object[][] objParentData;
	boolean canOnlyAddParent = false;
	Object[][] objSelfData;
	ArrayList<ImageIcon> listImages;
	ArrayList<String> listImagesCaptions;
	ArrayList<String> listTexts;

	String[] tableHeaderForPartnerAndAssocs;
	JTable tableNames;
	// Define items for external reference
    JMenuItem popMenu0;
    JMenuItem popMenuNamesEdit;
	DCTextField persName;

	JLabel  nameStyle;
	String[] styleNameDates;

	// Icons for media display
	Icon peopleIcon = new ImageIcon(getClass().getResource("/hre/images/people-48.png")); //$NON-NLS-1$
    Icon bookIcon = new ImageIcon(getClass().getResource("/hre/images/book-48.png")); //$NON-NLS-1$
    Icon audioIcon = new ImageIcon(getClass().getResource("/hre/images/audio-48.png")); //$NON-NLS-1$
    Icon videoIcon = new ImageIcon(getClass().getResource("/hre/images/video-48.png")); //$NON-NLS-1$

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

/**
 *  resetAllNametable();
 */
	public void resetAllNametable(HBProjectOpenData pointOpenProject) {
		objNameData = pointPersonHandler.getManagedNameTable();
		DefaultTableModel persNameModel = (DefaultTableModel) tableNames.getModel();
		pointPersonHandler = pointOpenProject.getPersonHandler();
		styleNameDates = pointPersonHandler.getManagedStyleNameAndDates();
		nameStyle.setText(styleNameDates[0]);
		persNameModel.setDataVector(objNameData, tableNameHeader);
		persName.setText(pointPersonHandler.getManagedPersonName());
	}

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0506ManagePerson(HBPersonHandler pointPersonHand
							, HBProjectOpenData pointOpenProject
							, String screenID
							, Long personPID) throws HBException {
		super(pointPersonHand,"Manage Person",true,true,true,true);	 //$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0506ManagePerson");}	//$NON-NLS-1$

	// Setup special toolbar buttons for this screen
		JButton btn_CopyIcon = new JButton(new ImageIcon(getClass().getResource("/hre/images/copyperson_BW_24.png"))); //$NON-NLS-1$
		btn_CopyIcon.setToolTipText(HG0506Msgs.Text_18);	// Copy this Person to a new Person entry
		btn_CopyIcon.setMinimumSize(new Dimension(24, 24));
		btn_CopyIcon.setMaximumSize(new Dimension(24, 24));

		JButton btn_DeleteIcon = new JButton(new ImageIcon(getClass().getResource("/hre/images/deleteperson_BW_24.png"))); //$NON-NLS-1$
		btn_DeleteIcon.setToolTipText(HG0506Msgs.Text_19);		// Delete this Person
		btn_DeleteIcon.setMinimumSize(new Dimension(24, 24));
		btn_DeleteIcon.setMaximumSize(new Dimension(24, 24));

		JButton btn_RenumIcon = new JButton(new ImageIcon(getClass().getResource("/hre/images/renumperson_BW_24.png"))); //$NON-NLS-1$
		btn_RenumIcon.setToolTipText(HG0506Msgs.Text_20);		// Renumber this Person
		btn_RenumIcon.setMinimumSize(new Dimension(24, 24));
		btn_RenumIcon.setMaximumSize(new Dimension(24, 24));

	// Setup all references for HG0506ManagePerson
		windowID = screenID;
		helpName = "manageperson";	//$NON-NLS-1$
    	this.pointOpenProject = pointOpenProject;
		this.personPID = personPID;
    	className = getClass().getSimpleName();
    	this.setResizable(true);
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		styleNameDates = pointPersonHand.getManagedStyleNameAndDates();
		HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
		listImages = pointMediaHandler.getImageList();
		listImagesCaptions = pointMediaHandler.getImageCaptionList();

	// Get table headers from T204
		tableNameHeader = pointPersonHandler.setTranslatedData(screenID, "4", false);	//$NON-NLS-1$   // Alternate Type, Names, Date
		tableHeaderForPartnerAndAssocs = pointPersonHandler.setTranslatedData(screenID, "3", false);	//$NON-NLS-1$	// Person, Role, Event, Date
	    setTitle(HG0506Msgs.Text_1 + pointOpenProject.getProjectName());	// Manage Person in Project

	// Enable the Add other Persons main menu items
	    HG0401HREMain.mainFrame.enableRelatives();

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[grow]", "[grow]10[grow]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add our menu icons and HG0451 icons
    	toolBar.add(btn_CopyIcon);
    	toolBar.add(btn_DeleteIcon);
    	toolBar.add(btn_RenumIcon);
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

		// Setup top area of Main panel for picture and Name/Parent detail
		JPanel picPanel = new JPanel();
		picPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(picPanel, "cell 0 0, grow");	//$NON-NLS-1$
		picPanel.setLayout(new MigLayout("fill, hidemode 3, insets 0", "[]", "[center]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		picPanel.setVisible(false);
		// NB: we have to leave populating this panel until AFTER the frame is made visible to get the size right

		JPanel nameParentPanel = new JPanel();
		nameParentPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(nameParentPanel, "cell 1 0, grow");	//$NON-NLS-1$
		nameParentPanel.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]5[]5[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Ident = new JLabel(HG0506Msgs.Text_2);		// Identity:
		nameParentPanel.add(lbl_Ident, "cell 0 0, align right");	//$NON-NLS-1$
		persName = new DCTextField(pointPersonHandler.getManagedPersonName());
		nameParentPanel.add(persName, "cell 1 0, growx");	//$NON-NLS-1$

		JLabel lbl_NStyle = new JLabel(HG0506Msgs.Text_3);		// Style:
		nameParentPanel.add(lbl_NStyle, "cell 1 0, align right, gapx 20");	//$NON-NLS-1$
		nameStyle = new JLabel (styleNameDates[0]);
		nameParentPanel.add(nameStyle, "cell 1 0");	//$NON-NLS-1$

		JLabel lbl_Reference = new JLabel(HG0506Msgs.Text_4);	// Reference:
		nameParentPanel.add(lbl_Reference, "cell 0 1, align right");	//$NON-NLS-1$
		JTextField reference = new JTextField(pointPersonHandler.getPersonReference());
		reference.setColumns(25);
		nameParentPanel.add(reference, "cell 1 1, growx");	//$NON-NLS-1$

		// HG0506Msgs.Text_50    (Surety: ) no longer used

		JLabel lbl_Sex = new JLabel(HG0506Msgs.Text_5);		// Birth Sex:
		nameParentPanel.add(lbl_Sex, "cell 1 1, align right, gapx 25");	//$NON-NLS-1$
		JLabel birthSex = new JLabel();
		nameParentPanel.add(birthSex, "cell 1 1");	//$NON-NLS-1$

		int partnerCount = pointPersonHandler.getNumberOfMarriages();
		JLabel lbl_Partners = new JLabel(HG0506Msgs.Text_6 + partnerCount);		// # Partners =
		nameParentPanel.add(lbl_Partners, "cell 1 1, gapx 25");	//$NON-NLS-1$

		int childCount = pointPersonHandler.getNumberOfCildren();
		JLabel lbl_Children = new JLabel(HG0506Msgs.Text_7 + childCount);		// # Children =
		nameParentPanel.add(lbl_Children, "cell 1 1, gapx 25");	//$NON-NLS-1$

		JLabel lbl_Parents = new JLabel(HG0506Msgs.Text_8);		// Parents:
		nameParentPanel.add(lbl_Parents, "cell 0 2, align right");	//$NON-NLS-1$

		tableParents = new JTable() {
			private static final long serialVersionUID = 1L;
		 		@Override
				public Dimension getPreferredScrollableViewportSize() {
					   return new Dimension(super.getPreferredSize().width,
					    	super.getRowCount() * (super.getRowHeight()+5));
				}
		 		@Override
				public boolean isCellEditable(int row, int column) {return false;}
		 	};
		objParentData = pointPersonHandler.getManagedParentTable();
		// Pre-process the Parent data to check for 2 unknown parents, in which case set
		// the 'Father', 'Mother', 'Not recorded' text in correct language
		if (objParentData.length == 2) {
			if (((String) objParentData[0][1]).trim().equals("---") && ((String) objParentData[1][1]).trim().equals("---")) { //$NON-NLS-1$ //$NON-NLS-2$
				objParentData[0][0] = HG0506Msgs.Text_87;		// Father
				objParentData[1][0] = HG0506Msgs.Text_88;		// Mother
				objParentData[0][1] = HG0506Msgs.Text_89;		// Not recorded
				objParentData[1][1] = HG0506Msgs.Text_89;		// Not recorded
				canOnlyAddParent= true;
			}
		}
		tableParents.setModel(new DefaultTableModel(objParentData,
									pointPersonHandler.setTranslatedData(screenID, "1", false)	//$NON-NLS-1$  // Role, Name, Surety
				 			));
		tableParents.getColumnModel().getColumn(0).setMinWidth(70);
		tableParents.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableParents.getColumnModel().getColumn(1).setMinWidth(200);
		tableParents.getColumnModel().getColumn(1).setPreferredWidth(440);
		tableParents.getColumnModel().getColumn(2).setMinWidth(50);
		tableParents.getColumnModel().getColumn(2).setPreferredWidth(50);
		JTableHeader tHdParents = tableParents.getTableHeader();
		tHdParents.setOpaque(false);
		tableParents.setFillsViewportHeight(true);
		// Table may need to expand for Father/Mother-adopt or -foster but minimum 2 rows
		int nProws = tableParents.getRowCount();
		if (nProws < 2) nProws = 2;

		// Calculate pane Height based on #rows*(row height + divider height) + header heights
		int height = (nProws+1)*(tableParents.getRowHeight()+1) + tableParents.getTableHeader().getHeight()+2;
		JScrollPane scrollParents = new JScrollPane(tableParents);
		scrollParents.setMinimumSize(new Dimension(650, height));
		tableParents.setPreferredScrollableViewportSize(new Dimension(650, height));
		nameParentPanel.add(scrollParents, "cell 1 2, grow");  	//$NON-NLS-1$

		// Binding to allow F4 (show tag type list) to be recognised
	    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	    ActionMap actionMap = rootPane.getActionMap();
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "F4");	//$NON-NLS-1$
	    actionMap.put("F4", new AbstractAction() {						//$NON-NLS-1$
	    	private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg4) {
	        	// Show ManageEvent screen to allow event add
	        	popMenu0.doClick();
	         	}
	    });

/**************************************
 * Setup left controlPanel and its contents
 **************************************/
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(controlPanel, "cell 0 1, growx, aligny top");	//$NON-NLS-1$
		controlPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[]10[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EditType = new JLabel(HG0506Msgs.Text_9);		// Edit Category
		controlPanel.add(lbl_EditType, "cell 0 0, alignx center");	//$NON-NLS-1$

		JRadioButton radio_Event = new JRadioButton(HG0506Msgs.Text_10);	// Events
		radio_Event.setSelected(true);
		controlPanel.add(radio_Event, "cell 0 1, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Partners = new JRadioButton(HG0506Msgs.Text_51);		// Partners
		controlPanel.add(radio_Partners, "cell 0 2, alignx left");	//$NON-NLS-1$

		JRadioButton radio_Assocs = new JRadioButton(HG0506Msgs.Text_11);		// Associates
		controlPanel.add(radio_Assocs, "cell 0 3, alignx left");	//$NON-NLS-1$

		radio_Names = new JRadioButton(HG0506Msgs.Text_12);		// All Names
		controlPanel.add(radio_Names, "cell 0 4, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Flag = new JRadioButton(HG0506Msgs.Text_13);		// Flags
		controlPanel.add(radio_Flag, "cell 0 5, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Media = new JRadioButton(HG0506Msgs.Text_14);		// Media
		controlPanel.add(radio_Media, "cell 0 6, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Note = new JRadioButton(HG0506Msgs.Text_15);		// Notepads
		controlPanel.add(radio_Note, "cell 0 7, alignx left");		//$NON-NLS-1$

		JRadioButton radio_DNA = new JRadioButton("DNA");			//$NON-NLS-1$
		controlPanel.add(radio_DNA, "cell 0 8, alignx left");		//$NON-NLS-1$

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radio_Event);
		radioGroup.add(radio_Partners);
		radioGroup.add(radio_Assocs);
		radioGroup.add(radio_Names);
		radioGroup.add(radio_Flag);
		radioGroup.add(radio_Media);
		radioGroup.add(radio_Note);
		radioGroup.add(radio_DNA);

/**************************************
 * Setup leftBotmPanel and its contents
 **************************************/
		JPanel leftBotmPanel = new JPanel();
		leftBotmPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(leftBotmPanel, "cell 0 2, growx, aligny bottom");	//$NON-NLS-1$
		leftBotmPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]10"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lblPerPrompt = new JLabel(HG0506Msgs.Text_16);		// Go to Person #:
		leftBotmPanel.add(lblPerPrompt, "cell 0 0, alignx center");	//$NON-NLS-1$

	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(5000000);
	    JFormattedTextField persNum = new JFormattedTextField(formatter);
	    persNum.setColumns(7);
	    persNum.setHorizontalAlignment(SwingConstants.CENTER);
		persNum.setToolTipText(HG0506Msgs.Text_17);		// Enter ID # of Person to be edited
		leftBotmPanel.add(persNum, "cell 0 1, alignx center");		//$NON-NLS-1$

/***************************************************
 * Setup rightPane and a CardLayout of Panels in it
 **************************************************/
		JPanel rightPanel = new JPanel(new CardLayout());
		rightPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(rightPanel, "cell 1 1 2 2, grow");	//$NON-NLS-1$

		// Define cards of the CardLayout, each card-Panel with its own layout manager
		JPanel cardEvents = new JPanel();
		cardEvents.setLayout(new MigLayout("", "[grow]", "[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardEvents, "EVENTS");	//$NON-NLS-1$
		JPanel cardPartners = new JPanel();
		cardPartners.setLayout(new MigLayout("", "[grow]", "[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardPartners, "PARTNERS");	//$NON-NLS-1$
		JPanel cardAssocs = new JPanel();
		cardAssocs.setLayout(new MigLayout("", "[grow]", "[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardAssocs, "ASSOCIATES");	//$NON-NLS-1$
		JPanel cardNames = new JPanel();
		cardNames.setLayout(new MigLayout("", "[grow]", "[grow]"));		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardNames, "NAMES");	//$NON-NLS-1$
		JPanel cardFlags = new JPanel();
		cardFlags.setLayout(new MigLayout("", "[]10[]", "[grow]"));		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardFlags, "FLAGS");		//$NON-NLS-1$
		JPanel cardMedia = new JPanel();
		cardMedia.setLayout(new MigLayout("", "[]", "[]20[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardMedia, "MEDIA");	//$NON-NLS-1$
		JPanel cardNotepads = new JPanel();
		cardNotepads.setLayout(new MigLayout("", "[grow]", "[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardNotepads, "NOTEPADS");	//$NON-NLS-1$
		JPanel cardDNA = new JPanel();
		cardDNA.setLayout(new MigLayout("", "[grow]", "[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardDNA, "DNA");	//$NON-NLS-1$

/*************
 * cardEvents - setup a scrollPane and table of Events
 *************/
		JCheckBox checkWitness = new JCheckBox(HG0506Msgs.Text_21);			// Include Witnessed Events
		checkWitness.setSelected(true);
		checkWitness.setHorizontalTextPosition(SwingConstants.TRAILING);
		checkWitness.setHorizontalAlignment(SwingConstants.RIGHT);
		JCheckBox checkChildren = new JCheckBox(HG0506Msgs.Text_22);		// Show Children
		checkChildren.setSelected(true);
		checkChildren.setHorizontalTextPosition(SwingConstants.TRAILING);
		checkChildren.setHorizontalAlignment(SwingConstants.RIGHT);
		cardEvents.add(checkWitness, "cell 0 0");	//$NON-NLS-1$
		cardEvents.add(checkChildren, "cell 0 0, gapx 20");	//$NON-NLS-1$

		// Create table for the Event Tags
		eventTableHeader = pointPersonHandler.setTranslatedData(screenID, "2", false);  //$NON-NLS-1$  // Event, Date, Role, Location or Name, Age
		objEventData = pointPersonHandler.getManagedEventTable();
		DefaultTableModel eventsModel = new DefaultTableModel(objEventData, eventTableHeader);
		JTable tableEvents = new JTable(eventsModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredScrollableViewportSize() {
			    return new Dimension(super.getPreferredSize().width,
			    		super.getPreferredScrollableViewportSize().height);
			  }
			// Make all columns non-editable
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
			// Refer to objEventData col5 for event code, where W=witnessed event, C=child
			@Override
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int col)  {
			        	Component c = super.prepareRenderer(renderer, row, col);
		        		c.setBackground(getBackground());
		        		c.setForeground(getForeground());
			        // COLORS: LIGHT_GRAY: java.awt.Color[r=192, g=192, b=192]
			        //               GRAY: java.awt.Color[r=128, g=128, b=128]
			        //			DARK_GRAY: java.awt.Color[r=64,  g=64,  b=64]
		        		Color verylightGray = new Color(224, 224, 224);

			        	if (!isRowSelected(row)) {
			        		// Make a witnessed event background slightly darker
				        	if(objEventData[row][5].equals("W")) 	 //$NON-NLS-1$
								c.setBackground(verylightGray);
				        	// Make a child event White on darker background
				            if(objEventData[row][5].equals("C")) {	//$NON-NLS-1$
				            	c.setBackground(getBackground().darker().darker());
				            	c.setForeground(Color.WHITE);
				            }
			        	}
			        return c;
			    }
		};

		tableEvents.setFillsViewportHeight(true);
		tableEvents.getColumnModel().getColumn(0).setMinWidth(80);
		tableEvents.getColumnModel().getColumn(0).setPreferredWidth(120);		// Event
		tableEvents.getColumnModel().getColumn(1).setMinWidth(100);
		tableEvents.getColumnModel().getColumn(1).setPreferredWidth(120);		// Date
		tableEvents.getColumnModel().getColumn(2).setMinWidth(100);
		tableEvents.getColumnModel().getColumn(2).setPreferredWidth(120);		// Role
		tableEvents.getColumnModel().getColumn(3).setMinWidth(100);
		tableEvents.getColumnModel().getColumn(3).setPreferredWidth(300);		// Location
		tableEvents.getColumnModel().getColumn(4).setMinWidth(40);
		tableEvents.getColumnModel().getColumn(4).setPreferredWidth(40);		// Age
		// Center Age column
        DefaultTableCellRenderer centerRendererE = new DefaultTableCellRenderer();
        centerRendererE.setHorizontalAlignment( SwingConstants.CENTER );
        tableEvents.getColumnModel().getColumn(4).setCellRenderer(centerRendererE);
	    // Set header format
		JTableHeader eventHeader = tableEvents.getTableHeader();
		eventHeader.setOpaque(false);
		TableCellRenderer evrendererFromHeader = tableEvents.getTableHeader().getDefaultRenderer();
		JLabel evheaderLabel = (JLabel) evrendererFromHeader;
		evheaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel eventSelectionModel = tableEvents.getSelectionModel();
		eventSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Add table of Events to the eventScrollPane
		JScrollPane eventScrollPane = new JScrollPane(tableEvents);
		cardEvents.add(eventScrollPane, "cell 0 1, grow");	//$NON-NLS-1$

/**************
 * cardPartners - setup a scrollPane and table for Partners
 *************/
	// Define and populate table of Partners
		tablePartners = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getPreferredScrollableViewportSize().height);
				  }
				@Override
				public boolean isCellEditable(int row, int column) {return false;}
			};
		tablePartners.setFillsViewportHeight(true);

		// Create table for the Partner Tags
		objPartnerData = pointPersonHandler.getManagedPartnerTable();

		tablePartners.setModel(new DefaultTableModel(objPartnerData, tableHeaderForPartnerAndAssocs));
		tablePartners.getColumnModel().getColumn(0).setMinWidth(100);
		tablePartners.getColumnModel().getColumn(0).setPreferredWidth(300);
		tablePartners.getColumnModel().getColumn(1).setMinWidth(50);
		tablePartners.getColumnModel().getColumn(1).setPreferredWidth(150);
		tablePartners.getColumnModel().getColumn(2).setMinWidth(50);
		tablePartners.getColumnModel().getColumn(2).setPreferredWidth(130);
		tablePartners.getColumnModel().getColumn(3).setMinWidth(80);
		tablePartners.getColumnModel().getColumn(3).setPreferredWidth(120);

		// Set the ability to sort on columns, but no presort
		tablePartners.setAutoCreateRowSorter(true);
	    TableRowSorter<TableModel> sortPartner = new TableRowSorter<>(tablePartners.getModel());
	    tablePartners.setRowSorter(sortPartner);

	    // Set tooltips and header format
		tablePartners.getTableHeader().setToolTipText(HG0506Msgs.Text_23); // Click to sort; Click again to sort in reverse order
		JTableHeader partnerHeader = tablePartners.getTableHeader();
		partnerHeader.setOpaque(false);
		TableCellRenderer paRendererFromHeader = tablePartners.getTableHeader().getDefaultRenderer();
		JLabel paHeaderLabel = (JLabel) paRendererFromHeader;
		paHeaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel partnerSelectionModel = tablePartners.getSelectionModel();
		partnerSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Add table of Partners to the partnerScrollPane
		partnerScrollPane = new JScrollPane(tablePartners);
		cardPartners.add(partnerScrollPane, "cell 0 0, grow");	//$NON-NLS-1$

/************
 * cardAssocs - setup a scrollPane and table for Associates
 ***********/
		JLabel lbl_TxtFilter = new JLabel(HG0506Msgs.Text_52);			// Text to filter for:
		cardAssocs.add(lbl_TxtFilter, "cell 0 0, alignx left"); //$NON-NLS-1$

		JTextField filterTextField = new JTextField();
		filterTextField.setColumns(15);
		cardAssocs.add(filterTextField, "flowx,cell 0 0"); //$NON-NLS-1$

		JLabel lbl_Subsets = new JLabel(HG0506Msgs.Text_53);			// Select:
		lbl_Subsets.setToolTipText(HG0506Msgs.Text_54);	// Select filter or Subset name
		cardAssocs.add(lbl_Subsets, "cell 0 0, gapx 20"); //$NON-NLS-1$

		comboBox_Subset = new JComboBox<>();
		comboBox_Subset.setPreferredSize(new Dimension(170, 20));
		comboBox_Subset.setToolTipText(HG0506Msgs.Text_55);	// List of saved filter and subset names
		for (String tableHeaderForPartnerAndAssoc : tableHeaderForPartnerAndAssocs) {
			comboBox_Subset.addItem(" " + tableHeaderForPartnerAndAssoc);		//$NON-NLS-1$
		}
		comboBox_Subset.addItem(" " + HG0506Msgs.Text_56);		//$NON-NLS-1$	// All Columns
		cardAssocs.add(comboBox_Subset, "cell 0 0"); //$NON-NLS-1$

		chkbox_Filter = new JCheckBox(HG0506Msgs.Text_57);	// Filter
		chkbox_Filter.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_Filter.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_Filter.setToolTipText(HG0506Msgs.Text_58);	// On if a filter in use
		cardAssocs.add(chkbox_Filter, "cell 0 0,gapx 20"); //$NON-NLS-1$

	// Define and populate table of Associates
		tableAssocs = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getPreferredScrollableViewportSize().height);
				  }
				@Override
				public boolean isCellEditable(int row, int column) {return false;}
			};
		tableAssocs.setFillsViewportHeight(true);
	// Create table for the Assoc Tags
		objAssocData = pointPersonHandler.getManagedAssociateTable();

		tableAssocs.setModel(new DefaultTableModel(objAssocData, tableHeaderForPartnerAndAssocs));
		tableAssocs.getColumnModel().getColumn(0).setMinWidth(100);
		tableAssocs.getColumnModel().getColumn(0).setPreferredWidth(300);
		tableAssocs.getColumnModel().getColumn(1).setMinWidth(50);
		tableAssocs.getColumnModel().getColumn(1).setPreferredWidth(150);
		tableAssocs.getColumnModel().getColumn(2).setMinWidth(50);
		tableAssocs.getColumnModel().getColumn(2).setPreferredWidth(130);
		tableAssocs.getColumnModel().getColumn(3).setMinWidth(80);
		tableAssocs.getColumnModel().getColumn(3).setPreferredWidth(120);

		// Set the ability to sort on columns, but no presort
		tableAssocs.setAutoCreateRowSorter(true);
	    TableRowSorter<TableModel> sortAssoc = new TableRowSorter<>(tableAssocs.getModel());
	    tableAssocs.setRowSorter(sortAssoc);

	    // Set tooltips and header format
		tableAssocs.getTableHeader().setToolTipText(HG0506Msgs.Text_23); // Click to sort; Click again to sort in reverse order
		JTableHeader assocHeader = tableAssocs.getTableHeader();
		assocHeader.setOpaque(false);
		TableCellRenderer asRendererFromHeader = tableAssocs.getTableHeader().getDefaultRenderer();
		JLabel asHeaderLabel = (JLabel) asRendererFromHeader;
		asHeaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel assocSelectionModel = tableAssocs.getSelectionModel();
		assocSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Add table of Assocs to the assocScrollPane
		assocScrollPane = new JScrollPane(tableAssocs);
		cardAssocs.add(assocScrollPane, "cell 0 1, grow");	//$NON-NLS-1$

/************
 * cardNames - setup a scrollPane and table for All Names
 ************/
	//JTable tableNames = new JTable() {
		tableNames = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getPreferredScrollableViewportSize().height);
				  }
				@Override
				public boolean isCellEditable(int row, int column) {return false;}
			};

		tableNames.setFillsViewportHeight(true);
		// Create table for the Name Tags
		objNameData = pointPersonHandler.getManagedNameTable();
		tableNames.setModel(new DefaultTableModel(objNameData,
												  tableNameHeader));	// Alternate Type, Names, Date
		tableNames.getColumnModel().getColumn(0).setMinWidth(80);
		tableNames.getColumnModel().getColumn(0).setPreferredWidth(130);
		tableNames.getColumnModel().getColumn(1).setMinWidth(170);
		tableNames.getColumnModel().getColumn(1).setPreferredWidth(450);
		tableNames.getColumnModel().getColumn(2).setMinWidth(80);
		tableNames.getColumnModel().getColumn(2).setPreferredWidth(120);
		tableNames.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableNames.setAutoCreateColumnsFromModel(false);	// preserve column setup
		// Set the ability to sort on columns but no presort
		tableNames.setAutoCreateRowSorter(true);
	    TableRowSorter<TableModel> sortName = new TableRowSorter<>(tableNames.getModel());
	    tableNames.setRowSorter(sortName);
	    // Set tooltips and header format
		tableNames.getTableHeader().setToolTipText(HG0506Msgs.Text_23);  // Click to sort; Click again to sort in reverse order
		JTableHeader nameHeader = tableNames.getTableHeader();
		nameHeader.setOpaque(false);
		TableCellRenderer naRendererFromHeader = tableNames.getTableHeader().getDefaultRenderer();
		JLabel naHeaderLabel = (JLabel) naRendererFromHeader;
		naHeaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel nameSelectionModel = tableNames.getSelectionModel();
		nameSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Add table of Names to the nameScrollPane
		JScrollPane nameScrollPane = new JScrollPane(tableNames);
		cardNames.add(nameScrollPane, "cell 0 0, grow");	//$NON-NLS-1$

/************
 * cardFlags - setup a table of Person's Flags
 ************/
		// Get all of the person's current flag data. Table columns are:
		// 0 - IS_SYSTEM?; 1 - ACTIVE?; 2 - GUI_SEQ; 3 - FLAG_ID; 4 - DEFAULT_INDEX; 5 - SETTING;
	    // 6 - LANG_CODE; 7 - FLAG_NAME; 8 - FLAG_VALUES; 9 - FLAG_DESC
		objAllFlagData = pointPersonHandler.getManagedFlagTable();
		// and sort on GUI-sequence (column 2)
		Arrays.sort(objAllFlagData, (o1, o2) -> Integer.compare((Integer) o1[2], (Integer) o2[2]));

		// Now count the number of Active Flags to be able to set size of objReqFlagData
		int activeFlags = 0;
		for (Object[] element : objAllFlagData) {
			if ((boolean) (element[1])) activeFlags++;
		}

		// Now build only the Active Flag data into objReqFlagData. Required table columns are:
		// 0 - FLAG_NAME; 1 - SETTING; 2 - FLAG_DESC; 3 - FLAG_ID; 4 - Flag values
		// Display columns 0,1 in table, col 2 in textarea, keep col 3 to identify flag changes, col 4 to get values
		objReqFlagData = new Object[activeFlags][5];
		int reqRow = 0;
		for (Object[] element : objAllFlagData) {
			if ((boolean) (element[1])) {
				objReqFlagData[reqRow][0] = element[7];	// Flag name
				objReqFlagData[reqRow][1] = element[5];	// Set value
				objReqFlagData[reqRow][2] = element[9];	// Flag Description
				objReqFlagData[reqRow][3] = element[3];	// Flag ID
				objReqFlagData[reqRow][4] = element[8];	// All possible Flag values
				reqRow++;
			}
		}

		// Find FlagID=1 (Birth Sex) and set the Birth text entry
		for (Object[] element : objReqFlagData) {
			if ((int) element[3] == 1) {
				birthSex.setText((String) element[1]);	// get Sex word for this language into GUI
				sexValues = (String) element[4];			// and save the Sex values list
			}
		}

		// Now we need to know if this Sex setting means Male or Female
		// we know the Flag values for Sex must be in the order of ?,F,M, so check the
		// Sex setting algainst the Sex values for this language to find a match. We
		// can then set sexCode to M or F for use in Main menu Recent Person list.
		String[] splitSexValues = sexValues.split(",");	//$NON-NLS-1$
		sexMaleValue = splitSexValues[2];
		sexFemValue = splitSexValues[1];
		if (birthSex.getText().equals(sexMaleValue)) sexCode = "M";	//$NON-NLS-1$
		if (birthSex.getText().equals(sexFemValue)) sexCode = "F";	//$NON-NLS-1$

		// Define the flag table to be displayed - needs comboboxes of flag data
		// in 2nd column, initialised with the possible and actual setting of each flag
		DefaultTableModel flagModel = new DefaultTableModel(objReqFlagData,
				pointPersonHandler.setTranslatedData(screenID, "5", false));	//$NON-NLS-1$   // Flag, Setting
		JTable tableFlags = new JTable(flagModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredScrollableViewportSize() {
			    return new Dimension(super.getPreferredSize().width,
			    		super.getPreferredScrollableViewportSize().height);
			  }
			// Make only col 1 of the table editable (the flag setting)
			@Override
			public boolean isCellEditable(int row, int col) {
				if (col == 1) return true;
				return false;
			}
			// Now change the Flag Settings to comboboxes of the Flag Values, so user can reset flags
			@Override
			public TableCellEditor getCellEditor(int row, int col) {
                if (col == 1)  {
                	String flagPossibles = (String) objReqFlagData[row][4];	// all possible flag values
                	String[] flagList = flagPossibles.split(",");  			//$NON-NLS-1$
            		DefaultComboBoxModel<String> comboFlagModel = new DefaultComboBoxModel<>(flagList);
            		JComboBox<String> comboBoxFlag = new JComboBox<>(comboFlagModel);
                    return new DefaultCellEditor( comboBoxFlag );
                	}
				return super.getCellEditor(row, col);
            }
		};
		tableFlags.setFillsViewportHeight(true);
		tableFlags.getColumnModel().getColumn(0).setMinWidth(100);
		tableFlags.getColumnModel().getColumn(0).setPreferredWidth(200);	// Flag name
		tableFlags.getColumnModel().getColumn(1).setMinWidth(30);
		tableFlags.getColumnModel().getColumn(1).setPreferredWidth(80);		// Flag setting
	// Ensure cannot sort on columns as this would destroy Flag GUI-sequencing
		tableFlags.setAutoCreateRowSorter(false);
	// Centre column 1 data
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableFlags.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
	// Set header format
		JTableHeader flagHeader = tableFlags.getTableHeader();
		flagHeader.setOpaque(false);
		TableCellRenderer flrendererFromHeader = tableFlags.getTableHeader().getDefaultRenderer();
		JLabel flheaderLabel = (JLabel) flrendererFromHeader;
		flheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ListSelectionModel flagSelectionModel = tableFlags.getSelectionModel();
		flagSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Set 1st row selected and add to cardFlags
		tableFlags.setRowSelectionInterval(0,0);
		JScrollPane flagScrollPane = new JScrollPane(tableFlags);
		cardFlags.add(flagScrollPane, "cell 0 0,grow");	//$NON-NLS-1$

	// Define a heading and text area for Flag Description
		JButton flagDescHead = new JButton(HG0506Msgs.Text_0);	// Selected Flag's Description
		flagDescHead.setEnabled(false);							// disable the button
		cardFlags.add(flagDescHead, "flowy,cell 1 0,growx");	//$NON-NLS-1$

		JTextArea flagDescTxt = new JTextArea();
		flagDescTxt.setWrapStyleWord(true);
		flagDescTxt.setLineWrap(true);
		flagDescTxt.setEditable(false);		// only allow edit in FlagEditor
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$
	    flagDescTxt.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    flagDescTxt.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    flagDescTxt.setBorder(new JTable().getBorder());		// match Table border
	 // Load textarea with 1st Flag-Description
		flagDescTxt.setText((String) objReqFlagData[0][2]);
	// Setup scrollpane with textarea and add to cardFlags
		JScrollPane flagDescScroll = new JScrollPane(flagDescTxt);
		flagDescScroll.setPreferredSize(new Dimension(400, 22));
		flagDescScroll.getViewport().setOpaque(false);
		flagDescScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		flagDescTxt.setCaretPosition(0);	// set scrollbar to top
		cardFlags.add(flagDescScroll, "cell 1 0, grow");	//$NON-NLS-1$

/************
 * cardMedia - load data into the Media card
 ************/
	// Setup buttons to add media
		JButton btn_AddImage = new JButton(HG0506Msgs.Text_44);	// Add Image
		cardMedia.add(btn_AddImage, "cell 0 0");				//$NON-NLS-1$
		JButton btn_AddText = new JButton(HG0506Msgs.Text_45);	// Add Text file
		cardMedia.add(btn_AddText, "cell 0 0, gapx 30");		//$NON-NLS-1$
		JButton btn_AddAudio = new JButton(HG0506Msgs.Text_46);	// Add Audio file
		cardMedia.add(btn_AddAudio, "cell 0 0, gapx 30");		//$NON-NLS-1$
		JButton btn_AddVideo = new JButton(HG0506Msgs.Text_47);	// Add Video file
		cardMedia.add(btn_AddVideo, "cell 0 0, gapx 30");		//$NON-NLS-1$

	// Define a media JPanel
    	JPanel mediaPanel = new JPanel();
	// Add Person Images to mediaPanel
		if (HGlobal.DEBUG)
			System.out.println(" number of Images: " + pointMediaHandler.getNumberOfImages());	//$NON-NLS-1$

        if (listImages.size() > 0) {
            for (int i = 0; i < listImages.size(); i++) {
	    		JPanel imagePanel = new JPanel();
	    		imagePanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		imagePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	            JLabel image = new JLabel();
	            ImageIcon exhibitImage = listImages.get(i);
	        	if (exhibitImage != null) image = new JLabel(exhibitImage);
	    		imagePanel.add(image, "cell 0 0, alignx center");	//$NON-NLS-1$
	    	// Collect image captions
	    		JTextArea txt_Image = new JTextArea(listImagesCaptions.get(i));
	    		txt_Image.setLineWrap(true);
	    		txt_Image.setWrapStyleWord(true);
        		txt_Image.setEditable(false);
	    		txt_Image.setSize(200,22);
	    		int lines = txt_Image.getLineCount();
	    		if (lines > 5) lines = 5;
	    		JScrollPane captionPane = new JScrollPane(txt_Image);
	    		captionPane.setPreferredSize(new Dimension(200, 22*lines));
	    		imagePanel.add(captionPane, "cell 0 1");		//$NON-NLS-1$
	    		mediaPanel.add(imagePanel);
            }
        }
        else {	// Add people icon with 'not present' msg
        	JPanel imagePanel = new JPanel();
    		imagePanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		imagePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            JLabel image = new JLabel(peopleIcon);
    		imagePanel.add(image, "cell 0 0, alignx center");	//$NON-NLS-1$
    		JTextArea txt_Image = new JTextArea(HG0506Msgs.Text_42);		//   No Image files present
    		txt_Image.setLineWrap(true);
    		txt_Image.setWrapStyleWord(true);
    		txt_Image.setEditable(false);
    		txt_Image.setSize(150,22);
       		imagePanel.add(txt_Image, "cell 0 1");		//$NON-NLS-1$
    		mediaPanel.add(imagePanel);
        }

     // Add Text type files with text icon
        listTexts = pointMediaHandler.getTextList();
        if (listTexts.size() > 0) {
        	for (String listText : listTexts) {
        		JPanel textPanel = new JPanel();
        		textPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		textPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        		JLabel lbl_Text = new JLabel(bookIcon);
        		textPanel.add(lbl_Text, "cell 0 0, alignx center");	//$NON-NLS-1$
        		JTextArea txt_Text = new JTextArea();
        	// If text is NOT a Filepath string, enclose it in quotes
        		if (!listText.startsWith("Filepath:")) { //$NON-NLS-1$
					txt_Text = new JTextArea("\""+listText+" ...\"");	//$NON-NLS-1$ //$NON-NLS-2$
				} else txt_Text = new JTextArea(listText);

        		txt_Text.setLineWrap(true);
        		txt_Text.setWrapStyleWord(true);
        		txt_Text.setEditable(false);
        		txt_Text.setSize(250,22);
	    		JScrollPane textPane = new JScrollPane(txt_Text);
	    		textPane.setMinimumSize(new Dimension(250, 22*txt_Text.getLineCount() ));
        		textPanel.add(textPane, "cell 0 1");	//$NON-NLS-1$
	    		mediaPanel.add(textPanel);
        	}
        }
        else {	// Add text icon with 'not present' msg
       		JPanel textPanel = new JPanel();
    		textPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		textPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    		JLabel lbl_Text = new JLabel(bookIcon);
    		textPanel.add(lbl_Text, "cell 0 0, alignx center");	//$NON-NLS-1$
    		JTextArea txt_Text = new JTextArea(HG0506Msgs.Text_43);		//   No Text files present
    		txt_Text.setLineWrap(true);
    		txt_Text.setWrapStyleWord(true);
    		txt_Text.setEditable(false);
    		txt_Text.setSize(150,22);
       		textPanel.add(txt_Text, "cell 0 1");		//$NON-NLS-1$
    		mediaPanel.add(textPanel);
        }

     // Add dummy entries for Audio/video until MediaHandler can deliver them (NOTE09)
   		JPanel audioPanel = new JPanel();
		audioPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		audioPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Audio = new JLabel(audioIcon);
		audioPanel.add(lbl_Audio, "cell 0 0, alignx center");		//$NON-NLS-1$
		JTextArea txt_Audio = new JTextArea(HG0506Msgs.Text_25);		//   No Audio files present
		txt_Audio.setLineWrap(true);
		txt_Audio.setWrapStyleWord(true);
		txt_Audio.setEditable(false);
		txt_Audio.setSize(150,22);
		audioPanel.add(txt_Audio, "cell 0 1");		//$NON-NLS-1$
		mediaPanel.add(audioPanel);

   		JPanel videoPanel = new JPanel();
		videoPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		videoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Video = new JLabel(videoIcon);
		videoPanel.add(lbl_Video, "cell 0 0, alignx center");	//$NON-NLS-1$
		JTextArea txt_Video = new JTextArea(HG0506Msgs.Text_26);		//    No Video files present
		txt_Video.setLineWrap(true);
		txt_Video.setWrapStyleWord(true);
		txt_Video.setEditable(false);
		txt_Video.setSize(150,22);
		videoPanel.add(txt_Video, "cell 0 1");		//$NON-NLS-1$
		mediaPanel.add(videoPanel);

	// Define a scrollPane to hold the mediaPanel and size it
		JScrollPane mediaScrollPane = new JScrollPane(mediaPanel) {
			private static final long serialVersionUID = 001L;
	        @Override
	        public Dimension getPreferredSize() {
	        	int pref_width = tableEvents.getPreferredSize().width + 20;	// add room for insets
	            setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	            Dimension dim = new Dimension(pref_width, super.getPreferredSize().height + getHorizontalScrollBar().getSize().height);
	            return dim;
	        }
	    };
	 // Set the scrollPane to open at left (yes the only way to do this is via a runnable!)
	    SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            mediaScrollPane.getViewport().setViewPosition(new Point(0, 0));
	        }
	    });

	 // Finally add the mediaScrolling pane to the mediaCard
		cardMedia.add(mediaScrollPane, "cell 0 1");		//$NON-NLS-1$

/**************
 * cardNotepads - put comment into the Notepads card
 *************/
		// NOTE06 - real code to be done later
		JLabel lbl_Notepads = new JLabel("Notepads to be able to be loaded and edited here eventually");	//$NON-NLS-1$ (temporary code)
		cardNotepads.add(lbl_Notepads);
/***********
 * cardDNA - put comment into the DNA card
 **********/
		// NOTE07 - real code to be done later
		JLabel lbl_DNA = new JLabel("DNA to be able to be loaded and edited here eventually");	//$NON-NLS-1$ (temporary code)
		cardDNA.add(lbl_DNA);

// Display the screen
		pack();
		HG0401HREMain.mainPane.add(personManagerFrame);
		personManagerFrame.setVisible(true);
		setCursor(Cursor.getDefaultCursor());

		// Now that frame is visible the picPanel has a size (is 0,0 otherwise)
		// This allows us to scale the primary image to fit into picPanel as big as possible
		if (listImages != null && listImages.size() > 0) {
			picPanel.setVisible(true);
			JTextPane picPane = new JTextPane();
	        picPane.setEditable(false);
	       ImageIcon primaryPic = pointMediaHandler.getExhibitImage();
	       // Get picPanel height and adjust for borders
	       int picHeight = picPanel.getHeight() - 20;
	       // Scale primaryPic, show it, repack
	        ImageIcon newscaleImage = pointMediaHandler.scaleImage(picHeight, picHeight, primaryPic);
	        picPane.insertIcon(newscaleImage);
	        picPanel .add(picPane, "cell 0 0, alignx center");  //$NON-NLS-1$
	        pack();
		}

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen or any Closing event
		personManagerFrame.addInternalFrameListener(new InternalFrameAdapter() {
	    	 @Override
			public void internalFrameClosed(InternalFrameEvent c)  {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0506ManagePerson"); //$NON-NLS-1$

	    	// As we exit, update the Recently Accessed person list under the Person menu,
	    	// and disable the Add other Persons main menu items, BUT only if this ManagePerson
			// instance is running against the current project.
			// EXCEPT if we are closing due to Person's deletion, when we must bypass this action
				if (!closeAfterDelete ) {
					String selectedProject = HG0401HREMain.mainFrame.getSelectedOpenProject().getProjectName(); // name of selected project
					if (pointOpenProject.getProjectName().contentEquals(selectedProject)) {			// compare our project name with selected one
						HG0401HREMain.mainFrame.updateRecentPersons(persName.getText(), sexCode, personPID);	// update the recent list
						HG0401HREMain.mainFrame.disableRelatives();												// disable other Add menu items
					}
		    	 }
	    	 }
	    	 @Override
			public void internalFrameClosing(InternalFrameEvent c)  {
			// close reminder display
				if (reminderDisplay != null) reminderDisplay.dispose();

		    // Set frame size in GUI data
				Dimension frameSize = getSize();
				pointOpenProject.setSizeScreen(screenID,frameSize);

			// Set position	in GUI data
				Point position = getLocation();
				pointOpenProject.setPositionScreen(screenID,position);

			// Set class name in GUI configuration data
				pointOpenProject.setClassName(screenID,"HG0506ManagePerson"); //$NON-NLS-1$

			// Mark the screen as closed in T302
				pointOpenProject.closeStatusScreen(screenID);
				dispose();
			} // return to main menu
		});

		// Listener for Copy Person toolbar icon
		btn_CopyIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE15 need code here to perform DB updates for Person copy
				// then switch the screen to show the copied entry (for editing)
				JOptionPane.showMessageDialog(contents, "Copy is not yet implemented",		//$NON-NLS-1$ (temporary code)
						"Copy Person", JOptionPane.INFORMATION_MESSAGE);					//$NON-NLS-1$ (temporary code)
			}
		});

	// Listener for Delete Person toolbar icon
		btn_DeleteIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
	       		if (JOptionPane.showConfirmDialog(contents, HG0506Msgs.Text_59	// Are you sure you want to delete \nperson:
							  + persName.getText() +" ?",	//$NON-NLS-1$
							    HG0506Msgs.Text_49,			// Delete Person
							   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
				try {
					pointPersonHandler.deletePersonInTable(personPID, pointOpenProject);
					JOptionPane.showMessageDialog(contents, HG0506Msgs.Text_48 + persName.getText(),	// Deleted Person:
							HG0506Msgs.Text_49, JOptionPane.INFORMATION_MESSAGE);						// Delete Person

				// Indicate we are closing this screen after a Person delete
					closeAfterDelete = true;

				// Delete person from Person menu Recents list
					HG0401HREMain.mainFrame.deleteRecentPerson(personPID);

    			// close reminder display
					if (reminderDisplay != null) reminderDisplay.dispose();

			    // Set frame size in GUI data
					Dimension frameSize = getSize();
					pointOpenProject.setSizeScreen(screenID,frameSize);

				// Set position	in GUI data
					Point position = getLocation();
					pointOpenProject.setPositionScreen(screenID,position);

				// Set class name in GUI configuration data
					pointOpenProject.setClassName(screenID,"HG0506ManagePerson"); //$NON-NLS-1$

				// Mark the screen as closed in T302 and remove open screeen
					pointOpenProject.closeStatusScreen(screenID);
					dispose();

				} catch (HBException hbe) {
					if (HGlobal.DEBUG)
						System.out.println(" ERROR in PersonHandler deletePersonInTable: " + hbe.getMessage());	//$NON-NLS-1$
					JOptionPane.showMessageDialog(contents, HG0506Msgs.Text_60 + persName.getText()		// ERROR: failed to delete
					 			+ hbe.getMessage(),
								HG0506Msgs.Text_49, JOptionPane.ERROR_MESSAGE);
					if (HGlobal.DEBUG) hbe.printStackTrace();
				}
			}
		});

	// Listener for Renumber Person toolbar icon
		btn_RenumIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE15 need code here to perform the Person renumber and save to DB
				JOptionPane.showMessageDialog(contents, "Renumber is not yet implemented",		//$NON-NLS-1$ (temporary code)
						"Renumber Person", JOptionPane.INFORMATION_MESSAGE);					//$NON-NLS-1$ (temporary code)
			}
		});

	// Listener for pressing Enter after entering new person number
		persNum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int enteredNum = ((Integer) persNum.getValue()).intValue();
	        	if (enteredNum > 0) {
	        		long personPID = pointOpenProject.getPersonPIDfromVisID(enteredNum);
	        		if (HGlobal.DEBUG)
						System.out.println("Entered:" + enteredNum + "  PID: "+ personPID); //$NON-NLS-1$ //$NON-NLS-2$

	        		if (personPID != null_RPID) {
	    				if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: reset HG0506ManagePerson"); //$NON-NLS-1$

    				// close reminder display
    					if (reminderDisplay != null) reminderDisplay.dispose();

    			    // Set frame size in GUI data
    					Dimension frameSize = getSize();
    					pointOpenProject.setSizeScreen(screenID,frameSize);

    				// Set position	in GUI data
    					Point position = getLocation();
    					pointOpenProject.setPositionScreen(screenID,position);

    				// Set class name in GUI configuration data
    					pointOpenProject.setClassName(screenID,"HG0506ManagePerson"); //$NON-NLS-1$

    				// Mark the screen as closed in T302 and remove open screeen
    					pointOpenProject.closeStatusScreen(screenID);
    					dispose();
	        			pointPersonHandler.initiateManagePerson(pointOpenProject, personPID, screenID);

	        		} else {
						JOptionPane.showMessageDialog(persNum, HG0506Msgs.Text_27 + enteredNum,
													HG0506Msgs.Text_28, JOptionPane.ERROR_MESSAGE);
					}
	        	}
			}
		});

	//Listeners for radio button group items, to show each rightPanel card
		 ActionListener actionRadioEvent = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "EVENTS");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioPartners = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "PARTNERS");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioAssocs = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "ASSOCIATES");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioNames = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "NAMES");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioFlag = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "FLAGS");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioMedia = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "MEDIA");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioNote = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "NOTEPADS");	//$NON-NLS-1$
		      }
		    };
		 ActionListener actionRadioDNA = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "DNA");	//$NON-NLS-1$
		      }
		    };
		// Link the listeners above to the radio buttons
		radio_Event.addActionListener(actionRadioEvent);
		radio_Partners.addActionListener(actionRadioPartners);
		radio_Assocs.addActionListener(actionRadioAssocs);
		radio_Names.addActionListener(actionRadioNames);
		radio_Flag.addActionListener(actionRadioFlag);
		radio_Media.addActionListener(actionRadioMedia);
		radio_Note.addActionListener(actionRadioNote);
		radio_DNA.addActionListener(actionRadioDNA);

		// Define ActionListeners for Events table right-click popupMenu
		// For popupMenu item popMenu0 & 10 - general Add Event option
	    ActionListener popAL0 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int selectedPartnerTableRow = tablePartners.getSelectedRow();
	        	HG0552ManageEvent eventScreen = pointHBWhereWhenHandler.activateAddSelectedEvent(pointOpenProject, selectedPartnerTableRow);
	        	eventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = persName.getLocationOnScreen();
				eventScreen.setLocation(xyShow.x, xyShow.y);
				eventScreen.setVisible(true);
	        }
	      };

	// For popupMenu item popMenu1 & 11 - open Birth Event entry
	    ActionListener popAL1 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int eventNumber = 1002, roleNumber = 1;
				HG0547EditEvent editEventScreen = pointHBWhereWhenHandler.activateAddFixedEvent(pointOpenProject,
												eventNumber, roleNumber, null_RPID, sexCode);
				editEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = persName.getLocationOnScreen();
				editEventScreen.setLocation(xyShow.x, xyShow.y);
				editEventScreen.setVisible(true);
	        }
	      };

	// For popupMenu item popMenu2 & 12 - open Partner Event entry
	    ActionListener popAL2 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int eventNumber = 1004, roleNumber = 1;
	        	int selectedPartnerTableRow = tablePartners.getSelectedRow();

	        	if (selectedPartnerTableRow >= 0) {
		        	Object[] partnerRelationData = pointPersonHandler.getPartnerTableData(selectedPartnerTableRow);
		        	long partnerTablePID = (long)partnerRelationData[0];

					HG0547EditEvent editPartnerScreen = pointHBWhereWhenHandler.activateAddPartnerEvent(pointOpenProject,
														eventNumber, roleNumber, partnerTablePID, selectedPartnerTableRow);
					if (editPartnerScreen == null) 	return;

					editPartnerScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = persName.getLocationOnScreen();
					editPartnerScreen.setLocation(xyShow.x, xyShow.y);
					editPartnerScreen.setVisible(true);
	        	} else {
					JOptionPane.showMessageDialog(tablePartners,
							HG0506Msgs.Text_81			// Proceed to the Partner table and either: \n
							+ HG0506Msgs.Text_82 		// Select a new partner by using 'Add new Partner' \n OR \n
							+ HG0506Msgs.Text_83,		//	Edit an existing partner to add a partner event
							HG0506Msgs.Text_39, 		// Add Partner Event
							JOptionPane.INFORMATION_MESSAGE);
	        	}
	        }
	      };

	// For popupMenu item popMenu3 & 13 - open Death Event entry
	    ActionListener popAL3 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int eventNumber = 1003, roleNumber = 1;
				HG0547EditEvent editEventScreen = pointHBWhereWhenHandler.activateAddFixedEvent(pointOpenProject,
													eventNumber, roleNumber, null_RPID, sexCode);
				editEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = persName.getLocationOnScreen();
				editEventScreen.setLocation(xyShow.x, xyShow.y);
				editEventScreen.setVisible(true);
	        }
	      };

		// For popupMenu item popMenu4 & 14 - open Burial Event entry
	    ActionListener popAL4 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int eventNumber = 1006, roleNumber = 1;
				HG0547EditEvent editEventScreen = pointHBWhereWhenHandler.activateAddFixedEvent(pointOpenProject,
												  eventNumber, roleNumber, null_RPID, sexCode);
				editEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = persName.getLocationOnScreen();
				editEventScreen.setLocation(xyShow.x, xyShow.y);
				editEventScreen.setVisible(true);
	        }
	      };

		// For popupMenu item popMenu5 - Delete tag
	    ActionListener popAL5 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	String eventName = " - ", selectString;	//$NON-NLS-1$
	        	int eventType, dataBaseIndex, rowInTable, eventGroup;
	        	long eventPID;
	        	ResultSet eventTableRS;
	        	dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	        	rowInTable = tableEvents.getSelectedRow();
	        	eventPID = pointPersonHandler.getEventPID(rowInTable);
	        	if (rowInTable < 0) return;

	        	try {
		    		selectString = pointPersonHandler.setSelectSQL("*", pointPersonHandler.eventTable, "PID = " + eventPID); //$NON-NLS-1$ //$NON-NLS-2$
		    		eventTableRS = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
		    		eventTableRS.first();
		    		eventType = eventTableRS.getInt("EVNT_TYPE");	//$NON-NLS-1$
		        	eventName = tableEvents.getModel().getValueAt(rowInTable, 0).toString();
		       		if (JOptionPane.showConfirmDialog(tableEvents, HG0506Msgs.Text_62	// Are you sure you want to delete \nevent '
								    + eventName.trim() +"'?",		//$NON-NLS-1$
								    HG0506Msgs.Text_63,				// Delete Event
								   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
		       		eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
		       		if (eventGroup == pointPersonHandler.marrGroup || eventGroup == pointPersonHandler.divorceGroup) {
						pointPersonHandler.deletePartnerEvent(eventPID);
					} else pointOpenProject.getWhereWhenHandler().deleteSingleEvent(eventPID);
					pointOpenProject.getPersonHandler().resetPersonManager();
				} catch (HBException | SQLException hbe) {
					System.out.println("HG0506ManagePerson delete event error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        	JOptionPane.showMessageDialog(tableEvents,
	        			HG0506Msgs.Text_64 + eventName.trim() + HG0506Msgs.Text_65); // Deleted '    // ' event
	        }
	      };

		// For popupMenu item popMenu6 - Copy tag
	    ActionListener popAL6 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	JOptionPane.showMessageDialog(tableEvents, "Copy event not yet implemented");  // NOTE02  //$NON-NLS-1$ (temporary code)
	        }
	      };

		// For popupMenu item popMenu7 - Edit the clicked Event
	    ActionListener popAL7 = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        // The right-clicked (or double-clicked) row is passed here in rowClicked
        		int rowInTable = tableEvents.convertRowIndexToModel(rowClicked);
	        	HBWhereWhenHandler pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
				HG0547EditEvent editEventScreen = pointHBWhereWhenHandler.activateUpdateEvent(pointOpenProject, rowInTable,
																							  true, sexCode);
				if (editEventScreen != null) {
					editEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = persName.getLocationOnScreen();
					editEventScreen.setLocation(xyShow.x, xyShow.y);
					editEventScreen.setVisible(true);
				}
	        }
	      };

		// Define popup menu items and actions to use in the 2 popupMenus
	    JMenuItem popMenu7 = new JMenuItem(HG0506Msgs.Text_29);		// Edit this Event
	    popMenu7.addActionListener(popAL7);
	    popMenu0 = new JMenuItem(HG0506Msgs.Text_30);		// Add any Event...
	    popMenu0.addActionListener(popAL0);
	    JMenuItem popMenu10 = new JMenuItem(HG0506Msgs.Text_30);	// Add any Event...
	    popMenu10.addActionListener(popAL0);
	    JMenuItem popMenu1 = new JMenuItem(HG0506Msgs.Text_32);		// Add Birth Event
	    popMenu1.addActionListener(popAL1);
	    JMenuItem popMenu11 = new JMenuItem(HG0506Msgs.Text_32);	// Add Birth Event
	    popMenu11.addActionListener(popAL1);
	    JMenuItem popMenu2 = new JMenuItem(HG0506Msgs.Text_34);		// Add Marriage Event
	    popMenu2.addActionListener(popAL2);
	    JMenuItem popMenu12 = new JMenuItem(HG0506Msgs.Text_34);	// Add Marriage Event
	    popMenu12.addActionListener(popAL2);
	    JMenuItem popMenu3 = new JMenuItem(HG0506Msgs.Text_36);		// Add Death Event
	    popMenu3.addActionListener(popAL3);
	    JMenuItem popMenu13 = new JMenuItem(HG0506Msgs.Text_36);	// Add Death Event
	    popMenu13.addActionListener(popAL3);
	    JMenuItem popMenu4 = new JMenuItem(HG0506Msgs.Text_38);		// Add Burial Event
	    popMenu4.addActionListener(popAL4);
	    JMenuItem popMenu14 = new JMenuItem(HG0506Msgs.Text_38);	// Add Burial Event
	    popMenu14.addActionListener(popAL4);
	    JMenuItem popMenu5 = new JMenuItem(HG0506Msgs.Text_40);			// Delete Event
	    popMenu5.addActionListener(popAL5);
	    JMenuItem popMenu6 = new JMenuItem(HG0506Msgs.Text_41);			// Copy Event
	    popMenu6.addActionListener(popAL6);
	    // Define a right-click popup menu to use in tableEvents
	    JPopupMenu popupMenu = new JPopupMenu();
	    popupMenu.add(popMenu7);
	    popupMenu.add(popMenu0);
	    popupMenu.add(popMenu1);
	    popupMenu.add(popMenu2);
	    popupMenu.add(popMenu3);
	    popupMenu.add(popMenu4);
	    popupMenu.addSeparator();
	    popupMenu.add(popMenu5);
	    popupMenu.add(popMenu6);
	    // Define 2nd right-click popupMenu for use anywhere in Viewport
	    JPopupMenu popupMenuAny = new JPopupMenu();
	    popupMenuAny.add(popMenu10);
	    popupMenuAny.add(popMenu11);
	    popupMenuAny.add(popMenu12);
	    popupMenuAny.add(popMenu13);
	    popupMenuAny.add(popMenu14);

	    // Listener for Events table mouse right-click
	    tableEvents.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
				// Detect RIGHT-CLICK - show popup menu OR Name screen if its a Name event
                if (me.getButton() == MouseEvent.BUTTON3) {
                	rowClicked = tableEvents.rowAtPoint(me.getPoint());
                	// if not within list of Events, use popupMenuAny
                	if (tableEvents.rowAtPoint(me.getPoint()) < 0)
										popupMenuAny.show(me.getComponent(), me.getX(), me.getY());
                	// Else, first check for a Name-Var event
					else {if (objEventData[rowClicked][5].equals(HG0506Msgs.Text_66)) {		// N
	                	 	HG0509ManagePersonName pointSelectPersonName = null;
	        	        	try {
	        					pointSelectPersonName = pointPersonHandler.activatePersonNameEdit(pointOpenProject, rowClicked,false);
	        		        	pointSelectPersonName.setModalityType(ModalityType.APPLICATION_MODAL);
	        					Point xyShow = tableEvents.getLocationOnScreen();
	        					pointSelectPersonName.setLocation(xyShow.x, xyShow.y);
	        					pointSelectPersonName.setVisible(true);
	        				} catch (HBException hbe) {
	        					System.out.println("HG0506ManagePerson - Edit Name error: " + hbe.getMessage());	//$NON-NLS-1$
	        					hbe.printStackTrace();
	        				}
                		}
					// Otherwise, just use the popMenu for event edits
						else popupMenu.show(me.getComponent(), me.getX(), me.getY());
                	}
                }
           		// Detect DOUBLE-CLICK - edit this event (but note Name-Var handled by different process)
	           	if (me.getClickCount() == 2 && tableEvents.getSelectedRow() != -1) {
	           		rowClicked = tableEvents.rowAtPoint(me.getPoint());
	           		// First, check for a Name-Var event edit
	           		if (objEventData[rowClicked][5].equals(HG0506Msgs.Text_66)) {		// N
                	 	HG0509ManagePersonName pointSelectPersonName = null;
        	        	try {
        					pointSelectPersonName = pointPersonHandler.activatePersonNameEdit(pointOpenProject, rowClicked,false);
        		        	pointSelectPersonName.setModalityType(ModalityType.APPLICATION_MODAL);
        					Point xyShow = tableEvents.getLocationOnScreen();
        					pointSelectPersonName.setLocation(xyShow.x, xyShow.y);
        					pointSelectPersonName.setVisible(true);
        				} catch (HBException hbe) {
        					System.out.println("HG0506ManagePerson - Edit Name error: " + hbe.getMessage());	//$NON-NLS-1$
        					hbe.printStackTrace();
        				}
            		}
	           		// Othewise, just do edit of the event
	           		else popMenu7.doClick();
	           	}
            }
        });

		// Define ActionListeners for Parents table right-click popupMenus
		// For popupMenu item popMenuEdit - Edit the clicked Parent
	    ActionListener popParentEdit = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Call the SelectPerson code to select & add new Parent
				HG0507SelectPerson personSelectScreen = null;
				try {
		// The right-clicked row is passed here in rowClicked
					int rowInTable = tableParents.convertRowIndexToModel(rowClicked);
					personSelectScreen = pointPersonHandler.activateParentEdit(pointOpenProject, rowInTable);
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = tableParents.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				} catch (HBException hbe) {
		// Attempt to edit parent relation without table recording - should never happen!
					if (hbe.getMessage().contains("No Parent")) {		//$NON-NLS-1$
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(personSelectScreen, HG0506Msgs.Text_84, // No Parent data available to edit
								HG0506Msgs.Text_85,	// Edit Parent
								JOptionPane.ERROR_MESSAGE);
					} else {
						System.out.println("HG0506ManagePerson - Edit parent error: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					}
				}
	        }
	      };

		// For popupMenu item popParentDelete - Delete the clicked Parent
	    ActionListener popParentDelete = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
		// The right-clicked row is passed here in rowClicked
        		int rowInTable = tableParents.convertRowIndexToModel(rowClicked);
        		String parentName = (String) tableParents.getModel().getValueAt(rowInTable, 1);
	       		if (JOptionPane.showConfirmDialog(contents, HG0506Msgs.Text_59	// Are you sure you want to delete \nperson:
							  + parentName +" ?",	//$NON-NLS-1$
							    HG0506Msgs.Text_49,			// Delete Person
							   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
	        	try {
					pointPersonHandler.deleteParent(pointOpenProject, rowInTable);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Delete parent error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

		// For popupMenu item popMenuParentAdd - general Add Parent
	    ActionListener popParentAdd = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Call the SelectPerson code to select & add new Parent
				HG0507SelectPerson personSelectScreen;
				try {
					personSelectScreen = pointPersonHandler.activateParentAdd(pointOpenProject);
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = tableParents.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Add parent error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// Define Parent popup menu items and actions
	    JMenuItem popMenuParentEdit = new JMenuItem(HG0506Msgs.Text_67);	// Edit this parent
	    popMenuParentEdit.addActionListener(popParentEdit);
	    JMenuItem popMenuParentDelete = new JMenuItem(HG0506Msgs.Text_68);	// Delete this parent
	    popMenuParentDelete.addActionListener(popParentDelete);
	    JMenuItem popMenuParentAdd = new JMenuItem(HG0506Msgs.Text_69);	// Add new parent
	    popMenuParentAdd.addActionListener(popParentAdd);
	    JMenuItem popMenuParentNew = new JMenuItem(HG0506Msgs.Text_69);	// Add new parent
	    popMenuParentNew.addActionListener(popParentAdd);

	 // Define  right-click popup menus to use in tableParents
	    JPopupMenu popupMenuParent = new JPopupMenu();
	    popupMenuParent.add(popMenuParentEdit);
	    popupMenuParent.add(popMenuParentDelete);
	    popupMenuParent.add(popMenuParentAdd);
	    JPopupMenu popupMenuParentNew = new JPopupMenu();
	    popupMenuParentNew.add(popMenuParentNew);
	 // Listener for Parents table mouse click
	    tableParents.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                // Action RIGHT-CLICK
                if (me.getButton() == MouseEvent.BUTTON3) {
                	rowClicked = tableParents.rowAtPoint(me.getPoint());
                	if (rowClicked == -1 || canOnlyAddParent)
                		popupMenuParentNew.show(me.getComponent(), me.getX(), me.getY());
                	else
                		popupMenuParent.show(me.getComponent(), me.getX(), me.getY());
                }
                // Action DOUBLE-CLICK - edit the parent
	           	if (me.getClickCount() == 2 && tableParents.getSelectedRow() != -1) {
	           		rowClicked = tableParents.getSelectedRow();
	           		popMenuParentEdit.doClick();
	            }
            }
        });

	// Define ActionListeners for Partner table right-click popupMenus
	// For popupMenu item popPartnerEdit - Edit the clicked Partner
	    ActionListener popPartnerEdit = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	HG0507SelectPerson partnerEditScreen;
	        	try {
	        		int rowInTable = tablePartners.convertRowIndexToModel(rowClicked);
	        		partnerEditScreen = pointPersonHandler.activatePartnerEdit(pointOpenProject, rowInTable);
	        		partnerEditScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = persName.getLocationOnScreen();
					partnerEditScreen.setLocation(xyShow.x, xyShow.y);
					partnerEditScreen.setVisible(true);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Edit partner error: " + hbe.getMessage());  //$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// For popupMenu item popPartnerDelete - Delete the clicked Partner
	    ActionListener popPartnerDelete = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
        		int rowInTable = tablePartners.convertRowIndexToModel(rowClicked);
        		String partnerName = (String) tablePartners.getModel().getValueAt(rowInTable, 0);
	       		if (JOptionPane.showConfirmDialog(contents, HG0506Msgs.Text_59	// Are you sure you want to delete \nperson:
							  + partnerName +" ?",	//$NON-NLS-1$
							    HG0506Msgs.Text_49,			// Delete Person
							   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
	        	try {
					pointPersonHandler.deletePartner(pointOpenProject, rowInTable);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Delete partner error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// For popupMenu item popPartnerAdd - general Add Partner
	    ActionListener popPartnerAdd = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// Call the SelectPerson code to select & add new Partner
				HG0507SelectPerson personSelectScreen;
				try {
					personSelectScreen = pointPersonHandler.activatePartnerAdd(pointOpenProject);
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = tablePartners.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Add partner error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// Define Partner popup menu items and actions
	    JMenuItem popMenuPartnerP1 = new JMenuItem(HG0506Msgs.Text_70);	// Edit this partner
	    popMenuPartnerP1.addActionListener(popPartnerEdit);
	    JMenuItem popMenuPartnerP2 = new JMenuItem(HG0506Msgs.Text_71);	// Delete this partner
	    popMenuPartnerP2.addActionListener(popPartnerDelete);
	    JMenuItem popMenuPartnerAdd = new JMenuItem(HG0506Msgs.Text_72);	// Add new partner
	    popMenuPartnerAdd.addActionListener(popPartnerAdd);
	    JMenuItem popMenuPartnerAddOnly = new JMenuItem(HG0506Msgs.Text_72);	// Add new partner
	    popMenuPartnerAddOnly.addActionListener(popPartnerAdd);
	// Define a right-click popup menu to use in tablePartners
	    JPopupMenu popupMenuPartner = new JPopupMenu();
	    popupMenuPartner.add(popMenuPartnerP1);
	    popupMenuPartner.add(popMenuPartnerP2);
	    popupMenuPartner.add(popMenuPartnerAdd);
	 // and a popup menu for anywhere in the table
	    JPopupMenu popupMenuPartnerAdd = new JPopupMenu();
	    popupMenuPartnerAdd.add(popMenuPartnerAddOnly);

	// Listener for Partners table mouse click
	    tablePartners.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                // Action RIGHT-CLICK
                if (me.getButton() == MouseEvent.BUTTON3) {
                	rowClicked = tablePartners.rowAtPoint(me.getPoint());
                	if (tablePartners.rowAtPoint(me.getPoint()) < 0)
						popupMenuPartnerAdd.show(me.getComponent(), me.getX(), me.getY());
					else
						popupMenuPartner.show(me.getComponent(), me.getX(), me.getY());
                }
                // Action DOUBLE-CLICK - edit the partner
	           	if (me.getClickCount() == 2 && tablePartners.getSelectedRow() != -1) {
	           		rowClicked = tablePartners.getSelectedRow();
	           		popMenuPartnerP1.doClick();
	            }
            }
        });

	// For popupMenu item popNameEdit - Edit the clicked Name
	    ActionListener popNameEdit = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	HG0509ManagePersonName pointSelectPersonName = null;
	        	int rowInTable = tableNames.convertRowIndexToModel(rowClicked);
	        	try {
					pointSelectPersonName = pointPersonHandler.activatePersonNameEdit(pointOpenProject, rowInTable, true);
		        	pointSelectPersonName.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = tableNames.getLocationOnScreen();
					pointSelectPersonName.setLocation(xyShow.x, xyShow.y);
					pointSelectPersonName.setVisible(true);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Edit Name error: " + hbe.getMessage());  //$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

  	// For popupMenu item popNameAdd - Add a new Name
	    ActionListener popNameAdd = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	HG0509ManagePersonName pointSelectPersonName = null;
	        	try {
					pointSelectPersonName = pointPersonHandler.activatePersonNameAdd(pointOpenProject, personPID);
		        	pointSelectPersonName.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = tableNames.getLocationOnScreen();
					pointSelectPersonName.setLocation(xyShow.x, xyShow.y);
					pointSelectPersonName.setVisible(true);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Add Name error: " + hbe.getMessage());  //$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// For popupMenu item popNameDelete - Delete the clicked Name
	    ActionListener popNameDelete = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
        		int rowInTable = tableNames.convertRowIndexToModel(rowClicked);
	        	try {
					pointPersonHandler.deleteNameForPerson(rowInTable);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - Delete Name error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// Define Names popup menu items and actions
	     popMenuNamesEdit = new JMenuItem(HG0506Msgs.Text_73);			// Edit this Name
	    popMenuNamesEdit.addActionListener(popNameEdit);
	    JMenuItem popMenuNamesDel = new JMenuItem(HG0506Msgs.Text_74);	// Delete this Name
	    popMenuNamesDel.addActionListener(popNameDelete);
	    JMenuItem popMenuNameAdd = new JMenuItem(HG0506Msgs.Text_75);	// Add new Name
	    popMenuNameAdd.addActionListener(popNameAdd);
	    JMenuItem popMenuNameAddOnly = new JMenuItem(HG0506Msgs.Text_75);	// Add new Name
	    popMenuNameAddOnly.addActionListener(popNameAdd);
	// Define a right-click popup menu to use in tableNames
	    JPopupMenu popupMenuName = new JPopupMenu();
	    popupMenuName.add(popMenuNamesEdit);
	    popupMenuName.add(popMenuNamesDel);
	    popupMenuName.add(popMenuNameAdd);
	 // and a popup menu for anywhere in the table
	    JPopupMenu popupMenuNameAdd = new JPopupMenu();
	    popupMenuNameAdd.add(popMenuNameAddOnly);

	// Listener for Names table mouse click
	    tableNames.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                // Action RIGHT-CLICK
                if (me.getButton() == MouseEvent.BUTTON3) {
                	rowClicked = tableNames.rowAtPoint(me.getPoint());
                	// if clicked outside table entries, just show Add Name option
                	if (tableNames.rowAtPoint(me.getPoint()) < 0) {
						popupMenuNameAdd.show(me.getComponent(), me.getX(), me.getY());
					} else {
						popupMenuName.show(me.getComponent(), me.getX(), me.getY());
					}
                }
                // Action DOUBLE-CLICK - edit the name
	           	if (me.getClickCount() == 2 && tableNames.getSelectedRow() != -1) {
	           		rowClicked = tableNames.getSelectedRow();
	           		popMenuNamesEdit.doClick();
	            }
            }
        });

		// Listener for Flag table mouse clicks - reset Flag-description
		tableFlags.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
	           	if (me.getClickCount() == 1 && tableFlags.getSelectedRow() != -1) {
	           		// SINGLE-CLICK - update Flag Description
	        		flagDescTxt.setText((String) objReqFlagData[tableFlags.getSelectedRow()][2]);
	            }
	        }
	    });

		// Listener for change of value in Flag Setting combobox in col 1 of flagTable
		tableFlags.getModel().addTableModelListener(new TableModelListener() {
	        @Override
			public void tableChanged(TableModelEvent evt) {
	            int clickedRow = tableFlags.getSelectedRow();
	        // Get the new combobox setting
	            String newValue = tableFlags.getValueAt(clickedRow, 1).toString();
	        // Get the FlagIDent that matches this table row
	            int flagID = (int) objReqFlagData[clickedRow][3];
	        // Just check it all looks good ->
	            if (HGlobal.DEBUG)
					System.out.println(" New Flag setting Row = "+ clickedRow + " newValue = " + newValue +		//$NON-NLS-1$//$NON-NLS-2$
	            						" Flag = " + objReqFlagData[clickedRow][0] + " FlagID = " + flagID);	//$NON-NLS-1$//$NON-NLS-2$

	         // Update the Flag in DB
	            try {
					pointPersonHandler.setPersonFlagSetting(flagID, newValue);
					// Reset the Birth Sex label in case it was the Flag that changed
					if ((int) objReqFlagData[clickedRow][3] == 1) birthSex.setText(newValue);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson flag update error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	    });

		// Listener for Add Image button
		btn_AddImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE09 need code here
			}
		});

		// Listener for Add Text file button
		btn_AddText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE09 need code here
			}
		});

		// Listener for Add Audio file button
		btn_AddAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE09 need code here
			}
		});

		// Listener for Add Video file button
		btn_AddVideo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE09 need code here
			}
		});

		// Listener for 'Show witnessed events' CheckBox
		checkWitness.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		        boolean state;
		        if (checkWitness.isSelected())state = true;
				else state = false;
		      	try {
					pointPersonHandler.setWitnessState(state);
				// Reset table data
					objEventData = pointPersonHandler.getManagedEventTable();
					DefaultTableModel eventModel = (DefaultTableModel) tableEvents.getModel();
					eventModel.setDataVector(objEventData, eventTableHeader);
				// Reset table renderers
				    DefaultTableCellRenderer centerRendererE = new DefaultTableCellRenderer();
				    centerRendererE.setHorizontalAlignment( SwingConstants.CENTER );
			        tableEvents.getColumnModel().getColumn(4).setCellRenderer(centerRendererE);
					tableEvents.getColumnModel().getColumn(0).setMinWidth(80);
					tableEvents.getColumnModel().getColumn(0).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(1).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(1).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(2).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(2).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(3).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(3).setPreferredWidth(300);
					tableEvents.getColumnModel().getColumn(4).setMinWidth(40);
					tableEvents.getColumnModel().getColumn(4).setPreferredWidth(40);
				} catch (HBException hbe) {
					System.out.println(" pointPersonHandler.setWitnessState error: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
		      }
		});

		// Listener for 'Show children' CheckBox
		checkChildren.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
			   boolean state;
			   if (checkChildren.isSelected()) 	state = true;
			   else state = false;
		        try {
					pointPersonHandler.setChildrenState(state);
					objEventData = pointPersonHandler.getManagedEventTable();
					DefaultTableModel eventModel = (DefaultTableModel) tableEvents.getModel();
					eventModel.setDataVector(objEventData, eventTableHeader);
				// Reset table renders
				    DefaultTableCellRenderer centerRendererE = new DefaultTableCellRenderer();
				    centerRendererE.setHorizontalAlignment( SwingConstants.CENTER );
			        tableEvents.getColumnModel().getColumn(4).setCellRenderer(centerRendererE);
					tableEvents.getColumnModel().getColumn(0).setMinWidth(80);
					tableEvents.getColumnModel().getColumn(0).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(1).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(1).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(2).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(2).setPreferredWidth(120);
					tableEvents.getColumnModel().getColumn(3).setMinWidth(100);
					tableEvents.getColumnModel().getColumn(3).setPreferredWidth(300);
					tableEvents.getColumnModel().getColumn(4).setMinWidth(40);
					tableEvents.getColumnModel().getColumn(4).setPreferredWidth(40);
				} catch (HBException hbe) {
					System.out.println("HG0506ManagePerson - setChildrenState " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
		      }
		 });

		// Filter Text field listener
		filterTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// no action here
			}
		});

		// Filter checkbox listener
		chkbox_Filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Reset filter for whole table when unselected, sorted as before
				if (!chkbox_Filter.isSelected()) {
						comboBox_Subset.setSelectedIndex(0);
						tableAssocs.setRowSorter(sortAssoc);
				}
			}
		});

		// On selection of a Subset perform appropriate action
		comboBox_Subset.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (chkbox_Filter.isSelected())
					setTableFilter(comboBox_Subset.getSelectedItem().toString(), filterTextField.getText());
			}
		});
	}	// End HG0506ManagePerson constructor

/**
 * Action setup and/or change of filter settings
 * @param selectString (table column name) and filterText
 */
	private void setTableFilter(String selectString, String filterText) {
		// Test filter for special characters and insert backslash (escape) if needed
		// Note that as backslash is also special, we need to double each use of it!
		filterText = filterText.replaceAll("\\?", "\\\\?");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\(", "\\\\(");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\)", "\\\\)");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\*", "\\\\*");		//$NON-NLS-1$	//$NON-NLS-2$
		// Setup sorter
		TableModel myModel = tableAssocs.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		try {
			if (selectString.trim().equals(HG0506Msgs.Text_56)) {		// All Columns
				// For whole table
				sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText));	// case-insensitive unicode filter //$NON-NLS-1$
			} else { // All Columns
				for (int i = 0; i < tableHeaderForPartnerAndAssocs.length; i++) {
									if (selectString.trim().equals(tableHeaderForPartnerAndAssocs[i].trim())) {
										// For only one column
										sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText, i));	// case-insensitive unicode filter //$NON-NLS-1$
										break;
									}
							}
			}
		} catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(chkbox_Filter, HG0506Msgs.Text_76 					// Cannot use
												+ filterText + HG0506Msgs.Text_77,				//  as a filter
												HG0506Msgs.Text_78, JOptionPane.ERROR_MESSAGE);	// Filter Text Error
		}
	    tableAssocs.setRowSorter(sorter);
	    // Set scroll bar to top of filter results
	    assocScrollPane.getViewport().setViewPosition(new Point(0,0));
	}		// End of setTableFilter

/**
 * DCTextField - new JTextfield class that only responds to double-click
 */
	public class DCTextField extends JTextField {
		private static final long serialVersionUID = 1L;
		public DCTextField(String text) {
	        super(text);
	    }
	    @Override
	    protected void processMouseEvent(MouseEvent e) {
	        if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() == 2) {
	            super.processMouseEvent(e);
	            // Switch to Names card
				radio_Names.doClick();
				// If only 1 Name it MUST be Primary, so show HG0509ManagePersonName for editing
				if (tableNames.getRowCount() == 1) popMenuNamesEdit.doClick();
	        }
	    }
	}	// End DCTextField

}  // End of HG0506ManagePerson
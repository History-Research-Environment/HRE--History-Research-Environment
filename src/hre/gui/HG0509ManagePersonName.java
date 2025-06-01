package hre.gui;
/*******************************************************************************
 * Manage Person Name
 * *****************************************************************************
 * v0.01.0027 2022-09-23 First draft (D Ferguson)
 * v0.01.0028 2022-11-26 Add Show Hidden button (D Ferguson)
 * 			  2022-12-08 Update of handling HIDDEN elements  (N. Tolleshaug)
 * 			  2023-02-07 Added name style and dates (N. Tolleshaug)
 * 			  2023-02-08 Implemented show and store person name style (N. Tolleshaug)
 * 			  2023-02-09 Added Save button specific to Style change (D Ferguson)
 * 			  2023-03-11 Add Primary name checkbox (D Ferguson)
 * 			  2023-03-13 Primary name update  (N. Tolleshaug)
 * 			  2023-03-18 Update name element data in T403 (N. Tolleshaug)
 * 			  2023-03-20 Update of Person Select table (N. Tolleshaug)
 * v0.03.0031 2023-12-11 Add Citation and Memo panels (D Ferguson)
 * 			  2024-01-24 NLS update (D Ferguson)
 * 			  2024-06-14 Split off Add/EditPersonName extensions (D Ferguson)
 * 			  2024-06-15 Add Name events combo, sort dates, SentenceEdit button (D Ferguson)
 * 			  2024-06-27 Activated parent classes for edit and add name (N Tolleshaug)
 * 			  2024-06-28 Removed sort date fields (N Tolleshaug)
 * 			  2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-10-25 Make date fields non-editable by keyboard (D Ferguson)
 * 			  2024-11-03 Removed SwingUtility for table cell focus (D Ferguson)
 * 			  2024-12-02 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * 			  2024-12-08 Updated listener tablePerson name changes (N Tolleshaug)
 * 			  2024-12-08 Updated name styles and event type handling (N Tolleshaug)
 * v0.04.0032 2024-12-31 Remove dummy Source data from citation table (D Ferguson)
 * 			  2025-02-13 Added code for name citiation (N. Tolleshaug)
 *  		  2025-02-14 Added activate HG0555EditCitation (N. Tolleshaug)
 *  		  2025-02-14 Added citationTablePID to HG0555EditCitation call(N. Tolleshaug)
 *  		  2025-02-26 Added add, edit and remove for name citations (N. Tolleshaug)
 * 			  2025-03-17 Adjust Citation table column sizes (D Ferguson)
 * 			  2025-04-21 Observe GUI Seq when loading citation data (D Ferguson)
 * 			  2025-05-25 Adjust structure of call to HG0555 (D Ferguson)
 ******************************************************************************
 * Notes on functions not yet enabled
 * NOTE04 Sentence edit function missing
 *****************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.gui.HGlobalCode.focusPolicy;
import hre.nls.HG0509Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage PersonName by Style
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2022-09-23
 */

public class HG0509ManagePersonName extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	HBWhereWhenHandler pointWhereWhenHandler;
	HBPersonHandler pointPersonHandler;
	public HBCitationSourceHandler pointCitationSourceHandler;
	HG0509ManagePersonName pointManagePersonName = this;
	private JPanel contents;

	public JComboBox<String> comboEvents;
	public JComboBox<String> comboStyles;
	int[] nameTypes;
	String[] nameEvents;
	JButton btn_Hidden;
	JLabel lbl_Style;
	JLabel lbl_CurrStyle;
	boolean showHiddenClicked = false;
	public JTable tablePerson;
	JToggleButton btn_Primary;
	DefaultTableModel citeModel;

	boolean eventTypeChanged = false;
	boolean nameChanged = false;
	boolean styleChanged = false;
	boolean citationOrderChanged = false;
	boolean memoChanged = false;
	boolean startDateOK = false;
	boolean endDateOK = false;

	int nameEventType = 1037;

	HG0590EditDate editStartDate;
	long startMainYear = 0L;
	String startMainDetails = "";	//$NON-NLS-1$
	long startExtraYear = 0L;
	String startExtraDetails = "";	//$NON-NLS-1$
	String startSortCode = "";		//$NON-NLS-1$
	Object[] startHREDate;
	long startHDatePID;

	HG0590EditDate editEndDate;
	long endMainYear = 0L;
	String endMainDetails = "";		//$NON-NLS-1$
	long endExtraYear = 0L;
	String endExtraDetails = "";	//$NON-NLS-1$
	String endSortCode = "";		//$NON-NLS-1$
	Object[] endHREDate;
	long endHDatePID, personNameTablePID;

	JTextField dateStart, dateEnd;
	JTextArea memoNameText;
	DocumentListener textListen;

	Object[][] tablePersData;
	Object[][] objNameCiteData;
	Object objCiteDataToEdit[] = new Object[2]; // to hold data to pass to Citation editor
	Object objTempCiteData[] = new Object[2];   // to temporarily hold a row of data when moving rows around
	String[] persHeaderData;
	String[] citeHeaderData;

	String [] nameData = {"","","","1037"};	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	boolean setPrimary = false;
	JButton btn_Save;
    static focusPolicy newPolicy;

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0509ManagePersonName(HBPersonHandler pointHBPersonHandler,
			 						HBProjectOpenData pointOpenProject,
			 						long personNameTablePID) throws HBException {
		this.pointPersonHandler = pointHBPersonHandler;
	// Set pointOpenproject in super - HG0450SuperDialog
		this.pointOpenProject = pointOpenProject;
		this.personNameTablePID = personNameTablePID;

	// Setup references for HG0509ManagePersonName
		if (HGlobal.DEBUG)
			System.out.println("HG0509ManagePersonName initated");	//$NON-NLS-1$
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
    	this.setResizable(true);
    // Setup screenID and helpName for 50600 as Help for this dialog is part of
    // managePerson Help and F1 has to select that (also this screen does not save to T302)
    	String screenID = "50600";		//$NON-NLS-1$
    	windowID = screenID;
		helpName = "manageperson";	//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0509ManagePersonName");}	//$NON-NLS-1$
	    pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
	    pointPersonHandler = pointOpenProject.getPersonHandler();
	    pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
	    citeHeaderData = pointPersonHandler.setTranslatedData("50500", "1", false); // Source#, Source, 1 2 D P M  //$NON-NLS-1$ //$NON-NLS-2$

/***********************************
 * Initiate load of Name/Style data
 ***********************************/
	 // Get Name style and start/end dates i exist for edit
	    if (!(this instanceof HG0509AddPersonName)) {
	    	nameData = pointPersonHandler.getNameData();
			String selectString = pointPersonHandler.setSelectSQL("*", pointPersonHandler.personNameTable, "PID = " + personNameTablePID); //$NON-NLS-1$ //$NON-NLS-2$
			ResultSet personNameTable = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
			try {
				personNameTable.first();
				startHDatePID = personNameTable.getLong("START_HDATE_RPID");	//$NON-NLS-1$
				endHDatePID = personNameTable.getLong("END_HDATE_RPID");		//$NON-NLS-1$

			} catch (SQLException sqle) {
				System.out.println(" SQL error personNameTable: " + sqle.getMessage());		//$NON-NLS-1$
				sqle.printStackTrace();
				throw new HBException(" SQL error personNameTable: " + sqle.getMessage());	//$NON-NLS-1$
			}
		// Collect start date Hdate
	    	startHREDate = pointPersonHandler.pointLibraryResultSet.
	    			dateIputHdate(startHDatePID, dataBaseIndex);
	    	if (startHREDate != null) {
				startMainYear = (long) startHREDate[0];
				startMainDetails = (String) startHREDate[1];
				startExtraYear = (long) startHREDate[2];
				startExtraDetails = (String) startHREDate[3];
				startSortCode = (String) startHREDate[4];
	    	}
	    // Collect end date Hdate
	    	endHREDate = pointPersonHandler.pointLibraryResultSet.
	    			dateIputHdate(endHDatePID, dataBaseIndex);
	    	if (endHREDate != null) {
				endMainYear = (long) endHREDate[0];
				endMainDetails = (String) endHREDate[1];
				endExtraYear = (long) endHREDate[2];
				endExtraDetails = (String) endHREDate[3];
				endSortCode = (String) endHREDate[4];
	    	}
	    }

	// Get Name Event types list & Name Events for combo box (Event Group 1 (Name))
	    comboEvents = new JComboBox<String>();
	    nameEvents = pointWhereWhenHandler.getEventTypeList(1);
    	nameTypes = pointWhereWhenHandler.getEventTypes();
	    comboEvents.setModel(new DefaultComboBoxModel<String>(nameEvents));

		// Now lookup the Event type list and set the combobox to the current Name event type
	    // For AddName, we'd need to set it to the 1037 event type (Name-Var) as a default
	    comboEvents.setSelectedIndex(1);
	    for (int i = 0; i < nameTypes.length; i++)
	    	if (nameData[3].equals("" + nameTypes[i])) {	//$NON-NLS-1$
	    		comboEvents.setSelectedIndex(i);
	    		nameEventType = nameTypes[i];
	    	}

	// Load Name Styles combo box
		DefaultComboBoxModel<String> nameStyles =
				new DefaultComboBoxModel<>(pointPersonHandler.getAvailableStyles());
		comboStyles = new JComboBox<>(nameStyles);
		comboStyles.setSelectedIndex(pointPersonHandler.getDefaultIndex());
		pointPersonHandler.setNameStyleIndex(pointPersonHandler.getDefaultIndex());

	// Match the currently stored Style against the combo-box entries
	// to be able to show the correct current values

		if (!(this instanceof HG0509AddPersonName)) {
			int ii = 0;
			for (ii = 0; ii < comboStyles.getItemCount(); ii++) {
				comboStyles.setSelectedIndex(ii);
				if (nameData[0].trim().equals(comboStyles.getSelectedItem().toString().trim())) break;
			}
		// Make sure we haven't got to loop end with no match
			if (ii == comboStyles.getItemCount()) ii = 0;
			// Get data for the Person elements and values
			pointPersonHandler.setNameStyleIndex(ii);
		}

	// Get data for the Person elements and values
	// Load the name data
		pointPersonHandler.updateManagePersonNameTable(personNameTablePID);
		persHeaderData = pointPersonHandler.getPersonTableHeader();
		tablePersData = pointPersonHandler.getPersonNameTable();

/********************
 * Setup main panel
 *******************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[]", "[]10[]10[]5[]5[]10[]5[]5[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Instruction = new JLabel(HG0509Msgs.Text_1);		// You may change Name Style and/or edit the Name parts and Dates.
		contents.add(lbl_Instruction, "cell 0 0 3 1");	//$NON-NLS-1$

		JLabel lbl_Event = new JLabel(HG0509Msgs.Text_19);	// Choose Event Type:
		contents.add(lbl_Event, "cell 0 1, alignx right");	//$NON-NLS-1$

		contents.add(comboEvents, "cell 1 1");	//$NON-NLS-1$

		lbl_Style = new JLabel(HG0509Msgs.Text_2);	// Stored Name Style:
		contents.add(lbl_Style, "cell 0 2, alignx right");	//$NON-NLS-1$

		lbl_CurrStyle = new JLabel(nameData[0]);
		contents.add(lbl_CurrStyle, "cell 1 2");	//$NON-NLS-1$

		JLabel lbl_SelStyle = new JLabel(HG0509Msgs.Text_3);	// Select new Name Style:
		contents.add(lbl_SelStyle, "cell 0 3, alignx right");	//$NON-NLS-1$

		contents.add(comboStyles, "cell 1 3");	//$NON-NLS-1$

		btn_Hidden = new JButton(HG0509Msgs.Text_4);	// Show Hidden
		btn_Hidden.setVisible(false);
		contents.add(btn_Hidden, "cell 1 3, gapx 10");	//$NON-NLS-1$

	// Setup editable Name table
		tablePerson = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				@Override
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				@Override
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        btn_Save.setEnabled(true);	// turn on Save button as soon as edit starts
				        nameChanged = true;
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
			};

		tablePerson.setFillsViewportHeight(true);

		// Create the table containing Person elements and values
		tablePerson.setModel(new DefaultTableModel(tablePersData, persHeaderData));		// 'Person Element'  'Value'
		// Make table single-click editable
		((DefaultCellEditor) tablePerson.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tablePerson.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

		tablePerson.getColumnModel().getColumn(0).setMinWidth(50);
		tablePerson.getColumnModel().getColumn(0).setPreferredWidth(120);
		tablePerson.getColumnModel().getColumn(1).setMinWidth(80);
		tablePerson.getColumnModel().getColumn(1).setPreferredWidth(280);
		tablePerson.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tablePerson.setAutoCreateColumnsFromModel(false);	// preserve column setup
		tablePerson.setIntercellSpacing(new Dimension(7,0));
		// Set header format
		JTableHeader persHeader = tablePerson.getTableHeader();
		persHeader.setOpaque(false);
		TableCellRenderer perendererFromHeader = tablePerson.getTableHeader().getDefaultRenderer();
		JLabel perheaderLabel = (JLabel) perendererFromHeader;
		perheaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel persSelectionModel = tablePerson.getSelectionModel();
		persSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Setup scrollpane
		JScrollPane persScrollPane = new JScrollPane(tablePerson);
		persScrollPane.setFocusTraversalKeysEnabled(false);
		persScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		persScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tablePerson, 0, tablePerson.getRowCount(), 1, 1);
		// Add to panel
		contents.add(persScrollPane, "cell 0 4 2");	//$NON-NLS-1$

	// Setup Source Citation area in col 2
		JLabel lbl_Citation = new JLabel(HG0509Msgs.Text_16);	// Citations:
		lbl_Citation.setFocusTraversalKeysEnabled(false);
		contents.add(lbl_Citation, "cell 2 3, alignx left, aligny center");		//$NON-NLS-1$

		JButton btn_Add = new JButton("+"); //$NON-NLS-1$
		btn_Add.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_Add.setMaximumSize(new Dimension(24, 24));
		btn_Add.setEnabled(true);
		contents.add(btn_Add, "cell 2 3, alignx right, aligny center, gapx 50"); //$NON-NLS-1$

		JButton btn_Del = new JButton("-"); //$NON-NLS-1$
		btn_Del.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_Del.setMaximumSize(new Dimension(24, 24));
		btn_Del.setEnabled(false);
		contents.add(btn_Del, "cell 2 3, aligny center"); //$NON-NLS-1$

		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_Up = new JButton(upArrow);
		btn_Up.setVerticalAlignment(SwingConstants.TOP);
		btn_Up.setToolTipText(HG0509Msgs.Text_17);		// Moves Citation up the list
		btn_Up.setMaximumSize(new Dimension(20, 20));
		btn_Up.setEnabled(false);
		contents.add(btn_Up, "cell 2 3, aligny center, gapx 10"); //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_Down = new JButton(downArrow);
		btn_Down.setVerticalAlignment(SwingConstants.TOP);
		btn_Down.setToolTipText(HG0509Msgs.Text_18);	// Moves Citation down the list
		btn_Down.setMaximumSize(new Dimension(20, 20));
		btn_Down.setEnabled(false);
		contents.add(btn_Down, "cell 2 3, aligny center"); //$NON-NLS-1$

		JLabel lbl_Surety = new JLabel(HG0509Msgs.Text_20);	// Surety
		contents.add(lbl_Surety, "cell 2 3, alignx right, aligny center, gapx 50");		//$NON-NLS-1$

	// Create scrollpane and table for the Name Citations
		objNameCiteData = pointCitationSourceHandler.getCitationSourceData(personNameTablePID, "T402");	//$NON-NLS-1$
		// and sort it on GUI sequence
		Arrays.sort(objNameCiteData, (o1, o2) -> Integer.compare((Integer) o1[4], (Integer) o2[4]));
		citeModel = new DefaultTableModel(objNameCiteData, citeHeaderData);
		JTable tableNameCite = new JTable(citeModel) {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
					// Force a minimum of a 9-row table, even if empty
					int r = super.getRowCount();
					if (r < 9) r = 9;
					  return new Dimension(super.getPreferredSize().width,
					    		r * super.getRowHeight());
					 }
				@Override
				public boolean isCellEditable(int row, int col) {
						return false;
			}};
		tableNameCite.getColumnModel().getColumn(0).setMinWidth(30);
		tableNameCite.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableNameCite.getColumnModel().getColumn(1).setMinWidth(100);
		tableNameCite.getColumnModel().getColumn(1).setPreferredWidth(190);
		tableNameCite.getColumnModel().getColumn(2).setMinWidth(80);
		tableNameCite.getColumnModel().getColumn(2).setPreferredWidth(80);
		tableNameCite.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader nameCiteHeader = tableNameCite.getTableHeader();
		nameCiteHeader.setOpaque(false);
		ListSelectionModel nameCiteSelectionModel = tableNameCite.getSelectionModel();
		nameCiteSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to Name panel
		tableNameCite.setFillsViewportHeight(true);
		JScrollPane nameCiteScrollPane = new JScrollPane(tableNameCite);
		nameCiteScrollPane.setFocusTraversalKeysEnabled(false);
		nameCiteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		nameCiteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	// Set Source#, Surety columns to be center-aligned
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableNameCite.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tableNameCite.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	// Set single row selection
		ListSelectionModel citeSelectionModel = tableNameCite.getSelectionModel();
		citeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup tabbing within table against all rows but only column 1
		if (tableNameCite.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableNameCite, 0, tableNameCite.getRowCount(), 1, 1);
		contents.add(nameCiteScrollPane, "cell 2 4, aligny top");	//$NON-NLS-1$

	// Setup Name type button
		JLabel lbl_NameType = new JLabel(HG0509Msgs.Text_12);		// Name Type is:
		contents.add(lbl_NameType, "cell 0 5, align right"); 		//$NON-NLS-1$
		String text;
		if (pointPersonHandler.getPrimaryName()) text = HG0509Msgs.Text_14; 	// Primary
				else text = HG0509Msgs.Text_15;								// Other
		btn_Primary = new JToggleButton(text);
		btn_Primary.setSelected(pointPersonHandler.getPrimaryName());
		if (btn_Primary.isSelected()) btn_Primary.setEnabled(false);
		contents.add(btn_Primary, "cell 1 5");	//$NON-NLS-1$

	// Setup Date fields
		JLabel lbl_DateStart = new JLabel(HG0509Msgs.Text_5);	// Start Date:
		contents.add(lbl_DateStart, "cell 0 6, align right");	//$NON-NLS-1$
		dateStart = new JTextField(nameData[1]);
		dateStart.setColumns(20);
		dateStart.setEditable(false);		// ensure field cannot be edited from keyboard
		dateStart.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		contents.add(dateStart, "cell 1 6");	//$NON-NLS-1$
		dateStart.setText(" " + pointPersonHandler.formatDateSelector(startMainYear, startMainDetails,  //$NON-NLS-1$
				startExtraYear, startExtraDetails).trim());

		JLabel lbl_DateEnd = new JLabel(HG0509Msgs.Text_6);		// End Date:
		contents.add(lbl_DateEnd, "cell 0 7, align right");		//$NON-NLS-1$
		dateEnd = new JTextField(nameData[2]);
		dateEnd.setColumns(20);
		dateEnd.setEditable(false);		// ensure field cannot be edited from keyboard
		dateEnd.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		contents.add(dateEnd, "cell 1 7");	//$NON-NLS-1$
		dateEnd.setText(" " + pointPersonHandler.formatDateSelector(endMainYear, endMainDetails,	//$NON-NLS-1$
				endExtraYear, endExtraDetails).trim());

	// Setup memo text area (scrollable) in col 2
		JLabel lbl_Memo = new JLabel(HG0509Msgs.Text_8);	// Memo:
		contents.add(lbl_Memo, "cell 2 5,alignx left");		//$NON-NLS-1$
		memoNameText = new JTextArea();
		memoNameText.setWrapStyleWord(true);
		memoNameText.setLineWrap(true);
		((DefaultCaret)memoNameText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$
	    memoNameText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoNameText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoNameText.setBorder(new JTable().getBorder());		// match Table border
	    // Setup memo scrollpane with textarea
		JScrollPane memoNameScroll = new JScrollPane(memoNameText);
		memoNameScroll.setMinimumSize(new Dimension(200, 50)); // set a starter size
		memoNameScroll.getViewport().setOpaque(false);
		memoNameScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoNameText.setCaretPosition(0);	// set scrollbar to top
		contents.add(memoNameScroll, "cell 2 6 1 3,grow");		//$NON-NLS-1$

	// Set Sentence Editor button
		JButton btn_Sentence = new JButton(HG0509Msgs.Text_21);	// Sentence Editor
		contents.add(btn_Sentence, "cell 1 8, alignx center");	//$NON-NLS-1$

	// Setup control buttons
		btn_Save = new JButton(HG0509Msgs.Text_9);			// Save
		btn_Save.setFocusTraversalKeysEnabled(false);
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 2 9, alignx right, gapx 20, tag ok");	//$NON-NLS-1$

		JButton btn_Close = new JButton(HG0509Msgs.Text_7);		// Close
		btn_Close.setFocusTraversalKeysEnabled(false);
		contents.add(btn_Close, "cell 2 9, alignx right, gapx 20, tag cancel");	//$NON-NLS-1$

	// Setup Order of components for Focus Policy
        Vector<Component> focusOrder = new Vector<>();
        focusOrder.add(tablePerson);
        focusOrder.add(dateStart);
        focusOrder.add(dateEnd);
        focusOrder.add(tableNameCite);
        focusOrder.add(memoNameText);
        contents.setFocusCycleRoot(true);
        contents.setFocusTraversalPolicy(new focusPolicy(focusOrder));
       	// Set initial focus of screen
    	tablePerson.requestFocusInWindow();
        pack();

/**************************************************************
 * ACTION LISTENERS
 **************************************************************/
		// Listener for clicking 'X on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});

		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If NOT in Save mode, just Close the screen
				if (!btn_Save.isEnabled() ) {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0509ManagePersonName");	//$NON-NLS-1$
					dispose();
				}
				// Else confirm exit without Save
				else {if (JOptionPane.showConfirmDialog(btn_Close, HG0509Msgs.Text_10		// There are unsaved changes. \n
																  +HG0509Msgs.Text_11,		// Do you wish to exit without Saving?
																   HG0509Msgs.Text_0,		// Manage Person Name
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0509ManagePersonName");	//$NON-NLS-1$
								dispose();	}	// yes option - exit
							}
					}
		});

		// Listener for changes made in tablePerson (name fields)
		TableModelListener persListener = new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
		                    String nameElementData = (String) tablePerson.getValueAt(row, 1);
							String element = (String) tablePerson.getValueAt(row, 0);
							if (HGlobal.DEBUG)
								System.out.println(" HG0509ManagePersonName - person table changed: " + row //$NON-NLS-1$
										 + " Element: " + element + "/" + nameElementData); 	//$NON-NLS-1$ //$NON-NLS-2$
						if (nameElementData != null) {
							btn_Save.setEnabled(true);
							nameChanged = true;
							pointPersonHandler.addToNameChangeList(row, nameElementData);
						}
                    }
				}
			}
		};
		tablePerson.getModel().addTableModelListener(persListener);

		// Listener for changes made in tableNameCite
/*		TableModelListener citeListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				btn_Save.setEnabled(true);
				citationChanged = true;
			}
		};
		tableNameCite.getModel().addTableModelListener(citeListener);
*/
		// Listener for Citation table mouse clicks
		tableNameCite.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
	           	if (me.getClickCount() == 1 && tableNameCite.getSelectedRow() != -1) {
	           	// SINGLE-CLICK - turn on table controls
	        		btn_Del.setEnabled(true);
	        		btn_Up.setEnabled(true);
	        		btn_Down.setEnabled(true);
	           	}
        		// DOUBLE_CLICK - get Object to pass to  Citation Editor and do so
	           	if (me.getClickCount() == 2 && tableNameCite.getSelectedRow() != -1) {
	           		int atRow = tableNameCite.getSelectedRow();
	           		objCiteDataToEdit = objNameCiteData[atRow]; // select whole row
	        	// Display HG0555EditCitation with this data
	           	// NB: keyAssocMin value defaulted to 1
					HG0555EditCitation citeScreen = new HG0555EditCitation(false, pointOpenProject, "T402", 1, (long)objCiteDataToEdit[3]);
					citeScreen.pointManagePersonName = pointManagePersonName;
					citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyCite = lbl_Style.getLocationOnScreen();
					citeScreen.setLocation(xyCite.x, xyCite.y + 30);
					citeScreen.setVisible(true);
					btn_Save.setEnabled(true);
	           	}
	        }
		});

		// Listener for Add Citation button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pointCitationSourceHandler.setCitedTableData("T402", personNameTablePID);	//$NON-NLS-1$
				// NB: keyAssocMin value defaulted to 1 in following line
				HG0555EditCitation citeScreen = new HG0555EditCitation(true, pointOpenProject, "T402", 1);
				citeScreen.pointManagePersonName = pointManagePersonName;
				citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyCite = lbl_Style.getLocationOnScreen();
				citeScreen.setLocation(xyCite.x, xyCite.y + 30);
				citeScreen.setVisible(true);
				btn_Save.setEnabled(true);
				citationOrderChanged = true;
			}
		});

		// Listener for Delete Citation button
		btn_Del.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
           		int atRow = tableNameCite.getSelectedRow();
           		objCiteDataToEdit = objNameCiteData[atRow]; // select whole row
           		try {
					pointCitationSourceHandler.deleteCitationRecord((long) objCiteDataToEdit[3]);
					citeModel.removeRow(atRow);
					pack();
				} catch (HBException hbe) {
					System.out.println("HG0509ManagePersonName delete citation error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});

		// Listener for move Citation up button
		btn_Up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = tableNameCite.getSelectedRow();
				// only allow move up if not at top of table
				if (selectedRow >= 1) {
				// Switch rows in citeModel
					citeModel.moveRow(selectedRow, selectedRow, selectedRow-1);
				// Switch rows in underlying objNameCiteData
					objTempCiteData = objNameCiteData[selectedRow-1];
					objNameCiteData[selectedRow-1] = objNameCiteData[selectedRow];
					objNameCiteData[selectedRow] = objTempCiteData;
				//  Reset visible selected row
				    tableNameCite.setRowSelectionInterval(selectedRow-1, selectedRow-1);
				    btn_Save.setEnabled(true);
					citationOrderChanged = true;
				}
			}
		});

		// Listener for move Citation down button
		btn_Down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int tableSize = tableNameCite.getRowCount();
				int selectedRow = tableNameCite.getSelectedRow();
				// only allow move down if not at end of table
				if (selectedRow < tableSize-1) {
				// Switch rows in tableModel
					citeModel.moveRow(selectedRow, selectedRow, selectedRow+1);
				// Switch rows in underlying objNameCiteData
					objTempCiteData = objNameCiteData[selectedRow];
					objNameCiteData[selectedRow] = objNameCiteData[selectedRow+1];
					objNameCiteData[selectedRow+1] = objTempCiteData;
				//  Reset visible selected row
					tableNameCite.setRowSelectionInterval(selectedRow+1, selectedRow+1);
					btn_Save.setEnabled(true);
					citationOrderChanged = true;
				}
			}
		});

		// Listener to turn on memoChanged flag after edit of Memo field
		textListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            	memoChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        memoNameText.getDocument().addDocumentListener(textListen);

		// Listener for 'Show Hidden' button
		btn_Hidden.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = comboStyles.getSelectedIndex();
				DefaultTableModel persModel = (DefaultTableModel) tablePerson.getModel();

				// Temporarily disable tablePerson listener while Hidden changes done
				persModel.removeTableModelListener(persListener);
				persModel.setNumRows(0);	// clear table
				try {
					pointPersonHandler.setNameStyleIndex(index);
					// if first click of 'Show Hidden', show hidden items
					if (!showHiddenClicked) {
							showHiddenClicked = true;
							pointPersonHandler.updateManagePersonNameTable();
						}
					// else if 2nd click, go back to previous item list
					else {
							showHiddenClicked = false;
							pointPersonHandler.updateManagePersonNameTable(personNameTablePID);
						}
					persHeaderData = pointPersonHandler.getPersonTableHeader();
					tablePersData = pointPersonHandler.getPersonNameTable();
					persModel.setDataVector(tablePersData, persHeaderData);
				} catch (HBException hbe) {
					System.out.println("HG0509ManagePersonName style error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
				// re-enable tablePerson listener
				persModel.addTableModelListener(persListener);
			}
		});

		// NameEvent type combo-box listener
		comboEvents.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedNmaeTypeIndex = comboEvents.getSelectedIndex();
				nameEventType = nameTypes[selectedNmaeTypeIndex];
				btn_Save.setEnabled(true);
				eventTypeChanged = true;
			}
		});

		// NameStyle combo-box listener
		comboStyles.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = comboStyles.getSelectedIndex();
				DefaultTableModel persModel = (DefaultTableModel) tablePerson.getModel();
			// Temporarily disable tablePerson listener while Style changes
				persModel.removeTableModelListener(persListener);
				persModel.setNumRows(0);	// clear table

				try {
					pointPersonHandler.setNameStyleIndex(index);
					pointPersonHandler.updateManagePersonNameTable(personNameTablePID);
					persHeaderData = pointPersonHandler.getPersonTableHeader();
					tablePersData = pointPersonHandler.getPersonNameTable();
					persModel.setDataVector(tablePersData, persHeaderData);
				// do test here - if elements are hidden, then show Hidden button
					if (pointPersonHandler.detectHiddenElements()) {
			 						btn_Hidden.setVisible(true);
			 						btn_Hidden.setEnabled(true);
			  		} else btn_Hidden.setVisible(false);
					btn_Save.setEnabled(true);
					styleChanged = true;
					clearPersonTableData();
				} catch (HBException hbe) {
					System.out.println("HG0509ManagePersonName style error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
				persModel.setRowCount(tablePersData.length);	// reset # table rows
				pack();	// reset screen size
			// re-enable tablePerson listener
				persModel.addTableModelListener(persListener);
			}
		});

		// Primary name toggle listener
		btn_Primary.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (btn_Primary.isSelected()) btn_Primary.setText(HG0509Msgs.Text_14); 		// Primary
					else btn_Primary.setText(HG0509Msgs.Text_15);							// Other
				if (btn_Primary.getText().equals(HG0509Msgs.Text_14))  setPrimary = true;	// Primary
				btn_Save.setEnabled(true);
				nameChanged = true;
			}
		});

		// Listener for Sentence Editor button
		btn_Sentence.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE04 need code here to allow sentence editing
				JOptionPane.showMessageDialog(btn_Sentence, "This function is not yet implemented");	//$NON-NLS-1$
			}
		});

	// Listener to Start date to enable save button
		dateStart.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editStartDate != null)
            		editStartDate.convertFromHDate(startMainYear, startMainDetails, startExtraYear, startExtraDetails);
            	else {
            		editStartDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editStartDate.dateType = 1;  // set start date
            		if (startHREDate != null)
            			editStartDate.convertFromHDate(startMainYear, startMainDetails, startExtraYear, startExtraDetails);
            	}
           // 	Set position and set visible
    			editStartDate.setModalityType(ModalityType.APPLICATION_MODAL);
    			Point xyDate = dateStart.getLocationOnScreen();
    			editStartDate.setLocation(xyDate.x, xyDate.y-100);
    			editStartDate.setVisible(true);
            }
        });

	// Listener to End date to enable save button
		dateEnd.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editEndDate != null)
            		editEndDate.convertFromHDate(endMainYear, endMainDetails, endExtraYear, endExtraDetails);
            	else {
            		editEndDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editEndDate.dateType = 2;  // set end date
            		if (endHREDate != null)
            			editEndDate.convertFromHDate(endMainYear, endMainDetails, endExtraYear, endExtraDetails);
            	}
           // 	Set positin and set visible
    			editEndDate.setModalityType(ModalityType.APPLICATION_MODAL);
    			Point xyDate = dateEnd.getLocationOnScreen();
    			editEndDate.setLocation(xyDate.x, xyDate.y-100);
    			editEndDate.setVisible(true);
            }
        });
	}	// End HG0509ManagePersonName constructor

/**
 * resetCitationTable()
 * @throws HBException
 */
	public void resetCitationTable() throws HBException {
		objNameCiteData = pointCitationSourceHandler.getCitationSourceData(personNameTablePID, "T402");	//$NON-NLS-1$
		citeModel.setDataVector(objNameCiteData, citeHeaderData);
	}
/**
 * saveStartDate()
 */
	@Override
	public void saveStartDate() {
		if (editStartDate != null) {
			startMainYear = editStartDate.returnYearToGUI();
			startMainDetails = editStartDate.returnDetailToGUI().trim();
			startExtraYear = editStartDate.returnYear2ToGUI();
			startExtraDetails = editStartDate.returnDetail2ToGUI().trim();
			startSortCode = editStartDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			dateStart.setText(" " + pointPersonHandler.formatDateSelector(startMainYear, startMainDetails,  //$NON-NLS-1$
					startExtraYear, startExtraDetails).trim());
		}
		if (startHREDate == null) startHREDate = new Object[5];
		startHREDate[0] = startMainYear;
		startHREDate[1] = startMainDetails;
		startHREDate[2] = startExtraYear;
		startHREDate[3] = startExtraDetails;
		startHREDate[4] = startSortCode;
		startDateOK = true;

	}
/**
 * saveEndDate()
 */
	@Override
	public void saveEndDate() {
		if (editEndDate != null) {
			endMainYear = editEndDate.returnYearToGUI();
			endMainDetails = editEndDate.returnDetailToGUI().trim();
			endExtraYear = editEndDate.returnYear2ToGUI();
			endExtraDetails = editEndDate.returnDetail2ToGUI().trim();
			endSortCode = editEndDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			dateEnd.setText(" " + pointPersonHandler.formatDateSelector(endMainYear, endMainDetails,	//$NON-NLS-1$
					endExtraYear, endExtraDetails).trim());
		}
		if (endHREDate == null) endHREDate = new Object[5];
		endHREDate[0] = endMainYear;
		endHREDate[1] = endMainDetails;
		endHREDate[2] = endExtraYear;
		endHREDate[3] = endExtraDetails;
		endHREDate[4] = endSortCode;
		endDateOK = true;
	}

/**
 * errorJOptionMessage(String title, String errorMess)
 * @param title
 * @param errorMess
 */
	public void errorJOptionMessage(String title, String errorMess) {
		JOptionPane.showMessageDialog(contents, errorMess, title, JOptionPane.ERROR_MESSAGE);
	}

/**
 * clearPersonTableData() - clear name data in JTable tablePerson for add new name
 */
	protected void clearPersonTableData() {
		for (int i = 0; i < tablePersData.length; i++)
			tablePersData[i][1] = "";	//$NON-NLS-1$

		DefaultTableModel persModel = (DefaultTableModel) tablePerson.getModel();
		persModel.setDataVector(tablePersData, persHeaderData);
		pack();
	}

}  // End of HG0509ManagePersonName
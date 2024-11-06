package hre.gui;
/*******************************************************************************
 * Manage Location - Specification 05.08 GUI_Manage_Location
 * v0.01.0027 2022-06-20 First draft (D Ferguson)
 * 			  2022-08-02 Imported TMG style data (N. Tolleshaug)
 * 			  2022-08-03 Change location name style (N. Tolleshaug)
 * 			  2022-08-05 Add save/Close buttons (D Ferguson)
 * v0.01.0028 2022-12-12 Add a 'Show Hidden' button and actions (D Ferguson)
 * 			  2023-02-08 Implemented show and store location name style (N. Tolleshaug)
 * 			  2023-02-09 Added Save button specific to Style change (D Ferguson)
 * 			  2023-03-04 Add Exhibit handling into media card (D Ferguson)
 * 			  2023-03-10 Add Caption handling into media card (N. Tolleshaug)
 * v0.03.0030 2023-09-13 Add scroll and image/text icons to media card (D Ferguson)
 * v0.03.0031 2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-10-25 make date fields non-editable by keyboard (D Ferguson)
 * 			  2024-11-03 Removed SwingUtility for table cell focus (D Ferguson)
 ********************************************************************************
 * NB: cannot execute 'externalize strings' check without temporarily commenting
 *     out the statement: txt_Text = new JTextArea("\""+listText+" ...\"");
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE01 - need to handle Notepads eventually
 *******************************************************************************
 */

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBMediaHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.gui.HGlobalCode.focusPolicy;
import hre.nls.HG0508Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Location
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2022-06-20
 */
public class HG0508ManageLocation extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBWhereWhenHandler pointWhereWhenHandler;
	public static String screenID = "50800";	//$NON-NLS-1$
	private String className;

	private JPanel contents;
	boolean showHiddenClicked = false;
	boolean locationChanged = false;
	boolean dateChanged = false;
	boolean startDateOK = false;
	boolean endDateOK = false;

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
	long endHDatePID;

	JTextField dateStart, dateEnd;

    static focusPolicy newPolicy;

	Object[][] tableLocnData;
	String[] locnHeaderData;
	String [] nameData;

	ArrayList<ImageIcon> listImages;
	ArrayList<String> listImagesCaptions;
	ArrayList<String> listTexts;

	// Icons for media display
    Icon placeIcon = new ImageIcon(getClass().getResource("/hre/images/places-48.png")); //$NON-NLS-1$
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
 * Create the dialog
 * @throws HBException
 */
	public HG0508ManageLocation(HBWhereWhenHandler pointWhereWhenHandler
							, HBProjectOpenData pointOpenProject
							, String screenID
							, long locationNamePID) throws HBException {
		this.pointWhereWhenHandler = pointWhereWhenHandler;
	// Set pointOpenproject in super - HG0450SuperDialog
		this.pointOpenProject = pointOpenProject;

	// Setup references for HG0508ManageLocation
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0508ManageLocation");	//$NON-NLS-1$
		windowID = screenID;
		helpName = "managelocation";	//$NON-NLS-1$
    	HG0508ManageLocation.screenID = screenID;
    	className = getClass().getSimpleName();
    	this.setResizable(true);
    	int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
    	String selectString =
    			pointWhereWhenHandler.setSelectSQL("*", pointWhereWhenHandler.locationNameTable,"PID = " + locationNamePID); //$NON-NLS-1$ //$NON-NLS-2$
    	ResultSet personNameTable = pointWhereWhenHandler.requestTableData(selectString, dataBaseIndex);
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
    	startHREDate = pointWhereWhenHandler.pointLibraryResultSet.dateIputHdate(startHDatePID, dataBaseIndex);
    	if (startHREDate != null) {
			startMainYear = (long) startHREDate[0];
			startMainDetails = (String) startHREDate[1];
			startExtraYear = (long) startHREDate[2];
			startExtraDetails = (String) startHREDate[3];
			startSortCode = (String) startHREDate[4];
    	}

    // Collect end date Hdate
    	endHREDate = pointWhereWhenHandler.pointLibraryResultSet.dateIputHdate(endHDatePID, dataBaseIndex);
    	if (endHREDate != null) {
			endMainYear = (long) endHREDate[0];
			endMainDetails = (String) endHREDate[1];
			endExtraYear = (long) endHREDate[2];
			endExtraDetails = (String) endHREDate[3];
			endSortCode = (String) endHREDate[4];
    	}

   	 // Get Name style
	    nameData = pointWhereWhenHandler.getNameData();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    setTitle(HG0508Msgs.Text_0 + pointOpenProject.getProjectName());		// Manage Locations in Project

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[grow]", "[grow]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/***********************************
 * Setup leftPanel and its contents
 **********************************/
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(leftPanel, "cell 0 0, growx, aligny top");	//$NON-NLS-1$
		leftPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EditType = new JLabel(HG0508Msgs.Text_1);	// Edit Category
		leftPanel.add(lbl_EditType, "cell 0 0, alignx center");	//$NON-NLS-1$

		JRadioButton radio_Locn = new JRadioButton(HG0508Msgs.Text_2);	// Location
		radio_Locn.setSelected(true);
		leftPanel.add(radio_Locn, "cell 0 1, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Media = new JRadioButton(HG0508Msgs.Text_3);	// Media
		leftPanel.add(radio_Media, "cell 0 2, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Note = new JRadioButton(HG0508Msgs.Text_4);	// Notepads
		leftPanel.add(radio_Note, "cell 0 3, alignx left");		//$NON-NLS-1$

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radio_Locn);
		radioGroup.add(radio_Media);
		radioGroup.add(radio_Note);

/***************************************************
 * Setup rightPanel and a CardLayout of Panels in it
 **************************************************/
		JPanel rightPanel = new JPanel(new CardLayout());
		rightPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(rightPanel, "cell 1 0, grow");	//$NON-NLS-1$
		// Define cards of the CardLayout, each card-Panel with its own layout manager
		JPanel cardLocn = new JPanel();
		cardLocn.setLayout(new MigLayout("", "[]10[grow]", "[]10[]10[]10[grow]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardLocn, "LOCATION");	//$NON-NLS-1$
		JPanel cardMedia = new JPanel();
		cardMedia.setLayout(new MigLayout("", "[]", "[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardMedia, "MEDIA");	//$NON-NLS-1$
		JPanel cardNotepads = new JPanel();
		cardNotepads.setLayout(new MigLayout("", "[grow]", "[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardNotepads, "NOTEPADS");	//$NON-NLS-1$

/**********************************
 * Setup bottom row for Save/Close
 **********************************/
		JButton btn_Close = new JButton(HG0508Msgs.Text_5);		// Close
		btn_Close.setFocusTraversalKeysEnabled(false);
		contents.add(btn_Close, "cell 0 1 2, alignx right, gapx 20, tag cancel");	//$NON-NLS-1$

		JButton btn_SaveStyle = new JButton(HG0508Msgs.Text_6);		// Save Name Style
		btn_SaveStyle.setFocusTraversalKeysEnabled(false);
		btn_SaveStyle.setEnabled(false);
		contents.add(btn_SaveStyle, "cell 0 1 2, alignx right, gapx 20, tag ok");		//$NON-NLS-1$

		JButton btn_SaveNameDate = new JButton(HG0508Msgs.Text_7);		// Save Name/Date Changes
		btn_SaveNameDate.setFocusTraversalKeysEnabled(false);
		btn_SaveNameDate.setEnabled(false);
		contents.add(btn_SaveNameDate, "cell 0 1 2, alignx right, gapx 20, tag ok");		//$NON-NLS-1$

/****************************************************
* cardLocn - load data into the Location card
*****************************************************/
		JLabel lbl_Instruction = new JLabel(HG0508Msgs.Text_8);		// You may change Location Style and/or edit its Element parts and Dates.
		cardLocn.add(lbl_Instruction, "cell 0 0 2 1");				//$NON-NLS-1$

		JLabel lbl_Style = new JLabel(HG0508Msgs.Text_9);			// Stored Location Style:
		cardLocn.add(lbl_Style, "cell 0 1, alignx right");			//$NON-NLS-1$

		JLabel lbl_CurrStyle = new JLabel(nameData[0]);
		cardLocn.add(lbl_CurrStyle, "cell 1 1");	//$NON-NLS-1$

		JLabel lbl_SelStyle = new JLabel(HG0508Msgs.Text_10);		// Select new Location Style:
		cardLocn.add(lbl_SelStyle, "cell 0 2, align right");		//$NON-NLS-1$

		DefaultComboBoxModel<String> comboNStyles =
				new DefaultComboBoxModel<>(pointWhereWhenHandler.getAvailableStyles());

		JComboBox<String> locnStyles = new JComboBox<>(comboNStyles);
		// Load all styles available
		locnStyles.setSelectedIndex(pointWhereWhenHandler.getDefaultIndex());
		cardLocn.add(locnStyles, "cell 1 2");	//$NON-NLS-1$

		JButton btn_Hidden = new JButton(HG0508Msgs.Text_11);	// Show Hidden
		btn_Hidden.setVisible(false);
		cardLocn.add(btn_Hidden, "cell 1 2, gapx 20");	//$NON-NLS-1$

		JTable tableLocation = new JTable() {
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
				        btn_SaveNameDate.setEnabled(true);	// turn on Save button as soon as edit starts
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if  (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));	//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));	//$NON-NLS-1$
					return cell;
				}
			};
		tableLocation.setFillsViewportHeight(true);

		// Match the currently stored Style against the combo-box entries
		// to be able to show the correct current values
		int ii = 0;
		for (ii = 0; ii < locnStyles.getItemCount(); ii++) {
			locnStyles.setSelectedIndex(ii);
			if (nameData[0].trim().equals(locnStyles.getSelectedItem().toString().trim())) break;
		}
		// Make sure we haven't got to loop end with no match
		if (ii == locnStyles.getItemCount()) ii = 0;
		// Get data for the Location elements and values
		pointWhereWhenHandler.setNameStyleIndex(ii);
		pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID);
		locnHeaderData = pointWhereWhenHandler.getLocationTableHeader();
		tableLocnData = pointWhereWhenHandler.getLocationNameTable();

		// Test the location data against the Name style used - if there are elements
		// not showing due to the Name Style ignoring them, show the 'Show Hidden' button
		if (pointWhereWhenHandler.detectHiddenElements()) {
 						btn_Hidden.setVisible(true);
 						btn_Hidden.setEnabled(true);
  		} else btn_Hidden.setVisible(false);

		// Create the table containing Location elements and values
		tableLocation.setModel(new DefaultTableModel(tableLocnData,
													locnHeaderData			// 'Location Element'  'Value'
													));
		// Make table single-click editable
		((DefaultCellEditor) tableLocation.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableLocation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);		//$NON-NLS-1$

		tableLocation.getColumnModel().getColumn(0).setMinWidth(50);
		tableLocation.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableLocation.getColumnModel().getColumn(1).setMinWidth(80);
		tableLocation.getColumnModel().getColumn(1).setPreferredWidth(310);
		tableLocation.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableLocation.setAutoCreateColumnsFromModel(false);	// preserve column setup
		tableLocation.setIntercellSpacing(new Dimension(7,0));
	// Set header format
		JTableHeader locnHeader = tableLocation.getTableHeader();
		locnHeader.setOpaque(false);
		TableCellRenderer lorendererFromHeader = tableLocation.getTableHeader().getDefaultRenderer();
		JLabel loheaderLabel = (JLabel) lorendererFromHeader;
		loheaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel locnSelectionModel = tableLocation.getSelectionModel();
		locnSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   // Setup scrollpane
		JScrollPane locnScrollPane = new JScrollPane(tableLocation);
		locnScrollPane.setFocusTraversalKeysEnabled(false);
		locnScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		locnScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableLocation, 0, tableLocation.getRowCount(), 1, 1);
   // Add to panel
		cardLocn.add(locnScrollPane, "cell 0 3 2, grow");	//$NON-NLS-1$

		JLabel lbl_DateStart = new JLabel(HG0508Msgs.Text_12);	// Start Date:
		cardLocn.add(lbl_DateStart, "cell 0 4, align right");	//$NON-NLS-1$
		dateStart = new JTextField(nameData[1]);
		dateStart.setColumns(22);
		dateStart.setEditable(false);		// ensure field cannot be edited from keyboard
		dateStart.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		cardLocn.add(dateStart, "cell 1 4");	//$NON-NLS-1$
		dateStart.setText(" " + pointWhereWhenHandler.formatDateSelector(startMainYear, startMainDetails,  //$NON-NLS-1$
				startExtraYear, startExtraDetails).trim());

		JLabel lbl_DateEnd = new JLabel(HG0508Msgs.Text_13);	// End Date:
		cardLocn.add(lbl_DateEnd, "cell 0 5, align right");		//$NON-NLS-1$
		dateEnd = new JTextField(nameData[2]);
		dateEnd.setColumns(22);
		dateEnd.setEditable(false);		// ensure field cannot be edited from keyboard
		dateEnd.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		cardLocn.add(dateEnd, "cell 1 5");	//$NON-NLS-1$
		dateEnd.setText(" " + pointWhereWhenHandler.formatDateSelector(endMainYear, endMainDetails,	//$NON-NLS-1$
				endExtraYear, endExtraDetails).trim());

	// Setup Order of components for Focus Policy
        Vector<Component> focusOrder = new Vector<>();
        focusOrder.add(tableLocation);
        focusOrder.add(dateStart);
        focusOrder.add(dateEnd);
        contents.setFocusCycleRoot(true);
        contents.setFocusTraversalPolicy(new focusPolicy(focusOrder));
       	// Set initial focus of screen
    	tableLocation.requestFocusInWindow();

/*********************************************************************
 * cardMedia - load data into a scrollpane on the Media card
 ********************************************************************/
    // Define a media JPanel
    	JPanel mediaPanel = new JPanel();

		HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
	// Add Location Images to the mediaPanel
		listImages = pointMediaHandler.getImageList();
		listImagesCaptions = pointMediaHandler.getImageCaptionList();
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
	    		JTextArea txt_Image = new JTextArea(listImagesCaptions.get(i));
	    		txt_Image.setLineWrap(true);
	    		txt_Image.setWrapStyleWord(true);
        		txt_Image.setEditable(false);
	    		txt_Image.setSize(300,22);
	    		int lines = txt_Image.getLineCount();
	    		if (lines > 5) lines = 5;
	    		JScrollPane captionPane = new JScrollPane(txt_Image);
	    		captionPane.setPreferredSize(new Dimension(300, 22*lines));
	    		imagePanel.add(captionPane, "cell 0 1");		//$NON-NLS-1$
	    		mediaPanel.add(imagePanel);
            }
        }
        else {	// Add place icon with 'not present' msg
        	JPanel imagePanel = new JPanel();
    		imagePanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		imagePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            JLabel lbl_Image = new JLabel(placeIcon);
    		imagePanel.add(lbl_Image, "cell 0 0, alignx center");	//$NON-NLS-1$
    		JTextArea txt_Image = new JTextArea(HG0508Msgs.Text_25);		//   No Images present
    		txt_Image.setLineWrap(true);
    		txt_Image.setWrapStyleWord(true);
    		txt_Image.setEditable(false);
    		txt_Image.setSize(150,22);
    		imagePanel.add(txt_Image, "cell 0 1");		//$NON-NLS-1$
    		mediaPanel.add(imagePanel);
        }

     // Add Text type files
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
        		if (!listText.startsWith("Filepath:"))				//$NON-NLS-1$
        			txt_Text = new JTextArea("\""+listText+" ...\"");	//$NON-NLS-1$ //$NON-NLS-2$
        			else txt_Text = new JTextArea(listText);
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
    		JTextArea txt_Text = new JTextArea(HG0508Msgs.Text_26);		//   No Text files present
    		txt_Text.setLineWrap(true);
    		txt_Text.setWrapStyleWord(true);
    		txt_Text.setEditable(false);
    		txt_Text.setSize(150,22);
    		textPanel.add(txt_Text, "cell 0 1");		//$NON-NLS-1$
    		mediaPanel.add(textPanel);
        }

     // Add dummy entries for Audio/video until MediaHandler can deliver them
   		JPanel audioPanel = new JPanel();
		audioPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		audioPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Audio = new JLabel(audioIcon);
		audioPanel.add(lbl_Audio, "cell 0 0, alignx center");		//$NON-NLS-1$
		JTextArea txt_Audio = new JTextArea(HG0508Msgs.Text_16);	// No Audio files present
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
		videoPanel.add(lbl_Video, "cell 0 0, alignx center");		//$NON-NLS-1$
		JTextArea txt_Video = new JTextArea(HG0508Msgs.Text_17);	// No Video files present
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
	            int pref_width = tableLocation.getPreferredSize().width + 20;	// add room for insets
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
		cardMedia.add(mediaScrollPane);

/*************************************************************
 * cardNotepads - put comment into the Notepads card
 *************************************************************/
	// NOTE01 - real code to be done later
		JLabel lbl_Notepads = new JLabel(HG0508Msgs.Text_18);	// Notepads to be able to be loaded and edited here eventually
		cardNotepads.add(lbl_Notepads);

    // Display the screen
		setVisible(true);
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
				if (!btn_SaveNameDate.isEnabled() & !btn_SaveStyle.isEnabled()) {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0508ManageLocation");	//$NON-NLS-1$
					closeActions();
					dispose();
				}
				// Else confirm exit without Save
				else {if (JOptionPane.showConfirmDialog(btn_Close, HG0508Msgs.Text_19		// There are unsaved Location Name changes. \n
																  +HG0508Msgs.Text_20,		// Do you wish to exit without Saving?
																   HG0508Msgs.Text_21,		// Manage Location Name
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0508ManageLocation");		//$NON-NLS-1$
							 // Perform close actions to save data into T302
								closeActions();
								dispose();	}	// yes option - exit
							}
					}
		});

		// Listener for SaveStyle button
		btn_SaveStyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Save the new name style in DB
				int selectIndex = locnStyles.getSelectedIndex();
				try {
					pointWhereWhenHandler.updateStoredNameStyle(selectIndex, locationNamePID);
				} catch (HBException hbe) {
					System.out.println("HG0508ManageLocation Save stored name style error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.logWrite("ERROR Stored Name Style : " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.printStackTraceToFile(hbe);
				}
			// Save new name style on screen
				lbl_Style.setText(HG0508Msgs.Text_22);		// Saved Name Style:
				lbl_CurrStyle.setText(String.valueOf(locnStyles.getSelectedItem()));
				btn_SaveStyle.setEnabled(false);
			}
		});

		// Listener for SaveName/Date button
		btn_SaveNameDate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TableModelEvent(tableLocation.getModel());
				try {
					if (locationChanged) {
					// Update name element table T403
						pointWhereWhenHandler.updateLocationElementData(locationNamePID);
						locationChanged = false;
					}
				// Save Start/End Dates if they changed
					if (startDateOK) pointWhereWhenHandler.createLocationNameDates(true, locationNamePID, "START_HDATE_RPID", startHREDate); //$NON-NLS-1$
					if (endDateOK) pointWhereWhenHandler.createLocationNameDates(true, locationNamePID, "END_HDATE_RPID", endHREDate);		 //$NON-NLS-1$

				} catch (HBException hbe) {
					System.out.println(" HG0508ManageLocation: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
					JOptionPane.showMessageDialog(btn_SaveNameDate, HG0508Msgs.Text_23 +  hbe.getMessage(), 	// Date format error: \n
											HG0508Msgs.Text_24, JOptionPane.ERROR_MESSAGE);			// Date input checker
					if (HGlobal.writeLogs) HB0711Logging.logWrite("ERROR Date format: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.printStackTraceToFile(hbe);
				}
			// Set reset location data
				pointWhereWhenHandler.resetLocationSelect(pointOpenProject);
				btn_SaveNameDate.setEnabled(false);
			}
		});

		// Listener for changes made in tableLocation
		TableModelListener locnListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				TableModel model = (TableModel)e.getSource();
				String nameElementData = (String) model.getValueAt(tableLocation.getSelectedRow(), 1);
				String element = (String) model.getValueAt(tableLocation.getSelectedRow(), 0);
				if (HGlobal.DEBUG)
						System.out.println("HG0508ManageLocation - table changed: "+ tableLocation.getSelectedRow() //$NON-NLS-1$
								 + " Element: " + element + "/" + nameElementData); 	//$NON-NLS-1$ //$NON-NLS-2$
				pointWhereWhenHandler.addToNameChangeList(tableLocation.getSelectedRow(), nameElementData);
				locationChanged = true;
				btn_SaveNameDate.setEnabled(true);
			}
		};
		tableLocation.getModel().addTableModelListener(locnListener);

		// Listener to Start date to enable save button
		dateStart.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				btn_SaveNameDate.setEnabled(true);
          // Initiate edit date window and preload
            	if (editStartDate != null)
            		editStartDate.convertFromHDate(startMainYear, startMainDetails, startExtraYear, startExtraDetails);
            	else {
            		editStartDate = new HG0590EditDate(pointOpenDisplay, false, false, true, false);
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
				btn_SaveNameDate.setEnabled(true);
                // Initiate edit date window and preload
            	if (editEndDate != null)
            		editEndDate.convertFromHDate(endMainYear, endMainDetails, endExtraYear, endExtraDetails);
            	else {
            		editEndDate = new HG0590EditDate(pointOpenDisplay, false, false, true, false);
            		editEndDate.dateType = 2;  // set end date
            		if (endHREDate != null)
            			editEndDate.convertFromHDate(endMainYear, endMainDetails, endExtraYear, endExtraDetails);
            	}
           // 	Set position and set visible
    			editEndDate.setModalityType(ModalityType.APPLICATION_MODAL);
    			Point xyDate = dateEnd.getLocationOnScreen();
    			editEndDate.setLocation(xyDate.x, xyDate.y-100);
    			editEndDate.setVisible(true);
            }
        });

		// Listener for 'Show Hidden' button
		btn_Hidden.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = locnStyles.getSelectedIndex();
				DefaultTableModel locnModel = (DefaultTableModel) tableLocation.getModel();
				// Temporarily disable tableLocation listener while Style changes
				locnModel.removeTableModelListener(locnListener);
				locnModel.setNumRows(0);	// clear table
				try {
					pointWhereWhenHandler.setNameStyleIndex(index);
					// if first click of 'Show Hidden', show hidden items
					if (!showHiddenClicked) {
							showHiddenClicked = true;
							pointWhereWhenHandler.updateManageLocationNameTable();
						}
					// else if 2nd click, go back to previous item list
					else {
							showHiddenClicked = false;
							pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID);
						}
					locnHeaderData = pointWhereWhenHandler.getLocationTableHeader();
					tableLocnData = pointWhereWhenHandler.getLocationNameTable();
					locnModel.setDataVector(tableLocnData, locnHeaderData);
				} catch (HBException hbe) {
					System.out.println("HG0508ManageLocation style error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.logWrite("ERROR Location Style: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.printStackTraceToFile(hbe);
				}
				// re-enable tableLocation listener
				locnModel.addTableModelListener(locnListener);
			}
		});

		// NameStyle combo-box listener
		locnStyles.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = locnStyles.getSelectedIndex();
				DefaultTableModel locnModel = (DefaultTableModel) tableLocation.getModel();
				// Temporarily disable tableocation listener while Style changes
				locnModel.removeTableModelListener(locnListener);
				locnModel.setNumRows(0);	// clear table
				try {
					pointWhereWhenHandler.setNameStyleIndex(index);
					pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID);
					locnHeaderData = pointWhereWhenHandler.getLocationTableHeader();
					tableLocnData = pointWhereWhenHandler.getLocationNameTable();
					locnModel.setDataVector(tableLocnData, locnHeaderData);
					// Do a test here - if elements are hidden, then show Hidden button
					if (pointWhereWhenHandler.detectHiddenElements()) {
			 						btn_Hidden.setVisible(true);
			 						btn_Hidden.setEnabled(true);
			  		} else btn_Hidden.setVisible(false);
					btn_SaveStyle.setEnabled(true);
				} catch (HBException hbe) {
					System.out.println(" HG0508ManageLocation style error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.logWrite("ERROR Location Style: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) HB0711Logging.printStackTraceToFile(hbe);
				}
				locnModel.setRowCount(tableLocnData.length);	// reset # table rows
				pack();	// reset screen size
				// re-enable tableLocation listener
				locnModel.addTableModelListener(locnListener);
			}
		});

/**
 * Listeners for radio button group items, to show each rightPanel card
 */
		 ActionListener actionRadioLocn = new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "LOCATION");	//$NON-NLS-1$
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
		// Link the listeners above to the radio buttons
		radio_Locn.addActionListener(actionRadioLocn );
		radio_Media.addActionListener(actionRadioMedia);
		radio_Note.addActionListener(actionRadioNote);

	}	// End HG0508ManageLocation constructor

	@Override
	public void saveStartDate() {
		if (editStartDate != null) {
			startMainYear = editStartDate.returnYearToGUI();
			startMainDetails = editStartDate.returnDetailToGUI().trim();
			startExtraYear = editStartDate.returnYear2ToGUI();
			startExtraDetails = editStartDate.returnDetail2ToGUI().trim();
			startSortCode = editStartDate.returnSortString().trim();
			pointWhereWhenHandler.setUpDateTranslation();
			dateStart.setText(" " + pointWhereWhenHandler.formatDateSelector(startMainYear, startMainDetails,  //$NON-NLS-1$
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

	@Override
	public void saveEndDate() {
		if (editEndDate != null) {
			endMainYear = editEndDate.returnYearToGUI();
			endMainDetails = editEndDate.returnDetailToGUI().trim();
			endExtraYear = editEndDate.returnYear2ToGUI();
			endExtraDetails = editEndDate.returnDetail2ToGUI().trim();
			endSortCode = editEndDate.returnSortString().trim();
			pointWhereWhenHandler.setUpDateTranslation();
			dateEnd.setText(" " + pointWhereWhenHandler.formatDateSelector(endMainYear, endMainDetails,	//$NON-NLS-1$
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
 * This performs the necessary closedown actions to shut Reminder
 * and save all screen info size/position data into T302
 */
		private void closeActions() {
			// Close reminder display
			if (reminderDisplay != null) reminderDisplay.dispose();
			// Set frame size in GUI data
			Dimension frameSize = getSize();
			pointOpenProject.setSizeScreen(screenID,frameSize);
			// Set position	in GUI data
			Point position = getLocation();
			pointOpenProject.setPositionScreen(screenID,position);
			// Set class name in GUI configuration data
			pointOpenProject.setClassName(screenID,"HG0508ManageLocation"); //$NON-NLS-1$
			// Mark the screen as closed in T302
			pointOpenProject.closeStatusScreen(screenID);
		}	// End closeActions

}  // End of HG0508ManageLocation
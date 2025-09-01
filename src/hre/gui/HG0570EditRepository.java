package hre.gui;
/*******************************************************************************
 * Edit Repository
 * v0.04.0032 2025-02-01 First draft (D Ferguson)
 *			  2025-05-26 Adjust miglayout sub-panel sizes (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE00 - needs WhWhHandler etc defined to be able to pickup location styles and data
 * NOTE01 needs Save code
 *
 ********************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.gui.HGlobalCode.focusPolicy;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Repository
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-02-01
 */

public class HG0570EditRepository extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBPersonHandler pointPersonHandler;

	public String screenID = "57000";	//$NON-NLS-1$

	private JPanel contents;

    // For Text area font setting
    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

    JTextArea memoText, fullNameText;
	DocumentListener memoTextChange, nameTextChange;
    boolean memoEdited, nameEdited = false; 		// Signals memo/name edited
    boolean changedLocationNameStyle = false;   	// Signals name style changed

    JComboBox<String> locationNameStyles;
    TableModelListener tableLocationListener;

    JTable tableLocation;
    Object[][] tableLocationData;
    String[] tableLocationHeader;

/**
 * Create the dialog
 */
	public HG0570EditRepository(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();

	// Setup references
		windowID = screenID;
		helpName = "editrepository";	//$NON-NLS-1$

    	setResizable(false);
    	setTitle("Edit Repository Details");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0570EditRepository");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Location table
		tableLocationHeader = pointPersonHandler.setTranslatedData("57000", "1", false); // ID, Repository //$NON-NLS-1$ //$NON-NLS-2$

/************************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/***************************************************
 * Setup panel for Repository name, location, memo
 **************************************************/
	// repo name sub-panel
		JPanel namePanel = new JPanel();
		namePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		namePanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel name = new JLabel("Abbreviated Name: ");
		namePanel.add(name, "cell 0 0");	//$NON-NLS-1$
		JTextField repoAbbrev = new JTextField();
		repoAbbrev.setColumns(30);
		repoAbbrev.setEditable(true);
		namePanel.add(repoAbbrev, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel ref = new JLabel("Reference Number: ");
		namePanel.add(ref, "cell 0 1");	//$NON-NLS-1$
		JTextField repoRef = new JTextField();
		repoRef.setColumns(5);
		repoRef.setEditable(true);
		namePanel.add(repoRef, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel fullName = new JLabel("Repository Name: ");
		namePanel.add(fullName, "cell 0 2");	//$NON-NLS-1$
		fullNameText = new JTextArea();
		fullNameText.setWrapStyleWord(true);
		fullNameText.setLineWrap(true);
		fullNameText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		fullNameText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)fullNameText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    fullNameText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    fullNameText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    fullNameText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane fullNameTextScroll = new JScrollPane(fullNameText);
		fullNameTextScroll.setMinimumSize(new Dimension(300, 50));
		fullNameTextScroll.getViewport().setOpaque(false);
		fullNameTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullNameText.setCaretPosition(0);	// set scrollbar to top
		namePanel.add(fullNameTextScroll, "cell 1 2, grow, alignx left");	//$NON-NLS-1$

		contents.add(namePanel, "cell 0 0");	//$NON-NLS-1$

	// Setup Location sub-panel
		JPanel locnPanel = new JPanel();
		locnPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		locnPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Styles = new JLabel("Location Style: ");	// Location Style:
		lbl_Styles.setFont(lbl_Styles.getFont().deriveFont(lbl_Styles.getFont().getStyle() | Font.BOLD));
		locnPanel.add(lbl_Styles, "cell 0 0, alignx left");	//$NON-NLS-1$
		DefaultComboBoxModel<String> comboLocnModel = new DefaultComboBoxModel<>();			// NOTE00 - temp definition!!!
//									= new DefaultComboBoxModel<>(pointWhereWhenHandler.getLocationStyles());
		locationNameStyles = new JComboBox<>(comboLocnModel);
//		locationNameStyles.setSelectedIndex(pointWhereWhenHandler.getDefaultLocationStyleIndex());	// NOTE00 temp disabled
		locnPanel.add(locationNameStyles, "cell 1 0, alignx left");	//$NON-NLS-1$

	// Create scrollpane and table for the Location data entry
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

		// NOTE00 some dummy data to test the table display format - to be removed
		tableLocationData = new Object[][] {{"Addressee ", " "},	{"Detail", "test place"},
										    {"City/Town ", " "},	{"County", " "},
										    {"Country ", " "},	{"Postal", " "} };

/*		NOTE00 following code disabled until correct WhWhHandler code ready
		int defaultLocationNameStyle = pointWhereWhenHandler.getDefaultLocationStyleIndex();
		locationNameStyles.setSelectedIndex(defaultLocationNameStyle);
		tableLocationData = pointWhereWhenHandler.getLocationDataTable(defaultLocationNameStyle);
		tableLocationHeader = pointWhereWhenHandler.getEventLocationTableHeader();
*/
		tableLocation.setModel(new DefaultTableModel(tableLocationData, tableLocationHeader));

	// Make table single-click editable
		((DefaultCellEditor) tableLocation.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableLocation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

		tableLocation.getColumnModel().getColumn(0).setMinWidth(80);
		tableLocation.getColumnModel().getColumn(0).setPreferredWidth(140);
		tableLocation.getColumnModel().getColumn(1).setMinWidth(100);
		tableLocation.getColumnModel().getColumn(1).setPreferredWidth(300);
		tableLocation.setAutoCreateColumnsFromModel(false);	// preserve column setup
		tableLocation.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader nameHeader = tableLocation.getTableHeader();
		nameHeader.setOpaque(false);
		ListSelectionModel nameSelectionModel = tableLocation.getSelectionModel();
		nameSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to Name panel
		tableLocation.setFillsViewportHeight(true);
		JScrollPane nameScrollPane = new JScrollPane(tableLocation);
		nameScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nameScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		if (tableLocation.getRowCount() > 0) {
			JTableCellTabbing.setTabMapping(tableLocation, 0, tableLocation.getRowCount(), 1, 1);
		}
		locnPanel.add(nameScrollPane, "cell 0 1 2, grow");	//$NON-NLS-1$

		contents.add(locnPanel, "cell 0 1, grow");	//$NON-NLS-1$

	// Setup Memo sub-panel
		JPanel memoPanel = new JPanel();
		memoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		memoPanel.setLayout(new MigLayout("insets 5", "[grow]", "[]5[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Memo = new JLabel("Memo: ");	// Memo:
		lbl_Memo.setFont(lbl_Memo.getFont().deriveFont(lbl_Memo.getFont().getStyle() | Font.BOLD));
		memoPanel.add(lbl_Memo, "cell 0 0, align left");	//$NON-NLS-1$

		memoText = new JTextArea();
		memoText.setWrapStyleWord(true);
		memoText.setLineWrap(true);
		memoText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoTextScroll = new JScrollPane(memoText);
		memoTextScroll.setMinimumSize(new Dimension(420, 75));
		memoTextScroll.getViewport().setOpaque(false);
		memoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoText.setCaretPosition(0);	// set scrollbar to top
		memoPanel.add(memoTextScroll, "cell 0 1, grow, alignx left, aligny top");	//$NON-NLS-1$

		contents.add(memoPanel, "cell 0 2, grow");	//$NON-NLS-1$

/*******************************
 * Setup final control buttons
 ******************************/
		JButton btn_Save = new JButton("Save");	// Save
		btn_Save.setToolTipText("Save the edited Repository data");	// Save the edited Repository data
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 3, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Close = new JButton("Close");	// Close
		btn_Close.setToolTipText("Close this screen");	// Close this screen
		contents.add(btn_Close, "cell 0 3, align right, gapx 20, tag cancel"); //$NON-NLS-1$

//*******************
// Setup Focus Policy
//*******************
        Vector<Component> focusOrder = new Vector<>();

        focusOrder.add(memoText);

        contents.setFocusCycleRoot(true);
        contents.setFocusTraversalPolicy(new focusPolicy(focusOrder));
	// Set initial focus of screen


	// Display the screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
	// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
	    		 btn_Close.doClick();
			} // return to main menu
		});

		// Listener for edits of fullNameText
		nameTextChange = new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void insertUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void changedUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	    };
	    fullNameText.getDocument().addDocumentListener(nameTextChange);

	// Listener for edits of memoText
		memoTextChange = new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	           nameEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void insertUpdate(DocumentEvent e) {
	           	nameEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void changedUpdate(DocumentEvent e) {
	           	nameEdited = true;
            	btn_Save.setEnabled(true);
	        }
	    };
	    memoText.getDocument().addDocumentListener(memoTextChange);

	// Listener for changes made in location
		tableLocationListener = new TableModelListener() {
			@Override
           public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
	                    String nameElementData = (String) tableLocation.getValueAt(row, 1);

						if (nameElementData != null) {

							btn_Save.setEnabled(true);
						}
                    }
                }
            }
		};
		tableLocation.getModel().addTableModelListener(tableLocationListener);

	// On selection of a locn style update the Location table
		locationNameStyles.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!(locationNameStyles.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableLocation.getModel();
					tableModel.setNumRows(0);	// clear table
//					tableLocationData = pointWhereWhenHandler.getLocationDataTable(locationNameStyles.getSelectedIndex()); //NOTE00 to be completed
					tableModel.setDataVector(tableLocationData, tableLocationHeader);
				// reset table rows
					tableModel.setRowCount(tableLocationData.length);
					changedLocationNameStyle = true;
					btn_Save.setEnabled(true);
				// reset screen size
					pack();
				}
			}
		});

	// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				// NOTE01 perform save actions

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: accepting updates and leaving HG0570EditRepository");		//$NON-NLS-1$
			}
		});

	// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Test for unsaved changes
			if (btn_Save.isEnabled() || memoEdited || nameEdited) {
				if (JOptionPane.showConfirmDialog(btn_Save,
						"There are unsaved changes. \n"		// There are unsaved changes. \n
						+ "Do you still wish to exit this screen?",	// Do you still wish to exit this screen?
						"Edit Repository",		// Edit Event
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0570EditRepository"); //$NON-NLS-1$
						// Yes option: clear Reminder and exit
							if (reminderDisplay != null) reminderDisplay.dispose();
							dispose();
						}
				} else {
					if (reminderDisplay != null) reminderDisplay.dispose();
					if (HGlobal.writeLogs) 	HB0711Logging.logWrite("Action: exiting HG0570EditRepository"); //$NON-NLS-1$
					dispose();
				}
			}
		});

	}	// End HG0570EditRepository constructor


}  // End of HG0570EditRepository

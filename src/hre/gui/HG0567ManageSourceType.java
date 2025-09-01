package hre.gui;
/**************************************************************************************
 * ManageSourceType -
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-31 Add Delete button, implement Add button (D Ferguson)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE02 implement Edit button
 * NOTE03 implement Copy button
 * NOTE04 doesn't allow editing/saving of source types
 * NOTE06 implement Delete button
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Source Types
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */

public class HG0567ManageSourceType extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBPersonHandler pointPersonHandler;

	public static final String screenID = "56700"; //$NON-NLS-1$

	private JPanel contents;

	String[] tableSourceTypeColHeads = null;
	Object[][] tableSourceTypeData;
	DefaultTableModel srcTableModel = null;

	JTextArea fullFootText, shortFootText, biblioText;

/**
 * Create the dialog
 */
	public HG0567ManageSourceType(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();

	// NOTE this can be called directly from 'Evidence' on mainMenu , when title should be 'Manage Source types'
	// OR from HG0565ManageSource, when the title should be 'Select Source Type'
		setTitle("Manage Source Types");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesourcetype";		 //$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0567ManageSourceType");	//$NON-NLS-1$


	// Collect static GUI text from T204 for Source type table
		tableSourceTypeColHeads = pointPersonHandler.setTranslatedData("56700", "1", false); // Source Type	//$NON-NLS-1$ //$NON-NLS-2$

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Define panel for Action buttons
		JPanel actionPanel = new JPanel();
		actionPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		actionPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JButton btn_Add = new JButton("Add");		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 0, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Edit = new JButton("Edit");		// Edit
		btn_Edit.setEnabled(false);
		actionPanel.add(btn_Edit, "cell 0 1, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Copy = new JButton("Copy");		// Copy
		btn_Copy.setEnabled(false);
		actionPanel.add(btn_Copy, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton("Delete");		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JLabel find = new JLabel("Find:");		// Find:
		actionPanel.add(find, "cell 0 4, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 5, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Source Type list
		JPanel sourceTypePanel = new JPanel();
		sourceTypePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		sourceTypePanel.setLayout(new MigLayout("insets 5", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Source Type data
		JTable tableSourceType = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		tableSourceType.setMaximumSize(new Dimension(32767, 32767));
		tableSourceType.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0
		if (tableSourceType.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSourceType, 0, tableSourceType.getRowCount(), 0, 0);
		// scrollPane contains the Source Type picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(305, 380));
		sourceTypePanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(sourceTypePanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define panel for Source Types templates
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		templatePanel.setLayout(new MigLayout("insets 5", "[]", "5[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel("Full footnote");
		templatePanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		fullFootText = new JTextArea();
		fullFootText.setWrapStyleWord(true);
		fullFootText.setLineWrap(true);
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)fullFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		fullFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setMinimumSize(new Dimension(200, 100));
		fullFootTextScroll.getViewport().setOpaque(false);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
		templatePanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		shortFootText = new JTextArea();
		shortFootText.setWrapStyleWord(true);
		shortFootText.setLineWrap(true);
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)shortFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		shortFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setMinimumSize(new Dimension(200, 100));
		shortFootTextScroll.getViewport().setOpaque(false);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		shortFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
		templatePanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		biblioText = new JTextArea();
		biblioText.setWrapStyleWord(true);
		biblioText.setLineWrap(true);
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)biblioText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		biblioText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setMinimumSize(new Dimension(200, 100));
		biblioTextScroll.getViewport().setOpaque(false);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		biblioText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(templatePanel, "cell 2 0, aligny top"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 2 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Select = new JButton("Select");		// Select
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 2 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

 	// Get Source Type data
		// load some dummy data for test & display - to be removed
		tableSourceTypeData = new Object[][] {{"Source type A "}, {"Source type B"}};
//		tableSourceTypeData = pointPersonHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Source data
		if (tableSourceTypeData == null ) {
			JOptionPane.showMessageDialog(scrollTable, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Source Type select error",		// Source Type select error
													   "Source Type Select", 			// Source Type Select
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

	 	// Setup tablePersData, model and renderer
		srcTableModel = new DefaultTableModel(
    		tableSourceTypeData, tableSourceTypeColHeads) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int column) {
						return getValueAt(0, column).getClass();
			}
		};
        tableSourceType.setModel(srcTableModel);
		tableSourceType.getColumnModel().getColumn(0).setMinWidth(50);
		tableSourceType.getColumnModel().getColumn(0).setPreferredWidth(300);
		tableSourceType.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Set the ability to sort on columns
		tableSourceType.setAutoCreateRowSorter(true);
	    TableModel myModel = tableSourceType.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

		// Presort on column 0
		psortKeys1.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableSourceType.setRowSorter(sorter);

	    // Set tooltips and header format
		tableSourceType.getTableHeader().setToolTipText("Click to sort; Click again to sort in reverse order");	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tableSourceType.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableSourceType.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Set row selection action
		ListSelectionModel rowSelectionModel = tableSourceType.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		// Show the table
		scrollTable.setViewportView(tableSourceType);
		tableSourceType.requestFocus();

		// Focus Policy still to be setup!

		pack();

/*****************************
 * CREATE All ACTION LISTENERS
 *****************************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
			}
		});

		// Listener for tableSourceType row selection
		tableSourceType.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSourceType.getSelectedRow() == -1) return;
				// find source clicked
					int clickedRow = tableSourceType.getSelectedRow();
					int selectedRowInTable = tableSourceType.convertRowIndexToModel(clickedRow);

				// Do something with the source type value so Select button can use it,
				// and get the source type templates and load them into the footnote/biblio areas (NOTE04)

					btn_Edit.setEnabled(true);
					btn_Copy.setEnabled(true);
					btn_Select.setEnabled(true);
					btn_Delete.setEnabled(true);
				}
			}
        });

		// Listener for Add button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				HG0568EditSourceType typeScreen = new HG0568EditSourceType(pointOpenProject);
				typeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyType = btn_Add.getLocationOnScreen();
				typeScreen.setLocation(xyType.x, xyType.y + 30);
				typeScreen.setVisible(true);
			}
		});

		// Listener for Edit button
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE02 does nothing yet
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE03 does nothing yet
			}
		});

		// Listener for Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE06 not implemented yet
			}
		});

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0567ManageSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for an entry in findText
		findText.getDocument().addDocumentListener(new DocumentListener() {
	          @Override
	          public void insertUpdate(DocumentEvent e) {
	              findTheText();
	          }
	          @Override
	          public void removeUpdate(DocumentEvent e) {
	        	  findTheText();
	          }
	          @Override
	          public void changedUpdate(DocumentEvent e) {
	        	  findTheText();
	          }
	          private void findTheText() {
	        	  String text = findText.getText();
		            for (int row = 0; row <= tableSourceType.getRowCount() - 1; row++) {
	            		String tableValue = (String) tableSourceType.getValueAt(row, 0);
   	                    if (tableValue.toLowerCase().contains(text.toLowerCase())) {
   	                    	tableSourceType.scrollRectToVisible(tableSourceType.getCellRect(row, 0, true));
   	                    // set the 'found' row as selected
   	                    	tableSourceType.changeSelection(row, 0, false, false);
   	                    	return;
   	                    }
	            }
	          }
		});

	}	// End HG0567ManageSourceType constructor

}  // End of HG0567ManageSourceType

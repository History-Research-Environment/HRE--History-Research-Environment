package hre.gui;
/****************************************************************************************
 * Location Select - Specification 05.07 GUI_EntitySelect 2019-09-16
 * v0.00.0012 2019-09-16 by D Ferguson
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0021 2020-04-13 Added new version of HG0507EntitySelect (N. Tolleshaug)
 *            2020-04-16 converted to MigLayout (D Ferguson)
 * v0.00.0022 2020-05-21 align code with PersonSelect (D Ferguson)
 *            2020-05-22 optionally put project name in frame title (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 *            2020-07-31 changed to JFrame (D Ferguson)
 * v0.01.0023 2020-08-18 renamed to HG0507LocationSelect; removed Tools, View functions
 *            2020-08-18 added Search on Name; fixed sort sequence after filter removed  (D Ferguson)
 *            2020-09-08 changed to use HG0452SuperFrame structure (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 *            2020-10-03 LocationSelect extends HG0451SuperIntFrame (N. Tolleshaug)
 * v0.01.0025 2020-11-02 Implemented location table with place data (N. Tolleshaug)
 *   		  2020-11-03 Fix table name for Output; set sort seq; fix Search (D Ferguson)
 *     		  2020-11-03 Corrected output table print error (N. Tolleshaug)
 *     		  2020-11-04 Add Events button (D Ferguson)
 *     		  2021-02-08 Implemented column control HG0581ConfigureTable (N. Tolleshaug)
 * 			  2021-02-27 Implemented list of events table in Location VP (N. Tolleshaug)
 *            2021-03-06 Implemented handling of 5 Location VP (N. Tolleshaug)
 *  		  2021-03-07 Make found 'Search' row marked as Selected (D Ferguson)
 * v0.01.0026 2021-09-19 Add 'Find Next' button to repeat Search (D Ferguson)
 * v0.01.0027 2021-11-29 Run picklist build on own thread plus progress bar (D Ferguson)
 * 			  2022-02-27 NLS converted (D Ferguson)
 *            2022-02-27 NLS updated for table headers (N. Tolleshaug)
 *            2022-03-07 Modified to use HGloDataMsgs (D Ferguson)
 *            2022-03-12 Change right-click in table to show popupMenu (D Ferguson)
 *            2022-05-10 Removed 'Show Events' button (D Ferguson)
 *            2022-05-31 Add Find Previous to Find (D Ferguson)
 *            2022-08-02 Handling of HG0508ManageLocation (N. Tolleshaug)
 * v0.01.0028 2022-12-09 Location name style setting up column headings (N. Tolleshaug)
 * 			  2022-12-19 Updates by (D Ferguson)
 * 			  2023-01-15 Translated texts collected from T204 (N. Tolleshaug)
 * 			  2023-01-26 Modified column with for Location (N. Tolleshaug)
 * 			  2023-01-29 Added catch block for PSE on filter (D Ferguson)
 * 			  2023-02-28 Modified lookup of Location table (N. Tolleshaug)
 ****************************************************************************************
 * NOTES for incomplete functionality
 * NOTE03 No code for importing saved filters
 ***************************************************************************************/

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBViewPointHandler;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG05075Msgs;

import net.miginfocom.swing.MigLayout;

/**
 * Location Select
 * @author D Ferguson
 * @version v0.01.0028
 * @since 2019-09-16
 */
public class HG0507LocationSelect extends HG0451SuperIntFrame  {
	private static final long serialVersionUID = 001L;
	public static final String screenID = "50750"; //$NON-NLS-1$
	final static int maxLocationVPIs = 5;

	private HBViewPointHandler pointViewPointHandler = null;
	private JPanel contents;
	private String selectString = ""; //$NON-NLS-1$
	private JCheckBox chkbox_Filter;
	JComboBox<String> comboBox_Subset;

	private int foundRow;
	private int selectedRow;
	private boolean findActivated = false;
	
	 private String idText, allColumnsText1, allColumnsText2;

	String[] tableColHeads = null;
	private Object [][] tableControlData;
	Object[][] tableData;
	
	HBProjectOpenData pointOpenProject;
	JInternalFrame locationFrame = this;
	private JTable table_Entity;
	JScrollPane scrollPane;

/**
 * Collect JTable for output
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Entity;
	}

/**
 * Create the dialog.
 * @throws HBException
 */
		public HG0507LocationSelect(HBWhereWhenHandler pointPlaceHand,
								    HBProjectOpenData pointOpenProject,
								    Object [][] tableControl) throws HBException {
		super(pointPlaceHand,"Location Select",true,true,true,true, tableControl);	 //$NON-NLS-1$
		this.pointOpenProject = pointOpenProject;
		
		pointViewPointHandler = pointOpenProject.getViewPointHandler();

		// Setup references for HG0451
		windowID = screenID;
		helpName = "selectlocation";		 //$NON-NLS-1$

		// Setup column headings and data
		tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);
		tableColHeads = setColumnHeaders(tableControlData);
		String projectName = pointOpenProject.getProjectName();
		
		// test translated data from T204		
		if (HGlobal.DEBUG) 
			System.out.println("Translated texts:  0-" 			//$NON-NLS-1$
					+ Arrays.toString(pointPlaceHand.setTranslatedData(screenID, "0", false)));		//$NON-NLS-1$
			
		// Collect static gui texts from T204 
		String[] translatedTexts = pointPlaceHand.setTranslatedData(screenID, "0", false);			//$NON-NLS-1$
		idText = " " + translatedTexts[0]; 				//$NON-NLS-1$
		allColumnsText1 = " " + translatedTexts[1];		//$NON-NLS-1$
		allColumnsText2 = translatedTexts[2];

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0507LocationSelect");																	   																					  		  //$NON-NLS-1$
		if (HGlobal.TIME) HGlobalCode.timeReport("start of HG0507LocSel on thread "+Thread.currentThread().getName());		 //$NON-NLS-1$

		// Create and Show Progress Bar
		JFrame progFrame = new JFrame(HG05075Msgs.Text_6);
		progFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		progFrame.setSize(400,50);
		progFrame.setLocation(HGlobal.mainX + 350, HGlobal.mainY + 150);
		JProgressBar progBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progBar.setStringPainted(true);
		progBar.setIndeterminate(false);
		progFrame.setAlwaysOnTop(true);
		progFrame.add(progBar);

	// Set pointer to progbar in Handler
		pointPlaceHand.setPointProgBar(progBar);

		if (HGlobal.TIME) HGlobalCode.timeReport("show HG0507LocSel progress bar on thread "+Thread.currentThread().getName());	//$NON-NLS-1$
		progFrame.setVisible(true);

		// Setup Location List panel
		setTitle(HG05075Msgs.Text_8 + projectName);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][][grow]", "[]15[]15[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(btn_Configicon);
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");    //$NON-NLS-1$

	// Define controls in CENTRAL area
	// Row 0
		JLabel lbl_Search = new JLabel(HG05075Msgs.Text_13);
		contents.add(lbl_Search, "cell 1 0"); //$NON-NLS-1$

		JTextField searchField = new JTextField();
		searchField.setColumns(30);
		searchField.setToolTipText(HG05075Msgs.Text_15);
		contents.add(searchField, "cell 2 0");		 //$NON-NLS-1$

		JButton searchPrevious = new JButton(HG05075Msgs.Text_19);			// Find Previous
		searchPrevious.setToolTipText(HG05075Msgs.Text_20);
		searchPrevious.setEnabled(false);
		contents.add(searchPrevious, "cell 2 0, gapx 20"); //$NON-NLS-1$

		JButton searchNext = new JButton(HG05075Msgs.Text_17);				// Find Next
		searchNext.setToolTipText(HG05075Msgs.Text_18);
		searchNext.setEnabled(false);
		contents.add(searchNext, "cell 2 0, gapx 10"); //$NON-NLS-1$

	// Row 1
		JLabel lbl_TxtFilter = new JLabel(HG05075Msgs.Text_23);
		contents.add(lbl_TxtFilter, "cell 1 1, align right"); //$NON-NLS-1$

		JTextField filterTextField = new JTextField();
		filterTextField.setColumns(15);
		filterTextField.setToolTipText(HG05075Msgs.Text_25);
		contents.add(filterTextField, "flowx,cell 2 1"); //$NON-NLS-1$

		chkbox_Filter = new JCheckBox(HG05075Msgs.Text_27);
		chkbox_Filter.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_Filter.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_Filter.setToolTipText(HG05075Msgs.Text_28);
		contents.add(chkbox_Filter, "cell 2 1,gapx 20"); //$NON-NLS-1$

		JLabel lbl_Subsets = new JLabel(HG05075Msgs.Text_30);
		lbl_Subsets.setToolTipText(HG05075Msgs.Text_31);
		contents.add(lbl_Subsets, "cell 2 1, gapx 20"); //$NON-NLS-1$

		comboBox_Subset = new JComboBox<String>();
		comboBox_Subset.setPreferredSize(new Dimension(170, 20));
		comboBox_Subset.setToolTipText(HG05075Msgs.Text_33);
		for (int i = 1; i < tableColHeads.length; i++) {
			comboBox_Subset.addItem(tableColHeads[i]);
		}
		comboBox_Subset.addItem(idText);					// ID 
		comboBox_Subset.addItem(allColumnsText1);			// All Columns
		contents.add(comboBox_Subset, "cell 2 1"); //$NON-NLS-1$ - 

	// Row 2
		scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 2 4 1, grow");  //$NON-NLS-1$

		// Setup JTable to show data
		table_Entity = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		table_Entity.setPreferredScrollableViewportSize(new Dimension(900, 400));
		table_Entity.setMaximumSize(new Dimension(32767, 32767));
		table_Entity.setFillsViewportHeight(true);

	    // Define SwingWorker task to build Location Picklist as a background task on worker thread.
	    SwingWorker<Void, Integer> buildLocList = new SwingWorker<Void, Integer>() {
	         @Override
	         protected Void doInBackground() throws Exception {
	        	 if (HGlobal.TIME) HGlobalCode.timeReport("start of HG0507LocSel background on thread "+Thread.currentThread().getName()); //$NON-NLS-1$
	        // Get the actual picklist data
	        	 tableData = pointPlaceHand.convertHRLocationData(tableColHeads.length, tableControlData, pointOpenProject);
				 if (tableData == null ) {
					userInfoConvertData(1);
					progFrame.dispose();
					errorCloseAction();
				 }
	        	 if (HGlobal.TIME) 
	        		 HGlobalCode.timeReport("end of HG0507LocSel background on thread " 		//$NON-NLS-1$
	        			 								+ Thread.currentThread().getName());  
	     		 return null;
	         }

	     // Takes SwingWorker publish %s and updates Progress Bar
	         @Override
	         protected void process(List<Integer> chunks) {
	             progBar.setValue(chunks.get(chunks.size()-1));
	         }

	     // All code to be executed AFTER background thread is done now follows
	         @Override
	         protected void done() {
	        	 if (HGlobal.TIME) 
	        		 HGlobalCode.timeReport("start of HG0507LocSel 'done' component on thread "+Thread.currentThread().getName());  //$NON-NLS-1$
				// No table data ??
				if (tableData == null) return;
				// Setup table and renderer
				DefaultTableModel myTableModel = new DefaultTableModel(
						tableData, tableColHeads) {
						private static final long serialVersionUID = 1L;
						@Override
						public Class<? extends Object> getColumnClass(int column) {
							if (HGlobal.DEBUG) 
								System.out.println("Column class: " + column + "/" + getValueAt(0, column).getClass());	//$NON-NLS-1$ //$NON-NLS-2$
							return getValueAt(0, column).getClass();
						}
		        };
			    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			    centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
			    table_Entity.setDefaultRenderer(Integer.class, centerRenderer);
			    table_Entity.setModel(myTableModel);
				table_Entity.getColumnModel().getColumn(0).setMinWidth(50);
				table_Entity.getColumnModel().getColumn(0).setPreferredWidth(50);
				table_Entity.getColumnModel().getColumn(1).setMinWidth(100);
				table_Entity.getColumnModel().getColumn(1).setPreferredWidth(200);
				int col = 2;
		        for (int i = 2; i < tableControlData.length; i++) {
		        	if ((boolean)tableControlData[i][1]) {
		        // Select col with in table
		        		table_Entity.getColumnModel().getColumn(col).setMinWidth(50);
		        		table_Entity.getColumnModel().getColumn(col).setPreferredWidth(120);
		        		col++;
		        	}
		        }
				table_Entity.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			    // Set the ability to sort on columns
				table_Entity.setAutoCreateRowSorter(true);
			    TableModel myModel = table_Entity.getModel();
			    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
				List <RowSorter.SortKey> psortKeys = new ArrayList<>();

			    // Presort on last column down through to column 1
				int columns = 1;
		        for (int i = 2; i < tableControlData.length; i++) {
		        	if ((boolean)tableControlData[i][1]) {
		        		psortKeys.add(new RowSorter.SortKey(columns, SortOrder.ASCENDING));
		        		columns++;
		        	}
		        }
				sorter.setSortKeys(psortKeys);
			    table_Entity.setRowSorter(sorter);

			    // Set tooltips and header format
				table_Entity.getTableHeader().setToolTipText(HG05075Msgs.Text_41);
				JTableHeader pHeader = table_Entity.getTableHeader();
				pHeader.setOpaque(false);
				TableCellRenderer prendererFromHeader = table_Entity.getTableHeader().getDefaultRenderer();
				JLabel pheaderLabel = (JLabel) prendererFromHeader;
				pheaderLabel.setHorizontalAlignment(SwingConstants.LEFT);

				// Set row selection action
				ListSelectionModel pcellSelectionModel = table_Entity.getSelectionModel();
				pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				// Show the table
				scrollPane.setViewportView(table_Entity);
				
				// Do update of T302	
				pointPlaceHand.finalActionT302(pointOpenProject); 

				// Set the frame visible
				locationFrame.pack();
			    HG0401HREMain.mainPane.add(locationFrame);
			    locationFrame.setVisible(true);
			    locationFrame.toFront();

				// Remove the Progress Bar panel
			    progFrame.dispose();
			    if (HGlobal.TIME) 
			    	HGlobalCode.timeReport("complete HG0507LocSel on thread "+Thread.currentThread().getName()); //$NON-NLS-1$

			/*****************************
			 * CREATE All ACTION LISTENERS
			 *****************************/
				// Listener for clicking 'X on screen - make same as Cancel button
			     locationFrame.addInternalFrameListener(new InternalFrameAdapter() {
			    	 @Override
					public void internalFrameClosing(InternalFrameEvent e)  {
						if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG00507 Location Select"); //$NON-NLS-1$
					// close reminder display
						if (reminderDisplay != null) reminderDisplay.dispose();
				    // Set frame size in GUI data
						Dimension frameSize = getSize();
						pointOpenProject.setSizeScreen(screenID,frameSize);
					// Set position	in GUI data
						Point position = getLocation();
						pointOpenProject.setPositionScreen(screenID,position);
					// Set class name in GUI config data
						pointOpenProject.setClassName(screenID,"HG0507LocationSelect"); //$NON-NLS-1$
					// Mark the screen as closed in T302
						pointOpenProject.closeStatusScreen(screenID);
						dispose();	
					// Remove pointer to LS window
						pointPlaceHand.locationScreen = null;
					}		// return to main menu
				});

				// Find anywhere field listener
			    searchField.addActionListener(new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			        	// First, find how many rows are visible in current scrollPane's size, and half it
			        	JViewport viewport = scrollPane.getViewport();
			            Dimension extentSize = viewport.getExtentSize();
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
			            Rectangle viewRect = viewport.getViewRect();
			        	// Now find the first table row where ANY Location field matches search value
			            String searchValue = searchField.getText();
			            // Set Find action
			            findActivated = true;
			            for (int row = 0; row <= table_Entity.getRowCount() - 1; row++) {
			            		for (int col = 1; col < table_Entity.getColumnCount(); col++) {
			            			String tabValue = (String) table_Entity.getValueAt(row, col);
			   	                    if (tabValue.toLowerCase().contains(searchValue.toLowerCase())) {
			   	                    	// set the found table row to show in the middle of the scrollpane; adjusted for scroll-up or down
			   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
			   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
			   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
			   	                    	// set the 'found' row as selected, clear any other selection, save it
			   	                    	table_Entity.changeSelection(row, 0, false, false);
			   	                    	foundRow = row;
			   	                   		searchNext.setEnabled(true);
			   	                    	return;
			   	                    	}
		                   	}
			            }
		   	            JOptionPane.showMessageDialog(searchField, HG05075Msgs.Text_45 		// Cannot find
		   	            							+ searchValue + HG05075Msgs.Text_46,	//  in any Location Field
		   	            											HG05075Msgs.Text_47,	// Search Result
		   	            											JOptionPane.INFORMATION_MESSAGE);
			        }
			    });

				// Find Next button listener
			    searchNext.addActionListener(new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			        	// First, find how many rows are visible in current scrollpane's size, and half it
			        	JViewport viewport = scrollPane.getViewport();
			            Dimension extentSize = viewport.getExtentSize();
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
			            Rectangle viewRect = viewport.getViewRect();
			        	// Now find the first table row after last found row which matches search value
			            String searchValue = searchField.getText();
			            // Set Find action
			            findActivated = true;
			            foundRow = foundRow + 1;
			            for (int row = foundRow; row <= table_Entity.getRowCount() - 1; row++) {
			            		for (int col = 1; col < table_Entity.getColumnCount(); col++) {
			            			String tabValue = (String) table_Entity.getValueAt(row, col);
			   	                    if (tabValue.toLowerCase().contains(searchValue.toLowerCase())) {
			   	                    	// set the found table row to show in the middle of the scrollpane; adjusted for scroll-up or down
			   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
			   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
			   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
			   	                    	// set the 'found' row as selected, clear any other selection, save it
			   	                    	table_Entity.changeSelection(row, 0, false, false);
			   	                    	foundRow = row;
			   	                    	searchPrevious.setEnabled(true);
			   	                    	return;
			   	                    	}
		                   	}
			            }
		   	            JOptionPane.showMessageDialog(searchNext, HG05075Msgs.Text_48 		// No more occurrences of
		   	            							+ searchValue + HG05075Msgs.Text_49,	//  in any Location field
		   	            											HG05075Msgs.Text_50,	// Search Result
		   	            											JOptionPane.INFORMATION_MESSAGE);
			        }
			    });

				// Find Previous button listener
			    searchPrevious.addActionListener(new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			        	// First, find how many rows are visible in current scrollpane's size, and half it
			        	JViewport viewport = scrollPane.getViewport();
			            Dimension extentSize = viewport.getExtentSize();
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
			            Rectangle viewRect = viewport.getViewRect();
			        	// Now find the first table row before last one which matches search value
			            String searchValue = searchField.getText();
			            // Set Find action
			            findActivated = true;
			            foundRow = foundRow - 2;
			            for (int row = foundRow; row >= 0; row--) {
			            		for (int col = 1; col < table_Entity.getColumnCount(); col++) {
			            			String tabValue = (String) table_Entity.getValueAt(row, col);
			   	                    if (tabValue.toLowerCase().contains(searchValue.toLowerCase())) {
			   	                    	// set the found table row to show in the middle of the scrollpane; adjusted for scroll-up or down
			   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
			   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
			   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
			   	                    	// set the 'found' row as selected, clear any other selection, save it
			   	                    	table_Entity.changeSelection(row, 0, false, false);
			   	                    	foundRow = row;
			   	                    	return;
			   	                    	}
		                   	}
			            }
		   	            JOptionPane.showMessageDialog(searchPrevious, HG05075Msgs.Text_48 		// No more occurrences of
		   	            							+ searchValue + HG05075Msgs.Text_49,		//  in any Location field
		   	            											HG05075Msgs.Text_50,		// Search Result
		   	            											JOptionPane.INFORMATION_MESSAGE);
			        }
			    });
			    
				// Filter Text field listener
				filterTextField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {

					}
				});

				// Filter checkbox listener
				chkbox_Filter.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Reset filter for whole table when unselected, sorted as before 
						if (!chkbox_Filter.isSelected()) {
								setTableFilter(allColumnsText2, ""); 		// All Columns  //$NON-NLS-1$
								comboBox_Subset.setSelectedIndex(0);
								table_Entity.setRowSorter(sorter);
						}
					}
				});

				// On selection of a Subset perform appropriate action
				comboBox_Subset.addActionListener (new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						if (chkbox_Filter.isSelected()) {
							selectString = comboBox_Subset.getSelectedItem().toString();
							setTableFilter(selectString, filterTextField.getText());
							// NOTE03 no code for getting stored subset selections yet
						}
					}
				});

				// On selection of a Location, perform action
				pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
					long locationTablePID;
					int viewRow, selectedRowInTable, locationVPindex = 0;
					@Override
					public void valueChanged(ListSelectionEvent event) {
						if (!event.getValueIsAdjusting()) {
						// Reject Find action
							if (findActivated) {
								findActivated = false;
								return;
							}
							try {
						// Find next open eventVP
								viewRow = table_Entity.getSelectedRow();
								selectedRowInTable = table_Entity.convertRowIndexToModel(viewRow);
								if (selectedRowInTable < 0) return;				//exit if listener call was caused by emptying table_User
								locationTablePID = pointPlaceHand.getLocationTablePID(selectedRowInTable + 1);
								locationVPindex = pointViewPointHandler.findClosedVP("5303", pointOpenProject);	//$NON-NLS-1$
								String locationVPident = pointViewPointHandler.getLocationScreenID(locationVPindex);

								if (HGlobal.DEBUG)
									System.out.println(" HG0507LocationSelect row: " + selectedRowInTable 	//$NON-NLS-1$
												+ " locationVPindex: "	+ locationVPindex	//$NON-NLS-1$
												+ " locationVPident: " + locationVPident	//$NON-NLS-1$
												+ " LocationPID: " + locationTablePID);		//$NON-NLS-1$
						// Set PID for event from event list
								if (locationVPindex >= 0)
									pointOpenProject.pointGuiData.setTableViewPointPID(locationVPident, locationTablePID);
								else {
									userInfoInitVP(1);
									if (HGlobal.DEBUG)
										System.out.println(" HG0507LocationSelect valueChanged - personVPindex: "	//$NON-NLS-1$
												+ locationVPindex + " VP ident: " +locationVPident);				//$NON-NLS-1$
								}

							} catch (HBException hbe) {
								if (HGlobal.DEBUG)
									System.out.println("HG0507LocationSelect - Not able to set location: " + hbe.getMessage());  //$NON-NLS-1$
								JOptionPane.showMessageDialog(null, HG05075Msgs.Text_59 + hbe.getMessage(),
										HG05075Msgs.Text_63,JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});

				// Define ActionListeners for each entry of the following popupMenu
				// For popupMenu item popMenu1 - show Location VP
			    ActionListener popAL1 = new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			        // Display Locn Viewpoint for the selected Location
			        //	The right-clicked row is passed here in selectedRow
			        	int errorCode = 0;
			        	//selectedRow = table_Entity.getSelectedRow();
	                	int selectedRowInTable = table_Entity.convertRowIndexToModel(selectedRow);
						//int visibleId = (Integer) myTableModel.getValueAt(selectedRowInTable, 0);
						//System.out.println(" Selected row: " + selectedRow + "/" + selectedRowInTable + " - " + visibleId);
	                	//int focusLocation = (Integer) myTableModel.getValueAt(selectedRowInTable, 0);
	                	if (selectedRowInTable < 0) return;		// exit if listener call caused by emptying table_Entity
	                
	                // Collect location data and show VP
	        			//long locationTablePID = pointPlaceHand.getLocationTablePID (focusLocation);
	        			long locationTablePID = pointPlaceHand.getLocationTablePID (selectedRowInTable);
	        			
						errorCode = pointViewPointHandler.initiateLocationVP(pointOpenProject, locationTablePID);
						if (errorCode > 0) userInfoInitVP(errorCode);
			        }
			      };

				// For popupMenu item popMenu2 - show Manage Location
			    ActionListener popAL2 = new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
				        // Display manage location for the selected Location
				        //	The right-clicked row is passed here in selectedRow
				        	int errorCode = 0;
				        	//selectedRow = table_Entity.getSelectedRow();
		                	int selectedRowInTable = table_Entity.convertRowIndexToModel(selectedRow);
		                	//int focusLocation = (Integer) myTableModel.getValueAt(selectedRowInTable, 0);
		                	if (selectedRowInTable < 0) return;		// exit if listener call caused by emptying table_Entity
		                // Collect location data and show manage location
		        			//long locationTablePID = pointPlaceHand.getLocationTablePID (focusLocation);
		        			long locationTablePID = pointPlaceHand.getLocationTablePID (selectedRowInTable);
							errorCode = pointPlaceHand.initiateManageLocation(pointOpenProject, locationTablePID,"50800");	//$NON-NLS-1$
							if (errorCode > 1)
								System.out.println("HG0507LocationSelect - show Manage Location errorCode = " + errorCode);	//$NON-NLS-1$
			        }
			      };
				// Define a right-click popup menu to use below
			    JPopupMenu popupMenu = new JPopupMenu();
			    JMenuItem popMenu1 = new JMenuItem(HG05075Msgs.Text_54);		// Show Location ViewPoint
			    popMenu1.addActionListener(popAL1);
			    popupMenu.add(popMenu1);
			    JMenuItem popMenu2 = new JMenuItem(HG05075Msgs.Text_55);		// Show Manage Location screen
			    popMenu2.addActionListener(popAL2);
			    popupMenu.add(popMenu2);

				// Listener for Location Picklist mouse right-click and double-click
				table_Entity.addMouseListener(new MouseAdapter() {
					@Override
		            public void mousePressed(MouseEvent me) {
						int errorCode = 0;
						// double-click
		            	if (me.getClickCount() == 2 && table_Entity.getSelectedRow() != -1) {
		            		selectedRow = table_Entity.getSelectedRow();
		            		int selectedRowInTable = table_Entity.convertRowIndexToModel(selectedRow);
		            		//int focusLocation = (Integer) myTableModel.getValueAt(selectedRowInTable, 0);
		            	// Collect location data
		        			//long locationTablePID = pointPlaceHand.getLocationTablePID (focusLocation);
		        			long locationTablePID = pointPlaceHand.getLocationTablePID (selectedRowInTable);
		        			errorCode = pointViewPointHandler.initiateLocationVP(pointOpenProject, locationTablePID);
							if (errorCode > 0) userInfoInitVP(errorCode);
		                }
		                if (me.getButton() == MouseEvent.BUTTON3) {
		                // right-click
		                	selectedRow = table_Entity.rowAtPoint(me.getPoint());
		                	int selectedRowInTable = table_Entity.convertRowIndexToModel(selectedRow);
		                	if (selectedRowInTable < 0) return;		// exit if listener call was caused by emptying table_Entity
		                // Show popup menu of all possible actions
		                	popupMenu.show(me.getComponent(), me.getX(), me.getY());
		                }
		            }
		        });

	          }	// End SwingWorker done component
	    };   // End of SwingWorker method

		// Execute the background thread to build the Location Picklist and (when
		// finished) build the rest of the GUI
	    buildLocList.execute();

	}	// End HG0507LocationSelect constructor

/**
 * void errorCloseAction()
 */
	private void errorCloseAction() {
		// close reminder display
		if (reminderDisplay != null) reminderDisplay.dispose();
		// Set frame size in GUI data
		Dimension frameSize = getSize();
		pointOpenProject.setSizeScreen(screenID,frameSize);
		// Set position	in GUI data
		Point position = getLocation();
		pointOpenProject.setPositionScreen(screenID,position);
		// Set class name in GUI config data
		pointOpenProject.setClassName(screenID,"HG0507LocationSelect"); //$NON-NLS-1$
		// Mark the screen as closed in T302
		pointOpenProject.closeStatusScreen(screenID);
		dispose();
	}	// End errorCloseAction

/**
 * Set up column headings
 * @param tableControl
 * @return
 */
	private String[] setColumnHeaders(Object[][] tableControl) {
		int rows = 2;
		for (int i = 2; i < tableControl.length; i++) if (((boolean)tableControl[i][1])) rows++;
		String [] tableHeads = new String[rows];
		tableHeads[0] = (String) tableControl[0][0];
		tableHeads[1] = (String) tableControl[1][0];
		int index = 2;
		for (int i = 2; i < tableControl.length; i++) {
			if ((boolean) tableControl[i][1]) {
				tableHeads[index] = (String) tableControl[i][0];
				index++;
			}
		}
		return tableHeads;
	}	// End setColumnHeaders
	
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
		TableModel myModel = table_Entity.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);		
		try {
			if (selectString.trim().equals(allColumnsText2.trim())) {				// All Columns
				// For whole table
				sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText));	// case-insensitive unicode filter //$NON-NLS-1$
			} else for (int i = 0; i < tableColHeads.length; i++) {
					if (selectString.trim().equals(tableColHeads[i].trim())) {	
						// For only one column
						sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText, i));	// case-insensitive unicode filter //$NON-NLS-1$
						break;
					}
			}
		} catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(chkbox_Filter, HG05075Msgs.Text_121 						// Cannot use
												+ filterText + HG05075Msgs.Text_122,				// as a filter
												HG05075Msgs.Text_123, JOptionPane.ERROR_MESSAGE);	// Filter Text Error
		}		
	    table_Entity.setRowSorter(sorter);
	    // Set scroll bar to top of filter results
	    scrollPane.getViewport().setViewPosition(new Point(0,0));
	}	// End setTableFilter	
	
/**
 * GUI user messages
 * @param errorCode
 */
	private void userInfoConvertData(int errorCode) {
		String errorMess = ""; //$NON-NLS-1$
		if (errorCode == 1) {
			errorMess = HG05075Msgs.Text_59;
			JOptionPane.showMessageDialog(null, HG05075Msgs.Text_60 +  errorMess,
										HG05075Msgs.Text_61, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoConvertData

	private void userInfoInitVP(int errorCode) {
		if (errorCode == 1) {
			JOptionPane.showMessageDialog(null, HG05075Msgs.Text_62 + maxLocationVPIs,
										HG05075Msgs.Text_63, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode == 2) {
			JOptionPane.showMessageDialog(null, HG05075Msgs.Text_64,
										HG05075Msgs.Text_65, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoInitVP

}  // End of HG0507LocationSelect

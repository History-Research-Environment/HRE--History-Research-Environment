package hre.gui;
/***************************************************************************************************
 * HRE ViewLocation - Specifications 05.30 GUI_Viewpoint
 * v0.00.0022 2020-05-10 Split off from Person VP to separate code and totally
 *                         rewritten to implement collapsable panel design (D Ferguson)
 *            2020-08-02 always put Project name in Frame Title (D Ferguson)
 * v0.01.0023 2020-09-04 all sub-panel build code merged back into here (D Ferguson)
 * 			  2020-09-24 Implemented LocationViewpoint reopen of screen (N. Tolleshaug)
 *            2020-09-30 changed panels to MigLayout; fonts removed for JTattoo (D Ferguson)
 * 			  2020-10-03 Errors corrected position + size setting (N. Tolleshaug)
 * v0.01.0025 2020-10-27 Added +/- controls on p2, p3 and other format changes (D Ferguson)
 * 			  2020-12-09 Removed GridBag from Splitpanes, changed to MigLayout (D Ferguson)
 * 			  2020-12-12 Fix splitpanes changing size when panels opened (D Ferguson)
 * 			  2021-02-27 Implemented list of events table in Location VP (N. Tolleshaug)
 *            2021-03-06 Implemented handling of 5 Location VP (N. Tolleshaug)
 * v0.01.0027 2021-12-02 Added timing console msgs for start/end of VP (D Ferguson)
 *			  2021-12-07 Aligned Action bar colors with Theme colors (D Ferguson)
 *			  2021-12-09 Make screen scroll if height exceeds mainPane (D Ferguson)
 * 			  2022-03-01 Converted to NLS (D Ferguson) (updated 2022-04-22)
 * 			  2022-03-07 Modified to use HGloDataMsgs (D Ferguson)
 * 			  2022-04-13 Set PID for EventVP into GUI config data (N. Tolleshaug)
 * 			  2022-07-30 Implement 'not-present' image if no image applies (D Ferguson)
 * v0.01.0028 2023-01-08 Translated table headers collected from T204 (N. Tolleshaug)
 * v0.03.0030 2023-06-30 Activated witness on/off for event table (N. Tolleshaug)
 * 			  2023-09-24 If frame is full-screen don't do special size control code (D Ferguson)
 * v0.03.0031 2024-11-04 Ensure all data non-editable (D Ferguson)
 * 			  2024-11-04 Fix +/- buttons failing if screen maximised (D Ferguson)
 *			  2024-12-01 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * v0.04.0032 2026-01-05 Log catch block and DEBUG actions (D Ferguson)
 *************** ***********************************************************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import hre.bila.HB0711Logging;
import hre.bila.HBBusinessLayer;
import hre.bila.HBException;
import hre.bila.HBMediaHandler;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBViewPointHandler;
import hre.nls.HG05303Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Viewpoint for Location with collapsing panels for Location Viewpoint structure
 * @author originally bbrita on Sun Java forums c.2006; modified extensively since
 * @author for this version D Ferguson
 * @version v0.03.0032
 * @since 2020-05-10
 */

public class HG0530ViewLocation extends HG0451SuperIntFrame implements MouseListener {
	private static final long serialVersionUID = 1L;

	boolean printNr = HGlobal.DEBUG;
	public String screenID = "53030"; 		//$NON-NLS-1$
	public String tableScreenID = "53030";  //$NON-NLS-1$
	final static int maxEventVPIs = HGlobal.maxEventVPIs;
	final static int maxPersonVPIs = HGlobal.maxPersonVPIs;

	String[] eventTableHeader;
	Object[][] objEventData;
	int allEvents = 0;
	int witnessEvents = 0;

	private String className;
	private int locationVPindex = 0;
	int maxHeight, widthFrame, heightFrame, dividerLocation;	// see correctPanelSize
	JInternalFrame locnFrame = this;
	private JPanel contents;
	JScrollPane scrollPanel;
	JSplitPane locationPanel;
    ActionLPanel[] actPanels;
    JPanel[] dataPanels;
    JPanel p00 = new JPanel(); 		// define extra padding panel for right side
    private String vpProject;		// to save Project name of this Viewpoint
    HBViewPointHandler pointViewpointHandler = null;
    HBProjectOpenData pointOpenProject;
	protected HBPersonHandler pointPersonHandler;
	public static HBBusinessLayer[] pointBusinessLayer = new HBBusinessLayer[10];

/**
 * String getClassName()
 * @return className
 */
        public String getClassName() {
        	return className;
        }
/**
 * Build the Location Viewpoint
 */
    public HG0530ViewLocation(HBViewPointHandler pointViewpointHandler,
    							HBProjectOpenData pointOpenProject,
    							int locationVPindex,
    							String screenID)  {
    	super(pointViewpointHandler," Location ViewPoint",true,true,true,true); //$NON-NLS-1$
    	this.pointOpenProject = pointOpenProject;
    	this.pointViewpointHandler = pointViewpointHandler;
    	this.locationVPindex = locationVPindex;
    	this.screenID = screenID;
		this.setResizable(true);
		pointPersonHandler = pointOpenProject.getPersonHandler();

		// Setup references for HG0451
		windowID = screenID;
		helpName = "locationvp"; //$NON-NLS-1$
		className = getClass().getSimpleName();

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0530 ViewLocation"); //$NON-NLS-1$
		if (HGlobal.TIME) HGlobalCode.timeReport("start HG0530ViewLoc on thread "+Thread.currentThread().getName()); //$NON-NLS-1$

		// Get project name
		vpProject = pointOpenProject.getProjectName();

		// Set Project name in Title
		locnFrame.setTitle(HG05303Msgs.Text_5 + (locationVPindex+1) 		// Location ViewPoint #
						 + HG05303Msgs.Text_6  + vpProject); 				// for Project

		// Define Content pane
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[grow, fill]", "[grow, fill]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Build the ActionPanels, DataPanels into locationPanel and display
        makeActionPanels();
        makeDataPanels();

        // Setup scrolling Panel containing peoplePanel
		scrollPanel = new JScrollPane(makeLocationPanel());
		scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        maxHeight = HG0401HREMain.mainPane.getHeight();
        scrollPanel.setMaximumSize(new Dimension(5000, maxHeight - 60));  // allow huge width but control height
		contents.add(scrollPanel);
      	pack();
        //Store initial frame height
        frameHeight = locnFrame.getHeight();

        // Add the Viewpoint to the Main menu pane and make it visible
     	HG0401HREMain.mainPane.add(locnFrame);
     	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: displaying Location Viewpoint from HG0530 ViewLocation");  //$NON-NLS-1$
		if (HGlobal.TIME) HGlobalCode.timeReport("end HG0530ViewLoc on thread "+Thread.currentThread().getName()); //$NON-NLS-1$

        locnFrame.setVisible(true);

	// Listener for Frame closing event
		locnFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
			public void internalFrameClosing(InternalFrameEvent e) {
			// close reminder display if open
				if (reminderDisplay != null) reminderDisplay.dispose();

			// Set frame size in GUI data
					Dimension frameSize = getSize();

			// Limit screen height to initial dimension
					double width = frameSize.getWidth();
					Dimension newSize = new Dimension();
					newSize.setSize(width, frameHeight);
					pointOpenProject.setSizeScreen(screenID,newSize);

			// Set position	in GUI data
					Point position = getLocation();
					pointOpenProject.setPositionScreen(screenID,position);

			// Mark the screen as closed in T302
					pointOpenProject.closeStatusScreen(screenID);

					if (printNr)
						System.out.println("Location VP counter nr: "  //$NON-NLS-1$
								+ pointOpenProject.getLocationVP());

			// Count down the Location view screens
					if (pointOpenProject.getLocationVP() > 0)
									pointOpenProject.countDownLocationVP();

			// Set class name in GUI config data
					pointOpenProject.setClassName(screenID, className);

			// Close window
				    if (HGlobal.writeLogs)  HB0711Logging.logWrite("Action: closing a Location Viewpoint in HG0530 ViewLocation"); //$NON-NLS-1$
				    dispose();
            }
         });

    }	// End HG0530ViewLocation constructor

/**
 * userInfoInitVP(int errorCode)
 * @param errorCode
 */
	private void userInfoInitVP(int errorCode) {
		if (errorCode == 1) {
			JOptionPane.showMessageDialog(contents,  HG05303Msgs.Text_88 		//Too many Event Viewpoints
												+ HG05303Msgs.Text_81		//Maximum allowed is
												+ maxEventVPIs,
					HG05303Msgs.Text_89, JOptionPane.INFORMATION_MESSAGE);	// Event Viewpoint Creation
			return;
		}
		if (errorCode == 2) {
			JOptionPane.showMessageDialog(contents,  HG05303Msgs.Text_83,		// Create Location ViewPoint error
					HG05303Msgs.Text_82, JOptionPane.ERROR_MESSAGE);		// Location Viewpoint Creation
			return;
		}
		if (errorCode == 3) {
			JOptionPane.showMessageDialog(contents,  HG05303Msgs.Text_84			// Image Thumbnail Error
												+ HG05303Msgs.Text_85		// From TMG Exhibit Log, perform a
												+ HG05303Msgs.Text_86		// 'Refresh all thumbnails' command,
												+ HG05303Msgs.Text_87,		// then re-import the TMG file.
						HG05303Msgs.Text_82, JOptionPane.ERROR_MESSAGE);	// Location Viewpoint Creation
			return;
		}
		if (errorCode == 4) {
			JOptionPane.showMessageDialog(contents,  HG05303Msgs.Text_90 		//Too many Person Viewpoints
												+ HG05303Msgs.Text_81 		//Maximum allowed is
												+ maxPersonVPIs,
					HG05303Msgs.Text_91, JOptionPane.INFORMATION_MESSAGE);	// Person Viewpoint Creation
			return;
		}
		System.out.println(" HG0530ViewLocation - Not identified errorcode: " + errorCode);	//$NON-NLS-1$
	}	// End userInfoInitVP

/**
 * Create the Action panels that control the dataPanels
 */
    private void makeActionPanels() {
        // actionPanel titles - first one never used as first dataPanel always open
    	String eventsTitle = HG05303Msgs.Text_15 + " (" + pointViewpointHandler.getNumberEvents(locationVPindex) + ")"; // Events (#) //$NON-NLS-1$ //$NON-NLS-2$
       	String superTitle = HG05303Msgs.Text_17 + " (" + "0" + ")";  	// Superior/Inf (0)      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       	String personTitle = HG05303Msgs.Text_18 + " (" + pointViewpointHandler.getNumberPersons(locationVPindex) + ")";   // Associates (#) //$NON-NLS-1$ //$NON-NLS-2$
    	String imagesTitle = HG05303Msgs.Text_20 + " (" + pointViewpointHandler.getLocationNumberOfImages(locationVPindex) + ")"; 	// Images (0) //$NON-NLS-1$ //$NON-NLS-2$
    	String flagsTitle = HG05303Msgs.Text_21 + " (" + "0" + ")";  	// Flags (0)      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String notepadTitle = HG05303Msgs.Text_22 + " (" + "0" + ")";   // Notepads (0)   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String[] titles = { "not used",  //$NON-NLS-1$
				eventsTitle, superTitle, personTitle,
				imagesTitle, flagsTitle, notepadTitle};
        actPanels = new ActionLPanel[titles.length];
        for(int j = 0; j < actPanels.length; j++)
            actPanels[j] = new ActionLPanel(titles[j], this);
    }	// End makeActionPanels

/**
 * Create dataPanels for each of the Location Viewpoint sections and build the panel array
 */
    private void makeDataPanels() {
        // Build all dataPanels and add into the dataPanels array
        JPanel p0 = locnInitial();
        JPanel p1 = locnEvents();
        JPanel p2 = locnSupInf();
        JPanel p3 = locnPeople();
        JPanel p4 = locnImages();
        JPanel p5 = locnFlags();
        JPanel p6 = locnNotes();
        dataPanels = new JPanel[] {p0, p1, p2, p3, p4, p5, p6};
        p00.setSize(250, p0.getHeight());		// set pad-panel p00 for top of right splitpane
    }	// End makeDataPanels

 // Define panel P0 for the initial Location data panel - this is always showing
    private JPanel locnInitial() {
        JPanel p0 = new JPanel(new MigLayout("insets 10", "[]10[grow][]", "[]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JLabel label01 = new JLabel(HG05303Msgs.Text_26);		// Location
        JTextField text01 = new JTextField(pointViewpointHandler.getLocationName(locationVPindex));
        text01.setEditable(false);
        text01.setPreferredSize(new Dimension(530, 16));
 		p0.add(label01, "cell 0 0"); //$NON-NLS-1$
		p0.add(text01, "cell 1 0, growx");  //$NON-NLS-1$
     	p0.add(btn_Remindericon, "cell 2 0");   //$NON-NLS-1$
    	p0.add(btn_Helpicon, "cell 2 0");  	//$NON-NLS-1$
		return p0;
    }	// End locnInitial

 // Define panel P1 for Events at this location
    private JPanel locnEvents() {
		JPanel p1 = new JPanel(new MigLayout("insets 10", "[]10[grow]10[]", "[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JCheckBox checkWitness = new JCheckBox(HG05303Msgs.Text_34);  // Includes Witnessed Events
		checkWitness.setSelected(true);
		checkWitness.setHorizontalTextPosition(SwingConstants.TRAILING);
		checkWitness.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton button11 = new JButton("+"); //$NON-NLS-1$
		button11.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button11.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		JButton button12 = new JButton("-"); //$NON-NLS-1$
		button12.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button12.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$

    	if (HGlobal.DEBUG && HGlobal.writeLogs)
    		HB0711Logging.logWrite("Status: in HG0530VL Initial Location:  1-" 	//$NON-NLS-1$
					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "1", false)));	//$NON-NLS-1$

		JTable table11 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};
		// Get event data, table header and event count
		objEventData = pointViewpointHandler.getEventTable(locationVPindex);
		eventTableHeader = pointViewpointHandler.setTranslatedData(tableScreenID, "1", false); //$NON-NLS-1$	// get Event Tag, Date, Summary
		allEvents = pointViewpointHandler.getNumberEvents(locationVPindex);
		// Build table
		table11.setModel(new DefaultTableModel(
				objEventData,
				eventTableHeader));
		table11.getColumnModel().getColumn(0).setPreferredWidth(120);
		table11.getColumnModel().getColumn(0).setMinWidth(70);
		table11.getColumnModel().getColumn(1).setPreferredWidth(120);
		table11.getColumnModel().getColumn(1).setMinWidth(70);
		table11.getColumnModel().getColumn(2).setPreferredWidth(400);
		table11.getColumnModel().getColumn(2).setMinWidth(200);
		// Set row selection action and header
		ListSelectionModel pcellSelectionModel = table11.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JTableHeader tHd11 = table11.getTableHeader();
		tHd11.setOpaque(false);
		// Set viewport size
		table11.setFillsViewportHeight(true);
		int nErows = table11.getRowCount();
		if (nErows == 0) nErows = 1;
		if (nErows > 5) nErows = 5;						// show 1-5 rows as default
		table11.setPreferredScrollableViewportSize(new Dimension(640, nErows*table11.getRowHeight()));
		JScrollPane scroll11 = new JScrollPane(table11);

		p1.add(checkWitness, "cell 0 0");			//$NON-NLS-1$
		p1.add(button11, "cell 2 0, alignx right"); //$NON-NLS-1$
		p1.add(button12, "cell 2 0"); 				//$NON-NLS-1$
		p1.add(scroll11, "cell 0 1 3, span, growx, growy");    //$NON-NLS-1$

		// Listener for 'Show witnessed events' CheckBox
		checkWitness.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		        boolean state;
		        if (checkWitness.isSelected()) state = true;
		        	else state = false;

		      	try {
				// Set up witness state prompt and recreate table
		      		pointViewpointHandler.setLocationWitnessState(locationVPindex, state);
		      		if (state) checkWitness.setText(HG05303Msgs.Text_34); // Includes Witnessed Events
		      			else {witnessEvents = allEvents - pointViewpointHandler.getNumberEvents(locationVPindex);
		      				  checkWitness.setText(HG05303Msgs.Text_35 + witnessEvents + HG05303Msgs.Text_36); // Include xx Witnessed Events
		      				}
				// Recreate table data
		      		objEventData = pointViewpointHandler.getEventTable(locationVPindex);
					DefaultTableModel eventModel = (DefaultTableModel) table11.getModel();
					eventModel.setDataVector(objEventData, eventTableHeader);
				// Reset table renderers
					table11.getColumnModel().getColumn(0).setPreferredWidth(120);
					table11.getColumnModel().getColumn(0).setMinWidth(70);
					table11.getColumnModel().getColumn(1).setPreferredWidth(120);
					table11.getColumnModel().getColumn(1).setMinWidth(70);
					table11.getColumnModel().getColumn(2).setPreferredWidth(400);
					table11.getColumnModel().getColumn(2).setMinWidth(200);

				} catch (Exception hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0530VL setWitnessState error " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
		      }
		});

		// Listener to action selection of row in event table - create EventVP
		pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int viewRow = table11.getSelectedRow();
					int selectedRowInTable = table11.convertRowIndexToModel(viewRow);
					if (selectedRowInTable < 0) return;	//exit if listener call was caused by emptying table_User
					try {
				// Find next open eventVP
						int eventVPindex = pointViewpointHandler.findClosedVP("5306", pointOpenProject);	//$NON-NLS-1$
						String eventVPident = pointViewpointHandler.getEventScreenID(eventVPindex);
						long eventPID = pointViewpointHandler.getLocationEventPID(locationVPindex, selectedRowInTable);

				// Set PID for event from event list
						if (eventVPident != null)
							pointOpenProject.pointGuiData.setTableViewPointPID(eventVPident, eventPID);
						else {
							userInfoInitVP(1);
					    	if (HGlobal.DEBUG && HGlobal.writeLogs)
					    		HB0711Logging.logWrite("Status: in HG0530VL valueChanged - eventVPindex: "	//$NON-NLS-1$
										+ eventVPindex);
						}
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0530VL select Event error " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		});

		// Listener for Event table mouse Double and Right-click
		table11.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				int rowClicked, colClicked, selectedRowInTable, selectedRow, errorCode = 0;
				long eventTablePID;
				if (me.getClickCount() == 2 && table11.getSelectedRow() != -1) {
	           		rowClicked = table11.getSelectedRow();
	           		colClicked = table11.getSelectedColumn();
	           		selectedRowInTable = table11.convertRowIndexToModel(rowClicked);
	           		eventTablePID = pointViewpointHandler.getLocationEventPID(locationVPindex, selectedRowInTable);
					// Double-click, open EventVP
	           		if (colClicked == 2) {
	           	    	if (HGlobal.DEBUG && HGlobal.writeLogs)
	           	    		HB0711Logging.logWrite("Status: in HG0530VL RClick Vpindex: " + locationVPindex + " Selected Row: " //$NON-NLS-1$ //$NON-NLS-2$
	           					+ selectedRowInTable + " EventPID: " + eventTablePID);	//$NON-NLS-1$
	           			errorCode = pointViewpointHandler.initiateEventVP(pointOpenProject, eventTablePID);
	           			if (errorCode > 0) userInfoInitVP(errorCode);

	           		} else {
	           			errorCode = pointViewpointHandler.initiateEventVP(pointOpenProject, eventTablePID);
	           			if (errorCode > 0) userInfoInitVP(errorCode);
	           		}

                } else
				// Right-click, open EventVP
                if (me.getButton() == MouseEvent.BUTTON3) {
                	selectedRow = table11.rowAtPoint(me.getPoint());
                	table11.addRowSelectionInterval(selectedRow, selectedRow);
               		colClicked = table11.getSelectedColumn();
               		selectedRowInTable = table11.convertRowIndexToModel(selectedRow);
               		eventTablePID = pointViewpointHandler.getLocationEventPID(locationVPindex, selectedRowInTable);

               	    if (HGlobal.DEBUG)
               			System.out.println(" HG0530ViewLocation RClick Vpindex: " + locationVPindex + " Selected Row: " //$NON-NLS-1$ //$NON-NLS-2$
         					+ selectedRowInTable + " EventPID: " + eventTablePID);										//$NON-NLS-1$

           			errorCode = pointViewpointHandler.initiateEventVP(pointOpenProject, eventTablePID);
           			if (errorCode > 0) userInfoInitVP(errorCode);
                }
	        }
	    });

		// These buttons allow the scrollPane to be made bigger/smaller as it has no edge to resize with
		button11.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll11.getVerticalScrollBar().setValue(1);	// set slider to bottom
				int max = table11.getRowHeight()*table11.getRowCount();
				int w1 = scroll11.getWidth();
				int h1plus = (int)Math.round(scroll11.getHeight()*1.3);
				if (h1plus > max) h1plus = max;					// stop increasing past table size
				table11.setPreferredScrollableViewportSize(new Dimension(w1, h1plus));
				scroll11.revalidate();
				correctPanelSize();
			}
		});
		button12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll11.getVerticalScrollBar().setValue(0);	// set slider to yop
				int w2 = scroll11.getWidth();
				int h1minus = (int)Math.round(scroll11.getHeight()*.75);
				table11.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll11.revalidate();
				correctPanelSize();
			}
		});

		return p1;
    }	// End locnEvents

 // Define panel P2 for Superior/Inferior location parts
    private JPanel locnSupInf() {
		JPanel p2 = new JPanel(new MigLayout("insets 10", "[]10[grow]10[]", "[]10[grow]"));			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JLabel label21 = new JLabel(HG05303Msgs.Text_55); 		// Show Locations By
		JComboBox<String> combo21 = new JComboBox<>();
		combo21.setPreferredSize(new Dimension(170, 20));
		combo21.addItem(HG05303Msgs.Text_56);					// Locality Size
		JButton button21 = new JButton(HG05303Msgs.Text_57);	// Options
		JButton button22 = new JButton("+"); //$NON-NLS-1$
		button22.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button22.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		JButton button23 = new JButton("-"); //$NON-NLS-1$
		button23.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button23.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$

    	if (HGlobal.DEBUG && HGlobal.writeLogs)
    		HB0711Logging.logWrite("Status: in HG0530VL Superior location:  2-" 		//$NON-NLS-1$
					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "2", false)));	//$NON-NLS-1$

		JTable table21 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};
		table21.setModel(new DefaultTableModel(
				new Object[][] {									// <-- to be replaced with real data
					{null, null, null},
					{null, null, null},
					{null, "target location", null}, //$NON-NLS-1$
					{null, null, null},
					{null, null, null},
					{null, null, null},
				},
				pointViewpointHandler.setTranslatedData(tableScreenID, "2", false)		//$NON-NLS-1$
			));
		table21.getColumnModel().getColumn(0).setPreferredWidth(250);
		table21.getColumnModel().getColumn(0).setMinWidth(150);
		table21.getColumnModel().getColumn(1).setPreferredWidth(250);
		table21.getColumnModel().getColumn(1).setMinWidth(100);
		table21.getColumnModel().getColumn(2).setPreferredWidth(140);
		table21.getColumnModel().getColumn(2).setMinWidth(50);
		JTableHeader tHd21 = table21.getTableHeader();
		tHd21.setOpaque(false);
		table21.setFillsViewportHeight(true);
		int nLrows = table21.getRowCount();
		if (nLrows == 0) nLrows = 1;
		if (nLrows > 5) nLrows = 5;						// show 1-5 rows as default
		table21.setPreferredScrollableViewportSize(new Dimension(640, nLrows*table21.getRowHeight()));
		JScrollPane scroll21 = new JScrollPane(table21);

		p2.add(label21, "cell 0 0"); //$NON-NLS-1$
		p2.add(combo21, "cell 1 0, gapright 10"); //$NON-NLS-1$
		p2.add(button21, "cell 1 0"); //$NON-NLS-1$
		p2.add(button22, "cell 2 0, alignx right"); //$NON-NLS-1$
		p2.add(button23, "cell 2 0"); //$NON-NLS-1$
		p2.add(scroll21, "cell 0 1, span, growx, growy"); //$NON-NLS-1$

		// These buttons allow the scrollPane to be made bigger/smaller as it has no edge to resize with
		button22.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll21.getVerticalScrollBar().setValue(1);	// set slider to bottom
				int max = table21.getRowHeight()*table21.getRowCount();
				int w1 = scroll21.getWidth();
				int h1plus = (int)Math.round(scroll21.getHeight()*1.3);
				if (h1plus > max) h1plus = max;					// stop increasing past table size
				table21.setPreferredScrollableViewportSize(new Dimension(w1, h1plus));
				scroll21.revalidate();
				correctPanelSize();
			}
		});
		button23.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll21.getVerticalScrollBar().setValue(0);	// set slider to yop
				int w2 = scroll21.getWidth();
				int h1minus = (int)Math.round(scroll21.getHeight()*.75);
				table21.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll21.revalidate();
				correctPanelSize();
			}
		});

		return p2;
    }	// End locnSupInf

// Define panel P3 for Associated People of this location
    private JPanel locnPeople() {
 		JPanel p3 = new JPanel(new MigLayout("insets 10", "[]10[grow]10[]10[]", "[]10[grow]"));		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JButton button31 = new JButton("+"); //$NON-NLS-1$
		button31.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button31.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		JButton button32 = new JButton("-"); //$NON-NLS-1$
		button32.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button32.setFont(new Font("Arial", Font.BOLD, 15));		 //$NON-NLS-1$

    	if (HGlobal.DEBUG && HGlobal.writeLogs)
    		HB0711Logging.logWrite("Status: in HG0530VL Associated People:  3-" 	//$NON-NLS-1$
					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "3", false)));	//$NON-NLS-1$

 		JTable table31 = new JTable() { private static final long serialVersionUID = 1L;
 		 								@Override
 		 								public boolean isCellEditable(int row, int column) {return false;}};
 		table31.setModel(new DefaultTableModel(
 				pointViewpointHandler.getPersonTable(locationVPindex),
 				pointViewpointHandler.setTranslatedData(tableScreenID, "3", false) //$NON-NLS-1$ // get Person, Role, Event, Date
 			));
		table31.getColumnModel().getColumn(0).setPreferredWidth(240);
		table31.getColumnModel().getColumn(0).setMinWidth(150);
		table31.getColumnModel().getColumn(1).setPreferredWidth(100);
		table31.getColumnModel().getColumn(1).setMinWidth(70);
		table31.getColumnModel().getColumn(2).setPreferredWidth(200);
		table31.getColumnModel().getColumn(2).setMinWidth(100);
		table31.getColumnModel().getColumn(3).setPreferredWidth(100);
		table31.getColumnModel().getColumn(3).setMinWidth(70);

		// Set row selection action and header
		ListSelectionModel pcellSelectionModel = table31.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader tHd31 = table31.getTableHeader();
		tHd31.setOpaque(false);
		table31.setFillsViewportHeight(true);
		int nProws = table31.getRowCount();
		if (nProws == 0) nProws = 1;
		if (nProws > 5) nProws = 5;						// show 1-5 rows as default
		table31.setPreferredScrollableViewportSize(new Dimension(640, nProws*table31.getRowHeight()));
		JScrollPane scroll31 = new JScrollPane(table31);

		p3.add(button31, "cell 2 0, alignx right"); //$NON-NLS-1$
		p3.add(button32, "cell 2 0");		 //$NON-NLS-1$
 		p3.add(scroll31, "cell 0 1, span, growx, growy");  //$NON-NLS-1$

		// Listener to action selection of row in associate table - create personVP but do not open it
		pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int viewRow = table31.getSelectedRow();
					int selectedRowInTable = table31.convertRowIndexToModel(viewRow);
					if (selectedRowInTable < 0) return;				//exit if listener call was caused by emptying table_User
					try {
				// Find next open personVP
						int personVPindex = pointViewpointHandler.findClosedVP("5300", pointOpenProject);	//$NON-NLS-1$
						String personVPident = pointViewpointHandler.getPersonScreenID(personVPindex);
						long personPID = pointViewpointHandler.getLocationAssociatePID(locationVPindex, selectedRowInTable);

				// Set PID for person from person list
						if (personVPident != null)
							pointOpenProject.pointGuiData.setTableViewPointPID(personVPident, personPID);
						else {
							userInfoInitVP(4);
					    	if (HGlobal.DEBUG && HGlobal.writeLogs)
					    		HB0711Logging.logWrite("Status: in HG0530VL valueChanged - personVPindex: "	//$NON-NLS-1$
										+ personVPindex);
						}
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0530VL select Associate error " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		});

	// Listener for Associates table mouse click
		table31.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				int rowClicked, colClicked, selectedRowInTable, selectedRow, errorCode = 0;
				long personTablePID;
	           	if (me.getClickCount() == 2 && table31.getSelectedRow() != -1) {
	           	// DOUBLE-CLICK
	           		rowClicked = table31.getSelectedRow();
	           		colClicked = table31.getSelectedColumn();
	           		personTablePID = pointViewpointHandler.getLocationAssociatePID(locationVPindex, rowClicked);

		        // DOUBLE-CLICK - if column = 0, change ManagePerson focus to this Person
	           		if (colClicked == 0) {
	           			pointPersonHandler.initiateManagePerson(pointOpenProject, personTablePID, screenID);
	           		}
	           		else if (colClicked > 0) {
			   	// DOUBLE-CLICK other columns open Person VP for this Person
	           			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
	           			if (errorCode > 0) userInfoInitVP(errorCode);
						return;
	           		}
	           	} else
	          // RIGHT-click, open PersonVP
	            if (me.getButton() == MouseEvent.BUTTON3) {
                	selectedRow = table31.rowAtPoint(me.getPoint());
                	table31.addRowSelectionInterval(selectedRow, selectedRow);
               		colClicked = table31.getSelectedColumn();
               		selectedRowInTable = table31.convertRowIndexToModel(selectedRow);
               		personTablePID = pointViewpointHandler.getLocationAssociatePID(locationVPindex, selectedRowInTable);

                	if (HGlobal.DEBUG && HGlobal.writeLogs)
                		HB0711Logging.logWrite("Status: in HG0530VL RClick Vpindex: " + locationVPindex + " Selected Row: " //$NON-NLS-1$ //$NON-NLS-2$
         					+ selectedRowInTable + " PersonPID: " + personTablePID);									//$NON-NLS-1$

	       			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
	       			if (errorCode > 0) userInfoInitVP(errorCode);
	            }
	        }
	    });

		// These buttons allow the scrollPane to be made bigger/smaller as it has no edge to resize with
		button31.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll31.getVerticalScrollBar().setValue(1);	// set slider to bottom
				int max = table31.getRowHeight()*table31.getRowCount();
				int w1 = scroll31.getWidth();
				int h1plus = (int)Math.round(scroll31.getHeight()*1.3);
				if (h1plus > max) h1plus = max;					// stop increasing past table size
				table31.setPreferredScrollableViewportSize(new Dimension(w1, h1plus));
				scroll31.revalidate();
				correctPanelSize();
			}
		});
		button32.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scroll31.getVerticalScrollBar().setValue(0);	// set slider to yop
				int w2 = scroll31.getWidth();
				int h1minus = (int)Math.round(scroll31.getHeight()*.75);
				table31.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll31.revalidate();
				correctPanelSize();
			}
		});
 		return p3;
    }	// End locnPeople

// Define panel for Images
    private JPanel locnImages() {
        ArrayList<ImageIcon> listImages = pointViewpointHandler.getLocationImageList(locationVPindex);
        // If no images, show not present image
        if (listImages == null || listImages.size() == 0) {
        	JPanel p4 = new JPanel(new GridLayout(1, 1, 1, 1));
        	ImageIcon scaledExhibit = new ImageIcon(getClass().getResource("/hre/images/notpresent-locn-100.png"));	//$NON-NLS-1$
        	p4.add(new JLabel(scaledExhibit));
        	return p4;
        }

        // If images present, put in 2-column, x row grid, scaled to fit the setSize
        JPanel p4 = new JPanel(new GridLayout(0, 2, 10, 10));
        JLabel labels[] = new JLabel[listImages.size()];
        p4.setSize(100,200);
        for (int i = 0; i < listImages.size(); i++) {
        	ImageIcon exhibitImage = listImages.get(i);
            if (exhibitImage != null) {
            	// Rescale exhibitImage to a new max. height/width to match p4
            	//HBMediaHandler pointMediaHandler = (HBMediaHandler)HG0401HREMain.pointBusinessLayer[10];
            	HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
        		ImageIcon scaledExhibit = pointMediaHandler.scaleImage(90, 90, exhibitImage);
             	labels[i] = new JLabel(scaledExhibit);
            }
            p4.add(labels[i]);
        }
        return p4;

    }	// End locnImages

 // Define panel for Flags - to be fully created later
    private JPanel locnFlags() {
        JPanel p5 = new JPanel(new GridLayout(1, 1, 1, 1));
        JTextArea text50 = new JTextArea(3, 14);
        text50.setLineWrap(true);
        text50.setText("Flags not implemented yet");         //$NON-NLS-1$
        p5.add(new JScrollPane(text50));
        return p5;
    }	// End locnFlags

// Define panel for Notepads - to be fully created later
    private JPanel locnNotes() {
        JPanel p6 = new JPanel(new GridLayout(1, 1, 1, 1));
        JTextArea text60 = new JTextArea(3, 14);
        text60.setLineWrap(true);
        text60.setText("Notepads not implemented yet");       //$NON-NLS-1$
        p6.add(new JScrollPane(text60));
        return p6;
    }	// End locnNotes

/**
 * makeLocationPanel - assembles the Action & Data Panels together into the total LocationPanel
 * @return	the assembled locationPanel (a JSplitPane with 2 MigLayouts)
 */
    private JSplitPane makeLocationPanel() {
    	// Setup the left Splitpane from dataPanels p0 to p3
    	JPanel locLeft = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][][][][][]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // First, add in dataPanel p0 with no actionPanel
        locLeft.add(dataPanels[0], "cell 0 0,growx"); //$NON-NLS-1$
        // Then add panels 1 - 3 action & data panels
       	locLeft.add(actPanels[1], "cell 0 1, growx"); //$NON-NLS-1$
    	locLeft.add(dataPanels[1], "cell 0 2, growx"); //$NON-NLS-1$
       	locLeft.add(actPanels[2], "cell 0 3, growx"); //$NON-NLS-1$
    	locLeft.add(dataPanels[2], "cell 0 4, growx"); //$NON-NLS-1$
       	locLeft.add(actPanels[3], "cell 0 5, growx"); //$NON-NLS-1$
    	locLeft.add(dataPanels[3], "cell 0 6, growx"); //$NON-NLS-1$
        for(int j = 1; j < 4; j++) {
            dataPanels[j].setVisible(false);	// set dataPanels 1 - 3 hidden initially
        	}

        // Setup the right Splitpane from padding panel p00 and dataPanels p4-6
       	JPanel locRight = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][][][][][]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // First, add in padding panel p00
        locRight.add(p00, "cell 0 0, growx"); //$NON-NLS-1$
        // Then add panels 4 - 6 action & data panels
        locRight.add(actPanels[4], "cell 0 1, growx"); //$NON-NLS-1$
    	locRight.add(dataPanels[4], "cell 0 2, growx"); //$NON-NLS-1$
        locRight.add(actPanels[5], "cell 0 3, growx"); //$NON-NLS-1$
     	locRight.add(dataPanels[5], "cell 0 4, growx"); //$NON-NLS-1$
        locRight.add(actPanels[6], "cell 0 5, growx"); //$NON-NLS-1$
     	locRight.add(dataPanels[6], "cell 0 6, growx"); //$NON-NLS-1$
        for(int j = 4; j < actPanels.length; j++) {
            dataPanels[j].setVisible(false);	// set dataPanels 4 - 6 hidden initially
        	}

		// Define splitpane locationPanel's parameters
		UIManager.put("SplitPane.centerOneTouchButtons", false);	// ensure buttons are at divider top, not centre //$NON-NLS-1$
    	locationPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, locLeft, locRight);
       	locationPanel.setOneTouchExpandable(true);
       	locationPanel.setDividerSize(10);
       	locationPanel.setResizeWeight(0.9);		// Screen growth goes mostly to left panel
      	locLeft.setMinimumSize(new Dimension(150, 0));			// full size 680, 200
      	locRight.setMinimumSize(new Dimension(100, 0));			// full size 250, 100
      	locationPanel.setContinuousLayout(true);

       	return locationPanel;
    } // End makeLocationPanel

/**
 * This handles the retention of the current Splitpane screen widths after
 * opening an ActionPanel or use of +/- buttons, when a pack() is required
 */
    private void correctPanelSize() {
       	// if frame was maximised, make sure we retain that height
    	if (locnFrame.isMaximum()) maxHeight = locnFrame.getHeight();
    	else maxHeight = HG0401HREMain.mainPane.getHeight() - 60;
    	// Get Frame width and splitpane divider location
    	widthFrame = locnFrame.getWidth();
    	dividerLocation = locationPanel.getDividerLocation();
        scrollPanel.setMaximumSize(new Dimension(5000, maxHeight));  // allow huge width but control height
    	// Reload the scrollPanel
    	locnFrame.revalidate();
		contents.add(scrollPanel);
		// Do pack only if frame not maximised
        if (!locnFrame.isMaximum()) locnFrame.pack();
        // Reset the divider (which creeps right unless you do this)
        locationPanel.setDividerLocation(dividerLocation);
        // Now get the frame height
        heightFrame = locnFrame.getHeight();
        // Restore the Frame to original width with this height (otherwise right panels grow as well!)
        this.setSize(widthFrame, heightFrame);
    }	// End correctPanelSize

/**
 * Handle Mouse events on the Action Panels
 */
    @Override
	public void mousePressed(MouseEvent e) {
        ActionLPanel ap = (ActionLPanel) e.getSource();
        if(ap.target.contains(e.getPoint())) {
            ap.toggleSelected();
            togglePanelVisible(ap);
            correctPanelSize();
        	}
    }	// End mousePressed

    // Interface Mouselistener methods
    @Override
	public void mouseClicked(MouseEvent e) {}
    @Override
	public void mouseEntered(MouseEvent e) {}
    @Override
	public void mouseExited(MouseEvent e) {}
    @Override
	public void mouseReleased(MouseEvent e) {}

/**
 * togglePanelVisible - turns the dataPanels visible or not
 * @param ap  is the index number of the dataPanel
 */
    private void togglePanelVisible(ActionLPanel ap) {
        int index = getPanelIndex(ap);
        if(dataPanels[index].isShowing())
            dataPanels[index].setVisible(false);
        else
            dataPanels[index].setVisible(true);
        ap.getParent().validate();
    }	// End togglePanelVisible

/**
 * Determines the index number of the panel
 */
    private int getPanelIndex(ActionLPanel ap) {
        for(int j = 0; j < actPanels.length; j++)
            if(ap == actPanels[j])
                return j;
        return -1;
    }	// End getPanelIndex

}	// End LocPanels class

/**
 * Class to handle build of the ActionPanels and their pointer controls
 */
class ActionLPanel extends JPanel  {
	private static final long serialVersionUID = 001L;
    String text;
    Font font;
    private boolean selected;
    BufferedImage open, closed;
    Rectangle target;
    final int offset = 30, pad = 5;

/**
 * Defines the ActionPanel layout
 * @param text is used for the panel's title
 * @param ml is the mouselistener for the action pointer
 */
    public ActionLPanel(String text, MouseListener ml) {
		setLayout(null);
        this.text = text;
        addMouseListener(ml);
        font = new Font("Arial", Font.PLAIN, 12); //$NON-NLS-1$
        selected = false;
        Color back = UIManager.getColor("TableHeader.background");  //$NON-NLS-1$
        setBackground(new ColorUIResource(back.getRed(), back.getGreen(), back.getBlue()));
        setBorder(BorderFactory.createRaisedBevelBorder());
        setMinimumSize(new Dimension(200,20));
        createPointers();
        JLabel title = new JLabel(text);
        Color fore = UIManager.getColor("TableHeader.foreground"); //$NON-NLS-1$
        title.setForeground(new ColorUIResource(fore.getRed(), fore.getGreen(), fore.getBlue()));
        title.setBounds(40, 4, 150, 13);
        this.add(title);
        setRequestFocusEnabled(true);
    }	// End ActionLPanel constructor

/**
 * Handle repaint after a pointer change
 */
    public void toggleSelected() {
        selected = !selected;
        repaint();
    }	// End toggleSelected

/**
 * Draw the open/closed pointers on the Action Panel
 */
    @Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if(selected)
            g2.drawImage(open, pad, 0, this);
        else
            g2.drawImage(closed, pad, 0, this);
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics(text, frc);
        float height = lm.getAscent() + lm.getDescent();
        float x = w + offset;
        float y = (h + height)/2 - lm.getDescent();
        g2.drawString(text, x, y);
    }	// End paintComponent

/**
 * Define the shape/color of open/closed pointers
 */
    private void createPointers() {
        int w = 20;
        int h = getMinimumSize().height;
        target = new Rectangle(2, 0, 20, 18);

        // Open pointer is green with blue outline
        open = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = open.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);
        int[] x = { 2, w/2, 18 };
        int[] y = { 4, 15,   4 };
        Polygon p = new Polygon(x, y, 3);
        g2.setPaint(Color.green.brighter());
        g2.fill(p);
        g2.setPaint(Color.blue.brighter());
        g2.draw(p);
        g2.dispose();

        // Closed pointer is red with blue outline
        closed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g2 = closed.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);
        x = new int[] { 3, 13,   3 };
        y = new int[] { 4, h/2, 16 };
        p = new Polygon(x, y, 3);
        g2.setPaint(Color.red);
        g2.fill(p);
        g2.setPaint(Color.blue.brighter());
        g2.draw(p);
        g2.dispose();
    }	// End createPointers

}	// End ActionLPanel class
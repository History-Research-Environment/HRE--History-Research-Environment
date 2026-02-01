package hre.gui;
/***************************************************************************************************
 * HRE ViewEvent - Specifications 05.30 GUI_Viewpoint
 * v0.00.0022 2020-05-29 Split off from People VP to separate code and totally
 *                         rewritten to implement collapsable panel design (D Ferguson)
 *            2020-08-02 always put Project name in Frame Title (D Ferguson)
 * v0.01.0023 2020-09-04 all sub-panel build code merged back into here (D Ferguson)
 * 			  2020-09-24 Implemented EventViewpoint reopen of screen (N. Tolleshaug)
 * 			  2020-09-30 changed panels to MigLayout; fonts removed for JTattoo (D Ferguson)
 * 			  2020-10-03 Errors corrected position + size setting (N. Tolleshaug)
 * v0.01.0025 2020-10-27 Convert to 2-column layout (D Ferguson)
 * 			  2020-12-09 Removed GridBag layout from Splitpanes, converted to MigLayout (D Ferguson)
 * 			  2020-12-12 Fix splitpanes changing size when panels opened (D Ferguson)
 * 			  2020-12-15 Add +/- controls to Assoc panel (D Ferguson)
 * v0.01.0027 2022-03-01 Converted to NLS (D Ferguson)	(updated 2022-04-22)
 * 			  2022-03-07 Modified to use HGloDataMsgs (D Ferguson)
 * 			  2022-04-13 Populated event name and associate table (N. Tolleshaug)
 * 			  2022-07-30 Implement 'not-present' image if no image applies (D Ferguson)
 * v0.01.0028 2023-01-08 Translated table headers collected from T204 (N. Tolleshaug)
 * v0.03.0030 2023-06-30 Activated on/off for associate table (N. Tolleshaug)
 * 			  2023-09-24 If frame is full-screen don't do special size control code (D Ferguson)
 * v0.03.0031 2024-04-28 Fix Assoc table column sizes (D Ferguson)
 * 			  2024-11-04 Ensure all data non-editable (D Ferguson)
 * 			  2024-11-04 Fix +/- buttons failing if screen maximised (D Ferguson)
 * 			  2024-12-01 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * v0.04.0032 2026-01-05 Log catch block and DEBUG actions (D Ferguson)
 ***************************************************************************************************/

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
import hre.bila.HBException;
import hre.bila.HBMediaHandler;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBViewPointHandler;
import hre.nls.HG05306Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Viewpoint for Events with collapsing panels for Event Viewpoint structure
 * @author originally bbrita on Sun Java forums c.2006; modified extensively since
 * @author for this version D Ferguson
 * @version v0.03.0032
 * @since 2020-05-29
 */

public class HG0530ViewEvent extends HG0451SuperIntFrame implements MouseListener {
	private static final long serialVersionUID = 1L;

	boolean printNr = HGlobal.DEBUG;

	public String screenID = "53060";			//$NON-NLS-1$
	public String tableScreenID = "53060";		//$NON-NLS-1$
	private String className;
	private int eventVPindex = 0;

	String[] eventTableHeader;
	Object[][] objAssocData;
	int allEvents = 0;
	int witnessEvents = 0;

	int maxHeight, widthFrame, heightFrame, dividerLocation;	// see correctPanelSize
    JInternalFrame eventFrame = this;
	private JPanel contents;
	JSplitPane eventPanel;
    ActionEPanel[] actPanels;
    JPanel[] dataPanels;
    private String vpProject;		// to save Project name of this Viewpoint
    HBViewPointHandler pointViewpointHandler = null;
    HBProjectOpenData pointOpenProject;
	protected HBPersonHandler pointPersonHandler;

	private static int maxPersonVPIs = HGlobal.maxPersonVPIs;
	private static int maxEventVPIs = HGlobal.maxEventVPIs;

/**
 * String getClassName()
 * @return className
 */
        public String getClassName() {
        	return className;
        }
/**
 * Build the Event Viewpoint
 */
    public HG0530ViewEvent(HBViewPointHandler pointViewpointHandler,
    					   HBProjectOpenData pointOpenProject,
    					   int eventVPindex,
						   String screenID) {
    	super(pointViewpointHandler," Event ViewPoint",true,true,true,true); //$NON-NLS-1$
    	this.pointViewpointHandler = pointViewpointHandler;
       	this.pointOpenProject = pointOpenProject;
       	this.eventVPindex = eventVPindex;
       	this.screenID = screenID;
		pointPersonHandler = pointOpenProject.getPersonHandler();

		// Setup references for HG0451
		windowID = screenID;
		helpName = "eventvp"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0530 ViewEvent"); //$NON-NLS-1$

		// Save the selected Project in a local variable (as may change)
		vpProject = pointOpenProject.getProjectName();

		// Set Project name in Title
		eventFrame.setTitle(HG05306Msgs.Text_7 + (eventVPindex+1) + HG05306Msgs.Text_8 + vpProject);

		// Define Content pane
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[grow]", "[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	// Build the ActionPanels, DataPanels into the eventFrame and display
    	makeActionEPanels();
        makeDataPanels();
        contents.add(makeEventPanel(), "cell 0 0, grow"); //$NON-NLS-1$
        eventFrame.pack();

        //Store initial frame height
        frameHeight = eventFrame.getHeight();

        // Add the Viewpoint to the Main menu pane and make it visible
     	HG0401HREMain.mainPane.add(eventFrame);
     	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: displaying Event Viewpoint from HG0530 ViewEvent"); //$NON-NLS-1$
        eventFrame.setVisible(true);

		// Listener for FrameClosing Event
		eventFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
			public void internalFrameClosing(InternalFrameEvent e) {
			// close reminder display
				if (reminderDisplay != null) reminderDisplay.dispose();

		    // Set frame size in GUI data
				Dimension frameSize = getSize();

			// Limit screen height to initial dimension
				double width = frameSize.getWidth();
				Dimension newSize = new Dimension();
				newSize.setSize(width, frameHeight);
				pointOpenProject.setSizeScreen(screenID, newSize);

			// Set position	in GUI data
				Point position = getLocation();
			 	pointOpenProject.setPositionScreen(screenID,position);

			// Mark the screen as closed in T302
				pointOpenProject.closeStatusScreen(screenID);
				if (printNr)
					System.out.println(" HG0530ViewEvent Event VP counter nr: "  			//$NON-NLS-1$
							+ pointOpenProject.getEventVP() + " ScreenID: " + screenID);	//$NON-NLS-1$

			// Count down the Location view screens
				if (pointOpenProject.getEventVP() > 0)
								pointOpenProject.countDownEventVP();

			// Set class name in GUI config data
				pointOpenProject.setClassName(screenID,"HG0530ViewEvent");	//$NON-NLS-1$

			// Close window
			    if (HGlobal.writeLogs)  HB0711Logging.logWrite("Action: closing Event Viewpoint in HG0530 ViewEvent");	 //$NON-NLS-1$
			    dispose();
            }
		});
    }	// End HG0530ViewEvent constructor

/**
 * userInfoInitVP(int errorCode)
 * @param errorCode
 */
	private void userInfoInitVP(int errorCode) {
	// Max event VP's
		if (errorCode == 1) {
			JOptionPane.showMessageDialog(contents,  HG05306Msgs.Text_88 		// Too many Event Viewpoints
												+ HG05306Msgs.Text_81 		// Maximum allowed is
												+ maxEventVPIs,
					HG05306Msgs.Text_89, JOptionPane.INFORMATION_MESSAGE);	// Event Viewpoint Creation
			return;
		}
		// intiate VP error
		if (errorCode == 2) {
			JOptionPane.showMessageDialog(contents,  HG05306Msgs.Text_83,		// Create Event ViewPoint error
					HG05306Msgs.Text_89, JOptionPane.ERROR_MESSAGE);		// Event Viewpoint Creation
			return;
		}
		// Thumbnail error
		if (errorCode == 3) {
			JOptionPane.showMessageDialog(contents,  HG05306Msgs.Text_84			// Image Thumbnail Error
												+ HG05306Msgs.Text_85		// From TMG Exhibit Log, perform a
												+ HG05306Msgs.Text_86		// 'Refresh all thumbnails' command,
												+ HG05306Msgs.Text_87,		// then re-import the TMG file.
												HG05306Msgs.Text_89, JOptionPane.ERROR_MESSAGE);	// Event Viewpoint Creation
			return;
		}
		// Person VP's
		if (errorCode == 4) {
			JOptionPane.showMessageDialog(contents,  HG05306Msgs.Text_80 		// Too many Person Viewpoints
												+ HG05306Msgs.Text_81 		// Maximum allowed is
												+ maxPersonVPIs,
					HG05306Msgs.Text_82, JOptionPane.INFORMATION_MESSAGE);	// Person Viewpoint Creation
			return;
		}
		System.out.println(" HG0530ViewEvent - Not identified errorcode: " + errorCode);		//$NON-NLS-1$
	}	// End userInfoInitVP

/**
 * Create the Action panels that control the dataPanels
 */
    private void makeActionEPanels() {
        // actionPanel titles - first one never used as first dataPanel always open
    	String associateTitle = HG05306Msgs.Text_19
    			+ " (" + pointViewpointHandler.getEventAssociateNumber(eventVPindex) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    	String imagesTitle = HG05306Msgs.Text_20
    			+ " (" + pointViewpointHandler.getEventNumberOfImages(eventVPindex) + ")"; 	//$NON-NLS-1$ //$NON-NLS-2$
    	String flagsTitle = HG05306Msgs.Text_21
    			+ " (" + "0" + ")"; // Flags (0)      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String notepadTitle = HG05306Msgs.Text_22
    			+ " (" + pointViewpointHandler.getEventNumberOfTexts(eventVPindex) + ")";   //$NON-NLS-1$ //$NON-NLS-2$
    	String[] titles = { "not used", associateTitle, imagesTitle, flagsTitle, notepadTitle}; //$NON-NLS-1$

        actPanels = new ActionEPanel[titles.length];
        for(int j = 0; j < actPanels.length; j++)
            actPanels[j] = new ActionEPanel(titles[j], this);
    }	// End makeActionEPanels

/**
 * Create dataPanels for each of the Event Viewpoint sections and create their array
 */
    private void makeDataPanels() {
    // Build the dataPanels and add into the dataPanels array
        JPanel p0 = eventInitial();
        JPanel p1 = eventAssocs();
        JPanel p2 = eventImages();
        JPanel p3 = eventFlags();
        JPanel p4 = eventNotes();
        dataPanels = new JPanel[] {p0, p1, p2, p3, p4};
    }	// End makeDataPanels

// Define panel for the initial Event data display
    private JPanel eventInitial() {
        JPanel p0 = new JPanel(new MigLayout("insets 5", "[]10[grow][]", "[][][]"));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Set Event description in event VP
        JLabel label01 = new JLabel(HG05306Msgs.Text_26);		// Event
       	p0.add(label01, "cell 0 0");  //$NON-NLS-1$
       	JTextField text01 = new JTextField();
       	text01.setEditable(false);
       	text01.setText(pointViewpointHandler.getEventIdentInfo(eventVPindex));
       	//System.out.println(" Event description: " + pointViewpointHandler.getEventIdentInfo(eventVPindex));
        text01.setPreferredSize(new Dimension(530, 16));
        p0.add(text01, "cell 1 0, growx"); //$NON-NLS-1$
     	p0.add(btn_Remindericon, "cell 2 0");   //$NON-NLS-1$
    	p0.add(btn_Helpicon, "cell 2 0");  	    	 //$NON-NLS-1$

    	// Set Event location
        JLabel label02 = new JLabel(HG05306Msgs.Text_31);		// Location
      	p0.add(label02, "cell 0 1");      	 //$NON-NLS-1$
      	JTextField text02 = new JTextField();
      	text02.setEditable(false);
      	text02.setText(pointViewpointHandler.getEventPlaceInfo(eventVPindex));
        text02.setPreferredSize(new Dimension(530, 16));
        p0.add(text02, "cell 1 1, growx");        //$NON-NLS-1$

        // Set Event date
        JLabel label03 = new JLabel(HG05306Msgs.Text_34);		// Date
     	p0.add(label03, "cell 0 2");  //$NON-NLS-1$
        JTextField text03 = new JTextField();
        text03.setEditable(false);
        text03.setText(pointViewpointHandler.getEventDateInfo(eventVPindex));
        text03.setPreferredSize(new Dimension(530, 16));
        p0.add(text03, "cell 1 2, growx");  //$NON-NLS-1$

		return p0;
    }	// End eventInitial

// Define panel for Associates
    private JPanel eventAssocs() {
    	JPanel p1 = new JPanel(new MigLayout("insets 5", "[]10[grow][][]", "[]5[grow]"));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JCheckBox checkWitness = new JCheckBox(HG05306Msgs.Text_40); // Includes Witnessed Events
		checkWitness.setSelected(true);
		checkWitness.setHorizontalTextPosition(SwingConstants.TRAILING);
		checkWitness.setHorizontalAlignment(SwingConstants.RIGHT);
		p1.add(checkWitness, "cell 0 0");	//$NON-NLS-1$
		JButton button11 = new JButton("+"); //$NON-NLS-1$
		button11.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button11.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		p1.add(button11, "cell 4 0"); //$NON-NLS-1$
		JButton button12 = new JButton("-"); //$NON-NLS-1$
		button12.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button12.setFont(new Font("Arial", Font.BOLD, 15));	 //$NON-NLS-1$
		p1.add(button12, "cell 4 0"); //$NON-NLS-1$

    	if (HGlobal.DEBUG && HGlobal.writeLogs)
    		HB0711Logging.logWrite("Status: in HG0530VE Initial event:  1-" 	//$NON-NLS-1$
					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "1", false)));		//$NON-NLS-1$

		JTable table11 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};
		 // Get assoc data, table header and event count
		objAssocData = pointViewpointHandler.getAssociateEventTable(eventVPindex);
		eventTableHeader = pointViewpointHandler.setTranslatedData(tableScreenID, "1", false); //$NON-NLS-1$
		allEvents = pointViewpointHandler.getEventAssociateNumber(eventVPindex);
		// Build table
		table11.setModel(new DefaultTableModel(objAssocData, eventTableHeader));
		table11.getColumnModel().getColumn(0).setPreferredWidth(250);
		table11.getColumnModel().getColumn(0).setMinWidth(250);
		table11.getColumnModel().getColumn(1).setPreferredWidth(70);
		table11.getColumnModel().getColumn(1).setMinWidth(70);
		table11.getColumnModel().getColumn(2).setPreferredWidth(150);
		table11.getColumnModel().getColumn(2).setMinWidth(150);
		table11.getColumnModel().getColumn(3).setPreferredWidth(70);
		table11.getColumnModel().getColumn(3).setMinWidth(70);

		// Set row selection action and header
		ListSelectionModel pcellSelectionModel = table11.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader tHd11 = table11.getTableHeader();
		tHd11.setOpaque(false);
		table11.setFillsViewportHeight(true);
		int nErows = table11.getRowCount();
		if (nErows == 0) nErows = 1;
		if (nErows > 5) nErows = 5;						// show 1-5 rows as default
		table11.setPreferredScrollableViewportSize(new Dimension(640, nErows*table11.getRowHeight()));
		JScrollPane scroll11 = new JScrollPane(table11);
		p1.add(scroll11, "cell 0 1 2, span, growx, growy");  //$NON-NLS-1$

		// Listener for 'Show witnessed events' CheckBox
		checkWitness.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		        boolean state;
		        if (checkWitness.isSelected()) state = true;
		        	else state = false;
		      	try {
				// Set up witness state and recreate table
		      		pointViewpointHandler.setEventWitnessState(eventVPindex, state);
		      		if (state) checkWitness.setText(HG05306Msgs.Text_40); // Includes Witnessed Events
	      			else {witnessEvents = allEvents - pointViewpointHandler.getEventAssociateNumber(eventVPindex);
	      				  checkWitness.setText(HG05306Msgs.Text_42 + witnessEvents + HG05306Msgs.Text_44); // Include xx Witnessed Events
	      				}
				// Reset table data
		      		objAssocData = pointViewpointHandler.getAssociateEventTable(eventVPindex);
					DefaultTableModel eventModel = (DefaultTableModel) table11.getModel();
					eventModel.setDataVector(objAssocData, eventTableHeader);
				// Reset table renderers
					table11.getColumnModel().getColumn(0).setPreferredWidth(120);
					table11.getColumnModel().getColumn(0).setMinWidth(100);
					table11.getColumnModel().getColumn(1).setPreferredWidth(120);
					table11.getColumnModel().getColumn(1).setMinWidth(70);
					table11.getColumnModel().getColumn(2).setPreferredWidth(320);
					table11.getColumnModel().getColumn(2).setMinWidth(200);
					table11.getColumnModel().getColumn(3).setPreferredWidth(80);
					table11.getColumnModel().getColumn(3).setMinWidth(50);

				} catch (Exception hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0530VE Event setWitnessState error " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
		      }
		});

		// These buttons allow the scrollpane height to be made bigger/smaller as it has no edge to resize with
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
				scroll11.getVerticalScrollBar().setValue(0);	// set slider to top
				int w2 = scroll11.getWidth();
				int h1minus = (int)Math.round(scroll11.getHeight()*.75);
				table11.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll11.revalidate();
		        correctPanelSize();
			}
		});

		// Listener to action selection of row in associate table - create personVP but do not open it
		pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int viewRow = table11.getSelectedRow();
					int selectedRowInTable = table11.convertRowIndexToModel(viewRow);
					if (selectedRowInTable < 0) return;				//exit if listener call was caused by emptying table_User
					try {
				// Find next open personVP
						int personVPindex = pointViewpointHandler.findClosedVP("5300", pointOpenProject);	//$NON-NLS-1$
						String personVPident = pointViewpointHandler.getPersonScreenID(personVPindex);
						long personPID = pointViewpointHandler.getEventAssociatePID(eventVPindex, selectedRowInTable);

				// Set PID for person from person list
						if (personVPident != null)
							pointOpenProject.pointGuiData.setTableViewPointPID(personVPident, personPID);
						else {
							userInfoInitVP(4);
					    	if (HGlobal.DEBUG && HGlobal.writeLogs)
					    		HB0711Logging.logWrite("Status: in HG0530VE valueChanged - personVPindex: "	//$NON-NLS-1$
										+ personVPindex);
						}
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0530VE Event select Event error " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		});

	// Listener for Assocs table mouse double-click
		table11.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				long personTablePID;
				int rowClicked, colClicked, selectedRowInTable, selectedRow, errorCode = 0;
				// DOUBLE-CLICK
				if (me.getClickCount() == 2 && table11.getSelectedRow() != -1) {
	           		colClicked = table11.getSelectedColumn();
	           		rowClicked = table11.getSelectedRow();
	       			personTablePID = pointViewpointHandler.getEventAssociatePID(eventVPindex, rowClicked);
	           		// DOUBLE-CLICK - if column=2, change ManagePerson to this Person
	           		if (colClicked == 2) {
		        		// Get PID of person in clicked row, column 2 and use it to change ManagePerson focus
	           			pointPersonHandler.initiateManagePerson(pointOpenProject, personTablePID, screenID);
	           		} else {
	           		// DOUBLE-CLICK other columns open Person VP for this Person
	           			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
	           			if (errorCode > 0) userInfoInitVP(errorCode);
	           		}
           		}  else

	      // RIGHT-click, open PersonVP
	            if (me.getButton() == MouseEvent.BUTTON3) {
                	selectedRow = table11.rowAtPoint(me.getPoint());
                	table11.addRowSelectionInterval(selectedRow, selectedRow);
               		colClicked = table11.getSelectedColumn();
               		selectedRowInTable = table11.convertRowIndexToModel(selectedRow);
               		personTablePID = pointViewpointHandler.getEventAssociatePID(eventVPindex, selectedRowInTable);
	       			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
	       			if (errorCode > 0) userInfoInitVP(errorCode + 3);
	            }
	        }
	    });

		return p1;
   }	// End event Assocs

// Define panel P2 for Images
    private JPanel eventImages() {
        ArrayList<ImageIcon> listImages = pointViewpointHandler.getEventImageList(eventVPindex);
        // If no images, show not present image
        if (listImages == null || listImages.size() == 0) {
        	JPanel p2 = new JPanel(new GridLayout(1, 1, 1, 1));
        	ImageIcon scaledExhibit = new ImageIcon(getClass().getResource("/hre/images/notpresent-event-100.png"));	//$NON-NLS-1$
        	p2.add(new JLabel(scaledExhibit));
        	return p2;
        }

        // If images present, put in 2-column, x row grid, scaled to fit the setSize
        JPanel p2 = new JPanel(new GridLayout(0, 2, 10, 10));
        JLabel labels[] = new JLabel[listImages.size()];
        p2.setSize(100,200);
        for (int i = 0; i < listImages.size(); i++) {
        	ImageIcon exhibitImage = listImages.get(i);
            if (exhibitImage != null) {
            // Rescale exhibitImage to a new max. height/width to match p2
            	//HBMediaHandler pointMediaHandler = (HBMediaHandler)HG0401HREMain.pointBusinessLayer[10];
            	HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
        		ImageIcon scaledExhibit = pointMediaHandler.scaleImage(90, 90, exhibitImage);
             	labels[i] = new JLabel(scaledExhibit);
            }
            p2.add(labels[i]);
        }
        return p2;
    }	// End eventImages

// Define panel P3 for Flags
    private JPanel eventFlags() {
    	JPanel p3 = new JPanel(new GridLayout(1, 1, 1, 1));
        JTextArea text3 = new JTextArea(3, 10);
        text3.setLineWrap(true);
        text3.setText("Flags not yet implemented");   //$NON-NLS-1$
        p3.add(new JScrollPane(text3));
        return p3;
    }	// End eventFlags

// Define panel P4 for Notepads
    private JPanel eventNotes() {
    	JPanel p4 = new JPanel(new GridLayout(1, 1, 1, 1));
        JTextArea text4 = new JTextArea(3, 10);
        text4.setLineWrap(true);
        ArrayList<String> exhibitTextList = pointViewpointHandler.gettextImageList(eventVPindex);
        if (exhibitTextList.size() > 0)
        	for (int i = 0; i < exhibitTextList.size(); i++)
        	text4.setText(exhibitTextList.get(i) + "\n");	//$NON-NLS-1$
        else text4.setText("Notepads not yet implemented"); //$NON-NLS-1$
        p4.add(new JScrollPane(text4));
        return p4;
    }	// End eventNotes

/**
 * makeEventPanel - assembles the Action & Data Panels together into the total EventPanel
 * @return	the assembled eventPanel (a JSplitPane with 2 MigLayouts)
 */
    private JSplitPane makeEventPanel() {
    	// Setup the left Splitpane from dataPanels p0, p1
        JPanel eveLeft = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // First, add in dataPanel p0 with no actionPanel
        eveLeft.add(dataPanels[0], "cell 0 0, growx"); //$NON-NLS-1$
        // Then add panel p1 action & data panel
        eveLeft.add(actPanels[1], "cell 0 1, growx"); //$NON-NLS-1$
        eveLeft.add(dataPanels[1], "cell 0 2, growx"); //$NON-NLS-1$
        dataPanels[1].setVisible(false);	// set dataPanel 1 hidden initially

        // Setup the right Splitpane from padding panel p00 and dataPanels p2-4
        JPanel eveRight = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // Then add panels 2 - 4 action & data panels
       	eveRight.add(actPanels[2], "cell 0 0, growx"); //$NON-NLS-1$
    	eveRight.add(dataPanels[2], "cell 0 1, growx"); //$NON-NLS-1$
      	eveRight.add(actPanels[3], "cell 0 2, growx"); //$NON-NLS-1$
    	eveRight.add(dataPanels[3], "cell 0 3, growx"); //$NON-NLS-1$
      	eveRight.add(actPanels[4], "cell 0 4, growx"); //$NON-NLS-1$
    	eveRight.add(dataPanels[4], "cell 0 5, growx"); //$NON-NLS-1$
        for(int j = 2; j < actPanels.length; j++) {
            dataPanels[j].setVisible(false);	// set dataPanels 2 - 4 hidden initially
        	}

		UIManager.put("SplitPane.centerOneTouchButtons", false);	// ensure buttons are at divider top, not centre //$NON-NLS-1$
    	eventPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eveLeft, eveRight);
       	eventPanel.setOneTouchExpandable(true);
       	eventPanel.setDividerSize(10);
       	eventPanel.setResizeWeight(0.9);		// Screen growth goes mostly to left panel
      	eveLeft.setMinimumSize(new Dimension(100, 0));			// initial size about 680, 200
      	eveRight.setMinimumSize(new Dimension(100, 0)); 		// initial size about 250, 200
      	eventPanel.setContinuousLayout(true);

        return eventPanel;
    } // End makeEventPanel

/**
 * This handles the retention of the current Splitpane screen widths after
 * opening an ActionPanel or use of +/- buttons, when a pack() is required
 */
    private void correctPanelSize() {
       	// if frame was maximised, make sure we retain that height
    	if (eventFrame.isMaximum()) maxHeight = eventFrame.getHeight();
    	else maxHeight = HG0401HREMain.mainPane.getHeight() - 60;
    	// Get Frame width and splitpane divider location
    	widthFrame = eventFrame.getWidth();
    	dividerLocation =eventPanel.getDividerLocation();
        eventPanel.setMaximumSize(new Dimension(5000, maxHeight));  // allow huge width but control height
    	// Reload the scrollPanel
    	eventFrame.revalidate();
		contents.add(eventPanel);
		// Do pack only if frame not maximised
        if (!eventFrame.isMaximum()) eventFrame.pack();
        // Reset the divider (which creeps right unless you do this)
        eventPanel.setDividerLocation(dividerLocation);
        // Now get the frame height
        heightFrame = eventFrame.getHeight();
        // Restore the Frame to original width with this height (otherwise right panels grow as well!)
        this.setSize(widthFrame, heightFrame);
    }	// End correctPanelSize

/**
 * Handle Mouse events on the Action Panels
 */
    @Override
	public void mousePressed(MouseEvent e) {
        ActionEPanel ap = (ActionEPanel) e.getSource();
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
    private void togglePanelVisible(ActionEPanel ap) {
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
    private int getPanelIndex(ActionEPanel ap) {
        for(int j = 0; j < actPanels.length; j++)
            if(ap == actPanels[j])
                return j;
        return -1;
    }	// End getPanelIndex

}	// End HG0530ViewEvent class

/**
 * Class to handle build of the ActionPanels and their pointer controls
 */
class ActionEPanel extends JPanel  {
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
    public ActionEPanel(String text, MouseListener ml) {
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
    }	// End ActionEPanel constructor

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
        int h = getPreferredSize().height;
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

}	// End ActionEPanel class
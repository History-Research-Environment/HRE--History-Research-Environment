package hre.gui;
/****************************************************************************************
 * HRE ViewPeople - Specifications 05.30 GUI_Viewpoint
 * v0.00.0022 2020-05-10 Split off Location VP to separate code and totally
 *                         rewritten to implement collapsable panel design (D Ferguson)
 *            2020-08-02 always put Project name in Frame Title (D Ferguson)
 * v0.01.0023 2020-09-01 Implemented read/write reminder to database (N Tolleshaug)
 * 			  2020-09-04 all sub-panel build code merged back into here (D Ferguson)
 * 			  2020-09-08 table headings modified to remove 'Other' (D Ferguson)
 * 			  2020-09-24 Implemented PersonViewpoint reopen of screen (N. Tolleshaug)
 * 			  2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * 			  2020-10-03 Errors corrected - position + size setting (N. Tolleshaug)
 * v0.01.0025 2020-10-27 Converted to 2-column layout and layout fixes (D Ferguson)
 * 			  2020-11-16 Framework for processing data PersonView (N. Tolleshaug)
 * 			  2020-12-05 Add scrolling into Event sub-panel; remove GridBag (D Ferguson)
 *  		  2020-12-05 Added sort of event for Age (N. Tolleshaug)
 *  		  2020-12-08 Added numbers in Event and Name titles (N. Tolleshaug)
 * 			  2020-12-15 Fix initial size and panel resize issues (D Ferguson)
 * 			  2020-12-21 Use head silhouette default for primary image (D Ferguson)
 * 			  2021-01-20 Reset person ID and multiple PersonViewPoint (N. Tolleshaug)
 * v0.01.0026 2021-09-14 Make screen scroll if height exceeds mainPane (D Ferguson)
 * v0.01.0027 2021-12-02 Added timing console msgs for start/end of VP (D Ferguson)
 * 			  2021-12-07 Aligned Action bar colors with Theme colors (D Ferguson)
 * 			  2022-01-22 Importing main picture from T676 (N Tolleshaug)
 * 			  2022-02-04 Importing all images to Image panel (N Tolleshaug, D Ferguson)
 * 			  2022-02-28 Converted to NLS (D Ferguson)  (updated 2022-04-22)
 * 			  2022-03-04 Added +/- control to Associates sub-panel (D Ferguson)
 * 			  2022-03-07 Modified to use HGloDataMsgs (D Ferguson)
 * v0.01.0028 2023-01-08 Translated table headers collected from T204 (N. Tolleshaug)
 * 			  2023-01-15 Translated texts collected from T204 (N. Tolleshaug)
 * v0.03.0030 2023-06-11 Add flag data to flag panel (D Ferguson)
 * 			  2023-06-29 Activated witness on/off for event table (N. Tolleshaug)
 * 			  2023-07-15 Activated translated table header flags (N. Tolleshaug)
 * 			  2023-09-24 If frame is full-screen don't do special size control code (D Ferguson)
 * v0.03.0031 2024-11-04 Ensure all data non-editable (D Ferguson)
 * 			  2024-11-04 Fix +/- buttons failing if screen maximised (D Ferguson)
 * 			  2024-12-01 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * 			  2024-12-12 Make Names Alt.Type column biggger (D Ferguson)
 ***************************************************************************************/

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
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBMediaHandler;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBViewPointHandler;
import hre.nls.HG05300Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Viewpoint for People with collapsing panels for People Viewpoint structure
 * @author originally bbrita on Sun Java forums c.2006; modified extensively since
 * @author for this version D Ferguson
 * @version v0.03.0031
 * @since 2020-05-10
 */

public class HG0530ViewPeople extends HG0451SuperIntFrame implements MouseListener {
	private static final long serialVersionUID = 1L;

	boolean printNr = HGlobal.DEBUG;
	public String screenID = "53000"; //$NON-NLS-1$
	public String tableScreenID = "53000"; //$NON-NLS-1$

	private String noImageText;

	private String className;
	private int personVPindex = 0;
	int maxHeight, widthFrame, heightFrame, dividerLocation;	// see correctPanelSize
	JInternalFrame peopleFrame = this;
	private JPanel contents;
	JScrollPane scrollPanel;
	JSplitPane personPanel;
	ActionPanel[] actPanels;
    JPanel[] dataPanels;
    JTextField text01;
    String selfLifespan;
    private String vpProject; 		// to save Project name of this Viewpoint

	String[] eventTableHeader;
	Object[][] objEventData;
    Object[][] objAllFlagData;
	Object[][] objReqFlagData;
	int activeFlags = 0;
	int allEvents = 0;
	int witnessEvents = 0;

    HBViewPointHandler pointViewpointHandler = null;
    HBProjectOpenData pointOpenProject;
	protected HBPersonHandler pointPersonHandler;
	private final static int maxPersonVPIs = HGlobal.maxPersonVPIs;
	private final static int maxEventVPIs = HGlobal.maxEventVPIs;

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

/**
 * Build the People Viewpoint
 */
    public HG0530ViewPeople(HBViewPointHandler pointViewpointHandler
    						, HBProjectOpenData pointOpenProject
    						, int personVPindex
    						, String screenID) {
    	super(pointViewpointHandler," Person ViewPoint",true,true,true,true); //$NON-NLS-1$
    	this.pointViewpointHandler = pointViewpointHandler;
    	this.pointOpenProject = pointOpenProject;
    	this.personVPindex = personVPindex;
    	this.screenID = screenID;
		pointPersonHandler = pointOpenProject.getPersonHandler();
    	className = getClass().getSimpleName();
    	this.setResizable(true);

    // Setup references for HG0451
		windowID = screenID;
		helpName = "personvp"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0530 ViewPeople"); //$NON-NLS-1$
		if (HGlobal.TIME) HGlobalCode.timeReport("start of HG0530ViewPers on thread "+Thread.currentThread().getName());  //$NON-NLS-1$

		// Save the selected Project in a local variable (as may change)
		vpProject = pointOpenProject.getProjectName();

		if (HGlobal.DEBUG)
			System.out.println("Translated texts:  0-" 		//$NON-NLS-1$
					+ Arrays.toString(pointPersonHandler.setTranslatedData(screenID, "0", false)));	//$NON-NLS-1$

		// Collect static text from T204
		noImageText = " " + pointPersonHandler.setTranslatedData(tableScreenID, "0", false)[0];	//$NON-NLS-1$	//$NON-NLS-2$

		// Define Content pane
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[grow, fill]", "[grow, fill]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Build the ActionPanels, DataPanels into PeoplePanel
    	makeActionPanels();
        makeDataPanels();

        // Setup scrolling Panel containing peoplePanel
		scrollPanel = new JScrollPane(makePersonPanel());
		scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        maxHeight = HG0401HREMain.mainPane.getHeight();
        scrollPanel.setMaximumSize(new Dimension(5000, maxHeight - 60));  // allow huge width but control height
		contents.add(scrollPanel);
		pack();

		// Store initial frame height
        frameHeight = peopleFrame.getHeight();

        // Add the Viewpoint to the Main menu pane and make it visible
     	HG0401HREMain.mainPane.add(peopleFrame);

     	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: displaying People Viewpoint from HG0530 ViewPeople");  //$NON-NLS-1$
		if (HGlobal.TIME) HGlobalCode.timeReport("end of HG0530ViewPers on thread "+Thread.currentThread().getName());	 //$NON-NLS-1$

    	setVisible(true);

	// Listener for FrameClosing Event
		peopleFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
			public void internalFrameClosing(InternalFrameEvent e) {
            	printNr = HGlobal.DEBUG;
			// close reminder display
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
					if (printNr)
						System.out.println("Store PersonVP() nr: "  //$NON-NLS-1$
								+ pointOpenProject.getPersonVP() + " / " + screenID); //$NON-NLS-1$
			// Set class name in GUI config data
					pointOpenProject.setClassName(screenID,className);
			// Mark the screen as closed in T302 and remove screen
					pointOpenProject.closeStatusScreen(screenID);
			// Count down the Location view screens
					if (pointOpenProject.getPersonVP() > 0)
									pointOpenProject.countDownPersonVP();
			// Close window
				    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: closing People Viewpoint in HG0530 ViewPeople");	 //$NON-NLS-1$
				    dispose();
            }
           });

    }	// End HG0530ViewPeople constructor

/**
 * userInfoInitVP(int errorCode)
 * @param errorCode
 */
	private void userInfoInitVP(int errorCode) {
		if (errorCode == 1) {
			JOptionPane.showMessageDialog(contents,  HG05300Msgs.Text_88 		// Too many Event Viewpoints
												+ HG05300Msgs.Text_81 + maxEventVPIs,	 //Maximum allowed is
					HG05300Msgs.Text_89, JOptionPane.INFORMATION_MESSAGE);	// Event Viewpoint Creation
			return;
		}
		if (errorCode == 2) {
			JOptionPane.showMessageDialog(contents,  HG05300Msgs.Text_83,		// Create Person ViewPoint error
					HG05300Msgs.Text_82, JOptionPane.ERROR_MESSAGE);		// Person Viewpoint Creation
			return;
		}
		if (errorCode == 3) {
			JOptionPane.showMessageDialog(contents,  HG05300Msgs.Text_84			// Image Thumbnail Error
												+ HG05300Msgs.Text_85		// From TMG Exhibit Log, perform a
												+ HG05300Msgs.Text_86		// 'Refresh all thumbnails' command,
												+ HG05300Msgs.Text_87,		// then re-import the TMG file.
												HG05300Msgs.Text_82, JOptionPane.ERROR_MESSAGE);	// Person Viewpoint Creation
			return;
		}
		if (errorCode == 4) {
			JOptionPane.showMessageDialog(contents,  HG05300Msgs.Text_80						// Too many Person Viewpoints
												+ HG05300Msgs.Text_81 + maxPersonVPIs,	// Maximum allowed is
												HG05300Msgs.Text_82, JOptionPane.INFORMATION_MESSAGE);	// Person Viewpoint Creation
			return;
		}
		System.out.println(" HG0530ViewPeople - Unidentified errorcode: " + errorCode);	//$NON-NLS-1$
	}	// End userInfoInitVP

/**
 * Create the Action panels that control the dataPanels, with item counts in the panel names
 */
    private void makeActionPanels() {
        // actionPanel titles - first and sixth not used as the corresponding dataPanels are always open
    	String eventsTitle = HG05300Msgs.Text_13 + " (" 									  //$NON-NLS-1$
    			+ pointViewpointHandler.getNumberOfEvents(personVPindex) + ")"; // Events (#) //$NON-NLS-1$
    	String namesTitle = HG05300Msgs.Text_16 + " (" 										  //$NON-NLS-1$
    			+ pointViewpointHandler.getNumberOfNames(personVPindex) + ")";   // Names (#) //$NON-NLS-1$
    	String imagesTitle = HG05300Msgs.Text_19 + " (" 										    //$NON-NLS-1$
    			+ pointViewpointHandler.getPersonNumberOfImages(personVPindex) + ")"; // Images (#) //$NON-NLS-1$
    	String associateTitle = HG05300Msgs.Text_24 + " (" 										    //$NON-NLS-1$
    			+ pointViewpointHandler.getNumberOfAsociates(personVPindex) + ")"; // Associate (#) //$NON-NLS-1$
    	// Load flag table
    	objAllFlagData = pointViewpointHandler.getFlagData(personVPindex);
		// and sort on GUI-sequence (column 2)
		Arrays.sort(objAllFlagData, (o1, o2) -> Integer.compare((Integer) o1[2], (Integer) o2[2]));
		// Now count the number of Active Flags to set size of objReqFlagData and use in panel item count
		for (int i = 0; i < objAllFlagData.length; i++) {
			if ((boolean) (objAllFlagData[i][1])) activeFlags++;
		}
    	// Then show Flag panel title
    	String flagsTitle = HG05300Msgs.Text_26
    			+ " (" + activeFlags + ")";   	// Flags (#)      //$NON-NLS-1$ //$NON-NLS-2$
    	String notepadTitle = HG05300Msgs.Text_27
    			+ " (" + "0" + ")";   // Notepads (0)   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String[] titles = {"not used", eventsTitle, HG05300Msgs.Text_23, associateTitle, namesTitle, // dummy, Events, Relatives, Associates, Names  //$NON-NLS-1$
    			 		   "not used", imagesTitle, flagsTitle, notepadTitle}; 			// Names, dummy, Images, Flags, Notepad)  //$NON-NLS-1$
        actPanels = new ActionPanel[titles.length];
        for(int j = 0; j < actPanels.length; j++)
            actPanels[j] = new ActionPanel(titles[j], this);
    }	// End makeActionPanels

/**
 * Create dataPanels for each of the Person Viewpoint sections
 */
    private void makeDataPanels() {
    // Build panels and add into the dataPanels array
        JPanel p0 = peopleInitial();
        JPanel p1 = peopleEvents();
        JPanel p2 = peopleRelative();
        JPanel p3 = peopleAssocs();
        JPanel p4 = peopleNames();
        JPanel p5 = peoplePriImage();
        JPanel p6 = peopleImages();
        JPanel p7 = peopleFlags();
        JPanel p8 = peopleNotes();
        dataPanels = new JPanel[] {p0, p1, p2, p3, p4, p5, p6, p7, p8};

    }	// End makeDataPanels

// Define panel P0 for the base Person data - this panel always showing
    private JPanel peopleInitial() {
        JPanel p0 = new JPanel(new MigLayout("insets 5", "[]5[grow][]", "[]5[]10[grow]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JLabel label01 = new JLabel(HG05300Msgs.Text_31);		// Identity
      	p0.add(label01, "cell 0 0, alignx right");  //$NON-NLS-1$
      	// Identify Person in Viewpoint Identity field and frame Title
        text01 = new JTextField(pointViewpointHandler.getPersonName(personVPindex));
        text01.setEditable(false);
        peopleFrame.setTitle(text01.getText() + HG05300Msgs.Text_33+(personVPindex+1) + HG05300Msgs.Text_34+vpProject); // in Person ViewPoint # , of Project
        text01.setPreferredSize(new Dimension(610, 20));
     	p0.add(text01, "cell 1 0, growx");    //$NON-NLS-1$
     	p0.add(btn_Remindericon, "cell 2 0");   //$NON-NLS-1$
     	p0.add(btn_Helpicon, "cell 2 0");    //$NON-NLS-1$

 // Set up sex in header
        JLabel label02 = new JLabel(HG05300Msgs.Text_35);		// Sex
       	p0.add(label02, "cell 0 1, alignx right");  //$NON-NLS-1$
       	String selfSex = pointViewpointHandler.getPersonSex(personVPindex);
        JTextField text02 = new JTextField("  " + selfSex);	//$NON-NLS-1$
        text02.setEditable(false);
        text02.setColumns(8);
        p0.add(text02, "cell 1 1");    //$NON-NLS-1$
        String reference = pointViewpointHandler.getPersonReference(personVPindex);
        JLabel label03 = new JLabel(HG05300Msgs.Text_36);		// Reference
       	p0.add(label03, "cell 1 1, gapx 20");  //$NON-NLS-1$

        JTextField text03 = new JTextField(" " + reference);	//$NON-NLS-1$
        text03.setEditable(false);
        text03.setColumns(25);
        p0.add(text03, "cell 1 1, gapx 10");    //$NON-NLS-1$

		if (HGlobal.DEBUG)
			System.out.println("Person VP Initial Person:  1-"		//$NON-NLS-1$
					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "1", false)));	//$NON-NLS-1$

		JTable table01 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public Dimension getPreferredScrollableViewportSize() {
		 								    return new Dimension(super.getPreferredSize().width,
		 								    		super.getRowCount() * super.getRowHeight());
		 								  }
		 								public boolean isCellEditable(int row, int column) {return false;}};
		table01.setModel(new DefaultTableModel(
									pointViewpointHandler.getParentData(personVPindex),
									pointViewpointHandler.setTranslatedData(tableScreenID, "1", false)	//$NON-NLS-1$	// get Role, Name, Surety
									));
		table01.getColumnModel().getColumn(0).setMinWidth(120);
		table01.getColumnModel().getColumn(0).setPreferredWidth(120);
		table01.getColumnModel().getColumn(1).setMinWidth(400);
		table01.getColumnModel().getColumn(1).setPreferredWidth(400);
		table01.getColumnModel().getColumn(2).setMinWidth(70);
		table01.getColumnModel().getColumn(2).setPreferredWidth(120);
		JTableHeader tHd01 = table01.getTableHeader();
		tHd01.setOpaque(false);
		table01.setFillsViewportHeight(true);
		// Table may need to expand for Father/Mother-adopt or -foster, so could be 2+ rows
		int nProws = table01.getRowCount();
		// Setup and show table
		JScrollPane scroll01 = new JScrollPane(table01);
		table01.setPreferredScrollableViewportSize(new Dimension(640, nProws*table01.getRowHeight()));

		p0.add(scroll01, "cell 0 2 3 1, grow");  //$NON-NLS-1$

		return p0;

    }	// End peopleInitial

/*
 * JPanel peopleEvents - panel P1
 */
    private JPanel peopleEvents() {
		JPanel p1 = new JPanel(new MigLayout("insets 5", "[]10[]10[grow]10[]", "[]5[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JCheckBox checkWitness = new JCheckBox(HG05300Msgs.Text_45); // Includes Witnessed Events
		checkWitness.setSelected(true);
		checkWitness.setHorizontalTextPosition(SwingConstants.TRAILING);
		checkWitness.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton button11 = new JButton("+"); //$NON-NLS-1$
		button11.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button11.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		JButton button12 = new JButton("-"); //$NON-NLS-1$
		button12.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button12.setFont(new Font("Arial", Font.BOLD, 15));	//$NON-NLS-1$

		if (HGlobal.DEBUG)
			System.out.println("Person VP Event Table:  2- " 		//$NON-NLS-1$
				+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "2", false)));		//$NON-NLS-1$

		JTable table11 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};
		 // Get event data, table header and event count
		objEventData = pointViewpointHandler.getEventData(personVPindex);
		eventTableHeader = pointViewpointHandler.setTranslatedData(tableScreenID, "2", false); //$NON-NLS-1$ // Event Tag, Role, Date, Location, Age
		allEvents = pointViewpointHandler.getNumberOfEvents(personVPindex);
		// Build table
		table11.setModel(new DefaultTableModel(
				objEventData,
				eventTableHeader));
		// Set a centred cell renderer for Age
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table11.getColumnModel().getColumn(0).setPreferredWidth(100);
		table11.getColumnModel().getColumn(0).setMinWidth(70);
		table11.getColumnModel().getColumn(1).setPreferredWidth(120);
		table11.getColumnModel().getColumn(1).setMinWidth(70);
		table11.getColumnModel().getColumn(2).setPreferredWidth(130);
		table11.getColumnModel().getColumn(2).setMinWidth(70);
		table11.getColumnModel().getColumn(3).setPreferredWidth(220);
		table11.getColumnModel().getColumn(3).setMinWidth(150);
		table11.getColumnModel().getColumn(4).setPreferredWidth(50);
		table11.getColumnModel().getColumn(4).setMaxWidth(50);
		table11.getColumnModel().getColumn(4).setMinWidth(50);
		table11.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		// Set row selection action and header
		ListSelectionModel pcellSelectionModel = table11.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JTableHeader tHd11 = table11.getTableHeader();
		tHd11.setOpaque(false);
		// Set Viewport size
		table11.setFillsViewportHeight(true);
		int nErows = table11.getRowCount();
		if (nErows == 0) nErows = 1;
		if (nErows > 5) nErows = 5;					// show 1-5 rows as default
		table11.setPreferredScrollableViewportSize(new Dimension(640, nErows*table11.getRowHeight()));
		JScrollPane scroll11 = new JScrollPane(table11);

		p1.add(checkWitness, "cell 0 0");	//$NON-NLS-1$
		p1.add(button11, "cell 4 0"); 		//$NON-NLS-1$
		p1.add(button12, "cell 4 0"); 		//$NON-NLS-1$
		p1.add(scroll11, "cell 0 1 4, span, grow"); //$NON-NLS-1$

		// Listener for 'Show witnessed events' CheckBox
		checkWitness.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		        boolean state;
		        if (checkWitness.isSelected()) state = true;
		        	else state = false;
		      	try {
					// Set up witness state prompt and recreate table
		      		pointViewpointHandler.setWitnessState(personVPindex, state);
		      		if (state) checkWitness.setText(HG05300Msgs.Text_45); // Includes Witnessed Events
	      			else {witnessEvents = allEvents - pointViewpointHandler.getNumberOfEvents(personVPindex);
	      				  checkWitness.setText(HG05300Msgs.Text_46 + witnessEvents + HG05300Msgs.Text_47); // Include xx Witnessed Events
	      				}
					// Recreatet table data
		      		objEventData = pointViewpointHandler.getEventData(personVPindex);
					DefaultTableModel eventModel = (DefaultTableModel) table11.getModel();
					eventModel.setDataVector(objEventData, eventTableHeader);

					// Reset table renderers - Set a centred cell renderer for Age
					DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
					centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
					table11.getColumnModel().getColumn(0).setPreferredWidth(100);
					table11.getColumnModel().getColumn(0).setMinWidth(70);
					table11.getColumnModel().getColumn(1).setPreferredWidth(120);
					table11.getColumnModel().getColumn(1).setMinWidth(70);
					table11.getColumnModel().getColumn(2).setPreferredWidth(130);
					table11.getColumnModel().getColumn(2).setMinWidth(70);
					table11.getColumnModel().getColumn(3).setPreferredWidth(220);
					table11.getColumnModel().getColumn(3).setMinWidth(150);
					table11.getColumnModel().getColumn(4).setPreferredWidth(50);
					table11.getColumnModel().getColumn(4).setMaxWidth(50);
					table11.getColumnModel().getColumn(4).setMinWidth(50);
					table11.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

				} catch (Exception hbe) {
					System.out.println(" HBViewPointHandler - setWitnessState error: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
		      }
		});

		// Listener to action selection of row in event table - create an Event VP
		pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int viewRow = table11.getSelectedRow();
					int selectedRowInTable = table11.convertRowIndexToModel(viewRow);
					if (selectedRowInTable < 0) return;				//exit if listener call was caused by emptying table_User
					try {
				// Find next open eventVP
						int eventVPindex = pointViewpointHandler.findClosedVP("5306", pointOpenProject);	//$NON-NLS-1$
						String eventVPident = pointViewpointHandler.getEventScreenID(eventVPindex);
						long eventPID = pointViewpointHandler.getPersonEventPID(personVPindex, selectedRowInTable);

				// Set PID for event from event list
						if (eventVPident != null)
							pointOpenProject.pointGuiData.setTableViewPointPID(eventVPident, eventPID);
						else {
							userInfoInitVP(1);
							if (HGlobal.DEBUG)
								System.out.println(" HG0530ViewPeople valueChanged - eventVPindex: "	//$NON-NLS-1$
										+ eventVPindex);
						}
					} catch (HBException hbe) {
						System.out.println(" HG0530ViewPeople mark event: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					}
				}
			}
		});

    	// Listener for mouse double click or right-click on a table11 (Events) row
		table11.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				int rowClicked, colClicked, selectedRowInTable, selectedRow, errorCode = 0;
				long eventTablePID;
				if (me.getClickCount() == 2 && table11.getSelectedRow() != -1) {
	           		rowClicked = table11.getSelectedRow();
	           		colClicked = table11.getSelectedColumn();
	           		selectedRowInTable = table11.convertRowIndexToModel(rowClicked);
	           		eventTablePID = pointViewpointHandler.getPersonEventPID(personVPindex, selectedRowInTable);
	           		// Double-click, open EventVP
	           		if (colClicked == 2) {
	           			if (HGlobal.DEBUG)
	           				System.out.println(" HG0530ViewLocation DClick Vpindex: " + personVPindex + " Selected Row: " //$NON-NLS-1$ //$NON-NLS-2$
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
               		eventTablePID = pointViewpointHandler.getPersonEventPID(personVPindex, selectedRowInTable);
           			errorCode = pointViewpointHandler.initiateEventVP(pointOpenProject, eventTablePID);
           			if (errorCode > 0) userInfoInitVP(errorCode);
                }
	        }
	    });

		// These buttons allow the scrollpane height to be made bigger/smaller as it has no edge to resize with
		button11.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int max = table11.getRowHeight()*table11.getRowCount();
				int w1 = scroll11.getWidth();
				int h1plus = (int)Math.round(scroll11.getHeight()*1.3);
				if (h1plus > max) h1plus = max;					// stop increasing past table size
				table11.setPreferredScrollableViewportSize(new Dimension(w1, h1plus));
				scroll11.revalidate();
	            correctPanelSize();
 	            scroll11.getVerticalScrollBar().setValue(table11.getRowCount());	// move slider down
			}
		});
		button12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int w2 = scroll11.getWidth();
				int h1minus = (int)Math.round(scroll11.getHeight()*.75);
				table11.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll11.revalidate();
		        correctPanelSize();
			}
		});

		return p1;

    }	// End peopleEvents

/*
 * JPanel peopleRelative  - panel P2
 */
    private JPanel peopleRelative() {
    	JPanel p2 = new JPanel(new MigLayout("insets 5", "[]10[grow]", "[]5[]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	//setup contents of first row of the panel
    	JLabel showGen = new JLabel(HG05300Msgs.Text_68);					// Click to show relevant generation:
    	JToggleButton buttonm2 = new JToggleButton(HG05300Msgs.Text_69);	// Gen(-2)
 		JToggleButton buttonm1 = new JToggleButton(HG05300Msgs.Text_70);	// Gen(-1)
    	JToggleButton button00 = new JToggleButton(HG05300Msgs.Text_71);	// Gen(0)
 		JToggleButton buttonp1 = new JToggleButton(HG05300Msgs.Text_72);	// Gen(+1)
 		JToggleButton buttonp2 = new JToggleButton(HG05300Msgs.Text_73);	// Gen(+2)
 		// Layout the first row of the panel
 		p2.add(showGen, "cell 0 0"); //$NON-NLS-1$
 		p2.add(buttonm2, "cell 1 0"); //$NON-NLS-1$
		p2.add(buttonm1, "cell 1 0"); //$NON-NLS-1$
		p2.add(button00, "cell 1 0"); //$NON-NLS-1$
		p2.add(buttonp1, "cell 1 0"); //$NON-NLS-1$
		p2.add(buttonp2, "cell 1 0"); //$NON-NLS-1$
/**
 * Create sub-panels for each of the generation sections from -2 to +2 generations
 */
     // Define panel for the Generation -2 data
        JPanel genm2 = new JPanel(new MigLayout("insets 0, gap 5", "[grow]", "[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //Define label and table
        JLabel labelm2 = new JLabel("(-2)");	//$NON-NLS-1$
 		JTable tablem2 = new JTable() { private static final long serialVersionUID = 1L;
 		 								@Override
 		 								public boolean isCellEditable(int row, int column) {return false;}};
 		if (HGlobal.DEBUG)
 		 	System.out.println("Person VP relatives -2 Table:  4- "	//$NON-NLS-1$
				+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)));	//$NON-NLS-1$

 		tablem2.setModel(new DefaultTableModel(
 				pointViewpointHandler.getRelativesGP2Data(personVPindex),
 				pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)	//$NON-NLS-1$ 	// get Name, Relationship, Sex, Lifespan
 			));
 		// Set a centered cell renderer for the Sex/Lifespan columns
 		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		tablem2.getColumnModel().getColumn(0).setPreferredWidth(220);
		tablem2.getColumnModel().getColumn(0).setMinWidth(170);
		tablem2.getColumnModel().getColumn(1).setPreferredWidth(90);
		tablem2.getColumnModel().getColumn(1).setMinWidth(80);
		tablem2.getColumnModel().getColumn(2).setPreferredWidth(40);
		tablem2.getColumnModel().getColumn(2).setMaxWidth(40);
		tablem2.getColumnModel().getColumn(2).setMinWidth(20);
		tablem2.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tablem2.getColumnModel().getColumn(3).setPreferredWidth(90);
		tablem2.getColumnModel().getColumn(3).setMaxWidth(90);
		tablem2.getColumnModel().getColumn(3).setMinWidth(40);
		tablem2.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		JTableHeader tHdm2 = tablem2.getTableHeader();
		tHdm2.setOpaque(false);
		tablem2.setFillsViewportHeight(true);
		int m2Rows = tablem2.getRowCount();
		if (m2Rows == 0) m2Rows = 1;
		tablem2.setPreferredScrollableViewportSize(new Dimension(440, m2Rows*tablem2.getRowHeight()));
		JScrollPane scrollm2 = new JScrollPane(tablem2);

     	genm2.add(labelm2, "cell 0 1"); //$NON-NLS-1$
 		genm2.add(scrollm2, "cell 0 1, span, growx, growy");  //$NON-NLS-1$

     // Define panel for the Generation -1 data
        JPanel genm1 = new JPanel(new MigLayout("insets 0, gap 5", "[grow]", "[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // Define label and table
        JLabel labelm1 = new JLabel("(-1)");	//$NON-NLS-1$
 		JTable tablem1 = new JTable() { private static final long serialVersionUID = 1L;
 		 								@Override
 		 								public boolean isCellEditable(int row, int column) {return false;}};

 		if (HGlobal.DEBUG)
 			System.out.println("Person VP relatives -1 Table:  4- " 		//$NON-NLS-1$
 					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "4", false))); 	//$NON-NLS-1$

 		tablem1.setModel(new DefaultTableModel(
    			pointViewpointHandler.getRelativesGP1Data(personVPindex),
    			pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)	//$NON-NLS-1$	// get Name, Relationship, Sex, Lifespan
 			));
		tablem1.getColumnModel().getColumn(0).setPreferredWidth(220);
		tablem1.getColumnModel().getColumn(0).setMinWidth(170);
		tablem1.getColumnModel().getColumn(1).setPreferredWidth(90);
		tablem1.getColumnModel().getColumn(1).setMinWidth(80);
		tablem1.getColumnModel().getColumn(2).setPreferredWidth(40);
		tablem1.getColumnModel().getColumn(2).setMaxWidth(40);
		tablem1.getColumnModel().getColumn(2).setMinWidth(20);
		tablem1.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tablem1.getColumnModel().getColumn(3).setPreferredWidth(90);
		tablem1.getColumnModel().getColumn(3).setMaxWidth(90);
		tablem1.getColumnModel().getColumn(3).setMinWidth(40);
		tablem1.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		JTableHeader tHdm1 = tablem1.getTableHeader();
		tHdm1.setOpaque(false);
		tablem1.setFillsViewportHeight(true);
		int m1Rows = tablem1.getRowCount();
		if (m1Rows == 0) m1Rows = 1;
		tablem1.setPreferredScrollableViewportSize(new Dimension(440, m1Rows*tablem1.getRowHeight()));
		JScrollPane scrollm1 = new JScrollPane(tablem1);

     	genm1.add(labelm1, "cell 0 1"); //$NON-NLS-1$
 		genm1.add(scrollm1, "cell 0 1, span, growx, growy");   //$NON-NLS-1$

     // Define panel for the Generation 0 data
        JPanel gen0 = new JPanel(new MigLayout("insets 0, gap 5", "[grow]", "[grow]"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //Define label and table
        JLabel label00 = new JLabel("(0) "); //$NON-NLS-1$
        JTable table00 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};

 		if (HGlobal.DEBUG)
 			System.out.println("Person VP relatives 0 Table:  3- " 	//$NON-NLS-1$
 					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "3", false))); 	//$NON-NLS-1$

		table00.setModel(new DefaultTableModel(
				pointViewpointHandler.getRelativesGS0Data(personVPindex),
				pointViewpointHandler.setTranslatedData(tableScreenID, "3", false)	//$NON-NLS-1$	// get Name, Relationship, Sex, Lifespan
			));
		table00.getColumnModel().getColumn(0).setPreferredWidth(220);
		table00.getColumnModel().getColumn(0).setMinWidth(170);
		table00.getColumnModel().getColumn(1).setPreferredWidth(90);
		table00.getColumnModel().getColumn(1).setMinWidth(80);
		table00.getColumnModel().getColumn(2).setPreferredWidth(40);
		table00.getColumnModel().getColumn(2).setMaxWidth(40);
		table00.getColumnModel().getColumn(2).setMinWidth(20);
		table00.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		table00.getColumnModel().getColumn(3).setPreferredWidth(90);
		table00.getColumnModel().getColumn(3).setMaxWidth(90);
		table00.getColumnModel().getColumn(3).setMinWidth(40);
		table00.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		JTableHeader tHd00 = table00.getTableHeader();
		tHd00.setOpaque(false);
		table00.setFillsViewportHeight(true);
		int zRows = table00.getRowCount();
		table00.setPreferredScrollableViewportSize(new Dimension(440, zRows*table00.getRowHeight()));
		JScrollPane scroll00 = new JScrollPane(table00);

    	gen0.add(label00, "cell 0 1"); //$NON-NLS-1$
		gen0.add(scroll00, "cell 0 1, span, growx, growy");  //$NON-NLS-1$
		// Now we have self's Lifespan, add it into his text in p0
		selfLifespan =  (String) table00.getModel().getValueAt(0, 3);
        text01.setText(text01.getText() + " (" + selfLifespan + ")"); //$NON-NLS-1$ //$NON-NLS-2$

     // Define panel for the Generation +1 data
        JPanel genp1 = new JPanel(new MigLayout("insets 0, gap 5", "[grow]", "[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //Define label and table
        JLabel labelp1 = new JLabel("(+1)");	//$NON-NLS-1$
 		JTable tablep1 = new JTable() { private static final long serialVersionUID = 1L;
 		 								@Override
 		 								public boolean isCellEditable(int row, int column) {return false;}};
 		if (HGlobal.DEBUG)
 		 	System.out.println("Person VP relatives +1 Table:  4- " 		//$NON-NLS-1$
 		 			+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)));	//$NON-NLS-1$

 		tablep1.setModel(new DefaultTableModel(
 				pointViewpointHandler.getRelativesGC1Data(personVPindex),
 				pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)	//$NON-NLS-1$	// get Name, Relationship, Sex, Lifespan, Partner
 			));
		tablep1.getColumnModel().getColumn(0).setPreferredWidth(160);
		tablep1.getColumnModel().getColumn(0).setMinWidth(120);
		tablep1.getColumnModel().getColumn(1).setPreferredWidth(70);
		tablep1.getColumnModel().getColumn(1).setMinWidth(60);
		tablep1.getColumnModel().getColumn(2).setPreferredWidth(40);
		tablep1.getColumnModel().getColumn(2).setMaxWidth(40);
		tablep1.getColumnModel().getColumn(2).setMinWidth(20);
		tablep1.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tablep1.getColumnModel().getColumn(3).setPreferredWidth(90);
		tablep1.getColumnModel().getColumn(3).setMaxWidth(90);
		tablep1.getColumnModel().getColumn(3).setMinWidth(40);
		tablep1.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tablep1.getColumnModel().getColumn(4).setPreferredWidth(80);
		tablep1.getColumnModel().getColumn(4).setMinWidth(40);
		JTableHeader tHdp1 = tablep1.getTableHeader();
		tHdp1.setOpaque(false);
		tablep1.setFillsViewportHeight(true);
		int p1Rows = tablep1.getRowCount();
		if (p1Rows == 0) p1Rows = 1;
		if (p1Rows > 5) p1Rows = 5;			// default to 1-5 rows
		tablep1.setPreferredScrollableViewportSize(new Dimension(440, p1Rows*tablep1.getRowHeight()));
		JScrollPane scrollp1 = new JScrollPane(tablep1);

     	genp1.add(labelp1, "cell 0 1"); //$NON-NLS-1$
 		genp1.add(scrollp1, "cell 0 1, span, growx, growy");   //$NON-NLS-1$

     // Define panel for the Generation +2 data
        JPanel genp2 = new JPanel(new MigLayout("insets 0, gap 5", "[grow]", "[grow]"));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //Define label and table
        JLabel labelp2 = new JLabel("(+2)"); //$NON-NLS-1$
 		JTable tablep2 = new JTable() { private static final long serialVersionUID = 1L;
 		 								@Override
 		 								public boolean isCellEditable(int row, int column) {return false;}};

 		if (HGlobal.DEBUG)
 			System.out.println("Person VP relatives  +2 Table:  4- " 		//$NON-NLS-1$
 					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)));		//$NON-NLS-1$

 		tablep2.setModel(new DefaultTableModel(
 				pointViewpointHandler.getRelativesGC2Data(personVPindex),
 				pointViewpointHandler.setTranslatedData(tableScreenID, "4", false)	//$NON-NLS-1$	// get Name, Relationship, Sex, Lifespan, Partner
 			));
		tablep2.getColumnModel().getColumn(0).setPreferredWidth(160);
		tablep2.getColumnModel().getColumn(0).setMinWidth(120);
		tablep2.getColumnModel().getColumn(1).setPreferredWidth(70);
		tablep2.getColumnModel().getColumn(1).setMinWidth(60);
		tablep2.getColumnModel().getColumn(2).setPreferredWidth(40);
		tablep2.getColumnModel().getColumn(2).setMaxWidth(40);
		tablep2.getColumnModel().getColumn(2).setMinWidth(20);
		tablep2.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tablep2.getColumnModel().getColumn(3).setPreferredWidth(90);
		tablep2.getColumnModel().getColumn(3).setMaxWidth(90);
		tablep2.getColumnModel().getColumn(3).setMinWidth(40);
		tablep2.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tablep2.getColumnModel().getColumn(4).setPreferredWidth(80);
		tablep2.getColumnModel().getColumn(4).setMinWidth(40);
		JTableHeader tHdp2 = tablep2.getTableHeader();
		tHdp2.setOpaque(false);
		tablep2.setFillsViewportHeight(true);
		int p2Rows = tablep2.getRowCount();
		if (p2Rows == 0) p2Rows = 1;
		if (p2Rows > 5) p2Rows = 5;			// default to 1-5 rows
		tablep2.setPreferredScrollableViewportSize(new Dimension(440, p2Rows*tablep2.getRowHeight()));
		JScrollPane scrollp2 = new JScrollPane(tablep2);

     	genp2.add(labelp2, "cell 0 1");	//$NON-NLS-1$
 		genp2.add(scrollp2, "cell 0 1, span, growx, growy"); //$NON-NLS-1$

     //Bind all the generation panels together
        JPanel[] generationPanels;
        generationPanels = new JPanel[] {genm2, genm1, gen0, genp1, genp2};
 /**
  * Assemble the generationPanels together into the allGenPanel
  */
        JPanel allGenPanel = new JPanel(new MigLayout("insets 0, gap 0, fillx, hidemode 2", "[]", "[][][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    // Add all the generationPanels
        allGenPanel.add(generationPanels[0], "cell 0 0, growx"); //$NON-NLS-1$
        allGenPanel.add(generationPanels[1], "cell 0 1, growx"); //$NON-NLS-1$
        allGenPanel.add(generationPanels[2], "cell 0 2, growx"); //$NON-NLS-1$
        allGenPanel.add(generationPanels[3], "cell 0 3, growx"); //$NON-NLS-1$
        allGenPanel.add(generationPanels[4], "cell 0 4, growx"); //$NON-NLS-1$
        for (JPanel generationPanel : generationPanels) {
            generationPanel.setVisible(false);	// set generationPanels hidden initially
        	}
	    // Add the allGenPanel to the overall P2 panel
	    p2.add(allGenPanel, "cell 0 1, span, grow");  //$NON-NLS-1$
/*
 * All ToggleButton listeners - these make the generation panels visible or not
 */
		buttonm2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (generationPanels[0].isVisible()) {
						generationPanels[0].setVisible(false);
						buttonm2.setSelected(false);
						}
					else {
						generationPanels[0].setVisible(true);
						buttonm2.setSelected(true);
						}
				revalidate();
		        correctPanelSize();
			}
		});
		buttonm1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (generationPanels[1].isVisible()) {
						generationPanels[1].setVisible(false);
						buttonm1.setSelected(false);
						}
					else {
						generationPanels[1].setVisible(true);
						buttonm1.setSelected(true);
						}
				revalidate();
		        correctPanelSize();
			}
		});
		button00.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (generationPanels[2].isVisible()) {
						generationPanels[2].setVisible(false);
						button00.setSelected(false);
						}
					else {
						generationPanels[2].setVisible(true);
						button00.setSelected(true);
						}
				revalidate();
		        correctPanelSize();
			}
		});
		buttonp1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (generationPanels[3].isVisible()) {
						generationPanels[3].setVisible(false);
						buttonp1.setSelected(false);
						}
					else {
						generationPanels[3].setVisible(true);
						buttonp1.setSelected(true);
						}
				revalidate();
		        correctPanelSize();
			}
		});
		buttonp2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (generationPanels[4].isVisible()) {
						generationPanels[4].setVisible(false);
						buttonp2.setSelected(false);
						}
					else {
						generationPanels[4].setVisible(true);
						buttonp2.setSelected(true);
						}
				revalidate();
		        correctPanelSize();
			}
		});

		return p2;
    } 	// End peopleRelatives

// Define panel P3 for Associates
    private JPanel peopleAssocs() {
 		JPanel p3 = new JPanel(new MigLayout("insets 5", "[]10[]10[grow]10[]", "[]5[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JButton button31 = new JButton("+"); //$NON-NLS-1$
		button31.setMargin(new java.awt.Insets(1, 1, 1, 1));
		button31.setFont(new Font("Arial", Font.BOLD, 15)); //$NON-NLS-1$
		JButton button32 = new JButton("-"); //$NON-NLS-1$
		button32.setMargin(new java.awt.Insets(1, 2, 1, 2));
		button32.setFont(new Font("Arial", Font.BOLD, 15));	//$NON-NLS-1$

		JTable table31 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};

 		if (HGlobal.DEBUG)
 			System.out.println("Person VP Asociate Table:  5- " 		//$NON-NLS-1$
 					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "5", false)));	//$NON-NLS-1$

		table31.setModel(new DefaultTableModel(
				pointViewpointHandler.getAssociateData(personVPindex),
				pointViewpointHandler.setTranslatedData(tableScreenID, "5", false)	//$NON-NLS-1$		// get Person, Role, Event, Date
			));
		table31.getColumnModel().getColumn(0).setPreferredWidth(250);
		table31.getColumnModel().getColumn(0).setMinWidth(250);
		table31.getColumnModel().getColumn(1).setPreferredWidth(70);
		table31.getColumnModel().getColumn(1).setMinWidth(70);
		table31.getColumnModel().getColumn(2).setPreferredWidth(150);
		table31.getColumnModel().getColumn(2).setMinWidth(150);
		table31.getColumnModel().getColumn(3).setPreferredWidth(70);
		table31.getColumnModel().getColumn(3).setMinWidth(70);

		// Set row selection action and header
		ListSelectionModel pcellSelectionModel = table31.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader tHd31 = table31.getTableHeader();
		tHd31.setOpaque(false);
		table31.setFillsViewportHeight(true);
		int asRows = table31.getRowCount();
		if (asRows == 0) asRows = 1;
		if (asRows > 5) asRows = 5;			// show 1-5 rows as default
		table31.setPreferredScrollableViewportSize(new Dimension(640, asRows*table31.getRowHeight()));
		JScrollPane scroll31 = new JScrollPane(table31);

		p3.add(button31, "cell 4 0, alignx right"); //$NON-NLS-1$
		p3.add(button32, "cell 4 0, alignx right"); //$NON-NLS-1$
		p3.add(scroll31, "cell 0 1 4, span, grow");  //$NON-NLS-1$

		// These buttons allow the scrollpane height to be made bigger/smaller as it has no edge to resize with
		button31.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int max = table31.getRowHeight()*table31.getRowCount();
				int w1 = scroll31.getWidth();
				int h1plus = (int)Math.round(scroll31.getHeight()*1.3);
				if (h1plus > max) h1plus = max;					// stop increasing past table size
				table31.setPreferredScrollableViewportSize(new Dimension(w1, h1plus));
				scroll31.revalidate();
	            correctPanelSize();
 	            scroll31.getVerticalScrollBar().setValue(table31.getRowCount());	// move slider down
			}
		});
		button32.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int w2 = scroll31.getWidth();
				int h1minus = (int)Math.round(scroll31.getHeight()*.75);
				table31.setPreferredScrollableViewportSize(new Dimension(w2, h1minus));
				scroll31.revalidate();
		        correctPanelSize();
			}
		});

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
						int eventVPindex = pointViewpointHandler.findClosedVP("5300", pointOpenProject);	//$NON-NLS-1$
						String personVPident = pointViewpointHandler.getPersonScreenID(eventVPindex);
						long personPID = pointViewpointHandler.getPersonAssociatePID(personVPindex, selectedRowInTable);

				// Set PID for person from person list
						if (personVPident != null)
							pointOpenProject.pointGuiData.setTableViewPointPID(personVPident, personPID);
						else {
							userInfoInitVP(4);
							if (HGlobal.DEBUG)
								System.out.println(" HG0530ViewPerson valueChanged - personVPindex: "	//$NON-NLS-1$
										+ personVPindex);
						}
					} catch (HBException hbe) {
						System.out.println(" HG0530ViewPerson mark person: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					}
				}
			}
		});

	// Listener for Associate table mouse Double and Right-click
		table31.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				int rowClicked, colClicked, selectedRowInTable, selectedRow, errorCode = 0;
				long personTablePID;
				// DOUBLE-CLICK
	           	if (me.getClickCount() == 2  && table31.getSelectedRow() != -1) {
	       			colClicked = table31.getSelectedColumn();
	       			rowClicked = table31.getSelectedRow();
	       			personTablePID = pointViewpointHandler.getPersonAssociatePID(personVPindex, rowClicked);
		           	// DOUBLE-CLICK - if column=0, change ManagePerson focus to this Person
	           			if (colClicked == 0) {
			        		// change ManagePerson focus
		           			pointPersonHandler.initiateManagePerson(pointOpenProject, personTablePID, screenID);
		           		} else {
		           	// DOUBLE-CLICK other columns open Person VP for this Person
		           			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
		           			if (errorCode > 0) userInfoInitVP(errorCode);
		           		}

	           	} else
	            // RIGHT-click, open PersonVP
                if (me.getButton() == MouseEvent.BUTTON3) {
                	selectedRow = table31.rowAtPoint(me.getPoint());
                	table31.addRowSelectionInterval(selectedRow, selectedRow);
               		colClicked = table31.getSelectedColumn();
               		selectedRowInTable = table31.convertRowIndexToModel(selectedRow);
           			personTablePID = pointViewpointHandler.getPersonAssociatePID(personVPindex, selectedRowInTable);
           			errorCode = pointViewpointHandler.initiatePersonVP(pointOpenProject, personTablePID);
           			if (errorCode > 0) userInfoInitVP(errorCode);
                }
	        }
	    });
		return p3;
    }	// End peopleAsssocs

// Define panel P4 for Other Names
    private JPanel peopleNames() {
        JPanel p4 = new JPanel(new MigLayout("insets 5", "[grow]", "[grow]"));     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JTable table41 = new JTable() { private static final long serialVersionUID = 1L;
		 								@Override
		 								public boolean isCellEditable(int row, int column) {return false;}};

 		if (HGlobal.DEBUG)
 			System.out.println("Person VP Navn Table:  6- "		//$NON-NLS-1$
 					+ Arrays.toString(pointViewpointHandler.setTranslatedData(tableScreenID, "6", false))); 	//$NON-NLS-1$

		table41.setModel(new DefaultTableModel(
				 pointViewpointHandler.getNameData(personVPindex),
				 pointViewpointHandler.setTranslatedData(tableScreenID, "6", false)		//$NON-NLS-1$	// get Alt.Type, Alternate Name, Date
				));
		table41.getColumnModel().getColumn(0).setMinWidth(80);
		table41.getColumnModel().getColumn(0).setPreferredWidth(120);
		table41.getColumnModel().getColumn(1).setMinWidth(200);
		table41.getColumnModel().getColumn(1).setPreferredWidth(400);
		table41.getColumnModel().getColumn(2).setMinWidth(80);
		table41.getColumnModel().getColumn(2).setPreferredWidth(120);
		JTableHeader tHd41 = table41.getTableHeader();
		tHd41.setOpaque(false);
		table41.setFillsViewportHeight(true);
		int naRows = table41.getRowCount();
		if (naRows == 0) naRows = 1;
		table41.setPreferredScrollableViewportSize(new Dimension(640, naRows*table41.getRowHeight()));
		JScrollPane scroll41 = new JScrollPane(table41);

		p4.add(scroll41, "cell 0 0, growx"); //$NON-NLS-1$
		return p4;
    }	// End peopleNames

 // Define panel P5 for Primary Image thumbnail (if present)
     private JPanel peoplePriImage() {
         JPanel p5 = new JPanel(new MigLayout("fillx", "[]", "[center]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         JTextPane text50 = new JTextPane();
         text50.setEditable(false);
        ImageIcon exhibitImage = pointViewpointHandler.getPersonThumbImage(personVPindex);
         if (exhibitImage != null) {
          // Scale exhibitImage to match height of P5 (120)
        	//HBMediaHandler pointMediaHandler = (HBMediaHandler)HG0401HREMain.pointBusinessLayer[10];
        	HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
         	ImageIcon newscaleImage = pointMediaHandler.scaleImage(120, 120, exhibitImage);
         	text50.insertIcon(newscaleImage);
         	}
	     else  // insert default silhouette
	         text50.insertIcon(new ImageIcon(getClass().getResource("/hre/images/person-head-120.png"))); //$NON-NLS-1$

         p5.add(text50, "cell 0 0, alignx center");  //$NON-NLS-1$
         return p5;
     }	// End peoplePriImage

// Define panel P6 for all Images
    private JPanel peopleImages() {
        ArrayList<ImageIcon> listImages = pointViewpointHandler.getPersonImageList(personVPindex);
        // If no images, just show message
        if (listImages == null || listImages.size() == 0) {
        	JPanel p6 = new JPanel(new GridLayout(1, 1, 1, 1));
            JTextArea text60 = new JTextArea(2, 10);
            text60.setLineWrap(true);
            text60.setText(noImageText);  		// No images available - noImageText
        	p6.add(text60);
        	return p6;
        }
        // If images present, put in 2-column, x row grid, scaled to fit the setSize
        JPanel p6 = new JPanel(new GridLayout(0, 2, 10, 10));
        JLabel labels[] = new JLabel[listImages.size()];
        p6.setSize(100,200);
        for (int i = 0; i < listImages.size(); i++) {
        	ImageIcon exhibitImage = listImages.get(i);
            if (exhibitImage != null) {
            	// Rescale exhibitImage to a new max. height/width of 90
            	HBMediaHandler pointMediaHandler = pointOpenProject.getMediaHandler();
        		ImageIcon scaledExhibit = pointMediaHandler.scaleImage(90, 90, exhibitImage);
             	labels[i] = new JLabel(scaledExhibit);
            }
            p6.add(labels[i]);
        }
        return p6;
    }	// End peopleImages

// Define panel P7 for Flags
    private JPanel peopleFlags() {
        JPanel p7 = new JPanel(new MigLayout("", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JTable table71 = new JTable() { private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {return false;}};

/** We have all the person's current flag data already in objAllFlagData. Table columns are:
 * 0 - IS_SYSTEM?; 1 - ACTIVE?; 2 - GUI_SEQ; 3 - FLAG_ID; 4 - DEFAULT_INDEX; 5 - SETTING;
 * 6 - LANG_CODE; 7 - FLAG_NAME; 8 - FLAG_VALUES; 9 - FLAG_DESC
 */
		// We have the count of Active flags already in activeFlags,
		// so build only the Active Flag data into objReqFlagData.
		// Required table columns are:  0 - FLAG_NAME; 1 - SETTING;
		objReqFlagData = new Object[activeFlags][2];
		int reqRow = 0;
		for (int i = 0; i < objAllFlagData.length; i++) {
			if ((boolean) (objAllFlagData[i][1])) {
			// Get all possible flag values into a list
				objReqFlagData[reqRow][0] = objAllFlagData[i][7];	// Flag name
				objReqFlagData[reqRow][1] = objAllFlagData[i][5];   // Flag setting
				reqRow++;
			}
		}

		String[] flagTableHeader = pointPersonHandler.setTranslatedData("50600", "5", false);	//$NON-NLS-1$ //$NON-NLS-2$
		table71.setModel(new DefaultTableModel(	objReqFlagData,
												flagTableHeader
											));
		table71.getColumnModel().getColumn(0).setMinWidth(50);
		table71.getColumnModel().getColumn(0).setPreferredWidth(130);	// Flag name
		table71.getColumnModel().getColumn(1).setMinWidth(30);
		table71.getColumnModel().getColumn(1).setPreferredWidth(50);	// Flag setting
	// Centre Setting data
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table71.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
	// Set header format
		JTableHeader tHd71 = table71.getTableHeader();
		tHd71.setOpaque(false);
	// Add to panel P7 but don't allow huge table to display (let it scroll)
		table71.setFillsViewportHeight(true);
		if (activeFlags > 10) activeFlags = 10;
		table71.setPreferredScrollableViewportSize(new Dimension(200, activeFlags*table71.getRowHeight()));
		JScrollPane flagScrollPane = new JScrollPane(table71);
        p7.add(flagScrollPane, "cell 0 0, grow");	//$NON-NLS-1$
        return p7;
    }	// End peopleFlags

// Define panel P8 for Notepads - to be fully created later
    private JPanel peopleNotes() {
    	JPanel p8 = new JPanel(new GridLayout(1, 1, 1, 1));
        JTextArea text80 = new JTextArea(3, 10);
        text80.setLineWrap(true);
        text80.setText("Notepads not yet implemented");        //$NON-NLS-1$
        p8.add(new JScrollPane(text80));
        return p8;
    }	// End peopleNotes

/**
 * makePersonPanel - assembles the Action & Data Panels together into the total PersonPanel
 * @return	the assembled personPanel (a JSplitPane with 2 MigLayouts)
 */
    private JSplitPane makePersonPanel() {
    	JPanel perLeft = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][][][][][][][]"));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // First, add in dataPanel p0 with no actionPanel
        perLeft.add(dataPanels[0], "cell 0 0, growx"); //$NON-NLS-1$
        // Then add panels 1 - 4 action & data panels
        perLeft.add(actPanels[1], "cell 0 1, growx"); //$NON-NLS-1$
        perLeft.add(dataPanels[1], "cell 0 2, growx"); //$NON-NLS-1$
        perLeft.add(actPanels[2], "cell 0 3, growx"); //$NON-NLS-1$
        perLeft.add(dataPanels[2], "cell 0 4, growx"); //$NON-NLS-1$
        perLeft.add(actPanels[3], "cell 0 5, growx"); //$NON-NLS-1$
        perLeft.add(dataPanels[3], "cell 0 6, growx"); //$NON-NLS-1$
        perLeft.add(actPanels[4], "cell 0 7, growx"); //$NON-NLS-1$
        perLeft.add(dataPanels[4], "cell 0 8, growx"); //$NON-NLS-1$
        for(int j = 1; j < 5; j++) {
            dataPanels[j].setVisible(false);	// set dataPanels 1 - 4 hidden initially
        	}

        // Setup the right Splitpane from dataPanels p5-8
       	JPanel perRight = new JPanel(new MigLayout("insets 0, gap 1, fillx, hidemode 2", "[]", "[][][][][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // First, add panel p5, no title, centred
        perRight.add(dataPanels[5], "cell 0 0, center");		 //$NON-NLS-1$
        // Then add panels 6 - 8 action & data panels
    	perRight.add(actPanels[6], "cell 0 1, growx"); //$NON-NLS-1$
    	perRight.add(dataPanels[6], "cell 0 2, growx"); //$NON-NLS-1$
       	perRight.add(actPanels[7], "cell 0 3, growx"); //$NON-NLS-1$
    	perRight.add(dataPanels[7], "cell 0 4, growx"); //$NON-NLS-1$
       	perRight.add(actPanels[8], "cell 0 5, growx"); //$NON-NLS-1$
    	perRight.add(dataPanels[8], "cell 0 6, growx"); //$NON-NLS-1$
        for(int j = 6; j < dataPanels.length; j++) {
            dataPanels[j].setVisible(false);	// set dataPanels 6 - 8 hidden initially
        	}

        // Define splitpane personPanel's parameters
		UIManager.put("SplitPane.centerOneTouchButtons", false);	// ensure buttons are at divider top, not centre //$NON-NLS-1$
        personPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, perLeft, perRight);
       	personPanel.setOneTouchExpandable(true);
       	personPanel.setDividerSize(10);
     	personPanel.setResizeWeight(0.9);		// Screen growth goes mainly to the left pane
      	perLeft.setMinimumSize(new Dimension(100,0));		// start size is about 680, 200 (w, h)
     	perRight.setMinimumSize(new Dimension(150,0));   	// start size is about 250, 200
       	personPanel.setContinuousLayout(true);

        return personPanel;

    } // End makePersonPanel

/**
 * This handles the retention of the current Splitpane screen widths after
 * opening an ActionPanel or use of +/- buttons, when a pack() is required
 */
    private void correctPanelSize() {
    	// if frame was maximised, make sure we retain that height
    	if (peopleFrame.isMaximum()) maxHeight = peopleFrame.getHeight();
    	else maxHeight = HG0401HREMain.mainPane.getHeight() - 60;
    	// Get Frame width and splitpane divider location
    	widthFrame = peopleFrame.getWidth();
    	dividerLocation = personPanel.getDividerLocation();
        scrollPanel.setMaximumSize(new Dimension(5000, maxHeight));  // allow huge width but control height
    	// Reload the scrollPanel
    	peopleFrame.revalidate();
		contents.add(scrollPanel);
		// Do pack only if frame not maximised
        if (!peopleFrame.isMaximum()) peopleFrame.pack();
        // Reset the divider (which creeps right unless you do this)
        personPanel.setDividerLocation(dividerLocation);
        // Now get the frame height
        heightFrame = peopleFrame.getHeight();
        // Restore the Frame to original width with this height (otherwise right panels grow as well!)
        this.setSize(widthFrame, heightFrame);
    }	// End correctPanelSize

/**
 * Handle Mouse events on the Action Panels
 */
    @Override
	public void mousePressed(MouseEvent e) {
        ActionPanel ap = (ActionPanel) e.getSource();
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
    private void togglePanelVisible(ActionPanel ap) {
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
    private int getPanelIndex(ActionPanel ap) {
        for(int j = 0; j < actPanels.length; j++)
            if(ap == actPanels[j])
                return j;
        return -1;
    }	// End getPanelIndex
}	// End HG0530ViewPeople class


/**
 * Class to handle build of the ActionPanels and their pointer controls
 */
	class ActionPanel extends JPanel  {
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
    public ActionPanel(String text, MouseListener ml) {
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
    }	// End ActionPanel constructor

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

}	// End ActionPanel class
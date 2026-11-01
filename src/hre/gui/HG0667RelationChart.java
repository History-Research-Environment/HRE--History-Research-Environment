package hre.gui;
/************************************************************************************************
 * Relationship Chart
 * v0.05.0034 2026-10-04 Initial draft (D Ferguson)
 *			  2026-10-05 Add print capability (ChatGPT)
 *			  2026-10-06 Re-arrange caption/buttons on screen and print layout (D Ferguson + ChatGPT))
 *			  2026-10-09 Add save to PDF/RTF functions (D Ferguson +ChatGPT)
 *			  2026-10-16 NLS al code (D Ferguson)
 ***********************************************************************************************
 * NOTES for incomplete functionality:
 *	Add ability to choose to add birth/death date/place into boxes
 *********************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBRelationHandler;
import hre.bila.HBRelationHandler.Relationship;
import hre.bila.HBRelationHandler.RelationshipClassifier;
import hre.bila.HBRelationHandler.RelationshipDescriptor;
import hre.bila.HBRelationHandler.RelationshipNamer;
import hre.bila.HBRelationHandler.RelationshipNamerFactory;
import hre.bila.HBRelationHandler.RelationshipPanel;
import hre.bila.HBRelationHandler.RelationshipPath;
import hre.nls.HG0667Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Relationship Chart
 * @author D Ferguson
 * @version v0.05.0034
 * @since 2026-10-04
 */
public class HG0667RelationChart extends HG0450SuperDialog implements TransferSelectedPerson {
	private static final long serialVersionUID = 001L;
	public static final String screenID = "66700";	//$NON-NLS-1$

	protected HBPersonHandler pointPersonHandler;
    protected HBProjectOpenData pointOpenProject;
    protected HBRelationHandler pointRelationHandler;
    HG0667RelationChart pointRelationChart = this;

	protected JDialog relateFrame = this;
	private JPanel contents;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

    // For relationships
    JPanel selectionPanel, chartPanel;
    JButton btn_Select1, btn_Select2, btn_Save;
    JButton btn_visual1, btn_visual2;
    JLabel persName1, persName2;
    long person1PID = null_RPID, person2PID = null_RPID;

    long selectedPID;
    String sexCode1 = "U", sexCode2 = "U";	//$NON-NLS-1$ //$NON-NLS-2$
    JLabel relationship1, relationship2;
	String relateText1 = "", relateText2 = "";	//$NON-NLS-1$ //$NON-NLS-2$
    int[] relateTable;
    String graphCaption1, graphCaption2;

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0667RelationChart(HBProjectOpenData pointOpenProject) throws HBException {

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0667RelationChart");}	//$NON-NLS-1$

	// Setup all references for HG0667
		windowID = screenID;
		helpName = "relationchart";	//$NON-NLS-1$
    	this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		setResizable(false);
    	setTitle(HG0667Msgs.Text_0);		// Relationship Chart Creation Tool

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[grow]", "[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add our menu icons and HG0451 icons
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/***************************
 * Setup selectionPanel
 **************************/
		selectionPanel = new JPanel();
		selectionPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		selectionPanel.setLayout(new MigLayout("insets 10", "[]10[grow]", "[]10[grow]10[grow]10[grow]10[grow]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		contents.add(selectionPanel, "cell 0 0");	//$NON-NLS-1$

		JLabel relation = new JLabel(HG0667Msgs.Text_1); // Find any Relationships between Selected People
		selectionPanel.add(relation, "cell 0 0 2");	//$NON-NLS-1$

		// Get the current JLabel height for this font
		int hi = (int) relation.getPreferredSize().getHeight();

		btn_Select1 = new JButton(HG0667Msgs.Text_2);		// Select Person
		selectionPanel.add(btn_Select1, "cell 0 1, alignx right");	//$NON-NLS-1$
		btn_Select2 = new JButton(HG0667Msgs.Text_3);		// Select Possible Relation
		selectionPanel.add(btn_Select2, "cell 0 2");		//$NON-NLS-1$

		persName1 = new JLabel();
		persName1.setPreferredSize(new Dimension(400, hi));	 // set a reasonable length for the Name
		selectionPanel.add(persName1, "cell 1 1");		//$NON-NLS-1$
		persName2 = new JLabel();
		selectionPanel.add(persName2, "cell 1 2");		//$NON-NLS-1$

		relationship1 = new JLabel();
		relationship2 = new JLabel();
		selectionPanel.add(relationship1, "cell 0 3 2");	//$NON-NLS-1$
		selectionPanel.add(relationship2, "cell 0 4 2, hidemode 3");	//$NON-NLS-1$

		btn_visual1 = new JButton(HG0667Msgs.Text_4);		// Visualize
		btn_visual1.setVisible(false);
		selectionPanel.add(btn_visual1, "cell 1 3, align right, hidemode 3");	//$NON-NLS-1$

		btn_visual2 = new JButton(HG0667Msgs.Text_4);		// Visualize
		btn_visual2.setVisible(false);
		selectionPanel.add(btn_visual2, "cell 1 4, align right, hidemode 3");	//$NON-NLS-1$

      	JButton btn_Close = new JButton(HG0667Msgs.Text_6);		// Close
    	selectionPanel.add(btn_Close, "cell 1 5, alignx right");		//$NON-NLS-1$

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
		    }
		});

	   	// listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0667RelationChart"); //$NON-NLS-1$
		    	dispose();
			}
    	});

		// Listener for selection of person1
		btn_Select1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				selectedPID = null_RPID;
				// Call the SelectPerson code to select a person
				HG0507SelectPerson personSelectScreen = null;
				try {
					personSelectScreen = new HG0507SelectPerson(pointPersonHandler, pointOpenProject, false);
					personSelectScreen.additionalPanel = false; // Setting in Selectperson
					personSelectScreen.pointSelectInterface = pointRelationChart;	// setting in selectPerson
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = btn_Select1.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
					// Save returned PID
					person1PID = selectedPID;
					// Put name into selectionpanel
					persName1.setText(pointPersonHandler.getPersonName(selectedPID));
					// Get sex of person1; assume unknown to start with
					sexCode1 = "U";		//$NON-NLS-1$
					if (selectedPID != null_RPID) {
						int sexNum = pointPersonHandler.getPersonSex(person1PID);
						if (sexNum == 2) sexCode1 = "M";	//$NON-NLS-1$
						if (sexNum == 1) sexCode1 = "F";	//$NON-NLS-1$
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} catch (HBException hbe) {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0667 findselect person1 " + hbe.getMessage());	//$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				}
				// If both person1 & 2 have been selected, then proceed with checking relationship
				if (person1PID != null_RPID && person2PID != null_RPID) {
					 // Check the sexCode - if = U we can't calculate a relationship
				    if (sexCode2.equals("U")) return;	//$NON-NLS-1$
				    // If they are both the same person, show msg and return
				    if (person1PID == person2PID) {
				    	relationship1.setText(HG0667Msgs.Text_7);		// Choose a different person
				    	return;
				    }
				    if (pointRelationHandler == null)
						pointRelationHandler = new HBRelationHandler(pointOpenProject);
					calculateRelationship(person1PID, person2PID);
				}
	        }
	      });

		// Listener for selection of person2
		btn_Select2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				selectedPID = null_RPID;
				// Call the SelectPerson code to select a person
				HG0507SelectPerson personSelectScreen = null;
				try {
					personSelectScreen = new HG0507SelectPerson(pointPersonHandler, pointOpenProject, false);
					personSelectScreen.additionalPanel = false; // Setting in Selectperson
					personSelectScreen.pointSelectInterface = pointRelationChart;	// setting in selectPerson
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = btn_Select2.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
					// Save returned PID
					person2PID = selectedPID;
					// Put name into selectionPanel
					persName2.setText(pointPersonHandler.getPersonName(selectedPID));
					// Get sex of person2; assume unknown to start with
					sexCode2 = "U";			//$NON-NLS-1$
					if (selectedPID != null_RPID) {
						int sexNum = pointPersonHandler.getPersonSex(person2PID);
						if (sexNum == 2) sexCode2 = "M";	//$NON-NLS-1$
						if (sexNum == 1) sexCode2 = "F";	//$NON-NLS-1$
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} catch (HBException hbe) {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0667 findselect person2 " + hbe.getMessage());	//$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				}
				// If both person1 & 2 have been selected, then proceed with checking relationship
				if (person1PID != null_RPID && person2PID != null_RPID) {
					 // Check the sexCode - if = U we can't calculate a relationship
				    if (sexCode2.equals("U")) return;	//$NON-NLS-1$
				    // If they are both the same person, show msg and return
				    if (person1PID == person2PID) {
				    	relationship1.setText(HG0667Msgs.Text_7);		// Choose a different person
				    	return;
				    }
				    if (pointRelationHandler == null)
						pointRelationHandler = new HBRelationHandler(pointOpenProject);
					calculateRelationship(person1PID, person2PID);
				}
	        }
	      });

		// Listener for Relationship visual1 button
		btn_visual1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Build the chart panel using primaryPath data (primary=true)
				buildRelationDiagramPanel(relationship1.getText(), true);
			}
		});

		// Listener for Relationship visual2 button
		btn_visual2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Build the chart panel using primaryPath data (primary=false)
				buildRelationDiagramPanel(relationship2.getText(), false);
			}
		});

	}	// End HG0667RelationChart constructor

/**
 * private void calculateRelationship(long person1PID, long person2PID)
 * @param person1PID
 * @param person2PID
 **/
    private void calculateRelationship(long person1PID, long person2PID) {
    	// Action after person 1 & 2 selected
    	// Clean out any possible previous displayed info
    	relateText1 = "";				//$NON-NLS-1$
    	relateText2 = "";				//$NON-NLS-1$
    	relationship1.setText("");		//$NON-NLS-1$
       	relationship2.setText("");		//$NON-NLS-1$
   		relationship2.setVisible(false);
		btn_visual1.setVisible(false);
		btn_visual2.setVisible(false);
		pack();

    	// Get relationship data (2 x 2 integer pairs)
    	Relationship relations = pointRelationHandler.findRelationships(person1PID, person2PID);
    	// following are the 2 relation pairs
    	int relate1A = relations.primary.x;
    	int relate1B = relations.primary.y;
    	int relate2A = relations.secondary.x;
    	int relate2B = relations.secondary.y;

    	// Check if first pair zero; if so, display 'No relation' msg; if not, get the relationship
    	if (relate1A == 0 && relate1B == 0) {
    		relationship1.setText(HG0667Msgs.Text_9); // No blood relationship exists between these two people
    		return;
    	}
    	if (relate1A != 0 || relate1B != 0) {
    		RelationshipDescriptor d1 = RelationshipClassifier.classify(relate1A, relate1B, sexCode2);
    		RelationshipNamer namer = RelationshipNamerFactory.forLanguage(HGlobal.dataLanguage);
    		relateText1 = namer.name(d1);
    	}
    	// Repeat for the 2nd pair
    	if (relate2A != 0 || relate2B != 0) {
    		RelationshipDescriptor d2 = RelationshipClassifier.classify(relate2A, relate2B, sexCode2);
    		RelationshipNamer namer = RelationshipNamerFactory.forLanguage(HGlobal.dataLanguage);
    		relateText2 = namer.name(d2);
    	}
    	// Put the text (if any) into the panel.
    	// Also build caption texts for the Relationship graphs (so ready if needed).
    	relationship1.setText(persName2.getText() + HG0667Msgs.Text_10			// is the
    			+ relateText1 + HG0667Msgs.Text_11 + persName1.getText());		//  of
    	graphCaption1 = relationship1.getText();
    	btn_visual1.setVisible(true);

    	if (!relateText2.isEmpty()) {
    		relationship2.setText(persName2.getText() + HG0667Msgs.Text_10 		// is the
    				+ relateText2 + HG0667Msgs.Text_11 + persName1.getText());	//  of
    		graphCaption2 = relationship2.getText();
    		relationship2.setVisible(true);
    		btn_visual2.setVisible(true);
    	}
    	pack();
    }

/**
 * Creates a scroll pane whose viewport grows with the relation diagram,
 * up to a requested maximum size. Scrollbars appear only when required.
 */
    private static JScrollPane createRelationshipScrollPane (
            RelationshipPanel panel, int maxViewportWidth,  int maxViewportHeight) {
        JScrollPane scrollPane = new JScrollPane(panel);
        Dimension diagramSize = panel.getPreferredSize();
        int viewportWidth = Math.min(diagramSize.width,maxViewportWidth) + 20;
        int viewportHeight = Math.min(diagramSize.height,maxViewportHeight) + 20;
        scrollPane.setPreferredSize(new Dimension(viewportWidth,viewportHeight));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }		// End createRelationshipScrollPane



/**
 * buildRelationDiagramPanel - common routine for use by btn_Visual1/2 listeners
 * @param caption - the diagram's caption
 * @param primary - whether to use the Relation Primary path (btn_Visual1) or Secondary (btn_Visual2)
 */
    @SuppressWarnings("unused")
    public void buildRelationDiagramPanel(String caption, boolean primary) {
    	// Build the relationship data for this pair
    	Relationship relations =
    			pointRelationHandler.findRelationships(person1PID, person2PID);
    	// Build the panel layout based on the Primary or Secondary person1PID -> LCA -> person2PID paths
    	RelationshipPanel panel;
    	if (primary) panel = new HBRelationHandler.RelationshipPanel(relations.primaryPath, false);  // turn off interactive selection
    	else panel = new HBRelationHandler.RelationshipPanel(relations.secondaryPath, false);  // turn off interactive selection

    	// Setup scrollpane to hold panel
    	JScrollPane scrollPane = createRelationshipScrollPane(panel, 760, 420);

    	// Define a new panel for holding everything
    	chartPanel = new JPanel();
    	chartPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
    	chartPanel.setLayout(new MigLayout("insets 10", "[]", "[grow][][grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	// Add a caption panel and text
    	JPanel captionPanel = new JPanel();
    	captionPanel.setLayout(new MigLayout("insets 10", "[]", "[grow][grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JLabel lbl_Heading = new JLabel(HG0667Msgs.Text_14);	// Relationship Chart
    	lbl_Heading.setFont(lbl_Heading.getFont().deriveFont(Font.BOLD));
    	captionPanel.add(lbl_Heading, "cell 0 0, alignx center");		//$NON-NLS-1$
    	JLabel lbl_Caption = new JLabel(caption);
    	captionPanel.add(lbl_Caption, "cell 0 1, alignx center");		//$NON-NLS-1$
    	chartPanel.add(captionPanel, "cell 0 0, alignx center");		//$NON-NLS-1$

    	// Add the scrollpane
    	chartPanel.add(scrollPane, "cell 0 1");	//$NON-NLS-1$

    	// Add controls for Save, Print, etc
    	btn_Save = new JButton(HG0667Msgs.Text_15);	// Save to File
    	chartPanel.add(btn_Save, "cell 0 2, alignx right");		//$NON-NLS-1$
    	JButton btn_Print = new JButton(HG0667Msgs.Text_16);	// Print
    	chartPanel.add(btn_Print, "cell 0 2, alignx right, gapx 20");		//$NON-NLS-1$

    	// Setup a Save listener -create the JPanel, then save as PDF or RTF
    	btn_Save.addActionListener(e -> {
       		JPanel printableChart;
    		if (primary) printableChart = createPrintableChart(relations.primaryPath, captionPanel);
    		else printableChart = createPrintableChart(relations.secondaryPath, captionPanel);
    		// Show choice of which filetype to save
    		saveChartToFile(printableChart, captionPanel);
    	});
    	// Setup Print listener - create the printable JPanel, then print it
    	btn_Print.addActionListener(e -> {
    		JPanel printableChart;
    		if (primary) printableChart = createPrintableChart(relations.primaryPath, captionPanel);
    		else printableChart = createPrintableChart(relations.secondaryPath, captionPanel);
    		printChart(printableChart);
    	});

    	// Setup a dialog owner
    	Window owner = SwingUtilities.getWindowAncestor(btn_visual1);
    	JInternalFrame driver = (JInternalFrame) SwingUtilities.getAncestorOfClass(
    			JInternalFrame.class, btn_visual1);

    	// Create the diagram dialog
    	JDialog graph = new JDialog(owner, HG0667Msgs.Text_14, JDialog.ModalityType.MODELESS); // Relationship Chart
    	// Ensure the diagram closes if the driver closes
    	if (driver != null) {
    		driver.addInternalFrameListener(new InternalFrameAdapter() {
    					@Override
    					public void internalFrameClosed(InternalFrameEvent e) {
    						graph.dispose();
    					}
    				});
    	}

    	// Display the whole diagram
    	graph.add(chartPanel);
    	graph.pack();
    	Point xy = btn_Select2.getLocationOnScreen();
    	graph.setLocation(xy);
    	graph.setVisible(true);
    }

/**
 * createPrintableChart - creates a printable (or save-able) JPanel
 */
    private JPanel createPrintableChart(RelationshipPath path, JPanel captionPanel) {
        // Create a separate RelationshipPanel for printing or saving.
        // IMPORTANT: Never use the on-screen panel here.
        RelationshipPanel printPanel = new HBRelationHandler.RelationshipPanel(path, false);

        // White background for the printed chart
        printPanel.setBackground(Color.WHITE);

       // Create print-only caption panel
        JPanel printCaptionPanel = new JPanel();
        printCaptionPanel.setOpaque(true);
        printCaptionPanel.setBackground(Color.WHITE);

        printCaptionPanel.setLayout(new MigLayout("insets 10","[grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Get the labels from the displayed caption panel
        JLabel displayHeading = (JLabel) captionPanel.getComponent(0);
        JLabel displayCaption = (JLabel) captionPanel.getComponent(1);

        // Recreate the Heading
        JLabel printHeading = new JLabel(displayHeading.getText());
        printHeading.setFont(displayHeading.getFont());
        printHeading.setHorizontalAlignment(SwingConstants.CENTER);

        // Recreate the Caption
        JLabel printCaption = new JLabel(displayCaption.getText());
        printCaption.setFont(displayCaption.getFont());
        printCaption.setHorizontalAlignment(SwingConstants.CENTER);

        // Add caption components
        printCaptionPanel.add(printHeading, "cell 0 0, growx, alignx center");	//$NON-NLS-1$
        printCaptionPanel.add(printCaption,"cell 0 1, growx, alignx center");	//$NON-NLS-1$

        // Create complete printable chart
        JPanel printableChart = new JPanel();
        printableChart.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
        printableChart.setBackground(Color.WHITE);
        printableChart.setOpaque(true);
        printableChart.setLayout(new MigLayout("insets 10","[grow]","[][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Caption
        printableChart.add(printCaptionPanel, "cell 0 0, alignx center");		//$NON-NLS-1$

        // Full relationship diagram
        printableChart.add(printPanel, "cell 0 1, alignx center");		//$NON-NLS-1$

        // Force layout calculations
        printableChart.doLayout();
        printCaptionPanel.doLayout();
        printPanel.doLayout();

        // Determine the full chart size
        Dimension chartSize = printableChart.getPreferredSize();
        printableChart.setSize(chartSize.width, chartSize.height);
        printableChart.doLayout();

        return printableChart;
    }		// End createPrintableChart

/**
 * printChart - print the previously created JPanel
 */
    private void printChart(JPanel chart) {
    	PrinterJob job = PrinterJob.getPrinterJob();
    	job.setJobName(HG0667Msgs.Text_14);		// Relationship Chart
    	job.setPrintable((graphics, pageFormat, pageIndex) -> {
    		if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
    		Graphics2D g2 = (Graphics2D) graphics.create();
    		try {
    			// Get complete chart dimensions
    			Dimension chartSize = chart.getPreferredSize();
    			int chartWidth = chartSize.width;
    			int chartHeight = chartSize.height;
    			// Make sure the chart has its full size
    			chart.setSize(chartWidth, chartHeight);
    			chart.doLayout();

    			// Printable page dimensions
    			double pageWidth = pageFormat.getImageableWidth();
    			double pageHeight = pageFormat.getImageableHeight();

    			// Scale chart to fit page
    			double scale = Math.min(pageWidth / chartWidth, pageHeight / chartHeight);

    			// Don't enlarge a chart that is already smaller
    			scale = Math.min(scale, 1.0);

    			// Centre horizontally
    			// Start at top of printable area
    			double x = pageFormat.getImageableX() + (pageWidth - chartWidth * scale) / 2.0;
    			double y = pageFormat.getImageableY();
    			g2.translate(x, y);
    			g2.scale(scale, scale);

    			// Print the complete chart
    			chart.printAll(g2);
    		}
    		finally {
    			g2.dispose();
    		}
    		return Printable.PAGE_EXISTS;
    	});

    	// Show standard printer dialog
    	if (!job.printDialog()) return;
    	try {
    		job.print();
    	}
    	catch (PrinterException ex) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0667 print: " + ex.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(ex);
			}
    	}
    }		// End printChart

/**
 * saveChartToFile - prompt for filetype to save
 * @param printableChart
 * @param captionPanel
 */
    private void saveChartToFile(JPanel printableChart, JPanel captionPanel) {
        Object[] options = {"PDF", "RTF (Word)", HG0667Msgs.Text_19};	//$NON-NLS-1$ //$NON-NLS-2$	// Cancel
        int choice = JOptionPane.showOptionDialog(
                        btn_Save,
                        HG0667Msgs.Text_20,		// Save relationship chart as:
                        HG0667Msgs.Text_21,		// Save Relationship Chart
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
        if (choice == 0) saveChartAsPDF(printableChart);
        else if (choice == 1) {saveChartAsRTF(printableChart, captionPanel);
        }
    }		// End saveChartToFile

/**
 * saveChartAsRTF
 * @param chart
 * @param captionPanel
 */
    private void saveChartAsRTF(JPanel chart, JPanel captionPanel) {

    	JFileChooser chooser = new JFileChooser(HGlobal.pathHRElocation);	// set default folder location
    	chooser.setDialogTitle(HG0667Msgs.Text_22);		// Save Relationship Chart as RTF
    	chooser.setSelectedFile(new File(HG0667Msgs.Text_23));	// Relationship Chart.rtf
    	if (chooser.showSaveDialog(chartPanel) != JFileChooser.APPROVE_OPTION) return;
    	File file = chooser.getSelectedFile();

    	if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".rtf")) {		//$NON-NLS-1$
    		file = new File(file.getAbsolutePath() + ".rtf");							//$NON-NLS-1$
    	}

    	try {
    		// Get the existing heading and caption.
    		JLabel displayHeading = (JLabel) captionPanel.getComponent(0);
    		JLabel displayCaption = (JLabel) captionPanel.getComponent(1);

    		// Render the complete chart at high resolution.
    		BufferedImage image = createChartImage(chart, 3.0);

    		// Encode the image as PNG bytes.
    		ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
    		ImageIO.write(image, "png", imageBuffer);		//$NON-NLS-1$
    		byte[] imageBytes = imageBuffer.toByteArray();

    		// RTF page dimensions: A4, in twips - Use half-inch margins.
    		int pageWidth = 11906;
    		int pageHeight = 16838;
    		int margin = 720;
    		int maxWidth = pageWidth - 2 * margin;
    		int maxHeight = pageHeight - 2 * margin;
    		// Reserve space for the heading and caption.
    		maxHeight -= 1800;

    		// Fit the image without distorting its proportions.
    		double scale = Math.min((double) maxWidth / image.getWidth(), (double) maxHeight / image.getHeight());
    		int pictureWidth = (int) Math.round(image.getWidth() * scale);
    		int pictureHeight = (int) Math.round(image.getHeight() * scale);

    		try (BufferedWriter out = new BufferedWriter(
    				new OutputStreamWriter(new FileOutputStream(file),  StandardCharsets.US_ASCII))) {
    			// RTF document header.
    			out.write("{\\rtf1\\ansi\\ansicpg1252\\uc1");		//$NON-NLS-1$
    			out.write("\\deff0");								//$NON-NLS-1$
    			out.write("{\\fonttbl{\\f0\\fnil Arial;}}");		//$NON-NLS-1$
    			out.write("\\paperw" + pageWidth);					//$NON-NLS-1$
    			out.write("\\paperh" + pageHeight);					//$NON-NLS-1$
    			out.write("\\margl" + margin);						//$NON-NLS-1$
    			out.write("\\margr" + margin);						//$NON-NLS-1$
    			out.write("\\margt" + margin);						//$NON-NLS-1$
    			out.write("\\margb" + margin);						//$NON-NLS-1$
    			out.write("\n");									//$NON-NLS-1$

    			// Editable heading.
    			Font headingFont = displayHeading.getFont();
    			out.write("\\pard\\qc\\sb120\\sa120");				//$NON-NLS-1$
    			out.write("\\f0\\fs" + headingFont.getSize() * 2);	//$NON-NLS-1$
    			if (headingFont.isBold()) out.write("\\b");			//$NON-NLS-1$
    			if (headingFont.isItalic()) out.write("\\i");		//$NON-NLS-1$
    			out.write(" ");										//$NON-NLS-1$
    			writeRtfText(out, displayHeading.getText());
    			out.write("\\b0\\i0\\par\n");						//$NON-NLS-1$

    			// Editable caption.
    			Font captionFont = displayCaption.getFont();
    			out.write("\\pard\\qc\\sb60\\sa240");				//$NON-NLS-1$
    			out.write("\\f0\\fs"+ captionFont.getSize() * 2);	//$NON-NLS-1$
    			out.write(" ");										//$NON-NLS-1$
    			writeRtfText(out, displayCaption.getText());
    			out.write("\\par\n");								//$NON-NLS-1$

    			// Embedded PNG relationship diagram.
    			out.write("\\pard\\qc\\sb120\\sa120 ");				//$NON-NLS-1$
    			out.write("{\\pict\\pngblip");						//$NON-NLS-1$
    			out.write("\\picw" + image.getWidth());				//$NON-NLS-1$
    			out.write("\\pich" + image.getHeight());			//$NON-NLS-1$
    			out.write("\\picwgoal" + pictureWidth);				//$NON-NLS-1$
    			out.write("\\pichgoal" + pictureHeight);			//$NON-NLS-1$
    			out.write("\n");									//$NON-NLS-1$

    			// Write PNG bytes as hexadecimal.
    			final char[] hex = "0123456789abcdef".toCharArray();	//$NON-NLS-1$
    			for (int i = 0; i < imageBytes.length; i++) {
    				int value = imageBytes[i] & 0xFF;
    				out.write(hex[value >>> 4]);
    				out.write(hex[value & 0x0F]);
    				// Keep individual output lines manageable.
    				if ((i & 31) == 31)  out.write('\n');
    			}
    			out.write("}}\\par\n");		//$NON-NLS-1$
    			out.write("}");				//$NON-NLS-1$
    		}

    		JOptionPane.showMessageDialog(btn_Save,
    				HG0667Msgs.Text_24,	// Relationship chart saved successfully
    				HG0667Msgs.Text_25,			// Save to RTF
    				JOptionPane.INFORMATION_MESSAGE);

    	}
    	catch (Exception ex) {
    		JOptionPane.showMessageDialog(btn_Save,
    				HG0667Msgs.Text_26,		// Unable to save the relationship chart
    				HG0667Msgs.Text_25,		// Save to RTF
    				JOptionPane.ERROR_MESSAGE);
    		ex.printStackTrace();
    	}
    }		// End saveChartAsRTF

/**
 * writeRtfText - to clean the RTF text
 * @param out
 * @param text
 * @throws IOException
 */
	private void writeRtfText(java.io.Writer out, String text) throws IOException {
		// RTF reserves characters such as \, { and } for its own formatting syntax.
		// This coder safely handles those characters and non-ASCII text, including names with accented characters.
		if (text == null) return;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			switch (ch) {
			case '\\':
				out.write("\\\\");		//$NON-NLS-1$
				break;
			case '{':
				out.write("\\{");		//$NON-NLS-1$
				break;
			case '}':
				out.write("\\}");		//$NON-NLS-1$
				break;
			case '\n':
				out.write("\\line ");	//$NON-NLS-1$
				break;
			case '\r':
				// Ignore carriage returns.
				break;
			case '\t':
				out.write("\\tab ");	//$NON-NLS-1$
				break;
			default:
				if (ch >= 32 && ch <= 126) out.write(ch);
				else if (ch >= 0xD800 && ch <= 0xDFFF) {
					// Preserve UTF-16 surrogate code units,
					// including those used by supplementary
					// Unicode characters.
					out.write("\\u");		//$NON-NLS-1$
					out.write(Integer.toString((int) ch - 65536));
					out.write("?");		//$NON-NLS-1$
				}
				else if (ch > 126) {
					// RTF Unicode escape uses a signed
					// 16-bit value.
					int unicode = ch > 32767 ? ch - 65536 : ch;
					out.write("\\u");		//$NON-NLS-1$
					out.write(Integer.toString(unicode));
					out.write("?");		//$NON-NLS-1$
				}
				else out.write(' ');
				break;
			}
		}
	}		// End writeRtfText

/**
 * saveChartAsPDF
 */
    private void saveChartAsPDF(JPanel chart) {
        JFileChooser chooser = new JFileChooser(HGlobal.pathHRElocation);	// set default folder location
        chooser.setDialogTitle(HG0667Msgs.Text_28);		// Save Relationship Chart as PDF
        chooser.setSelectedFile(new File(HG0667Msgs.Text_29));		// Relationship Chart.pdf
        if (chooser.showSaveDialog(chartPanel) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        // Ensure .pdf extension
        if (!file.getName().toLowerCase().endsWith(".pdf")) {		//$NON-NLS-1$
            file = new File(file.getAbsolutePath() + ".pdf");		//$NON-NLS-1$
        }

        try {
            // Render the complete Swing chart
            BufferedImage image = createChartImage(chart, 3.0);
            // Convert image to PNG data for iText
            File imageFile = File.createTempFile("relationship-chart-", ".png");	//$NON-NLS-1$ //$NON-NLS-2$
            try {
                ImageIO.write(image, "png", imageFile);	//$NON-NLS-1$
                // Create PDF
                PdfWriter writer = new PdfWriter(new FileOutputStream(file));
                PdfDocument pdf = new PdfDocument(writer);
                // Use A4 portrait
                Document document = new Document(pdf, PageSize.A4);
                document.setMargins(20, 20, 20, 20);
                // Load PNG into iText
                ImageData imageData = ImageDataFactory.create(imageFile.getAbsolutePath());
                Image pdfImage = new Image(imageData);
                // Available page width
                float pageWidth = PageSize.A4.getWidth() - document.getLeftMargin() - document.getRightMargin();
                pdfImage.scaleToFit( pageWidth,PageSize.A4.getHeight() - document.getTopMargin() - document.getBottomMargin());
                document.add(pdfImage);
                document.close();
            }
            finally {
                if (!imageFile.delete()) {imageFile.deleteOnExit();}
            }
            JOptionPane.showMessageDialog( btn_Save,
                    HG0667Msgs.Text_24,		// Relationship chart saved successfully
                    HG0667Msgs.Text_31,		// Save to PDF
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(btn_Save,
                    HG0667Msgs.Text_26,		// Unable to save the relationship chart
                    HG0667Msgs.Text_31,		// Save to PDF
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }		// End saveChartAsPDF

/**
 * createChartImage - rendering routine for use by saveChartAsXXX routines
 */
    private BufferedImage createChartImage(JPanel chart, double scale) {
        Dimension size = chart.getPreferredSize();
        int width = (int) Math.ceil(size.width * scale);
        int height = (int) Math.ceil(size.height * scale);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 =image.createGraphics();
        try {
            // White background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
            // High-quality rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Give the panel its normal logical size
            chart.setSize(size.width, size.height);
            chart.doLayout();
            // Render at higher resolution
            g2.scale(scale, scale);
            chart.printAll(g2);
        }
        finally {
            g2.dispose();
        }
        return image;
    }		// End createChartImage

/**
 * interface TransferSelectedPerson
 */
		public void updatePersonPointer(long personPID) {
			selectedPID = personPID;
		}	// End updatePersonPointe

}  // End of HG0667RelationChart
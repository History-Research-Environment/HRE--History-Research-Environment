package hre.gui;
/**************************************************************************************
 * Edit Citation
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-19 Add Visualize button; add Listeners for text fields (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE02 all save functions need completing
 * NOTE03 Visualise button needs action written
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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Citation
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */

public class HG0555EditCitation extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "55500"; //$NON-NLS-1$

	private JPanel contents;
	boolean outputVisible = false;

/**
 * Create the dialog
 */
	public HG0555EditCitation()  {
		setTitle("Edit Citation");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editcitation";		 //$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0555EditCitation");	//$NON-NLS-1$

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Setup Citation panel for citation details
		JPanel citePanel = new JPanel();
		citePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		citePanel.setLayout(new MigLayout("", "[]10[]", "[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel sorc = new JLabel("Source:");
		citePanel.add(sorc, "cell 0 0, alignx right");	//$NON-NLS-1$
		JTextField sorcName = new JTextField();
		sorcName.setColumns(25);
		sorcName.setEditable(false);
		citePanel.add(sorcName, "cell 1 0, alignx left");	//$NON-NLS-1$
		JButton btn_sorcSelect = new JButton("Select Source");	// Select Source
		citePanel.add(btn_sorcSelect, "cell 1 0, gapx 10, alignx left");	//$NON-NLS-1$

	// Citation text fields
		JLabel citeDetail = new JLabel("Citation Detail:");
		citePanel.add(citeDetail, "cell 0 1, alignx right");	//$NON-NLS-1$
		JTextArea citeDetailText = new JTextArea();
		citeDetailText.setWrapStyleWord(true);
		citeDetailText.setLineWrap(true);
		citeDetailText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		citeDetailText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)citeDetailText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		citeDetailText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		citeDetailText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		citeDetailText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane citeDetailTextScroll = new JScrollPane(citeDetailText);
		citeDetailTextScroll.setMinimumSize(new Dimension(350, 75));
		citeDetailTextScroll.getViewport().setOpaque(false);
		citeDetailTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		citeDetailText.setCaretPosition(0);	// set scrollbar to top
		citePanel.add(citeDetailTextScroll, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel citeMemo = new JLabel("Citation Memo:");
		citePanel.add(citeMemo, "cell 0 2, alignx right");	//$NON-NLS-1$
		JTextArea citeMemoText = new JTextArea();
		citeMemoText.setWrapStyleWord(true);
		citeMemoText.setLineWrap(true);
		citeMemoText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		citeMemoText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)citeMemoText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		citeMemoText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		citeMemoText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		citeMemoText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane citeMemoTextScroll = new JScrollPane(citeMemoText);
		citeMemoTextScroll.setMinimumSize(new Dimension(350, 75));
		citeMemoTextScroll.getViewport().setOpaque(false);
		citeMemoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		citeMemoText.setCaretPosition(0);	// set scrollbar to top
		citePanel.add(citeMemoTextScroll, "cell 1 2, alignx left");	//$NON-NLS-1$

		JLabel ref = new JLabel("Reference:");
		citePanel.add(ref, "cell 0 3, alignx right");	//$NON-NLS-1$
		JTextField refText = new JTextField();
		refText.setColumns(20);
		citePanel.add(refText, "cell 1 3, alignx left");	//$NON-NLS-1$

		JLabel sure = new JLabel("Surety:");
		citePanel.add(sure, "cell 0 4, alignx right");	//$NON-NLS-1$
		// Define Surety panel
		JPanel surePanel = new JPanel();
		surePanel.setLayout(new MigLayout("", "[]15[][][][][]", "[][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel sure12PDM = new JLabel("  1    2    P    D    M");
		surePanel.add(sure12PDM, "cell 1 0, alignx left");	//$NON-NLS-1$
		JLabel fidelity = new JLabel("Fidelity:");
		surePanel.add(fidelity, "cell 0 1, alignx right");	//$NON-NLS-1$
		JTextField fid1 = new JTextField();
		fid1.setColumns(1);
		surePanel.add(fid1, "cell 1 1");	//$NON-NLS-1$
		JTextField fid2 = new JTextField();
		fid2.setColumns(1);
		surePanel.add(fid2, "cell 1 1");	//$NON-NLS-1$
		JTextField fidP = new JTextField();
		fidP.setColumns(1);
		surePanel.add(fidP, "cell 1 1");	//$NON-NLS-1$
		JTextField fidD = new JTextField();
		fidD.setColumns(1);
		surePanel.add(fidD, "cell 1 1");	//$NON-NLS-1$
		JTextField fidM = new JTextField();
		fidM.setColumns(1);
		surePanel.add(fidM, "cell 1 1");	//$NON-NLS-1$

		JLabel accuracy = new JLabel("Accuracy:");
		surePanel.add(accuracy, "cell 0 2, alignx right");	//$NON-NLS-1$
		JTextField acc1 = new JTextField();
		acc1.setColumns(1);
		surePanel.add(acc1, "cell 1 2");	//$NON-NLS-1$
		JTextField acc2 = new JTextField();
		acc2.setColumns(1);
		surePanel.add(acc2, "cell 1 2");	//$NON-NLS-1$
		JTextField accP = new JTextField();
		accP.setColumns(1);
		surePanel.add(accP, "cell 1 2");	//$NON-NLS-1$
		JTextField accD = new JTextField();
		accD.setColumns(1);
		surePanel.add(accD, "cell 1 2");	//$NON-NLS-1$
		JTextField accM = new JTextField();
		accM.setColumns(1);
		surePanel.add(accM, "cell 1 2");	//$NON-NLS-1$

		citePanel.add(surePanel, "cell 1 4, alignx left");	//$NON-NLS-1$
		contents.add(citePanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Setup Output panel for display of how citation looks
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		outputPanel.setLayout(new MigLayout("", "[]", "[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel("Full footnote");
		outputPanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		JTextArea fullFootText = new JTextArea();
		fullFootText.setEditable(false);
		fullFootText.setFocusable(false);
		fullFootText.setWrapStyleWord(true);
		fullFootText.setLineWrap(true);
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)fullFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		fullFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setMinimumSize(new Dimension(200, 90));
		fullFootTextScroll.getViewport().setOpaque(false);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullFootText.setCaretPosition(0);	// set scrollbar to top
		outputPanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
		outputPanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		JTextArea shortFootText = new JTextArea();
		shortFootText.setEditable(false);
		shortFootText.setFocusable(false);
		shortFootText.setWrapStyleWord(true);
		shortFootText.setLineWrap(true);
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)shortFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		shortFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setMinimumSize(new Dimension(200, 75));
		shortFootTextScroll.getViewport().setOpaque(false);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		shortFootText.setCaretPosition(0);	// set scrollbar to top
		outputPanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
		outputPanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		JTextArea biblioText = new JTextArea();
		biblioText.setEditable(false);
		biblioText.setFocusable(false);
		biblioText.setWrapStyleWord(true);
		biblioText.setLineWrap(true);
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)biblioText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		biblioText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setMinimumSize(new Dimension(200, 75));
		biblioTextScroll.getViewport().setOpaque(false);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		biblioText.setCaretPosition(0);	// set scrollbar to top
		outputPanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(outputPanel, "cell 1 0,aligny top, hidemode 3"); //$NON-NLS-1$
		outputPanel.setVisible(false);

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Display = new JButton("Visualise");		// Visualise
		btn_Display.setEnabled(false);
		contents.add(btn_Display, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

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

		// Listener for Source Select button
		btn_sorcSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				HG0565ManageSource sorcScreen = new HG0565ManageSource();
				sorcScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySorc = btn_sorcSelect.getLocationOnScreen();
				sorcScreen.setLocation(xySorc.x - 300, xySorc.y - 100);
				sorcScreen.setVisible(true);
				btn_Display.setEnabled(true);
				}
		});

		// Listener for Display button
		btn_Display.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
			// Unhide the Output panel
				outputPanel.setVisible(true);
				outputVisible = true;
				pack();
			// NOTE03 needs action code here to parse the Citation text
			// into the Source templates and display it in the new output panel
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0565ManageSource");	//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0555EditCitation constructor

}  // End of HG0555EditCitation

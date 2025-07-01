package hre.gui;
/**************************************************************************************
 * EditSourceType -
 * ***********************************************************************************
 * v0.04.0032 2025-01-31 Original draft (D Ferguson)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 action Save button
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
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
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBProjectOpenData;
import net.miginfocom.swing.MigLayout;

/**
 * Edit SourceType
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-31
 */

public class HG0568EditSourceType extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "56800"; //$NON-NLS-1$
	private JPanel contents;
	JTextArea fullFootText, shortFootText, biblioText, remindText;

/**
 * Create the dialog
 */
	public HG0568EditSourceType(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;

		setTitle("Edit Source Type");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsourcetype";		 //$NON-NLS-1$

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0568EditSourceType");	//$NON-NLS-1$

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

		JLabel sourceLabel = new JLabel("Source Type");
		contents.add(sourceLabel, "cell 0 0, alignx right");	//$NON-NLS-1$
		JTextField sourceType = new JTextField("Source Type");
		sourceType.setColumns(30);
		sourceType.setEditable(false);
		contents.add(sourceType, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel fullFoot = new JLabel("Full footnote");
		contents.add(fullFoot, "cell 0 1, alignx right");	//$NON-NLS-1$
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
		fullFootTextScroll.setMinimumSize(new Dimension(300, 50));
		fullFootTextScroll.getViewport().setOpaque(false);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullFootText.setCaretPosition(0);	// set scrollbar to top
		contents.add(fullFootTextScroll, "cell 1 1, grow, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
		contents.add(shortFoot, "cell 0 2, alignx right");	//$NON-NLS-1$
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
		shortFootTextScroll.setMinimumSize(new Dimension(300, 50));
		shortFootTextScroll.getViewport().setOpaque(false);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		shortFootText.setCaretPosition(0);	// set scrollbar to top
		contents.add(shortFootTextScroll, "cell 1 2, grow, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
		contents.add(biblio, "cell 0 3, alignx right");	//$NON-NLS-1$
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
		biblioTextScroll.setMinimumSize(new Dimension(300, 50));
		biblioTextScroll.getViewport().setOpaque(false);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		biblioText.setCaretPosition(0);	// set scrollbar to top
		contents.add(biblioTextScroll, "cell 1 3, grow, alignx left");	//$NON-NLS-1$

		JLabel remind = new JLabel("Reminder");
		contents.add(remind, "cell 0 4, alignx right");	//$NON-NLS-1$
		remindText = new JTextArea();
		remindText.setWrapStyleWord(true);
		remindText.setLineWrap(true);
		remindText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		remindText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)remindText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		remindText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		remindText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		remindText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane remindTextScroll = new JScrollPane(remindText);
		remindTextScroll.setMinimumSize(new Dimension(300, 50));
		remindTextScroll.getViewport().setOpaque(false);
		remindTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		remindText.setCaretPosition(0);	// set scrollbar to top
		contents.add(remindTextScroll, "cell 1 4, grow, alignx left");	//$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 6, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Savet");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 6, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

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

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE01 save all data

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit of HG0568EditSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0568EditSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0568EditSourceType constructor

}  // End of HG0568EditSourceType

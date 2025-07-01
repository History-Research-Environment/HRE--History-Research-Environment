package hre.gui;
/**************************************************************************************
 * Edit Sentence
 * ***********************************************************************************
 * v0.04.0032 2025-06-25 Original draft (D Ferguson)
 *
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 rolename loading to be done
 * NOTE02 sentence loading not done
 * NOTE03 sentence saving after edit not done
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import net.miginfocom.swing.MigLayout;


/**
 * Edit Citation
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-06-25
 */

public class HG0548EditSentence extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBWhereWhenHandler pointWhereWhenHandler;
	HG0547EditEvent pointEditEvent;

	public static final String screenID = "54800"; //$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	private JPanel contents;

	JTextArea sentenceText, previewText;
	DocumentListener sentenceEditListen;
	boolean sentenceChanged = false;
	String sentenceTemplate = "";

	String[] eventRoleList;

/**
 * Create the dialog
 */
	public HG0548EditSentence(HBProjectOpenData pointOpenProject, int eventNumber, HG0547EditEvent pointEditEvent)  {
		this.pointEditEvent = pointEditEvent;
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsentence";		 //$NON-NLS-1$
		setTitle("Sentence Editor");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0548EditSentence");	//$NON-NLS-1$

		// Get the list of Roles for this eventtype  PROBLEM HERE! - ONLY EVER RETURNS PRINCIPAL!
		try {
			eventRoleList = pointWhereWhenHandler.getEventRoleList(eventNumber, "");
		} catch (HBException e) {
			System.out.println("HG0548EditSentence role load error: " + e.getMessage());	//$NON-NLS-1$
			e.printStackTrace();
		}

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

		JLabel role = new JLabel("Select Role");		// Select Role:
		contents.add(role, "cell 0 0, alignx left");		//$NON-NLS-1$

		JComboBox<String> comboRoleNames = new JComboBox<String>();
		contents.add(comboRoleNames, "cell 0 0, gap 10");		//$NON-NLS-1$
		for (int i = 0; i < eventRoleList.length; i++){
	       comboRoleNames.addItem(eventRoleList[i]);
	    }
		comboRoleNames.setSelectedIndex(0);		// set at start

		//***********************************************************************************
		// NOTE01 -Load all GLOBALsentences for these roles.
		// Then load all LOCAL sentences (if any) for this event.
		// NOTE02 - Then for 1st role in list, load its sentence (LOCAL, if exists, otherwise the GLOBAL one)
		// into sentenceText, and set the 'default' JLabel as visible (if GLOBAL) or not (if LOCAL).
		//*******************************************************************************

	// Setup panel with labels and text areas
		JLabel sentence = new JLabel("Sentence template: ");	// Sentence template:
		contents.add(sentence, "cell 0 1, alignx left");		//$NON-NLS-1$
		JLabel lbl_default = new JLabel("(default):");				// (default):
		contents.add(lbl_default, "cell 0 1, alignx left");		//$NON-NLS-1$

		sentenceText = new JTextArea();
//		 enter the 1st role's sentence in here to start with  );
		sentenceText.setWrapStyleWord(true);
		sentenceText.setLineWrap(true);
		sentenceText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		sentenceText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)sentenceText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		sentenceText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		sentenceText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		sentenceText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane sentenceTextScroll = new JScrollPane(sentenceText);
		sentenceTextScroll.setMinimumSize(new Dimension(400, 75));
		sentenceTextScroll.getViewport().setOpaque(false);
		sentenceTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		sentenceText.setCaretPosition(0);	// set scrollbar to top
		contents.add(sentenceTextScroll, "cell 0 2, alignx left");	//$NON-NLS-1$

		JLabel lbl_Preview = new JLabel("Preview of output sentence:");		// Preview
		contents.add(lbl_Preview, "cell 0 3, alignx left"); //$NON-NLS-1$

		previewText = new JTextArea("       Sentence previews not yet implemented");
		previewText.setEditable(false);
		previewText.setFocusable(false);
		previewText.setWrapStyleWord(true);
		previewText.setLineWrap(true);
		previewText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		previewText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane previewTextScroll = new JScrollPane(previewText);
		previewTextScroll.setMinimumSize(new Dimension(400, 75));
		previewTextScroll.getViewport().setOpaque(false);
		previewTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		previewText.setCaretPosition(0);	// set scrollbar to top
		contents.add(previewTextScroll, "cell 0 4, alignx left");	//$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 5, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");			// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 5, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

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

		// Listener for sentence textarea
		sentenceEditListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {

            	 // for every edit, if the edited sentence matches the event's default role sentence,
            	// enable the word "(defualt)" in the 'lbl_default field
            	// if it doesn't match, diable the default field
            	sentenceChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        sentenceText.getDocument().addDocumentListener(sentenceEditListen);

		// Listener for roleName combobox			// <<<<<<--  NOTE02 - get the sentence for this role from the DB here
        comboRoleNames.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// int roleEntry = comboRoleNames.getSelectedIndex();
				// NOTE01 - now get the sentence for this rolename and replace contents of sentenceText

			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

			// NOTE03 - perform Save of the edited sentence text for the current role as a LOCAL sentence

				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit HG0548EditSentence");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0548EditSentence");	//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0548EditSentence constructor

}  // End of HG0548EditSentence

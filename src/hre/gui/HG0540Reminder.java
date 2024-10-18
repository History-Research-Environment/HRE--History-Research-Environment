package hre.gui;
/************************************************************************
/* Reminder - Specification 05.40 GUI_Reminder 2018-06-20
 * v0.00.0007 2019-02-10 by R Thompson, updated 2019-07-19 by D Ferguson
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-11-29 changed code for HG0514 call (D Ferguson)
 * v0.00.0022 2020-07-22 changed to MigLayout and JTextPane (D Ferguson)
 *            2020-07-28 added basic text editing capability (D Ferguson)
 * 			  2020-07-29 common HG0450SuperDialog for all JDialog windows
 * v0.01.0023 2020-08-04 constructor (old) and for HG0450 (new) (N.Tolleshaug)
 *   		  2020-09-01 Implemented read/write reminder to database(N. Tolleshaug)
 *   		  2020-09-03 Implemented for HG0451SuperIntFrame (N. Tolleshaug)
 *   		  2020-09-03 Implemented for HG0452SuperFrame (N. Tolleshaug)
 *   		  2020-09-07 Remove nl key; make CR save <br> HTML (D Ferguson)
 *   		  2020-09-15 Reminder implemented for all SuperXxx's (N. Tolleshaug)
 * v0.01.0027 2022-02-24 NLS converted (D Ferguson)
 ********************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import hre.bila.HB0614Help;
import hre.bila.HB0711Logging;
import hre.nls.HG0540Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Reminder display and editor
 * @author R Thompson, D Ferguson from code by Charles Bell, May 27, 2002 (HTMLEditor)
 * @version v0.01.0027
 * @since 2019-02-10
 */

public class HG0540Reminder extends JDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "54000"; //$NON-NLS-1$
	private HTMLDocument document;
	private JPanel contents;
	private JTextPane textPane = new JTextPane();
	private HG0450SuperDialog pointDisplay;
	private HG0451SuperIntFrame pointIntFrame;
	private HG0452SuperFrame pointFrame;

	private Action boldAction = new StyledEditorKit.BoldAction();
	private Action underlineAction = new StyledEditorKit.UnderlineAction();
	private Action italicAction = new StyledEditorKit.ItalicAction();
	private Action listAction = new HTMLEditorKit.InsertHTMLTextAction("List", "<ul> </ul>", HTML.Tag.BODY, HTML.Tag.UL); //$NON-NLS-1$ //$NON-NLS-2$

	private String NEW_LINE = "new-line"; //$NON-NLS-1$

/**
 * Standard Reminder without super class
 * @param screenID
 * @wbp.parser.constructor
 */
	public HG0540Reminder(String screenID){

		buildScreen();

	}	// End HG0540Reminder constructor

/**
 * Get any existing Reminder and setup the screen, for HG0450SuperDialog
 * @param screenID
 * @param pointDisplay
 */
	public HG0540Reminder(String screenID, HG0450SuperDialog pointDisplay) {
		this.pointDisplay = pointDisplay;
		buildScreen();
	}	// End HG0540Reminder constructor

/**
 * Get any existing Reminder and setup the screen, for HG0451SuperIntFrame
 * @param screenID
 * @param pointIntFrame
 */
	public HG0540Reminder(String screenID, HG0451SuperIntFrame pointIntFrame) {
		this.pointIntFrame = pointIntFrame;
		buildScreen();
	}	// End HG0540Reminder constructor

/**
 * Get any existing Reminder and setup the screen, for HG0452SuperFrame
 * @param screenID
 * @param pointFrame
 */
	public HG0540Reminder(String screenID, HG0452SuperFrame pointFrame) {
		this.pointFrame = pointFrame;
		buildScreen();
	}	// End HG0540Reminder constructor

/**
 * Create the screen
 */
	public void buildScreen() {
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0540 Reminder");} //$NON-NLS-1$
		Window windowInstance = this;
		setTitle(HG0540Msgs.Text_5);			// Reminder
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 3", "[grow]", "[grow]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(200, 24));
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton boldButton = new JButton(boldAction);
		boldButton.setVisible(false);
		boldButton.setPreferredSize(new Dimension(24, 24));
		boldButton.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		boldButton.setToolTipText(HG0540Msgs.Text_9);
		boldButton.setFont(new Font("Arial", Font.BOLD, 16)); //$NON-NLS-1$
		boldButton.setText("B"); //$NON-NLS-1$
		toolBar.add(boldButton);

		JButton italButton = new JButton(italicAction);
		italButton.setVisible(false);
		italButton.setPreferredSize(new Dimension(24, 24));
		italButton.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		italButton.setToolTipText(HG0540Msgs.Text_12);
		italButton.setFont(new Font("Arial", Font.ITALIC, 16)); //$NON-NLS-1$
		italButton.setText("i"); //$NON-NLS-1$
		toolBar.add(italButton);

		JButton underButton = new JButton(underlineAction);
		underButton.setVisible(false);
		underButton.setPreferredSize(new Dimension(24, 24));
		underButton.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		underButton.setToolTipText(HG0540Msgs.Text_15);
		underButton.setFont(new Font("Arial", Font.PLAIN, 16)); //$NON-NLS-1$
		underButton.setText("u");	 //$NON-NLS-1$
		toolBar.add(underButton);

		toolBar.add(Box.createRigidArea(new Dimension(10,24)));

		JButton listButton = new JButton(listAction);
		listButton.setVisible(false);
		listButton.setPreferredSize(new Dimension(28, 24));
		listButton.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		listButton.setToolTipText(HG0540Msgs.Text_18);
		listButton.setFont(new Font("Arial", Font.PLAIN, 16)); //$NON-NLS-1$
		listButton.setText("list"); //$NON-NLS-1$
		toolBar.add(listButton);

		//Toolbar-add glue, so icons are pushed to the right
		toolBar.add(Box.createHorizontalGlue());

		JButton btn_Helpicon = new JButton();
		btn_Helpicon.setPreferredSize(new Dimension(24, 24));
		btn_Helpicon.setToolTipText(HG0540Msgs.Text_21);
		toolBar.add(btn_Helpicon);
		btn_Helpicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/help_BW_24.png"))); //$NON-NLS-1$

		contents.add(toolBar, "north"); //$NON-NLS-1$

		// Setup HTML Editor and textPane as HTML
		HTMLEditorKit editorKit = new HTMLEditorKit();
		document = (HTMLDocument)editorKit.createDefaultDocument();
		textPane = new JTextPane(document);
		textPane.setEditorKit(editorKit);
		textPane.setContentType("text/html"); //$NON-NLS-1$
		// NB: preferred size of JTextPane effectively sets the initial screen size
		textPane.setPreferredSize(new Dimension(400, 100));
		textPane.setEnabled(false);
		textPane.setEditable(false);
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		DefaultCaret caret = (DefaultCaret)textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(textPane);

		// Setup key map to recognise Enter key
		InputMap input = textPane.getInputMap();
	    KeyStroke enter = KeyStroke.getKeyStroke("ENTER"); //$NON-NLS-1$
	    input.put(enter, NEW_LINE);
	    ActionMap actions = textPane.getActionMap();

	    contents.add(scrollPane, "cell 0 0, grow"); //$NON-NLS-1$

		// Define buttons in bottom row, right-aligned
		JButton btn_Save = new JButton(HG0540Msgs.Text_27);			// Save
		btn_Save.setEnabled(false);
		btn_Save.setToolTipText(HG0540Msgs.Text_28);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 20"); //$NON-NLS-1$

		JButton btn_Edit = new JButton(HG0540Msgs.Text_30);			// Edit
		btn_Edit.setToolTipText(HG0540Msgs.Text_31);
		contents.add(btn_Edit, "cell 0 1, alignx right, gapx 20"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0540Msgs.Text_33);		// Edit
		btn_Close.setToolTipText(HG0540Msgs.Text_34);
		contents.add(btn_Close, "cell 0 1, alignx right, gapx 20"); //$NON-NLS-1$

		pack();

/**
 * CREATE ACTION BUTTON LISTENERS
 **/
		//icons
		btn_Helpicon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HB0614Help.contextHelp(windowInstance, btn_Helpicon, screenID+"reminder");		 //$NON-NLS-1$
			}
		});

		//buttons
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// The HTML coded version is obtained by getting textPane.getText();
				if (pointIntFrame != null)
					pointIntFrame.storeReminderText(textPane.getText());
				else if (pointDisplay != null)
					pointDisplay.storeReminderText(textPane.getText());
				else if (pointFrame != null)
					pointFrame.storeReminderText(textPane.getText());
				else if (HGlobal.DEBUG) System.out.println("storeReminderText - Reminder store not implemented"); //$NON-NLS-1$
				if (HGlobal.DEBUG) System.out.println(textPane.getText());
			}
		});

		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				textPane.setEditable(true);
				textPane.setEnabled(true);
				btn_Save.setEnabled(true);
				boldButton.setVisible(true);
				underButton.setVisible(true);
				italButton.setVisible(true);
				listButton.setVisible(true);
			}
		});

		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				if (pointIntFrame != null)
					pointIntFrame.reminderDisplay = null;
				else if (pointDisplay != null)
					pointDisplay.reminderDisplay = null;
				else if (pointFrame != null)
					pointFrame.reminderDisplay = null;

				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HG0540 Reminder");} //$NON-NLS-1$
				//dispose();
			}
		});

		// Listener for clicking 'X' on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});

		// Action code for 'Enter' key (CR) to create line-break
		actions.put(NEW_LINE, new AbstractAction() {
			private static final long serialVersionUID = 001L;
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            try {
	                editorKit.insertHTML((HTMLDocument)textPane.getDocument(), textPane.getCaretPosition(),
	                        "<br>", 0,0, HTML.Tag.BR); //$NON-NLS-1$
	                textPane.setCaretPosition(textPane.getCaretPosition()); // This moves caret to next line
	            } catch (BadLocationException | IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	    });

		// Set default Reminder text, then get any stored text
		String reminderDefault = HG0540Msgs.Text_0; 	// Default prompt message to click Edit to enter Reminder text
		String reminder = null;
		if (pointIntFrame != null)
			reminder = pointIntFrame.readReminderText();
		else if (pointFrame != null)
			reminder = pointFrame.readReminderText();
		else if (pointDisplay != null)
			reminder = pointDisplay.readReminderText();
		else if (HGlobal.DEBUG) System.out.println("readReminderText - Reminder store not implemented"); //$NON-NLS-1$
		// If returned text is empty, use the Default string
		if (reminder == null || reminder.isEmpty()) textPane.setText(reminderDefault);
			else textPane.setText(reminder);
	}	// End buildScreen

}		// End HG0540Reminder
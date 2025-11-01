package hre.gui;
/**************************************************************************************
 * EditSourceType -
 * ***********************************************************************************
 * v0.04.0032 2025-01-31 Original draft (D Ferguson)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 * 			  2025-09-27 Load the Source templates converted to readable Element names (D Ferguson)
 * 			  2025-10-17 When add new type, create empty data for adding text (N. Tolleshaug)
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
import java.util.HashMap;
import java.util.Map;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
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
	long null_RPID  = 1999999999999999L;
	public HBCitationSourceHandler pointCitationSourceHandler;

	public static final String screenID = "56800"; //$NON-NLS-1$
	private JPanel contents;
	Object[] sourceDefnData = null;
	String[][] tableSrcElmntData;
	JTextArea fullFootText, shortFootText, biblioText, remindText;
	DocumentListener fullFootChange, shortFootChange, biblioChange, remindChange;
	boolean fullFootEdited = false, shortFootEdited = false, biblioEdited = false, remindEdited = false;
	long sorcDefnPID;

/**
 * Create the dialog
 */
	public HG0568EditSourceType(HBProjectOpenData pointOpenProject, long sorcDefnPID)  {
		this.pointOpenProject = pointOpenProject;
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		this.sorcDefnPID = sorcDefnPID;

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
		JTextField sourceDefnName = new JTextField();
		sourceDefnName.setColumns(30);
		sourceDefnName.setEditable(false);
		contents.add(sourceDefnName, "cell 1 0, alignx left");	//$NON-NLS-1$

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
		shortFootTextScroll.setMinimumSize(new Dimension(350, 50));
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
		biblioTextScroll.setMinimumSize(new Dimension(350, 50));
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
		remindTextScroll.setMinimumSize(new Dimension(350, 50));
		remindTextScroll.getViewport().setOpaque(false);
		remindTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		remindText.setCaretPosition(0);	// set scrollbar to top
		contents.add(remindTextScroll, "cell 1 4, grow, alignx left");	//$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 6, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 6, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

	// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			System.out.println( " Error loading Source Element list: " + hbe.getMessage());
			hbe.printStackTrace();
		}
	    // Then construct a lookup for "12345" → "[element text]" conversion
        Map<String, String> codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }
        // Then construct a lookup for "[text here]" → "12345" conversion back
        Map<String, String> textToCodeMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2) {
                String trimmedText = row[0].trim();
                textToCodeMap.put(trimmedText, row[1]);
            }
        }

	// Get the data for this Source Defn, then load into the screen
    // if add with no sourceDefPID,  empty sourceDefnData  must be created
		try {
			if (sorcDefnPID != null_RPID) 
				sourceDefnData = pointCitationSourceHandler.getSourceDefnEditData(sorcDefnPID);
			else {
				sourceDefnData = new String[7];
				for (int i = 0; i < sourceDefnData.length; i++)
					sourceDefnData[i] = "";	
			}
		} catch (HBException hbe) {
			System.out.println( " Error loading Source Defn data: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		sourceDefnName.setText(" " + (String)sourceDefnData[0]);	//$NON-NLS-1$
		// Get the source templates and convert the Element [nnnnn] entries into
		// Element Names via the hashmap codeToTextMap and load into the text areas for display.
		// Start with the Full footer
		fullFootText.setText(HGlobalCode.convertNumsToNames((String)sourceDefnData[3], codeToTextMap));
		// then the Short footer
		shortFootText.setText(HGlobalCode.convertNumsToNames((String)sourceDefnData[4], codeToTextMap));
		// then the Bibliography template
		biblioText.setText(HGlobalCode.convertNumsToNames((String)sourceDefnData[5], codeToTextMap));
		// and the Reminder text
		remindText.setText((String)sourceDefnData[6]);

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

		// Listener for edit of fullFoot text
		fullFootChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				fullFootEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				fullFootEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				fullFootEdited = true;
				btn_Save.setEnabled(true);
			}
		};
		fullFootText.getDocument().addDocumentListener(fullFootChange);

		// Listener for edit of shortFoot text
		shortFootChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				shortFootEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				shortFootEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				shortFootEdited = true;
				btn_Save.setEnabled(true);
			}
		};
		shortFootText.getDocument().addDocumentListener(shortFootChange);

		// Listener for edit of biblio text
		biblioChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				biblioEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				biblioEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				biblioEdited = true;
				btn_Save.setEnabled(true);
			}
		};
		biblioText.getDocument().addDocumentListener(biblioChange);

		// Listener for edit of remind text
		remindChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				remindEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				remindEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				remindEdited = true;
				btn_Save.setEnabled(true);
			}
		};
		remindText.getDocument().addDocumentListener(remindChange);

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE01 save any changed data by checking the xxxEdited booleans

				// When saving source templates use the convertNamesToNums routine with the textToCodeMap hashmap
				// to check all Element names exist and can be converted back to Element numbers, and do the
				// conversion back to the numbered version for saving.
				// It throws error msg and returns null, so if null returned, do not save!
				// Example usage using fullFoot text, with before/after console routines:
				//    System.out.println("Input="+fullFootText.getText());
				//    String fullFootToSave = HGlobalCode.convertNamesToNums(fullFootText.getText(), textToCodeMap);
				//    System.out.println("Output="+fullFootToSave);
				// then check for null response and do NOT dispose! (leave user to fix it and try again!)

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit of HG0568EditSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

				// need to check for unsaved data...

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0568EditSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0568EditSourceType constructor

}  // End of HG0568EditSourceType

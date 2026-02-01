package hre.gui;
/**************************************************************************************
 * EditSourceType -
 * ***********************************************************************************
 * v0.04.0032 2025-01-31 Original draft (D Ferguson)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 * 			  2025-09-27 Load the Source templates converted to readable Element names (D Ferguson)
 * 			  2025-10-17 When add new type, create empty data for adding text (N. Tolleshaug)
 * 			  2025-11-17 Change HGlobalCode routines to be in ReportHandler (D Ferguson)
 *			  2025-12-22 Save edited source type implemented (N. Tolleshaug)
 *			  2025-12-28 Set Save button enabled if this is a Copy (D Ferguson)
 *						 Added Unsaved chnages warning to Cancel button (D Ferguson)
 *			  2025-12-29 NLS all code to this point (D Ferguson)
 *			  2025-12-31 Updated language setting for add source definition (N. Tolleshaug)
 *			  2026-01-06 Log catch block msgs (D Ferguson)
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
import javax.swing.JOptionPane;
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
import hre.bila.HBReportHandler;
import hre.nls.HG0567Msgs;
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
	HBReportHandler pointReportHandler;

	public static final String screenID = "56800"; //$NON-NLS-1$
	boolean editSourceType, copySourceType;
	private JPanel contents;
	String sourceDefinLanguage = HGlobal.dataLanguage;
	Object[] sourceDefnData = null;
	Object[] sourceTypeStoreData;
	String[][] tableSrcElmntData;
	JTextField sourceDefnName;
	JTextArea fullFootText, shortFootText, biblioText, remindText;
	DocumentListener sourceTypeNameCahnge, fullFootChange, shortFootChange, biblioChange, remindChange;
	String fullFootToSave, shortFootToSave, biblioFootToSave;
	boolean typeNameEdited = false, fullFootEdited = false,
			shortFootEdited = false, biblioEdited = false, remindEdited = false;
	long sourceDefnTablePID;
	int sourceDefType = 0;

	JButton btn_Save;

	Map<String, String> codeToTextMap;
	Map<String, String> textToCodeMap;

/**
 * Create the dialog
 */
	public HG0568EditSourceType(HBProjectOpenData pointOpenProject,
								boolean editSourceType,
								boolean copySourceType,
								long defnTablePID)  {
		this.pointOpenProject = pointOpenProject;
		this.editSourceType = editSourceType;
		this.copySourceType = copySourceType;
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointReportHandler = pointOpenProject.getReportHandler();
		this.sourceDefnTablePID = defnTablePID;

		if (editSourceType) setTitle(HG0567Msgs.Text_20);		// Edit Source Type
		else if (copySourceType) setTitle(HG0567Msgs.Text_21);	// Copy Source Type
		else setTitle(HG0567Msgs.Text_22);						// Add Source Type

		// Setup references for HG0450
		windowID = screenID;
		helpName = "editsourcetype";	//$NON-NLS-1$

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

		JLabel sourceLabel = new JLabel(HG0567Msgs.Text_23);		// Source Type Name
		contents.add(sourceLabel, "cell 0 0, alignx right");	//$NON-NLS-1$
		sourceDefnName = new JTextField();
		sourceDefnName.setColumns(30);
		//sourceDefnName.setEditable(false);
		sourceDefnName.setEditable(true);
		contents.add(sourceDefnName, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel fullFoot = new JLabel(HG0567Msgs.Text_7);		// Full footnote
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

		JLabel shortFoot = new JLabel(HG0567Msgs.Text_8);		// Short footnote
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

		JLabel biblio = new JLabel(HG0567Msgs.Text_9);		// Bibliography
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

		JLabel remind = new JLabel(HG0567Msgs.Text_24);		// Reminder
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
		JButton btn_Cancel = new JButton(HG0567Msgs.Text_10);		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 6, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		btn_Save = new JButton(HG0567Msgs.Text_25);		// Save
		if (copySourceType) btn_Save.setEnabled(true);
		else btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 6, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

	// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0568 loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	    // Then construct a lookup for "12345" → "[element text]" conversion
        codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }
        // Then construct a lookup for "[text here]" → "12345" conversion back
        textToCodeMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2) {
                String trimmedText = row[0].trim();
                textToCodeMap.put(trimmedText, row[1]);
            }
        }

	// Get the data for this Source Defn, then load into the screen
    // if add with no sourceDefPID,  empty sourceDefnData  must be created
		try {
			if (sourceDefnTablePID != null_RPID)
				sourceDefnData = pointCitationSourceHandler.getSourceDefnEditData(sourceDefnTablePID);
			else {
				sourceDefnData = new String[7];
			// Set add source type default values for sourceDefnData
				sourceDefnData[0] = HG0567Msgs.Text_34;		// New Source Type Name
				sourceDefnData[1] = "";		//$NON-NLS-1$
				sourceDefnData[2] = "";		//$NON-NLS-1$
				sourceDefnData[3] = HG0567Msgs.Text_35;		// [CD] or [DC] if French
				sourceDefnData[4] = HG0567Msgs.Text_35; 	// [CD] or [DC] if French
				sourceDefnData[5] = HG0567Msgs.Text_35; 	// [CD] or [DC] if French
				sourceDefnData[6] = "";		//$NON-NLS-1$
			}
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0568 loading Source Defns: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		if (copySourceType)
			sourceDefnName.setText(HG0567Msgs.Text_26 + (String)sourceDefnData[0]);		//  COPY -
		else sourceDefnName.setText(" " + (String)sourceDefnData[0]);	//$NON-NLS-1$
	// Get the source templates and convert the Element [nnnnn] entries into
	// Element Names via the hashmap codeToTextMap and load into the text areas for display.
	// Start with the Full footer
		fullFootText.setText(pointReportHandler.convertNumsToNames((String)sourceDefnData[3], codeToTextMap));
	// then the Short footer
		shortFootText.setText(pointReportHandler.convertNumsToNames((String)sourceDefnData[4], codeToTextMap));
	// then the Bibliography template
		biblioText.setText(pointReportHandler.convertNumsToNames((String)sourceDefnData[5], codeToTextMap));
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

	// Listener for edit of source type name text
		sourceTypeNameCahnge = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				typeNameEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				typeNameEdited = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				typeNameEdited = true;
				btn_Save.setEnabled(true);
			}
		};
		sourceDefnName.getDocument().addDocumentListener(sourceTypeNameCahnge);

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

	// Listener for edit of reminder text
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
				// Save any changed data by checking the xxxEdited booleans
				// When saving source templates use the convertNamesToNums routine with the textToCodeMap hashmap
				// to check all Element names exist and can be converted back to Element numbers, and do the
				// conversion back to the numbered version for saving.
				// If error throw new HBExcption with element name error
				boolean enableDispose = false;
				if (!evaluateFootNotes()) return;
				enableDispose = true;
				try {
					if (copySourceType)
						pointCitationSourceHandler.createSourceDefRecord(storeSourceDefData());
					if (editSourceType)
						pointCitationSourceHandler.updateSourceDefRecord(sourceDefnTablePID, storeSourceDefData());
					if (!copySourceType && !editSourceType)
						pointCitationSourceHandler.createSourceDefRecord(storeSourceDefData());
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0568 saving Source Defn: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit of HG0568EditSourceType");	//$NON-NLS-1$
				if (enableDispose) dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check for unsaved changes before exit
				if (btn_Save.isEnabled())
					if (JOptionPane.showConfirmDialog (btn_Save,
							HG0567Msgs.Text_27			// There are unsaved changes. \n
							+ HG0567Msgs.Text_28,		// Do you still wish to exit?
							getTitle(),					// (use dialog title as msg title)
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						// YES option
						if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: closing without saving from HG0568EditSourceType"); //$NON-NLS-1$
						// Clear Reminder if present
						if (reminderDisplay != null) reminderDisplay.dispose();
						dispose();
						}
						else; // NO option - do nothing
				else { // btn_Save not enabled - exit
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: closing HG0568EditSourceType"); //$NON-NLS-1$
					// Clear Reminder if present
					if (reminderDisplay != null) reminderDisplay.dispose();
					dispose();
				}
			}
		});

	}	// End HG0568EditSourceType constructor

/**
 * private boolean testFootNotes()
 * @return
 */
	private boolean evaluateFootNotes() {
		boolean testOK = true;
		String footTextType = "";		//$NON-NLS-1$
		for (int ix = 1; ix < 4; ix++)
			try {
				if (ix == 1) {
					footTextType = HG0567Msgs.Text_29;		//  in Full footnote
					fullFootToSave = pointReportHandler.convertNamesToNums(fullFootText.getText(), textToCodeMap);
				}
				if (ix == 2) {
					footTextType = HG0567Msgs.Text_30;		//  in Short footnote
					shortFootToSave = pointReportHandler.convertNamesToNums(shortFootText.getText(), textToCodeMap);
				}
				if (ix == 3) {
					footTextType = HG0567Msgs.Text_31;		//  in Bibliography
					biblioFootToSave = pointReportHandler.convertNamesToNums(biblioText.getText(), textToCodeMap);
				}
			} catch (HBException hbe) {
		        JOptionPane.showMessageDialog(btn_Save,
		        		HG0567Msgs.Text_32			// Unknown Source Element Name \n
		        		+ hbe.getMessage()			// (shows the name)
		        		+ footTextType,				// in Full/Short/Bibliography
		        		HG0567Msgs.Text_33,			// Source Element Name error
		                JOptionPane.ERROR_MESSAGE);
		        testOK = false;

			}
		return testOK;
	}

/**
 * private Object[] storeData()
 * @return
 */
	private Object[] storeSourceDefData() {
		sourceTypeStoreData = new Object[7];
		sourceTypeStoreData[0] = sourceDefnName.getText().trim();
		sourceTypeStoreData[1] = sourceDefnData[1];
		if (!copySourceType && !editSourceType)
			sourceTypeStoreData[2] = sourceDefinLanguage;
		else sourceTypeStoreData[2] = sourceDefnData[2];
		sourceTypeStoreData[3] = fullFootToSave;
		sourceTypeStoreData[4] = shortFootToSave;
		sourceTypeStoreData[5] = biblioFootToSave;
		sourceTypeStoreData[6] = remindText.getText().trim();
		return sourceTypeStoreData;
	}

}  // End of HG0568EditSourceType

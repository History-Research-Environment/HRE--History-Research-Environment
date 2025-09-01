package hre.gui;
/**************************************************************************************
 * Edit Sentence
 * ***********************************************************************************
 * v0.04.0032 2025-06-25 Original draft (D Ferguson)
 *			  2025-07-02 Loading sentences from T168 (N. Tolleshaug)
 *			  2025-07-06 Set role combobox based on passed roleName (D Ferguson)
 *			  2025-07-07 Add display of current data language (D Ferguson)
 *			  2025-07-10 Display sentence with roleNumbers changed to roleNames (D Ferguson)
 *			  2025-07-11 Convert sentence to internal format (roleNumbers) for Save (D Ferguson)
 *			  2025-07-13 Handle TMG male/female sentence structures (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE03 sentence saving after edit
 * NOTE04 sentence preview
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	int dataBaseIndex;
	int eventNumber;

	public static final String screenID = "54800"; //$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	private JPanel contents;

	JTextArea sentenceTextArea, previewTextArea;
	JLabel lbl_Preview;
	JButton btn_Save;
	DocumentListener sentenceEditListen;
	boolean sentenceChanged = false;
	boolean multiSexSentences = false;
	String sexCode;
	String roleSentence = "";	//$NON-NLS-1$
	String[] sexSentences;
	String workSentence = "";	//$NON-NLS-1$
	String editedSentence = ""; //$NON-NLS-1$
	String sentenceToSave = "";	//$NON-NLS-1$

	String[] eventRoleNames;
	int[] eventRoleNumbers;
	String[] usRoleNames;
	int[] usRoleNumbers;
	JComboBox<String> comboRoleNames;
	int currentComboIndex = 0;
	String roleName =" ";	//$NON-NLS-1$
	String displayLanguage ="";	//$NON-NLS-1$
	String lang_code = HGlobal.dataLanguage;
	boolean showWarning = false;

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0548EditSentence(HBProjectOpenData pointOpenProject, HG0547EditEvent pointEditEvent,
								int eventNumber, String roleName, String sexCode)  {
		this.pointEditEvent = pointEditEvent;
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		this.roleName = roleName;
		this.eventNumber = eventNumber;
		this.sexCode = sexCode;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsentence";		 //$NON-NLS-1$
		setTitle("Sentence Editor");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0548EditSentence");	//$NON-NLS-1$

		// Get the lists of Roles and their reference Numbers for this eventNumber
		try {
			eventRoleNames = pointWhereWhenHandler.getEventRoleList(eventNumber, "");	//$NON-NLS-1$
			eventRoleNumbers = pointWhereWhenHandler.getEventRoleTypes();
			// Above gets the data for the current language, but we also need to get the Eng(US) versions
			// To do this, temporarily set the global datalanguage to ENG(US) and then restore it
			String tempLang = HGlobal.dataLanguage;
			HGlobal.dataLanguage = "en-US";	//$NON-NLS-1$
			usRoleNames = pointWhereWhenHandler.getEventRoleList(eventNumber, "");	//$NON-NLS-1$
			usRoleNumbers = pointWhereWhenHandler.getEventRoleTypes();
			HGlobal.dataLanguage = tempLang;
		} catch (HBException e) {
			System.out.println("HG0548EditSentence role load error: " + e.getMessage());	//$NON-NLS-1$
			e.printStackTrace();
		}

		// Get the proper language name of current HGlobal dataLanguage code
		for (int i=0; i < HG0501AppSettings.dataReptLangCodes.length; i++) {
			if (HGlobal.dataLanguage.equals(HG0501AppSettings.dataReptLangCodes[i]))
					displayLanguage = HG0501AppSettings.dataReptLanguages[i];
		}

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]40[]", "[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		// Load the combobox with the rolenames and set the selected one to match roleName parameter
		comboRoleNames = new JComboBox<String>();
		contents.add(comboRoleNames, "cell 0 0, gap 10");		//$NON-NLS-1$
		for (int i = 0; i < eventRoleNames.length; i++){
	       comboRoleNames.addItem(eventRoleNames[i]);
	       if (roleName.equals(eventRoleNames[i])) comboRoleNames.setSelectedIndex(i);
	    }
		// And save the combobox setting
		currentComboIndex = comboRoleNames.getSelectedIndex();

		JLabel lbl_lang = new JLabel("Language is set to: ");		// Language is set to:
		contents.add(lbl_lang, "cell 1 0, alignx left");		//$NON-NLS-1$
		JLabel langCode = new JLabel(displayLanguage);
		contents.add(langCode, "cell 1 0, alignx left");		//$NON-NLS-1$

		//**********************************************************************************************
		// NOTE01 - need the GLOBALsentence for eache role and also the LOCAL sentence (if there is one).
		// NOTE02 - LOCAL, if exists, over-rides the GLOBAL one)
		// Also need to set the 'default' JLabel as visible (if GLOBAL) or not (if LOCAL).
		//**********************************************************************************************

	// Setup panel with labels and Sentence text areas
		JLabel sentence = new JLabel("Sentence template: ");	// Sentence template:
		contents.add(sentence, "cell 0 1, alignx left");		//$NON-NLS-1$
		JLabel lbl_default = new JLabel("(default):");				// (default):
		contents.add(lbl_default, "cell 0 1, alignx left");		//$NON-NLS-1$

		sentenceTextArea = new JTextArea();
		sentenceTextArea.setWrapStyleWord(true);
		sentenceTextArea.setLineWrap(true);
		sentenceTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		sentenceTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)sentenceTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		sentenceTextArea.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		sentenceTextArea.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		sentenceTextArea.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane sentenceTextScroll = new JScrollPane(sentenceTextArea);
		sentenceTextScroll.setMinimumSize(new Dimension(400, 75));
		sentenceTextScroll.getViewport().setOpaque(false);
		sentenceTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		sentenceTextArea.setCaretPosition(0);	// set scrollbar to top
		contents.add(sentenceTextScroll, "cell 0 2 2, alignx left");	//$NON-NLS-1$

		lbl_Preview = new JLabel("Preview of output sentence:");		// Preview
		contents.add(lbl_Preview, "cell 0 3, alignx left"); //$NON-NLS-1$

		previewTextArea = new JTextArea("       Sentence previews not yet implemented");  // NOTE04
		previewTextArea.setEditable(false);
		previewTextArea.setFocusable(false);
		previewTextArea.setWrapStyleWord(true);
		previewTextArea.setLineWrap(true);
		previewTextArea.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		previewTextArea.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane previewTextScroll = new JScrollPane(previewTextArea);
		previewTextScroll.setMinimumSize(new Dimension(400, 75));
		previewTextScroll.getViewport().setOpaque(false);
		previewTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		previewTextArea.setCaretPosition(0);	// set scrollbar to top
		contents.add(previewTextScroll, "cell 0 4 2, alignx left");	//$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 5, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		btn_Save = new JButton("Save");			// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 5, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definition

	// Load initial Role setting's sentence and convert role numbers to names
		sentenceTextArea.append(convertSentRoleNumToNames());
		// If we need to, show the Sentence Warning msg
		if (showWarning) {
			warningMsg();
			showWarning = false;
		}

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

		// Listener for sentence textarea edits
		sentenceEditListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {

            	 // for every edit, if the edited sentence matches the event's default role sentence,
            	// enable the word "(default)" in the 'lbl_default field
            	// if it doesn't match, diable the default field
            	sentenceChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        sentenceTextArea.getDocument().addDocumentListener(sentenceEditListen);

		// Listener for roleName combobox selection
        comboRoleNames.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Check if previous sentence was changed before proceeding
				comboRoleNames.getUI().setPopupVisible(comboRoleNames, false);
				if (sentenceChanged) {
					if (JOptionPane.showConfirmDialog(lbl_Preview,
							"This sentence edit has not been saved. \n" +
							"Continue editing or Save this sentence? ",
							"Sentence not saved?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								// YES option - reset combobox and return
								comboRoleNames.setSelectedIndex(currentComboIndex);
								return;
								}
					}
				// NO option - carry on with the new combobox selection
				currentComboIndex = comboRoleNames.getSelectedIndex();
				// Clear out current sentence
				roleSentence = "";		//$NON-NLS-1$
				sentenceTextArea.setText("");	//$NON-NLS-1$
				// Load and convert sentence role numbers to role namese
				sentenceTextArea.append(convertSentRoleNumToNames());
				// If we need to, show the Sentence Warning msg
				if (showWarning) {
					warningMsg();
					showWarning = false;
				}
				sentenceChanged = false;
				btn_Save.setEnabled(false);
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Convert rolenames back to rolenumbers via convert routine
				editedSentence = sentenceTextArea.getText();
				sentenceToSave = convertSentRoleNamesToNums(editedSentence);
				// if sentenceToSave is null, the conversion routine flagged an error - do not save it
				if (sentenceToSave == null) return;

		// NOTE03 - Save the edited sentence text for the current eventnumber/role/langcode as a LOCAL sentence

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

/**
 * convertSentRoleNumToNames - load sentence and convert role numbers to role names
 * @return formatted sentence
 */
	public String convertSentRoleNumToNames() {
		// Setup default rolename/nums for this routine to use
		String[] formatRoleNames = eventRoleNames;
		int[] formatRoleNums = eventRoleNumbers;
        String replacement = "";		//$NON-NLS-1$

		// Load the sentence for this langcode and selected combobox entry
		try {
			roleSentence = pointWhereWhenHandler.pointLibraryResultSet.
					selectSentenceString(eventNumber, eventRoleNumbers[comboRoleNames.getSelectedIndex()],
										 lang_code, dataBaseIndex);
		} catch (HBException e) {
			e.printStackTrace();
		}

		// If roleSentence has error flag, change error message and return
		if (roleSentence.equals("NOSENTENCE")) {			//$NON-NLS-1$
			roleSentence = "No sentence exists for this role in this language.";
			return roleSentence;
		}
		// Check for male/female sentences needing to be split at the TMG $!& marker
		// and check sexCode to select correct sentence to use
		if (roleSentence.contains("$!&")) {		//$NON-NLS-1$
			multiSexSentences = true;
			sexSentences = roleSentence.split("\\$!&");	// NOTE need escape for $!  //$NON-NLS-1$
			if (sexCode.equals("F")) workSentence = sexSentences[1]; // use female sentence		//$NON-NLS-1$
			else workSentence = sexSentences[0]; // use male sentence for 'M' or 'U'
			}
		// If no male/female sentences, use whole sentence
		else workSentence = roleSentence;
		// Look for role references (like [RF:00005] etc)
		// If there are none, return the sentence unchanged
		if (!workSentence.contains("[R")) return workSentence;		//$NON-NLS-1$
		// Now check for the sentence being the en-US version - this means there is no
		// sentence in the current language and the US version has been substituted, so
		// we need to use the US version of role names and numbers
		if (roleSentence.startsWith("(en-US)")) {			//$NON-NLS-1$
			formatRoleNames = usRoleNames;
			formatRoleNums = usRoleNumbers;
			showWarning = true;
		}

		// Setup a regex to find role reference patterns
		Pattern pattern = Pattern.compile("(\\[R[a-zA-Z0-9]{0,4}:)(\\d{5})(])");		//$NON-NLS-1$
        Matcher matcher = pattern.matcher(workSentence);
        StringBuffer result = new StringBuffer();

        // Search for the [R: references and extract the integer role number
        while (matcher.find()) {
            String prefix = matcher.group(1); // e.g., "[RX:"
            int index = Integer.parseInt(matcher.group(2)); // e.g., 00012
            String suffix = matcher.group(3); // e.g., "]"
            // then use 'index' to find correct roleName
    		for (int i=0; i < formatRoleNames.length; i++) {
    			if (index == formatRoleNums[i])
    					replacement = formatRoleNames[i];
    		}
   		// insert the role name and look for another
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + replacement + suffix));
            replacement = "";			//$NON-NLS-1$
        }
        matcher.appendTail(result);
        // Return the formatted sentence text
        return result.toString();
	}		// End convertSentRoleNumToNames

/**
 * warningMsg - show message re sentence missing
 */
	public void warningMsg() {
		// collapse combobox display
		comboRoleNames.getUI().setPopupVisible(comboRoleNames, false);
		// show warning msg
		JOptionPane.showMessageDialog(lbl_Preview,
				"No sentence exists in " + displayLanguage + " for this Role. \n" +
				"The English(US) sentence is shown for reference.",
				"Role sentence missing", JOptionPane.WARNING_MESSAGE);
	}		// End warningMsg

/**
 * convertSentRoleNamesToNums - convert sentence role names back to numbers, for the Save
 * @param editedSentence
 * @return converted sentence
 */
	public String convertSentRoleNamesToNums(String sentence) {
        String replacement = "";		//$NON-NLS-1$

		// Look for role references (like [RF:father] etc). If there are none,
		// and we aren't in male/female sentence mode, return sentence unchanged
		if (!sentence.contains("[R") && !multiSexSentences) return sentence;		//$NON-NLS-1$

		// Setup a regex to find role reference patterns
		Pattern pattern = Pattern.compile("(\\[R[a-zA-Z0-9]{0,4}:)([^]]+)(])"); 		//$NON-NLS-1$
        Matcher matcher = pattern.matcher(sentence);
        StringBuffer result = new StringBuffer();

        // Search for the [R: references and extract the rolename string
        while (matcher.find()) {
            String prefix = matcher.group(1); // e.g., "[RX:"
            String value = matcher.group(2); // e.g., father
            String suffix = matcher.group(3); // e.g., "]"
            // then use 'value' to find correct rolenumber for that name, converted to a 5-char string
    		for (int i=0; i < eventRoleNames.length; i++) {
    			if (value.equals(eventRoleNames[i]))
    					replacement = String.format("%05d", eventRoleNumbers[i]);		//$NON-NLS-1$
    		}
    		// Check if no match found; if so throw error msg and return null as error flag
    		if (replacement.isEmpty()) {
    			JOptionPane.showMessageDialog(lbl_Preview,
    					"The role " + value + " does not exist in " + displayLanguage +
    					"\n for this event. Please revise your edit.",
    					"Role not matched", JOptionPane.WARNING_MESSAGE);
    			btn_Save.setEnabled(false);
    			return null;
    		}
    		// Otherwies insert the role name and look for another
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + replacement + suffix));
            replacement = "";			//$NON-NLS-1$
        }
        matcher.appendTail(result);
        // Store the reformatted sentence text
        workSentence = result.toString();

        // Now we need to know if we worked on a male or fenale or complete sentence
        // so that we can return the complete roleSentence string back to be saved
        if (!multiSexSentences) return workSentence;
        // If it was the female sentence, reconstitute and return the full roleSentence
        if (sexCode.equals("F")) return sexSentences[0] + "$!&" + workSentence;	//$NON-NLS-1$ //$NON-NLS-2$
		return workSentence + "$!&" + sexSentences[1];							//$NON-NLS-1$

	}		// End convertSentRoleNamesToNums

}  // End of HG0548EditSentence

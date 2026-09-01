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
 *			  2025-12-17 NLS code up to this point (D Ferguson)
 *            2026-01-01 Updated code for pointer to HBEventRoleManager (N. Tolleshaug)
 *			  2026-01-04 Log catch block errors (D Ferguson)
 *			  2026-02-21 Added preliminary methods for sentence preload (N. Tolleshaug)
 * v0.05.0033 2026-03-08 Added more code for sentence preload (N. Tolleshaug)
 * 			  2026-06-05 Removed console printout (N. Tolleshaug)
 * 			  2026-06-12 Activated save for standard T168 sentences (N. Tolleshaug)
 * v0.05.0034 2026-07-01-Only defaukt sentence implemented  (N. Tolleshaug)
 * 			  2026-07-11-Defaukt and local sentence implemented  (N. Tolleshaug)
 * 			  2026-07-15 Handling selection local, global and English(US) sentences (N. Tolleshaug)
 * 			  2026-07-16 Added warning English(US) sentences (N. Tolleshaug)
 * 			  2026-07-17 Improved message and eror handling (N. Tolleshaug)
 * 			  2026-07-20 Add check for trailing $!& at sentence end (D Ferguson)
 * 			  2026-07-25 Added name evente sentence processing (N. Tolleshaug)
 * 		      2026-07-30 Modified contructor to receive eventtabkePID (N. Tolleshaug)
 * 			  2026-07-30 Fixed save event sentence (N. Tolleshaug)
 * 			  2026-08-05 Handling LOCAL sentences with ownerTypes (N. Tolleshaug)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 v0.05.0034 -  Only defaukt sentence implemented NToLocal sentence need update
 * 						defaultSentence = true
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
//import java.sql.ResultSet;
//import java.sql.SQLException;
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
//import hre.bila.HBBusinessLayer;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBReportHandler;
import hre.nls.HG0548Msgs;
import net.miginfocom.swing.MigLayout;
//import testbed.parser.TMG_TB_Parser_Case;
//import tmg_parser.TMG_Parser;

/**
 * Edit Sentence
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-06-25
 */
public class HG0548EditSentence extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	private static final int nameRelatedEventGroup = 1;
	public static final String screenID = "54800"; //$NON-NLS-1$
	
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;
	String lang_code = HGlobal.dataLanguage;
	
	HBEventRoleManager pointEventRoleManager;
	HBReportHandler pointReportHandler;
	HBProjectOpenData pointOpenProject;
	HG0548EditSentence pointEditSentence = this;
	HG0507SelectPartner pointSelectPartner;
	int dataBaseIndex, eventNumber, eventNameType;
	int ownerType = 0; // owner type:  1 - name, 2 - event, 3 - associate, 4 - partner

	private JPanel contents;
	long eventTablePID, ownerTablePID, nameTablePID, roleSentencePID = null_RPID, sentenceTablePID;

	JTextArea sentenceTextArea, previewTextArea;
	JLabel lbl_Preview;
	JButton btn_Save;
	DocumentListener sentenceEditListen;
	boolean sentenceChanged = false;
	boolean multiSexSentences = false;
	boolean showWarning = false;
	boolean localSentence = false;
	boolean englishUSsentence = false;
	boolean nameSentence = false;
	String sexCode;
	String eventRoleSentence = "";	//$NON-NLS-1$
	String[] sexSentences;
	String workSentence = "";	//$NON-NLS-1$
	String editedSentence = ""; //$NON-NLS-1$
	String sentenceToSave = "";	//$NON-NLS-1$

	String[] eventRoleNames;
	String[] nameEventsTypes;
	int[] nameEventsNumbers;
	int[] eventRoleNumbers;
	long[] eventRoleSentensePID;
	JComboBox<String> comboRoleNames;
	JComboBox<String> comboSentenceOption;
	String[] sentenceOptions = {" Local"," Global"," Englist(US)"};
	int currentComboIndex = 0, roleNumber;
	String displayLanguage = "";	//$NON-NLS-1$
	
/**
 * HG0548EditSentence constructor for edit name
 * @throws HBException
 */
	public HG0548EditSentence(HBProjectOpenData pointOpenProject, long personNameTablePID,
			int eventNameType)  {
		this.nameTablePID = personNameTablePID;
		this.ownerTablePID = personNameTablePID;
		this.pointOpenProject = pointOpenProject;
		this.eventNumber = eventNameType;
		pointEventRoleManager = pointOpenProject.getEventRoleManager();
		pointReportHandler = pointOpenProject.getReportHandler();
		pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		nameSentence = true;
		ownerType = 1; // Set owner type name sentence for LOCAL
		editSentenseConstructor();
	}
	
/**
 * HG0548EditSentence constructor for edit event
 * @throws HBException
 */
	public HG0548EditSentence(HBProjectOpenData pointOpenProject, long ownerTablePID,
								int ownerType, int eventNumber, int roleNumber, String sexCode)  {
		this.pointOpenProject = pointOpenProject;
		this.ownerType = ownerType;
		this.roleNumber = roleNumber;
		this.eventNumber = eventNumber;
		this.sexCode = sexCode;
		this.ownerTablePID = ownerTablePID;
		this.eventTablePID = ownerTablePID;
		pointEventRoleManager = pointOpenProject.getEventRoleManager();
		pointReportHandler = pointOpenProject.getReportHandler();
		pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		editSentenseConstructor();
	}
	
	
	/**
	 * HG0548EditSentence constructor for edit event
	 * @throws HBException
	 */
		public HG0548EditSentence(HBProjectOpenData pointOpenProject, long ownerTablePID, long eventTablePID,
									int ownerType, int eventNumber, int roleNumber, String sexCode)  {
			this.pointOpenProject = pointOpenProject;
			this.ownerType = ownerType;
			this.roleNumber = roleNumber;
			this.eventNumber = eventNumber;
			this.sexCode = sexCode;
			this.ownerTablePID = ownerTablePID;
			this.eventTablePID = eventTablePID;
			pointEventRoleManager = pointOpenProject.getEventRoleManager();
			pointReportHandler = pointOpenProject.getReportHandler();
			pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			editSentenseConstructor();
		}
		
/**
 * Create the dialog
 * @throws HBException
 */	
	private void editSentenseConstructor() {

	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsentence";		 //$NON-NLS-1$
		setTitle(HG0548Msgs.Text_0);		// Sentence Editor
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0548EditSentence");	//$NON-NLS-1$

		// Get the lists of Roles and their reference Numbers for this eventNumber
		if (nameSentence) {
			//System.out.println(" Edit name sentence type " + eventNameType + " Name PID: " + nameTablePID);
			try {
				nameEventsTypes = pointEventRoleManager.getEventTypeList(nameRelatedEventGroup);
				nameEventsNumbers = pointEventRoleManager.getEventTypes();
			} catch (HBException hbe) {
				System.out.println(" Name event list error: " + hbe.getMessage());
				hbe.printStackTrace();
			}
		} else 
			try {
				eventRoleNames = pointEventRoleManager.getRolesForEvent(eventNumber, "");	//$NON-NLS-1$
				eventRoleNumbers = pointEventRoleManager.getEventRoleNumbers();
				eventRoleSentensePID = pointEventRoleManager.getEventRoleSentencePID();
				// Above gets the data for the current language, but we also need to get the Eng(US) versions
				// To do this, temporarily set the global datalanguage to ENG(US) and then restore it
	
			} catch (HBException hbe) {
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in HG0548 sentence role loading " + hbe.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(hbe);
				}
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

		JLabel role = new JLabel(HG0548Msgs.Text_1);// Select Role
		if (nameSentence) role.setText("Select Type");
		contents.add(role, "cell 0 0, alignx left");		//$NON-NLS-1$
	// Load the combobox with the rolenames and set the selected one to match roleName parameter
		comboRoleNames = new JComboBox<String>();
		contents.add(comboRoleNames, "cell 0 0, gap 10");		//$NON-NLS-1$
		if (nameSentence) {
			for (int i = 0; i < nameEventsTypes.length; i++) {
			       comboRoleNames.addItem(nameEventsTypes[i]);
			       if (eventNumber == nameEventsNumbers[i]) comboRoleNames.setSelectedIndex(i);
			}
		} else {
			for (int i = 0; i < eventRoleNames.length; i++){
		       comboRoleNames.addItem(eventRoleNames[i]);
		       if (roleNumber == eventRoleNumbers[i]) comboRoleNames.setSelectedIndex(i);
		    }
		}
	// And save the combobox setting
		currentComboIndex = comboRoleNames.getSelectedIndex();

		JLabel lbl_lang = new JLabel(HG0548Msgs.Text_2);		// Language is set to:
		contents.add(lbl_lang, "cell 1 0, alignx left");		//$NON-NLS-1$
		JLabel langCode = new JLabel(displayLanguage);
		contents.add(langCode, "cell 1 0, alignx left");		//$NON-NLS-1$

	//**********************************************************************************************
	// NOTE01 - need the GLOBALsentence for eache role and also the LOCAL sentence (if there is one).
	// NOTE02 - LOCAL, if exists, over-rides the GLOBAL one)
	// Also need to set the 'default' JLabel as visible (if GLOBAL) or not (if LOCAL).
	//**********************************************************************************************

	// Setup panel with labels and Sentence text areas
		JLabel sentence = new JLabel(HG0548Msgs.Text_3);	// Sentence template:
		contents.add(sentence, "cell 0 1, alignx left");		//$NON-NLS-1$

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

		lbl_Preview = new JLabel(HG0548Msgs.Text_5);		// Preview of output sentence:
		contents.add(lbl_Preview, "cell 0 3, alignx left"); //$NON-NLS-1$

		previewTextArea = new JTextArea(HG0548Msgs.Text_6);
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
		JButton btn_Cancel = new JButton(HG0548Msgs.Text_7);	// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 5, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		btn_Save = new JButton(HG0548Msgs.Text_8);		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 5, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

		comboSentenceOption = new JComboBox<String>(sentenceOptions);
		contents.add(comboSentenceOption, "cell 0 1, alignx right");		//$NON-NLS-1$

	// End of Panel Definition

	// create eventReportDate instance
		try {
			
	// Test if LOCAL sentence exist
			if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
			sentenceTablePID = pointReportHandler.pointLibraryResultSet.
				selectSentenceSetPID(ownerTablePID, ownerType, eventNumber, roleNumber,
									 			lang_code, dataBaseIndex);
			if (sentenceTablePID == null_RPID) 
				comboSentenceOption.setSelectedIndex(1);
			 else {
				comboSentenceOption.setSelectedIndex(0);
				localSentence = true; // Set LOCAL sentence
				//System.out.println(" Local detected");
			}
			
		// Set up sentence processing for events or names
			if (nameSentence) pointReportHandler.createReportNameData(nameTablePID);
			else pointReportHandler.createReportEventData(eventTablePID, ownerTablePID, ownerType);
			
		// Load initial Role setting's sentence and convert role numbers to names
			currentComboIndex = comboRoleNames.getSelectedIndex();
			if (nameSentence) eventNumber = nameEventsNumbers[currentComboIndex];
			if (nameSentence) roleSentencePID = null_RPID;
			else roleSentencePID = eventRoleSentensePID[currentComboIndex];
			
		//System.out.println(" Global sentence initial  PID: " + roleSentencePID + " Event Type: " + eventNumber);
			eventRoleSentence = getEventRoleSentenceforEvents(currentComboIndex, roleSentencePID);
			sentenceTextArea.append(convertSentRoleNumToNames(eventRoleSentence));
	    	previewTextArea.setText("");	//$NON-NLS-1$
			previewTextArea.append(pointReportHandler.sentenceParser(eventRoleSentence));
			pointReportHandler.runTMGparcer(eventRoleSentence);
		} catch (HBException hbe) {
			System.out.println(" HG0548EditSentence - initiate: " + hbe.getMessage());	//$NON-NLS-1$
			hbe.printStackTrace();
		}

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

       /* for every edit, if the edited sentence matches the event's default role sentence,
          enable the word "(default)" in the 'lbl_default field
          if it doesn't match, diable the default field */
	        	eventRoleSentence = convertSentRoleNamesToNums(sentenceTextArea.getText());
	        	previewTextArea.setText("");	//$NON-NLS-1$
	        	try {
					previewTextArea.append(pointReportHandler.sentenceParser(eventRoleSentence));
				// Attempt to trigger complete sentence parcer
					//pointReportHandler.runTMGparcer(eventRoleSentence);
	            	sentenceChanged = true;
	           // Not possible to update default English(US) sentense
	            	if (!englishUSsentence) btn_Save.setEnabled(true);
				} catch (HBException hbe) {
					System.out.println(" HG0548EditSentence - sentence edit error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
            }
        };
        sentenceTextArea.getDocument().addDocumentListener(sentenceEditListen);

		// Listener for roleName combobox selection
        comboRoleNames.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String eventRoleSentence;
				// Check if previous sentence was changed before proceeding
				comboRoleNames.getUI().setPopupVisible(comboRoleNames, false);
				if (!(comboRoleNames.getSelectedIndex() == -1)) {
					if (sentenceChanged) {
						if (JOptionPane.showConfirmDialog(lbl_Preview,
								HG0548Msgs.Text_9 +		// This sentence edit has not been saved. \n
								HG0548Msgs.Text_10,		// Continue editing or Save this sentence?
								HG0548Msgs.Text_11,		// Sentence not saved?
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
									// YES option - reset combobox and return
									comboRoleNames.setSelectedIndex(currentComboIndex);
									return;
								}
						}

					try {
						localSentence = false; // Set GLOBAL sentence
					// NO option - carry on with the new combobox selection
						currentComboIndex = comboRoleNames.getSelectedIndex();
						if (nameSentence) roleSentencePID = null_RPID;
						else roleSentencePID = eventRoleSentensePID[currentComboIndex];
						if (nameSentence) eventNumber = nameEventsNumbers[currentComboIndex];
						//System.out.println(" Global role selected sentense PID: " + roleSentencePID);
					// Test if LOCAL sentence exist
						if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
						sentenceTablePID = pointReportHandler.pointLibraryResultSet.
							selectSentenceSetPID(ownerTablePID, ownerType, eventNumber, roleNumber,
												 			lang_code, dataBaseIndex);
						if (sentenceTablePID == null_RPID) comboSentenceOption.setSelectedIndex(1);
						else {
							comboSentenceOption.setSelectedIndex(0);
							localSentence = true; // Set LOCAL sentence
							//System.out.println(" Local detected");
						}
					// Collect current sentence
						eventRoleSentence = getEventRoleSentenceforEvents(currentComboIndex, roleSentencePID);
					// Clear out current sentence
						sentenceTextArea.setText("");		//$NON-NLS-1$
					// Load and convert sentence role numbers to role namese
						sentenceTextArea.append(convertSentRoleNumToNames(eventRoleSentence));
					// Set up preview of sentence
				    	previewTextArea.setText("");		//$NON-NLS-1$
						previewTextArea.append(pointReportHandler.sentenceParser(eventRoleSentence));
						//pointReportHandler.runTMGparcer(eventRoleSentence);
					} catch (HBException hbe) {
						System.out.println(" HG0548EditSentence - listener combo: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					}
				// If we need to, show the Sentence Warning msg
					if (showWarning) {
						warningMsg();
						showWarning = false;
					}
					sentenceChanged = false;
					btn_Save.setEnabled(false);
				}
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (englishUSsentence) {
				    JOptionPane.showMessageDialog(null,"Cannot update Englis(US) sentence",
				    		" Save Enlish(US)",JOptionPane.ERROR_MESSAGE);
					return;
				}
			// Convert rolenames back to rolenumbers via convert routine
				currentComboIndex = comboRoleNames.getSelectedIndex();
				editedSentence = sentenceTextArea.getText();
				sentenceToSave = convertSentRoleNamesToNums(editedSentence);
			// if sentenceToSave is null, the conversion routine flagged an error - do not save it
				if (sentenceToSave == null) {
					System.out.println(" HG0548EditSentence - sentenceToSave epty ");	//$NON-NLS-1$
					return;
				}

				try {
					if (localSentence) {
						if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
						pointReportHandler.pointLibraryResultSet.storeLocalSentence(ownerTablePID, ownerType, 
								sentenceToSave, eventNumber, roleNumber, lang_code, pointOpenProject);
					} else {
						if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
						pointReportHandler.pointLibraryResultSet.storeGlobalSentence(sentenceTablePID, sentenceToSave, eventNumber,
								roleNumber, lang_code, pointOpenProject);
					}

				} catch (HBException hbe) {
					System.out.println(" Save new sentence error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}

				sentenceChanged = false;

				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit HG0548EditSentence");	//$NON-NLS-1$
				dispose();
			}
		});

	// Switc between local and global sentenses
		comboSentenceOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				int selection = comboSentenceOption.getSelectedIndex();
				//System.out.println(" Selection: " + comboSentenceOption.getSelectedItem());
				if (selection == 0) {
					localSentence = true;
					englishUSsentence = false;
					btn_Save.setEnabled(true);
				}  else if (selection == 1) {
					localSentence = false;
					englishUSsentence = false;
					btn_Save.setEnabled(true);
				}  else if (selection == 2) {
					englishUSsentence = true;
					localSentence = false;
					btn_Save.setEnabled(false);
				}

				sentenceChanged = false;
				try {
					sentenceTextArea.getDocument().removeDocumentListener(sentenceEditListen);
					currentComboIndex = comboRoleNames.getSelectedIndex();
					if (nameSentence) eventNumber = nameEventsNumbers[currentComboIndex];
					if (nameSentence) roleSentencePID = null_RPID;
					else roleSentencePID = eventRoleSentensePID[currentComboIndex];
					//roleSentencePID = eventRoleSentensePID[currentComboIndex];

				// Collect current sentence
					eventRoleSentence = getEventRoleSentenceforEvents(currentComboIndex, roleSentencePID);

				// Clear out current sentence
					sentenceTextArea.setText("");		//$NON-NLS-1$

				// Load and convert sentence role numbers to role namese
					sentenceTextArea.append(convertSentRoleNumToNames(eventRoleSentence));
				// Set up preview of sentence
			    	previewTextArea.setText("");		//$NON-NLS-1$
					previewTextArea.append(pointReportHandler.sentenceParser(eventRoleSentence));
					//pointReportHandler.runTMGparcer(eventRoleSentence);
					sentenceTextArea.getDocument().addDocumentListener(sentenceEditListen);
				} catch (HBException hbe) {
					System.out.println(" HG0548EditSentence - local/global button: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
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
 * eteventRoleSentenceforEvents
 * @param currentComboIndex
 * @return
 */
	private String getEventRoleSentenceforEvents(int currentComboIndex, long globalSentenTablePID) {
	// Load the sentence for this langcode either local or global
		String sentenceFound = "NOSENTENCE";//$NON-NLS-1$
		int roleNumber;
		try {
			if (localSentence) {
				if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
				sentenceTablePID = pointReportHandler.pointLibraryResultSet.
					selectSentenceSetPID(ownerTablePID, ownerType, eventNumber, roleNumber,
										 			lang_code, dataBaseIndex);
			} else {
				if (englishUSsentence) {
					if (nameSentence) roleNumber = 1; else roleNumber = eventRoleNumbers[currentComboIndex];
					sentenceTablePID =  pointReportHandler.pointLibraryResultSet.selectFallBackUSsentSetPID(eventNumber,
											roleNumber, dataBaseIndex);
				} else {
					if (nameSentence) sentenceTablePID = pointReportHandler.pointLibraryResultSet.
									selectSentenceSetPID(null_RPID, 0, eventNumber,1 ,lang_code, dataBaseIndex);
					else sentenceTablePID = globalSentenTablePID;
					if (HGlobal.DEBUG)  
						System.out.println(" Global sentence PID:" + sentenceTablePID + " Lang: " + lang_code); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (sentenceTablePID == null_RPID) return sentenceFound;
			sentenceFound = pointReportHandler.pointLibraryResultSet. getSentenceSetString(sentenceTablePID, dataBaseIndex);
			if (HGlobal.DEBUG) System.out.println(" Sentence found: " + sentenceFound);	//$NON-NLS-1$
			return sentenceFound;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) System.out.println("ERROR: in HG0548 sentence loading " + hbe.getMessage());	//$NON-NLS-1$
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0548 sentence loading " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		return sentenceFound;
	}

/**
 * public String convertSentRoleNumToNames(String eventRoleSentence)
 * @param eventRoleSentence
 * @return
 */
	public String convertSentRoleNumToNames(String eventRoleSentence) {
		// Setup default rolename/nums for this routine to use
		String[] formatRoleNames = eventRoleNames;
		int[] formatRoleNums = eventRoleNumbers;
        String replacement = "";		//$NON-NLS-1$
	// If eventRoleSentence has error flag, change error message and return
		if (eventRoleSentence.equals("NOSENTENCE")) {			//$NON-NLS-1$
			//eventRoleSentence = HG0548Msgs.Text_12;		// No sentence exists for this role in this language
			eventRoleSentence = "";		//$NON-NLS-1$
			return eventRoleSentence;
		}
	// Fix any illegal sentence (one which ends in $!&)
		if (eventRoleSentence.endsWith("$!&") ) eventRoleSentence = eventRoleSentence.replace("$!&",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// Check for male/female sentences needing to be split at the TMG $!& marker
	// and check sexCode to select correct sentence to use
		if (eventRoleSentence.contains("$!&")) {		//$NON-NLS-1$
			multiSexSentences = true;
			sexSentences = eventRoleSentence.split("\\$!&");	// NOTE need escape for $!  //$NON-NLS-1$
			if (sexCode.equals("F")) workSentence = sexSentences[1]; // use female sentence		//$NON-NLS-1$
			else workSentence = sexSentences[0]; // use male sentence for 'M' or 'U'
			}
		// If no male/female sentences, use whole sentence
		else workSentence = eventRoleSentence;
		// Look for role references (like [RF:00005] etc)
		// If there are none, return the sentence unchanged
		if (!workSentence.contains("[R")) return workSentence;		//$NON-NLS-1$

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
				HG0548Msgs.Text_13 + displayLanguage 	// No sentence exists in
				+ HG0548Msgs.Text_14					// for this Role. \n
				+ "copy and edit english(US) sentence",			// The English(US) sentence is shown for reference.
				HG0548Msgs.Text_16, 					// Role sentence missing
				JOptionPane.WARNING_MESSAGE);
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
    					HG0548Msgs.Text_17 							// The role
    					+ value + HG0548Msgs.Text_18 				// does not exist in
    					+ displayLanguage + HG0548Msgs.Text_19,		// for this event. Please revise your edit.
    					HG0548Msgs.Text_20, 						// Role not matched
    					JOptionPane.WARNING_MESSAGE);
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
        // so that we can return the complete eventRoleSentence string back to be saved
        if (!multiSexSentences) return workSentence;
        // If it was the female sentence, reconstitute and return the full eventRoleSentence
        if (sexCode.equals("F")) return sexSentences[0] + "$!&" + workSentence;	//$NON-NLS-1$ //$NON-NLS-2$
		return workSentence + "$!&" + sexSentences[1];							//$NON-NLS-1$

	}		// End convertSentRoleNamesToNums
}  // End of HG0548EditSentence

package hre.gui;
/***********************************************************************************
* Convert TMG Name Style
************************************************************************************
* v0.01.0029 2023-04-14 Initial draft (D Ferguson)
*			 2023-04-18 Modifed initial element match processing (D Ferguson)
* 			 2023-04-19 All NLS entries moved to HG0524Msgs (D Ferguson)
* v0.03.0031 2024-10-01 Clean whitespace (D Ferguson)
* 			 2024-12-05 Set JOptionPane dialog so is always inside this (D Ferguson)
* v0.04.0032 2026-01-08 Log all catch block msgs (D Ferguson)
***********************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBNameStyleManager;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0524Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Convert a TMG Name Style
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2023-04-14
 */

public class HG0527ConvertNameStyle extends HG0524ManageNameStyles {

	private static final long serialVersionUID = 001L;
	public static final String screenID = "52700";	//$NON-NLS-1$
	private JPanel contents;
	HBNameStyleManager pointStyleHandler;

	// For controlling element conversion
	int tmgIndex;
	String tmgElement = "";		//$NON-NLS-1$
	String tmgCode;
	boolean case1 = false;			// true if we are in Element conversion Case 1
	boolean case2 = false;			// true if we are in Element conversion Case 2
	Object[] options1 = {HG0524Msgs.Text_170, 	// Replace an Element
						HG0524Msgs.Text_171,	// Add an Element
						HG0524Msgs.Text_172};	// Leave as is
	Object[] options2 = {HG0524Msgs.Text_173,	// OK
						HG0524Msgs.Text_174}; 	// Cancel

/**
 * Create the Dialog
 * @param pointStyleHandler
 * @param tmgStyleName - name of the TMG Style being converted
 * @param styleIndex - index into the list of TMG Name Styles
 * @param nameType - Person or Location type
 */
	public HG0527ConvertNameStyle(HBProjectOpenData pointOpenProject, HBNameStyleManager pointStyleHandler, String tmgStyleName, int styleIndex, String nameType) {
		super(pointOpenProject, pointStyleHandler);

		// Setup references for Help. Although has no Help button, we must handle F1
		windowID = screenID;
		helpName = "convertnamestyle";	//$NON-NLS-1$

		// Setup main panel
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[]", "[]10[]5[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// note: no toolbar
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0527ConvertNameStyle");	//$NON-NLS-1$
		setResizable(false);
		setNameType(nameType);
		setTitle(HG0524Msgs.Text_175 + tmgStyleName.trim() + "'");	//$NON-NLS-1$ // Convert TMG Name Style '

		// Define model lists to be displayed in the scrollpanes
		//  - inherit list_AllElements from HG0524
		//  - inherit allElementsModel to contain list of ALL Name Elements of the name type (Pers/Locn/etc) from HG0524
		//  - inherit allCodesModel to hold ALL Name Element Codes list so they can be updated from HG0524

		//  tmgElementsModel=ChosenElementsModel to contain list of the Name Elements belonging to TMG Name Style
		//  - inherit ChosenElementsModel from HG0524
		//  - inherit ChosenCodesModel from HG0524

		//  convStyleModel to contain list of the Name Elements for the converted Name Style
		DefaultListModel<String> convStyleModel = new DefaultListModel<>();
		//  model to hold converted Name Element Codes list. Never displayed so no JList defined
		DefaultListModel<String> convCodesModel = new DefaultListModel<String>();

		// Instruction panel row 0 across all 3 columns
		JTextArea txtHelp = new JTextArea(6, 1);	// 6 rows max
		txtHelp.setBorder(UIManager.getBorder("TextField.border")); //$NON-NLS-1$
		txtHelp.setAlignmentY(Component.TOP_ALIGNMENT);
		txtHelp.setOpaque(false);
		txtHelp.setEditable(false);
		txtHelp.setLineWrap(true);
	    txtHelp.setWrapStyleWord(true);
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$
	    txtHelp.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));		// Set text size/font to current JTattoo setting
		txtHelp.setText(HG0524Msgs.Text_176 + HG0524Msgs.Text_177 + HG0524Msgs.Text_178 +HG0524Msgs.Text_179);
			//These text fields read as follows:
//				"INSTRUCTIONS: this screen shows the HRE equivalent elements for each element of the selected TMG style. \n\n" +
//				"You may choose to select a replacement element from the All Elements list OR add an element to the All Elements list (which will then be used), " +
//				"OR leave the element set to its current HRE equivalent. \n" +
//				"When all elements are processed, click the 'Perform Conversion' button."
		JScrollPane helpScroll = new JScrollPane(txtHelp);
		helpScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll as needed
		txtHelp.setCaretPosition(0);	// set scrollbar to top
		contents.add(helpScroll, "cell 0 0 3, grow"); //$NON-NLS-1$

		// Column 0 contents
		JLabel lbl_From = new JLabel(HG0524Msgs.Text_180);		// All Elements list:
		contents.add(lbl_From, "cell 0 1");	//$NON-NLS-1$

//		JList<String> list_AllElements = new JList<>(allElementsModel);
		list_AllElements.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_AllElements.setLayoutOrientation(JList.VERTICAL);
		list_AllElements.setEnabled(false);		// Initially, set the list non-clickable
		JScrollPane scroll_Elements = new JScrollPane(list_AllElements);
		scroll_Elements.setPreferredSize(new Dimension(200, 250));
		contents.add(scroll_Elements, "cell 0 2"); //$NON-NLS-1$
		list_AllElements.setCellRenderer(new allListCellRenderer());

		// Column 1 contents
		JLabel lbl_TmgElements = new JLabel(HG0524Msgs.Text_181);		// TMG Style Elements:
		contents.add(lbl_TmgElements, "cell 1 1");	//$NON-NLS-1$

		JList<String> list_TmgElements = new JList<>(chosenElementsModel);
		list_TmgElements.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_TmgElements.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_TMG = new JScrollPane(list_TmgElements);
		scroll_TMG.setPreferredSize(new Dimension(200, 250));
		contents.add(scroll_TMG, "cell 1 2"); //$NON-NLS-1$

		// Column 2 contents
		JLabel lbl_To = new JLabel(HG0524Msgs.Text_182);	// Equivalent HRE Style Elements:
		contents.add(lbl_To, "cell 2 1");	//$NON-NLS-1$

		JList<String> list_ConvertedStyle = new JList<>(convStyleModel);
		list_ConvertedStyle.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_ConvertedStyle.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_ConvertedStyle = new JScrollPane(list_ConvertedStyle);
		scroll_ConvertedStyle.setPreferredSize(new Dimension(200, 250));
		contents.add(scroll_ConvertedStyle, "cell 2 2");	//$NON-NLS-1$

		JButton btn_DoConvert = new JButton(HG0524Msgs.Text_183);		// Perform Conversion
		btn_DoConvert.setEnabled(false);
		contents.add(btn_DoConvert, "cell 1 3 2, tag OK");	//$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0524Msgs.Text_184);		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 3 2, alignx right, gapx 20, tag cancel");	//$NON-NLS-1$

    	pack();
    	setVisible(true);

		// Define a user dialog so always be on top and set a location (nothing
		// will show on screen yet as no content is defined until JOptionPane's start)
	    JDialog dialog = new JDialog();
	    dialog.setAlwaysOnTop(true);
	    dialog.setLocationRelativeTo(this.contents);
	    dialog.setVisible(true);

/**
 * INITIALISE PHASE1: load style data to screen in columns 0, 1, 2
 */
        // Load list of ALL Name Style Elements/codes from T163
 		pointStyleHandler.getAllElements(allElementsModel);
 		pointStyleHandler.getAllCodes(allCodesModel);

 		// Load elements/codes of TMG style being converted
 		// (codesModel should exist, inherited from HG0524, but safer to reload)
    	pointStyleHandler.setNameStyleData(styleIndex);
		pointStyleHandler.getChosenCodes(chosenCodesModel);
		pointStyleHandler.getChosenElements(chosenElementsModel);

		// Now load Converted style lists with 'Not converted' & '0000'
		for (int i = 0; i < chosenElementsModel.getSize(); i++) {
			convStyleModel.addElement("Not converted"); //$NON-NLS-1$
			convCodesModel.addElement("0000");		// start with a default code	//$NON-NLS-1$
		}

/**
 * INITIALISE PHASE2: match all TMG elements against the All list by Code ONLY
 */
    	int allSize = allElementsModel.getSize();
    	int tmgSize = chosenElementsModel.getSize();
    	// run through all the TMG elements to match them for conversion
    	for (int i = 0; i < tmgSize; i++) {
        	tmgCode = chosenCodesModel.getElementAt(i);
	    // if the TMG Element code matches an entry in the ALL list
    	// then place Name/code into Converted list.
    		for (int j = 0; j < allSize; j++) {
    			if (tmgCode.equals(allCodesModel.getElementAt(j)) )  {
    						convStyleModel.setElementAt(allElementsModel.getElementAt(j), i);
    						convCodesModel.setElementAt(tmgCode, i);
    			}
    		}
    	}
		// Check if all conversion now done by testing convCodesModel (it should be)
		if (checkAllDone(convCodesModel)) btn_DoConvert.setEnabled(true);

/**
 * SETUP LISTENERS FOR THIS METHOD
 */
		// Listener for clicking 'X on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0527ConvertNameStyle"); //$NON-NLS-1$
				dispose();
				}
		});

		// Listener for a selection in list_TmgElements
		list_TmgElements.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eCh) {
		        if (!eCh.getValueIsAdjusting()) {
		        // Get the values of the selected list item
		        	tmgIndex = list_TmgElements.getSelectedIndex();
		        	tmgElement = list_TmgElements.getSelectedValue();
		        	tmgCode = chosenCodesModel.getElementAt(tmgIndex);

		    	// CASE 1: does user want to use an element from the ALL list to replace the tmgElement name?
		    	// If Yes, show ALL-list selection (which will then do a copy into Converted list)
		    		int answer = JOptionPane.showOptionDialog(dialog,
		    				HG0524Msgs.Text_185 +	// You may replace the selected Element with an element from \n
		    				HG0524Msgs.Text_186	+	// the All Elements list, OR add a new element to the \n
		    				HG0524Msgs.Text_187,	// All Elements list OR leave the element as it is.
		    				HG0524Msgs.Text_188,	// Convert a Name Style
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options1,
							options1[0]);

		    		// if 1st option, let user choose from All list
		    		if (answer == JOptionPane.YES_OPTION) {
		    			case1 = true;
						list_AllElements.setEnabled(true);		// make All list selectable
          				// Rest of process handled in list_AllElements selection listener
						return;
		            }
		    		// If 3rd option or OptionPane Close, do nothing
		    		if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) return;
		    		// else fall through to Case 2 (2nd option)

		    	// CASE 2: prompt user where to ADD an element to the ALL list to replace the tmgElement name
	    			answer = JOptionPane.showOptionDialog(dialog,
	    				HG0524Msgs.Text_189 +	// Select an entry in the All Elements list \n
		    			HG0524Msgs.Text_190,	// as the point to add a new Element.
		    			HG0524Msgs.Text_188,	// Convert a Name Style
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options2,
						options2[0]);
    				if (answer == JOptionPane.YES_OPTION) {
							case2 = true;
	    					list_AllElements.setEnabled(true);		// make All list selectable
	              			// Rest of process handled in list_AllElements selection listener
					}
		        }
		    }
		});

		// Listener for a selection in list_AllElements
		list_AllElements.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eCh) {
		        if (!eCh.getValueIsAdjusting()) {
		        // First test if the chosen element in the All list is already in the Converted list
		        // If so, tell user to try again.
					int allIndex = list_AllElements.getSelectedIndex();
					String allElement = list_AllElements.getSelectedValue();
					String allCode = allCodesModel.getElementAt(allIndex);
		        	if (checkCodeUsed(convCodesModel, allCode)) {
		        		JOptionPane.showMessageDialog(dialog,
		        				HG0524Msgs.Text_191 +			// Element already in use \n
								HG0524Msgs.Text_111, 			// Please try again.
								HG0524Msgs.Text_112, JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        // If this is Conversion Case 1, copy data from All list to Converted list
					if (case1 && list_AllElements.getSelectedIndex() != -1) {
						convStyleModel.setElementAt(allElement, tmgIndex);
						convCodesModel.setElementAt(allCodesModel.getElementAt(allIndex), tmgIndex);
						case1 = false;							// finished with Case 1
						list_AllElements.setEnabled(false);		// make All list non-selectable	again
						// Check if all conversion now done by testing convCodesModel
						if (checkAllDone(convCodesModel)) btn_DoConvert.setEnabled(true);
					}

		        // If this is Conversion Case 2, add new entry to All list, save it and copy to Converted
					if (case2 && list_AllElements.getSelectedIndex() != -1) {
						String newNameElemnt = JOptionPane.showInputDialog(dialog,
								HG0524Msgs.Text_105						// Enter name of new Name Style Element.\n\n
								+ HG0524Msgs.Text_106					// The new element will be placed AFTER the currently\n
								+ HG0524Msgs.Text_107, 					// selected entry in the list of All Elements
								HG0524Msgs.Text_108, 					// Define New Element
								JOptionPane.INFORMATION_MESSAGE);
						// When we get a non-null name entered we proceed
						if (newNameElemnt != null && newNameElemnt.length() > 0 ) {
							// Check the element name doesn't already exist in the list
							if (allElementsModel.contains(newNameElemnt)) {
								JOptionPane.showMessageDialog(dialog,
										HG0524Msgs.Text_109 + newNameElemnt.trim()				// Element '
										+ HG0524Msgs.Text_110									// ' already exists.\n
										+ HG0524Msgs.Text_111, 									// Please try again.
										HG0524Msgs.Text_112, JOptionPane.INFORMATION_MESSAGE);	// Define New Element
								return;
							}
							// Get currently selected item index
							int indexA = list_AllElements.getSelectedIndex();
							// add new name element after that point
							allElementsModel.add(indexA+1, newNameElemnt);
							// Now add new entry into Name Element Code list for this new Element
							// First, find if indexA is at the end of the Name Element Code list.
							boolean atTheListEnd = false;
							if (allCodesModel.getSize() == indexA+1) atTheListEnd = true;
							// Now get the entry at indexA and the one after that, unless
							// we are at the end of the list.
							String codeBefore = allCodesModel.getElementAt(indexA);
							String codeAfter = ""; 	//$NON-NLS-1$
							if (!atTheListEnd) codeAfter = allCodesModel.getElementAt(indexA+1);
							// Now convert these 2 strings to integer and calculate a value between them,
							// unless we're at list end, when we set numAfter to be numBefore + 100.
							int numBefore = 0;
							int numAfter = 0;
							try{
					            numBefore = Integer.valueOf(codeBefore);
					            if (atTheListEnd) numAfter = numBefore + 100;
					            	else numAfter = Integer.valueOf(codeAfter);
					        }
					        catch (NumberFormatException ex){
								if (HGlobal.writeLogs) {
									HB0711Logging.logWrite("ERROR: in HG0527 listing elements: " + ex.getMessage()); //$NON-NLS-1$
									HB0711Logging.printStackTraceToFile(ex);
								}
					        }
							String newCodeElemnt = Integer.toString((numBefore+numAfter)/2);
							//Set leading "0" if only 3 chars
							if (newCodeElemnt.length() == 3) newCodeElemnt = "0" + newCodeElemnt; //$NON-NLS-1$
							// Now insert the new Name Element Code into the code List
							allCodesModel.add(indexA+1, newCodeElemnt);
							// and save the updated All Element list
							try {
								pointStyleHandler.updateAllElementTable(nameType, allElementsModel, allCodesModel);
							} catch (HBException hbe) {
								if (HGlobal.writeLogs) {
									HB0711Logging.logWrite("ERROR: in HG0527 saving element list: " + hbe.getMessage()); //$NON-NLS-1$
									HB0711Logging.printStackTraceToFile(hbe);
								}
								errorMessage("HBE00","Save all name style changes error: +\n" + hbe.getMessage(), 0, nameType); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}

						// Now get the new element name/code from the All list
						allIndex = list_AllElements.getSelectedIndex()+1;
						allElement = allElementsModel.getElementAt(allIndex);
						allCode = allCodesModel.getElementAt(allIndex);
						// and save them into the converted lists
						convStyleModel.setElementAt(allElement, tmgIndex);
						convCodesModel.setElementAt(allCode, tmgIndex);
						case2 = false;							// finished with Case 2
						list_AllElements.setEnabled(false);		// make All list non-selectable	again
						// Check if all conversion now done by testing convCodesModel
						if (checkAllDone(convCodesModel)) btn_DoConvert.setEnabled(true);
					}
		        }
		    }
		});

		// Listener for DoConvert button
		btn_DoConvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// FIRST - update T403/553 with new styleCodes as now stored in convCodesModel
				long nameStylePID = pointManageNameStyles.pointStyleHandler.getNameStylePID(styleIndex);
				String oldCode, newCode;
				// Loop through the converted codes and do updates for changed codes
				for (int v = 0; v < convCodesModel.getSize(); v++) {
					oldCode = chosenCodesModel.getElementAt(v);
					newCode = convCodesModel.getElementAt(v);
					if (!oldCode.equals(newCode))
						pointManageNameStyles.pointStyleHandler.updateNameElementCodes(nameType, nameStylePID,
																				   oldCode, newCode);
				}

			// SECOND - call HBNameStyleManager to convert selected TMG style to an HRE one
			// with a new name and a new elementCode string; then delete old TMG style
				// Build the new element code string from convCodes
				String convCodeString = "";		//$NON-NLS-1$
				for (int v = 0; v < convCodesModel.getSize(); v++) {
					convCodeString = convCodeString + convCodesModel.getElementAt(v) + "|";		//$NON-NLS-1$
				}
				// Setup count of number of changes made in T160 records
					int substitutions = 0;		// for count of number of changes made
				// Create new style name as HRE + old name with new element code list
				// and update all records holding the old name style
					try {
						pointStyleHandler.addNewTableRow(styleIndex, false, nameType, convCodeString);
						substitutions = pointStyleHandler.updateNameStyleRPID(styleIndex, nameType);
						String messageDelete = HG0524Msgs.Text_164			// TMG style replaced for
								+ substitutions + HG0524Msgs.Text_165;		// xxx name entries
						pointStyleHandler.setNameStyleTable(nameType);
						// and then delete old TMG style
						pointStyleHandler.deleteNameStyleTableRow(styleIndex, nameType);
				// Now show number of changes
						setCursor(Cursor.getDefaultCursor());
					    JDialog dialog = new JDialog();
					    dialog.setAlwaysOnTop(true);
						JOptionPane.showMessageDialog(dialog,
													messageDelete,
													HG0524Msgs.Text_188,		// Convert a Name Style
													JOptionPane.INFORMATION_MESSAGE);
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0527 converting style: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
						errorMessage("HBE00","Convert TMG style name to HRE error\n"+ hbe.getMessage(), 0, nameType); //$NON-NLS-1$ //$NON-NLS-2$
					}

				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0527ConvertNameStyle");	//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0527ConvertNameStyle constructor

/**
 * allListCellRenderer - set allElements entries bold if they match TMG list
 */
	class allListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		    for (int i = 0; i < chosenElementsModel.getSize(); i++) {
		        if (value.equals(chosenElementsModel.getElementAt(i))) {
		        	Font f = label.getFont();
		        	label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		        }
			}
      		return this;
        }
    }		// End allListCellRenderer

/**
 * checkAllDone(DefaultListModel<String>) - to check all elements converted
 * @param convCodes - the converted Codes Model
 */
	private boolean checkAllDone(DefaultListModel<String> convCodes) {
        // Check if all converted Codes are other than '0000' - if true, all conversion
    	// has been done and we can turn on the 'Perform Conversion' button
    		boolean allDone = true;		// assume all done to start with
    		for (int k = 0; k < chosenElementsModel.getSize(); k++) {
    			if ("0000".equals(convCodes.getElementAt(k))) allDone = false; //$NON-NLS-1$
    		}
			return allDone;
	}		// End checkAllDone

/**
 * checkCodeUsed(DefaultListModel<String>, String) - to check if chosen code already in use
 * @param convCodes - the converted Codes Model
 * @param codeToTest
 */
	private boolean checkCodeUsed(DefaultListModel<String> convCodes, String codeToTest) {
    		boolean used = false;		// assume not used to start with
    		for (int k = 0; k < chosenElementsModel.getSize(); k++) {
    			if (codeToTest.equals(convCodes.getElementAt(k))) used = true;
    		}
			return used;
	}		// End checkCodeUsed

}		// End HG0527ConvertNameStyle
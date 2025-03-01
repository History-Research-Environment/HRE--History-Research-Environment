package hre.gui;
/***********************************************************************************
* Administer Name Styles - Specification 05.24 GUI_NameStyleEdit
************************************************************************************
* v0.01.0026 2021-04-23 initial draft (D Ferguson)
* 			 2021-09-16 Apply tag codes to screen control buttons (D Ferguson)
* v0.01.0027 2022-08-24 Renamed to HG0524ManageNameStyles for Pers/Locn sub-classes (D Ferguson)
* 			 2022-08-26 add Element Rename and better button control (D Ferguson)
*			 2022-09-06 Error fix in name element update (D Ferguson)
*			 2022-09-07 Activated name style data from database (N. Tolleshaug)
* v0.01.0028 2022-10-06 Add Rename button for NS Elements (D Ferguson)
* 			 2022-10-27 Change layout; add output style section (D Ferguson)
* 			 2022-11-08 Change layout; add output desc and defaults (D Ferguson)
* 			 2022-11-12 Activated output style data from database (N. Tolleshaug)
* 			 2022-11-13 Adjust button settings for Output area (D Ferguson)
* 			 2022-11-18 Set default chkbox; use radiobutton for out-type(D Ferguson)
* 			 2022-11-24 Activated setting of style type in database (N. Tolleshaug)
* 			 2022-12-14 Remove Rename button for NS Elements (D Ferguson)
* 			 2023-01-02 Error setting styleOutIndex - global variable (N. Tolleshaug)
* 			 2023-01-23 Add Convert button (D Ferguson)
* 			 2023-01-25 Implemented Convert TMG and new exception handling (N. Tolleshaug)
* 			 2023-01-31 NLS converted (D Ferguson)
* v0.01.0029 2023-04-14 Convert function modified (D Ferguson)
* v0.03.0030 2023-10-06 Remove Source radio-button (D Ferguson) (Text_58 now unused)
* v0.03.0031 2024-10-01 Clean whitespace (D Ferguson)
* v0.04.0032 2025-02-23 Fix crash when invoking Reminder (D Ferguson)
*
**********************************************************************************
*/

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBNameStyleManager;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0524Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Admin Name Styles
 * @author D Ferguson
 * @version v0.04.0032
 * @param HBNameStyleManager pointStyleHandler
 * @since 2022-10-16
 */

public class HG0524ManageNameStyles extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private JPanel contents;
	private CaretListener nameDescChange = null;
	private CaretListener outDescChange = null;
	private char outType;

	HBNameStyleManager pointStyleHandler;
	HG0524ManageNameStyles pointManageNameStyles = this;
	HBWhereWhenHandler pointWhereWhenhandler = null;
	String nameType;
	int styleIndex;
	int styleOutIndex;

	// Define List Models etc to hold data for HG0525/526 to load/save
	//  namestyleModel/JList to contain list of existing Name Styles as loaded from database
	DefaultListModel<String> namestyleModel = new DefaultListModel<>();
	JList<String> list_Styles = new JList<>(namestyleModel);

	//  namestyleModel/JList to contain list of existing Name Output Styles as loaded from database
	DefaultListModel<String> nameOutstyleModel = new DefaultListModel<>();
	JList<String> list_OutStyles = new JList<>(nameOutstyleModel);

	//  elementsModel to contain list of ALL Name Elements of the name type (Pers/Locn/etc)
	DefaultListModel<String> allElementsModel = new DefaultListModel<>();
	JList<String> list_AllElements = new JList<>(allElementsModel);
	//  model to hold ALL Name Element Codes list so they can be updated. Never displayed so no JList defined
	DefaultListModel<String> allCodesModel = new DefaultListModel<>();

	//  chosenElementsModel to contain list of the Name Elements chosen by user to belong to selected Name Style
	DefaultListModel<String> chosenElementsModel = new DefaultListModel<>();
	//  model to hold Chosen Name Element Codes list so they can be updated. Never displayed so no JList defined
	DefaultListModel<String> chosenCodesModel = new DefaultListModel<String>();

	//  chosenOutElementsModel to contain list of the Name Elements chosen to belong to Output Name Style
	DefaultListModel<String> chosenOutElementsModel = new DefaultListModel<>();
	//  model to hold Chosen Name Output Element Codes list so they can be updated. Never displayed so no JList defined
	DefaultListModel<String> chosenOutCodesModel = new DefaultListModel<String>();

	//  txtpnDesc to contain description of the selected Name Style - set from HG0525/526
	JTextPane txtpnDesc = new JTextPane();

	//  txtpnOutDesc to contain description of the selected Output Name Style - set from HG0525/526
	JTextPane txtpnOutDesc = new JTextPane();

	// Default checkbox - set from HG0525/526
	JCheckBox chk_Default = new JCheckBox();

	// Define buttons with Listeners or reference in HG0525/526
	JButton btn_Copy = new JButton(HG0524Msgs.Text_20);			// Copy
	JButton btn_Rename = new JButton(HG0524Msgs.Text_22);		// Rename
	JButton btn_Delete = new JButton(HG0524Msgs.Text_24);		// Delete
	JButton btn_Save = new JButton(HG0524Msgs.Text_0);			// Save Style Changes
	JButton btn_OutSave = new JButton(HG0524Msgs.Text_1);		// Save Output Changes
	JButton btn_Close = new JButton(HG0524Msgs.Text_2);			// Close
	JButton btn_Convert = new JButton(HG0524Msgs.Text_3);		// Convert

/**
 * Set name type style parameter
 */
	protected void setNameType(String nameType) {
		this.nameType = nameType;
	}

/**
 * Create the Dialog.
 * @param <NameStyleData>
 **/
	public HG0524ManageNameStyles(HBProjectOpenData pointOpenProject, HBNameStyleManager pointStyleHandler) {
		this.pointStyleHandler =  pointStyleHandler;
		pointWhereWhenhandler = pointOpenProject.getWhereWhenHandler();
		this.pointOpenProject = pointOpenProject;
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][][][][]", "[]5[][]10[]5[]10[]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		// Setup Column 0 layout - Usage notes and All Element list
		JLabel lbl_Notes = new JLabel(HG0524Msgs.Text_8);		// How to use this screen
		contents.add(lbl_Notes, "cell 0 0"); //$NON-NLS-1$

		JTextArea txtHelp = new JTextArea(16, 1);	// 16 rows max
		txtHelp.setBorder(UIManager.getBorder("TextField.border")); //$NON-NLS-1$
		txtHelp.setAlignmentY(Component.TOP_ALIGNMENT);
		txtHelp.setOpaque(false);
		txtHelp.setEditable(false);
		txtHelp.setLineWrap(true);
	    txtHelp.setWrapStyleWord(true);
	    Font font = UIManager.getFont("TextArea.font");	//$NON-NLS-1$
	    txtHelp.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));		// Set text size/font to current JTattoo setting
		txtHelp.setText(HG0524Msgs.Text_11 +		// Elements of a Name Style may be selected from the list below.\n\n
						HG0524Msgs.Text_12 +		// Selecting a Name Style in the 2nd column allows editing its description and content.\n\n
						HG0524Msgs.Text_13 +		// Selecting an Output Style in the 3rd column allows editing its description and content.\n\n
						HG0524Msgs.Text_14 +		// TMG imported Styles are restricted in how they may be changed.\n\n
						HG0524Msgs.Text_15);		// See the Help information for full details for using this screen.
		JScrollPane helpScroll = new JScrollPane(txtHelp);
		helpScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll as needed
		txtHelp.setCaretPosition(0);	// set scrollbar to top
		contents.add(helpScroll, "cell 0 1 1 4, grow"); //$NON-NLS-1$

		JLabel lbl_Elements = new JLabel(HG0524Msgs.Text_16);		// All Style Elements
		contents.add(lbl_Elements, "cell 0 5"); //$NON-NLS-1$

		list_AllElements.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_AllElements.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_Elements = new JScrollPane(list_AllElements);
		Color savedColor = scroll_Elements.getViewport().getView().getBackground();
		scroll_Elements.setPreferredSize(new Dimension(200, 275));
		contents.add(scroll_Elements, "cell 0 6"); //$NON-NLS-1$

		JLabel lbl_Find = new JLabel(HG0524Msgs.Text_17);		// Find Element:
		contents.add(lbl_Find, "cell 0 7"); //$NON-NLS-1$

		JTextField textToFind = new JTextField();
		contents.add(textToFind, "cell 0 7, growx, gapx 10"); //$NON-NLS-1$

		// Setup Column 1 layout - control buttons
		btn_Convert.setPreferredSize(new Dimension(80, 21));
		btn_Convert.setEnabled(false);
		contents.add(btn_Convert, "cell 1 0,alignx right");		//$NON-NLS-1$

		btn_Copy.setPreferredSize(new Dimension(80, 21));
		btn_Copy.setEnabled(false);
		contents.add(btn_Copy, "flowy,cell 1 1,alignx right"); //$NON-NLS-1$

		btn_Rename.setPreferredSize(new Dimension(80, 21));
		btn_Rename.setEnabled(false);
		contents.add(btn_Rename, "flowy,cell 1 1,alignx right"); //$NON-NLS-1$

		btn_Delete.setPreferredSize(new Dimension(80, 21));
		btn_Delete.setEnabled(false);
		contents.add(btn_Delete, "cell 1 1,alignx right"); //$NON-NLS-1$

		JLabel lbl_Default = new JLabel(HG0524Msgs.Text_26);		// Default?
		contents.add(lbl_Default, "cell 1 2, alignx right"); //$NON-NLS-1$
		contents.add(chk_Default, "cell 1 2, alignx right, gapx 10"); //$NON-NLS-1$

		JButton btn_RenameElemnt = new JButton(HG0524Msgs.Text_29);		// << Rename Element
		btn_RenameElemnt.setHorizontalAlignment(SwingConstants.LEFT);
		btn_RenameElemnt.setEnabled(false);
		contents.add(btn_RenameElemnt, "flowy,cell 1 6,growx"); //$NON-NLS-1$

		JButton btn_DefineNew = new JButton(HG0524Msgs.Text_31);		// << Define New Element
		btn_DefineNew.setHorizontalAlignment(SwingConstants.LEFT);
		btn_DefineNew.setEnabled(false);
		contents.add(btn_DefineNew, "flowy,cell 1 6,growx"); //$NON-NLS-1$

		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_Up = new JButton(HG0524Msgs.Text_33, upArrow);		// Move
		btn_Up.setVerticalTextPosition(SwingConstants.BOTTOM);
		btn_Up.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_Up.setEnabled(false);
		contents.add(btn_Up, "flowy,cell 1 6,alignx right,gapy 20"); //$NON-NLS-1$

		JButton btn_Choose = new JButton(HG0524Msgs.Text_35);		// >> Choose Element >>
		btn_Choose.setEnabled(false);
		contents.add(btn_Choose, "flowy,cell 1 6,growx"); //$NON-NLS-1$

		JButton btn_Remove = new JButton(HG0524Msgs.Text_37);		// Remove Element <<
		btn_Remove.setHorizontalAlignment(SwingConstants.RIGHT);
		btn_Remove.setEnabled(false);
		contents.add(btn_Remove, "flowy,cell 1 6,growx"); //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_Down = new JButton(HG0524Msgs.Text_33, downArrow);		// Move
		btn_Down.setVerticalTextPosition(SwingConstants.TOP);
		btn_Down.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_Down.setEnabled(false);
		contents.add(btn_Down, "cell 1 6,alignx right"); //$NON-NLS-1$

		// Setup Column 2 layout - Style Selection, Description and Chosen Element list
		JLabel lbl_Name = new JLabel(HG0524Msgs.Text_41);		// Select a Style
		contents.add(lbl_Name, "cell 2 0"); //$NON-NLS-1$

		list_Styles.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_Styles.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_Styles = new JScrollPane(list_Styles);
		scroll_Styles.setPreferredSize(new Dimension(200, 140));
		contents.add(scroll_Styles, "cell 2 1 1 2"); //$NON-NLS-1$

		JLabel lbl_Description = new JLabel(HG0524Msgs.Text_44);		// Style Description
		contents.add(lbl_Description, "cell 2 3"); //$NON-NLS-1$

		txtpnDesc.setContentType("text/plain"); //$NON-NLS-1$
		txtpnDesc.setPreferredSize(new Dimension(200, 60));
		txtpnDesc.setEditable(false);
		txtpnDesc.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		DefaultCaret caret = (DefaultCaret)txtpnDesc.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll_Desc = new JScrollPane(txtpnDesc);
	    contents.add(scroll_Desc, "cell 2 4, growy"); //$NON-NLS-1$

		JLabel lbl_Chosen = new JLabel(HG0524Msgs.Text_48);		// Elements of Selected Style
		contents.add(lbl_Chosen, "cell 2 5");	 //$NON-NLS-1$

		JList<String> list_ChosenElements = new JList<>(chosenElementsModel);
		list_ChosenElements.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_ChosenElements.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_Chosen = new JScrollPane(list_ChosenElements);
		scroll_Chosen.setPreferredSize(new Dimension(200, 275));
		contents.add(scroll_Chosen, "flowy,cell 2 6"); //$NON-NLS-1$

		contents.add(btn_Save, "cell 2 7,alignx center"); //$NON-NLS-1$

		// Setup Column 3 layout - control buttons
		JButton btn_OutCopy = new JButton(HG0524Msgs.Text_52);		// Copy
		btn_OutCopy.setPreferredSize(new Dimension(80, 21));
		btn_OutCopy.setEnabled(false);
		contents.add(btn_OutCopy, "flowy,cell 3 1,alignx right"); //$NON-NLS-1$

		JButton btn_OutRename = new JButton(HG0524Msgs.Text_54);		// Rename
		btn_OutRename.setPreferredSize(new Dimension(80, 21));
		btn_OutRename.setEnabled(false);
		contents.add(btn_OutRename, "flowy,cell 3 1,alignx right"); //$NON-NLS-1$

		// Button group for Output type radio buttons
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton buttonR = new JRadioButton(HG0524Msgs.Text_56);		// Report
		buttonR.setIconTextGap(10);
		buttonR.setMargin(new Insets(2, 2, 0, 2));
		buttonR.setHorizontalTextPosition(SwingConstants.LEADING);
		buttonR.setEnabled(false);
		JRadioButton buttonD = new JRadioButton(HG0524Msgs.Text_57);		// Display
		buttonD.setIconTextGap(10);
		buttonD.setMargin(new Insets(0, 2, 0, 2));
		buttonD.setHorizontalTextPosition(SwingConstants.LEADING);
		buttonD.setEnabled(false);
		buttonGroup.add(buttonR);
		buttonGroup.add(buttonD);
		contents.add(buttonR, "cell 3 2, flowy, alignx right"); //$NON-NLS-1$
		contents.add(buttonD, "cell 3 2, alignx right"); //$NON-NLS-1$

		JButton btn_OutDelete = new JButton(HG0524Msgs.Text_62);		// Delete
		btn_OutDelete.setPreferredSize(new Dimension(80, 21));
		btn_OutDelete.setEnabled(false);
		contents.add(btn_OutDelete, "flowy,cell 3 1,alignx right"); //$NON-NLS-1$

		JButton btn_OutUp = new JButton(HG0524Msgs.Text_33, upArrow);		// Move
		btn_OutUp.setVerticalTextPosition(SwingConstants.BOTTOM);
		btn_OutUp.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_OutUp.setEnabled(false);
		contents.add(btn_OutUp, "flowy,cell 3 6,alignx right,gapy 20"); //$NON-NLS-1$

		JButton btn_OutChoose = new JButton(HG0524Msgs.Text_66);		// >> Choose Element >>
		btn_OutChoose.setEnabled(false);
		contents.add(btn_OutChoose, "flowy,cell 3 6,growx"); //$NON-NLS-1$

		JButton btn_OutRemove = new JButton(HG0524Msgs.Text_68);		// Remove Element <<
		btn_OutRemove.setHorizontalAlignment(SwingConstants.RIGHT);
		btn_OutRemove.setEnabled(false);
		contents.add(btn_OutRemove, "flowy,cell 3 6,growx"); //$NON-NLS-1$

		JButton btn_OutDown = new JButton(HG0524Msgs.Text_33, downArrow);	// Move
		btn_OutDown.setVerticalTextPosition(SwingConstants.TOP);
		btn_OutDown.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_OutDown.setEnabled(false);
		contents.add(btn_OutDown, "cell 3 6,alignx right"); //$NON-NLS-1$

		// Setup Column 4 layout - Output Style list, Description and Output element list
		JLabel lbl_OutName = new JLabel(HG0524Msgs.Text_72);		// Associated Output Style(s)
		contents.add(lbl_OutName, "cell 4 0"); //$NON-NLS-1$

		list_OutStyles.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_OutStyles.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_OutStyles = new JScrollPane(list_OutStyles);
		scroll_OutStyles.setPreferredSize(new Dimension(200, 140));
		contents.add(scroll_OutStyles, "cell 4 1 1 2"); //$NON-NLS-1$

		JLabel lbl_OutDescription = new JLabel(HG0524Msgs.Text_75);		// Output Style Description
		contents.add(lbl_OutDescription, "cell 4 3"); //$NON-NLS-1$

		txtpnOutDesc.setContentType("text/plain"); //$NON-NLS-1$
		txtpnOutDesc.setPreferredSize(new Dimension(200, 60));
		txtpnOutDesc.setEditable(false);
		txtpnOutDesc.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		DefaultCaret caretOut = (DefaultCaret)txtpnOutDesc.getCaret();
		caretOut.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll_OutDesc = new JScrollPane(txtpnOutDesc);
	    contents.add(scroll_OutDesc, "cell 4 4, growy"); //$NON-NLS-1$

		JLabel lbl_OutChosen = new JLabel(HG0524Msgs.Text_79);		// Elements of Output Style
		contents.add(lbl_OutChosen, "cell 4 5");	 //$NON-NLS-1$

		JList<String> list_ChosenOutElements = new JList<>(chosenOutElementsModel);
		list_ChosenOutElements.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list_ChosenOutElements.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scroll_OutChosen = new JScrollPane(list_ChosenOutElements);
		scroll_OutChosen.setPreferredSize(new Dimension(200, 250));
		contents.add(scroll_OutChosen, "flowy, cell 4 6"); //$NON-NLS-1$

		JButton btn_OutComma = new JButton(HG0524Msgs.Text_82);		// Add/Remove Comma
		btn_OutComma.setEnabled(false);
		contents.add(btn_OutComma, "flowy,cell 4 6,alignx center"); //$NON-NLS-1$

		contents.add(btn_OutSave, "cell 4 7,alignx left"); //$NON-NLS-1$
		contents.add(btn_Close, "cell 4 7,gapx 10"); //$NON-NLS-1$

		pack();

/**
 * CREATE ALL ACTION LISTENERS
 **/
		// Listener for an entry in TextField textToFind
		textToFind.getDocument().addDocumentListener(new DocumentListener() {
	          @Override
	          public void insertUpdate(DocumentEvent e) {
	        	  list_AllElements.clearSelection();
	              findTheText();
	          }
	          @Override
	          public void removeUpdate(DocumentEvent e) {
	        	  list_AllElements.clearSelection();
	        	  findTheText();
	          }
	          @Override
	          public void changedUpdate(DocumentEvent e) {
	        	  list_AllElements.clearSelection();
	        	  findTheText();
	          }
	          private void findTheText() {
	        	  String text = textToFind.getText();
	        	  if (text.length() == 0) return;		// return if text is now null
	      	      int textIndex = list_AllElements.getNextMatch(text, 0, javax.swing.text.Position.Bias.Forward);
	    	      if (textIndex < 0) return;			// return if index has no matches
	    	      list_AllElements.ensureIndexIsVisible(textIndex);		// make sure selected item visible in scrollpane
	    	      list_AllElements.setSelectedIndex(textIndex);	 		// highlight it
	          }
		});

		// Listener for an edit in JTextPane txtpnDesc
		nameDescChange = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				btn_Save.setEnabled(true);
			}
		};
		txtpnDesc.addCaretListener(nameDescChange);

		// Listener for an edit in JTextPane txtpnOutDesc
		outDescChange = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				btn_OutSave.setEnabled(true);
			}
		};
		txtpnOutDesc.addCaretListener(outDescChange);

		// Listener for default JCheckBox. Sets default and removes old default
		ItemListener dft_Listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
            	if (arg0.getStateChange() == ItemEvent.SELECTED) {
          		if (HGlobal.DEBUG) System.out.println(styleIndex + " Default Selected!"); //$NON-NLS-1$
            		pointStyleHandler.setNameStyleIsDefault(styleIndex);
            		btn_Delete.setEnabled(false);
            		btn_Save.setEnabled(true);
            	}
            }
        };
		chk_Default.addItemListener(dft_Listener);

		// Listener for a selection in the list of Name Styles
		list_Styles.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eStyle) {
				if (!eStyle.getValueIsAdjusting()) {
					// Get the selected index
					styleIndex = list_Styles.getSelectedIndex();
		            if (styleIndex != -1) {
			        // IF a TMG style is selected, clear the AllElement display and set its color to match GUI
			            if (pointStyleHandler.getNameStyleIsTMG(styleIndex)) {
			            	allElementsModel.clear();
			        		allCodesModel.clear();
			        		scroll_Elements.getViewport().getView().setBackground(getContentPane().getBackground());
			            	}
			            // else reload it (in case the last selection was a TMG style) and reset color
			            else {
			            	allElementsModel.clear();
			        		allCodesModel.clear();
			        		scroll_Elements.getViewport().getView().setBackground(savedColor);
			        		pointStyleHandler.getAllElements(allElementsModel);
			        		pointStyleHandler.getAllCodes(allCodesModel);
			            }
		            // Clear selections in other lists
		                list_AllElements.clearSelection();
		                list_ChosenElements.clearSelection();
	                	chosenCodesModel.clear();
	                	chosenElementsModel.clear();
	                // Get the chosen elements of this style
	                	pointStyleHandler.setNameStyleData(styleIndex);
	            		pointStyleHandler.getChosenCodes(chosenCodesModel);
	            		pointStyleHandler.getChosenElements(chosenElementsModel);
	                // Disable JTextPane listener while we load the text, then re-enable
	                	txtpnDesc.removeCaretListener(nameDescChange);
	                	txtpnDesc.setText(pointStyleHandler.getNameStyleText(styleIndex));
	                	txtpnDesc.addCaretListener(nameDescChange);

	                /* Check if this is default style, set chkbox appropriately.
	                   If checked on, it cannot be unchecked (only an unchecked
	                   chk_Default can be turned on, which disables the current default item) */
	                	// disable chk_Default listener while we do this setting
	                	chk_Default.removeItemListener(dft_Listener);
	                	if (pointStyleHandler.getNameStyleIsDefault(styleIndex)) {
	                					chk_Default.setSelected(true);
	                					chk_Default.setEnabled(false);
	                	} else {
	                		chk_Default.setSelected(false);
	                		chk_Default.setEnabled(true);
	                	}
	                	chk_Default.addItemListener(dft_Listener);

	            	// Set up outputNameStyles list
	            		try {
							pointStyleHandler.setOutputStyleTable(nameType);
						} catch (HBException hbe) {
							System.out.println("Selection Listener error: " + hbe.getMessage()); //$NON-NLS-1$
							errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
						}
	            		nameOutstyleModel.clear();
	            		pointStyleHandler.getOutputStyles(nameOutstyleModel);
	            		list_OutStyles.clearSelection();
	            		styleOutIndex = 0;

	            	// Clear any previous Output selections and description
	            		buttonGroup.clearSelection();
	            		chosenOutCodesModel.clear();
	                	chosenOutElementsModel.clear();
	                	txtpnOutDesc.removeCaretListener(outDescChange);
	                	txtpnOutDesc.setText(null);
	                	txtpnOutDesc.addCaretListener(outDescChange);

		            // Test Name Styles
			            if (HGlobal.DEBUG)
			            	System.out.println(" Name Style type -"    //$NON-NLS-1$
			            		+ " System: " + pointStyleHandler.getNameStyleIsSystem(styleIndex)   //$NON-NLS-1$
			            		+ ", Default: "+ pointStyleHandler.getNameStyleIsDefault(styleIndex)   //$NON-NLS-1$
			            		+ ", TMG: "+ pointStyleHandler.getNameStyleIsTMG(styleIndex)); //$NON-NLS-1$

		            // Set valid buttons for this list
			            // If a TMG Name Style, do not allow Copy, but do allow Convert
			            if (pointStyleHandler.getNameStyleIsTMG(styleIndex)) {
							btn_Copy.setEnabled(false);
							btn_Convert.setEnabled(true);
							}
			            	else {
								btn_Copy.setEnabled(true);
								btn_Convert.setEnabled(false);
								}
			            // If an HRE System supplied style, do not allow Rename
			            if (pointStyleHandler.getNameStyleIsSystem(styleIndex)) btn_Rename.setEnabled(false);
			            	else btn_Rename.setEnabled(true);
			            // Allow Delete if more than 1 style exists
			            if (namestyleModel.getSize() > 1) btn_Delete.setEnabled(true);
			            // EXCEPT don't allow Delete of an HRE system supplied Name Style
			            if (pointStyleHandler.getNameStyleIsSystem(styleIndex)) btn_Delete.setEnabled(false);
			            txtpnDesc.setEditable(true);

                	// Disable all other buttons
			            btn_Remove.setEnabled(false);
			            btn_Choose.setEnabled(false);
			            btn_RenameElemnt.setEnabled(false);
			            btn_DefineNew.setEnabled(false);
			            btn_Up.setEnabled(false);
			            btn_Down.setEnabled(false);
			            btn_OutCopy.setEnabled(false);
			            btn_OutRename.setEnabled(false);
			            btn_OutDelete.setEnabled(false);
			            btn_OutChoose.setEnabled(false);
			            btn_OutComma.setEnabled(false);
					}
		        }
		    }
		});

		// Listener for a selection in the list of Output Name Styles
		list_OutStyles.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eStyle) {
		        if (!eStyle.getValueIsAdjusting()) {
					// Get the selected index
		        	styleOutIndex = list_OutStyles.getSelectedIndex();
		            if (styleOutIndex != -1) {
		            	// Clear selections in other lists
		                list_ChosenElements.clearSelection();
	                	chosenOutCodesModel.clear();
	                	chosenOutElementsModel.clear();
	                	pointStyleHandler.setOutputStyleData(styleOutIndex);
	            		pointStyleHandler.getOutputCodes(chosenOutCodesModel);
	                	pointStyleHandler.getOutputElements(chosenOutElementsModel);
	                // Disable JTextPane listener while we load the text, then re-enable
	                	txtpnOutDesc.removeCaretListener(outDescChange);
	                	txtpnOutDesc.setText(pointStyleHandler.getOutputStyleText(styleOutIndex));
	                	txtpnOutDesc.addCaretListener(outDescChange);

	                // Enable and Set the appropriate Output style-type radiobutton
	                	outType = pointStyleHandler.getOutputStyleType(styleOutIndex);
	                	buttonR.setEnabled(true);
	                	buttonD.setEnabled(true);
	                	if (outType == 'R') buttonR.setSelected(true);
	                	if (outType == 'D') buttonD.setSelected(true);

		            // Test Output Styles
	                	if (HGlobal.DEBUG)
	                		System.out.println("Output Style -"   //$NON-NLS-1$
	                			    +  " System: " + pointStyleHandler.getOutputStyleIsSystem(styleOutIndex)  //$NON-NLS-1$
	            					+ ", TMG: " + pointStyleHandler.getOutputStyleIsTMG(styleOutIndex) //$NON-NLS-1$
	                				+ ", OutType: " + pointStyleHandler.getOutputStyleType(styleOutIndex) //$NON-NLS-1$
				            		+ ", Last D: " + pointStyleHandler.lastDtypeOutNS(styleOutIndex, nameType)); //$NON-NLS-1$

		            // Set valid buttons for this Output list
	                	// If a TMG Output style, do not allow Copy
	                	if (pointStyleHandler.getOutputStyleIsTMG(styleOutIndex)) btn_OutCopy.setEnabled(false);
	                		else btn_OutCopy.setEnabled(true);
	                	// If an HRE System supplied Output style, do not allow Rename
	                	if (pointStyleHandler.getOutputStyleIsSystem(styleOutIndex)) btn_OutRename.setEnabled(false);
	                		else btn_OutRename.setEnabled(true);

	                	// Allow Delete if more than 1 Output style exists
	                	if (nameOutstyleModel.getSize() > 1) btn_OutDelete.setEnabled(true);
	                	// BUT don't allow Delete if # of Display/Report Output styles left are at their minimum
	                	if (outType == 'D' ) {
	                		if (pointStyleHandler.lastDtypeOutNS(styleIndex, nameType)) btn_OutDelete.setEnabled(false);
	                			else btn_OutDelete.setEnabled(true);
	                	}
	                	if (outType == 'R' ) {
	                		if (pointStyleHandler.lastRtypeOutNS(styleIndex, nameType)) btn_OutDelete.setEnabled(false);
	                			else btn_OutDelete.setEnabled(true);
	                	}
						// EXCEPT don't allow Delete of an HRE system supplied Output style
						if (pointStyleHandler.getOutputStyleIsSystem(styleOutIndex)) btn_OutDelete.setEnabled(false);

	                	txtpnOutDesc.setEditable(true);
	                	btn_OutComma.setEnabled(false);

                	// Disable other buttons
	                	btn_OutChoose.setEnabled(false);
	                	btn_OutRemove.setEnabled(false);
	                	btn_OutUp.setEnabled(false);
	                	btn_OutDown.setEnabled(false);
	                	btn_Remove.setEnabled(false);
	                	btn_Choose.setEnabled(false);
	                	btn_Rename.setEnabled(false);
	                	btn_Up.setEnabled(false);
	                	btn_Down.setEnabled(false);
		        	}
		        }
		    }
		});

		// Listener for a selection in list_AllElements
		list_AllElements.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eAll) {
		        if (!eAll.getValueIsAdjusting()) {
					// Get the selected index
		            if (list_AllElements.getSelectedIndex() != -1)
		               	list_ChosenElements.clearSelection();
		           // Don't allow update of any sort if IS_SYSTEM & IS_TMG true
		            if (pointStyleHandler.getNameStyleIsSystem(styleIndex) & pointStyleHandler.getNameStyleIsTMG(styleIndex)) return;
		           // Setup valid buttons for only this list
		            btn_Remove.setEnabled(false);
	            	btn_Choose.setEnabled(true);
	            	btn_RenameElemnt.setEnabled(true);
                	btn_DefineNew.setEnabled(true);
                	btn_Up.setEnabled(false);
			        btn_Down.setEnabled(false);
		        }
		    }
		});

		// Listener for a selection in list_ChosenElements
		list_ChosenElements.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eCh) {
		        if (!eCh.getValueIsAdjusting()) {
		            if (list_ChosenElements.getSelectedIndex() != -1)
	                	list_AllElements.clearSelection();
			        // Don't allow update of any sort if IS_SYSTEM & IS_TMG true
		            if (pointStyleHandler.getNameStyleIsSystem(styleIndex) & pointStyleHandler.getNameStyleIsTMG(styleIndex)) return;
		            // Setup valid buttons for only this list
	            	btn_Remove.setEnabled(true);
	            	btn_Choose.setEnabled(false);
	            	btn_RenameElemnt.setEnabled(false);
                	btn_DefineNew.setEnabled(false);
                	// including the Output chosen buttons
                	if (list_OutStyles.getSelectedIndex() != -1) btn_OutChoose.setEnabled(true);
                	btn_OutRemove.setEnabled(false);
                	btn_OutUp.setEnabled(false);
			        btn_OutDown.setEnabled(false);
		            int sizeC = chosenElementsModel.getSize();
		            if (sizeC > 1) {
		            				btn_Up.setEnabled(true);
		            				btn_Down.setEnabled(true);
		            				}
		            	else {
		            		  btn_Up.setEnabled(false);
  					          btn_Down.setEnabled(false);
		            	}
		        }
		    }
		});

		// Listener for a selection in list_ChosenOutElements
		list_ChosenOutElements.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent oCh) {
		        if (!oCh.getValueIsAdjusting()) {
					// Get the selected index
		            if (list_ChosenOutElements.getSelectedIndex() != -1)
	                	list_ChosenElements.clearSelection();
			        // Don't allow update of any sort if IS_SYSTEM & IS_TMG true
		            if (pointStyleHandler.getOutputStyleIsSystem(styleOutIndex) & pointStyleHandler.getOutputStyleIsTMG(styleOutIndex)) return;
		            // Setup valid buttons for only this list
	            	btn_OutRemove.setEnabled(true);
	            	btn_OutChoose.setEnabled(false);
	            	btn_OutComma.setEnabled(true);
		            int sizeO = chosenOutElementsModel.getSize();
		            if (sizeO > 1) {
		            				btn_OutUp.setEnabled(true);
		            				btn_OutDown.setEnabled(true);
		            				}
		            	else {
		            		  btn_OutUp.setEnabled(false);
  					          btn_OutDown.setEnabled(false);
		            	}
		        }
		    }
		});

		/**********************************************************************
		 * Listeners for the edit and control buttons for changing a Name Style
		 **********************************************************************/
		// Listener for btn_Copy (copy a Name Style)
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// first get the entry name
				int indexS = list_Styles.getSelectedIndex();
				if (indexS == -1) return;
			// Insert new entry name as old name + '- Copy' and save it to DB
				try {
					pointStyleHandler.addNewTableRow(indexS, true , nameType, null);
					pointStyleHandler.setNameStyleTable(nameType);
				} catch (HBException hbe) {
					System.out.println("Listener for copy style button error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
				}
				namestyleModel.clear();
				pointStyleHandler.getNameStyles(namestyleModel);
			// Allow deletion
				btn_Delete.setEnabled(true);
			}
		});

		// Listener for btn_Rename (rename a Name Style)
		btn_Rename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// first get the entry name
				int indexS = list_Styles.getSelectedIndex();
				if (indexS == -1) return;
				String listEntry = namestyleModel.get(indexS);
				// now ask user for new name of this entry, max 30 chars
				String newName = (String) JOptionPane.showInputDialog(btn_Rename,
						HG0524Msgs.Text_98,		// Enter new Name to replace:
						HG0524Msgs.Text_100,	// Input New Style Name
						JOptionPane.QUESTION_MESSAGE,null,null, listEntry.trim());
				if (newName == null || newName.trim().isEmpty()) return;	// no name entered
				if (newName.length() > 30) newName = newName.substring(0, 30);
				// then rename the entry
				namestyleModel.set(indexS, newName);
				String newDescription = txtpnDesc.getText();

				try {
					pointStyleHandler.updateStyleName(indexS, newName, newDescription);
					pointStyleHandler.setNameStyleTable(nameType);
				} catch (HBException hbe) {
					System.out.println("Listener for Rename style button error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
				}
				namestyleModel.clear();
				pointStyleHandler.getNameStyles(namestyleModel);
				list_Styles.setSelectedIndex(indexS);	// re-select the entry
			}
		});

		// Listener for btn_Delete (delete a Name Style)
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Get the entry's index, delete it and clear screen areas
				int indexS = list_Styles.getSelectedIndex();
				if (indexS == -1) return;
				try {
					pointStyleHandler.deleteNameStyleTableRow(indexS, nameType);
					namestyleModel.remove(indexS);
					pointStyleHandler.setNameStyleTable(nameType);
					namestyleModel.clear();
					pointStyleHandler.getNameStyles(namestyleModel);
					chosenElementsModel.clear();
				// Reload Name Style 0 from T165 contents and select it
				    txtpnDesc.setText(pointStyleHandler.getNameStyleText(0));
					pointStyleHandler.getChosenElements(chosenElementsModel);
				// set default as selected
					list_Styles.setSelectedIndex(pointStyleHandler.getDefaultStyleIndex());
				// If only 1 entry left, disable delete button
				    if (namestyleModel.getSize() == 1) btn_Delete.setEnabled(false);
				} catch (HBException hbe) {
					if (HGlobal.DEBUG) System.out.println("Listener for Delete NS button error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
				}
			}
		});

		// Listener for btn_Up (move a Chosen Element and Code upwards)
		btn_Up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get index of element and code selected
				int indexC = list_ChosenElements.getSelectedIndex();
				String selectedElement = list_ChosenElements.getSelectedValue();	//get element value
				String selectedCode = chosenCodesModel.getElementAt(indexC);		//get code value
				// move the element and code up one in their lists
				if(indexC > 0) {
		            chosenElementsModel.remove(indexC);						// remove selected element
		            chosenElementsModel.add(indexC - 1, selectedElement);	// add the element up 1 in the list
		            list_ChosenElements.setSelectedIndex(indexC - 1);		// reset selection to the new element
		            chosenCodesModel.remove(indexC);						// remove related code
		            chosenCodesModel.add(indexC - 1, selectedCode);			// add the code up 1 in the list
		        }
				btn_Save.setEnabled(true);
			}
		});

		// Listener for btn_Choose (copy a Name Element to the Chosen list)
		btn_Choose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// first get the entry name selected
				int indexE = list_AllElements.getSelectedIndex();
				String listEntry = allElementsModel.get(indexE);
				String codeEntry = allCodesModel.get(indexE);
				// now check it's not already in the Chosen list
				if (chosenElementsModel.contains(listEntry)) return;
				// insert entry name into Chosen models for Elements and Codes
				chosenElementsModel.addElement(listEntry);
				chosenCodesModel.addElement(codeEntry);
				btn_Save.setEnabled(true);
			}
		});

		// Listener for btn_Remove (remove an Element from the Chosen lists)
		btn_Remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get the entry index and delete it
				int indexC = list_ChosenElements.getSelectedIndex();
				if (indexC == -1) return;
				// remove from Chosen models for Elements and Codes
				chosenElementsModel.remove(indexC);
				chosenCodesModel.remove(indexC);
			    if (chosenElementsModel.getSize() == 0) {		// if no entries left, disable buttons
			    				btn_Remove.setEnabled(false);
			    				btn_Up.setEnabled(false);
			    				btn_Down.setEnabled(false);
			    			}
			    btn_Save.setEnabled(true);
			}
		});

		// Listener for btn_Down (move a Chosen Element and Code downwards)
		btn_Down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get index of element and code selected
				int indexC = list_ChosenElements.getSelectedIndex();
				String selectedElement = list_ChosenElements.getSelectedValue();	//get element value
				String selectedCode = chosenCodesModel.getElementAt(indexC);		//get code value
				// move the element and code down one in their lists
				if( indexC < chosenElementsModel.getSize() -1 ){
					chosenElementsModel.remove(indexC);						// remove selected element
					chosenElementsModel.add(indexC + 1, selectedElement);	// add the element down 1 in the list
		            list_ChosenElements.setSelectedIndex(indexC + 1);		// reset selection to the new element
					chosenCodesModel.remove(indexC);						// remove related code
					chosenCodesModel.add(indexC + 1, selectedCode);			// add the code down 1 in the list
		        }
				btn_Save.setEnabled(true);
			}
		});

		// Listener for btn_RenameElement (rename an All Name Element)
		btn_RenameElemnt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Get currently selected item index
				int indexA = list_AllElements.getSelectedIndex();
				String renameElemnt = JOptionPane.showInputDialog(btn_RenameElemnt,
						HG0524Msgs.Text_103,										// Enter new name of selected Name Style Element
						HG0524Msgs.Text_104, JOptionPane.INFORMATION_MESSAGE);		// Rename Element
				if (renameElemnt != null && renameElemnt.length() > 0 ) {
					// rename element at that point
					allElementsModel.set(indexA, renameElemnt);
		        }
				// Set Save button on, as DB update required
			    btn_Save.setEnabled(true);
			}
		});

		// Listener for btn_Define (define a new Name Element in the All list)
		btn_DefineNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newNameElemnt = JOptionPane.showInputDialog(btn_DefineNew,
						HG0524Msgs.Text_105											// Enter name of new Name Style Element.\n\n
						+ HG0524Msgs.Text_106										// The new element will be placed AFTER the currently\n
						+ HG0524Msgs.Text_107, 										// selected entry in the list of All Elements
						HG0524Msgs.Text_108, JOptionPane.INFORMATION_MESSAGE);		// Define New Element
				// If we get a non-null name entered, then proceed
				if (newNameElemnt != null && newNameElemnt.length() > 0 ) {
					// Check the element doesn't already exist in the list
					if (allElementsModel.contains(newNameElemnt)) {
						JOptionPane.showMessageDialog(btn_DefineNew,
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
					String codeAfter = ""; //$NON-NLS-1$
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
			        	if (HGlobal.DEBUG) System.out.println(" NameStyles number error: " + ex.getMessage() ); //$NON-NLS-1$
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("Error: HG0524ManageNameStyles: " + ex.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(ex);
						}
			        }
					String newCodeElemnt = Integer.toString((numBefore+numAfter)/2);

				//Set leading "0" if only 3 chars
					if (newCodeElemnt.length() == 3) newCodeElemnt = "0" + newCodeElemnt; //$NON-NLS-1$
				// Now insert the new Name Element Code into the code List
					allCodesModel.add(indexA+1, newCodeElemnt);

					btn_Save.setEnabled(true);
				}
			}
		});

		/*************************************************************************
		 * Listeners for the edit and control buttons for changing an Output Style
		 *************************************************************************/
		// Listener for btn_OutCopy (copy an Output Style)
		btn_OutCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// first get the entry name
				int indexOS = list_OutStyles.getSelectedIndex();
				if (indexOS == -1) return;
			// Insert new entry name as old name + '- Copy' and save it to DB
				try {
					pointStyleHandler.addNewOutTableRow(indexOS, nameType);
					pointStyleHandler.setOutputStyleTable(nameType);
				} catch (HBException hbe) {
					System.out.println("Change output style Listener error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
					hbe.printStackTrace();
				}
				nameOutstyleModel.clear();
				pointStyleHandler.getOutputStyles(nameOutstyleModel);
			// Allow deletion
				btn_OutDelete.setEnabled(true);
			}
		});

		// Listener for btn_OutRename (rename an Output Style)
		btn_OutRename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// first get the entry name
				int indexOS = list_OutStyles.getSelectedIndex();
				if (indexOS == -1) return;
				String listEntry = nameOutstyleModel.get(indexOS);
				// now ask user for new name of this entry, max 30 chars
				String newName = (String) JOptionPane.showInputDialog(btn_OutRename,
						HG0524Msgs.Text_98,			// Enter new Name to replace:
						HG0524Msgs.Text_100,		// Input New Style Name
						JOptionPane.QUESTION_MESSAGE, null, null, listEntry.trim());
				if (newName == null || newName.trim().isEmpty()) return;	// no name entered
				if (newName.length() > 30) newName = newName.substring(0, 30);
				// then rename the entry
				nameOutstyleModel.set(indexOS, newName);
				try {
					pointStyleHandler.updateOutputStyleName(indexOS, newName, txtpnDesc.getText());
					pointStyleHandler.setOutputStyleTable(nameType);
				} catch (HBException hbe) {
					System.out.println("Out rename Listener error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
				}
				nameOutstyleModel.clear();
				pointStyleHandler.getOutputStyles(nameOutstyleModel);
				list_OutStyles.setSelectedIndex(indexOS);	// re-select the entry
			}
		});

		// Listener for btn_OutDelete (delete an Output Style)
		btn_OutDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Get the entry's index, delete it and clear screen areas
				int indexOS = list_OutStyles.getSelectedIndex();
				if (indexOS == -1) return;
				try {
					pointStyleHandler.deleteOutputStyleTableRow(indexOS,nameType);
					nameOutstyleModel.remove(indexOS);
					pointStyleHandler.setOutputStyleTable(nameType);
					nameOutstyleModel.clear();
					pointStyleHandler.getOutputStyles(nameOutstyleModel);
					chosenOutElementsModel.clear();
				// Reload Style 0 from T163 contents and select it
					pointStyleHandler.getChosenElements(chosenOutElementsModel);
					list_OutStyles.setSelectedIndex(0);
				// If only 1 entry left, disable delete button
				    if (nameOutstyleModel.getSize() == 1) btn_OutDelete.setEnabled(false);
				} catch (HBException hbe) {
					if (HGlobal.DEBUG) System.out.println("Listener for delete out button error: " + hbe.getMessage()); //$NON-NLS-1$
					errorMessage(hbe.getErrorCode(), hbe.getMessage(), hbe.getValue(), nameType);
				}
			}
		});

		// Listener for all Output type radioButtons
		ActionListener btn_Listener = new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent buttonEvent) {
	        		int styleOutIndex = list_OutStyles.getSelectedIndex();
	        	// Check number of Display/Report type styles present
	        		boolean minDtype = pointStyleHandler.lastDtypeOutNS(styleOutIndex, nameType);
	        		boolean minRtype = pointStyleHandler.lastRtypeOutNS(styleOutIndex, nameType);
	        	// Check current radio button setting - don't allow a change FROM a D type if minDtype true
	        		if (outType == 'D' & minDtype) {
						JOptionPane.showMessageDialog(buttonR,
								HG0524Msgs.Text_123, 								// You cannot change this Output Style Type
								HG0524Msgs.Text_124, JOptionPane.ERROR_MESSAGE);	// Output Type Setting
						buttonD.setSelected(true);	// force it back to D type
						return;
					}
		        // Check current radio button setting - don't allow a change FROM an R type if minRtype true
	        		if (outType == 'R' & minRtype) {
						JOptionPane.showMessageDialog(buttonR,
								HG0524Msgs.Text_123, 								// You cannot change this Output Style Type
								HG0524Msgs.Text_124, JOptionPane.ERROR_MESSAGE);	// Output Type Setting
						buttonR.setSelected(true);	// force it back to R type
						return;
					}
	        		if(buttonEvent.getSource() == buttonR)
	        			pointStyleHandler.setOutStyleType(styleOutIndex, "R"); //$NON-NLS-1$
	        		if(buttonEvent.getSource() == buttonD)
	        			pointStyleHandler.setOutStyleType(styleOutIndex, "D"); //$NON-NLS-1$
	        		btn_OutSave.setEnabled(true);
	        }
	    };
		buttonR.addActionListener(btn_Listener);
		buttonD.addActionListener(btn_Listener);

		// Listener for btn_OutUp (move an Output Element and Code upwards)
		btn_OutUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get index of element and code selected
				int indexOC = list_ChosenOutElements.getSelectedIndex();
				String selectedElement = list_ChosenOutElements.getSelectedValue();	//get element value
				String selectedCode = chosenOutCodesModel.getElementAt(indexOC);		//get code value
				// move the element and code up one in their lists
				if(indexOC > 0) {
		            chosenOutElementsModel.remove(indexOC);						// remove selected element
		            chosenOutElementsModel.add(indexOC - 1, selectedElement);	// add the element up 1 in the list
		            list_ChosenOutElements.setSelectedIndex(indexOC - 1);		// reset selection to the new element
		            chosenOutCodesModel.remove(indexOC);						// remove related code
		            chosenOutCodesModel.add(indexOC - 1, selectedCode);			// add the code up 1 in the list
		        }
				btn_OutSave.setEnabled(true);
			}
		});

		// Listener for btn_OutChoose (copy an Element to the Output list)
		btn_OutChoose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// first get the entry name selected
				int indexOE = list_ChosenElements.getSelectedIndex();
				String listEntry = chosenElementsModel.get(indexOE);
				String codeEntry = chosenCodesModel.get(indexOE);
				// now check it's not already in the Output list
				if (chosenOutElementsModel.contains(listEntry)) return;
				// insert entry name into Chosen Output models for Elements and Codes
				chosenOutElementsModel.addElement(listEntry);
				chosenOutCodesModel.addElement(codeEntry);
				btn_OutSave.setEnabled(true);
			}
		});

		// Listener for btn_OutRemove (remove an Element from the Output list)
		btn_OutRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get the entry index and delete it
				int indexOC = list_ChosenOutElements.getSelectedIndex();
				if (indexOC == -1) return;
				// remove from Output models for Elements and Codes
				chosenOutElementsModel.remove(indexOC);
				chosenOutCodesModel.remove(indexOC);
			    if (chosenOutElementsModel.getSize() == 0) {		// if no entries left, disable buttons
			    				btn_OutRemove.setEnabled(false);
			    				btn_OutUp.setEnabled(false);
			    				btn_OutDown.setEnabled(false);
			    				btn_OutComma.setEnabled(false);
			    			}
			    btn_OutSave.setEnabled(true);
			}
		});

		// Listener for btn_OutDown (move an Output Element and Code downwards)
		btn_OutDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get index of element and code selected
				int indexOC = list_ChosenOutElements.getSelectedIndex();
				String selectedElement = list_ChosenOutElements.getSelectedValue();		//get element value
				String selectedCode = chosenOutCodesModel.getElementAt(indexOC);		//get code value
				// move the element and code down one in their lists
				if( indexOC < chosenOutElementsModel.getSize() -1 ){
					chosenOutElementsModel.remove(indexOC);						// remove selected element
					chosenOutElementsModel.add(indexOC + 1, selectedElement);	// add the element down 1 in the list
		            list_ChosenOutElements.setSelectedIndex(indexOC + 1);		// reset selection to the new element
					chosenOutCodesModel.remove(indexOC);						// remove related code
					chosenOutCodesModel.add(indexOC + 1, selectedCode);			// add the code down 1 in the list
		        }
				btn_OutSave.setEnabled(true);
			}
		});

		// Listener for btn_OutComma (add/remove a comma to Element & Code in the Output list)
		btn_OutComma.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// get the entry index
				int indexOC = list_ChosenOutElements.getSelectedIndex();
				if (indexOC == -1) return;
				// Edit Output models for Elements and Codes
				// If the entry has a comma, remove it; else add a comma
				// For the code entry, add/remove the literal'#,'
				String listEntry = chosenOutElementsModel.get(indexOC);
				String codeEntry = chosenOutCodesModel.get(indexOC);
				if (listEntry.endsWith(",")) { //$NON-NLS-1$
						chosenOutElementsModel.set(indexOC, listEntry.substring(0, listEntry.length()-1));	// reduce length by 1
						chosenOutCodesModel.set(indexOC, codeEntry.substring(0, codeEntry.length()-2));		// reduce length by 2
					}
				else {
						chosenOutElementsModel.set(indexOC, listEntry + ","); //$NON-NLS-1$
						chosenOutCodesModel.set(indexOC, codeEntry + "#,"); //$NON-NLS-1$
					}
			    btn_OutSave.setEnabled(true);
			}
		});

		// Listener for clicking 'X on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});

	}	// End HG0524 constructor

/**
 * resetElementLists()
 * @throws HBException
 */
	protected void resetElementLists() throws HBException {
		pointStyleHandler.resetStyleTable(nameType, styleIndex);

	// Set changed style in WhereWhenHandler
		pointWhereWhenhandler.setlocationStyleChanged(true);

	// Load list of ALL Style Elements from T163
		allElementsModel.clear();
		allCodesModel.clear();

	// if TMG do not reset allElementList
		if (!pointStyleHandler.isTmgNameStyle[list_Styles.getSelectedIndex()])
			pointStyleHandler.getAllElements(allElementsModel);
		pointStyleHandler.getAllCodes(allCodesModel);

	// Reset style elements list
    	chosenElementsModel.clear();
    	chosenCodesModel.clear();
    	pointStyleHandler.setNameStyleData(styleIndex);
    	pointStyleHandler.getChosenCodes(chosenCodesModel);
    	pointStyleHandler.getChosenElements(chosenElementsModel);

   // Reset output style elements list
    	if (list_OutStyles.getSelectedIndex() >= 0) {
    	// If selected output style -->
	    	chosenOutCodesModel.clear();
	    	chosenOutElementsModel.clear();
	    	pointStyleHandler.setOutputStyleData(styleOutIndex);
			pointStyleHandler.getOutputCodes(chosenOutCodesModel);
	    	pointStyleHandler.getOutputElements(chosenOutElementsModel);
    	}
	}

	protected void dumpElementCodes(DefaultListModel<String> elements) {
		String codeString = ""; //$NON-NLS-1$
		for (int i = 0; i < elements.getSize(); i++)
			codeString = codeString + elements.get(i) + "|"; //$NON-NLS-1$
		System.out.println(" Codes: " + codeString); //$NON-NLS-1$
	}

/**
 * errorMessage(String errCode, String userMessage, int value, String nameType)
 * @param errCode
 * @param userMessage
 * @param value
 * @param nameType
 * *****************************************
 * Only messages to user need translation.
 * System errors can be presented in English
 * *****************************************
 */
	protected void errorMessage(String errCode, String userMessage, int value, String nameType) {
		  String dialogMessage;
	      JDialog dialog = new JDialog();
	      dialog.setAlwaysOnTop(true);
	      if (userMessage.startsWith("HBE00")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE01")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE02")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE03")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE04")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE05")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE06")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE07")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE08")) dialogMessage = HG0524Msgs.Text_143; //$NON-NLS-1$		// You cannot delete the default name style
	      else if (errCode.startsWith("HBE09")) dialogMessage = HG0524Msgs.Text_145	//$NON-NLS-1$  		// You cannot delete this name style\n as it is used
	    		  												+ value + HG0524Msgs.Text_146; 			// times
	      else if (errCode.startsWith("HBE10")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE11")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE12")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE13")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE14")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE15")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE16")) dialogMessage = userMessage; //$NON-NLS-1$
	      else if (errCode.startsWith("HBE17")) dialogMessage = userMessage; //$NON-NLS-1$
	      else dialogMessage = HG0524Msgs.Text_155 + userMessage;						// Unknown error code
	      JOptionPane.showMessageDialog(dialog, dialogMessage, HG0524Msgs.Text_156,		// Name Style Handling
	    		  JOptionPane.ERROR_MESSAGE);
	}

}
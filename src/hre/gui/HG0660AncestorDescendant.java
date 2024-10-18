package hre.gui;
 /*****************************************************************************************
 * HB0660AncestorDescendant
 * v0.01.0023 2020-09-02 Created as an Ancestor/Descendant report writer (D Ferguson)
 *            2020-09-03 Converted to use HG0450SuperDialog (D Ferguson)
 *            2020-09-30 Fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2020-11-18 Implemented name display options DISP/SORT (N. Tolleshaug)
 * 			  2021-02-28 NLS implemented (D Ferguson)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0030 2023-08-05 - Enable update status bar - on/off (N. Tolleshaug)
 ***************************************************************************************/

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import hre.bila.HB0631OutputComponent;
import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBTreeCreator;
import hre.bila.HBTreeCreator.GenealogyTree;
import hre.nls.HG0660Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Class HG0660AncestorDescendant
 * @version 0.01.0026
 * @since 2020-09-02
 */
public class HG0660AncestorDescendant extends HG0450SuperDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	HBPersonHandler pointPersonHandler;
	HBProjectOpenData pointOpenProject;

	private String screenID = "66000"; //$NON-NLS-1$
	private JPanel contents, leftPane, rightPane;

    private static String SHOW_ANCESTOR_CMD = "showAncestor"; //$NON-NLS-1$
    private static String SHOW_DESCEND_CMD = "showDescendant";  //$NON-NLS-1$
    private static String EXPAND_ALL_CMD = "expandAll"; //$NON-NLS-1$
    private static String COLLAPSE_ALL_CMD = "collapseAll"; //$NON-NLS-1$
    private static String EXPAND_CMD = "expand"; //$NON-NLS-1$
    private static String COLLAPSE_CMD = "collapse"; //$NON-NLS-1$

    HBTreeCreator pointTree;
	GenealogyTree tree;
	int focusPerson = 1;

/**
 * Constructor
 */
    public HG0660AncestorDescendant(HBProjectOpenData openProject, HBPersonHandler pointPersonHand) throws HBException {
    	super(pointPersonHand);
    	this.pointPersonHandler = pointPersonHand;
    	this.pointOpenProject = openProject;
		// Setup references for HG0450
		windowID = screenID;
		helpName = "ancestdescendtree"; //$NON-NLS-1$

		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0660 Anc-Dec Tree Report");} //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Set pointer to HBProjectOpenData
		String projectName = openProject.getProjectName();
		setTitle(HG0660Msgs.Text_0 + projectName);

		setBounds(100, 100, 650, 650);
		// Setup contents
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][grow]", "[grow]10[]10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JLabel treeTitle = new JLabel(HG0660Msgs.Text_13);
		toolBar.add(treeTitle);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		// Add the content pane action buttons
		JButton btn_Print = new JButton(HG0660Msgs.Text_17);
		btn_Print.setEnabled(false);
		btn_Print.setMargin(new Insets(2, 16, 2, 16));
	    contents.add(btn_Print, "cell 1 1, align right, gapx 15, tag apply"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0660Msgs.Text_15);
		btn_Close.setMargin(new Insets(2, 16, 2, 16));
	    contents.add(btn_Close, "cell 1 1, align right, gapx 15, tag cancel"); //$NON-NLS-1$

		// Layout the left and right panes
	    leftPane = new JPanel();
	    leftPane.setBorder(UIManager.getBorder("FileChooser.listViewBorder")); //$NON-NLS-1$
		leftPane.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		contents.add(leftPane, "cell 0 0, top"); //$NON-NLS-1$
	    rightPane = new JPanel();
	    rightPane.setBorder(UIManager.getBorder("ScrollPane.border")); //$NON-NLS-1$
		rightPane.setLayout(new MigLayout("insets 10", "[grow]", "[]10[grow]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		contents.add(rightPane, "cell 1 0, top"); //$NON-NLS-1$

		// Leftpane contents
		JLabel leftTitle = new JLabel(HG0660Msgs.Text_29);
		leftTitle.setFont(leftTitle.getFont().deriveFont(leftTitle.getFont().getStyle() | Font.BOLD));
		leftPane.add(leftTitle, "cell 0 0, center"); //$NON-NLS-1$

		JLabel getPerson = new JLabel(HG0660Msgs.Text_31);

		leftPane.add(getPerson, "cell 0 1"); //$NON-NLS-1$

	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(1000000);			// << needs setting to max personID
	    JFormattedTextField inputPerson = new JFormattedTextField(formatter);
	    inputPerson.setColumns(7);
	    leftPane.add(inputPerson, "cell 0 1");  	    	     //$NON-NLS-1$

		JCheckBox headerBox = new JCheckBox(HG0660Msgs.Text_34);
		headerBox.setEnabled(false);
		headerBox.setSelected(false);
		headerBox.setToolTipText(HG0660Msgs.Text_35);
		leftPane.add(headerBox, "cell 0 2");  //$NON-NLS-1$

		JCheckBox footerBox = new JCheckBox(HG0660Msgs.Text_37);
		footerBox.setEnabled(false);
		footerBox.setSelected(false);
		footerBox.setToolTipText(HG0660Msgs.Text_38);
		leftPane.add(footerBox, "cell 0 3");  //$NON-NLS-1$

		JRadioButton showAncestor = new JRadioButton(HG0660Msgs.Text_40, true);
		showAncestor.setEnabled(false);
		JRadioButton showDescendant = new JRadioButton(HG0660Msgs.Text_41);
		showDescendant.setEnabled(false);
        showAncestor.addActionListener(this);
        showDescendant.addActionListener(this);
        showAncestor.setActionCommand(SHOW_ANCESTOR_CMD);
        showDescendant.setActionCommand(SHOW_DESCEND_CMD);
	    ButtonGroup bGroup = new ButtonGroup();
	    bGroup.add(showAncestor);
	    bGroup.add(showDescendant);
	    leftPane.add(showAncestor, "cell 0 4");	     //$NON-NLS-1$
	    leftPane.add(showDescendant, "cell 0 5"); //$NON-NLS-1$

	    JButton expandAll = new JButton(HG0660Msgs.Text_44);
	    expandAll.setEnabled(false);
	    expandAll.addActionListener(this);
	    expandAll.setActionCommand(EXPAND_ALL_CMD);
	    leftPane.add(expandAll, "cell 0 6, grow"); //$NON-NLS-1$
	    JButton expandOne = new JButton(HG0660Msgs.Text_46);
	    expandOne.setEnabled(false);
	    expandOne.addActionListener(this);
	    expandOne.setActionCommand(EXPAND_CMD);
	    leftPane.add(expandOne, "cell 0 7, grow");	 //$NON-NLS-1$

		JButton collapseAll = new JButton(HG0660Msgs.Text_48);
		collapseAll.setEnabled(false);
	    collapseAll.addActionListener(this);
	    collapseAll.setActionCommand(COLLAPSE_ALL_CMD);
	    leftPane.add(collapseAll, "cell 0 8, grow"); //$NON-NLS-1$
	    JButton collapseOne = new JButton(HG0660Msgs.Text_50);
	    collapseOne.setEnabled(false);
	    collapseOne.addActionListener(this);
	    collapseOne.setActionCommand(COLLAPSE_CMD);
	    leftPane.add(collapseOne, "cell 0 9, grow"); //$NON-NLS-1$

		JTextArea reportNote = new JTextArea(HG0660Msgs.Text_52);
		reportNote.setFont(reportNote.getFont().deriveFont(reportNote.getFont().getStyle() | Font.ITALIC));
		reportNote.setEditable(false);
		reportNote.setColumns(20);
		reportNote.setLineWrap(true);
		reportNote.setWrapStyleWord(true);
		leftPane.add(reportNote, "cell 0 10"); //$NON-NLS-1$

	    // Rightpane contents
		JTextField headerField = new JTextField();
		headerField.setHorizontalAlignment(SwingConstants.CENTER);
		headerField.setVisible(false);
		headerField.setText(null);
		headerField.setColumns(30);
		rightPane.add(headerField, "cell 0 0, center"); //$NON-NLS-1$

		// Do not create a tree yet - need to select a person first
        JScrollPane scrollTree = new JScrollPane(tree);
        scrollTree.setMinimumSize(new Dimension(250, 200));
        scrollTree.setPreferredSize(new Dimension(2000, 2000));
		rightPane.add(scrollTree, "cell 0 1,grow"); //$NON-NLS-1$

		JTextField footerField = new JTextField();
		footerField.setHorizontalAlignment(SwingConstants.CENTER);
		footerField.setVisible(false);
		footerField.setText(null);
		footerField.setColumns(30);
		rightPane.add(footerField, "cell 0 2, center"); //$NON-NLS-1$

/**
 * CREATE ACTION BUTTON LISTENERS
 **/
		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0660 Anc-Dec Tree Report"); //$NON-NLS-1$
			// close reminder display
				if (reminderDisplay != null) reminderDisplay.dispose();
				dispose();
			}		// return to main menu
		});

		// Listener for clicking 'X on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});

		// Listener for Print button - print the rightPanel
		btn_Print.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent arg0) {
		    	// If Header or Footer are used, print the whole panel; else just the tree
		    	try {
		    	if (headerField.getText().isEmpty() && footerField.getText().isEmpty()) new HB0631OutputComponent(tree);
		    		else new HB0631OutputComponent(rightPane);
		    	} catch (HBException hbe) {
					  JOptionPane.showMessageDialog(null, "Printer error: " + hbe.getMessage(),  //$NON-NLS-1$
							  						"Print Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
		    	}
		    }
		});

		// Listener to handle the selected Person
		inputPerson.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Set (or reset) Tree for the selected person
			focusPerson = ((Integer) inputPerson.getValue()).intValue();

			// Set name Display index for this instance of HBTreeCreator
			int nameDisplayIndex = openProject.getNameDisplayIndex();

			try {
	     		//pointPersonHandler.processPartnerList(pointOpenProject);
				pointPersonHandler.enableUpdateMonitor(false);
	     		pointPersonHandler.initiatePersonData(5, pointOpenProject);
				pointTree = new HBTreeCreator(openProject, focusPerson, nameDisplayIndex, null);
			} catch (HBException hbe) {
				System.out.println("HBTreeCreator call error");  //$NON-NLS-1$
				hbe.printStackTrace();
			}
			int errorCode = pointTree.initateTree();

			if (errorCode > 0) {
				userInfoTreeCreator(errorCode);
				return;
			}
			tree = pointTree.getTree();
			scrollTree.setViewportView(tree);
			// Now enable all buttons
			headerBox.setEnabled(true);
			footerBox.setEnabled(true);
			showAncestor.setEnabled(true);
			showDescendant.setEnabled(true);
			expandAll.setEnabled(true);
			expandOne.setEnabled(true);
			collapseAll.setEnabled(true);
			collapseOne.setEnabled(true);
			btn_Print.setEnabled(true);
			}
		});

		// Checkbox listeners for selecting header/footer
		headerBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent ae) {
            	headerField.setVisible(true);
                headerField.setEnabled(headerBox.isSelected());
            }
        });
		footerBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent ae) {
            	footerField.setVisible(true);
                footerField.setEnabled(footerBox.isSelected());
            }
        });

    }		// End HG0660AncestorDescendant constructor

	// Listeners for formatting button to drive HBTreeCreator actions
	@Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand() == SHOW_ANCESTOR_CMD) {
        	pointTree.setFamView(true);
        }
        if (ae.getActionCommand() == SHOW_DESCEND_CMD) {
        	pointTree.setFamView(false);
        }
        if (ae.getActionCommand() == EXPAND_ALL_CMD) {
        	pointTree.expandAll();
        }
        if (ae.getActionCommand() == COLLAPSE_ALL_CMD) {
        	pointTree.collapseAll();
        }
        if (ae.getActionCommand() == EXPAND_CMD) {
        	pointTree.expandOne();
        }
        if (ae.getActionCommand() == COLLAPSE_CMD) {
        	pointTree.collapseOne();
        }
	}

/**
 * GUI user messages
 * @param errorCode
 */
	private void userInfoTreeCreator(int errorCode) {
		String errorMess = ""; //$NON-NLS-1$
		String errorTitle = HG0660Msgs.Text_53;
		if (errorCode > 0) {
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("Action: failed in HG0660 TreeCreation");	 //$NON-NLS-1$
			if (errorCode == 1)  errorMess = HG0660Msgs.Text_54 +  HG0660Msgs.Text_55;
			JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoTreeCreator

}	// End HG0660AncestorDescendant

package hre.gui;
/**********************************************************************************
 * Project Status - Specification 06.20 GUI_DB Maintenance
 * v0.00.0025 2021-02-12 initial draft (D Ferguson)
 * 			  2021-03-07 Add confirmation of action messages (D Ferguson)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2021-11-30 Check for no projects open condition (D Ferguson)
 * v0.04.0032 2025-05-20 Perform NLS update (D Ferguson)
 * 			  2026-01-04 Log catch block errors (D Ferguson)
 * 			  2026-01-18 Add ability to export an HRE table to CSV file (D Ferguson)
 * 			  2026-01-19 Handle H2 LOB export and just display a byte-count (D Ferguson)
 * 			  2026-01-24 Update NLS for latest chnages (D Ferguson)
 **********************************************************************************/

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0620Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project DB Maintenance
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2021-02-12
 */

public class HG0620DBMaintenance extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "62000"; //$NON-NLS-1$
	private static JPanel contents;
	private static String selected = ""; //$NON-NLS-1$

	// For CSV output formatting
	private final Map<Class<?>, Function<Object,String>> formatters = new HashMap<>();

/**
 * Create the Dialog.
 */
	public HG0620DBMaintenance(HBProjectOpenData pointOpenProject) {

		// Setup references for HG0450
		windowID = screenID;
		helpName = "dbmaintenance"; //$NON-NLS-1$

		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		setTitle(HG0620Msgs.Text_4);		// Database Maintenance
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0620DBMaintenance"); //$NON-NLS-1$

		// Heading
		JLabel heading = new JLabel(HG0620Msgs.Text_5); 	// Select the action to be performed, then Execute
		contents.add(heading, "cell 0 0"); //$NON-NLS-1$

		// Button group for radio buttons
		String reset = HG0620Msgs.Text_9;		// Perform reset of all screen configuration data
		String relation = HG0620Msgs.Text_11;	// Refresh Relationship settings
		String validate = HG0620Msgs.Text_10;	// Perform validation of all data in project database
		String tableDump = HG0620Msgs.Text_12;	// Extract an HRE table to a CSV file

		JRadioButton buttonA = new JRadioButton(reset);
		JRadioButton buttonB = new JRadioButton(relation);
		JRadioButton buttonC = new JRadioButton(validate);
		JRadioButton buttonD = new JRadioButton(tableDump);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonA);
		buttonGroup.add(buttonB);
		buttonGroup.add(buttonC);
		buttonGroup.add(buttonD);
		contents.add(buttonA, "cell 0 1"); //$NON-NLS-1$
		contents.add(buttonB, "cell 0 2"); //$NON-NLS-1$
		contents.add(buttonC, "cell 0 3"); //$NON-NLS-1$
		contents.add(buttonD, "cell 0 4"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0620Msgs.Text_13);	// Cancel
		contents.add(btn_Cancel, "cell 0 5, align right, gapx 15, tag cancel"); //$NON-NLS-1$

		JButton btn_Execute = new JButton(HG0620Msgs.Text_15);	// Execute
		contents.add(btn_Execute, "cell 0 5, align right, gapx 15, tag ok"); //$NON-NLS-1$

		pack();

/**
* Create formatting registry for handling different CSV data types
* BIGINT values (long) are made to look like text so Excel doesn't put it into scientific format
* BLOB/CLOB values are set to eventually identify the type and show how many bytes exist
**/
	// ---- Excel-safe numeric types ----
        Function<Object,String> excelSafe = o -> "=\"" + o.toString() + "\"";	//$NON-NLS-1$ //$NON-NLS-2$
        formatters.put(Long.class, excelSafe);     // BIGINT
        formatters.put(Integer.class, excelSafe);  // INTEGER
        formatters.put(Short.class, excelSafe);    // SMALLINT
        formatters.put(Byte.class, excelSafe);     // TINYINT
    // ---- Boolean ----
        formatters.put(Boolean.class, o -> ((Boolean)o) ? "TRUE" : "FALSE");	//$NON-NLS-1$ //$NON-NLS-2$
     // ---- Character / Varchar ----
        formatters.put(String.class, o -> (String)o);
     // ---- Timestamp ----
        formatters.put(Timestamp.class, o -> ((Timestamp)o).toInstant().toString());

/**
* Create Action Listeners
**/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting out of HG0620DBMaintenance"); //$NON-NLS-1$
				dispose();
		    }
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0620DBMaintenance"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Execute button
		btn_Execute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.numOpenProjects == 0) {
					JOptionPane.showMessageDialog(btn_Execute, HG0620Msgs.Text_19,	// There are no projects open to \nperform Maintenance on.
							HG0620Msgs.Text_20, JOptionPane.ERROR_MESSAGE);			// Maintenance
				} else {
			        	if (selected.equals(reset)) {
			        		if (HGlobal.writeLogs)
			        			HB0711Logging.logWrite("Action: performing screen config reset in HG0620DBMaintenance"); //$NON-NLS-1$
			        		try {
			        			if (!pointOpenProject.pointT302_GUI_CONFIG.isClosed()) {
									pointOpenProject.pointGuiData.deleteRecords(pointOpenProject.pointT302_GUI_CONFIG);
									pointOpenProject.pointGuiData.createT302data(pointOpenProject.pointT302_GUI_CONFIG,true);
									int dbIndex = pointOpenProject.getOpenDatabaseIndex();
									pointOpenProject.loadT302data(dbIndex);
									JOptionPane.showMessageDialog(btn_Execute,
													HG0620Msgs.Text_22, // Performed reset of screen configuration data
													HG0620Msgs.Text_20, // Maintenance
													JOptionPane.INFORMATION_MESSAGE);
			        			}
			        		} catch (HBException | HeadlessException | SQLException hbe) {
			        			if (HGlobal.writeLogs) {
			        				HB0711Logging.logWrite("ERROR: in HG0620 resetting T302 " + hbe.getMessage());	//$NON-NLS-1$
			        				HB0711Logging.printStackTraceToFile(hbe);
			        			}
							}

			        	}
						if (selected.equals(validate) || selected.equals(relation)) {
							JOptionPane.showMessageDialog(btn_Execute, HG0620Msgs.Text_26, // Action not yet available
																	   HG0620Msgs.Text_20, // Maintenance
																	   JOptionPane.INFORMATION_MESSAGE);
			        	}
						if (selected.equals(tableDump)) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: in HG0620 start table dump"); //$NON-NLS-1$
						// Get all table names and load to a combobox for display
							JComboBox<String> comboTables
										= new JComboBox<String>(new Vector<String>(pointOpenProject.getTableList()));
						// Build a JPanel to hold everything
							String instructions = HG0620Msgs.Text_27;	// Select the required table from the list below
							String title = HG0620Msgs.Text_28;	// Select Table to extract from HRE
							JPanel msgPanel = new JPanel();
							msgPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							JLabel label = new JLabel(instructions);
							msgPanel.add(label, "cell 0 0");			//$NON-NLS-1$
							msgPanel.add(comboTables, "cell 0 1");		//$NON-NLS-1$
							int btns = JOptionPane.OK_CANCEL_OPTION;
						// Show the JOptionPane msg
							int selection = JOptionPane.showConfirmDialog(btn_Execute, msgPanel, title, btns);
						// If if Cancel, exit now
							if (selection == 2) dispose();
						// Else write out selected file
							else {
								// Get table name
								String selectedTable = (String) comboTables.getSelectedItem();
								// Get the table's resultSet
								ResultSet requestedRS = pointOpenProject.getTableToExtract(selectedTable);
								// Build a filename in the HRE folder: User Home dir + /HRE/ + tablename + ".csv"
								String csvFile = System.getProperty("user.home") + File.separator	//$NON-NLS-1$
										+ "HRE" + File.separator + selectedTable + ".csv";	//$NON-NLS-1$ //$NON-NLS-2$
								// Write the table out to this CSV file
								try (Writer writer = new BufferedWriter(new FileWriter(csvFile))) {
									if (HGlobal.writeLogs)
										HB0711Logging.logWrite("Action: in HG00620 writing " + csvFile); //$NON-NLS-1$
									writeCSV(requestedRS, writer);
								} catch (SQLException | IOException ex) {
									if (HGlobal.writeLogs) {
										HB0711Logging.logWrite("ERROR: in HG0620 writing " + csvFile + ex.getMessage()); //$NON-NLS-1$
										HB0711Logging.printStackTraceToFile(ex);
										dispose();		// exit if errors
									}
								}
								// If it worked ok, confirm the action
								JOptionPane.showMessageDialog(btn_Execute,
										selectedTable + HG0620Msgs.Text_29,	// .csv \nwritten successfully
										title,
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
				}
				// If any action other than tableDump, then exit; else allow another table selection
				if (!selected.equals(tableDump)) dispose();
			}
		});

		// Listener for buttonA (reset)
		buttonA.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });
		// Listener for buttonB (relation)
		buttonB.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });
		// Listener for buttonC (Validate)
		buttonC.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });
		// Listener for buttonD (tableDump)
		buttonD.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });
	}	// End HG0620 constructor

/*
* writeCSV(ResultSet rs, Writer writer)
* @param rs
* @param writer
 */
    public void writeCSV(ResultSet rs, Writer writer) throws SQLException, IOException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        // ---- Header ----
        for (int i = 1; i <= columnCount; i++) {
            writer.write(escape(meta.getColumnLabel(i)));
            if (i < columnCount) writer.write(",");	//$NON-NLS-1$
        }
        writer.write("\n");	//$NON-NLS-1$

        // ---- Rows ----
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                int jdbcType = meta.getColumnType(i);
                Object obj = getTypedValue(rs, jdbcType, i);
                String value = format(obj, jdbcType);
                writer.write(escape(value));
                if (i < columnCount) writer.write(",");	//$NON-NLS-1$
            }
            writer.write("\n");	//$NON-NLS-1$
        }
    }		// End writeCSV

    // ---- Correct LOB retrieval ----
    private Object getTypedValue(ResultSet rs, int jdbcType, int index) throws SQLException {
        switch (jdbcType) {
          // Binary LOBs
            case java.sql.Types.BLOB:
            case java.sql.Types.LONGVARBINARY:
                return rs.getBlob(index);
          // Character LOBs
            case java.sql.Types.CLOB:
            case java.sql.Types.NCLOB:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                return rs.getClob(index);
            default:
                return rs.getObject(index);
        }
    }		// End getTypedValue

    // ---- Format based on JDBC type first, format registry second ----
    private String format(Object obj, int jdbcType) {
        if (obj == null) return "";	//$NON-NLS-1$
        switch (jdbcType) {
          // ---- BLOB ----
            case java.sql.Types.BLOB:
            case java.sql.Types.LONGVARBINARY:
                try {
                    Blob b = (Blob)obj;
                    return "<binary: " + b.length() + " bytes>";	//$NON-NLS-1$ //$NON-NLS-2$
                } catch (Exception e) {
                    return "<binary: unreadable>";		//$NON-NLS-1$
                }
          // ---- CLOB ----
            case java.sql.Types.CLOB:
            case java.sql.Types.NCLOB:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                try {
                    Clob c = (Clob)obj;
                    return "<text: " + c.length() + " bytes>";	//$NON-NLS-1$ //$NON-NLS-2$
                } catch (Exception e) {
                    return "<text: unreadable>";	//$NON-NLS-1$
                }
        }
        // ---- Non-LOB types → registry ----
        Function<Object,String> f = formatters.get(obj.getClass());
        if (f != null) return f.apply(obj);
        return obj.toString();
    }		// End format

    // ---- CSV escaping ----
    private static String escape(String value) {
        if (value == null) return "";	//$NON-NLS-1$
        boolean needsQuotes =
                value.contains(",") ||		//$NON-NLS-1$
                value.contains("\"") ||		//$NON-NLS-1$
                value.contains("\n");		//$NON-NLS-1$
        String escaped = value.replace("\"", "\"\"");		//$NON-NLS-1$ //$NON-NLS-2$
        return needsQuotes ? "\"" + escaped + "\"" : escaped;	//$NON-NLS-1$ //$NON-NLS-2$
    }		// End escape

}	// End HG0620DBMaintenance

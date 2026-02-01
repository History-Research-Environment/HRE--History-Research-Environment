package hre.gui;
/***********************************************************************************
 * Output (Printer and File) -  Specification 05.31 GUI_Output 2019-04-04
 * v0.00.0007 2019-02-22 by R Thompson, updated 2019-07-18 by D Ferguson
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 * v0.01.0024 2020-09-30 changed to MigLayout; fonts removed for JTattoo (D Ferguson)
 * v0.01.0025 2020-11-24 use location Setting for default report folder (D Ferguson)
 * v0.01.0026 2021-05-01 add option for TSV file output (D Ferguson)
 * v0.03.0030 2023-06-10 Allow CSV files to have commas in fields (D Ferguson)
 * 		      2023-06-19 Add iText-based PDF output file option (D Ferguson)
 * 			  2023-08-09 Add iText note and page number (D Ferguson)
 * 			  2023-09-28 Implement NLS (D Ferguson)
 * v0.03.0031 2024-10-01 Clean whitespace (D Ferguson)
 * v0.04.0032 2025-06-14 Modify headerfooter eventHandler for iText v9.2.0 (D Ferguson)
 * 			  2026-01-05 Log catch block errors (D Ferguson)
 ************************************************************************************/

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import hre.bila.HB0711Logging;
import hre.nls.HG0531Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Output (to print or file)
 * @author R Thompson
 * @version v0.04.0032
 * @since 2019-02-22
 *
 * Using iText v9.2 (AGPL version) for PDF file output
 */

public class HG0531Output extends JDialog {
	private static final long serialVersionUID = 001L;

	private JPanel contents;
	private JTextField headerField;
	private JTextField footerField;
	private String separatorType;	// for CSV or TSV (^ or tab)
	private String fileType;
	private JTable tableToOutput;
	String docHeader = null;

/**
 * Create the Dialog.
 * @param callingWindow - the calling Window's Title
 * @param tableToOutput - the JTable to be output to print or a file
 **/
	public HG0531Output(String callingWindow,  JTable tableToOutput) {
		this.tableToOutput = tableToOutput;

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0531Output");	//$NON-NLS-1$
		setTitle(HG0531Msgs.Text_0);		// Output Selector
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_RequestWindow = new JLabel(HG0531Msgs.Text_1);				// Requesting Window:
		contents.add(lbl_RequestWindow, "cell 0 0");	//$NON-NLS-1$

		JLabel lbl_RequestWindowData = new JLabel();
		lbl_RequestWindowData.setText(callingWindow);
		contents.add(lbl_RequestWindowData, "cell 1 0");	//$NON-NLS-1$

		JRadioButton rdbtn_Printer = new JRadioButton(HG0531Msgs.Text_2);		// Output to your Printer
		contents.add(rdbtn_Printer, "cell 0 1 2");	//$NON-NLS-1$

		JRadioButton rdbtn_CFile = new JRadioButton(HG0531Msgs.Text_3);			// Output to a CSV format File
		contents.add(rdbtn_CFile, "cell 0 2 2");	//$NON-NLS-1$

		JRadioButton rdbtn_TFile = new JRadioButton(HG0531Msgs.Text_4);			// Output to a TSV format File
		contents.add(rdbtn_TFile, "cell 0 3 2");	//$NON-NLS-1$

		JRadioButton rdbtn_PDFFile = new JRadioButton(HG0531Msgs.Text_5);		// Output to a PDF format File
		contents.add(rdbtn_PDFFile, "cell 0 4 2");	//$NON-NLS-1$

		// Make group of ONLY the file buttons
		ButtonGroup fileButtons = new ButtonGroup();
		fileButtons.add(rdbtn_CFile);
		fileButtons.add(rdbtn_TFile);
		fileButtons.add(rdbtn_PDFFile);

		JCheckBox headerBox = new JCheckBox(HG0531Msgs.Text_6);		// Header:
		headerBox.setVisible(false);
		headerBox.setSelected(true);
		headerBox.setToolTipText(HG0531Msgs.Text_7);		// Include a page header
		contents.add(headerBox, "cell 0 5, hidemode 3");	//$NON-NLS-1$

		headerField = new JTextField();
		headerField.setVisible(false);
		headerField.setText(HG0531Msgs.Text_8);				// Enter a Document Title
		headerField.setToolTipText(HG0531Msgs.Text_9);		// Page Header (Use {0} to include page number)
		headerField.setColumns(15);
		contents.add(headerField, "cell 1 5, hidemode 3");	//$NON-NLS-1$

		JCheckBox footerBox = new JCheckBox(HG0531Msgs.Text_10);	// Footer:
		footerBox.setVisible(false);
		footerBox.setSelected(true);
		footerBox.setToolTipText(HG0531Msgs.Text_11);		// Include a page footer
		contents.add(footerBox, "cell 0 6, hidemode 3");	//$NON-NLS-1$

		footerField = new JTextField();
		footerField.setVisible(false);
		footerField.setToolTipText(HG0531Msgs.Text_12);		// Page Footer (Use {0} to Include Page Number)
		footerField.setText(HG0531Msgs.Text_13);			// Page {0}
		footerField.setColumns(15);
		contents.add(footerField, "cell 1 6, hidemode 3");	//$NON-NLS-1$

		JButton btn_Write = new JButton();
		btn_Write.setVisible(false);
		contents.add(btn_Write, "cell 1 7,alignx right, hidemode 3");	//$NON-NLS-1$

		JButton btn_Print = new JButton(HG0531Msgs.Text_14);			// Print
		btn_Print.setVisible(false);
		contents.add(btn_Print, "cell 1 7,alignx right, hidemode 3");	//$NON-NLS-1$

		pack();

/*****************************
 * CREATE ALL BUTTON LISTENERS
 *****************************/
		// Checkbox listeners for selecting header/footer
		headerBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent ae) {
                headerField.setEnabled(headerBox.isSelected());
            }
        });
		footerBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent ae) {
                footerField.setEnabled(footerBox.isSelected());
            }
        });

		// Radio button for printing - show print fields for use
		rdbtn_Printer.addActionListener(new ActionListener(){
		    @Override
			public void actionPerformed(ActionEvent e) {
		    	fileButtons.clearSelection();	// clear file buttons in case they were active
		    	btn_Write.setVisible(false);
		    	rdbtn_Printer.setSelected(true);
		    	headerBox.setVisible(true);
		    	footerBox.setVisible(true);
		    	headerField.setVisible(true);
		    	footerField.setVisible(true);
		    	btn_Print.setVisible(true);
		    	pack();
		    }
		});

		// Print button to drive print dialog and final printing
		btn_Print.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// check if we should print a document header
			    	MessageFormat header = null;
			        if (headerBox.isSelected()) {
			            header = new MessageFormat(headerField.getText());
			        }
			        // check if we should print a footer
			        MessageFormat footer = null;
			        if (footerBox.isSelected()) {
			            footer = new MessageFormat(footerField.getText());
			        }
			        // set mode to always fit to width of page
					JTable.PrintMode mode = JTable.PrintMode.FIT_WIDTH;
			        // Table print - parameters are: Mode(fitWidth), header, footer, PrintDialog(true), PrintRequest(no), Interactive(true), PrintService(no)
			        try {
			            boolean complete = tableToOutput.print(mode, header, footer, true, null, true, null);
			            // when printing completes
			            if (complete) {
			                // show a success message
			                JOptionPane.showMessageDialog(btn_Print, HG0531Msgs.Text_15, 		// Printing Complete
			                										 HG0531Msgs.Text_16, 		// Printing Result
			                										 JOptionPane.INFORMATION_MESSAGE);
			                if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: print completed by HG0531Output");	//$NON-NLS-1$
			            } else {
			                // show a message indicating that printing was cancelled
			                JOptionPane.showMessageDialog(btn_Print, HG0531Msgs.Text_17, 		// Printing Cancelled
			                										 HG0531Msgs.Text_16, 		// Printing Result
			                										 JOptionPane.INFORMATION_MESSAGE);
			                if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: print cancelled in HG0531Output");		//$NON-NLS-1$
			            }
			        } catch (PrinterException pe) {
			            // Printing failed, report to the user
			            JOptionPane.showMessageDialog(btn_Print, HG0531Msgs.Text_19 + pe.getMessage(), 	// Printing Failed:
			            										 HG0531Msgs.Text_16, 					// Printing Result
			            										 JOptionPane.ERROR_MESSAGE);
			            if (HGlobal.writeLogs)
			            	HB0711Logging.logWrite("ERROR: in HG0531 print error " + pe.getMessage()); //$NON-NLS-1$
			        }
			        rdbtn_Printer.setSelected(false);				// de-select button once printing done
			        headerBox.setVisible(false); 					// and make all print selections invisible
			    	footerBox.setVisible(false);
			    	headerField.setVisible(false);
			    	footerField.setVisible(false);
			    	btn_Print.setVisible(false);
			    	pack();
				}
		});

		// Radio button for CSV file output drives FileChooser for file location
		rdbtn_CFile.addActionListener(new ActionListener(){
		    @Override
			public void actionPerformed(ActionEvent e) {
		    	fileType = "CSV";		//$NON-NLS-1$
		    	rdbtn_Printer.setSelected(false);				// de-select print (in case it is on)
		    	headerBox.setVisible(false); 					// and make all print selections invisible
		    	footerBox.setVisible(false);
		    	headerField.setVisible(false);
		    	footerField.setVisible(false);
		    	btn_Print.setVisible(false);
		    	pack();
		    	// Set the separator type for CSV files
                // For CSV files we want to allow fields to contain commas, so force
                // a special separator format, using ^ (unlikely to be part of the text)
		    	separatorType = "^";	//$NON-NLS-1$
		    	// Open fileChooser to set a location for the file
		    	rdbtn_CFile.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	HG0577FileChooser chooseFile
		    			= new HG0577FileChooser(HG0531Msgs.Text_22, 	// Select
		    									HG0531Msgs.Text_23,		// CSV files (*.csv)
		    									"csv", "Output.csv", HGlobal.pathHREreports, 1); //$NON-NLS-1$ //$NON-NLS-2$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = rdbtn_CFile.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets chooser screen top-left corner relative to that
				chooseFile.setVisible(true);
				rdbtn_CFile.setCursor(Cursor.getDefaultCursor());
		    	// Chosen filename and folder are stored in 'chosen' fields
				if (HGlobal.chosenFilename == "" ) { rdbtn_CFile.setSelected(false); }	 // do nothing if no filename setup		//$NON-NLS-1$
					else {  String lower = HGlobal.chosenFilename.toLowerCase();  								//make filename all lower case
							int test = lower.lastIndexOf(".csv");	//$NON-NLS-1$								// test if it ends in .csv
							if (test == -1)  HGlobal.chosenFilename = HGlobal.chosenFilename + ".csv";  		// if not, add .csv		//$NON-NLS-1$
						    btn_Write.setText(HG0531Msgs.Text_26 + HGlobal.chosenFilename);			// Write to file
						    btn_Write.setVisible(true);		// else make Write button Enabled
						    btn_Write.setEnabled(true);
						    pack();
						    }
			 			}
		});

		// Radio button for TSV file output drives FileChooser for file location
		rdbtn_TFile.addActionListener(new ActionListener(){
		    @Override
			public void actionPerformed(ActionEvent e) {
		    	fileType = "TSV";	//$NON-NLS-1$
		    	rdbtn_Printer.setSelected(false);				// de-select print (in case it is on)
		    	headerBox.setVisible(false); 					// and make all print selections invisible
		    	footerBox.setVisible(false);
		    	headerField.setVisible(false);
		    	footerField.setVisible(false);
		    	btn_Print.setVisible(false);
		    	pack();
		    	// Set the separator type for TSV files (tab)
		    	separatorType = "	 ";		// field is 'tab blank'	//$NON-NLS-1$
		    	// Open fileChooser to set a location for the file
		    	rdbtn_TFile.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	HG0577FileChooser chooseFile
		    			= new HG0577FileChooser(HG0531Msgs.Text_22,		// Select
		    									HG0531Msgs.Text_28,		// TSV files (*.tsv)
		    									"tsv", "Output.tsv", HGlobal.pathHREreports, 1); //$NON-NLS-1$ //$NON-NLS-2$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = rdbtn_TFile.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets chooser screen top-left corner relative to that
				chooseFile.setVisible(true);
				rdbtn_TFile.setCursor(Cursor.getDefaultCursor());
		    	// Chosen filename and folder are stored in 'chosen' fields
				if (HGlobal.chosenFilename == "" )  rdbtn_TFile.setSelected(false); 	 // do nothing if no filename setup				//$NON-NLS-1$
					else {  String lower = HGlobal.chosenFilename.toLowerCase();  								//make filename all lower case
							int test = lower.lastIndexOf(".tsv");	//$NON-NLS-1$								// test if it ends in .tsv
							if (test == -1)   HGlobal.chosenFilename = HGlobal.chosenFilename + ".tsv";  		// if not, add .tsv		//$NON-NLS-1$
						    btn_Write.setText(HG0531Msgs.Text_26 + HGlobal.chosenFilename);			// Write to file
						    btn_Write.setVisible(true);		// else make Write button Enabled
						    btn_Write.setEnabled(true);
						    pack();
						    }
			 			}
		});

		// Radio button for PDF file output: drives FileChooser for file location
		rdbtn_PDFFile.addActionListener(new ActionListener(){
		    @Override
			public void actionPerformed(ActionEvent e) {
		    	fileType = "PDF";	//$NON-NLS-1$
		    	rdbtn_PDFFile.setSelected(true);
		    	rdbtn_Printer.setSelected(false);		// de-select print (in case it is on)
		    	headerBox.setVisible(true); 			// make only headers valid
		    	headerField.setVisible(true);
				headerField.setToolTipText("");		//$NON-NLS-1$
		    	footerBox.setVisible(false);
		    	footerField.setVisible(false);
		    	btn_Print.setVisible(false);
		    	pack();

		    	// Open fileChooser to set a location for the file
		    	rdbtn_PDFFile.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	HG0577FileChooser chooseFile
		    			= new HG0577FileChooser(HG0531Msgs.Text_22, 		// Select
		    									HG0531Msgs.Text_33,			// PDF files (*.pdf)
		    									"pdf", "Output.pdf", HGlobal.pathHREreports, 1); //$NON-NLS-1$ //$NON-NLS-2$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = rdbtn_PDFFile.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets chooser screen top-left corner relative to that
				chooseFile.setVisible(true);
				rdbtn_PDFFile.setCursor(Cursor.getDefaultCursor());
		    	// Chosen filename and folder are stored in 'chosen' fields
				if (HGlobal.chosenFilename == "" ) { rdbtn_PDFFile.setSelected(false); }	//$NON-NLS-1$	 // do nothing if no filename setup
					else {  String lower = HGlobal.chosenFilename.toLowerCase();  								//make filename all lower case
							int test = lower.lastIndexOf(".pdf");	//$NON-NLS-1$								// test if it ends in .pdf
							if (test == -1)  HGlobal.chosenFilename = HGlobal.chosenFilename + ".pdf";  		// if not, add .pdf		//$NON-NLS-1$
						    btn_Write.setText(HG0531Msgs.Text_26 + HGlobal.chosenFilename);		// Write to file
						    btn_Write.setVisible(true);			// else make Write button Enabled
						    btn_Write.setEnabled(true);
						    pack();
						    }
			 			}
		});

		// Write button drives output from JTable to file in CSV/TSV/PDF format
		btn_Write.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Check if output file already exists
				File outFile = new File(HGlobal.chosenFolder+File.separator+HGlobal.chosenFilename);	// check if file already exists
				boolean exists = outFile.exists();
				if (exists) {if (JOptionPane.showConfirmDialog(btn_Write, HG0531Msgs.Text_37,		// File already exists. Do you want to over-write it?
																		  HG0531Msgs.Text_38,		// Write File
									JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
																	fileButtons.clearSelection();		// no to Over-write question, so reset fields
											    					btn_Write.setVisible(false);
											    					return;								// and exit
											    					}
							}

			// Write TSV or CSV files using Java Writer
				if (fileType.equals("CSV") || (fileType.equals("TSV"))) {		//$NON-NLS-1$ //$NON-NLS-2$
			        try {
			        	createXSV(outFile);
		            }
			        catch (IOException iox) {
			        	JOptionPane.showMessageDialog(btn_Write, HG0531Msgs.Text_39 + iox.getMessage(), 	// Write failed; I/O error:
			        											 HG0531Msgs.Text_38,						// Write File
			        											 JOptionPane.ERROR_MESSAGE);
			        	if (HGlobal.writeLogs)
			        		HB0711Logging.logWrite("ERROR: in HG0531 CSV/TSV file output error " + iox.getMessage()); //$NON-NLS-1$
			        }
				}

			// or Write PDF files using iText
				if (fileType.equals("PDF") ) {		//$NON-NLS-1$
					// Get user's Document name
			        if (headerBox.isSelected()) docHeader = headerField.getText();
			        	else docHeader = "";		//$NON-NLS-1$
			        // And send it all to the iText PDF writing tool
					try {
				    	createPDF(outFile);
					} catch (Exception e) {
			        	JOptionPane.showMessageDialog(btn_Write, HG0531Msgs.Text_41 + e.getMessage(), 	// Write failed; I/O error:
																 HG0531Msgs.Text_38, 					// Write File
																 JOptionPane.ERROR_MESSAGE);
			        	if (HGlobal.writeLogs)
			        		HB0711Logging.logWrite("ERROR: in HG0531 PDF file output error " + e.getMessage()); //$NON-NLS-1$
					}
				}

		     // Perform exit for all fileTypes
                JOptionPane.showMessageDialog(btn_Write, (HG0531Msgs.Text_43 + HGlobal.chosenFilename),  // Data written to
                										  HG0531Msgs.Text_38,							// Write File
                										  JOptionPane.INFORMATION_MESSAGE);
                if (HGlobal.writeLogs)
                	HB0711Logging.logWrite( "Action: data written to " + HGlobal.chosenFilename + " from HG0531Output"); //$NON-NLS-1$ //$NON-NLS-2$
                dispose();
				}
		});

	}		// End HG0531Output constructor

/**
 * Routine to write the JTable in CSV/TSV format
 * @param outFile - file destination
 * @throws IOException
 */
	public void createXSV(File outFile) throws IOException {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int numCols = tableToOutput.getColumnCount();
		int numRows = tableToOutput.getRowCount();
		Writer writer = null;
        writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (outFile), "utf-8"));	//$NON-NLS-1$
        // As we want to allow CSV fields to contain commas, we are using
        // the ^ as separator, so we need to say so in 1st line of file
        if (separatorType.equals("^")) {					//$NON-NLS-1$
	    	String firstLine = "sep=^";						//$NON-NLS-1$
        	writer.write(firstLine.toString() + "\r\n");	//$NON-NLS-1$
        }
        StringBuffer bufferHeader = new StringBuffer();
        for (int j = 0; j < numCols; j++) {
            bufferHeader.append(tableToOutput.getColumnName(j));
            if (j!=numCols) bufferHeader.append(separatorType);
        }
        writer.write(bufferHeader.toString() + "\r\n");	//$NON-NLS-1$
        for (int i = 0 ; i < numRows ; i++){
             StringBuffer buffer = new StringBuffer();
            for (int j = 0 ; j < numCols ; j++){
                buffer.append(tableToOutput.getValueAt(i,j));
                if (j!=numCols) buffer.append(separatorType);
            }
            writer.write(buffer.toString() + "\r\n");	//$NON-NLS-1$
        }
        writer.close();
	}	// End createXSV

/**
 * Routine to handle docEvents for Header/Footer in iText 3rd-party product code below
 */
	protected class headerFooterEventHandler  extends AbstractPdfDocumentEventHandler {
		public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			int pageNumber = pdfDoc.getPageNumber(page);
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(
					page.newContentStreamBefore(), page.getResources(), pdfDoc);
			// Set the font and size
			try {
				pdfCanvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 10);
			} catch (IOException e) {
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in HG0531 iText canvas setting error " + e.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(e);
				}
			}
			//Add document name header and an iText note/page number footer
			pdfCanvas.beginText()
			// Position header and show it (PDFs use 72 points/inch)
			.moveText(pageSize.getWidth()/2-60, pageSize.getTop()-20)
			.showText(docHeader)
			// Position footer and show it
			.moveText(-(pageSize.getWidth()/2-100), -pageSize.getTop()+40)
			.showText(HG0531Msgs.Text_45)			// PDF output formatted by iText
			.moveText(pageSize.getWidth()/2-60, 0)
			.showText(String.valueOf(pageNumber))
			.endText();
			pdfCanvas.release();
		}
	}

/**
 * Routine to write the JTable in PDF format via iText 3rd-party product
 * @param outFile - file destination
 * @throws IOException
 * @throws ITextException
 */
	public void createPDF(File outFile) throws IOException, ITextException {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int numCols = tableToOutput.getColumnCount();
		int numRows = tableToOutput.getRowCount();
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFile));
		Document doc;

		// Set the PDF metadata to mention iText (NB: AGPL version of iText sets copyright producer line by itself)
		PdfDocumentInfo info = pdfDoc.getDocumentInfo();
		info.setTitle(docHeader);
		info.setAuthor(HG0531Msgs.Text_46);			// HRE using iText software by apryse

		// Force landscape mode for large number of columns (could add code to set pagesize/rotation?)
		if (numCols > 6) doc = new Document(pdfDoc, PageSize.A4.rotate());
			else doc = new Document(pdfDoc, PageSize.A4);

		// Add event handler to create header/footer text
		pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new headerFooterEventHandler());
		pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new headerFooterEventHandler());

		// Define a pdf table to put the data into
	    Table pdfTable = new Table(UnitValue.createPercentArray(numCols)).useAllAvailableWidth();
        // Create table header
        for (int i = 0; i < numCols; i++) {
        	Cell hdrCell = new Cell().add(new Paragraph(tableToOutput.getColumnName(i)));
        	hdrCell.setBackgroundColor(new DeviceRgb(210, 210, 210));	// light grey
        	pdfTable.addHeaderCell(hdrCell);
        }
        // Extract all data from the JTable and insert it into the PDF table
        for (int rows = 0; rows < numRows; rows++) {
            for (int cols = 0; cols < numCols; cols++) {
            	pdfTable.addCell(tableToOutput.getValueAt(rows, cols).toString());
            }
        }
        doc.add(pdfTable);
        doc.close();

	}	// End createPDF

}		// End HG0531Output
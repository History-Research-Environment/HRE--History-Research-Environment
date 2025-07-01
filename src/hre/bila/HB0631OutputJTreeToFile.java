package hre.bila;
/****************************************************************************
* OutputJTreeToFile
* v0.04.0032 2025-06-24 Multi-page PDF JTree file writer (D Ferguson)
*****************************************************************************/

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JTree;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.AreaBreakType;

import hre.gui.HGlobal;

/**
 * Output a JTree to a PDF file
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-06-30
 */

public class HB0631OutputJTreeToFile {

	JTree tree;
	File outputPDFfile;
	String header, footer;
	int headerHeight, footerHeight;

/**
 * Constructor
 * @throws HBException
 */
	public HB0631OutputJTreeToFile(JTree tree, String header, String footer, File outFile) throws HBException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: entering HG0631OutputJTreeToFile");

		this.tree = tree;
		this.outputPDFfile = outFile;
		this.header = header;
		this.footer = footer;
		// Set header/footer size - same as we will use in header/footer code
		if (header.isEmpty()) headerHeight = 0;
		else headerHeight = 14;
		if (footer.isEmpty()) footerHeight = 0;
		else footerHeight = 10;

		// Calculate necessary width + layout adjustments
		int rowHeight = tree.getRowHeight();
		if (rowHeight <= 0)
			rowHeight = tree.getFontMetrics(tree.getFont()).getHeight();
		tree.expandRow(0); // Expand so size correctly calculated
		tree.setSize(tree.getPreferredSize());
		int treeWidth = tree.getPreferredSize().width + 60; // generous buffer
		int totalRows = tree.getRowCount();

		// Calculate page parameters
        float margin = 36f;
        float pageWidth = PageSize.A4.getWidth();
        float pageHeight = PageSize.A4.getHeight();
        float usableWidth = pageWidth - 2 * margin;
        float usableHeight = pageHeight - 2 * margin;
        int rowsPerPage = (int) (usableHeight / rowHeight);

        // Write the tree to the file
        try {
        	PdfDocument pdf = new PdfDocument(new PdfWriter(outputPDFfile));
        	Document doc = new Document(pdf, PageSize.A4);
        	doc.setMargins(margin, margin, margin, margin);

    		// Add event handler to create header/footer text, if needed
    		if (headerHeight > 0) pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new headerEventHandler());
    		if (footerHeight > 0) pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new footerEventHandler());

        	for (int startRow = 0; startRow < totalRows; startRow += rowsPerPage) {
        		int imgHeight = rowsPerPage * rowHeight + 4;

        		BufferedImage img = new BufferedImage(treeWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        		Graphics2D g = img.createGraphics();
        		g.fillRect(0, 0, treeWidth, imgHeight);
        		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        		g.translate(0, -startRow * rowHeight);
        		tree.paint(g);
        		g.dispose();

        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        		ImageIO.write(img, "png", baos);
        		Image pdfImage = new Image(ImageDataFactory.create(baos.toByteArray()));
        		pdfImage.setFixedPosition(margin, margin);
        		pdfImage.setWidth(usableWidth);
        		pdfImage.setHeight(usableHeight);
        		doc.add(pdfImage);

        		if (startRow + rowsPerPage < totalRows)
        			doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        	}
        	doc.close();
        } catch (Exception e) {
        	if (HGlobal.writeLogs)
        		HB0711Logging.logWrite("Error: PDF file output " + e.getMessage() + " in HG0631OutputJTreeToFile"); //$NON-NLS-1$ //$NON-NLS-2$
        	throw new HBException("Error: PDF file output " + e.getMessage());
        }

        // and exit
        if (HGlobal.writeLogs)
        	HB0711Logging.logWrite("Action: exiting HG0631OutputJTreeToFile");
	}

/**
 * Routine to handle docEvents for Header in iText
 */
	protected class headerEventHandler  extends AbstractPdfDocumentEventHandler {
		public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
	        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
	        PdfDocument pdfDoc = docEvent.getDocument();
	        PdfPage page = docEvent.getPage();
	        Rectangle pageSize = page.getPageSize();

	        try {
	        	PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
	        	float fontSize = 14f;
	        	float pageWidth = pdfDoc.getDefaultPageSize().getWidth();
	        	float headerWidth = font.getWidth(header, fontSize);

	        	PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
	        	// Add header by position and show it (PDFs use 72 points/inch)
	        	canvas.beginText()
					.setFontAndSize(font, fontSize)
		        	.moveText((pageWidth - headerWidth)/2, pageSize.getTop()-20)
		        	.showText(header)
		        	.endText()
		        	.release();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
		}
	}

/**
 * Routine to handle docEvents for Footer in iText
 */
	protected class footerEventHandler  extends AbstractPdfDocumentEventHandler {
		public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();

			try {
				PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
				float fontSize = 10f;
				float pageWidth = pdfDoc.getDefaultPageSize().getWidth();
				float footerWidth = font.getWidth(footer, fontSize);

				PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
				// Add footer
				canvas.beginText()
					.setFontAndSize(font, fontSize)
					.moveText((pageWidth - footerWidth)/2, 20)  // 20 points from bottom
					.showText(footer)
					.endText()
					.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}

}
package hre.bila;
/****************************************************************************
* OutputJTreeToPrinter
* v0.04.0032 2025-06-24 Multi-page PDF JTree print writer (D Ferguson)
*****************************************************************************/

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import javax.swing.JTree;

import hre.gui.HGlobal;

/**
 * Output a JTree to a printer
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-06-30
 */

public class HB0631OutputJTreeToPrinter {

	JTree tree;
	String header, footer;
	int headerHeight, footerHeight;
	int rowHeight;

/**
 * Constructor
 * @throws HBException
 */
	public HB0631OutputJTreeToPrinter(JTree tree, String header, String footer) throws HBException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: entering HG0631OutputJTreeToPrinter");

		this.tree = tree;
		this.header = header;
		this.footer = footer;
		// Set header/footer size - same as we will use in header/footer code
		if (header.isEmpty()) headerHeight = 0;
		else headerHeight = 14;
		if (footer.isEmpty()) footerHeight = 0;
		else footerHeight = 10;

		// Calculate necessary tree width + layout adjustments
		rowHeight = tree.getRowHeight();
		if (rowHeight <= 0)
			rowHeight = tree.getFontMetrics(tree.getFont()).getHeight();
		tree.expandRow(0); // Expand so size correctly calculated
		tree.setSize(tree.getPreferredSize());
		int treeWidth = tree.getPreferredSize().width + 60; // generous buffer
		int totalRows = tree.getRowCount();

		// Now print it
        try {
            int margin = 36; // Match PDF visual margin (~0.5 inch)

            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage();

            double printableHeight = pf.getImageableHeight() - headerHeight - footerHeight;
            int rowsPerPage = (int) (printableHeight / rowHeight);
            int imageHeight = rowsPerPage * rowHeight + 4;
            int totalPages = (int) Math.ceil((double) totalRows/rowsPerPage);

            job.setPrintable((graphics, format, pageIndex) -> {
                if (pageIndex >= totalPages) return Printable.NO_SUCH_PAGE;

                int startRow = pageIndex * rowsPerPage;

                BufferedImage pageImage = new BufferedImage(treeWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = pageImage.createGraphics();
                g2d.fillRect(0, 0, treeWidth, imageHeight);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.translate(0, -startRow * rowHeight);
                tree.paint(g2d);
                g2d.dispose();

                Graphics2D printerG = (Graphics2D) graphics;
                double scaleX = format.getImageableWidth()/pageImage.getWidth();
                double scaleY = format.getImageableHeight()/pageImage.getHeight();
                double scale = Math.min(scaleX, scaleY);
                double scaledImageHeight = pageImage.getHeight() * scale;
                double offsetX = format.getImageableX() + margin; 				// 0.5" fixed left margin
                double offsetY = format.getImageableY() + Math.max((format.getImageableHeight() - scaledImageHeight)/2, margin);
                // Print Header (if exists) centred
                if (headerHeight > 0) {
                	int headerX = (int) (pf.getImageableWidth()/2 - g2d.getFontMetrics().stringWidth(header)/2); // centred
                	printerG.setFont(new Font("HELVETICA", Font.BOLD, 14)); // Set the default header font
                	printerG.drawString(header, headerX, (int) offsetY);
                	offsetY = offsetY + headerHeight;
                }
                // Print the tree image for this page
                printerG.translate(offsetX, offsetY);
                printerG.scale(scale, scale);
                printerG.drawImage(pageImage, 0, 0, null);
                // Print Footer (if exists) centred
                if (footerHeight > 0) {
                	int footerX = (int) (pf.getImageableWidth()/2 - g2d.getFontMetrics().stringWidth(footer)/2); // centred
                	int footerY = (int) (pf.getImageableHeight()- footerHeight);
                	printerG.setFont(new Font("HELVETICA", Font.PLAIN, 10)); // Set the default footer font
                	printerG.drawString(footer, footerX, footerY);
                	}
                return Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) job.print();

        } catch (Exception e) {
        	if (HGlobal.writeLogs)
        		HB0711Logging.logWrite("Error: printing " + e.getMessage() + " in HG0631OutputJTreeToPrinter"); //$NON-NLS-1$ //$NON-NLS-2$
        	throw new HBException("Error: printing " + e.getMessage());
        }

		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: exiting HG0631OutputJTreeToPrinter");
    }

}		// End HB0631OuputJTreeToPrinter
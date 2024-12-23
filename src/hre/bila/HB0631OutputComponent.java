package hre.bila;
/****************************************************************************
* OutputComponent
* v0.01.0023 2020-09-02 Created as multi-page JComponent printer (D Ferguson)
*****************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

import hre.gui.HGlobal;

/**
 * Output a JComponent to print or file (if PDF)
 * @author D Ferguson
 * @version v0.01.0023
 * @since 2020-09-02
 */

public class HB0631OutputComponent implements Printable, Pageable {

	private Component compToPrint;
	private PageFormat format;
    private int numPages;
    private double xRatio;

/**
 * Constructor
 * @throws HBException
 */
	  public HB0631OutputComponent(Component componentToBePrinted) throws HBException {
		  if (HGlobal.writeLogs) {
			HB0711Logging.logWrite("Action: entering HG0531 OutputComponent");
		}
		  compToPrint = componentToBePrinted;
		  // get total space from component
	      Dimension page = this.compToPrint.getPreferredSize();
	      // calculate for printer's default page size
	      format = PrinterJob.getPrinterJob().defaultPage();
	      numPages = (int) Math.ceil(page.height/format.getImageableHeight());
	      // Get the width of the component
	      Dimension dim = compToPrint.getSize();
	      double compWidth = dim.getWidth();
	      // Get the width of the printable area
	      double pageWidth = format.getImageableWidth();
	      // Calculate width scaling ratio to fit on page
	      xRatio = pageWidth / compWidth;
	      // But don't allow scaling up
	      if (xRatio > 1) {
			xRatio = 1;
		}
	      // Adjust number of pages by scale
	      numPages = (int) Math.ceil(numPages * xRatio);

		  print(compToPrint);

		  if (HGlobal.writeLogs) {
			HB0711Logging.logWrite("Action: exiting HG0531 OutputComponent");
		}

	  }		// End HG0531OutputComponent constructor

/**
 * Drive printer interface to setup printable item
 * @throws HBException
 */
	  public void print(Component c) throws HBException {
		  PrinterJob printJob = PrinterJob.getPrinterJob();
		  // show page-dialog with default page
	      format = printJob.pageDialog(printJob.defaultPage());

		  printJob.setPrintable(this);
		  printJob.setPageable(this);

		  if (printJob.printDialog()) {
			try {
				  printJob.print();
			  	} catch(PrinterException pe) {
		          if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("Error: printer output " + pe.getMessage() + " in HG0531 OutputComponent");
				}
		          throw new HBException("Error: printer output " + pe.getMessage());
	      }
		}
	  }

/**
 * Do graphical transformation on component
 * Note disable/enable of doublebuffering for performance
 */
	  @Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		  if ((pageIndex < 0) | (pageIndex >= numPages)) {
	            return(NO_SUCH_PAGE);
		  	}
		  	else {
		  			Graphics2D g2d = (Graphics2D)g;
		  			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY() - pageIndex * pageFormat.getImageableHeight());
		  			disableDoubleBuffering(compToPrint);
		  			// Scale x, y by the x ratio
		  	        g2d.scale(xRatio, xRatio);
		  			compToPrint.paintAll(g2d);
		  			enableDoubleBuffering(compToPrint);
		  			return(PAGE_EXISTS);
		  		}
	  }

	  // Perform buffering changes, etc
	  public static void disableDoubleBuffering(Component c) {
		  RepaintManager currentManager = RepaintManager.currentManager(c);
		  currentManager.setDoubleBufferingEnabled(false);
	  }
	  public static void enableDoubleBuffering(Component c) {
		  RepaintManager currentManager = RepaintManager.currentManager(c);
		  currentManager.setDoubleBufferingEnabled(true);
	  }

	  @Override
	  public int getNumberOfPages() {
	      return numPages;
	  }
	  @Override
	  public PageFormat getPageFormat(int arg0) throws IndexOutOfBoundsException {
	      return format;
	  }
	  @Override
	  public Printable getPrintable(int arg0) throws IndexOutOfBoundsException {
	      return this;
	  }

}		// End HG0531OutputComponent
package hre.tmgjava;
/*****************************************************************************************
 * Action Progress - Specification 05.87.05 GUI_Message Pattern-ActionProgress 2019-02-09
 * v0.00.0007 2019-02-10 by R Thompson
 * v0.00.0008 2019-07-20 fix fonts, add listener for 'X' on frame (D Ferguson)
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0019 2020-03-15 implemented in TMG to HRE conversion (N. Tolleshaug)
 * v0.00.0019 2020-03-16 implemented with SwingWorker (N. Tolleshaug)
 * v0.00.0019 2020-03-17 improved layout of window (D Ferguson)
 * v0.00.0021 2020-04-03 changed to resizeable layouts, removed logging, Reminder (D Ferguson)
 * v0.00.0021 2020-04-04 added frame.pack() to set preferred frame size (N.Tolleshaug)
 * v0.00.0025 2021-01-04 removed static reference for createAndShowGUI() (N.Tolleshaug)
 * v0.04.0032 2026-01-15 Log catch block and other msgs (D Ferguson)
 * 			  2026-01-15 Removed icon for non-existent Help (D Ferguson)
 *****************************************************************************************/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.gui.HGlobal;
import net.miginfocom.swing.MigLayout;

/**
 * Action Progress
 * @author R Thompson
 * @version v0.04.0032
 * @since 2019-02-10
 */

public class TMGHREprogressMonitor extends JFrame
				implements PropertyChangeListener {

	private static final long serialVersionUID = 001L;
	private static JPanel monitorPanel;
	private static JFrame frame;
	private JProgressBar progressBar;
	private JTextArea txtrContextOfAction;
	private JLabel lbl_Size,lbl_Time;
	private JLabel lblNewLabel;

	private TMGHREconverter task;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
	}

	public void startConversion(String dataBasePath) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task = new TMGHREconverter(this,dataBasePath);
        task.addPropertyChangeListener(this);
        task.execute();
	}

    public void startMonitor() {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI();
            }
        });
    }

	public void setProgress(int percent) {
		progressBar.setValue(percent);
	}

	public void setContextOfAction(String infoText) {
		txtrContextOfAction.append("\n" + infoText);
		txtrContextOfAction.setLineWrap(true);
	}

	public void setToolTip(String infoText) {
		progressBar.setToolTipText(infoText);
	}

	public void setStatus(int nr, int total) {
		lbl_Size.setText("Pass "  + nr + " of " + total + " completed");
	}

	public void setTransferTime(double time) {
		lbl_Time.setText(String.format("Time in sec:  %5.2f",time));
	}

	public void progressClose() {
		try {
			System.out.println(" HRE Monitor Closing");
			task.hreLoader.closeDatabase();
			task.stopTCPServer();
		} catch (HCException | NullPointerException ex) {
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("ERROR: in HREprogressMonitor: " + ex.getMessage());
			dispose();
		}
		setVisible(false);
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

    public void endConvert(String message) {
    	Toolkit.getDefaultToolkit().beep();
    	setCursor(null); //turn off the wait cursor
    	setContextOfAction(" TMG conversion: " + message + "\n");
     	if (TMGglobal.DEBUG) System.out.println(" ** Conversion status - " + message);
    }

/**
 * Create the GUI and show it. As with all GUI code, this must run
 * on the event-dispatching thread.
 */
    private void createAndShowGUI() {
        //Create and set up the window.
    	frame = new JFrame();
    	frame.setTitle("TMG to HRE Progress report");
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));
    	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(monitorPanel);
    	frame.setLocation(50,50);
    	frame.pack();
        frame.setVisible(true);
    }

/**
 * Create the Dialog.
 */
public TMGHREprogressMonitor() {
		//Window windowInstance = this;
		monitorPanel = new JPanel();
		monitorPanel.setPreferredSize(new Dimension(400, 550));
		monitorPanel.setLayout(new BorderLayout());
		monitorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(monitorPanel);

		// Setup toolBar as NORTH area of the layout
		JToolBar toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(13, 32));
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);

		lblNewLabel = new JLabel(" TMG to HRE conversion progress monitor");
		lblNewLabel.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
		toolBar.add(lblNewLabel);
		toolBar.add(Box.createHorizontalGlue());

		getContentPane().add(toolBar, BorderLayout.NORTH);

		// Setup Scrollpane/Text area as CENTER area with filler panels each side for spacing
		JScrollPane scrollPane_Context = new JScrollPane();

		JPanel lFill = new JPanel();
	    lFill.setPreferredSize(new Dimension(8, 10));
	    lFill.setMinimumSize(new Dimension(10, 10));
	    getContentPane().add(lFill, BorderLayout.WEST);
	    JPanel rFill = new JPanel();
	    rFill.setPreferredSize(new Dimension(8, 10));
	    rFill.setMinimumSize(new Dimension(10, 10));
	    getContentPane().add(rFill, BorderLayout.EAST);

		txtrContextOfAction = new JTextArea();
		txtrContextOfAction.setLineWrap(true);
		txtrContextOfAction.setWrapStyleWord(true);
		txtrContextOfAction.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
		DefaultCaret caret = (DefaultCaret)txtrContextOfAction.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		txtrContextOfAction.setText("*TMG to HRE conversion initiated!");
		scrollPane_Context.setViewportView(txtrContextOfAction);

		getContentPane().add(scrollPane_Context, BorderLayout.CENTER);

		// Setup a JPanel to contain all other components as SOUTH area
		JPanel bottom = new JPanel();
		bottom.setPreferredSize(new Dimension(10, 100));
	    getContentPane().add(bottom, BorderLayout.SOUTH);
	    bottom.setLayout(new MigLayout("", "[][grow][]", "[]15[]15[]"));

		progressBar = new JProgressBar();
		progressBar.setToolTipText("Progress of TMG to HRE!");
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        bottom.add(progressBar, "spanx 3,grow");

		lbl_Size = new JLabel("Status of passes");
		lbl_Size.setHorizontalAlignment(SwingConstants.LEFT);
		lbl_Size.setHorizontalTextPosition(SwingConstants.LEFT);
		lbl_Size.setPreferredSize(new Dimension(160, 14));
		lbl_Size.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
        bottom.add(lbl_Size, "cell 0 1");

		lbl_Time = new JLabel("Time used");
		lbl_Time.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_Size.setHorizontalTextPosition(SwingConstants.RIGHT);
		lbl_Time.setPreferredSize(new Dimension(160, 14));
		lbl_Time.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
        bottom.add(lbl_Time, "cell 2 1,alignx right");

		final JButton btn_Close = new JButton("Close");
		btn_Close.setHorizontalAlignment(SwingConstants.RIGHT);
		btn_Close.setToolTipText("Stop the TMG to HRE progress and close");
		btn_Close.setFont(new Font(TMGglobal.dfltGUIfont, Font.PLAIN, 13));
        bottom.add(btn_Close, "cell 2 2,alignx right");

/**
* CREATE ACTION BUTTON LISTENERS
**/
		//buttons
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				progressClose();
			}
		});

		// Listener for clicking 'X' on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});
	}
}

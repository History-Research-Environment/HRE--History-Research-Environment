package hre.gui;
/**********************************************************************************
 * Color Chooser - Specification 05.78 GUI_ColorChooser 2019-07-16
 * v0.00.0007 2019-07-19 by D Ferguson
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.01.0023 2020-09-30 add usage instruction (D Ferguson)
 * v0.01.0026 2021-06-20 adjust interface from AppSettings (D Ferguson)
 ***********************************************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import hre.bila.HB0711Logging;

/**
 * Color Chooser
 * @author D Ferguson
 * @version v0.01.0026
 * @since 2019-07-19
 */

public class HG0578ColorChooser extends JDialog {

	private static final long serialVersionUID = 001L;
	private JPanel contentPane;

/**
 * @param promptMsg a prompt msg to state what this color choice is for
 * @param colorID   the color to initially show in the Preview area
 */
	public HG0578ColorChooser(String promptMsg, Color colorID) {
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0578 Color Chooser");}	//$NON-NLS-1$
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));	//$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 645, 415);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 50));

		/*************************************
		/* Tailor the ColorChooser and panel
		/*************************************/
		// Increase swatch box sizes (default=10)
        UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(15, 15));	//$NON-NLS-1$
        UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(15, 15));	//$NON-NLS-1$
        JColorChooser chooser = new JColorChooser();

        // Set the promptMsg into the border of the panel, at left top
        chooser.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), promptMsg, TitledBorder.LEADING, TitledBorder.TOP)); //$NON-NLS-1$

        // Set the chooser start color to match the current color of the item
        // we're choosing a color for - otherwise it defaults to white.
        chooser.setColor(colorID);

        // Remove all chooser tabs except Swatch and RGB (too confusing otherwise)
        AbstractColorChooserPanel[] panels=chooser.getChooserPanels();
        for(AbstractColorChooserPanel p:panels){
            String displayName=p.getDisplayName();
            switch (displayName) {
                case "HSV":		//$NON-NLS-1$
                    chooser.removeChooserPanel(p);
                    break;
                case "HSL":		//$NON-NLS-1$
                    chooser.removeChooserPanel(p);
                    break;
                case "CMYK":		//$NON-NLS-1$
                    chooser.removeChooserPanel(p);
                    break;
            }}

        // Create a tailored Preview pane with just a Label component
		PreviewArea preview = new PreviewArea(chooser);
        chooser.setPreviewPanel(preview);

        // Create a change listener to detect color changes
        chooser.getSelectionModel().addChangeListener(new ChangeListener()
           {
              @Override
			public void stateChanged(ChangeEvent event) {
            	  ColorSelectionModel model = (ColorSelectionModel) event.getSource();
                  preview.curColor = model.getSelectedColor();
              }
           });

        // Add the Color chooser to the content pane
		contentPane.add(chooser);

		// Listener for clicking 'X' on screen - save chosen color on exit
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	 HGlobal.returnColor = preview.curColor;
		    	 if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HG0578 Color Chooser");}		//$NON-NLS-1$
		    }
		});
	}
}

/************************************************
/* Class to define PreviewPanel and make it work
/***********************************************/
	class PreviewArea extends JComponent {
		private static final long serialVersionUID = 001L;
		Color curColor;
		  public PreviewArea(JColorChooser chooser) {
		    curColor = chooser.getColor();
		    setPreferredSize(new Dimension(400, 30));
		  }
		  @Override
		  public void paint(Graphics g) {
		    g.setColor(curColor);
		    g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		  }
	}

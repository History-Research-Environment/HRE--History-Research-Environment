package hre.gui;
/********************************************************************
 * HRE Help About - Specification 04.15 GUI_Help AboutHRE 2019-01-26
 * v0.00.0007 2019-03-16 by D Ferguson, updated 2019-07-18
 * v0.01.0023 2020-09-30 changed for JTattoo install (D Ferguson)
 * v0.01.0028 2023-03-31 Add hyperlinks to Licence text (D Ferguson)
 * v0.03.0030 2023-07-14 Move to v0.3 logo, add iText (D Ferguson)
 * v0.03.0031 2023-11-10 Adjust HRE wording
 * 			  2024-10-01 Organize imports (D Ferguson)
 *******************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;

import hre.bila.HB0711Logging;

/**
 * HRE Help About
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2019-03-16
 */

public class HG0415HelpAboutHRE extends JDialog {

	private static final long serialVersionUID = 001L;

	/*********************************
	 * Create the HelpAbout screen
	 ********************************/
	public HG0415HelpAboutHRE() {
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0415 HelpAbout");}
		setFont(new Font("Arial", Font.BOLD, 13));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setFont(new Font("Arial", Font.BOLD, 15));
		getContentPane().setLocation(new Point(30, 0));
		setResizable(false);
		setBackground(Color.WHITE);
		getContentPane().setBackground(Color.WHITE);
		setTitle("About HRE");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));
		setBounds(100, 100, 639, 756);
		getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(12, 13, 217, 208);
		lblNewLabel.setBackground(SystemColor.menu);
		lblNewLabel.setIcon(new ImageIcon(getClass().getResource("/hre/images/HRE-logo.png")));
		getContentPane().add(lblNewLabel);

		JTextArea txtHREname = new JTextArea();
		txtHREname.setEditable(false);
		txtHREname.setBackground(Color.WHITE);
		txtHREname.setForeground(Color.BLACK);
		txtHREname.setFont(new Font("Arial", Font.BOLD, 24));
		txtHREname.setText("History Research \r\nEnvironment Ltd");
		txtHREname.setBounds(300, 13, 253, 67);
		getContentPane().add(txtHREname);

		JTextArea txtVer = new JTextArea();
		txtVer.setForeground(Color.BLACK);
		txtVer.setEditable(false);
		txtVer.setBackground(Color.WHITE);
		txtVer.setFont(new Font("Arial", Font.PLAIN, 15));
		txtVer.setText(("HRE Build             v") + HGlobal.buildNo);
		txtVer.setBounds(300, 91, 250, 25);
		getContentPane().add(txtVer);

		JTextArea txtDate = new JTextArea();
		txtDate.setForeground(Color.BLACK);
		txtDate.setEditable(false);
		txtDate.setBackground(Color.WHITE);
		txtDate.setFont(new Font("Arial", Font.PLAIN, 15));
		txtDate.setText(("Release Date      ") + HGlobal.releaseDate);
		txtDate.setBounds(300, 116, 217, 25);
		getContentPane().add(txtDate);

		JLabel lblNewLabel_3 = new JLabel("Website:");
		lblNewLabel_3.setToolTipText("HRE website");
		lblNewLabel_3.setFont(new Font("Arial", Font.BOLD, 13));
		lblNewLabel_3.setBounds(275, 169, 86, 14);
		getContentPane().add(lblNewLabel_3);

		JButton btnHREwebsite = new JButton("www.historyresearchenvironment.org");
		btnHREwebsite.setToolTipText("Click to access HRE website");
		btnHREwebsite.setForeground(Color.BLUE);
		btnHREwebsite.setHorizontalAlignment(SwingConstants.LEFT);
		btnHREwebsite.setContentAreaFilled(false);
		btnHREwebsite.setBackground(Color.WHITE);
		btnHREwebsite.setFont(new Font("Arial", Font.PLAIN, 13));
		btnHREwebsite.setBorderPainted(false);
		btnHREwebsite.setBounds(325, 165, 270, 23);
		getContentPane().add(btnHREwebsite);

		JLabel lblNewLabel_2 = new JLabel("Licences");
		lblNewLabel_2.setFont(new Font("Arial", Font.BOLD, 13));
		lblNewLabel_2.setBounds(49, 230, 86, 14);
		getContentPane().add(lblNewLabel_2);

		JTextPane txtLicence = new JTextPane();
		txtLicence.setContentType("text/html");
		txtLicence.setBackground(Color.WHITE);
		txtLicence.setDisabledTextColor(Color.BLACK);
		txtLicence.setMargin(new Insets(4, 5, 4, 5));
		txtLicence.setAlignmentY(Component.TOP_ALIGNMENT);
		txtLicence.setEditable(false);
		txtLicence.setText(
			"History Research Environment (HRE) is a community project to create a free open source platform for recording a wide range of genealogical, historical and social research.<br><br>" +
			"The development community is supported by a not-for-profit company, History Research Environment Limited, limited by guarantee and registered under the laws of England and Wales.<br><br>" +
			"All code developed by HRE Ltd is copyright the individual developers, and is released under the GNU Affero General Public Licence (GNU AGPL) available at <a href=https://www.gnu.org/licenses/agpl-3.0.en.html>https://www.gnu.org/licenses/agpl-3.0.en.html</a><br><br>" +
			"Reuse of the HRE software is permitted only under the terms of the licence. Using the HRE software or any derivative of the software to offer a service over the internet is strictly prohibited.<br><br>" +
			"HRE was created with the use of Eclipse, made available under the Eclipse Public Licence v2.0, using AdoptOpenJDK Java licenced under Apache Licence v2.0 and GPL v2 + CE.<br><br>" +
			"HRE software contains binary redistributions of the following open-source components:<br>" +
			"<ul>" +
			"<li>H2 database system (<a href=https://h2database.com/>https://h2database.com/</a>) which is dual licensed and available under the MPL 2.0 (Mozilla Public License) or under the EPL 1.0 (Eclipse Public License)." +
			"  An original copy of the license agreement can be found at <a href=https://h2database.com/html/license.html>https://h2database.com/html/license.html</a><br>" +
			"<li>iText PDF library available under the open source Affero General Public License (AGPLv3))  " +
			"<li>JavaDBF (DBF reader) by Alberto Fernandez, licenced under the GNU Lesser General Public License (LGPL v3) <a href=https://www.gnu.org/licenses/lgpl.txt>https://www.gnu.org/licenses/lgpl.txt</a><br>" +
			"<li>JTattoo licenced under the terms and conditions of the GNU General Public License v3.0 or later as published by the Free Software Foundation<br>" +
			"<li>OpenCSV (CSV parser), Apache Commons Language and Commons IO under an Apache 2.0 licence<br>" +
			"<li>Oracle Help system, under the Oracle Program License Terms to provide an online help system with Licensee\u2019s applications" +
			"<li>PF4j (Plugin for Java) under an Apache 2.0 licence<br>" +
			"<li>SLF4j (Simple Logging Facade) under an MIT licence, including Logback and Java Servlet under GNU Lesser Public Licences 2.1<br>" +
			"</ul>");
		txtLicence.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		txtLicence.setFont(new Font("Arial", Font.PLAIN, 13));
		txtLicence.setCaretPosition(0);			// force text to display from the top

		JScrollPane scrollLicence = new JScrollPane(txtLicence);
		scrollLicence.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		scrollLicence.setAutoscrolls(true);
		scrollLicence.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollLicence.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollLicence.setFont(new Font("Arial", Font.PLAIN, 13));
		scrollLicence.setSize(530, 200);
		scrollLicence.setLocation(49, 251);
		getContentPane().add(scrollLicence);

		JLabel lblNewLabel_1 = new JLabel("Developers");
		lblNewLabel_1.setFont(new Font("Arial", Font.BOLD, 13));
		lblNewLabel_1.setBounds(49, 480, 160, 14);
		getContentPane().add(lblNewLabel_1);

		JTextArea txtDeveloper = new JTextArea();
		txtDeveloper.setBackground(Color.WHITE);
		txtDeveloper.setDisabledTextColor(Color.BLACK);
		txtDeveloper.setMargin(new Insets(4, 5, 4, 5));
		txtDeveloper.setAlignmentY(Component.TOP_ALIGNMENT);
		txtDeveloper.setWrapStyleWord(true);
		txtDeveloper.setLineWrap(true);
		txtDeveloper.setEnabled(false);
		txtDeveloper.setEditable(false);
		txtDeveloper.setText(
			"Development of History Research Engine software has resulted from the contribution of a growing number of people who have volunteered their time and skills to the project. " +
			"In grateful acknowledgement of their efforts, we recognize:\r\n\t" +
					"Neil Bradley\r\n\tRon Chenier\r\n\tMichael Erichsen\r\n\t" +
					"Bruce Fairhall\r\n\tDon Ferguson\r\n\tHarry Goegebeur\r\n\t" +
					"John Whitelock\r\n\tRobin Lamacraft\r\n\t" +
					"Helmut Leininger\r\n\tRod Thompson\r\n\tNils Tolleshaug");
		txtDeveloper.setFont(new Font("Arial", Font.PLAIN, 13));
		txtDeveloper.setCaretPosition(0);			// force text to display from the top

		JScrollPane scrollDeveloper = new JScrollPane(txtDeveloper);
		scrollDeveloper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		scrollDeveloper.setAutoscrolls(true);
		scrollDeveloper.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollDeveloper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollDeveloper.setFont(new Font("Arial", Font.PLAIN, 13));
		scrollDeveloper.setSize(530, 150);
		scrollDeveloper.setLocation(49, 500);
		getContentPane().add(scrollDeveloper);

		JButton btnClose = new JButton("Close");
		btnClose.setBounds(486, 680, 97, 25);
		getContentPane().add(btnClose);

/***
 * CREATE ACTION LISTENERS
 **/
		// website button -> load HRE Website
		btnHREwebsite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: opening HRE website in HG0415HelpAboutHRE");}
				if(Desktop.isDesktopSupported())
				{
					try {
					Desktop.getDesktop().browse(new URI("https://www.historyresearchenvironment.org"));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
			}
		});

		// Any html in Licence textPane invokes the target html through this:
		txtLicence.addHyperlinkListener(e -> {
	        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
	          Desktop desktop = Desktop.getDesktop();
	          try {
	            desktop.browse(e.getURL().toURI());
	          } catch (Exception ex) {
	            ex.printStackTrace();
	          }
	        }
	      });

		// Close button - exit
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HG0415HelpAboutHRE");}
				dispose();
			}
		});

		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				btnClose.doClick();
		    }
		});
	}

}		// End HG0415HelpAboutHRE
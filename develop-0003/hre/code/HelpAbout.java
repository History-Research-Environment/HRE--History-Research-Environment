package hre.code;

import java.awt.EventQueue;

import javax.swing.JDialog;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.SystemColor;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.Point;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HelpAbout extends JDialog {

	/**
	 * Set Version ID
	 */
	private static final long serialVersionUID = 003L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HelpAbout dialog = new HelpAbout();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the HelpAbout splash screen as per '04.15 GUI_Help_About 2018-11-10' specification
	 */
	public HelpAbout() {
		getContentPane().setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().setLocation(new Point(30, 0));
		setResizable(false);
		setBackground(SystemColor.menu);
		setAlwaysOnTop(true);
		getContentPane().setBackground(SystemColor.menu);
		setTitle("About HRE");
		setIconImage(Toolkit.getDefaultToolkit().getImage(HelpAbout.class.getResource("/hre/images/HRE-32.png")));
		setBounds(100, 100, 650, 588);
		getContentPane().setLayout(null);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(0, 0, 72, 458);
		textArea.setFont(new Font("Calibri", Font.PLAIN, 12));
		textArea.setText("     ");
		textArea.setBackground(SystemColor.menu);
		textArea.setEditable(false);
		getContentPane().add(textArea);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(12, 13, 241, 208);
		lblNewLabel.setBackground(SystemColor.menu);
		lblNewLabel.setIcon(new ImageIcon(HelpAbout.class.getResource("/hre/images/HRE-logo-v01.png")));
		getContentPane().add(lblNewLabel);
		
		JTextArea txtrHistoryResearchEnvironment = new JTextArea();
		txtrHistoryResearchEnvironment.setBackground(UIManager.getColor("InternalFrame.minimizeIconBackground"));
		txtrHistoryResearchEnvironment.setForeground(Color.BLACK);
		txtrHistoryResearchEnvironment.setFont(new Font("Calibri", Font.BOLD, 30));
		txtrHistoryResearchEnvironment.setText("History Research \r\n   Environment");
		txtrHistoryResearchEnvironment.setBounds(304, 23, 253, 78);
		getContentPane().add(txtrHistoryResearchEnvironment);
		
		JTextArea txtrV = new JTextArea();
		txtrV.setBackground(UIManager.getColor("InternalFrame.minimizeIconBackground"));
		txtrV.setFont(new Font("Calibri", Font.PLAIN, 18));
		txtrV.setText("Build                  v0.00.0003\r\nRelease Date   25 Dec 2018");
		txtrV.setBounds(304, 133, 217, 62);
		getContentPane().add(txtrV);
		
		JTextArea txtrHistoryResearchEnvironment_1 = new JTextArea();
		txtrHistoryResearchEnvironment_1.setForeground(Color.BLACK);
		txtrHistoryResearchEnvironment_1.setBackground(UIManager.getColor("InternalFrame.minimizeIconBackground"));
		txtrHistoryResearchEnvironment_1.setEnabled(false);
		txtrHistoryResearchEnvironment_1.setEditable(false);
		txtrHistoryResearchEnvironment_1.setLineWrap(true);
		txtrHistoryResearchEnvironment_1.setWrapStyleWord(true);
		txtrHistoryResearchEnvironment_1.setFont(new Font("Calibri Light", Font.BOLD, 14));
		txtrHistoryResearchEnvironment_1.setText("History Research Environment (HRE) is a community project to create a free open source platform for recording a wide range of genealogical, historical and social research.\r\nThe development community is supported by a not-for-profit company, History Research Environment Limited, limited by guarantee and registered under the laws of England and Wales.\r\nAll code developed by HRE is copyright the individual developers, and is released under the GNU Affero General Public Licence (GNU AGPL) https://www.gnu.org/licenses/agpl-3.0.en.html.  For further details, see\r\n https://hrewiki.org/index.php?title=Why_have_we_chosen_these_licences%3F.  \r\nReuse of the HRE software is permitted only under the terms of the licence. Using the HRE software or any derivative of the software to offer a service over the internet is strictly prohibited.\r\nWebsite: https://historyresearchenvironment.org/\r\nWiki: https://hrewiki.org");
		txtrHistoryResearchEnvironment_1.setBounds(69, 225, 531, 267);
		getContentPane().add(txtrHistoryResearchEnvironment_1);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		btnClose.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnClose.setBounds(503, 505, 97, 25);
		getContentPane().add(btnClose);
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnHelp.setBounds(69, 505, 97, 25);
		getContentPane().add(btnHelp);
		
		JButton btnLicenses = new JButton("Licenses");
		btnLicenses.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnLicenses.setBounds(189, 505, 97, 25);
		getContentPane().add(btnLicenses);

		
	}
}

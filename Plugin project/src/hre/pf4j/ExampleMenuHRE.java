package hre.pf4j;
/**
 * Class ExampleMenu with main
 */
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginManager;

import hre.pf4j.ext.MainMenuExtensionPoint;
import hre.pf4j.ext.ReportMenuExtensionPoint;
import hre.pf4j.ext.ResearchMenuExtensionPoint;
import hre.pf4j.ext.ToolMenuExtensionPoint;
import hre.pf4j.ext.ViewpointMenuExtensionPoint;

class ExampleMenuHRE {

	public static void main(String[] args) {
		System.out.println("*** Start HRE Plugin Test");
		
/**
 * Init the plugin environment.
 * This should be done once during the boot process of the application.
 */
		
	// Set Plugin test Dir
		//String pathUserPlugin = "C:\\Users\\nils\\Documents\\Utvikling\\HRE-project\\Eksempler\\pf4j-demo\\menu-plugin"; 
		String pathUserPlugin = "C:\\Users\\nils\\Documents\\Utvikling\\HRE-project\\Plugin-code\\plugins-2025-01-04"; 
		System.setProperty("pf4j.pluginsDir", pathUserPlugin); 		
		System.out.println("*** Path for pf4j.pluginsDir: " + System.getProperty("pf4j.pluginsDir"));
		
		//final PluginManager pluginManager = new DefaultPluginManager();
			
        final PluginManager pluginManager = new DefaultPluginManager() {

	        protected ExtensionFinder createExtensionFinder() {
	            DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
	            extensionFinder.addServiceProviderExtensionFinder(); // to activate "HowdyGreeting" extension
	            return extensionFinder;
	        }
    	}; 
		
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

	// Launch Swing application.
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
			// Build the menu bar by using the available extensions.
				JMenuBar mainMenu = new JMenuBar();
				JMenu menuReportItem = new JMenu("Reports");
				JMenu menuResearchItem = new JMenu("Researchs");
				JMenu menuToolItem = new JMenu("Tools");
				JMenu menuViewpointItem = new JMenu("Viewpoints");
				mainMenu.add(menuReportItem);
				mainMenu.add(menuResearchItem);
				mainMenu.add(menuToolItem);
				mainMenu.add(menuViewpointItem);
				
			// Find Extensions
				System.out.println("Finding plugin main extensions!");
		        List<MainMenuExtensionPoint> extensions = pluginManager.getExtensions(MainMenuExtensionPoint.class);
		        System.out.println(String.format("Found %d extensions for extension point '%s'", extensions.size(), MainMenuExtensionPoint.class.getName()));
		        for (MainMenuExtensionPoint mainMenuExtension : extensions) {
		        	System.out.println("   >>> extension: " + mainMenuExtension.className()); 
		        	mainMenuExtension.buildMenuBar(mainMenu);
		        }
		        
				System.out.println("Finding plugin report extensions!");
		        List<ReportMenuExtensionPoint> reportMenuExtensions = pluginManager.getExtensions(ReportMenuExtensionPoint.class);
		        System.out.println(String.format("Found %d extensions for extension point '%s'", extensions.size(), ReportMenuExtensionPoint.class.getName()));
		        for (ReportMenuExtensionPoint reportMenuExtension : reportMenuExtensions) {
		        	System.out.println("   >>> extension: " + reportMenuExtension.className());  
		        	reportMenuExtension.buildMenu(menuReportItem);
		        	//reportMenuExtension.setDDLversion("v22c - ***");
		        }
		        
				System.out.println("Finding plugin Research extensions!");
		        List<ResearchMenuExtensionPoint> researchMenuExtensions = pluginManager.getExtensions(ResearchMenuExtensionPoint.class);
		        System.out.println(String.format("Found %d extensions for extension point '%s'", extensions.size(), ResearchMenuExtensionPoint.class.getName()));
		        for (ResearchMenuExtensionPoint researchMenuExtension : researchMenuExtensions) {
		        	System.out.println("   >>> extension: " + researchMenuExtension.className());  
		        	researchMenuExtension.buildMenu(menuResearchItem);
		        }
		        
				System.out.println("Finding plugin Tool extensions!");
		        List<ToolMenuExtensionPoint> toolMenuExtensions = pluginManager.getExtensions(ToolMenuExtensionPoint.class);
		        System.out.println(String.format("Found %d extensions for extension point '%s'", extensions.size(), ToolMenuExtensionPoint.class.getName()));
		        for (ToolMenuExtensionPoint toolMenuExtension : toolMenuExtensions) {
		        	System.out.println("   >>> extension: " + toolMenuExtension.className());  
		        	toolMenuExtension.buildMenu(menuToolItem);
		        }
		        
				System.out.println("Finding plugin Viewpoint extensions!");
		        List<ViewpointMenuExtensionPoint> viewpointMenuExtensions = pluginManager.getExtensions(ViewpointMenuExtensionPoint.class);
		        System.out.println(String.format("Found %d extensions for extension point '%s'", extensions.size(), ViewpointMenuExtensionPoint.class.getName()));
		        for (ViewpointMenuExtensionPoint viewpointMenuExtension : viewpointMenuExtensions) {
		        	System.out.println("   >>> extension: " + viewpointMenuExtension.className());  
		        	viewpointMenuExtension.buildMenu(menuViewpointItem);
		        }

			// Create and show a dialog with the menu bar.
				JDialog dialog = new JDialog();
				dialog.setTitle("Example dialog");
				dialog.setSize(450,300);
				dialog.setJMenuBar(mainMenu);				
				dialog.setVisible(true);
			}

		});
	}
}

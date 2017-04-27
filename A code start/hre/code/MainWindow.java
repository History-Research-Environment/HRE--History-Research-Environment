package hre.code;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.graphics.Rectangle;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;

public class MainWindow {

	protected Shell shlHreMain;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the Main HRE window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		// Ensure HRE Window is centred on primary monitor
		// so then the splash screen is centred within this window
		shlHreMain.setSize(800,500);
	    Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shlHreMain.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;		
		shlHreMain.setLocation(x, y);
		
		// Setup a composite using stacklayout in the MainWindow under the menu
		Composite Parent = new Composite(shlHreMain, SWT.BORDER);
		Parent.setLayout(new StackLayout());

		// Display the MainWindow
		shlHreMain.open();
		shlHreMain.layout();
		while (!shlHreMain.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
		}
		}
	}
	
	/**
	 * Setup heading and icon
	 */
	protected void createContents() {
		shlHreMain = new Shell();
		shlHreMain.setImage(SWTResourceManager.getImage(MainWindow.class, "/hre/images/HRE-32.png"));
		shlHreMain.setSize(611, 373);
		shlHreMain.setText("  HRE - History Research Environment");
		shlHreMain.setLayout(new FillLayout(SWT.HORIZONTAL));
		Menu menu = new Menu(shlHreMain, SWT.BAR);
		shlHreMain.setMenuBar(menu);

		/**
		 * Setup all menus
		 */
		MenuItem mntmProject = new MenuItem(menu, SWT.CASCADE);
		mntmProject.setToolTipText("HRE Project choices");
		mntmProject.setText("Project");
		
		Menu menu_1 = new Menu(mntmProject);
		mntmProject.setMenu(menu_1);
		
		MenuItem mntmOpenProject = new MenuItem(menu_1, SWT.NONE);
		mntmOpenProject.setText("Open Project");
		
		MenuItem mntmRecentlyUsed = new MenuItem(menu_1, SWT.NONE);
		mntmRecentlyUsed.setText("Recently Used");
		
		MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
		mntmNewItem.setText("New Project");
		
		MenuItem mntmBackup = new MenuItem(menu_1, SWT.NONE);
		mntmBackup.setText("Backup Project");
				
		MenuItem mntmRestore = new MenuItem(menu_1, SWT.NONE);
		mntmRestore.setText("Restore Project");
				
		MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
		mntmClose.setText("Close Project");
				
		MenuItem mntmCopyAs = new MenuItem(menu_1, SWT.NONE);
		mntmCopyAs.setText("Copy Project");
				
		MenuItem mntmRename = new MenuItem(menu_1, SWT.NONE);
		mntmRename.setText("Rename Project");
				
		MenuItem mntmDelete = new MenuItem(menu_1, SWT.NONE);
		mntmDelete.setText("Delete Project");
				
		MenuItem mntmExportToXml = new MenuItem(menu_1, SWT.NONE);
		mntmExportToXml.setText("Import from TMG");
				
		MenuItem mntmExportFromHre = new MenuItem(menu_1, SWT.NONE);
		mntmExportFromHre.setText("Export from HRE...");
		
		MenuItem mntmCloseExit = new MenuItem(menu_1, SWT.NONE);
		mntmCloseExit.setText("Close and Exit HRE");
		
		MenuItem mntmPerson = new MenuItem(menu, SWT.CASCADE);
		mntmPerson.setText("Person");
		
		Menu menu_4 = new Menu(mntmPerson);
		mntmPerson.setMenu(menu_4);
		
		MenuItem mntmSelectBy = new MenuItem(menu_4, SWT.NONE);
		mntmSelectBy.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_1 = new MenuItem(menu_4, SWT.NONE);
		mntmRecentlyUsed_1.setText("Recently Used");
		
		MenuItem mntmManagePersons = new MenuItem(menu_4, SWT.CASCADE);
		mntmManagePersons.setText("Manage Persons...");
		
		Menu menu_2 = new Menu(mntmManagePersons);
		mntmManagePersons.setMenu(menu_2);
		
		MenuItem mntmAdd = new MenuItem(menu_2, SWT.NONE);
		mntmAdd.setText("Add");
		
		MenuItem mntmDelete_1 = new MenuItem(menu_2, SWT.NONE);
		mntmDelete_1.setText("Delete");
		
		MenuItem mntmEdit = new MenuItem(menu_2, SWT.NONE);
		mntmEdit.setText("Edit");
		
		MenuItem mntmManagePersonName = new MenuItem(menu_4, SWT.CASCADE);
		mntmManagePersonName.setText("Manage Person Name Styles...");
		
		Menu menu_3 = new Menu(mntmManagePersonName);
		mntmManagePersonName.setMenu(menu_3);
		
		MenuItem menuItem_3 = new MenuItem(menu_3, SWT.NONE);
		menuItem_3.setText("Add");
		
		MenuItem menuItem_4 = new MenuItem(menu_3, SWT.NONE);
		menuItem_4.setText("Delete");
		
		MenuItem menuItem_5 = new MenuItem(menu_3, SWT.NONE);
		menuItem_5.setText("Edit");
		
		MenuItem mntmManagePersonFlags = new MenuItem(menu_4, SWT.CASCADE);
		mntmManagePersonFlags.setText("Manage Person Flags...");
		
		Menu menu_29 = new Menu(mntmManagePersonFlags);
		mntmManagePersonFlags.setMenu(menu_29);
		
		MenuItem mntmAdd_1 = new MenuItem(menu_29, SWT.NONE);
		mntmAdd_1.setText("Add");
		
		MenuItem mntmDelete_2 = new MenuItem(menu_29, SWT.NONE);
		mntmDelete_2.setText("Delete");
		
		MenuItem mntmEdit_1 = new MenuItem(menu_29, SWT.NONE);
		mntmEdit_1.setText("Edit");
		
		MenuItem mntmManagePersonNotepads = new MenuItem(menu_4, SWT.CASCADE);
		mntmManagePersonNotepads.setText("Manage Person NotePads...");
		
		Menu menu_30 = new Menu(mntmManagePersonNotepads);
		mntmManagePersonNotepads.setMenu(menu_30);
		
		MenuItem mntmAdd_2 = new MenuItem(menu_30, SWT.NONE);
		mntmAdd_2.setText("Add");
		
		MenuItem mntmDelete_3 = new MenuItem(menu_30, SWT.NONE);
		mntmDelete_3.setText("Delete");
		
		MenuItem mntmEdit_2 = new MenuItem(menu_30, SWT.NONE);
		mntmEdit_2.setText("Edit");
		
		MenuItem mntmAssociates = new MenuItem(menu_4, SWT.CASCADE);
		mntmAssociates.setText("Manage Person Accents...");
		
		Menu menu_5 = new Menu(mntmAssociates);
		mntmAssociates.setMenu(menu_5);
		
		MenuItem menuItem_8 = new MenuItem(menu_5, SWT.NONE);
		menuItem_8.setText("Add");
		
		MenuItem menuItem_9 = new MenuItem(menu_5, SWT.NONE);
		menuItem_9.setText("Delete");
		
		MenuItem menuItem_10 = new MenuItem(menu_5, SWT.NONE);
		menuItem_10.setText("Edit");
		
		MenuItem mntmAssociates_1 = new MenuItem(menu_4, SWT.CASCADE);
		mntmAssociates_1.setText("Associates...");
		
		Menu menu_28 = new Menu(mntmAssociates_1);
		mntmAssociates_1.setMenu(menu_28);
		
		MenuItem mntmManageEventAssociates = new MenuItem(menu_28, SWT.NONE);
		mntmManageEventAssociates.setText("Manage Event Associates");
		
		MenuItem mntmManageEventAssociate = new MenuItem(menu_28, SWT.NONE);
		mntmManageEventAssociate.setText("Manage Event Associate Flags");
		
		MenuItem mntmManageEventAssociate_1 = new MenuItem(menu_28, SWT.NONE);
		mntmManageEventAssociate_1.setText("Manage Event Associate Notepads");
		
		MenuItem mntmManageTaskAssociates = new MenuItem(menu_28, SWT.NONE);
		mntmManageTaskAssociates.setText("Manage Task Associates");
		
		MenuItem mntmManageTaskAssociate = new MenuItem(menu_28, SWT.NONE);
		mntmManageTaskAssociate.setText("Manage Task Associate Flags");
		
		MenuItem mntmManageTaskAssociate_1 = new MenuItem(menu_28, SWT.NONE);
		mntmManageTaskAssociate_1.setText("Manage Task Associate Notepads");
		
		MenuItem mntmOtherRt = new MenuItem(menu, SWT.CASCADE);
		mntmOtherRt.setText("Research Types");
		
		Menu menu_6 = new Menu(mntmOtherRt);
		mntmOtherRt.setMenu(menu_6);
		
		MenuItem mntmEvent = new MenuItem(menu, SWT.CASCADE);
		mntmEvent.setText("Events/Tasks");
		
		Menu menu_7 = new Menu(mntmEvent);
		mntmEvent.setMenu(menu_7);
		
		MenuItem mntmEvents = new MenuItem(menu_7, SWT.CASCADE);
		mntmEvents.setText("Events...");
		
		Menu menu_10 = new Menu(mntmEvents);
		mntmEvents.setMenu(menu_10);
		
		MenuItem mntmSelectBy_1 = new MenuItem(menu_10, SWT.NONE);
		mntmSelectBy_1.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_3 = new MenuItem(menu_10, SWT.NONE);
		mntmRecentlyUsed_3.setText("Recently Used");
		
		MenuItem mntmPickList = new MenuItem(menu_10, SWT.NONE);
		mntmPickList.setText("Pick List");
		
		MenuItem mntmManageEvents = new MenuItem(menu_10, SWT.NONE);
		mntmManageEvents.setText("Manage Events...");
		
		MenuItem mntmManageEventFlags = new MenuItem(menu_10, SWT.NONE);
		mntmManageEventFlags.setText("Manage Event Flags...");
		
		MenuItem mntmManageEventNotepads = new MenuItem(menu_10, SWT.NONE);
		mntmManageEventNotepads.setText("Manage Event Notepads...");
		
		MenuItem mntmTasks = new MenuItem(menu_7, SWT.CASCADE);
		mntmTasks.setText("Tasks...");
		
		Menu menu_16 = new Menu(mntmTasks);
		mntmTasks.setMenu(menu_16);
		
		MenuItem mntmSelectBy_2 = new MenuItem(menu_16, SWT.NONE);
		mntmSelectBy_2.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_4 = new MenuItem(menu_16, SWT.NONE);
		mntmRecentlyUsed_4.setText("Recently Used");
		
		MenuItem mntmPickList_1 = new MenuItem(menu_16, SWT.NONE);
		mntmPickList_1.setText("Pick List");
		
		MenuItem mntmManageTasks = new MenuItem(menu_16, SWT.NONE);
		mntmManageTasks.setText("Manage Tasks...");
		
		MenuItem mntmWherewhen = new MenuItem(menu, SWT.CASCADE);
		mntmWherewhen.setText("Where/When");
		
		Menu menu_17 = new Menu(mntmWherewhen);
		mntmWherewhen.setMenu(menu_17);
		
		MenuItem mntmLocations = new MenuItem(menu_17, SWT.CASCADE);
		mntmLocations.setText("Locations...");
		
		Menu menu_8 = new Menu(mntmLocations);
		mntmLocations.setMenu(menu_8);
		
		MenuItem mntmSelectBy_3 = new MenuItem(menu_8, SWT.NONE);
		mntmSelectBy_3.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_5 = new MenuItem(menu_8, SWT.NONE);
		mntmRecentlyUsed_5.setText("Recently Used");
		
		MenuItem mntmManageLocations = new MenuItem(menu_8, SWT.NONE);
		mntmManageLocations.setText("Manage Locations...");
		
		MenuItem mntmManageLocationName = new MenuItem(menu_8, SWT.NONE);
		mntmManageLocationName.setText("Manage Location Name Styles...");
		
		MenuItem mntmManageLocationFlags = new MenuItem(menu_8, SWT.NONE);
		mntmManageLocationFlags.setText("Manage Location Flags...");
		
		MenuItem mntmManageLocationNotepads = new MenuItem(menu_8, SWT.NONE);
		mntmManageLocationNotepads.setText("Manage Location Notepads...");
		
		MenuItem mntmOccasions = new MenuItem(menu_17, SWT.CASCADE);
		mntmOccasions.setText("Occasions...");
		
		Menu menu_18 = new Menu(mntmOccasions);
		mntmOccasions.setMenu(menu_18);
		
		MenuItem mntmSelectBy_5 = new MenuItem(menu_18, SWT.NONE);
		mntmSelectBy_5.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_6 = new MenuItem(menu_18, SWT.NONE);
		mntmRecentlyUsed_6.setText("Recently Used");
		
		MenuItem mntmIckList = new MenuItem(menu_18, SWT.NONE);
		mntmIckList.setText("Pick List");
		
		MenuItem mntmManageOccasions = new MenuItem(menu_18, SWT.NONE);
		mntmManageOccasions.setText("Manage Occasions...");
		
		MenuItem mntmHistoricalDates = new MenuItem(menu_17, SWT.CASCADE);
		mntmHistoricalDates.setText("Historical Dates...");
		
		Menu menu_19 = new Menu(mntmHistoricalDates);
		mntmHistoricalDates.setMenu(menu_19);
		
		MenuItem mntmSelectBy_4 = new MenuItem(menu_19, SWT.NONE);
		mntmSelectBy_4.setText("Select By...");
		
		MenuItem mntmManageHistoricalDates = new MenuItem(menu_19, SWT.NONE);
		mntmManageHistoricalDates.setText("Manage Historical Dates...");
		
		MenuItem mntmDateConverter = new MenuItem(menu_19, SWT.NONE);
		mntmDateConverter.setText("Date Converter");
		
		MenuItem mntmDateCalculator = new MenuItem(menu_19, SWT.NONE);
		mntmDateCalculator.setText("Date Calculator");
		
		MenuItem mntmSource = new MenuItem(menu, SWT.CASCADE);
		mntmSource.setText("Evidence");
		
		Menu menu_9 = new Menu(mntmSource);
		mntmSource.setMenu(menu_9);
		
		MenuItem mntmSources = new MenuItem(menu_9, SWT.CASCADE);
		mntmSources.setText("Sources...");
		
		Menu menu_22 = new Menu(mntmSources);
		mntmSources.setMenu(menu_22);
		
		MenuItem mntmSelectBy_6 = new MenuItem(menu_22, SWT.NONE);
		mntmSelectBy_6.setText("Select By...");
		
		MenuItem mntmRecentlyUsed_7 = new MenuItem(menu_22, SWT.NONE);
		mntmRecentlyUsed_7.setText("Recently Used");
		
		MenuItem mntmPickList_2 = new MenuItem(menu_22, SWT.NONE);
		mntmPickList_2.setText("Pick List");
		
		MenuItem mntmManageSources = new MenuItem(menu_22, SWT.NONE);
		mntmManageSources.setText("Manage Sources...");
		
		MenuItem mntmManageSourceName = new MenuItem(menu_22, SWT.NONE);
		mntmManageSourceName.setText("Manage Source Name Styles...");
		
		MenuItem mntmManageSourceFlags = new MenuItem(menu_22, SWT.NONE);
		mntmManageSourceFlags.setText("Manage Source Flags...");
		
		MenuItem mntmManageSourceNotepads = new MenuItem(menu_22, SWT.NONE);
		mntmManageSourceNotepads.setText("Manage Source Notepads...");
		
		MenuItem mntmManageSourcerepositoryLinks = new MenuItem(menu_22, SWT.NONE);
		mntmManageSourcerepositoryLinks.setText("Manage Source-Repository Links...");
		
		MenuItem mntmCitations_1 = new MenuItem(menu_9, SWT.CASCADE);
		mntmCitations_1.setText("Citations...");
		
		Menu menu_23 = new Menu(mntmCitations_1);
		mntmCitations_1.setMenu(menu_23);
		
		MenuItem mntmSelectBy_7 = new MenuItem(menu_23, SWT.NONE);
		mntmSelectBy_7.setText("Select By...");
		
		MenuItem mntmPickList_3 = new MenuItem(menu_23, SWT.NONE);
		mntmPickList_3.setText("Pick List");
		
		MenuItem mntmRecentlyUsed_8 = new MenuItem(menu_23, SWT.NONE);
		mntmRecentlyUsed_8.setText("Recently Used");
		
		MenuItem mntmManageCitations = new MenuItem(menu_23, SWT.NONE);
		mntmManageCitations.setText("Manage Citations...");
		
		MenuItem mntmManageCitationFlags = new MenuItem(menu_23, SWT.NONE);
		mntmManageCitationFlags.setText("Manage Citation Flags...");
		
		MenuItem mntmManageCitationNotepads = new MenuItem(menu_23, SWT.NONE);
		mntmManageCitationNotepads.setText("Manage Citation Notepads...");
		
		MenuItem mntmRepositories_1 = new MenuItem(menu_9, SWT.CASCADE);
		mntmRepositories_1.setText("Repository...");
		
		Menu menu_24 = new Menu(mntmRepositories_1);
		mntmRepositories_1.setMenu(menu_24);
		
		MenuItem mntmSelectBy_8 = new MenuItem(menu_24, SWT.NONE);
		mntmSelectBy_8.setText("Select By...");
		
		MenuItem mntmPickList_4 = new MenuItem(menu_24, SWT.NONE);
		mntmPickList_4.setText("Pick List");
		
		MenuItem mntmRecentlyUsed_9 = new MenuItem(menu_24, SWT.NONE);
		mntmRecentlyUsed_9.setText("Recently Used");
		
		MenuItem mntmManageRepositories = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositories.setText("Manage Repositories...");
		
		MenuItem mntmManageRepositoryFlags = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositoryFlags.setText("Manage Repository Flags...");
		
		MenuItem mntmManageRepositoryNotepads = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositoryNotepads.setText("Manage Repository Notepads...");
		
		MenuItem mntmManageRepositorysourceLinks = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositorysourceLinks.setText("Manage Repository-Source Links...");
		
		MenuItem mntmManageRepositoryLink = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositoryLink.setText("Manage Repository Link Flags...");
		
		MenuItem mntmManageRepositoryLink_1 = new MenuItem(menu_24, SWT.NONE);
		mntmManageRepositoryLink_1.setText("Manage Repository Link Notepads...");
		
		MenuItem mntmImages_1 = new MenuItem(menu_9, SWT.CASCADE);
		mntmImages_1.setText("Images...");
		
		Menu menu_31 = new Menu(mntmImages_1);
		mntmImages_1.setMenu(menu_31);
		
		MenuItem mntmSelectBy_9 = new MenuItem(menu_31, SWT.NONE);
		mntmSelectBy_9.setText("Select By...");
		
		MenuItem mntmPickList_5 = new MenuItem(menu_31, SWT.NONE);
		mntmPickList_5.setText("Pick List");
		
		MenuItem mntmEcentlyUsed = new MenuItem(menu_31, SWT.NONE);
		mntmEcentlyUsed.setText("Recently Used");
		
		MenuItem mntmManageImages = new MenuItem(menu_31, SWT.NONE);
		mntmManageImages.setText("Manage Images...");
		
		MenuItem mntmManageImageFlags = new MenuItem(menu_31, SWT.NONE);
		mntmManageImageFlags.setText("Manage Image Flags...");
		
		MenuItem mntmManageImageNotepads = new MenuItem(menu_31, SWT.NONE);
		mntmManageImageNotepads.setText("Manage Image Notepads...");
		
		MenuItem mntmElectLinksTo = new MenuItem(menu_31, SWT.NONE);
		mntmElectLinksTo.setText("Select links to...");
		
		MenuItem mntmManageLinksTo = new MenuItem(menu_31, SWT.NONE);
		mntmManageLinksTo.setText("Manage Links to...");
		
		MenuItem mntmExternalFiles_1 = new MenuItem(menu_9, SWT.CASCADE);
		mntmExternalFiles_1.setText("External Files...");
		
		Menu menu_32 = new Menu(mntmExternalFiles_1);
		mntmExternalFiles_1.setMenu(menu_32);
		
		MenuItem mntmSelectBy_10 = new MenuItem(menu_32, SWT.NONE);
		mntmSelectBy_10.setText("Select By...");
		
		MenuItem mntmPickList_6 = new MenuItem(menu_32, SWT.NONE);
		mntmPickList_6.setText("Pick List");
		
		MenuItem mntmRecentlyUsed_10 = new MenuItem(menu_32, SWT.NONE);
		mntmRecentlyUsed_10.setText("Recently Used");
		
		MenuItem mntmAnageFiles = new MenuItem(menu_32, SWT.NONE);
		mntmAnageFiles.setText("Manage Files...");
		
		MenuItem mntmManageFileFlags = new MenuItem(menu_32, SWT.NONE);
		mntmManageFileFlags.setText("Manage File Flags...");
		
		MenuItem mntmManageFileNotepads = new MenuItem(menu_32, SWT.NONE);
		mntmManageFileNotepads.setText("Manage File Notepads...");
		
		MenuItem mntmSelectLinksTo = new MenuItem(menu_32, SWT.NONE);
		mntmSelectLinksTo.setText("Select Links to...");
		
		MenuItem mntmManageLinksTo_1 = new MenuItem(menu_32, SWT.NONE);
		mntmManageLinksTo_1.setText("Manage Links to...");
		
		MenuItem mntmInternalText_1 = new MenuItem(menu_9, SWT.CASCADE);
		mntmInternalText_1.setText("Internal Text...");
		
		Menu menu_33 = new Menu(mntmInternalText_1);
		mntmInternalText_1.setMenu(menu_33);
		
		MenuItem mntmSelectBy_11 = new MenuItem(menu_33, SWT.NONE);
		mntmSelectBy_11.setText("Select By...");
		
		MenuItem mntmIckList_1 = new MenuItem(menu_33, SWT.NONE);
		mntmIckList_1.setText("Pick List");
		
		MenuItem mntmRecentlyUsed_11 = new MenuItem(menu_33, SWT.NONE);
		mntmRecentlyUsed_11.setText("Recently Used");
		
		MenuItem mntmManageInternalText = new MenuItem(menu_33, SWT.NONE);
		mntmManageInternalText.setText("Manage Internal Text...");
		
		MenuItem mntmManageInternalText_1 = new MenuItem(menu_33, SWT.NONE);
		mntmManageInternalText_1.setText("Manage Internal Text Flags...");
		
		MenuItem mntmManageInternalText_2 = new MenuItem(menu_33, SWT.NONE);
		mntmManageInternalText_2.setText("Manage Internal Text Notepads...");
		
		MenuItem mntmSelectLinksTo_1 = new MenuItem(menu_33, SWT.NONE);
		mntmSelectLinksTo_1.setText("Select Links to...");
		
		MenuItem mntmManageLinksTo_2 = new MenuItem(menu_33, SWT.NONE);
		mntmManageLinksTo_2.setText("Manage Links to...");
		
		MenuItem mntmReport = new MenuItem(menu, SWT.CASCADE);
		mntmReport.setText("Reports");
		
		Menu menu_11 = new Menu(mntmReport);
		mntmReport.setMenu(menu_11);
		
		MenuItem mntmRecentlyUsed_2 = new MenuItem(menu_11, SWT.NONE);
		mntmRecentlyUsed_2.setText("Recently Used");
		
		MenuItem mntmListOf = new MenuItem(menu_11, SWT.CASCADE);
		mntmListOf.setText("List of...");
		
		Menu menu_25 = new Menu(mntmListOf);
		mntmListOf.setMenu(menu_25);
		
		MenuItem mntmPersons = new MenuItem(menu_25, SWT.NONE);
		mntmPersons.setText("Persons");
		
		MenuItem mntmLocations_1 = new MenuItem(menu_25, SWT.NONE);
		mntmLocations_1.setText("Locations");
		
		MenuItem mntmEvents_1 = new MenuItem(menu_25, SWT.NONE);
		mntmEvents_1.setText("Events");
		
		MenuItem mntmTasks_1 = new MenuItem(menu_25, SWT.NONE);
		mntmTasks_1.setText("Tasks");
		
		MenuItem mntmOurces = new MenuItem(menu_25, SWT.NONE);
		mntmOurces.setText("Sources");
		
		MenuItem mntmRepositories = new MenuItem(menu_25, SWT.NONE);
		mntmRepositories.setText("Repositories");
		
		MenuItem mntmCitations = new MenuItem(menu_25, SWT.NONE);
		mntmCitations.setText("Citations");
		
		MenuItem mntmImages = new MenuItem(menu_25, SWT.NONE);
		mntmImages.setText("Images");
		
		MenuItem mntmExternalFiles = new MenuItem(menu_25, SWT.NONE);
		mntmExternalFiles.setText("External Files");
		
		MenuItem mntmInternalText = new MenuItem(menu_25, SWT.NONE);
		mntmInternalText.setText("Internal Text");
		
		MenuItem mntmManage = new MenuItem(menu, SWT.CASCADE);
		mntmManage.setText("Tools");
		
		Menu menu_12 = new Menu(mntmManage);
		mntmManage.setMenu(menu_12);
		
		MenuItem mntmProjSettings = new MenuItem(menu_12, SWT.CASCADE);
		mntmProjSettings.setText("Settings...");
		
		Menu menu_13 = new Menu(mntmProjSettings);
		mntmProjSettings.setMenu(menu_13);
		
		MenuItem mntmProjectSetting = new MenuItem(menu_13, SWT.NONE);
		mntmProjectSetting.setText("Client");
		
		MenuItem mntmComputer = new MenuItem(menu_13, SWT.NONE);
		mntmComputer.setText("User");
		
		MenuItem mntmServer = new MenuItem(menu_13, SWT.NONE);
		mntmServer.setText("Server");
		
		MenuItem mntmScreen = new MenuItem(menu_13, SWT.NONE);
		mntmScreen.setText("Monitor");
		
		MenuItem mntmUser = new MenuItem(menu_13, SWT.NONE);
		mntmUser.setText("GUI Language");
		
		MenuItem mntmAdmin = new MenuItem(menu_13, SWT.NONE);
		mntmAdmin.setText("Project");
		
		MenuItem mntmProject_1 = new MenuItem(menu_13, SWT.NONE);
		mntmProject_1.setText("Persons");
		
		MenuItem mntmLocation_1 = new MenuItem(menu_13, SWT.NONE);
		mntmLocation_1.setText("Location");
		
		MenuItem mntmHistoricalDates_1 = new MenuItem(menu_13, SWT.NONE);
		mntmHistoricalDates_1.setText("Historical Date");
		
		MenuItem mntmEvents_2 = new MenuItem(menu_13, SWT.NONE);
		mntmEvents_2.setText("Events");
		
		MenuItem mntmTasks_2 = new MenuItem(menu_13, SWT.NONE);
		mntmTasks_2.setText("Tasks");
		
		MenuItem mntmOccasions_1 = new MenuItem(menu_13, SWT.NONE);
		mntmOccasions_1.setText("Occasions");
		
		MenuItem mntmSources_1 = new MenuItem(menu_13, SWT.NONE);
		mntmSources_1.setText("Sources");
		
		MenuItem mntmSourceNameStyles = new MenuItem(menu_13, SWT.NONE);
		mntmSourceNameStyles.setText("Source Name Styles");
		
		MenuItem mntmRepositories_2 = new MenuItem(menu_13, SWT.NONE);
		mntmRepositories_2.setText("Repositories");
		
		MenuItem mntmRepositorySource = new MenuItem(menu_13, SWT.NONE);
		mntmRepositorySource.setText("Repository-Source Links");
		
		MenuItem mntmTools = new MenuItem(menu_12, SWT.CASCADE);
		mntmTools.setText("Application Language...");
		
		Menu menu_14 = new Menu(mntmTools);
		mntmTools.setMenu(menu_14);
		
		MenuItem mntmApplicationLanguage = new MenuItem(menu_14, SWT.CASCADE);
		mntmApplicationLanguage.setText("Select");
		
		Menu menu_26 = new Menu(mntmApplicationLanguage);
		mntmApplicationLanguage.setMenu(menu_26);
		
		MenuItem mntmUserLanguage = new MenuItem(menu_14, SWT.CASCADE);
		mntmUserLanguage.setText("Delete");
		
		Menu menu_27 = new Menu(mntmUserLanguage);
		mntmUserLanguage.setMenu(menu_27);
		
		MenuItem mntmEdit_3 = new MenuItem(menu_14, SWT.NONE);
		mntmEdit_3.setText("Edit");
		
		MenuItem mntmUserLanguage_1 = new MenuItem(menu_12, SWT.CASCADE);
		mntmUserLanguage_1.setText("User Language...");
		
		Menu menu_20 = new Menu(mntmUserLanguage_1);
		mntmUserLanguage_1.setMenu(menu_20);
		
		MenuItem mntmSelect = new MenuItem(menu_20, SWT.NONE);
		mntmSelect.setText("Select");
		
		MenuItem mntmDelete_4 = new MenuItem(menu_20, SWT.NONE);
		mntmDelete_4.setText("Delete");
		
		MenuItem mntmDit = new MenuItem(menu_20, SWT.NONE);
		mntmDit.setText("Edit");
		
		MenuItem mntmViewpoints = new MenuItem(menu_12, SWT.CASCADE);
		mntmViewpoints.setText("Viewpoints...");
		
		Menu menu_21 = new Menu(mntmViewpoints);
		mntmViewpoints.setMenu(menu_21);
		
		MenuItem mntmSelect_1 = new MenuItem(menu_21, SWT.NONE);
		mntmSelect_1.setText("Select");
		
		MenuItem mntmAdd_3 = new MenuItem(menu_21, SWT.NONE);
		mntmAdd_3.setText("Add");
		
		MenuItem mntmDelete_5 = new MenuItem(menu_21, SWT.NONE);
		mntmDelete_5.setText("Delete");
		
		MenuItem mntmEdit_4 = new MenuItem(menu_21, SWT.NONE);
		mntmEdit_4.setText("Edit");
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_15 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_15);
		
		MenuItem mntmSearchHelp = new MenuItem(menu_15, SWT.NONE);
		mntmSearchHelp.setText("Help Contents");
		
		MenuItem mntmEditHelp = new MenuItem(menu_15, SWT.NONE);
		mntmEditHelp.setText("Edit Help");
		
		MenuItem mntmWebsite = new MenuItem(menu_15, SWT.NONE);
		mntmWebsite.setText("HRE website");
		
		MenuItem mntmAbout = new MenuItem(menu_15, SWT.NONE);
		mntmAbout.setText("About HRE");
		
		// Project, Close & Exit
		mntmCloseExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			//
			//**  So far, this just closes program - will need to add code here to close project files etc	
			//    as per Specification xxx.
			//    Same code applies to use of 'X' button on MainWindow.
			System.exit(0);}
		});
		
				
		// For Help Menus //
		// Help, Website (go to)
		mntmWebsite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			if(Desktop.isDesktopSupported())
				{
				  try {
					Desktop.getDesktop().browse(new URI("http://www.historyresearchenvironment.org"));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}    
			}
		});
		// Help, About
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			HelpAbout splashscreen=new HelpAbout();  
			splashscreen.setLocationRelativeTo(null);
			splashscreen.setVisible(true);}
		});		

		/**
		 * Show the Help, About screen at program startup as a Splash screen
		 */
		//
		//  Bug - when splash closed, MainWindow doesn't get focus again 
		//
		HelpAbout splashscreen=new HelpAbout();  
		splashscreen.setLocationRelativeTo(null);
		splashscreen.setVisible(true);
		
	}
}

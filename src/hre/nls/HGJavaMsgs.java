package hre.nls;

import javax.swing.UIManager;

import org.eclipse.osgi.util.NLS;

public class HGJavaMsgs extends NLS {

	public HGJavaMsgs(String guiLanguage) {		
		// Set JOptionpane, ColorChooser, FileChooser translations 
		switch (guiLanguage) {
	        case "en":			// English(US)
	        case "gb":			// English(UK/AU/NZ)	
	        	UIManager.put("OptionPane.cancelButtonText", "Cancel");
	    		UIManager.put("OptionPane.noButtonText", "No");
	    		UIManager.put("OptionPane.okButtonText", "OK");
	    		UIManager.put("OptionPane.yesButtonText", "Yes");
	    		
	    		UIManager.put("ColorChooser.cancelText", "Cancel");
	    		UIManager.put("ColorChooser.okText", "OK");
	    		UIManager.put("ColorChooser.previewText", "Preview");
	    		UIManager.put("ColorChooser.resetText", "Reset");
	    		UIManager.put("ColorChooser.swatchesRecentText", "Recent:");
	    		UIManager.put("ColorChooser.swatchesNameText", "Swatches");
	    		
	    		UIManager.put("FileChooser.cancelButtonText", "Cancel");
	    		UIManager.put("FileChooser.cancelButtonToolTipText", "Abort File Chooser Dialog");
	    		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Details");
	    		UIManager.put("FileChooser.detailsViewButtonToolTipText", "Details");
	    		UIManager.put("FileChooser.directoryDescriptionText", "Directory");
	    		UIManager.put("FileChooser.directoryOpenButtonText", "Open");
	    		UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Open selected directory");
	    		UIManager.put("FileChooser.fileDescriptionText", "Generic file");
	    		UIManager.put("FileChooser.fileNameLabelText", "File Name: ");
	    		UIManager.put("FileChooser.filesOfTypeLabelText", "Files of Type: ");
	    		UIManager.put("FileChooser.helpButtonText", "Help");
	    		UIManager.put("FileChooser.helpButtonToolTipText", "FileChooser Help");
	    		UIManager.put("FileChooser.homeFolderAccessibleName", "Home");
	    		UIManager.put("FileChooser.homeFolderToolTipText", "Home");
	    		UIManager.put("FileChooser.listViewButtonAccessibleName", "List");
	    		UIManager.put("FileChooser.listViewButtonToolTipText", "List");
	    		UIManager.put("FileChooser.lookInLabelText", "Look In: ");
	    		UIManager.put("FileChooser.newFolderAccessibleName", "New Folder");
	    		UIManager.put("FileChooser.newFolderErrorText", "Error creating new folder");
	    		UIManager.put("FileChooser.newFolderToolTipText", "Create new folder");
	    		UIManager.put("FileChooser.openButtonText", "Open");
	    		UIManager.put("FileChooser.openButtonToolTipText", "Open selected file");
	    		UIManager.put("FileChooser.openDialogTitleText", "Open");
	    		UIManager.put("FileChooser.saveButtonText", "Save");
	    		UIManager.put("FileChooser.saveButtonToolTipText", "Save selected file");
	    		UIManager.put("FileChooser.saveDialogTitleText", "Save");
	    		UIManager.put("FileChooser.saveInLabelText", "Save In: ");
	    		UIManager.put("FileChooser.updateButtonText", "Update");
	    		UIManager.put("FileChooser.updateButtonToolTipText", "Update directory listing");
	    		UIManager.put("FileChooser.upFolderToolTipText", "Up One Level");
	            break;
	        case "fr":			// French
	        	UIManager.put("OptionPane.cancelButtonText", "Annuler");
	    		UIManager.put("OptionPane.noButtonText", "Non");
	    		UIManager.put("OptionPane.okButtonText", "OK");
	    		UIManager.put("OptionPane.yesButtonText", "Oui");
	    		
	    		UIManager.put("ColorChooser.cancelText", "Annuler");
	    		UIManager.put("ColorChooser.okText", "OK");
	    		UIManager.put("ColorChooser.previewText", "Aper�u");
	    		UIManager.put("ColorChooser.resetText", "R�initialiser");
	    		UIManager.put("ColorChooser.swatchesRecentText", "R�cent:");
	    		UIManager.put("ColorChooser.swatchesNameText", "Nuancier");
	    		
	    		UIManager.put("FileChooser.cancelButtonText", "Annuler");
	    		UIManager.put("FileChooser.cancelButtonToolTipText", "Bo�te de dialogue Abandonner le s�lecteur de fichier");
	    		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Des d�tails");
	    		UIManager.put("FileChooser.detailsViewButtonToolTipText", "Des d�tails");
	    		UIManager.put("FileChooser.directoryDescriptionText", "Annuaire");
	    		UIManager.put("FileChooser.directoryOpenButtonText", "Ouvert");
	    		UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Ouvrir le r�pertoire s�lectionn�");
	    		UIManager.put("FileChooser.fileDescriptionText", "Fichier g�n�rique");
	    		UIManager.put("FileChooser.fileNameLabelText", "Nom de fichier: ");
	    		UIManager.put("FileChooser.filesOfTypeLabelText", "Des fichiers de type: ");
	    		UIManager.put("FileChooser.helpButtonText", "Aider");
	    		UIManager.put("FileChooser.helpButtonToolTipText", "Aide FileChooser");
	    		UIManager.put("FileChooser.homeFolderAccessibleName", "Maison");
	    		UIManager.put("FileChooser.homeFolderToolTipText", "Maison");
	    		UIManager.put("FileChooser.listViewButtonAccessibleName", "Lister");
	    		UIManager.put("FileChooser.listViewButtonToolTipText", "Lister");
	    		UIManager.put("FileChooser.lookInLabelText", "Regarder dans: ");
	    		UIManager.put("FileChooser.newFolderAccessibleName", "Nouveau dossier");
	    		UIManager.put("FileChooser.newFolderErrorText", "Erreur lors de la cr�ation d'un nouveau dossier");
	    		UIManager.put("FileChooser.newFolderToolTipText", "Cr�er un nouveau dossier");
	    		UIManager.put("FileChooser.openButtonText", "Ouvert");
	    		UIManager.put("FileChooser.openButtonToolTipText", "Ouvrir le fichier s�lectionn�");
	    		UIManager.put("FileChooser.openDialogTitleText", "Ouvert");
	    		UIManager.put("FileChooser.saveButtonText", "Sauvegarder");
	    		UIManager.put("FileChooser.saveButtonToolTipText", "Enregistrer le fichier s�lectionn�");
	    		UIManager.put("FileChooser.saveDialogTitleText", "Sauvegarder");
	    		UIManager.put("FileChooser.saveInLabelText", "Sauver dans: ");
	    		UIManager.put("FileChooser.updateButtonText", "Mettre � jour");
	    		UIManager.put("FileChooser.updateButtonToolTipText", "Mettre � jour la liste des r�pertoires");
	    		UIManager.put("FileChooser.upFolderToolTipText", "Augmente d'un niveau");
	    		break;
	        case "de":			// German
	        	UIManager.put("OptionPane.cancelButtonText", "Abbrechen");
	    		UIManager.put("OptionPane.noButtonText", "Nein");
	    		UIManager.put("OptionPane.okButtonText", "OK");
	    		UIManager.put("OptionPane.yesButtonText", "Ja");
	    		
	    		UIManager.put("ColorChooser.cancelText", "Abbrechen");
	    		UIManager.put("ColorChooser.okText", "OK");
	    		UIManager.put("ColorChooser.previewText", "Vorschau");
	    		UIManager.put("ColorChooser.resetText", "Zur�cksetzen");
	    		UIManager.put("ColorChooser.swatchesRecentText", "K�rzlich:");
	    		UIManager.put("ColorChooser.swatchesNameText", "Farbfelder");
	    		
	    		UIManager.put("FileChooser.cancelButtonText", "Abbrechen");
	    		UIManager.put("FileChooser.cancelButtonToolTipText", "Dialogfeld \"Dateiauswahl abbrechen\"");
	    		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Einzelheiten");
	    		UIManager.put("FileChooser.detailsViewButtonToolTipText", "Einzelheiten");
	    		UIManager.put("FileChooser.directoryDescriptionText", "Verzeichnis");
	    		UIManager.put("FileChooser.directoryOpenButtonText", "�ffnen");
	    		UIManager.put("FileChooser.directoryOpenButtonToolTipText", "�ffnen Sie das ausgew�hlte Verzeichnis");
	    		UIManager.put("FileChooser.fileDescriptionText", "Generische Datei");
	    		UIManager.put("FileChooser.fileNameLabelText", "Dateinamen: ");
	    		UIManager.put("FileChooser.filesOfTypeLabelText", "Dateien des Typs: ");
	    		UIManager.put("FileChooser.helpButtonText", "Hilfe");
	    		UIManager.put("FileChooser.helpButtonToolTipText", "FileChooser-Hilfe");
	    		UIManager.put("FileChooser.homeFolderAccessibleName", "Zuhause");
	    		UIManager.put("FileChooser.homeFolderToolTipText", "Zuhause");
	    		UIManager.put("FileChooser.listViewButtonAccessibleName", "Liste");
	    		UIManager.put("FileChooser.listViewButtonToolTipText", "Liste");
	    		UIManager.put("FileChooser.lookInLabelText", "Suchen in: ");
	    		UIManager.put("FileChooser.newFolderAccessibleName", "Neuer Ordner");
	    		UIManager.put("FileChooser.newFolderErrorText", "Fehler beim Erstellen eines neuen Ordners");
	    		UIManager.put("FileChooser.newFolderToolTipText", "Neuen Ordner erstellen");
	    		UIManager.put("FileChooser.openButtonText", "�ffnen");
	    		UIManager.put("FileChooser.openButtonToolTipText", "�ffnen Sie die ausgew�hlte Datei");
	    		UIManager.put("FileChooser.openDialogTitleText", "�ffnen");
	    		UIManager.put("FileChooser.saveButtonText", "Speichern");
	    		UIManager.put("FileChooser.saveButtonToolTipText", "Speichern Sie die ausgew�hlte Datei");
	    		UIManager.put("FileChooser.saveDialogTitleText", "Speichern");
	    		UIManager.put("FileChooser.saveInLabelText", "Speichern in: ");
	    		UIManager.put("FileChooser.updateButtonText", "Aktualisieren");
	    		UIManager.put("FileChooser.updateButtonToolTipText", "Verzeichnisliste aktualisieren");
	    		UIManager.put("FileChooser.upFolderToolTipText", "Eine Ebene h�her");
	    		break;
	        case "it":			// Italian
	        	UIManager.put("OptionPane.cancelButtonText", "Annulla");
	        	UIManager.put("OptionPane.noButtonText", "No");
	        	UIManager.put("OptionPane.okButtonText", "OK");
	        	UIManager.put("OptionPane.yesButtonText", "S�");

	        	UIManager.put("ColorChooser.cancelText", "Annulla");
	        	UIManager.put("ColorChooser.okText", "OK");
	        	UIManager.put("ColorChooser.previewText", "Anteprima");
	        	UIManager.put("ColorChooser.resetText", "Ripristina");
	        	UIManager.put("ColorChooser.swatchesRecentText", "Recenti:");
	        	UIManager.put("ColorChooser.swatchesNameText", "Campioni");

	        	UIManager.put("FileChooser.cancelButtonText", "Annulla");
	        	UIManager.put("FileChooser.cancelButtonToolTipText", "Finestra di dialogo Annulla selezione file");
	        	UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Dettagli");
	        	UIManager.put("FileChooser.detailsViewButtonToolTipText", "Dettagli");
	        	UIManager.put("FileChooser.directoryDescriptionText", "Direttorio");
	        	UIManager.put("FileChooser.directoryOpenButtonText", "Apri");
	        	UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Apri cartella selezionata");
	        	UIManager.put("FileChooser.fileDescriptionText", "File generico");
	        	UIManager.put("FileChooser.fileNameLabelText", "Nome file: ");
	        	UIManager.put("FileChooser.filesOfTypeLabelText", "File di tipo: ");
	        	UIManager.put("FileChooser.helpButtonText", "Aiuto");
	        	UIManager.put("FileChooser.helpButtonToolTipText", "Aiuto FileChooser");
	        	UIManager.put("FileChooser.homeFolderAccessibleName", "Casa");
	        	UIManager.put("FileChooser.homeFolderToolTipText", "Casa");
	        	UIManager.put("FileChooser.listViewButtonAccessibleName", "Elenco");
	        	UIManager.put("FileChooser.listViewButtonToolTipText", "Elenco");
	        	UIManager.put("FileChooser.lookInLabelText", "Cerca in: ");
	        	UIManager.put("FileChooser.newFolderAccessibleName", "Nuova cartella");
	        	UIManager.put("FileChooser.newFolderErrorText", "Errore durante la creazione di una nuova cartella");
	        	UIManager.put("FileChooser.newFolderToolTipText", "Crea nuova cartella");
	        	UIManager.put("FileChooser.openButtonText", "Apri");
	        	UIManager.put("FileChooser.openButtonToolTipText", "Apri file selezionato");
	        	UIManager.put("FileChooser.openDialogTitleText", "Apri");
	        	UIManager.put("FileChooser.saveButtonText", "Salva");
	        	UIManager.put("FileChooser.saveButtonToolTipText", "Salva file selezionato");
	        	UIManager.put("FileChooser.saveDialogTitleText", "Salva");
	        	UIManager.put("FileChooser.saveInLabelText", "Salva in: ");
	        	UIManager.put("FileChooser.updateButtonText", "Aggiorna");
	        	UIManager.put("FileChooser.updateButtonToolTipText", "Aggiorna elenco directory");
	        	UIManager.put("FileChooser.upFolderToolTipText", "Su un livello");
	    		break;
	        case "es":			// Spanish
	        	UIManager.put("OptionPane.cancelButtonText", "Cancelar");
	        	UIManager.put("OptionPane.noButtonText", "No");
	        	UIManager.put("OptionPane.okButtonText", "OK");
	        	UIManager.put("OptionPane.yesButtonText", "S�");

	        	UIManager.put("ColorChooser.cancelText", "Cancelar");
	        	UIManager.put("ColorChooser.okText", "OK");
	        	UIManager.put("ColorChooser.previewText", "Vista previa");
	        	UIManager.put("ColorChooser.resetText", "Restablecer");
	        	UIManager.put("ColorChooser.swatchesRecentText", "Reciente:");
	        	UIManager.put("ColorChooser.swatchesNameText", "Muestras");

	        	UIManager.put("FileChooser.cancelButtonText", "Cancelar");
	        	UIManager.put("FileChooser.cancelButtonToolTipText", "Cancelar cuadro de di�logo del selector de archivos");
	        	UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Detalles");
	        	UIManager.put("FileChooser.detailsViewButtonToolTipText", "Detalles");
	        	UIManager.put("FileChooser.directoryDescriptionText", "Directorio");
	        	UIManager.put("FileChooser.directoryOpenButtonText", "Abrir");
	        	UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Abrir directorio seleccionado");
	        	UIManager.put("FileChooser.fileDescriptionText", "Archivo gen�rico");
	        	UIManager.put("FileChooser.fileNameLabelText", "Nombre de archivo: ");
	        	UIManager.put("FileChooser.filesOfTypeLabelText", "Archivos de tipo: ");
	        	UIManager.put("FileChooser.helpButtonText", "Ayuda");
	        	UIManager.put("FileChooser.helpButtonToolTipText", "Ayuda de FileChooser");
	        	UIManager.put("FileChooser.homeFolderAccessibleName", "Inicio");
	        	UIManager.put("FileChooser.homeFolderToolTipText", "Inicio");
	        	UIManager.put("FileChooser.listViewButtonAccessibleName", "Lista");
	        	UIManager.put("FileChooser.listViewButtonToolTipText", "Lista");
	        	UIManager.put("FileChooser.lookInLabelText", "Buscar en: ");
	        	UIManager.put("FileChooser.newFolderAccessibleName", "Nueva carpeta");
	        	UIManager.put("FileChooser.newFolderErrorText", "Error al crear una nueva carpeta");
	        	UIManager.put("FileChooser.newFolderToolTipText", "Crear nueva carpeta");
	        	UIManager.put("FileChooser.openButtonText", "Abrir");
	        	UIManager.put("FileChooser.openButtonToolTipText", "Abrir archivo seleccionado");
	        	UIManager.put("FileChooser.openDialogTitleText", "Abrir");
	        	UIManager.put("FileChooser.saveButtonText", "Guardar");
	        	UIManager.put("FileChooser.saveButtonToolTipText", "Guardar archivo seleccionado");
	        	UIManager.put("FileChooser.saveDialogTitleText", "Guardar");
	        	UIManager.put("FileChooser.saveInLabelText", "Guardar en: ");
	        	UIManager.put("FileChooser.updateButtonText", "Actualizar");
	        	UIManager.put("FileChooser.updateButtonToolTipText", "Actualizar lista de directorios");
	        	UIManager.put("FileChooser.upFolderToolTipText", "Subir un nivel");
	        	break;
	        case "nl":			// Dutch
	        	UIManager.put("OptionPane.cancelButtonText", "Annuleren");
	    		UIManager.put("OptionPane.noButtonText", "Nee");
	    		UIManager.put("OptionPane.okButtonText", "OK");
	    		UIManager.put("OptionPane.yesButtonText", "Ja");
	    		
	    		UIManager.put("ColorChooser.cancelText", "Annuleren");
	    		UIManager.put("ColorChooser.okText", "OK");
	    		UIManager.put("ColorChooser.previewText", "Voorbeeld");
	    		UIManager.put("ColorChooser.resetText", "Opnieuw instellen");
	    		UIManager.put("ColorChooser.swatchesRecentText", "Recent:");
	    		UIManager.put("ColorChooser.swatchesNameText", "Stalen");
	    		
	    		UIManager.put("FileChooser.cancelButtonText", "Annuleren");
	    		UIManager.put("FileChooser.cancelButtonToolTipText", "Dialoogvenster Bestandkiezer afbreken");
	    		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Details");
	    		UIManager.put("FileChooser.detailsViewButtonToolTipText", "Details");
	    		UIManager.put("FileChooser.directoryDescriptionText", "Directory");
	    		UIManager.put("FileChooser.directoryOpenButtonText", "Open");
	    		UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Geselecteerde map openen");
	    		UIManager.put("FileChooser.fileDescriptionText", "Algemeen bestand");
	    		UIManager.put("FileChooser.fileNameLabelText", "Bestandsnaam: ");
	    		UIManager.put("FileChooser.filesOfTypeLabelText", "Bestanden van Type: ");
	    		UIManager.put("FileChooser.helpButtonText", "Hulp");
	    		UIManager.put("FileChooser.helpButtonToolTipText", "Bestandskiezer Help");
	    		UIManager.put("FileChooser.homeFolderAccessibleName", "Thuis");
	    		UIManager.put("FileChooser.homeFolderToolTipText", "Thuis");
	    		UIManager.put("FileChooser.listViewButtonAccessibleName", "Lijst");
	    		UIManager.put("FileChooser.listViewButtonToolTipText", "Lijst");
	    		UIManager.put("FileChooser.lookInLabelText", "Kijk in: ");
	    		UIManager.put("FileChooser.newFolderAccessibleName", "Nieuwe map");
	    		UIManager.put("FileChooser.newFolderErrorText", "Fout bij het maken van een nieuwe map");
	    		UIManager.put("FileChooser.newFolderToolTipText", "Nieuwe map maken");
	    		UIManager.put("FileChooser.openButtonText", "Open");
	    		UIManager.put("FileChooser.openButtonToolTipText", "Geselecteerd bestand openen");
	    		UIManager.put("FileChooser.openDialogTitleText", "Open");
	    		UIManager.put("FileChooser.saveButtonText", "Sla");
	    		UIManager.put("FileChooser.saveButtonToolTipText", "Geselecteerd bestand opslaan");
	    		UIManager.put("FileChooser.saveDialogTitleText", "Sla");
	    		UIManager.put("FileChooser.saveInLabelText", "Opslaan in: ");
	    		UIManager.put("FileChooser.updateButtonText", "Update");
	    		UIManager.put("FileChooser.updateButtonToolTipText", "Lijst met telefoonboeken bijwerken");
	    		UIManager.put("FileChooser.upFolderToolTipText", "Eén niveau omhoog");
	            break;	        	
	        case "no":			// Norwegian
	        	UIManager.put("OptionPane.cancelButtonText", "Avbryt");
	    		UIManager.put("OptionPane.noButtonText", "Nei");
	    		UIManager.put("OptionPane.okButtonText", "OK");
	    		UIManager.put("OptionPane.yesButtonText", "Ja");
	    		
	    		UIManager.put("ColorChooser.cancelText", "Avbryt");
	    		UIManager.put("ColorChooser.okText", "OK");
	    		UIManager.put("ColorChooser.previewText", "Forh�ndsvisning");
	    		UIManager.put("ColorChooser.resetText", "Nullstille");
	    		UIManager.put("ColorChooser.swatchesRecentText", "Nylig:");
	    		UIManager.put("ColorChooser.swatchesNameText", "Fargepr�ver");
	    		
	    		UIManager.put("FileChooser.cancelButtonText", "Avbryt");
	    		UIManager.put("FileChooser.cancelButtonToolTipText", "Dialog for avbryt filvalg");
	    		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Detaljer");
	    		UIManager.put("FileChooser.detailsViewButtonToolTipText", "Detaljer");
	    		UIManager.put("FileChooser.directoryDescriptionText", "Katalog");
	    		UIManager.put("FileChooser.directoryOpenButtonText", "�pen");
	    		UIManager.put("FileChooser.directoryOpenButtonToolTipText", "�pne valgt katalog");
	    		UIManager.put("FileChooser.fileDescriptionText", "Generisk fil");
	    		UIManager.put("FileChooser.fileNameLabelText", "Filnavn: ");
	    		UIManager.put("FileChooser.filesOfTypeLabelText", "Filer av typen: ");
	    		UIManager.put("FileChooser.helpButtonText", "Hjelp");
	    		UIManager.put("FileChooser.helpButtonToolTipText", "FileChooser Hjelp");
	    		UIManager.put("FileChooser.homeFolderAccessibleName", "Hjem");
	    		UIManager.put("FileChooser.homeFolderToolTipText", "Hjem");
	    		UIManager.put("FileChooser.listViewButtonAccessibleName", "Liste");
	    		UIManager.put("FileChooser.listViewButtonToolTipText", "Liste");
	    		UIManager.put("FileChooser.lookInLabelText", "Se inn: ");
	    		UIManager.put("FileChooser.newFolderAccessibleName", "Ny mappe");
	    		UIManager.put("FileChooser.newFolderErrorText", "Feil ved oppretting av ny mappe");
	    		UIManager.put("FileChooser.newFolderToolTipText", "Opprett ny mappe");
	    		UIManager.put("FileChooser.openButtonText", "�pen");
	    		UIManager.put("FileChooser.openButtonToolTipText", "�pne valgt fil");
	    		UIManager.put("FileChooser.openDialogTitleText", "�pen");
	    		UIManager.put("FileChooser.saveButtonText", "Lagre");
	    		UIManager.put("FileChooser.saveButtonToolTipText", "Lagre valgt fil");
	    		UIManager.put("FileChooser.saveDialogTitleText", "Lagre");
	    		UIManager.put("FileChooser.saveInLabelText", "Lagre inn: ");
	    		UIManager.put("FileChooser.updateButtonText", "Oppdater");
	    		UIManager.put("FileChooser.updateButtonToolTipText", "Oppdater katalogoppf�ringen");
	    		UIManager.put("FileChooser.upFolderToolTipText", "Opp ett niv�");
	    		break;
		}	
	}
}

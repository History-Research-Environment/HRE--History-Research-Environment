# Sample Code HRE 00002

This folder contains an HRE starter pack, constructed within the Eclipse RCP
IDE, as this is the intended development environment for HRE.

It consists of sample code (and its required images) to setup and display the
HRE Menu system and a simple ‘About HRE’ splash screen in Eclipse. Instructions on how to setup an Eclipse environment are included in this folder, and this code may be used to test whether the environment has been setup correctly.

It does conform to the HRE specifications for the menu layout for the planned
v0.1 to v0.6 development, but is otherwise considered to be **THROW-AWAY code
ONLY**. It was built to test the use of Eclipse as a suitable platform, NOT as a
base for HRE development. It does not conform to any coding standard other than
Eclipse code generation rules.

It does **NOT** observe the module naming laid out in the HRE specification
documents.

It does **NOT** conform to OSGi requirements.

It does **NOT** save changes the user may make to the window layout and/or position (as HRE specifications require).

It does **NOT** consider the accessibility specifications of HRE, which require all features to be accessible by keyboard action or by single mouse clicks (as double-clicking cannot be performed consistently by MS or Parkinson sufferers). This accessibility aspect also applies to the control of color, contrast and the size and choice of fonts, none of which are changeable in this code.

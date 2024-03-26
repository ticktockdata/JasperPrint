NOTICE:  This project has been moved to SourceHut  https://git.sr.ht/~ticktockdata/JasperPrint

JasperPrint is a java report printing utility that utilizes TIBCO(R) Jasper Reports library.
Developed by Joseph A Miller / Tick Tock Data (javajoe@programmer.net) over an extended period of time (ditched the first attempt and started over in 2019).
Copyright:  Jasper Reports library and this application are open source, under the accomplying GNU LESSER GENERAL PUBLIC LICENSE Version 3.

Jasper Reports is a powerful and efficient printing solution for database applications, but the author found it very verbose and programming intensive to use.  This utility application provides a 'wrapper' around Jasper Reports that (hopefully) makes it easier to use.

This project was developed in NetBeans IDE 8.2 with the source code level at Java 8.  It uses a standard Ant build script, and has all resources embedded, no additional outside resources required.
For the purpose of running as a stand-alone application the entire project can be packaged into a single jar by executing the custom Ant target 'package-single-jar'.

JasperPrint can function in 2 different modes, either as an embedded printing solution in your Java application, or as a stand-alone print server that enables printing by command line or TCP socket connections.

As a Print Server JasperPrint creates a TCP Server Socket which can accept print jobs by any application connecting to that socket, or by sending the PRINT commands via JasperPrint.jar.  Documentation is pretty sketchy, but required information should be obtainable by querying the application from the command line:  java -jar JasperPrint.jar HELP

The original purpose of this application was to enable printing Jasper Reports (.jrxml source files) from office suites, OpenOffice / LibreOffice / MS Office.  This is accomplished by using the Shell command (in Basic / VBA) to send commands to the JasperPrint.jar.
Some GUI additions (PrintButton, ReportsPanel) were made to enable use as a printing solution inside a java desktop application.

The current solution for creating Jasper Reports (.jrxml files) is TIBCO Jaspersoft(R) Studio.  The author has a personal dislike for this application (based on Eclipse) and does most report development using the older iReport 5.6.0 Jasper Report Designer (based on NetBeans Platform).  Using iReport requires Java 7 or 6, and has has a few bugs that need to be worked around.
Bug 1: Refuses to save report.  Try using Ctrl+S, or clicking the save button multiple times.  It appears to have something to do with what has the focus, try clicking to set focus on the internal pane before saving.
Bug 2: If doing a save, or more often a Save As, occasionally it shows a "Sax Parser Error / Premature End Of File" exception.  If this happens the actual jrxml file is a blank.  You do NOT want to close the report at this point or it will be gone!  Trigger the report's modified status by selecting any visual element, move it one tick in any direction then back, then save again.  This always takes care of it for me, but I did lose some work before I discovered the problem.

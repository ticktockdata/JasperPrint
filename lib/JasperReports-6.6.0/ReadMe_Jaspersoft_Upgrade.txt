I attempted to update system to JasperReports 6.6.0 n August 17, 2018.  JAM

Results were: 
Works on Java 8.
Does Not work on Java 7 (unsupported major.minor version).
Printing works on Java 9, but there are other CA issues with 9.
	(fails to open form with: class not found: javax/xml/bind/JAXBException.
	This is in rt.jar (java 8).  Possibly need to update Spring Framework.)

Reverted changes.  If we ever update to Java 8 then do this.

The following dependancies were REMOVED:
	jasperreports-6.0.3.jar
	jasperreports-fonts-6.0.3.jar
	groovy-all-1.6.0.jar
	poi-3.10.1.jar
	JFreeChart-1.0.13/jfreechart-1.0.13.jar
	JFreeChart-1.0.13/jcommon-1.0.16.jar
	

The following dependancies were ADDED:
	jasperreports-6.6.0.jar
	jasperreports-fonts-6.6.0.jar
	jasperreports-functions-6.6.0.jar
	groovy-all_2.4.5.jar
	itext-2.1.7.js3.jar
	joda-time_2.9.9.jar
	poi_3.15/poi-3.15.jar
	poi_3.15/commons-collections4-4.1.jar
	poi_3.15/poi-ooxml-3.15.jar
	poi_3.15/poi-ooxml-schemas-3.15.jar
	JFreeChart-1.0.19/jfreechart-1.0.19.jar
	JFreeChart-1.0.19/jcommon-1.0.23.jar

>> Note that commons-collections-3.2.1.jar was NOT removed, it is completely different from v4.

All these are now in JasperReports-6.6.0 directory.
	This directory also contains doc.zip (javadoc for jasperreports 6.6.0)
	and an Other directory that contains jars that >might< be needed.

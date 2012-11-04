all: javac
	jar cfm BoxOffice.jar BoxOffice.manifest *.class

# don't use the zip directive - it's quite obsolete!
zip:
	zip -q BoxOffice.src.zip *.java *.php *.html *.css *.js *.xls

javac: *.java
	javac *.java

clean:
	rm -f *.class
	rm -f BoxOffice.jar
 

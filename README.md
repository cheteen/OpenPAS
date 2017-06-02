OpenPAS:
========================================
Welcome to OpenPAS.

See the project site for an overview of PAS and OpenPAS:

http://openpas.steweche.co.uk/


Running OpenPAS:
========================================

OpenPAS needs Java 1.8 or higher installed. You can either obtain OpenPAS from a release or build it yourself (see below).

Download the current release: https://github.com/cheteen/OpenPAS/releases/download/1.0/OpenPAS-1.0.jar

You can run OpenPAS in interactive mode as follows:

$ java -jar OpenPAS-1.0.jar

You can also execute an OPS file like this by passing the file as the first parameter, e.g.:

$ java -jar OpenPAS-1.0.jar scripts/heavy_rain.ops


Building OpenPAS:
========================================

Build dependencies:
- Java 1.8 (or higher)
- Ant 1.9 (or likely earlier is also fine)

You can use the following command at the project root to compile all needed files and build a jar file in OpenPAS:

$ ant

This should create a .jar file as follows:
./build/jar/OpenPAS-1.0.jar

(or OpenPAS-X.X.jar for a later major version.)

If all you want is to build the project (but not a jar file) then the following would suffice:

$ ant autobuild.build

Documentation
========================================

The project web site is a good reference to start with.

The Javadoc for the code is in the project tree under /docs (currently manually updated).

A version of the Javadoc is also hosted here (also manually updated):

http://openpas.steweche.co.uk/docs/

OpenPAS:
========================================
Welcome to OpenPAS.

See the project site for an overview of PAS and OpenPAS:

http://openpas.steweche.co.uk/


Building OpenPAS:
========================================

Build and runtime dependencies:
- Java runtime 1.8 (or higher)
- Ant 1.9 (or likely earlier is also fine)

You can use the following command at the project root to compile all needed files and build a jar file in OpenPAS:

$ ant

This should create a .jar file as follows:
./build/jar/OpenPAS-1.0.jar

(or OpenPAS-X.X.jar for a later major version.)

If all you want is to build the project (but not a jar file) then the following would suffice:

$ ant autobuild.build

I personally develop OpenPAS in Eclipse so it contains Eclipsisms such as warning suppressions, and includes build files auto-generated using
Eclipse, but you shouldn't need to use Eclipse to build/run/develop in OpenPAS.


Running OpenPAS:
========================================

OpenPAS needs Java 1.8 or higher installed. You can run OpenPAS in interactive mode as follows:

$ java -jar ./build/jar/OpenPAS-1.0.jar

You can also execute an OPS file like this by passing the file as the first parameter, e.g.:

$ java -jar ./build/jar/OpenPAS-1.0.jar docs/heavy_rain.ops

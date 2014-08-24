CodonPDX
========

CodonPDX is a senior capstone project of the computer science department at Portland State University.
It is a search engine to find host organisms for a virus.  It is comprised of two primary parts.  The first
is in a sister repo, codonpdx-python, and is where the back end processing and loading of databases
happens.  The second portion is the web application, which this repo is related to.

The CodonPDX web application is a Java application using Java Servlets.  It should be usable in
any application server, but has only been tested in Tomcat 7.  There is a Gradle file to download
the dependencies, but none of the dependencies are Tomcat specific.  The servlet interface dependencies
are listed as Provided in the Gradle file, so it will default to use what the application server uses.
The Gradle file will create a .WAR file when ran.

Configuration
------
Modify the sample-mq.properties, sample-database.properties, sample-tomcat.properties to include the proper
data for your postgresql, rabbitmq, and shared folder.  Rename the files to remove the sample.

Building
------
Run:
gradle build

to build the .WAR file

Deploying
------
Drop .WAR file in your application servers application directory and follow your application servers
instructions for deploying.

Servlets
------
There are two primary servlets, though most of the lifting happens in the CodonPDX servlet.  It would be
best to refactor this out to different servlets.  The other servlet is the ResultsViewServlet, which just
serves the needed HTML file based on the number of arguments in the URL.

RabbitMQ
------
Because the system the application was developed for is distributed, we need to be able to pass commands
to the python backend.  We use RabbitMQ as the message queue, and so there are some classes used for
writing to, reading from queue's.

ResponseParser
------
We had issues using the doPost handler to unpack the contents of the Request object.  Because we could
see the data from the AJAX call in the web request being sent, but we couldn't use the built in Request
object methods to extract it, we hacked together a parser to read the data from the Post.

Front-end
------
The front end is 4 primary pages.  Index.html, about.html, resultsView.html, compareTwo.html.  Index is
the primary page where a user submits a file or a plain codon sequence.  resultsView is meant as a 1-many
view of the comparisons, which is displayed as a large table.  It gets the data from an ajax call to
an endpoint.  compareTwo is meant as a 1-1..1-n comparison, where n is some arbitrary number (not tested
for values larger than n=6).  View ability will decrease as n increases.
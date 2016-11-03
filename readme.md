# Restservices-Server

A simple rest server providing the basics of http rest methods.
Inspired by these articles:  
 
* <http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api>  
* <http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api>

The whole thing is still under construction...
I just wanted to get it out of the door.
Tests are running.

## Contents
<!-- process:toc(2) -->
* [Quickstart](#Quickstart)
* [Technologies used](#Technologies used)
* ...

<a name="Quickstart"></a>
## Quickstart
* Have java installed and call on terminal:  

  ``java -version``  
  
  Did you see meaningful output?
* Get the latest release:  

  TODO: reference to dowload:``curl -o ``

* Start Server with filebased json storage:  
  
  ``java -jar SimpleRestServices-<Version>.jar -port.http 7777``

* Call HTTP-Post to insert data:  

  ``curl -X POST -D"{text:"some text"}" http://localhost:7777/testcollections ``

* Call HTTP-List to get data:  
  
  ``curl -X POST http://localhost:777/testcollections``
  
* stop service  
TODO fill



## Features
* REST Server for Json Documents
* Fileupload and Download.
* Delivering webcontent
* Admin GUI

## Configurations
### Server configurations
### Application configurations
* http port
* https port 

##Http-Methods

* POST
* GET - single document
* GET - several documents
* POST
* PUT
* PATCH  
``curl -X PATCH -H"Content-Type: application/json" -d'someValue' address/<collection>/<id>/<property>``
 
 Basically values are being set as they are parseable to. _true_ is boolean. _-12.43_ will be a number. Anything else is a String.
 
 Setting a value definetly as a text by quoting it: 
 ``curl -X PATCH -H"Content-Type: application/json" -d'"someValue"' address/<collection>/<id>/<property>``
 
 Setting an empty text is done by
 ``curl -X PATCH -H"Content-Type: application/json" -D'' address/<collection>/<id>/<property>``
 or
 ```curl -X PATCH -H"Content-Type: application/json" -D'""' address/<collection>/<id>/<property>``

 Setting a null value without defining the -d flag:
 ```curl -X PATCH -H"Content-Type: application/json" address/<collection>/<id>/<property>`` 

## Collections

The name of a collection always ends with an 's'. This indicates the plural of having more than one entry in a collection.


##Http-Method META
* Ping  

 You can run a simple ping which returns a simple pong with a timestamp 

 ``curl -X META <address:port>/ping``

 which will answer:   
 
 ```{"pong":"yyyy-MM-ddTHH:mm:ss.SSS"}```

* Status

 delivers a status of the system.
 
 ``curl -X META <address:port>/status``
 
* Logs

 shows log files

 curl -X META <address:port>/status

## Assumptions made, Predefined things
* property names of user json documents must never start with a $ or _
* system identifier of a document is **$id** 


### Customs-Methods Java

TODO: convert $id, $created $updated into custom methods

### Customs-Methods Javascript

TODO: connect rhino/nashorn

### Customs-Methods Skala

##Differences in implementation from viniaysahni and mwaysolutions
* meta parameters
 
 Meta parameters are prefixed with **_** (underscore). In turn that means that no property of a user json must start with an underscore.

* next item

## Known issues and TODOs
* Make reading inputstream entity from request check on amount bytes read in order to prevent big data sending
* Implement reading Json by direct json formal check in order to prevent bad json upload.
* Copy a default log4j.xml on pure startup into the basedir
No issues known yet.  
But please do report them.


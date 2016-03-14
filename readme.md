# Restservices-Server

## Contents
<!-- process:toc(2) -->
* [Quickstart](#Quickstart)
* Implementation inspired and followed by
* [Technologies used](#Technologies used)
* ...

<a name="Quickstart"></a>
## Quickstart
* Store the latest release
* java -jar RestServices-X.jar

Call HTTP-Post to insert data:  
```
curl -X POST -D"" http://localhost:8080/testcollections 
```

Call HTTP-List to load data:  
```
curl -X POST http://localhost:8080/testcollections 
```

## Quickstart

* prerequesites  
TODO fill
* download  
* start java jar  
TODO fill  
* post via curl  
TODO fill
* get  
TODO fill  
* stop service  
TODO fill

## Implementation inspired and followed by

* <http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api>
* <http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api>

## Features
* REST Server which in base configuration handles JSONs on the file system
* Fileupload and Download. Integrated into the Json
* Delivering webcontent
* 

## <a name="Technologies used"></a>Technologies used
but not needed for running

* Java
* Jetty
* Jersey



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

## Collection inspectation
* List Collections

##Http-Method META
* Ping  

 You can run a simple ping which returns a simple pong with a timestamp 

 ``curl -X STATUS <address:port>/ping``

 which will answer:   
 
 ```{"pong":"yyyy-MM-ddTHH:mm:ss.SSS"}```

* Status

 delivers a status of the system.
 
* Logs


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
No issues known yet.  
But please do report them.


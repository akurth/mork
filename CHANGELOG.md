## Changelog 

### 1.1.5 (pending)

* compile for Java 8
* update parent pom
* update sushi 2.8.14 to sushi 3.1.6 and inline 1.1.1


### 1.1.4 (2014-06-12)

* Fix multi-threading problem: Visits cannot be shared, I have to use new instances.

### 1.1.3 (2013-08-06)

* Fix operand decoding when loading wide iinc and ret instructions.


### 1.1.2 (2013-02-25)

* Update sushi 2.8.2 to 2.8.7 and graph 1.0.0. Changed all PrintStreams to PrintWriters.
* Changed PrintStreams to PrintWriters in all public methods.


### 1.1.1 (2012-11-09)

* Update sushi 2.8.0 to 2.8.1.


### 1.1.0 (2012-10-18)

* Changed License from LGPL to Apache 2.0.
* Renamed net.sf.beezle.mork to net.oneandone.mork.
* Compile for Java 7.
* Update Sushi 2.6.x to 2.8.1.
* Improved ErrorHandler:
   * added ExceptionErrorHandler to report exceptions immediately
   * added a close() method for deferred exceptions. Mapper will never return null now.
   * IOException no longer reported to the handler - they are throw immediately now.


### 1.0.1 (2012-03-08)

* Parallel lr(k) generation.
* Improved conflict output.
* Classfile api tweaks.


### 1.0 (2012-01-29)

* Support interfaces when merging attribute types.
* Generate Class Files with version 50 (= Java 6).
* Conflict resolution.
* Dump LALR(1) generation - generate LR(k) instead.
* Dump xml support, it never worked.
* Changed terminology: grammar files are syntax files now.
* Changed References to syntax files in Mapper file from "grm" to "syntax".
* Changed naming convention for syntax files: capitalized with extension .syntax.
* Changed naming convention for mapper files: base name like syntax file, extension .mapper.


### 0.7 (2011-11-28)

* Moved Project into Beezle: copied source code in svn; changed package and groupId to net.oneandone.mork.
* Changed examples into self-contained Maven projects.
* Manual updates.
* Update Sushi 2.2 to 2.6.1.


## 0.6.4 (2011-09-01)

* Fixed ClassRef.resolve for Array types.


### 0.6.3 (2011-03-09)

* Improved repository and class file loading performance.


### 0.6.2 (2011-03-02)

* Improved class file loading performance.


### 0.6.1 (2011-02-23)

# Update Sushi 2.0.0 to 2.2.0

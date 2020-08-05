# Frost

!Under Construction!

FrozenMPQ2 is a multi-platform MPQ editing library written in pure Java. The MPQ editing logic was implemented based on Storm's implementation notes.

# Features

- MPQ Opening
- Add your own Listfile
- Extract files
- Extract all files
- Import files
- Rebuild archive

# Usage guide

1: Import FrozenMPQ2 as a dependency to your project using Maven (or equivalent)

2: Create an MPQ object from a file or String path:
```$xslt
IMpq frostMPQ = new Mpq(inFile);
```

3: Use the operations on the MPQ, for example:
```$xslt
        if(frostMPQ.fileExists(fileName)) {
            frostMPQ.extractFile(fileName);
        } else {
            System.out.println("File does not exist.");
        }
```

The internal details of the MPQ are deliberately exposed through public getters and setters. The intention of this is to allow developers who are familiar with the MPQ format to be able to directly examine and modify the data as they want, in order to support advanced use cases.

If you do not want to worry about the internal details, simply use the interface (IMpq) which does not have these details. Using the IMpq interface will prevent you from shooting yourself in the foot.

# Operations provided

TODO.

# Command line interface

FMPQ2 comes with a simple command line interface for use as a demo, or to interface with other languages that can't import maven project.

In order to use, simply type a command like this:
```$xslt
java -jar FrozenMPQ2 (TODO)
```

# Settings

This utility comes with many settings you can use to customize how the tool works.

1: Log Settings.

You can set the log level to several different values.

DEBUG:  Debug logging will log the maximum amount of data possible. This is a lot of logging, and as such I recommend it only for debugging purposes.

INFO:   Informational logging will log details about the opened MPQ and about the operation performed.

WARN:   Warn level will only print logs if the tool thinks something is going wrong.

ERROR:  Error level will only print logs if something goes wrong.

NONE:   No logging provided.


The log level by default is WARN.

You can set the logger of the MPQ to any PrintStream. By default, it uses System.out.

2: MPQ Open Settings

You can choose how sensitive FMPQ2 will be to MPQ errors. Available settings are:

ANY:        Any error will terminate program.

CRITICAL:   Only terminates on critical problems

PERMISSIVE: Never terminates

By default, set to ANY.

3: Compression Settings

On re-writing MPQ, you can decide how much (if at all) it'll compress files.

MAX:        Compresses the data as many times as possible

DEFLATE:    Uses only Deflate algorithm.

NONE:       Does not compress files.

By default uses DEFLATE.

4: Security Settings

You can decide whether or not FMPQ2 will encrypt the files in the File Data. Available settings are:

ENCRYPTION_ENABLED:    Encrypts all files.

ENCRYPTION_DISABLED:   Does not encrypt any files


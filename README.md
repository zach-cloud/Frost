# Frost

Frost is a multi-platform MPQ editing tool which can:

- Extract

- Import

- Fix MPQ corruption

And more!

This tool is meant to mimic the implementation of Storm as much as possible. 
There is no such thing as a "read only" MPQ for Frost. This tool can bypass 
PG protection and Spazzler and import files to corrupted maps.

This tool is very much in an alpha state. It will be improved going forward, but
it is usable right now. Please report all bugs through either GitHub issues or the
release post. Feel free to make any suggestions or ask questions as well!

# Features

- MPQ Opening
- Add your own Listfile
- Extract files
- Extract all files
- Import files
- Rebuild archive

# User Guide

You need Java installed to use this.

Double click run.bat (rename to run.sh for Linux based machines, such as Macs)

A command line interface will open. and prompt you to select a file. Write the name of the file
you want to import. This file should be in the same location as the Frost jar file.

After the file opens up successfully, it will give you a menu of operations to choose.
This is what each of the operations do:

1: Extract.

The extract action will prompt you for a file name to extract. This should be the exact
file name as it appears in the MPQ Archive. It will tell you if it doesn't exist.

If the file exists, it will extract it to the "out" folder.

2: List

This will provide you a list of all known filenames in the archive.

It first attempts to use the internal listfile. If that listfile doesn't exist or isn't
complete, then it will try to use an external listfile. If there is a file named "listfile.txt",
it will use that. If there isn't, it will prompt you for a listfile to use. If you don't enter a
listfile, it'll continue without it.

After finding all known files, it will give you a report with all file names.

3: ExtractAllKnown

This will extract all known files to the "out" directory.

It will not extract files that it does not know the name of.

It first attempts to use the internal listfile. If that listfile doesn't exist or isn't
complete, then it will try to use an external listfile. If there is a file named "listfile.txt",
it will use that. If there isn't, it will prompt you for a listfile to use. If you don't enter a
listfile, it'll continue without it.

4: Import

Imports a file from disk into the archive. The file should exist in the same directory
as the Frost jar, and should be the same filename as you want to put into the archive.

If the file already exists, it will delete it first.

If the hash table is filled up, it will refuse to add new files.

It adds the file as a single entity, uncompressed.

5: Delete

Deletes the file name from the archive.

6: Save

Saves the MPQ archive with modifications. It will prompt you for the name to save as
(will not overwrite by default). Before saving, the tool will delete the (attributes) file

Note that the mpq WILL NOT BE SAVED if you don't use this command. If you import a file and
don't press save after, it will not be imported!

7: Quit

Quits the program.

# Limitations

Currently, the tool is limited in the following ways. I plan to improve it going forward.

- We do not support all compression types. It's possible the tool will find a compression
type that it doesn't know how to decompress, in which case it will refuse to extract
the file. These types were not found during my testing.

- All files are only added as an uncompressed, single entity. This means the map size
will increase when you replace files.

- It's not the most user friendly tool yet. It is command-line only and lacks very
good error reporting.

- The Extended Hash Table, Extended Block Table, Weak Signature, and Strong Signature are
not supported. I have not seen any maps using these fields in my tests.

# Programmer guide

NOT AVAILABLE YET THROUGH MAVEN!
Coming sooon!

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

Coming soon!!

# Settings

NOTE: Most of these options don't do anything yet.

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


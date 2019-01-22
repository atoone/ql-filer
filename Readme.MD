# QL Filer

### What?

This is a utility to deal with three common archive formats for Sinclair
QL media. It can parse, list and extract files from .mdv, .zip and .win files. 

It displays and exports QL-specific file header information, including 
headers stored in .zip files generated with the QL zip utility.

The utility can be run on PC, Mac or Linux machines using the Java 
runtime. 

### How?

The utility runs on the command line and accepts a series of commands 
to open a media file, list files on it and extract or (hex) dump them
to the native disk.

```
> java -jar qlfiler.jar <command list>
```

For example, to get detailed information on a microdrive cartridge (.mdv) file:

```
> java -jar qlfiler.jar open rockfall.mdv info

Cartridge: Andy 179/255
File: BOOT                 Type: 0 Length: 22         Dataspace: 0        Updated 2018-12-21 18:51 Acc/Ver/FID: 0/1/0
File: RockFall             Type: 0 Length: 215        Dataspace: 0        Updated 2018-12-21 18:51 Acc/Ver/FID: 0/1/0
File: RockFall_code        Type: 0 Length: 36700      Dataspace: 0        Updated 2018-12-21 18:52 Acc/Ver/FID: 0/1/0
```

### Header info

QL Files natively have 64 bytes of header information. In addition 
to the file name and length, this includes the file type and in
the case of executable files the dataspace. 

When files are extracted from QL media, a second file is written that
contains a machine readable dump of the header. This may be useful 
when moving files from a non-QL filesystem to QL.

This is an example of the file header dump for an executable file:

```
0000 0ece 0001 0000 0800 000e 0804 000a ...?............
4472 7643 686b 5f65 7865 0000 0000 0000 DrvChk_exe......
0000 0000 0000 0000 0000 0000 0000 0000 ................
0000 0000 40e9 ab15 0001 0003 4108 f958 ....@??.....A.?X
----------
File: DrvChk_exe           Type: 1 Length: 3726       Dataspace: 2048     Updated 1995-07-06 18:25 Acc/Ver/FID: 0/1/3
```
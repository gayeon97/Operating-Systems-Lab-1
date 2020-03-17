Two-Pass-Linker
===============
**Operating Systems, Spring 2020**

*Author: Gayeon Park*

For this program, we are implementing a two-pass linker in Java. The target machine is word addressable with a maximum memory of 300 words, each consisting of 4 decimal digits. 
We are trying to replicate a linker in real life.

The program accepts standard input.

To execute the LinkerTwoPass program using the contents of a text file, use the '<' character to redirect any input text file into standard input.

Type the below instruction into the Terminal to compile the LinkerTwoPass.java program.
### Compiling
```
javac LinkerTwoPass.java
```

### Running
To execute the code, type following:
```
java LinkerTwoPass <inputTextFileName.txt

```

For example, let's say input text files and LinkerTwoPass.java file are located in different folders, "Lab1" and "src", respectively, and "src" folder is inside of the Lab1 folder. If you want to execute the LinkerTwoPass.java file with the contents of input03.txt file (located inside of the Lab1 folder), do the following: 
### Compiling
```
javac LinkerTwoPass.java
```

### Running
To execute the code, type following:
```
java LinkerTwoPass <../input03.txt

```

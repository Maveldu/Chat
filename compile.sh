#!/bin/bash
cd src
javac *.java
jar cfm chat manifest.mf *.class
java -jar chat


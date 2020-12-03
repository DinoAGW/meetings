# meetings

[![Build Status](https://travis-ci.org/DinoAGW/meetings.svg?branch=master)](https://travis-ci.org/github/DinoAGW/meetings "Meetings Travis-CI integration build")

## Installationsanleitung

1. in Eclipse unter der git Ansicht das Repository Clonen
2. `$ sudo apt install mariadb-server`
3. `$ sudo mysql_secure_installation`
4. root Passwort in properties.txt Datei im Homeverzeichnis eintragen.
Beispiel:
```
#default Kommentar
#Mon Jun 29 16:24:30 CEST 2020
password=<passwort>
```
5. `$ sudo mariadb`
6. `> CREATE DATABASE meetings;`
7. `> USE meetings;`
8. `> CREATE TABLE ueberordnungen (ID VARCHAR(20), URL VARCHAR (200), Status INT );`
9. `> CREATE TABLE abstracts (Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), Status INT );`
10. `> GRANT ALL ON *.* TO 'root'@'localhost' IDENTIFIED BY 'password' WITH GRANT OPTION;`
11. `> FLUSH PRIVILEGES;`

## Explanation

### Structure of Meetings in https://www.egms.de/static/en/meetings/index.htm

* There is a List of congresses. For every congress, you first see the so called 'Überordnung' of a congress.
* The german word Überordnung refers to the Overview of the congress, for example: https://www.egms.de/dynamic/en/meetings/maf2020/index.htm .
* In this example, there are three Pages which have information about the congress, found at 'MAF 2020' (landing page), 'Contact' and 'Imprint'.
* All information to a 'Kongressüberordnung' (in that case, the content of these three websites) should be downloaded, processed and transformed to a PDF
* Every Überordnung have a set of Sessionlists (In the one given example, its just one here: https://www.egms.de/dynamic/en/meetings/maf2020/index.htm?main=1 ).
* Every Session in the Sessionlist has a List of Abstracts in it, for example: https://www.egms.de/static/en/meetings/maf2020/20maf01.shtml
* For every Abstract, the website should be downloaded, processed and transformed to a PDF

### Database purpose

The Database is used to keep track of work, which is done for the congress and their abstracts respectively.

Status of congresslinks:
| Status | Meaning |
| ---: | --- |
| 10 | LinkCrawl took the URL from the GMS Landingpage and inserted it into the table |
| 30 | UeberordnungDownload downloaded all Websites for the congress Overview and performed a merge of some of them (and maintain connection to their prerequisites) |
| 50 | Convert transformed the merge html with their prerequisites to a PDF via iText |

Status of abstracts:
| Status | Meaning |
| ---: | --- |
| 10 | UeberordnungDownload found the link in a Sessionlist of the congress overview and inserted it into the table |
| 30 | AbstractDownload downloaded the website of the abstract with all of their prerequisites on harddisk  |
| 50 | AbstractConvert converted the html with their prerequisites to PDF vie iText |

### Folder and File Overview

"/" is written here as the root folder for the project to work in (not the harddisk root Folder). Example given for [this congress](https://www.egms.de/dynamic/en/meetings/maf2020/index.htm)
| Folder | Purpose |
| ---: | :--- |
| /landingPage/{de\en}/ | The target for the first langindPage of GMS Meetings ([GMS landingPage](https://www.egms.de/static/en/meetings/index.htm)) |
| /Ueberordnungen/ | The folder where the landing Page of all Meetings will be saved into subfolders |
| /Ueberordnungen/{kurzID}{languageSpec}/ | The Subfolder for the langindPage of one particular Meeting Overview, for Example /Ueberordnungen/maf2020/ (German Version) or /Ueberordnungen/maf2020_en/ (english Version) |
| /Ueberordnungen/{kurzID}{languageSpec}/{i}/ | The Subfolders for additional information from the links, listed in owner_links, like 0/ for https://www.egms.de/static/en/meetings/maf2020/contact.htm or 1/ for https://www.egms.de/static/en/meetings/maf2020/imprint.htm |
| /Ueberordnungen/{kurzID}{languageSpec}/merge/ | In here, the websites belonging to a Meeting Overview will be merged together |
| /Ueberordnungen/{kurzID}{languageSpec}/merge/content/target.html | The file, where all content of there html Files are merged into (with linkings to its prerequisites) |
| /Ueberordnungen/{kurzID}{languageSpec}/abstractlist{i}/ | Subfolder where one Abstractlist of the Sessionlist is downloaded to |
| /Ueberordnungen/{kurzID}{languageSpec}/{kurzID}{languageSpec}.pdf | The PDF Version of the congress Overview |
| /Abstracts/{kurzID}{languageSpec}/{Ab_ID}/ | The Destination for the Abstract Download, for example /Ueberordnungen/kongresse/maf2020_en/20maf01/ for https://www.egms.de/static/en/meetings/maf2020/20maf01.shtml |
| /Abstracts/{kurzID}{languageSpec}/{Ab_ID}/merge/ | just like the merge folder in the congress overview part |
| /Abstracts/{kurzID}{languageSpec}/{Ab_ID}/merge/content/target.html | just like the congress overview part |
| /Abstracts/{kurzID}{languageSpec}/{Ab_ID}/{Ab_ID}{languageSpec}.pdf | The PDF Version of the Abstract |

### Modules Overview

| Module | Purpose |
| ---: | :--- |
| linkCrawl.LinkCrawl | Downloads the LandingPage of the egms.de congresses and writes found congresslinks into database |
| linkDownload.UeberordnungDownload | Downloads a Congress Overview on harddisk and prepares it to be transformed to PDF |
| html2pdf.UeberordnungConvert | Transforms Congressoverview to PDF |
| linkDownload.AbstractDownload | Downloads a Congress Abstract on harddisk and prepares it to be transformed to PDF |
| html2pdf.AbstractConvert | Transforms Congressabstracts to PDF |
| utilities.Clean | Cleans the working directory up |
| testing | Calling Method for the other Modules |

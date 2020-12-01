# meetings

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

The german word Überordnung refers to the Overview of the congress, for example: https://www.egms.de/dynamic/en/meetings/maf2020/index.htm

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

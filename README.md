# meetings

## Installationsanleitung

1) in Eclipse unter der git Ansicht das Repository Clonen
2) $ sudo apt install mariadb-server
3) $ sudo mysql_secure_installation
4) root Passwort in properties.txt Datei im Homeverzeichnis eintragen.
Beispiel:
```
#default Kommentar
#Mon Jun 29 16:24:30 CEST 2020
password=<passwort>
```
5) $ sudo mariadb
6) > CREATE DATABASE meetings;
7) > USE meetings;
8) > CREATE TABLE ueberordnungen (ID VARCHAR(20), URL VARCHAR (200), Status INT );
9) > CREATE TABLE abstracts (Ue_ID VARCHAR(20), Ab_ID VARCHAR(20), URL VARCHAR (200), Status INT );
10) > GRANT ALL ON *.* TO 'root'@'localhost' IDENTIFIED BY 'password' WITH GRANT OPTION;
11) > FLUSH PRIVILEGES;

### Notizen, was zu RNP-03 gemacht werden muss

* spezifizieren, ob nur TCP oder auch UDP benutzt wird(/ werden darf)
* Welche Komponenten?
* Welche Infos werden erwartet, z.B.
  * Hostname
  * Portnummer
  * In welcher Reihenfolge?
  * In welchem Format?
* Architektur
* Implementationsdetails
  * Char-Set angeben für einheitlichen Output
* ergänzen:
  * separates Fenster an der Seite, dass eingeloggte User anzeigt
  * `synchronized` einbauen für Zugriffsmethoden auf ArrayList bei Impl master (-> bei alternative_impl auch checken)
    * Bsp bei `clientList` -> mit Monitor für ClientList _auch_ synchronisieren
* Format von Message selber spezifizieren
* bei Impl master:
  * TCP-Server
    * __Achtung__: Hier greifen potentiell 2 oder mehr Threads auf 1 Socket zu (`outToClient`)
      * nur `writeToClient` nutzen zum Kapseln & mit `synchronized`

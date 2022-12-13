# Verteilte_Systeme_Labor3_v2



#1 Ausführung der Aufgabe
 - In den OrdnerVSP3_STDMA gehen
 - 4 terminals aufmachen
 - Im ersten Terminal "ifconfig" ausführen um die Adressen zu bekommen
 - Im zweiten Terminal den sniffer starten mit "./STDMAsniffer <mcast address> <listen port>"
 - Im dritten Terminal die Station A starten mit 
 "./startStations.sh <interface name> <multicast address> <eingangsport> <von> <zu> <Stadion> <offset>
 - Im vierten Terminal die Station B starten mit 
 "./startStations.sh <interface name> <multicast address> <eingangsport> <von> <zu> <Stadion> <offset>
  
  Wenn man ein Beispiel für die eingaben der start stationen haben möchte kann man auch nur "./startStations.sh" eingeben. Dann steht im Terminal eine beispielzeile wie das aussehen kann.

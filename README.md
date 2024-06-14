# ColorAnalyzer
Progetto per l'esame del corso di Embedded System Programming 23/24, corso di Laurea in Ingegneria Informatica, Università degli Studi di Padova. 

Studente: &nbsp;&nbsp;&nbsp;&nbsp; Fulvio Bruzzese &nbsp; | &nbsp; 2032572

### _Obiettivo_  
Implementazione di un'applicazione per sistemi Android capace di fornire il colore medio rilavato dalla fotocamera in tempo reale e salvarlo, in modo tale da poter visualizzare successivamente i dati registrati negli ultimi 5 minuti.

### _Descrizione_  
Al primo avvio dell'applicazione, verranno richiesti all'utente i <ins>permessi necessari</ins> per il corretto funzionamento dell'app:
- permessi della CAMERA  

Successivamente, compariranno sullo schermo:
- un'**antemprima** della fotocamera (in background)
- una **TextView** che mostra i dati rilevati in tempo reale
- un **bottone** per visualizzare i dati registrati


## Requisiti
-> Android 10+  
-> API level 29

### Dipendenze fondamentali
* **CameraX**: per l'utilizzo della fotocamera
* **Room**: per salvare i dati in un database


## Note
Lo sviluppo dell'applicazione è stato guidato dalla volontà di cercare di mantenere il più possibile una fedeltà alle versioni più recenti delle librerie di sviluppo per Android.  

All'interno dei file è possibile trovare commenti dettagliati sul funzionamento della maggior parte dei blocchi di codice; nonostante ciò, qui di seguito vengono riportate le funzioni principali, ritrovabili in **MainActivity.kt**:  
    
    startCamera()
esegue la build degli "use case" fondamentali (Preview, ImageAnalysis) da legare alla fotocamera

    showArchive()
visualizza a schermo i dati registrati nel database negli ultimi 5 minuti;  
per modificare tale l'intervallo di tempo, occorre cambiare il valore della variabile "TIME_LIMIT" nel _companion object_ della MainActivity

    allPermissionsGranted()
verifica che tutti i permessi necessari al funzionamento dell'applicazione sia stati concessi

    requirePermissions()
richiede i permessi necessari
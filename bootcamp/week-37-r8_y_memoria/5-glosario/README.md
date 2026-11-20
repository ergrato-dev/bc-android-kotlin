# Glosario — Semana 37

| Término | Definición |
| --- | --- |
| **`consumerProguardFiles`** | Las reglas de una biblioteca de Android que viajan en su AAR (`proguard.txt`) y aplica R8 en la app que la usa |
| **`configuration.txt`** | Todas las reglas que ha usado R8, juntas, con el archivo de donde sale cada bloque |
| **`DetectLeaksAfterTestSuccess`** | Regla de JUnit de `leakcanary-android-instrumentation`: tras cada test que pasa, busca objetos retenidos y falla con el informe |
| **Fuga de memoria** | Un objeto que debería haberse liberado y sigue apuntado por algo que vive más, como una actividad destruida en una lista de nivel superior |
| **`isMinifyEnabled`** | Enciende R8 en un tipo de build: quitar, optimizar y renombrar el código |
| **`isShrinkResources`** | Quita los recursos que nadie usa, después de R8. Necesita `isMinifyEnabled` |
| **`-keep`** | Regla que conserva una clase, con su nombre, y los miembros que diga |
| **`-keepclassmembers`** | Regla que conserva los miembros que diga de una clase, si la clase queda. La clase puede renombrarse |
| **LeakCanary** | Biblioteca de Square que, en debug, vigila lo que se destruye, vuelca el heap si sigue en memoria y enseña el camino que lo retiene |
| **`mapping.txt`** | El nombre de antes y el de después de cada clase, campo y método, con sus líneas. Hace falta para leer las trazas de una versión |
| **Modo completo** (*full mode*) | El modo de R8 por defecto desde AGP 8: conserva solo lo que dicen las reglas |
| **`pg_map_id`** | El identificador de un mapping. La traza de la release lo lleva donde iba el archivo fuente (`r8-map-id-…`) |
| **PSS** (*proportional set size*) | La memoria de un proceso, con la compartida repartida entre quien la comparte. La de `TOTAL PSS` en `dumpsys meminfo` |
| **R8** | El compilador de la release: quita lo que no se usa, optimiza, renombra y convierte a dex. Lee reglas en el formato de ProGuard |
| **`retrace`** | La herramienta que devuelve una traza ofuscada a sus nombres y líneas con el mapping |
| **`seeds.txt`** | Lo que las reglas mandan conservar |
| **StrictMode** | La herramienta de Android que avisa de lo que la app no debería hacer: disco o red en el hilo principal, objetos sin cerrar |
| **`usage.txt`** | Lo que R8 ha quitado |

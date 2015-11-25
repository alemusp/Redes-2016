Rosas Bocanegra José Ricardo
Lemus Pablo Arturo

/******* Modificado de la primera parte de la tarea *******/

La clase principal es la clase Main.java

Se agregaron 2 clases, Emisor.java y Receptor.java los cuales envían y reciben el archivo. Ambas extienden de Thread.java

Además se modificó la clase ComunicationChannel.java que hacía que siempre fallara el enviar una parte del mensaje y que la cola de entrada 
nunca mandara los paquetes a la cola de salida.

Se crean 4 colas, 2 de entrada y 2 de salida y después se les pasa como argumento a un objeto ComunicationChannel además de un entero 
que indica la probabilidad de que el enviar una parte del archivo, falle. Además se crea un emisor y un receptor e inicia los hilos de ambos.  

El emisor pide un nombre de archivo, manda el nombre, después manda los paquetes con las partes del archivo, el checksum y reenvía los paquetes 
que el emisor indica que no llegaron correctamente, no termina de ejecutarse en caso de que el receptor aún necesite recibir partes del archivo que
llegaron incorrectamente.

El receptor recibe el nombre del archivo y los paquetes. Revisa el checksum del paquete y si es incorrecto avisa por medio de la cola de Warnings
que el paquete llegó incorrectamente. Si es correcto sólo lo guarda. Finaliza cuando todos los paquetes del archivo fueron recibidos y escribe
el archivo con "copia_<nombre original>" como nombre del archivo.

/*********************************************************************************************************************************************
**********************************************************************************************************************************************
**********************************************************************************************************************************************/

Se agregaron las características de la tarea 2.
La clase Receptor.java envía su llave para cifrar con RSA. Y la clase Emisor.java ahora pierde mensajes con probabilidad 0.3 que puede cambiarse
en la misma clase.
El receptor se da cuenta cuando se perdieron los mensajes y los pide de nuevo. Para esto, revisa que todos los mensajes s envían en orden y si se 
envían en desorden, supone que no llegaron correctamete. Para el último mensaje, si no lo recibe después de un tiempo lo pide de nuevo porque 
supone que no llegó. 
# libreriaD
Programa de una librería corriendo en un sistema distribuido<br>
Las computadoras deben tener un nombre que empieza con el letra "E" seguido de un número de forma incremental (No aplica para el servidor de tiempo).<br>
PE: Computadora 1: "E1", computadora 2: "E2", ... computadora n: "En".<br>
<br>
Para echarlo a andar.<br>
En caso de ser la primera vez que se va a ejecutar.<br>
  1.-Verificar que en la carpeta del proyecto no se encuentre el archivo "siguientes.obj" en ninguno de los equipos a levantar, en        caso contrario eliminarlo.<br>
  2.-En el archivo "AlgoritmoBerkeley.java" modificar "IPSERV" por la ip del servidor de tiempo.<br>
  3.-Cargar "reloj.sql" en el SGBD.<br>
  4.-Cargar "liberia.sql" en el SGBD.<br>
  5.-En el archivo "MuestraImage.java" modificar la variable "con" y "con2" por la parte de usuario y contraseña de la SGBD.<br>
  6.-Lanzar el archivo "ServidorTiempo.java".<br>
  7.-Lanzar el archivo "MuestraImage.java", esperar hasta que se muestre una ventana.<br>
  8.-Repetir el paso 7 n veces en las diferentes PC (El programa está configurado para un max de 4, igual se puede cambiar). Cabe destacar que el nodo principal siempre debe estar encendido.<br>
  9.-Ejecutar "RelojUsuario.java", seleccionar un número de reloj<br>
  10.-LISTO!<br>
En caso de caerse un nodo<br>
  1.-No borrar el archivo "siguientes.obj".<br>
  2.-Ejecutar "MuestraImage.java".<br>

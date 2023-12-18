La carpeta tablasQ contiene las tablas Q de cada mapa, hay que colocarla en la carpeta principal de minidungeons

Q Learning uso lo siguiente en el entrenamiento:
    epsilon:                1.0 a 0.1, disminuyendo linealmente por episodio.
    factor de descuento:    0.9
    learning rate:          0.5
    episodios:              10000, a excepcion del mapa 4 con 30000
    acciones maximas:       300

    recompensa por ganar:   1
    recompensa por morir:   -1
    en otro caso:           -0.01



Otra cosa, tal vez muy importante, la lectura del mapa en distintos computadores es distinta,
en uno se lee correctamente y en otro se agrega una columna extra de camino a la derecha.
Tambien algunas funciones tiran error.
# VivaVida_G3
Proyecto para Aplicaciones Móviles 2020/21.
Autores: Gonzalo Guerrero Torija, María Sanz Gómez y Álvaro Asensio Calvo.

VivaVida es una aplicación orientada a mejorar los hábitos saludables de una manera sencilla, intuitiva y fácil de seguir. La aplicación se centra en dos piedras angulares;
el deporte y la alimentación. Esta aplicación está orientada para cualquier tipo de público que busque mejorar su salud y alcanzar nuestro objetivo: Vivir la vida a través del cuidado personal y de invertir tiempo en uno mismo.

A nivel de desarrollo, la aplicación integra 4 funcionalidades básicas que se muestran en el menú principal:

## Tracking de actividad

Esta funcionalidad consiste en la capacidad de realizar un seguimiento de running, senderismo, ciclismo y natación. Incluye un mapa donde se muestra
la ubicación actual, el recorrido realizado (track GPS), así como una serie de estadísticas: calorías quemadas, tiempo de actividad realizado, distancia recorrida, y datos
metereológicos. Para alcanzar dichas funcionalidades, a continuación se describe como se implementarían:

- Se requieren de servicios de localización, para ello se va a usar la API de Google Maps.
- Para el acceso a datos metereológicos, se requiere una conexión al servidor de [AEMET (Open Data)](https://opendata.aemet.es/centrodedescargas/inicio), del que se va a obtener
la temeperatura y estados durante la actividad realizada.
- Para las estadísticas, sabiendo el tiempo realizado, la distancia, y el peso del usuario, con simples calculos matemáticos, se puede realizar una estimación de las calorias quemadas. La distacia se recogería en relación a los datos proporcionados por el GPS y tiempo, teniendo que integrar un cronómetro para medir dicho tiempo.
- Estos datos se recogén tras finalizar la actividad y son guardados en una base de datos SQLite, para que el usuario tenga un resgistro de dicha actividad.

## Registro de macros

Se pretende con esta función recoger la ingesta total de macros del usuario permitiendo saber que alimentos ingerir, objetivos cumplidos/a completar. 

- Para obtener los macros, hay dos posibilidades, que el usuario determine las kcal y el grupo de macronutrientes, o escanear el producto a través de la API de [Open Food Facts](https://world.openfoodfacts.org/data), el usuario ingresa la cantidad en gramos, y automáticamente se obtiene, a través del desglose de los datos aportados por la API, los macronutrientes, ayudándonos con el lector de código de barras Zxing.
- Para visualizar dichos datos, se emplean 3 barras de progreso(o más, dependiendo de los tipos de nutrientes que el usuario quiera trackear) las cuales se irán llenando a medida que el usuario ingresa datos de los alimentos.
- Entendemos que el tracking sea simplemente diario, por tanto no es necesario que se almacenen los datos en una base de datos si no en las mismas preferencias de la aplicación. Son datos de poco peso (integers) que se restauran diariamente. No habría resgistro de anteriores días.
- Los objetivos de cantidades de macros y calorías pueden ser modificados por el usuario.

## Datos de Usuario

En esta pestaña el usurio puede editar sus datos (peso, edad, sexo, altura) así como definir las cantidades de macronutrientes que debe completar de acuerdo a su dieta o necesidad. Estas cantidades se verían reflejadas en la segunda funcionalidad, modificando los valores de la barra de progreso. Como son datos de poco peso, formarían parte de las preferencias. Por otra parte, en esta misma pantalla se mostraría un log de las actividades físicas realizadas, a modo de feed.

## Registro de actividades

Permite al usuario consultar todas las actividades que ha realizado, mostrándose las estadísticas de cada una así como el recorrido realizado en un mapa. Se realiza madiante el acceso a una base de datos de tipo SQLite.

## Servicio de mapas

Para poder el servicio de Mapas de Google, introducir tu Google Maps API key en res/values/google_maps_api.xml


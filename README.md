# Market-Simulation
Simulacion de mercado de transaccion de bienes

Pensado para:

* Enseñar oferta y demanda
* Enseñar temas como utilidad marginal y tasa marginal de sustitucion


#Running

The database pre-configured is an h2, so you just have to:


        $ sbt run

#Using

	Para probar todas las cosas, ver el archivo de routes.
	Ej:
	$http://localhost:9000/takeOffer/1/1/2
	Usuario 1 toma oferta 2 del mercado 1
	$http://localhost:9000/getOffers/1
	Obtener las ofertas del mercado 1
	$http://localhost:9000/getProducts/1/1
	Obtener los productos del usuario uno del mercado 1

#TODO

Tests

#Credits

To make this template, I just mixed the play scala template with play slick.


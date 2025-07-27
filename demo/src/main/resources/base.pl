% base.pl - Base de conocimientos de aves de Costa Rica

% Estructura: ave(Nombre, Familia, Reino, Filo, ColorPrincipal, Peso, Alimentacion, Habitat)

ave(trogon_elegante, trogonidae, animalia, chordata, rojo, grande, frugivoro, volcan).
ave(colibri_esmeralda, trochilidae, animalia, chordata, azul, pequeno, nectarivoro, playa).
ave(tucan_amarillo, ramphastidae, animalia, chordata, amarillo, grande, frugivoro, bosque).
ave(quetzal_resplandeciente, trogonidae, animalia, chordata, verde, grande, frugivoro, bosque).
ave(zanate, icteridae, animalia, chordata, negro, mediano, omnivoro, ciudad).
ave(pajaro_carpintero, picidae, animalia, chordata, rojo, mediano, insectivoro, bosque).
ave(garza_blanca, ardeidae, animalia, chordata, blanco, grande, piscivoro, pantano).

% Regla de clasificación simplificada (4 argumentos)
clasificar(Color, Zona, Tamano, Resultado) :-
    ave(Resultado, _, _, _, Color, Tamano, _, _),
    zona_valida(Resultado, Zona).

% Mapeo de aves a zonas donde se pueden encontrar (simplificado)
zona_valida(trogon_elegante, norte).
zona_valida(colibri_esmeralda, sur).
zona_valida(tucan_amarillo, centro).
zona_valida(quetzal_resplandeciente, norte).
zona_valida(zanate, centro).
zona_valida(pajaro_carpintero, norte).
zona_valida(garza_blanca, sur).

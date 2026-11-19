package equipo2.logica.patron.factory;

import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;
import equipo2.logica.ValidadorEnvio;

// Patron Factory: centraliza la creacion y validacion de objetos Envio
public class EnvioFactory {

    // Segun el tipo crea un EnvioLocal o un EnvioInterurbano
    public static Envio crearEnvio(String tipo, String codigo, String destino, double pesoKg, double datoEspecifico) {
        // Valida los datos comunes
        ValidadorEnvio.validarTipo(tipo);
        ValidadorEnvio.validarCodigo(codigo);
        ValidadorEnvio.validarDestino(destino);
        ValidadorEnvio.validarPesoKg(pesoKg);

        String tipoNorm = tipo.trim().toUpperCase();
        String codigoNorm = codigo.trim();
        String destinoNorm = destino.trim();

        // Segun el tipo, valida el dato especifico e instancia la clase correcta
        if (tipoNorm.equals("LOCAL")) {
            ValidadorEnvio.validarTarifaPorKg(datoEspecifico);
            return new EnvioLocal(codigoNorm, destinoNorm, pesoKg, datoEspecifico);
        } else if (tipoNorm.equals("INTERURBANO")) {
            ValidadorEnvio.validarDistanciaKm(datoEspecifico);
            return new EnvioInterurbano(codigoNorm, destinoNorm, pesoKg, datoEspecifico);
        } else {
            throw new IllegalArgumentException("Tipo de envio desconocido: " + tipo);
        }
    }
}

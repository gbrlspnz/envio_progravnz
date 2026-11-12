package equipo2.logica.patron.factory;

import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;
import equipo2.logica.ValidadorEnvio;

/**
 * Patrón de diseño Creacional: Factory Method / Simple Factory.
 * Centraliza la creación y validación de las instancias de Envio (EnvioLocal y EnvioInterurbano).
 */
public class EnvioFactory {

    public static Envio crearEnvio(String tipo, String codigo, String destino, double pesoKg, double datoEspecifico) {
        ValidadorEnvio.validarTipo(tipo);
        ValidadorEnvio.validarCodigo(codigo);
        ValidadorEnvio.validarDestino(destino);
        ValidadorEnvio.validarPesoKg(pesoKg);

        String tipoNorm = tipo.trim().toUpperCase();
        String codigoNorm = codigo.trim();
        String destinoNorm = destino.trim();

        if (tipoNorm.equals("LOCAL")) {
            ValidadorEnvio.validarTarifaPorKg(datoEspecifico);
            return new EnvioLocal(codigoNorm, destinoNorm, pesoKg, datoEspecifico);
        } else if (tipoNorm.equals("INTERURBANO")) {
            ValidadorEnvio.validarDistanciaKm(datoEspecifico);
            return new EnvioInterurbano(codigoNorm, destinoNorm, pesoKg, datoEspecifico);
        } else {
            throw new IllegalArgumentException("Tipo de envío desconocido: " + tipo);
        }
    }
}

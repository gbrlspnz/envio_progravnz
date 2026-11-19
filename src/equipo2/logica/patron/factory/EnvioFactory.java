package equipo2.logica.patron.factory;

import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;
import equipo2.logica.ValidadorEnvio;

/**
 * Fábrica de objetos {@link Envio} que implementa el <b>patrón de diseño Factory Method</b>.
 *
 * <p><b>¿Por qué usamos el patrón Factory?</b><br>
 * El patrón Factory centraliza la creación de objetos en un único lugar.
 * Esto significa que, si en el futuro se agrega un nuevo tipo de envío,
 * solo hay que modificar esta clase. El resto del sistema no necesita cambiar.</p>
 *
 * <p>Esta clase también integra las <b>validaciones</b> antes de crear el objeto,
 * garantizando que nunca se instancie un envío con datos inválidos.</p>
 *
 * <p>Flujo de creación:</p>
 * <ol>
 *   <li>Validar el tipo de envío (LOCAL o INTERURBANO).</li>
 *   <li>Validar los campos comunes (código, destino, peso).</li>
 *   <li>Validar el dato específico según el tipo (tarifa o distancia).</li>
 *   <li>Instanciar y retornar el objeto concreto correspondiente.</li>
 * </ol>
 *
 * <p>Arquitectura: capa <b>Lógica</b> — patrón <b>Factory</b>.</p>
 */
public class EnvioFactory {

    // ============================================================
    // PATRÓN FACTORY: método de creación centralizado
    // ============================================================

    /**
     * Crea y retorna un objeto {@link Envio} del tipo adecuado según los parámetros recibidos.
     *
     * <p>El parámetro {@code datoEspecifico} tiene un significado diferente según el tipo:</p>
     * <ul>
     *   <li>Si {@code tipo} es {@code "LOCAL"} → es la <b>tarifa por kg</b> en pesos.</li>
     *   <li>Si {@code tipo} es {@code "INTERURBANO"} → es la <b>distancia en km</b>.</li>
     * </ul>
     *
     * @param tipo           tipo de envío: "LOCAL" o "INTERURBANO" (insensible a mayúsculas)
     * @param codigo         código único del envío
     * @param destino        destino del paquete
     * @param pesoKg         peso del paquete en kilogramos
     * @param datoEspecifico tarifa por kg (LOCAL) o distancia en km (INTERURBANO)
     * @return un objeto {@link EnvioLocal} o {@link EnvioInterurbano} ya validado
     * @throws IllegalArgumentException si algún parámetro no cumple las reglas de negocio
     */
    public static Envio crearEnvio(String tipo, String codigo, String destino,
                                   double pesoKg, double datoEspecifico) {

        // Paso 1: validamos los campos comunes a todos los tipos de envío
        ValidadorEnvio.validarTipo(tipo);
        ValidadorEnvio.validarCodigo(codigo);
        ValidadorEnvio.validarDestino(destino);
        ValidadorEnvio.validarPesoKg(pesoKg);

        // Normalizamos las cadenas para evitar inconsistencias por espacios o mayúsculas
        String tipoNorm    = tipo.trim().toUpperCase();
        String codigoNorm  = codigo.trim();
        String destinoNorm = destino.trim();

        // Paso 2: según el tipo, validamos el dato específico y creamos el objeto concreto
        if (tipoNorm.equals("LOCAL")) {
            // Para envíos locales: el dato específico es la tarifa por kg
            ValidadorEnvio.validarTarifaPorKg(datoEspecifico);
            return new EnvioLocal(codigoNorm, destinoNorm, pesoKg, datoEspecifico);

        } else if (tipoNorm.equals("INTERURBANO")) {
            // Para envíos interurbanos: el dato específico es la distancia en km
            ValidadorEnvio.validarDistanciaKm(datoEspecifico);
            return new EnvioInterurbano(codigoNorm, destinoNorm, pesoKg, datoEspecifico);

        } else {
            // Este caso no debería ocurrir tras validarTipo(), pero lo cubrimos por seguridad
            throw new IllegalArgumentException("Tipo de envío desconocido: " + tipo);
        }
    }
}

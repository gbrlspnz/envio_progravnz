package equipo2.logica;

/**
 * Clase utilitaria que centraliza todas las <b>validaciones de negocio</b> del sistema.
 *
 * <p>Todos sus métodos son estáticos (sin instancia) y lanzan una
 * {@link IllegalArgumentException} con un mensaje descriptivo si el valor
 * ingresado no cumple las reglas establecidas por el enunciado:</p>
 * <ul>
 *   <li>Peso: entre 0.1 y 50 kg (ambos incluidos)</li>
 *   <li>Tarifa por kg: mayor a 0</li>
 *   <li>Distancia: entre 1 y 2.000 km (ambos incluidos)</li>
 *   <li>Texto: no vacío, sin punto y coma (";") ni saltos de línea</li>
 * </ul>
 *
 * <p>Esta clase protege tanto la lógica como la persistencia: cualquier intento
 * de crear un envío con datos inválidos es rechazado antes de que el objeto exista.</p>
 *
 * <p>Arquitectura: capa <b>Lógica / Controlador</b> del patrón MVC.</p>
 */
public class ValidadorEnvio {

    // ============================================================
    // Validaciones de campos de texto
    // ============================================================

    /**
     * Valida que un campo de texto no esté vacío y no contenga caracteres
     * que rompan el formato del archivo de persistencia (";", "\n", "\r").
     *
     * <p>El separador ";" está prohibido porque es el delimitador del archivo .txt.
     * Los saltos de línea se prohíben porque cada envío ocupa exactamente una línea.</p>
     *
     * @param valor       el texto a validar
     * @param nombreCampo nombre descriptivo del campo (para el mensaje de error)
     * @throws IllegalArgumentException si el valor es nulo, vacío o contiene caracteres inválidos
     */
    public static void validarCampoTexto(String valor, String nombreCampo) {
        // Verificar que el campo no sea nulo ni vacío
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacío.");
        }
        // Verificar que no contenga el separador del archivo ni saltos de línea
        if (valor.contains(";") || valor.contains("\n") || valor.contains("\r")) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreCampo + "' no puede contener ';' ni saltos de línea.");
        }
    }

    /**
     * Valida que el código del envío no esté vacío y sea un texto simple válido.
     *
     * @param codigo el código a validar
     * @throws IllegalArgumentException si el código no cumple las reglas de texto
     */
    public static void validarCodigo(String codigo) {
        validarCampoTexto(codigo, "código");
    }

    /**
     * Valida que el destino del envío no esté vacío y sea un texto válido.
     *
     * @param destino el destino a validar
     * @throws IllegalArgumentException si el destino no cumple las reglas de texto
     */
    public static void validarDestino(String destino) {
        validarCampoTexto(destino, "destino");
    }

    // ============================================================
    // Validaciones de campos numéricos
    // ============================================================

    /**
     * Valida que el peso esté dentro del rango permitido: entre 0.1 y 50 kg.
     *
     * @param pesoKg el peso en kilogramos a validar
     * @throws IllegalArgumentException si el peso no es un número finito o está fuera de rango
     */
    public static void validarPesoKg(double pesoKg) {
        // Rechazar valores no numéricos o infinitos (pueden venir de conversiones erróneas)
        if (Double.isNaN(pesoKg) || Double.isInfinite(pesoKg)) {
            throw new IllegalArgumentException("El peso debe ser un número finito válido.");
        }
        // Regla de negocio: peso entre 0.1 kg y 50 kg
        if (pesoKg < 0.1 || pesoKg > 50.0) {
            throw new IllegalArgumentException(
                    "El peso debe estar entre 0.1 y 50 kg (ambos incluidos). Recibido: " + pesoKg);
        }
    }

    /**
     * Valida que la tarifa por kg sea un valor positivo (mayor que cero).
     * Aplica únicamente a los envíos de tipo {@code LOCAL}.
     *
     * @param tarifaPorKg la tarifa en pesos por kilogramo
     * @throws IllegalArgumentException si la tarifa no es finita o no es positiva
     */
    public static void validarTarifaPorKg(double tarifaPorKg) {
        // Rechazar valores no numéricos o infinitos
        if (Double.isNaN(tarifaPorKg) || Double.isInfinite(tarifaPorKg)) {
            throw new IllegalArgumentException("La tarifa por kg debe ser un número finito válido.");
        }
        // La tarifa debe ser estrictamente positiva
        if (tarifaPorKg <= 0.0) {
            throw new IllegalArgumentException(
                    "La tarifa por kg debe ser mayor que 0. Recibido: " + tarifaPorKg);
        }
    }

    /**
     * Valida que la distancia esté dentro del rango permitido: entre 1 y 2.000 km.
     * Aplica únicamente a los envíos de tipo {@code INTERURBANO}.
     *
     * @param distanciaKm la distancia en kilómetros a validar
     * @throws IllegalArgumentException si la distancia no es finita o está fuera de rango
     */
    public static void validarDistanciaKm(double distanciaKm) {
        // Rechazar valores no numéricos o infinitos
        if (Double.isNaN(distanciaKm) || Double.isInfinite(distanciaKm)) {
            throw new IllegalArgumentException("La distancia debe ser un número finito válido.");
        }
        // Regla de negocio: distancia entre 1 km y 2.000 km
        if (distanciaKm < 1.0 || distanciaKm > 2000.0) {
            throw new IllegalArgumentException(
                    "La distancia debe estar entre 1 y 2000 km (ambos incluidos). Recibido: " + distanciaKm);
        }
    }

    /**
     * Valida que el tipo de envío sea uno de los valores aceptados: "LOCAL" o "INTERURBANO".
     *
     * @param tipo el tipo de envío ingresado (puede estar en mayúsculas o minúsculas)
     * @throws IllegalArgumentException si el tipo es nulo o no reconocido
     */
    public static void validarTipo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de envío no puede ser nulo.");
        }
        // Normalizamos a mayúsculas para aceptar variantes como "local" o "Local"
        String tipoNorm = tipo.trim().toUpperCase();
        if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
            throw new IllegalArgumentException(
                    "Tipo desconocido: '" + tipo + "'. Debe ser LOCAL o INTERURBANO.");
        }
    }
}

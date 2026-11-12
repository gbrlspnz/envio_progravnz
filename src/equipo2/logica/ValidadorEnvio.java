package equipo2.logica;

/**
 * Capa de validaciones según los requisitos del ENUNCIADO:
 * - pesoKg entre 0.1 y 50 (ambos incluidos).
 * - tarifaPorKg > 0.
 * - distanciaKm entre 1 y 2000 (ambos incluidos).
 * - No se permiten campos vacíos, punto y coma ni saltos de línea.
 * - Valores numéricos finitos y válidos.
 */
public class ValidadorEnvio {

    public static void validarCampoTexto(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacío.");
        }
        if (valor.contains(";") || valor.contains("\n") || valor.contains("\r")) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede contener ';' ni saltos de línea.");
        }
    }

    public static void validarCodigo(String codigo) {
        validarCampoTexto(codigo, "código");
    }

    public static void validarDestino(String destino) {
        validarCampoTexto(destino, "destino");
    }

    public static void validarPesoKg(double pesoKg) {
        if (Double.isNaN(pesoKg) || Double.isInfinite(pesoKg)) {
            throw new IllegalArgumentException("El peso debe ser un número finito válido.");
        }
        if (pesoKg < 0.1 || pesoKg > 50.0) {
            throw new IllegalArgumentException("El peso debe estar entre 0.1 y 50 kg (ambos incluidos). Recibido: " + pesoKg);
        }
    }

    public static void validarTarifaPorKg(double tarifaPorKg) {
        if (Double.isNaN(tarifaPorKg) || Double.isInfinite(tarifaPorKg)) {
            throw new IllegalArgumentException("La tarifa por kg debe ser un número finito válido.");
        }
        if (tarifaPorKg <= 0.0) {
            throw new IllegalArgumentException("La tarifa por kg debe ser mayor que 0. Recibido: " + tarifaPorKg);
        }
    }

    public static void validarDistanciaKm(double distanciaKm) {
        if (Double.isNaN(distanciaKm) || Double.isInfinite(distanciaKm)) {
            throw new IllegalArgumentException("La distancia debe ser un número finito válido.");
        }
        if (distanciaKm < 1.0 || distanciaKm > 2000.0) {
            throw new IllegalArgumentException("La distancia debe estar entre 1 y 2000 km (ambos incluidos). Recibido: " + distanciaKm);
        }
    }

    public static void validarTipo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de envío no puede ser nulo.");
        }
        String tipoNorm = tipo.trim().toUpperCase();
        if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
            throw new IllegalArgumentException("Tipo desconocido: '" + tipo + "'. Debe ser LOCAL o INTERURBANO.");
        }
    }
}

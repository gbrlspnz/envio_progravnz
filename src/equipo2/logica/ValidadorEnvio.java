package equipo2.logica;

// Validaciones del sistema segun las reglas del enunciado
public class ValidadorEnvio {

    // Evita campos vacios, punto y coma o saltos de linea que rompan el archivo
    public static void validarCampoTexto(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacio.");
        }
        if (valor.contains(";") || valor.contains("\n") || valor.contains("\r")) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede contener ';' ni saltos de linea.");
        }
    }

    public static void validarCodigo(String codigo) {
        validarCampoTexto(codigo, "codigo");
    }

    public static void validarDestino(String destino) {
        validarCampoTexto(destino, "destino");
    }

    // El peso debe estar entre 0.1 y 50 kg
    public static void validarPesoKg(double pesoKg) {
        if (Double.isNaN(pesoKg) || Double.isInfinite(pesoKg)) {
            throw new IllegalArgumentException("El peso debe ser un numero valido.");
        }
        if (pesoKg < 0.1 || pesoKg > 50.0) {
            throw new IllegalArgumentException("El peso debe estar entre 0.1 y 50 kg.");
        }
    }

    // La tarifa por kg debe ser mayor a 0
    public static void validarTarifaPorKg(double tarifaPorKg) {
        if (Double.isNaN(tarifaPorKg) || Double.isInfinite(tarifaPorKg)) {
            throw new IllegalArgumentException("La tarifa debe ser un numero valido.");
        }
        if (tarifaPorKg <= 0.0) {
            throw new IllegalArgumentException("La tarifa por kg debe ser mayor que 0.");
        }
    }

    // La distancia debe estar entre 1 y 2000 km
    public static void validarDistanciaKm(double distanciaKm) {
        if (Double.isNaN(distanciaKm) || Double.isInfinite(distanciaKm)) {
            throw new IllegalArgumentException("La distancia debe ser un numero valido.");
        }
        if (distanciaKm < 1.0 || distanciaKm > 2000.0) {
            throw new IllegalArgumentException("La distancia debe estar entre 1 y 2000 km.");
        }
    }

    // Solo se aceptan tipos LOCAL o INTERURBANO
    public static void validarTipo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de envio no puede ser nulo.");
        }
        String tipoNorm = tipo.trim().toUpperCase();
        if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
            throw new IllegalArgumentException("Tipo desconocido: '" + tipo + "'. Debe ser LOCAL o INTERURBANO.");
        }
    }
}

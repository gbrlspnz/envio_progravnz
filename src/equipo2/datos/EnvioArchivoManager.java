package equipo2.datos;

import equipo2.logica.patron.factory.EnvioFactory;
import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Encargado de la lectura y persistencia del catálogo en formato UTF-8 con delimitador ';'.
 * Formato de cada línea: tipo;codigo;destino;pesoKg;datoEspecifico
 * Cumple los requisitos de manejo de líneas vacías, detección de duplicados e informe de errores por línea.
 */
public class EnvioArchivoManager {

    private static final String SEPARADOR = ";";
    private static final int CAMPOS_ESPERADOS = 5;

    public static class ResultadoCarga {
        private final List<Envio> envios = new ArrayList<>();
        private final List<String> advertencias = new ArrayList<>();

        public List<Envio> getEnvios() {
            return envios;
        }

        public List<String> getAdvertencias() {
            return advertencias;
        }

        public boolean tieneAdvertencias() {
            return !advertencias.isEmpty();
        }
    }

    /**
     * Lee el archivo indicado (UTF-8, separador ';').
     * - Ignora líneas vacías.
     * - Si una línea es inválida o tiene código repetido, registra la advertencia con su número
     *   y continúa con las demás. Ante duplicados conserva el primer registro válido.
     *
     * @throws FileNotFoundException si el archivo no existe.
     * @throws IOException           si ocurre un error de lectura.
     */
    public ResultadoCarga leer(String rutaArchivo) throws IOException {
        File file = new File(rutaArchivo);
        if (!file.exists()) {
            throw new FileNotFoundException("El archivo no existe: " + rutaArchivo);
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Set<String> codigosVistos = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String linea;
            int numLinea = 0;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                // Quitar BOM de UTF-8 si el archivo fue guardado con él (solo puede estar en la 1ª línea)
                if (numLinea == 1 && linea.startsWith("﻿")) {
                    linea = linea.substring(1);
                }

                // Ignorar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Solo se acepta ';' como separador: cualquier otra cosa es error de formato
                if (!linea.contains(SEPARADOR)) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (no contiene el separador ';').");
                    continue;
                }

                String[] partes = linea.split(SEPARADOR, -1);
                if (partes.length != CAMPOS_ESPERADOS) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (se esperaban " + CAMPOS_ESPERADOS
                            + " campos y se encontraron " + partes.length + ").");
                    continue;
                }

                String tipo = partes[0].trim();
                String codigo = partes[1].trim();
                String destino = partes[2].trim();
                String pesoStr = partes[3].trim();
                String datoEspStr = partes[4].trim();

                if (tipo.isEmpty() || codigo.isEmpty() || destino.isEmpty()
                        || pesoStr.isEmpty() || datoEspStr.isEmpty()) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Hay campos vacíos.");
                    continue;
                }

                // Tipo desconocido = error de formato
                String tipoNorm = tipo.toUpperCase();
                if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (tipo desconocido '" + tipo + "').");
                    continue;
                }

                double pesoKg;
                double datoEspecifico;
                try {
                    pesoKg = Double.parseDouble(pesoStr);
                    datoEspecifico = Double.parseDouble(datoEspStr);
                } catch (NumberFormatException e) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Texto en campo numérico ('" + pesoStr + "', '" + datoEspStr + "').");
                    continue;
                }

                // Código duplicado (sin espacios extremos y sin distinguir mayúsculas)
                String codigoKey = codigo.toUpperCase(Locale.ROOT);
                if (codigosVistos.contains(codigoKey)) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Código duplicado ('"
                            + codigo + "'). Se conserva el primer registro válido.");
                    continue;
                }

                try {
                    // Patrón Factory: valida rangos y valores finitos, y crea la clase hija correcta
                    Envio envio = EnvioFactory.crearEnvio(tipo, codigo, destino, pesoKg, datoEspecifico);
                    resultado.getEnvios().add(envio);
                    codigosVistos.add(codigoKey);
                } catch (IllegalArgumentException ex) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": " + ex.getMessage());
                }
            }
        }

        return resultado;
    }

    /**
     * Guarda la colección completa en el archivo indicado (UTF-8, separador ';'),
     * reemplazando su contenido: no se duplican registros y los eliminados no reaparecen.
     * Se escribe primero en un archivo temporal y luego se reemplaza el original,
     * para que un error de escritura no deje el archivo a medio escribir.
     */
    public void guardar(String rutaArchivo, List<Envio> envios) throws IOException {
        Path destino = new File(rutaArchivo).getAbsoluteFile().toPath();
        Path carpeta = destino.getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }

        Path temporal = Files.createTempFile(carpeta, "envios", ".tmp");
        try {
            try (BufferedWriter bw = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                for (Envio e : envios) {
                    bw.write(aLinea(e));
                    bw.newLine();
                }
            }
            try {
                Files.move(temporal, destino,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicoNoSoportado) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    /** Convierte un envío a una línea del archivo: tipo;codigo;destino;pesoKg;datoEspecifico */
    private String aLinea(Envio e) {
        String tipo;
        double datoEspecifico;

        if (e instanceof EnvioLocal) {
            tipo = "LOCAL";
            datoEspecifico = ((EnvioLocal) e).getTarifaPorKg();
        } else if (e instanceof EnvioInterurbano) {
            tipo = "INTERURBANO";
            datoEspecifico = ((EnvioInterurbano) e).getDistanciaKm();
        } else {
            throw new IllegalStateException("Tipo de envío no soportado: " + e.getClass().getSimpleName());
        }

        return String.join(SEPARADOR,
                tipo,
                e.getCodigo().trim(),
                e.getDestino().trim(),
                formatearNumero(e.getPesoKg()),
                formatearNumero(datoEspecifico));
    }

    /**
     * Número sin separador de miles, con punto decimal y sin notación científica.
     * Ej.: 2.5 -> "2.5", 1800.0 -> "1800", 1.0E7 -> "10000000".
     */
    static String formatearNumero(double valor) {
        BigDecimal bd = BigDecimal.valueOf(valor).stripTrailingZeros();
        if (bd.scale() < 0) {
            bd = bd.setScale(0);
        }
        return bd.toPlainString();
    }
}

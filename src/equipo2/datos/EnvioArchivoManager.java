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

// Maneja la lectura y guardado del archivo de texto
public class EnvioArchivoManager {

    private static final String SEPARADOR = ";";
    private static final int CAMPOS_ESPERADOS = 5;

    // Guarda los envios cargados y la lista de advertencias si hubo lineas con error
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

    // Lee el archivo datos.txt linea por linea
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

                // Quita la marca BOM de UTF-8 si existe
                if (numLinea == 1 && linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }

                // Salta lineas vacias
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Verifica que contenga el separador ';'
                if (!linea.contains(SEPARADOR)) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": No contiene el separador ';'.");
                    continue;
                }

                String[] partes = linea.split(SEPARADOR, -1);
                if (partes.length != CAMPOS_ESPERADOS) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": Se esperaban 5 campos y se encontraron " + partes.length + ".");
                    continue;
                }

                String tipo = partes[0].trim();
                String codigo = partes[1].trim();
                String destino = partes[2].trim();
                String pesoStr = partes[3].trim();
                String datoEspStr = partes[4].trim();

                if (tipo.isEmpty() || codigo.isEmpty() || destino.isEmpty()
                        || pesoStr.isEmpty() || datoEspStr.isEmpty()) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": Hay campos vacios.");
                    continue;
                }

                String tipoNorm = tipo.toUpperCase();
                if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": Tipo desconocido '" + tipo + "'.");
                    continue;
                }

                double pesoKg;
                double datoEspecifico;
                try {
                    pesoKg = Double.parseDouble(pesoStr);
                    datoEspecifico = Double.parseDouble(datoEspStr);
                } catch (NumberFormatException e) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": Texto en campo numerico ('" + pesoStr + "', '" + datoEspStr + "').");
                    continue;
                }

                // Evita codigos duplicados (conserva el primero)
                String codigoKey = codigo.toUpperCase(Locale.ROOT);
                if (codigosVistos.contains(codigoKey)) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": Codigo duplicado ('" + codigo + "'). Se conserva el primero.");
                    continue;
                }

                try {
                    // Crea el envio mediante la fabrica
                    Envio envio = EnvioFactory.crearEnvio(tipo, codigo, destino, pesoKg, datoEspecifico);
                    resultado.getEnvios().add(envio);
                    codigosVistos.add(codigoKey);
                } catch (IllegalArgumentException ex) {
                    resultado.getAdvertencias().add("Linea " + numLinea + ": " + ex.getMessage());
                }
            }
        }

        return resultado;
    }

    // Guarda los envios en el archivo (usa un archivo temporal para no perder datos si hay error)
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

    // Convierte un objeto Envio al formato del archivo: tipo;codigo;destino;pesoKg;datoEspecifico
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
            throw new IllegalStateException("Tipo no soportado: " + e.getClass().getSimpleName());
        }

        return String.join(SEPARADOR,
                tipo,
                e.getCodigo().trim(),
                e.getDestino().trim(),
                formatearNumero(e.getPesoKg()),
                formatearNumero(datoEspecifico));
    }

    // Formatea los numeros con punto decimal y sin notacion cientifica
    static String formatearNumero(double valor) {
        BigDecimal bd = BigDecimal.valueOf(valor).stripTrailingZeros();
        if (bd.scale() < 0) {
            bd = bd.setScale(0);
        }
        return bd.toPlainString();
    }
}

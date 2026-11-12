package equipo2.datos;

import equipo2.logica.patron.factory.EnvioFactory;
import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Encargado de la persistencia y lectura de archivos (.txt o .csv) en formato UTF-8 con delimitador ';'.
 * Cumple los requisitos de manejo de líneas vacías, detección de duplicados e informe de errores por línea.
 */
public class EnvioArchivoManager {

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
     * Lee un archivo de texto o csv delimitado por ';' o ',' codificado en UTF-8.
     * Si una línea es inválida o tiene código repetido, se registra la advertencia con el número de línea
     * y se continúa procesando las demás líneas.
     */
    public ResultadoCarga leer(String rutaArchivo) throws IOException {
        File file = new File(rutaArchivo);
        if (!file.exists()) {
            throw new FileNotFoundException("El archivo no existe: " + rutaArchivo);
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Set<String> codigosVistos = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String linea;
            int numLinea = 0;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                String trimmed = linea.trim();

                // Ignorar líneas vacías
                if (trimmed.isEmpty()) {
                    continue;
                }

                // Separador principal: ';' o si no contiene ';', admitir ','
                String[] partes;
                if (trimmed.contains(";")) {
                    partes = trimmed.split(";", -1);
                } else if (trimmed.contains(",")) {
                    partes = trimmed.split(",", -1);
                } else {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Formato incorrecto (no contiene separador ';' ni ','). Contenido: '" + trimmed + "'");
                    continue;
                }

                if (partes.length != 5) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Número incorrecto de columnas (esperadas 5, encontradas " + partes.length + ").");
                    continue;
                }

                String tipo = partes[0].trim();
                String codigo = partes[1].trim();
                String destino = partes[2].trim();
                String pesoStr = partes[3].trim();
                String datoEspStr = partes[4].trim();

                if (codigo.isEmpty() || destino.isEmpty() || pesoStr.isEmpty() || datoEspStr.isEmpty() || tipo.isEmpty()) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Campos obligatorios vacíos.");
                    continue;
                }

                // Verificar código duplicado (insensible a mayúsculas)
                String codigoKey = codigo.toUpperCase();
                if (codigosVistos.contains(codigoKey)) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Código duplicado ignorado ('" + codigo + "'). Se conserva el primer registro válido.");
                    continue;
                }

                double pesoKg;
                double datoEspecifico;
                try {
                    pesoKg = Double.parseDouble(pesoStr);
                    datoEspecifico = Double.parseDouble(datoEspStr);
                } catch (NumberFormatException e) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Error en valores numéricos ('" + pesoStr + "', '" + datoEspStr + "').");
                    continue;
                }

                try {
                    Envio envio = EnvioFactory.crearEnvio(tipo, codigo, destino, pesoKg, datoEspecifico);
                    resultado.getEnvios().add(envio);
                    codigosVistos.add(codigoKey);
                } catch (Exception ex) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Validación rechazada - " + ex.getMessage());
                }
            }
        }

        return resultado;
    }

    /**
     * Guarda la colección de envíos en el archivo indicado en UTF-8 y delimitador ';'.
     * Formato: tipo;codigo;destino;pesoKg;datoEspecifico
     */
    public void guardar(String rutaArchivo, List<Envio> envios) throws IOException {
        File file = new File(rutaArchivo);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (Envio e : envios) {
                String tipo;
                double datoEspecifico;

                if (e instanceof EnvioLocal) {
                    tipo = "LOCAL";
                    datoEspecifico = ((EnvioLocal) e).getTarifaPorKg();
                } else if (e instanceof EnvioInterurbano) {
                    tipo = "INTERURBANO";
                    datoEspecifico = ((EnvioInterurbano) e).getDistanciaKm();
                } else {
                    continue;
                }

                // Formato sin separador de miles y con punto para decimales
                String datoEspFormateado = (datoEspecifico == (long) datoEspecifico)
                        ? String.valueOf((long) datoEspecifico)
                        : String.valueOf(datoEspecifico);

                String pesoFormateado = (e.getPesoKg() == (long) e.getPesoKg())
                        ? String.valueOf((long) e.getPesoKg())
                        : String.valueOf(e.getPesoKg());

                String linea = String.format("%s;%s;%s;%s;%s",
                        tipo,
                        e.getCodigo().trim(),
                        e.getDestino().trim(),
                        pesoFormateado,
                        datoEspFormateado
                );
                bw.write(linea);
                bw.newLine();
            }
        }
    }
}

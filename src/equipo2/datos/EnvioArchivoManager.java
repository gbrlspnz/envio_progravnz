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
 * Clase responsable de la <b>persistencia</b> de envíos en un archivo de texto plano.
 *
 * <p>Implementa la lectura y escritura del archivo {@code datos/datos.txt}, cuyo formato
 * es de campos separados por punto y coma ({@code ;}), con exactamente 5 campos por línea:</p>
 * <pre>
 *   TIPO;CODIGO;DESTINO;PESO_KG;DATO_ESPECIFICO
 * </pre>
 * <p>Ejemplos:</p>
 * <pre>
 *   LOCAL;ENV001;Buenos Aires;3.5;500
 *   INTERURBANO;ENV002;Córdoba;10;750
 * </pre>
 *
 * <p>Características importantes:</p>
 * <ul>
 *   <li>La lectura es <b>tolerante a errores</b>: las líneas mal formadas se omiten
 *       y se acumulan como advertencias en {@link ResultadoCarga}, sin cortar la carga.</li>
 *   <li>La escritura usa un <b>archivo temporal</b> para garantizar que, si el proceso
 *       falla a mitad de camino, el archivo original no quede corrupto.</li>
 *   <li>El encoding usado es <b>UTF-8</b> para soportar caracteres con tilde.</li>
 * </ul>
 *
 * <p>Arquitectura: capa <b>Datos</b> del patrón MVC.</p>
 */
public class EnvioArchivoManager {

    // ============================================================
    // Constantes del formato del archivo
    // ============================================================

    /** Separador de campos usado en el archivo de persistencia. */
    private static final String SEPARADOR = ";";

    /** Número exacto de campos que debe tener cada línea válida del archivo. */
    private static final int CAMPOS_ESPERADOS = 5;

    // ============================================================
    // Clase interna: resultado de la carga
    // ============================================================

    /**
     * Encapsula el resultado de una operación de lectura del archivo.
     *
     * <p>Separa los envíos cargados correctamente de las advertencias generadas
     * por líneas con errores de formato o datos inválidos. Esto permite que la
     * vista informe al usuario exactamente qué líneas fueron omitidas y por qué.</p>
     */
    public static class ResultadoCarga {

        /** Envíos correctamente parseados y validados del archivo. */
        private final List<Envio> envios = new ArrayList<>();

        /** Mensajes descriptivos de las líneas que tuvieron algún problema. */
        private final List<String> advertencias = new ArrayList<>();

        /** @return lista de envíos cargados correctamente */
        public List<Envio> getEnvios() { return envios; }

        /** @return lista de mensajes de advertencia sobre líneas con error */
        public List<String> getAdvertencias() { return advertencias; }

        /** @return {@code true} si hubo al menos una advertencia durante la carga */
        public boolean tieneAdvertencias() { return !advertencias.isEmpty(); }
    }

    // ============================================================
    // Lectura del archivo
    // ============================================================

    /**
     * Lee el archivo de datos y retorna un {@link ResultadoCarga} con los envíos válidos
     * y las advertencias de las líneas que no pudieron procesarse.
     *
     * <p>Proceso de lectura por cada línea:</p>
     * <ol>
     *   <li>Se omiten líneas en blanco.</li>
     *   <li>Se verifica que contenga el separador ";".</li>
     *   <li>Se divide en 5 campos exactos.</li>
     *   <li>Se valida que ningún campo esté vacío.</li>
     *   <li>Se convierte los campos numéricos a {@code double}.</li>
     *   <li>Se detectan y omiten códigos duplicados.</li>
     *   <li>Se crea el envío via {@link EnvioFactory} (que también valida rangos).</li>
     * </ol>
     *
     * @param rutaArchivo ruta absoluta o relativa al archivo .txt a leer
     * @return resultado con la lista de envíos y las advertencias de las líneas con error
     * @throws FileNotFoundException si el archivo no existe en la ruta indicada
     * @throws IOException           si ocurre un error al leer el archivo
     */
    public ResultadoCarga leer(String rutaArchivo) throws IOException {
        File file = new File(rutaArchivo);
        if (!file.exists()) {
            throw new FileNotFoundException("El archivo no existe: " + rutaArchivo);
        }

        ResultadoCarga resultado = new ResultadoCarga();
        // Usamos un Set para detectar códigos duplicados durante la carga
        Set<String> codigosVistos = new HashSet<>();

        // Abrimos el archivo en UTF-8 para soportar tildes y caracteres especiales
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String linea;
            int numLinea = 0;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                // Eliminar BOM (Byte Order Mark) de UTF-8 si está presente en la primera línea
                // El BOM es un carácter invisible que algunos editores agregan al inicio del archivo
                if (numLinea == 1 && linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }

                // Saltear líneas vacías (por ejemplo, líneas en blanco al final del archivo)
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Verificar que la línea tenga al menos un separador ";"
                if (!linea.contains(SEPARADOR)) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (no contiene el separador ';').");
                    continue;
                }

                // Dividir la línea en campos (el -1 evita que se ignoren los campos vacíos al final)
                String[] partes = linea.split(SEPARADOR, -1);
                if (partes.length != CAMPOS_ESPERADOS) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (se esperaban " + CAMPOS_ESPERADOS
                            + " campos y se encontraron " + partes.length + ").");
                    continue;
                }

                // Extraer y limpiar cada campo
                String tipo      = partes[0].trim();
                String codigo    = partes[1].trim();
                String destino   = partes[2].trim();
                String pesoStr   = partes[3].trim();
                String datoEspStr = partes[4].trim();

                // Verificar que ningún campo esté vacío tras quitar espacios
                if (tipo.isEmpty() || codigo.isEmpty() || destino.isEmpty()
                        || pesoStr.isEmpty() || datoEspStr.isEmpty()) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Hay campos vacíos.");
                    continue;
                }

                // Validar que el tipo sea reconocido antes de intentar parsear números
                String tipoNorm = tipo.toUpperCase();
                if (!tipoNorm.equals("LOCAL") && !tipoNorm.equals("INTERURBANO")) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Error de formato (tipo desconocido '" + tipo + "').");
                    continue;
                }

                // Convertir los campos numéricos (peso y dato específico)
                double pesoKg;
                double datoEspecifico;
                try {
                    pesoKg        = Double.parseDouble(pesoStr);
                    datoEspecifico = Double.parseDouble(datoEspStr);
                } catch (NumberFormatException e) {
                    resultado.getAdvertencias().add("Línea " + numLinea
                            + ": Texto en campo numérico ('" + pesoStr + "', '" + datoEspStr + "').");
                    continue;
                }

                // Detectar y omitir códigos duplicados dentro del mismo archivo
                String codigoKey = codigo.toUpperCase(Locale.ROOT);
                if (codigosVistos.contains(codigoKey)) {
                    resultado.getAdvertencias().add("Línea " + numLinea + ": Código duplicado ('"
                            + codigo + "'). Se conserva el primer registro válido.");
                    continue;
                }

                // Crear el envío usando la Factory (que también aplica validaciones de rango)
                try {
                    Envio envio = EnvioFactory.crearEnvio(tipo, codigo, destino, pesoKg, datoEspecifico);
                    resultado.getEnvios().add(envio);
                    codigosVistos.add(codigoKey);
                } catch (IllegalArgumentException ex) {
                    // La Factory detectó datos fuera de rango (ej: peso > 50 kg)
                    resultado.getAdvertencias().add("Línea " + numLinea + ": " + ex.getMessage());
                }
            }
        }

        return resultado;
    }

    // ============================================================
    // Escritura del archivo
    // ============================================================

    /**
     * Guarda la lista de envíos en el archivo indicado, sobreescribiendo su contenido.
     *
     * <p>Estrategia de escritura segura:</p>
     * <ol>
     *   <li>Se escribe primero en un archivo <b>temporal</b> en la misma carpeta.</li>
     *   <li>Una vez terminada la escritura, se reemplaza el archivo destino con el temporal.</li>
     *   <li>Si el proceso falla en cualquier punto, el archivo original no se corrompe.</li>
     * </ol>
     *
     * @param rutaArchivo ruta del archivo donde guardar los envíos
     * @param envios      lista de envíos a persistir
     * @throws IOException si ocurre un error al crear directorios, escribir o mover el archivo
     */
    public void guardar(String rutaArchivo, List<Envio> envios) throws IOException {
        Path destino = new File(rutaArchivo).getAbsoluteFile().toPath();
        Path carpeta = destino.getParent();

        // Crear la carpeta de destino si no existe (ej: primera ejecución del programa)
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }

        // Escribir en archivo temporal para proteger el archivo original durante la escritura
        Path temporal = Files.createTempFile(carpeta, "envios", ".tmp");
        try {
            // Escribir todos los envíos en el archivo temporal, uno por línea
            try (BufferedWriter bw = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                for (Envio e : envios) {
                    bw.write(aLinea(e));
                    bw.newLine();
                }
            }

            // Reemplazar el archivo destino con el temporal (operación atómica si el SO lo soporta)
            try {
                Files.move(temporal, destino,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicoNoSoportado) {
                // Si el movimiento atómico no está disponible, usamos el movimiento estándar
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            // Garantizamos que el archivo temporal siempre se elimine, incluso si hay errores
            Files.deleteIfExists(temporal);
        }
    }

    // ============================================================
    // Método auxiliar: conversión de envío a línea de texto
    // ============================================================

    /**
     * Convierte un objeto {@link Envio} en la línea de texto que se escribirá en el archivo.
     *
     * <p>Formato generado: {@code TIPO;CODIGO;DESTINO;PESO;DATO_ESPECIFICO}</p>
     * <p>El dato específico es la tarifa por kg (LOCAL) o la distancia en km (INTERURBANO).</p>
     *
     * @param e el envío a serializar en texto
     * @return la línea de texto lista para escribir en el archivo
     * @throws IllegalStateException si el tipo de envío no es reconocido
     */
    private String aLinea(Envio e) {
        String tipo;
        double datoEspecifico;

        // Determinamos el tipo y el dato específico usando instanceof (polimorfismo en acción)
        if (e instanceof EnvioLocal) {
            tipo           = "LOCAL";
            datoEspecifico = ((EnvioLocal) e).getTarifaPorKg();
        } else if (e instanceof EnvioInterurbano) {
            tipo           = "INTERURBANO";
            datoEspecifico = ((EnvioInterurbano) e).getDistanciaKm();
        } else {
            // No debería ocurrir, pero lo cubrimos para detectar errores de desarrollo
            throw new IllegalStateException("Tipo de envío no soportado: " + e.getClass().getSimpleName());
        }

        // Unimos los 5 campos con el separador ";"
        return String.join(SEPARADOR,
                tipo,
                e.getCodigo().trim(),
                e.getDestino().trim(),
                formatearNumero(e.getPesoKg()),
                formatearNumero(datoEspecifico));
    }

    /**
     * Formatea un número {@code double} como texto, eliminando ceros decimales innecesarios.
     *
     * <p>Ejemplos: {@code 3.0} → {@code "3"}, {@code 2.5} → {@code "2.5"}.</p>
     * <p>Se usa {@link BigDecimal} para evitar la notación científica (ej: "1.5E3")
     * que {@code Double.toString()} puede generar con valores grandes.</p>
     *
     * @param valor el número a formatear
     * @return representación decimal limpia sin ceros redundantes
     */
    static String formatearNumero(double valor) {
        // stripTrailingZeros elimina los ceros finales (ej: 3.00 → 3)
        BigDecimal bd = BigDecimal.valueOf(valor).stripTrailingZeros();
        // Si la escala es negativa (ej: 1E+3), la fijamos en 0 para usar toPlainString()
        if (bd.scale() < 0) {
            bd = bd.setScale(0);
        }
        // toPlainString evita la notación científica en el archivo de texto
        return bd.toPlainString();
    }
}

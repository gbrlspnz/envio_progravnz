package equipo2.logica;

import equipo2.modelo.Envio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestiona la colección de envíos en memoria durante la ejecución del programa.
 *
 * <p>Esta clase actúa como el <b>Modelo</b> de la arquitectura MVC: mantiene
 * el estado central de la aplicación (la lista de envíos) y ofrece operaciones
 * de alta, baja y consulta sobre esa lista.</p>
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Almacenar los envíos mientras la aplicación está en ejecución.</li>
 *   <li>Garantizar que no existan dos envíos con el mismo código.</li>
 *   <li>Proveer acceso de solo lectura a la lista (via {@link Collections#unmodifiableList})
 *       para evitar modificaciones accidentales desde fuera de esta clase.</li>
 * </ul>
 *
 * <p>La persistencia en disco es responsabilidad de {@link equipo2.datos.EnvioArchivoManager}.</p>
 */
public class GestorEnvios {

    // ============================================================
    // Colección principal de envíos en memoria
    // ============================================================

    /**
     * Lista interna donde se almacenan todos los envíos registrados.
     * El acceso externo se controla mediante getters con distintos niveles de permiso.
     */
    private final List<Envio> envios = new ArrayList<>();

    // ============================================================
    // Acceso a la lista
    // ============================================================

    /**
     * Retorna una vista de <b>solo lectura</b> de la lista de envíos.
     *
     * <p>Usar este método es seguro para iterar y mostrar datos sin riesgo
     * de modificar la colección internamente.</p>
     *
     * @return lista inmutable con todos los envíos registrados
     */
    public List<Envio> getEnvios() {
        // Collections.unmodifiableList impide agregar o eliminar desde afuera
        return Collections.unmodifiableList(envios);
    }

    /**
     * Retorna la lista interna modificable de envíos.
     *
     * <p>Usar con cuidado: permite eliminar envíos individualmente sin pasar
     * por {@link #eliminarEnvio(int)}. Se usa desde la vista para eliminar
     * el objeto exacto seleccionado en la tabla.</p>
     *
     * @return lista interna modificable de envíos
     */
    public List<Envio> getEnviosModificables() {
        return envios;
    }

    /**
     * Reemplaza completamente la lista de envíos en memoria.
     *
     * <p>Este método se usa al cargar un archivo: primero vacía la lista actual
     * y luego carga los nuevos envíos leídos desde el archivo.</p>
     *
     * @param nuevosEnvios lista de envíos a cargar (puede ser {@code null}, lo que equivale a limpiar)
     */
    public void setEnvios(List<Envio> nuevosEnvios) {
        this.envios.clear();
        // Agregamos los envíos solo si la lista no es nula
        if (nuevosEnvios != null) {
            this.envios.addAll(nuevosEnvios);
        }
    }

    // ============================================================
    // Operaciones sobre los envíos
    // ============================================================

    /**
     * Verifica si ya existe un envío registrado con el código dado.
     *
     * <p>La comparación es insensible a mayúsculas/minúsculas y espacios,
     * para evitar duplicados del tipo "ENV001" vs "env001".</p>
     *
     * @param codigo el código a buscar
     * @return {@code true} si ya existe un envío con ese código; {@code false} en caso contrario
     */
    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String normalizado = codigo.trim();
        for (Envio e : envios) {
            // Comparamos ignorando mayúsculas para detectar duplicados como "ABC" y "abc"
            if (e.getCodigo().trim().equalsIgnoreCase(normalizado)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Agrega un nuevo envío a la colección, validando que no sea nulo
     * y que su código no esté ya registrado.
     *
     * @param envio el envío a agregar
     * @throws IllegalArgumentException si el envío es nulo o su código ya existe
     */
    public void agregarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envío no puede ser nulo.");
        }
        // Garantizamos unicidad de código antes de agregar
        if (existeCodigo(envio.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe un envío registrado con el código: " + envio.getCodigo());
        }
        this.envios.add(envio);
    }

    /**
     * Elimina el envío ubicado en la posición dada de la lista.
     *
     * @param indice posición (0-basada) del envío a eliminar
     * @return el envío que fue eliminado
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor al tamaño de la lista
     */
    public Envio eliminarEnvio(int indice) {
        if (indice < 0 || indice >= envios.size()) {
            throw new IndexOutOfBoundsException("Índice de envío inválido: " + indice);
        }
        return this.envios.remove(indice);
    }

    /**
     * Retorna la cantidad de envíos actualmente registrados en memoria.
     *
     * @return número de envíos en la lista
     */
    public int getCantidad() {
        return envios.size();
    }

    /**
     * Elimina todos los envíos de la lista, dejándola vacía.
     * Se utiliza al cargar un nuevo archivo para reemplazar los datos anteriores.
     */
    public void limpiar() {
        this.envios.clear();
    }
}

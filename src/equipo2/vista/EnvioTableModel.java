package equipo2.vista;

import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modelo de datos para el componente {@link javax.swing.JTable} de la vista principal.
 *
 * <p>Extiende {@link AbstractTableModel} de Swing para actuar como el adaptador entre
 * la lista de {@link Envio} (modelo del dominio) y la tabla gráfica que ve el usuario.</p>
 *
 * <p>Define 6 columnas para mostrar en la tabla:</p>
 * <ol>
 *   <li><b>Tipo</b>: "LOCAL" o "INTERURBANO"</li>
 *   <li><b>Código</b>: identificador único del envío</li>
 *   <li><b>Destino</b>: ciudad de destino</li>
 *   <li><b>Peso (kg)</b>: peso del paquete formateado con 2 decimales</li>
 *   <li><b>Tarifa / Distancia</b>: dato específico según el tipo de envío</li>
 *   <li><b>Importe Base ($)</b>: resultado de {@link Envio#calcularImporte()} formateado</li>
 * </ol>
 *
 * <p>Arquitectura: capa <b>Vista</b> del patrón MVC.</p>
 */
public class EnvioTableModel extends AbstractTableModel {

    // ============================================================
    // Definición de columnas de la tabla
    // ============================================================

    /** Nombres de las columnas tal como aparecen en el encabezado de la tabla. */
    private final String[] columnas = {
            "Tipo", "Código", "Destino", "Peso (kg)", "Tarifa / Distancia", "Importe Base ($)"
    };

    /** Lista de envíos actualmente mostrados en la tabla (puede ser un subconjunto filtrado). */
    private List<Envio> envios = new ArrayList<>();

    // ============================================================
    // Actualización de datos
    // ============================================================

    /**
     * Reemplaza la lista de envíos mostrados en la tabla y notifica a Swing
     * que los datos cambiaron para que redibuje la tabla.
     *
     * <p>Se hace una copia defensiva de la lista recibida para que cambios
     * externos no afecten lo que se muestra en la tabla.</p>
     *
     * @param envios nueva lista de envíos a mostrar (puede ser {@code null})
     */
    public void setEnvios(List<Envio> envios) {
        // Copia defensiva: evita que modificaciones externas afecten la vista
        this.envios = (envios != null) ? new ArrayList<>(envios) : new ArrayList<>();
        // Notifica a la JTable que los datos cambiaron → la tabla se redibuja
        fireTableDataChanged();
    }

    /**
     * Retorna el envío ubicado en la fila indicada del modelo (no de la vista,
     * ya que el orden puede cambiar al ordenar columnas).
     *
     * @param fila índice de fila en el modelo (0-basado)
     * @return el envío de esa fila, o {@code null} si el índice es inválido
     */
    public Envio getEnvio(int fila) {
        if (fila >= 0 && fila < envios.size()) {
            return envios.get(fila);
        }
        return null;
    }

    // ============================================================
    // Implementación de AbstractTableModel (obligatoria)
    // ============================================================

    /** @return cantidad de filas (un envío por fila) */
    @Override
    public int getRowCount() { return envios.size(); }

    /** @return cantidad de columnas definidas */
    @Override
    public int getColumnCount() { return columnas.length; }

    /** @return el nombre de la columna indicada (para el encabezado de la tabla) */
    @Override
    public String getColumnName(int columna) { return columnas[columna]; }

    /**
     * Retorna el valor a mostrar en una celda específica de la tabla.
     *
     * <p>Cada columna tiene su propia lógica de formateo:</p>
     * <ul>
     *   <li>Columna 0 (Tipo): "LOCAL" o "INTERURBANO" según la subclase del envío.</li>
     *   <li>Columna 4 (Tarifa/Distancia): muestra la tarifa por kg o la distancia, según el tipo.</li>
     *   <li>Columna 5 (Importe): llama a {@code calcularImporte()} vía polimorfismo.</li>
     * </ul>
     *
     * @param fila    índice de fila (0-basado)
     * @param columna índice de columna (0-basado)
     * @return el valor a mostrar en esa celda, formateado como String
     */
    @Override
    public Object getValueAt(int fila, int columna) {
        Envio e = envios.get(fila);

        switch (columna) {
            case 0:
                // Usamos instanceof para determinar el tipo de envío (polimorfismo)
                return (e instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO";
            case 1:
                return e.getCodigo();
            case 2:
                return e.getDestino();
            case 3:
                // Peso formateado con 2 decimales usando punto como separador decimal
                return String.format(Locale.US, "%.2f", e.getPesoKg());
            case 4:
                // La columna 4 muestra información diferente según el tipo de envío
                if (e instanceof EnvioLocal) {
                    return String.format(Locale.US, "$%.2f /kg", ((EnvioLocal) e).getTarifaPorKg());
                } else if (e instanceof EnvioInterurbano) {
                    return String.format(Locale.US, "%.2f km", ((EnvioInterurbano) e).getDistanciaKm());
                }
                return "";
            case 5:
                // Llamada polimórfica: cada envío calcula su propio importe
                return String.format(Locale.US, "$%.2f", e.calcularImporte());
            default:
                return "";
        }
    }
}

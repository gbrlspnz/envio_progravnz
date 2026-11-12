package equipo2.vista;

import equipo2.modelo.Envio;
import equipo2.modelo.EnvioInterurbano;
import equipo2.modelo.EnvioLocal;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modelo de tabla para presentar la colección polimórfica de Envio en un JTable.
 * Inspirado en la plantilla PacienteTableModel dada en clases.
 */
public class EnvioTableModel extends AbstractTableModel {

    private final String[] columnas = {
            "Tipo", "Código", "Destino", "Peso (kg)", "Tarifa / Distancia", "Importe Base ($)"
    };
    private List<Envio> envios = new ArrayList<>();

    public void setEnvios(List<Envio> envios) {
        this.envios = (envios != null) ? new ArrayList<>(envios) : new ArrayList<>();
        fireTableDataChanged();
    }

    public Envio getEnvio(int fila) {
        if (fila >= 0 && fila < envios.size()) {
            return envios.get(fila);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return envios.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Envio e = envios.get(fila);

        switch (columna) {
            case 0:
                return (e instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO";
            case 1:
                return e.getCodigo();
            case 2:
                return e.getDestino();
            case 3:
                return String.format(Locale.US, "%.2f", e.getPesoKg());
            case 4:
                if (e instanceof EnvioLocal) {
                    return String.format(Locale.US, "$%.2f /kg", ((EnvioLocal) e).getTarifaPorKg());
                } else if (e instanceof EnvioInterurbano) {
                    return String.format(Locale.US, "%.2f km", ((EnvioInterurbano) e).getDistanciaKm());
                }
                return "";
            case 5:
                // Cálculo polimórfico mediante el contrato común de la clase padre
                return String.format(Locale.US, "$%.2f", e.calcularImporte());
            default:
                return "";
        }
    }
}

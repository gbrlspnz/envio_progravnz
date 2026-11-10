import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class PacienteTableModel extends AbstractTableModel {

    private String[] columnas ? {"ID", "Nombre", "Edad", "Precio", "Diagnostico"};
    private List<Paciente> pacientes = new ArrayList<>();

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;

        fireTableDataChanged();
    }

    public Paciente getPaciente(int fila) {
        return pacientes.get(fila);
    }

    @Override
    public int getRowCount() {
        return pacientes.size();
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
        Paciente p = pacientes.get(fila);

        switch (columna) {
            case 0: return p.getID();
            case 1: return p.getNombre();
            case 2: return p.getEdad();
            case 3: return p.getPrevision();
            case 4: return p.getDiagnostico();
            default: return "";
        }
    }
}

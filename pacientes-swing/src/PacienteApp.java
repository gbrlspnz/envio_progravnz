import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;

public class PacienteApp {

    private JTextField txtRuta;
    private JTextField txtBuscar;
    private JLabel lblEstado;
    private JTable tabla;
    private PacienteTableModel modelo;
    private TableRowSorter<PacienteTableModel> sorter;

    private list<Paciente> pacienteOriginales = new ArrayList<>();
    private PacienteCSVReader reader = new PacienteCSVReader();

    public PacienteApp() {

        setTitle("Gestor de Pacientes - CSV + Swing");
        setSize(900, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12,12)
        );

        construirPanelSuperior();
        construirTabla();
        construirPanelInferior();
    }

    private void construirPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(8 ,8));
        panel.setBorder(BorderFactory.createTitledBorder("Archivo de pacientes:"));

        txtRuta = new JTextField("data/pacientes.csv");
        JButton btnCargar = new JButton("Cargar CSV..");
        JButton btnExaminar = new JButton("Examinar...");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnExaminar);
        botones.add(btnCargar);

        panel.add(new JLabel("Ruta:"), BorderLayout.WEST);
        panel.add(txtRuta, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.EAST);

        btnExaminar.addActionListener(e -> SeleccionarArchivo());
        btnCargar.addActionListener(e -> cargarPacientes());
    }

    private void construirTabla() {

        modelo = new PacienteTableModel();
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setAutoCreateRowSorter(true);

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Pacientes cargados"));

        add(scroll, BorderLayout.CENTER);
    }

    private void construirPanelInferior() {

        JPanel panel = new JPanel(new BorderLayout(8 ,8));
        panel.setBorder(BorderFactory.createTitledBorder("Busqueda"));

        txtBuscar = new JTextField();
        JButton btnLimpiar = new JButton("Limpiar filtro");
        lblEstado = new JLabel("Esperando carga de archivo...");

        panel.add(new JLabel("Buscar por ID, nombre o previsión"), BorderLayout.WEST);
        panel.add(txtBuscar, BorderLayout.CENTER);
        panel.add(btnLimpiar, BorderLayout.EAST);
        panel.add(lblEstado, BorderLayout.SOUTH);

        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        btnLimpiar.addActionListener(e -> txtBuscar.setText(""));

        add(panel, BorderLayout.SOUTH)
    }

    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showOpenDialog(this);

        if (resultado == JFileChooser.APROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            txtRuta.setText(archivo.getAbsoultePath());
        }
    }

    private void cargarPacientes() {
        try {
            pacienteOriginales = reader.leer(txtRuta.getText());
            modelo.setPacientes(pacienteOriginales);
            lblEstado.setText(pacienteOriginales.size() + " Pacientes cargados correctamente.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "No se puede cargar el archivo: " + ex.getMessage(), "Error de lectura", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();

        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
            lblEstado.setText(modelo.getRowCount() + " Pacientes cargados. ");
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)"+ texto));
            lblEstado.setText("Filtro aplicado: " + texto);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater()( -> {
            new PacienteApp().setVisible(true);
        });
    }
}

package equipo2.vista;

import equipo2.datos.EnvioArchivoManager;
import equipo2.logica.GestorEnvios;
import equipo2.logica.patron.strategy.ModalidadBeneficioStrategy;
import equipo2.logica.patron.strategy.ModalidadNormalStrategy;
import equipo2.logica.patron.strategy.ModalidadStrategy;
import equipo2.modelo.Envio;
import equipo2.modelo.EnvioLocal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Aplicación principal con interfaz gráfica Java Swing para el Equipo 2: Cotización de envíos.
 */
public class EnvioApp extends JFrame {

    private JTextField txtRuta;
    private JTextField txtBuscar;
    private JLabel lblEstado;
    private JTable tabla;
    private EnvioTableModel modelo;
    private TableRowSorter<EnvioTableModel> sorter;

    // Filtros por tipo
    private JCheckBox chkLocal;
    private JCheckBox chkInterurbano;

    // Lógica y Persistencia
    private final GestorEnvios gestor = new GestorEnvios();
    private final EnvioArchivoManager archivoManager = new EnvioArchivoManager();

    // Patrón Strategy
    private ModalidadStrategy estrategiaActual = new ModalidadNormalStrategy();
    private final ModalidadStrategy estrategiaNormal    = new ModalidadNormalStrategy();
    private final ModalidadStrategy estrategiaBeneficio = new ModalidadBeneficioStrategy();

    // Componentes de Cotización
    private JRadioButton rbNormal;
    private JRadioButton rbBeneficio;
    private JLabel lblDetalleSeleccion;
    private JLabel lblImporteBase;
    private JLabel lblDescuento;
    private JLabel lblTotal;

    // Componentes de Suma múltiple
    private JLabel lblSumaSeleccion;
    private JLabel lblSumaImportes;
    private JLabel lblSumaDescuentos;
    private JLabel lblSumaTotal;
    private JButton btnSelTodos;
    private JButton btnDeselTodos;

    public EnvioApp() {
        setTitle("Gestor y Cotización de Envíos - Equipo 2");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelContenedor = (JPanel) getContentPane();
        panelContenedor.setBorder(new EmptyBorder(12, 12, 12, 12));

        construirPanelSuperior();
        construirCentro();
        construirPanelInferior();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANEL SUPERIOR: selección y carga de archivo
    // ─────────────────────────────────────────────────────────────────────────
    private void construirPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Archivo de envíos:"));

        txtRuta = new JTextField();
        txtRuta.setEditable(false);
        txtRuta.setForeground(Color.GRAY);
        txtRuta.setText("(ningún archivo seleccionado)");

        JButton btnExaminar = new JButton("Examinar...");
        JButton btnCargar   = new JButton("Cargar archivo");
        JButton btnGuardar  = new JButton("Guardar cambios");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnExaminar);
        botones.add(btnCargar);
        botones.add(btnGuardar);

        panel.add(new JLabel("Archivo:"), BorderLayout.WEST);
        panel.add(txtRuta, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.EAST);

        btnExaminar.addActionListener(e -> seleccionarArchivo());
        btnCargar.addActionListener(e   -> cargarEnvios());
        btnGuardar.addActionListener(e  -> guardarEnvios());

        add(panel, BorderLayout.NORTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CENTRO: tabla + filtros + panel derecho
    // ─────────────────────────────────────────────────────────────────────────
    private void construirCentro() {
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        // ── Tabla (multiselección) ────────────────────────────────────────────
        modelo = new EnvioTableModel();
        tabla  = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // multiselección

        // Ordenamiento: columnas numéricas comparan como double
        sorter = new TableRowSorter<>(modelo);
        sorter.setComparator(3, Comparator.comparingDouble(
                o -> Double.parseDouble(o.toString().replace(',', '.'))));
        sorter.setComparator(5, Comparator.comparingDouble(
                o -> Double.parseDouble(o.toString().replace("$", "").replace(',', '.').trim())));
        tabla.setRowSorter(sorter);

        tabla.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) actualizarCalculo();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Envíos registrados  (Ctrl+clic o Shift+clic para seleccionar varios · clic en columna para ordenar ▲▼)"));

        // ── Filtros ───────────────────────────────────────────────────────────
        JPanel panelFiltros = new JPanel(new BorderLayout(8, 6));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));

        JPanel filaBuscar = new JPanel(new BorderLayout(8, 0));
        txtBuscar = new JTextField();
        JButton btnLimpiarFiltro = new JButton("Limpiar todo");
        filaBuscar.add(new JLabel("Buscar (código / destino):"), BorderLayout.WEST);
        filaBuscar.add(txtBuscar, BorderLayout.CENTER);
        filaBuscar.add(btnLimpiarFiltro, BorderLayout.EAST);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e)  { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        JPanel filaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        chkLocal       = new JCheckBox("Mostrar LOCAL",       true);
        chkInterurbano = new JCheckBox("Mostrar INTERURBANO", true);
        chkLocal.addActionListener(e       -> aplicarFiltros());
        chkInterurbano.addActionListener(e -> aplicarFiltros());

        btnLimpiarFiltro.addActionListener(e -> {
            txtBuscar.setText("");
            chkLocal.setSelected(true);
            chkInterurbano.setSelected(true);
            aplicarFiltros();
        });

        filaTipo.add(new JLabel("Tipo:"));
        filaTipo.add(chkLocal);
        filaTipo.add(chkInterurbano);

        panelFiltros.add(filaBuscar, BorderLayout.CENTER);
        panelFiltros.add(filaTipo,   BorderLayout.SOUTH);

        JPanel panelIzquierdo = new JPanel(new BorderLayout(8, 6));
        panelIzquierdo.add(scroll,       BorderLayout.CENTER);
        panelIzquierdo.add(panelFiltros, BorderLayout.SOUTH);

        JPanel panelDerecho = construirPanelDerecho();

        panelCentro.add(panelIzquierdo, BorderLayout.CENTER);
        panelCentro.add(panelDerecho,   BorderLayout.EAST);

        add(panelCentro, BorderLayout.CENTER);
    }

    private JPanel construirPanelDerecho() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(320, 0));

        // ── Operaciones ───────────────────────────────────────────────────────
        JPanel panelOperaciones = new JPanel(new GridLayout(2, 1, 6, 6));
        panelOperaciones.setBorder(BorderFactory.createTitledBorder("Operaciones"));
        JButton btnAgregar  = new JButton("+ Agregar Envío...");
        JButton btnEliminar = new JButton("- Eliminar Seleccionado");
        btnAgregar.addActionListener(e  -> agregarEnvio());
        btnEliminar.addActionListener(e -> eliminarEnvio());
        panelOperaciones.add(btnAgregar);
        panelOperaciones.add(btnEliminar);

        // ── Cotización individual (Strategy) ──────────────────────────────────
        JPanel panelCotizacion = new JPanel();
        panelCotizacion.setLayout(new BoxLayout(panelCotizacion, BoxLayout.Y_AXIS));
        panelCotizacion.setBorder(BorderFactory.createTitledBorder("Cotización del envío"));

        rbNormal    = new JRadioButton("NORMAL (sin descuento)",    true);
        rbBeneficio = new JRadioButton("BENEFICIO (10% descuento)", false);
        ButtonGroup grupoModalidad = new ButtonGroup();
        grupoModalidad.add(rbNormal);
        grupoModalidad.add(rbBeneficio);
        rbNormal.addActionListener(e    -> { estrategiaActual = estrategiaNormal;    actualizarCalculo(); });
        rbBeneficio.addActionListener(e -> { estrategiaActual = estrategiaBeneficio; actualizarCalculo(); });

        lblDetalleSeleccion = new JLabel("Seleccione envío(s) en la tabla");
        lblDetalleSeleccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblImporteBase = new JLabel("Importe Base: $0.00");
        lblDescuento   = new JLabel("Descuento: $0.00");
        lblTotal       = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTotal.setForeground(new Color(0, 102, 0));

        panelCotizacion.add(rbNormal);
        panelCotizacion.add(rbBeneficio);
        panelCotizacion.add(Box.createVerticalStrut(8));
        panelCotizacion.add(lblDetalleSeleccion);
        panelCotizacion.add(Box.createVerticalStrut(5));
        panelCotizacion.add(lblImporteBase);
        panelCotizacion.add(Box.createVerticalStrut(3));
        panelCotizacion.add(lblDescuento);
        panelCotizacion.add(Box.createVerticalStrut(5));
        panelCotizacion.add(lblTotal);

        // ── Suma de múltiples envíos ───────────────────────────────────────────
        JPanel panelSuma = new JPanel();
        panelSuma.setLayout(new BoxLayout(panelSuma, BoxLayout.Y_AXIS));
        panelSuma.setBorder(BorderFactory.createTitledBorder("Suma de selección múltiple"));

        // Botones de selección rápida
        JPanel filaBotonesSel = new JPanel(new GridLayout(1, 2, 6, 0));
        btnSelTodos   = new JButton("Seleccionar todos");
        btnDeselTodos = new JButton("Deseleccionar todos");
        btnSelTodos.addActionListener(e   -> tabla.selectAll());
        btnDeselTodos.addActionListener(e -> tabla.clearSelection());
        filaBotonesSel.add(btnSelTodos);
        filaBotonesSel.add(btnDeselTodos);

        lblSumaSeleccion  = new JLabel("Envíos seleccionados: 0");
        lblSumaSeleccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSumaImportes   = new JLabel("Suma importes base: $0.00");
        lblSumaDescuentos = new JLabel("Suma descuentos: $0.00");
        lblSumaTotal      = new JLabel("TOTAL A PAGAR: $0.00");
        lblSumaTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSumaTotal.setForeground(new Color(140, 0, 0));

        JPanel wrapBotones = new JPanel(new BorderLayout());
        wrapBotones.add(filaBotonesSel);
        wrapBotones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        panelSuma.add(wrapBotones);
        panelSuma.add(Box.createVerticalStrut(8));
        panelSuma.add(lblSumaSeleccion);
        panelSuma.add(Box.createVerticalStrut(4));
        panelSuma.add(lblSumaImportes);
        panelSuma.add(Box.createVerticalStrut(3));
        panelSuma.add(lblSumaDescuentos);
        panelSuma.add(Box.createVerticalStrut(6));
        panelSuma.add(lblSumaTotal);

        panel.add(panelOperaciones);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panelCotizacion);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panelSuma);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void construirPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));

        lblEstado = new JLabel("Use 'Examinar...' para seleccionar un archivo .txt o .csv y luego 'Cargar archivo'.");
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblEstado, BorderLayout.WEST);

        add(panel, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCIONES DE ARCHIVO
    // ─────────────────────────────────────────────────────────────────────────
    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo de envíos (.txt o .csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de datos (*.txt, *.csv)", "txt", "csv"));
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            txtRuta.setText(archivo.getAbsolutePath());
            txtRuta.setForeground(Color.BLACK);
        }
    }

    private void cargarEnvios() {
        String ruta = txtRuta.getText().trim();
        if (ruta.isEmpty() || ruta.equals("(ningún archivo seleccionado)")) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un archivo con 'Examinar...'.",
                    "Sin archivo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            EnvioArchivoManager.ResultadoCarga res = archivoManager.leer(ruta);
            gestor.setEnvios(res.getEnvios());
            aplicarFiltros();

            if (res.tieneAdvertencias()) {
                StringBuilder msj = new StringBuilder("Se cargaron ")
                        .append(res.getEnvios().size()).append(" envíos válidos.\n\nAdvertencias:\n");
                for (String adv : res.getAdvertencias()) msj.append("• ").append(adv).append("\n");
                JOptionPane.showMessageDialog(this, msj.toString(),
                        "Advertencias al cargar", JOptionPane.WARNING_MESSAGE);
            }

            lblEstado.setText(gestor.getCantidad() + " envíos cargados desde: " + new File(ruta).getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se puede cargar el archivo:\n" + ex.getMessage(),
                    "Error de lectura", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarEnvios() {
        String ruta = txtRuta.getText().trim();
        if (ruta.isEmpty() || ruta.equals("(ningún archivo seleccionado)")) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione el archivo destino con 'Examinar...'.",
                    "Sin archivo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            archivoManager.guardar(ruta, gestor.getEnvios());
            JOptionPane.showMessageDialog(this,
                    "Se guardaron " + gestor.getCantidad() + " envíos en:\n" + ruta,
                    "Guardado exitoso", JOptionPane.INFORMATION_MESSAGE);
            lblEstado.setText("Guardado: " + new File(ruta).getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar:\n" + ex.getMessage(),
                    "Error de escritura", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTRADO
    // ─────────────────────────────────────────────────────────────────────────
    private void aplicarFiltros() {
        boolean mostrarLocal       = chkLocal.isSelected();
        boolean mostrarInterurbano = chkInterurbano.isSelected();
        String texto = txtBuscar.getText().trim();

        List<Envio> filtrados = new ArrayList<>();
        for (Envio e : gestor.getEnvios()) {
            boolean esLocal = (e instanceof EnvioLocal);
            if (esLocal && !mostrarLocal)        continue;
            if (!esLocal && !mostrarInterurbano) continue;
            filtrados.add(e);
        }
        modelo.setEnvios(filtrados);

        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
            } catch (Exception ex) {
                sorter.setRowFilter(null);
            }
        }

        lblEstado.setText(tabla.getRowCount() + " envío(s) mostrado(s)  —  " +
                gestor.getCantidad() + " en total.");
        actualizarCalculo();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGREGAR / ELIMINAR
    // ─────────────────────────────────────────────────────────────────────────
    private void agregarEnvio() {
        DialogoAgregarEnvio dialogo = new DialogoAgregarEnvio(this, gestor);
        dialogo.setVisible(true);

        Envio nuevo = dialogo.getEnvioCreado();
        if (nuevo != null) {
            try {
                gestor.agregarEnvio(nuevo);
                aplicarFiltros();
                lblEstado.setText("Envío agregado: " + nuevo.getCodigo());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al agregar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarEnvio() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un envío en la tabla para eliminar.",
                    "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Si hay varios seleccionados, eliminar solo el primero y avisar
        if (tabla.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un único envío para eliminar.",
                    "Selección múltiple", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Envio seleccionado = modelo.getEnvio(filaModelo);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el envío seleccionado?\n\n" +
                "Código:  " + seleccionado.getCodigo() + "\n" +
                "Destino: " + seleccionado.getDestino() + "\n" +
                "Tipo:    " + ((seleccionado instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO"),
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            gestor.getEnviosModificables().remove(seleccionado);
            aplicarFiltros();
            lblEstado.setText("Envío " + seleccionado.getCodigo() + " eliminado.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COTIZACIÓN: individual + suma de múltiples seleccionados
    // ─────────────────────────────────────────────────────────────────────────
    private void actualizarCalculo() {
        int[] filasVista = tabla.getSelectedRows();
        int cantidad = filasVista.length;

        if (cantidad == 0) {
            // Sin selección
            lblDetalleSeleccion.setText("Seleccione envío(s) en la tabla");
            lblImporteBase.setText("Importe Base: $0.00");
            lblDescuento.setText("Descuento: $0.00");
            lblTotal.setText("Total: $0.00");
            limpiarSuma();
            return;
        }

        if (cantidad == 1) {
            // Selección individual: muestra detalle completo
            int filaModelo = tabla.convertRowIndexToModel(filasVista[0]);
            Envio e = modelo.getEnvio(filaModelo);
            if (e == null) return;

            double base      = e.calcularImporte();
            double descuento = estrategiaActual.calcularDescuento(base);
            double total     = estrategiaActual.calcularTotal(base);
            String tipo      = (e instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO";

            lblDetalleSeleccion.setText(String.format("Código: %s (%s)", e.getCodigo(), tipo));
            lblImporteBase.setText(String.format(Locale.US, "Importe Base: $%.2f", base));
            lblDescuento.setText(String.format(Locale.US, "Descuento (%s): $%.2f", estrategiaActual.getNombre(), descuento));
            lblTotal.setText(String.format(Locale.US, "Total: $%.2f", total));
        } else {
            // Múltiple: el panel individual muestra resumen
            lblDetalleSeleccion.setText(cantidad + " envíos seleccionados");
            lblImporteBase.setText("—");
            lblDescuento.setText("—");
            lblTotal.setText("Ver suma a la derecha ↓");
        }

        // Suma acumulada de todos los seleccionados
        double sumaBase      = 0;
        double sumaDescuento = 0;
        double sumaTotal     = 0;
        for (int fv : filasVista) {
            int fm = tabla.convertRowIndexToModel(fv);
            Envio e = modelo.getEnvio(fm);
            if (e == null) continue;
            double base  = e.calcularImporte();
            sumaBase      += base;
            sumaDescuento += estrategiaActual.calcularDescuento(base);
            sumaTotal     += estrategiaActual.calcularTotal(base);
        }

        lblSumaSeleccion.setText("Envíos seleccionados: " + cantidad);
        lblSumaImportes.setText(String.format(Locale.US, "Suma importes base: $%.2f", sumaBase));
        lblSumaDescuentos.setText(String.format(Locale.US, "Suma descuentos: $%.2f", sumaDescuento));
        lblSumaTotal.setText(String.format(Locale.US, "TOTAL A PAGAR: $%.2f", sumaTotal));
    }

    private void limpiarSuma() {
        lblSumaSeleccion.setText("Envíos seleccionados: 0");
        lblSumaImportes.setText("Suma importes base: $0.00");
        lblSumaDescuentos.setText("Suma descuentos: $0.00");
        lblSumaTotal.setText("TOTAL A PAGAR: $0.00");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new EnvioApp().setVisible(true));
    }
}

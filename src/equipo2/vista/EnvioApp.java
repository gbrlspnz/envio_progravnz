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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Ventana principal de la aplicación de <b>Gestión y Cotización de Envíos</b>.
 *
 * <p>Esta clase es el <b>Controlador y Vista</b> del patrón MVC: orquesta la interacción
 * entre el usuario y el modelo ({@link GestorEnvios}, {@link equipo2.modelo.Envio}).</p>
 *
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li><b>Persistencia</b>: cargar y guardar envíos desde/hacia un archivo .txt.</li>
 *   <li><b>CRUD</b>: agregar y eliminar envíos a través de formularios gráficos.</li>
 *   <li><b>Filtrado</b>: filtrar la tabla por tipo (LOCAL/INTERURBANO) y texto libre.</li>
 *   <li><b>Cotización</b>: calcular el importe individual o acumulado de los envíos
 *       seleccionados, aplicando la modalidad elegida (patrón Strategy).</li>
 * </ul>
 *
 * <p>Patrones de diseño aplicados:</p>
 * <ul>
 *   <li><b>MVC</b>: esta clase es la Vista/Controlador; {@link GestorEnvios} es el Modelo.</li>
 *   <li><b>Strategy</b>: la modalidad de cotización (NORMAL o BENEFICIO) se intercambia
 *       en tiempo de ejecución sin modificar el código de cálculo.</li>
 *   <li><b>Factory</b>: la creación de envíos se delega a {@link equipo2.logica.patron.factory.EnvioFactory}.</li>
 * </ul>
 */
public class EnvioApp extends JFrame {

    // ============================================================
    // Constante: ruta del archivo de datos por defecto
    // ============================================================

    /**
     * Ruta relativa al directorio de trabajo donde se guarda el archivo de envíos.
     * Al iniciar la aplicación, se intenta cargar este archivo automáticamente.
     */
    public static final String RUTA_PREDETERMINADA = "datos" + File.separator + "datos.txt";

    // ============================================================
    // Componentes de la interfaz gráfica
    // ============================================================

    /** Campo de texto que muestra la ruta del archivo actualmente cargado. */
    private JTextField txtRuta;

    /** Campo de texto para buscar envíos por código o destino. */
    private JTextField txtBuscar;

    /** Barra de estado en la parte inferior de la ventana. */
    private JLabel lblEstado;

    /** Tabla que muestra los envíos registrados. */
    private JTable tabla;

    /** Modelo de datos de la tabla (adaptador entre la lista de envíos y la JTable). */
    private EnvioTableModel modelo;

    /** Permite ordenar las filas de la tabla al hacer clic en las columnas. */
    private TableRowSorter<EnvioTableModel> sorter;

    // Filtros por tipo de envío
    /** Checkbox para mostrar u ocultar los envíos de tipo LOCAL. */
    private JCheckBox chkLocal;

    /** Checkbox para mostrar u ocultar los envíos de tipo INTERURBANO. */
    private JCheckBox chkInterurbano;

    // ============================================================
    // Capa lógica y persistencia (inyección desde la Vista)
    // ============================================================

    /** Gestiona la lista de envíos en memoria durante la ejecución. */
    private final GestorEnvios gestor = new GestorEnvios();

    /** Lee y escribe el archivo de persistencia (.txt con separador ";"). */
    private final EnvioArchivoManager archivoManager = new EnvioArchivoManager();

    // ============================================================
    // PATRÓN STRATEGY: modalidades de cotización
    // ============================================================

    /**
     * Estrategia actualmente activa para calcular descuentos.
     * Se intercambia en tiempo de ejecución según el botón de radio seleccionado.
     */
    private ModalidadStrategy estrategiaActual = new ModalidadNormalStrategy();

    /** Estrategia NORMAL: sin descuento (cliente estándar). */
    private final ModalidadStrategy estrategiaNormal    = new ModalidadNormalStrategy();

    /** Estrategia BENEFICIO: 10% de descuento (cliente con beneficios). */
    private final ModalidadStrategy estrategiaBeneficio = new ModalidadBeneficioStrategy();

    // ============================================================
    // Componentes del panel de cotización individual
    // ============================================================

    /** Botón de radio para seleccionar modalidad NORMAL (sin descuento). */
    private JRadioButton rbNormal;

    /** Botón de radio para seleccionar modalidad BENEFICIO (10% descuento). */
    private JRadioButton rbBeneficio;

    /** Muestra el código y tipo del envío seleccionado individualmente. */
    private JLabel lblDetalleSeleccion;

    /** Muestra el importe base calculado por el envío (antes de descuentos). */
    private JLabel lblImporteBase;

    /** Muestra el monto del descuento aplicado según la estrategia activa. */
    private JLabel lblDescuento;

    /** Muestra el total final a pagar por el envío individual. */
    private JLabel lblTotal;

    // ============================================================
    // Componentes del panel de suma de selección múltiple
    // ============================================================

    /** Muestra cuántos envíos están seleccionados en la tabla. */
    private JLabel lblSumaSeleccion;

    /** Muestra la suma de los importes base de todos los envíos seleccionados. */
    private JLabel lblSumaImportes;

    /** Muestra la suma de los descuentos de todos los envíos seleccionados. */
    private JLabel lblSumaDescuentos;

    /** Muestra el total acumulado a pagar por todos los envíos seleccionados. */
    private JLabel lblSumaTotal;

    /** Botón para seleccionar todos los envíos de la tabla de una vez. */
    private JButton btnSelTodos;

    /** Botón para limpiar la selección de la tabla. */
    private JButton btnDeselTodos;

    // ============================================================
    // Constructor
    // ============================================================

    /**
     * Construye la ventana principal de la aplicación e inicializa todos sus paneles.
     */
    public EnvioApp() {
        setTitle("Gestor y Cotización de Envíos - Equipo 2");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10));

        // Margen interior de la ventana para que los paneles no queden pegados al borde
        JPanel panelContenedor = (JPanel) getContentPane();
        panelContenedor.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Construir los tres paneles principales de la ventana
        construirPanelSuperior();  // Barra de archivo (ruta, cargar, guardar)
        construirCentro();         // Tabla de envíos + filtros + cotización
        construirPanelInferior();  // Barra de estado
    }

    // ============================================================
    // Construcción del panel superior: selección y carga de archivo
    // ============================================================

    /**
     * Construye el panel superior con los controles para seleccionar, cargar y guardar
     * el archivo de datos de envíos.
     */
    private void construirPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Archivo de envíos:"));

        // Campo de texto no editable que muestra la ruta del archivo seleccionado
        txtRuta = new JTextField();
        txtRuta.setEditable(false);
        txtRuta.setForeground(Color.GRAY);
        txtRuta.setText("(ningún archivo seleccionado)");

        JButton btnExaminar = new JButton("Examinar...");
        JButton btnCargar   = new JButton("Cargar archivo");
        JButton btnGuardar  = new JButton("Guardar cambios");

        // Panel de botones alineado a la derecha
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnExaminar);
        botones.add(btnCargar);
        botones.add(btnGuardar);

        panel.add(new JLabel("Archivo:"), BorderLayout.WEST);
        panel.add(txtRuta,  BorderLayout.CENTER);
        panel.add(botones,  BorderLayout.EAST);

        // Conectar botones con sus métodos de acción (listeners)
        btnExaminar.addActionListener(e -> seleccionarArchivo());
        btnCargar.addActionListener(e   -> cargarEnvios());
        btnGuardar.addActionListener(e  -> guardarEnvios());

        add(panel, BorderLayout.NORTH);
    }

    // ============================================================
    // Construcción del panel central: tabla + filtros + cotización
    // ============================================================

    /**
     * Construye el panel central de la ventana, que contiene:
     * <ul>
     *   <li>La tabla de envíos con soporte de ordenamiento y multiselección.</li>
     *   <li>El panel de filtros (por tipo y por texto).</li>
     *   <li>El panel derecho con operaciones, cotización individual y suma múltiple.</li>
     * </ul>
     */
    private void construirCentro() {
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        // ── Tabla de envíos ──────────────────────────────────────────────────
        modelo = new EnvioTableModel();
        tabla  = new JTable(modelo);
        tabla.setRowHeight(26);
        // MULTIPLE_INTERVAL_SELECTION: permite seleccionar varias filas con Ctrl+clic o Shift+clic
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Configurar el ordenador de filas para que las columnas numéricas se ordenen correctamente
        sorter = new TableRowSorter<>(modelo);
        // Comparador para la columna Peso (columna 3): ordena como número, no como texto
        sorter.setComparator(3, Comparator.comparingDouble(
                o -> Double.parseDouble(o.toString().replace(',', '.'))));
        // Comparador para la columna Importe (columna 5): elimina el "$" antes de comparar
        sorter.setComparator(5, Comparator.comparingDouble(
                o -> Double.parseDouble(o.toString().replace("$", "").replace(',', '.').trim())));
        tabla.setRowSorter(sorter);

        // Listener de selección: recalcula la cotización cada vez que el usuario selecciona filas
        tabla.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) actualizarCalculo();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Envíos registrados  (Ctrl+clic o Shift+clic para seleccionar varios · clic en columna para ordenar ▲▼)"));

        // ── Panel de filtros ─────────────────────────────────────────────────
        JPanel panelFiltros = new JPanel(new BorderLayout(8, 6));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));

        // Fila superior del panel de filtros: búsqueda por texto libre
        JPanel filaBuscar = new JPanel(new BorderLayout(8, 0));
        txtBuscar = new JTextField();
        JButton btnLimpiarFiltro = new JButton("Limpiar todo");
        filaBuscar.add(new JLabel("Buscar (código / destino):"), BorderLayout.WEST);
        filaBuscar.add(txtBuscar, BorderLayout.CENTER);
        filaBuscar.add(btnLimpiarFiltro, BorderLayout.EAST);

        // Aplicar filtro en tiempo real mientras el usuario escribe (DocumentListener)
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e)  { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        // Fila inferior del panel de filtros: checkboxes por tipo de envío
        JPanel filaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        chkLocal       = new JCheckBox("Mostrar LOCAL",       true);
        chkInterurbano = new JCheckBox("Mostrar INTERURBANO", true);
        chkLocal.addActionListener(e       -> aplicarFiltros());
        chkInterurbano.addActionListener(e -> aplicarFiltros());

        // Botón para limpiar todos los filtros a la vez
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

        // ── Ensamblado del panel izquierdo (tabla + filtros) ─────────────────
        JPanel panelIzquierdo = new JPanel(new BorderLayout(8, 6));
        panelIzquierdo.add(scroll,       BorderLayout.CENTER);
        panelIzquierdo.add(panelFiltros, BorderLayout.SOUTH);

        // ── Panel derecho (operaciones + cotización) ─────────────────────────
        JPanel panelDerecho = construirPanelDerecho();

        panelCentro.add(panelIzquierdo, BorderLayout.CENTER);
        panelCentro.add(panelDerecho,   BorderLayout.EAST);

        add(panelCentro, BorderLayout.CENTER);
    }

    /**
     * Construye el panel lateral derecho con tres secciones:
     * <ol>
     *   <li><b>Operaciones</b>: botones Agregar y Eliminar envío.</li>
     *   <li><b>Cotización individual</b>: selector de modalidad (Strategy) y detalle de un envío.</li>
     *   <li><b>Suma múltiple</b>: totales acumulados de los envíos seleccionados.</li>
     * </ol>
     *
     * @return el panel derecho completamente ensamblado
     */
    private JPanel construirPanelDerecho() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(320, 0));

        // ── Sección 1: Operaciones CRUD ──────────────────────────────────────
        JPanel panelOperaciones = new JPanel(new GridLayout(2, 1, 6, 6));
        panelOperaciones.setBorder(BorderFactory.createTitledBorder("Operaciones"));
        JButton btnAgregar  = new JButton("+ Agregar Envío...");
        JButton btnEliminar = new JButton("- Eliminar Seleccionado");
        btnAgregar.addActionListener(e  -> agregarEnvio());
        btnEliminar.addActionListener(e -> eliminarEnvio());
        panelOperaciones.add(btnAgregar);
        panelOperaciones.add(btnEliminar);

        // ── Sección 2: Cotización individual con patrón Strategy ─────────────
        JPanel panelCotizacion = new JPanel();
        panelCotizacion.setLayout(new BoxLayout(panelCotizacion, BoxLayout.Y_AXIS));
        panelCotizacion.setBorder(BorderFactory.createTitledBorder("Cotización del envío"));

        // PATRÓN STRATEGY: los botones de radio intercambian la estrategia de descuento
        rbNormal    = new JRadioButton("NORMAL (sin descuento)",    true);
        rbBeneficio = new JRadioButton("BENEFICIO (10% descuento)", false);
        ButtonGroup grupoModalidad = new ButtonGroup();
        grupoModalidad.add(rbNormal);
        grupoModalidad.add(rbBeneficio);

        // Al cambiar la modalidad, se actualiza la estrategia y se recalcula en tiempo real
        rbNormal.addActionListener(e    -> { estrategiaActual = estrategiaNormal;    actualizarCalculo(); });
        rbBeneficio.addActionListener(e -> { estrategiaActual = estrategiaBeneficio; actualizarCalculo(); });

        // Etiquetas que muestran el resultado de la cotización individual
        lblDetalleSeleccion = new JLabel("Seleccione envío(s) en la tabla");
        lblDetalleSeleccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblImporteBase = new JLabel("Importe Base: $0.00");
        lblDescuento   = new JLabel("Descuento: $0.00");
        lblTotal       = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTotal.setForeground(new Color(0, 102, 0)); // Verde oscuro para el total

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

        // ── Sección 3: Suma de múltiples envíos ─────────────────────────────
        JPanel panelSuma = new JPanel();
        panelSuma.setLayout(new BoxLayout(panelSuma, BoxLayout.Y_AXIS));
        panelSuma.setBorder(BorderFactory.createTitledBorder("Suma de selección múltiple"));

        // Botones de selección rápida (seleccionar / deseleccionar toda la tabla)
        JPanel filaBotonesSel = new JPanel(new GridLayout(1, 2, 6, 0));
        btnSelTodos   = new JButton("Seleccionar todos");
        btnDeselTodos = new JButton("Deseleccionar todos");
        btnSelTodos.addActionListener(e   -> tabla.selectAll());
        btnDeselTodos.addActionListener(e -> tabla.clearSelection());
        filaBotonesSel.add(btnSelTodos);
        filaBotonesSel.add(btnDeselTodos);

        // Etiquetas que muestran los totales acumulados de la selección múltiple
        lblSumaSeleccion  = new JLabel("Envíos seleccionados: 0");
        lblSumaSeleccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSumaImportes   = new JLabel("Suma importes base: $0.00");
        lblSumaDescuentos = new JLabel("Suma descuentos: $0.00");
        lblSumaTotal      = new JLabel("TOTAL A PAGAR: $0.00");
        lblSumaTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSumaTotal.setForeground(new Color(140, 0, 0)); // Rojo oscuro para destacar el total

        // Envolvemos los botones en un panel para limitar su altura máxima
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

        // Ensamblar las tres secciones en el panel derecho
        panel.add(panelOperaciones);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panelCotizacion);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panelSuma);
        panel.add(Box.createVerticalGlue()); // Empuja el contenido hacia arriba

        return panel;
    }

    /**
     * Construye la barra de estado en la parte inferior de la ventana.
     * Muestra mensajes informativos sobre la última operación realizada.
     */
    private void construirPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));

        lblEstado = new JLabel("Use 'Examinar...' para seleccionar un archivo .txt o .csv y luego 'Cargar archivo'.");
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblEstado, BorderLayout.WEST);

        add(panel, BorderLayout.SOUTH);
    }

    // ============================================================
    // Acciones relacionadas con archivos
    // ============================================================

    /**
     * Abre un selector de archivos para que el usuario elija el archivo .txt a cargar.
     * Solo actualiza la ruta en pantalla; no carga el archivo automáticamente.
     */
    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo de envíos (.txt o .csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de datos (*.txt, *.csv)", "txt", "csv"));
        // Abrir el selector en el directorio de trabajo actual
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            txtRuta.setText(archivo.getAbsolutePath());
            txtRuta.setForeground(Color.BLACK);
        }
    }

    /**
     * Realiza la <b>carga automática al iniciar</b> la aplicación.
     *
     * <p>Intenta leer el archivo {@value #RUTA_PREDETERMINADA} en el directorio de trabajo.
     * Si no existe, avisa al usuario y permite trabajar con una colección vacía.
     * Al guardar posteriormente, se creará el archivo automáticamente.</p>
     */
    public void cargaInicial() {
        File archivo = new File(RUTA_PREDETERMINADA).getAbsoluteFile();
        // Mostrar la ruta del archivo por defecto en el campo de ruta
        txtRuta.setText(archivo.getPath());
        txtRuta.setForeground(Color.BLACK);
        cargarDesde(archivo.getPath());
    }

    /**
     * Acción del botón "Cargar archivo": recarga el archivo cuya ruta está en pantalla.
     * Muestra un aviso si aún no se seleccionó ningún archivo.
     */
    private void cargarEnvios() {
        String ruta = txtRuta.getText().trim();
        // Verificar que haya una ruta válida antes de intentar cargar
        if (ruta.isEmpty() || ruta.equals("(ningún archivo seleccionado)")) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un archivo con 'Examinar...'.",
                    "Sin archivo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cargarDesde(ruta);
    }

    /**
     * Carga los envíos desde el archivo en la ruta indicada y actualiza la vista.
     *
     * <p>El método es <b>tolerante a errores</b>: si algunas líneas del archivo tienen
     * problemas de formato, se omiten y se informa al usuario sin interrumpir la carga
     * de las líneas correctas.</p>
     *
     * @param ruta ruta absoluta o relativa del archivo a cargar
     */
    private void cargarDesde(String ruta) {
        try {
            // Leer el archivo (maneja errores línea a línea y retorna advertencias)
            EnvioArchivoManager.ResultadoCarga res = archivoManager.leer(ruta);
            gestor.setEnvios(res.getEnvios()); // Reemplaza la lista en memoria con los datos del archivo
            aplicarFiltros(); // Actualizar la tabla con los datos recién cargados

            // Si hubo líneas con errores, informar al usuario detalladamente
            if (res.tieneAdvertencias()) {
                StringBuilder msj = new StringBuilder("Se cargaron ")
                        .append(res.getEnvios().size()).append(" envíos válidos.\n\nLíneas omitidas:\n");
                for (String adv : res.getAdvertencias()) msj.append("• ").append(adv).append("\n");
                JOptionPane.showMessageDialog(this, msj.toString(),
                        "Advertencias al cargar", JOptionPane.WARNING_MESSAGE);
            }

            lblEstado.setText(gestor.getCantidad() + " envíos cargados desde: " + new File(ruta).getName());

        } catch (FileNotFoundException ex) {
            // El archivo no existe: se trabaja con lista vacía (normal en la primera ejecución)
            gestor.limpiar();
            aplicarFiltros();
            JOptionPane.showMessageDialog(this,
                    "No se encontró el archivo:\n" + ruta
                            + "\n\nSe trabajará con una lista vacía. Al guardar se creará el archivo.",
                    "Archivo no encontrado", JOptionPane.WARNING_MESSAGE);
            lblEstado.setText("Archivo no encontrado: se trabaja con una lista vacía.");

        } catch (IOException ex) {
            // Error de lectura del sistema operativo: no cerramos la app, conservamos los datos
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo:\n" + ex.getMessage(),
                    "Error de lectura", JOptionPane.ERROR_MESSAGE);
            lblEstado.setText("Error de lectura: no se modificaron los datos.");
        }
    }

    /**
     * Guarda todos los envíos en memoria en el archivo indicado en la ruta.
     * Si el archivo no existe, lo crea. Si existe, lo sobreescribe completamente.
     */
    private void guardarEnvios() {
        String ruta = txtRuta.getText().trim();
        // Verificar que haya una ruta válida antes de guardar
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

    // ============================================================
    // Filtrado de la tabla
    // ============================================================

    /**
     * Aplica los filtros activos sobre la lista de envíos y actualiza la tabla.
     *
     * <p>Los filtros se combinan:</p>
     * <ul>
     *   <li>Los checkboxes filtran por tipo (LOCAL / INTERURBANO).</li>
     *   <li>El campo de texto filtra por código o destino usando una expresión regular.</li>
     * </ul>
     *
     * <p>Este método se llama automáticamente cada vez que el usuario cambia
     * algún filtro o cuando se cargan o modifican los datos.</p>
     */
    private void aplicarFiltros() {
        boolean mostrarLocal       = chkLocal.isSelected();
        boolean mostrarInterurbano = chkInterurbano.isSelected();
        String texto               = txtBuscar.getText().trim();

        // Filtrar por tipo: recorremos todos los envíos y conservamos solo los que corresponden
        List<Envio> filtrados = new ArrayList<>();
        for (Envio e : gestor.getEnvios()) {
            boolean esLocal = (e instanceof EnvioLocal);
            if (esLocal && !mostrarLocal)        continue; // Ocultar LOCAL si el checkbox está desmarcado
            if (!esLocal && !mostrarInterurbano) continue; // Ocultar INTERURBANO si está desmarcado
            filtrados.add(e);
        }
        modelo.setEnvios(filtrados); // Actualizar el modelo de la tabla con los envíos filtrados

        // Aplicar filtro adicional por texto libre (busca en todas las columnas de la tabla)
        if (texto.isEmpty()) {
            sorter.setRowFilter(null); // Sin filtro de texto: mostrar todo lo del modelo
        } else {
            try {
                // "(?i)" hace la búsqueda insensible a mayúsculas/minúsculas
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
            } catch (Exception ex) {
                // Si el texto es una expresión regular inválida (ej: "["), ignoramos el filtro
                sorter.setRowFilter(null);
            }
        }

        // Actualizar la barra de estado con el conteo de filas visibles vs total
        lblEstado.setText(tabla.getRowCount() + " envío(s) mostrado(s)  —  " +
                gestor.getCantidad() + " en total.");
        actualizarCalculo(); // Recalcular la cotización porque puede haber cambiado la selección
    }

    // ============================================================
    // Operaciones CRUD: agregar y eliminar envíos
    // ============================================================

    /**
     * Abre el diálogo para agregar un nuevo envío y, si el usuario confirma,
     * agrega el envío al gestor y actualiza la tabla.
     */
    private void agregarEnvio() {
        // Abrir el diálogo modal; la ejecución se bloquea aquí hasta que el diálogo se cierre
        DialogoAgregarEnvio dialogo = new DialogoAgregarEnvio(this, gestor);
        dialogo.setVisible(true);

        // Una vez que el diálogo se cierra, verificamos si el usuario creó un envío
        Envio nuevo = dialogo.getEnvioCreado();
        if (nuevo != null) {
            try {
                gestor.agregarEnvio(nuevo);  // Agregar al modelo de negocio
                aplicarFiltros();            // Refrescar la tabla
                lblEstado.setText("Envío agregado: " + nuevo.getCodigo());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al agregar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Elimina el envío actualmente seleccionado en la tabla, previa confirmación del usuario.
     *
     * <p>Solo se puede eliminar un envío a la vez. Si hay varios seleccionados,
     * se muestra un aviso solicitando que se seleccione solo uno.</p>
     */
    private void eliminarEnvio() {
        int filaVista = tabla.getSelectedRow();

        // Verificar que haya algún envío seleccionado
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un envío en la tabla para eliminar.",
                    "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar que no haya más de un envío seleccionado
        if (tabla.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un único envío para eliminar.",
                    "Selección múltiple", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir la fila de la vista al índice del modelo (pueden diferir si hay ordenamiento activo)
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Envio seleccionado = modelo.getEnvio(filaModelo);

        // Pedir confirmación antes de eliminar
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el envío seleccionado?\n\n" +
                "Código:  " + seleccionado.getCodigo() + "\n" +
                "Destino: " + seleccionado.getDestino() + "\n" +
                "Tipo:    " + ((seleccionado instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO"),
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Eliminar el objeto directamente de la lista interna del gestor
            gestor.getEnviosModificables().remove(seleccionado);
            aplicarFiltros(); // Refrescar la tabla
            lblEstado.setText("Envío " + seleccionado.getCodigo() + " eliminado.");
        }
    }

    // ============================================================
    // Cotización: individual y suma de selección múltiple
    // ============================================================

    /**
     * Calcula y muestra la cotización de los envíos seleccionados en la tabla.
     *
     * <p>Comportamiento según la cantidad de envíos seleccionados:</p>
     * <ul>
     *   <li><b>Ninguno</b>: limpia todos los campos de cotización.</li>
     *   <li><b>Uno</b>: muestra el detalle completo (código, tipo, base, descuento, total).</li>
     *   <li><b>Varios</b>: el panel individual muestra un resumen; el panel de suma
     *       muestra el acumulado de todos los seleccionados.</li>
     * </ul>
     *
     * <p>La estrategia de descuento activa se aplica usando el <b>patrón Strategy</b>:
     * la misma interfaz {@link ModalidadStrategy} se llama sin importar si es NORMAL o BENEFICIO.</p>
     */
    private void actualizarCalculo() {
        int[] filasVista = tabla.getSelectedRows(); // Índices de las filas seleccionadas en la vista
        int cantidad = filasVista.length;

        if (cantidad == 0) {
            // Sin selección: limpiar todos los paneles de cotización
            lblDetalleSeleccion.setText("Seleccione envío(s) en la tabla");
            lblImporteBase.setText("Importe Base: $0.00");
            lblDescuento.setText("Descuento: $0.00");
            lblTotal.setText("Total: $0.00");
            limpiarSuma();
            return;
        }

        if (cantidad == 1) {
            // Selección individual: mostrar el detalle completo del envío seleccionado
            int filaModelo = tabla.convertRowIndexToModel(filasVista[0]);
            Envio e = modelo.getEnvio(filaModelo);
            if (e == null) return;

            // Calcular usando la estrategia activa (NORMAL o BENEFICIO)
            double base      = e.calcularImporte();                        // Importe bruto del envío
            double descuento = estrategiaActual.calcularDescuento(base);   // Descuento según estrategia
            double total     = estrategiaActual.calcularTotal(base);       // Total final a pagar
            String tipo      = (e instanceof EnvioLocal) ? "LOCAL" : "INTERURBANO";

            lblDetalleSeleccion.setText(String.format("Código: %s (%s)", e.getCodigo(), tipo));
            lblImporteBase.setText(String.format(Locale.US, "Importe Base: $%.2f", base));
            lblDescuento.setText(String.format(Locale.US, "Descuento (%s): $%.2f",
                    estrategiaActual.getNombre(), descuento));
            lblTotal.setText(String.format(Locale.US, "Total: $%.2f", total));

        } else {
            // Selección múltiple: el panel individual solo indica cuántos hay seleccionados
            lblDetalleSeleccion.setText(cantidad + " envíos seleccionados");
            lblImporteBase.setText("—");
            lblDescuento.setText("—");
            lblTotal.setText("Ver suma a la derecha ↓");
        }

        // ── Acumular totales de TODOS los envíos seleccionados ───────────────
        double sumaBase      = 0;
        double sumaDescuento = 0;
        double sumaTotal     = 0;

        for (int filaVista : filasVista) {
            int filaModelo = tabla.convertRowIndexToModel(filaVista);
            Envio e = modelo.getEnvio(filaModelo);
            if (e == null) continue;

            double base   = e.calcularImporte();
            sumaBase      += base;
            sumaDescuento += estrategiaActual.calcularDescuento(base);
            sumaTotal     += estrategiaActual.calcularTotal(base);
        }

        // Actualizar el panel de suma múltiple con los acumulados calculados
        lblSumaSeleccion.setText("Envíos seleccionados: " + cantidad);
        lblSumaImportes.setText(String.format(Locale.US, "Suma importes base: $%.2f", sumaBase));
        lblSumaDescuentos.setText(String.format(Locale.US, "Suma descuentos: $%.2f", sumaDescuento));
        lblSumaTotal.setText(String.format(Locale.US, "TOTAL A PAGAR: $%.2f", sumaTotal));
    }

    /**
     * Reinicia todos los campos del panel de suma múltiple a su estado inicial (cero).
     * Se llama cuando no hay ningún envío seleccionado en la tabla.
     */
    private void limpiarSuma() {
        lblSumaSeleccion.setText("Envíos seleccionados: 0");
        lblSumaImportes.setText("Suma importes base: $0.00");
        lblSumaDescuentos.setText("Suma descuentos: $0.00");
        lblSumaTotal.setText("TOTAL A PAGAR: $0.00");
    }

    // ============================================================
    // Punto de entrada principal de la aplicación
    // ============================================================

    /**
     * Método {@code main}: punto de entrada de la aplicación.
     *
     * <p>Configura el Look and Feel nativo del sistema operativo y lanza la ventana
     * principal en el hilo de eventos de Swing ({@code SwingUtilities.invokeLater}),
     * tal como exige la especificación de Swing para garantizar la seguridad de hilos.</p>
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        // Intentar usar el estilo visual nativo del sistema operativo (Windows, macOS, Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, Swing usará su propio estilo por defecto (Metal)
        }

        // Crear y mostrar la ventana en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            EnvioApp app = new EnvioApp();
            app.setVisible(true);
            // Intentar cargar automáticamente el archivo de datos al iniciar
            app.cargaInicial();
        });
    }
}

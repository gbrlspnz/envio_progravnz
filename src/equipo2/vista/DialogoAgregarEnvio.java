package equipo2.vista;

import equipo2.logica.GestorEnvios;
import equipo2.logica.patron.factory.EnvioFactory;
import equipo2.modelo.Envio;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para registrar un <b>nuevo envío</b> en el sistema.
 *
 * <p>Esta ventana de diálogo recolecta del usuario todos los datos necesarios
 * para crear un envío y delega la creación al {@link EnvioFactory} (patrón Factory),
 * que también aplica las validaciones de negocio antes de instanciar el objeto.</p>
 *
 * <p>Flujo de uso:</p>
 * <ol>
 *   <li>El usuario selecciona el tipo de envío (LOCAL o INTERURBANO).</li>
 *   <li>Completa los campos: código, destino, peso, y tarifa o distancia.</li>
 *   <li>Al presionar "Guardar Envío", se validan los datos y se crea el envío.</li>
 *   <li>La ventana principal recupera el envío creado con {@link #getEnvioCreado()}.</li>
 * </ol>
 *
 * <p>Arquitectura: capa <b>Vista</b> del patrón MVC.</p>
 */
public class DialogoAgregarEnvio extends JDialog {

    // ============================================================
    // Componentes del formulario
    // ============================================================

    /** Selector del tipo de envío: "LOCAL" o "INTERURBANO". */
    private final JComboBox<String> cmbTipo;

    /** Campo de texto para ingresar el código único del envío. */
    private final JTextField txtCodigo;

    /** Campo de texto para ingresar el destino del paquete. */
    private final JTextField txtDestino;

    /** Campo de texto para ingresar el peso en kilogramos. */
    private final JTextField txtPeso;

    /** Etiqueta dinámica que cambia según el tipo de envío seleccionado. */
    private final JLabel lblDatoEspecifico;

    /** Campo de texto para la tarifa por kg (LOCAL) o distancia en km (INTERURBANO). */
    private final JTextField txtDatoEspecifico;

    // ============================================================
    // Estado del diálogo
    // ============================================================

    /**
     * Almacena el envío creado tras una operación exitosa.
     * Es {@code null} si el usuario canceló o hubo un error de validación.
     */
    private Envio envioCreado = null;

    /**
     * Referencia al gestor de envíos para verificar duplicidad de códigos
     * antes de intentar crear el envío nuevo.
     */
    private final GestorEnvios gestor;

    // ============================================================
    // Constructor
    // ============================================================

    /**
     * Construye el diálogo modal para agregar un nuevo envío.
     *
     * @param parent  ventana padre (para centrar el diálogo y bloquear la interacción)
     * @param gestor  gestor de envíos para verificar que el código no esté duplicado
     */
    public DialogoAgregarEnvio(Frame parent, GestorEnvios gestor) {
        // "true" en el tercer parámetro hace que sea modal (bloquea la ventana padre)
        super(parent, "Agregar Nuevo Envío", true);
        this.gestor = gestor;

        // Configuración básica de la ventana
        setSize(420, 320);
        setLocationRelativeTo(parent);  // Centrar respecto a la ventana padre
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // ── Formulario con 5 filas de campos ────────────────────────────────
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 8, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // Inicialización de los componentes del formulario
        cmbTipo            = new JComboBox<>(new String[]{"LOCAL", "INTERURBANO"});
        txtCodigo          = new JTextField();
        txtDestino         = new JTextField();
        txtPeso            = new JTextField();
        lblDatoEspecifico  = new JLabel("Tarifa por Kg ($):");
        txtDatoEspecifico  = new JTextField();

        // Cuando el usuario cambia el tipo, actualizamos la etiqueta del campo específico
        cmbTipo.addActionListener(e -> {
            String tipo = (String) cmbTipo.getSelectedItem();
            if ("LOCAL".equals(tipo)) {
                lblDatoEspecifico.setText("Tarifa por Kg ($):");
            } else {
                lblDatoEspecifico.setText("Distancia (Km):");
            }
        });

        // Agregar los campos al panel del formulario (en orden: etiqueta, campo)
        panelForm.add(new JLabel("Tipo de Envío:"));
        panelForm.add(cmbTipo);
        panelForm.add(new JLabel("Código:"));
        panelForm.add(txtCodigo);
        panelForm.add(new JLabel("Destino:"));
        panelForm.add(txtDestino);
        panelForm.add(new JLabel("Peso en Kg (0.1 - 50):"));
        panelForm.add(txtPeso);
        panelForm.add(lblDatoEspecifico);
        panelForm.add(txtDatoEspecifico);

        add(panelForm, BorderLayout.CENTER);

        // ── Botones de acción ────────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnGuardar  = new JButton("Guardar Envío");
        JButton btnCancelar = new JButton("Cancelar");

        // Al guardar, se ejecuta la validación y creación del envío
        btnGuardar.addActionListener(e  -> guardar());
        // Al cancelar, se cierra el diálogo sin crear ningún envío
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // ============================================================
    // Lógica de guardado con validación
    // ============================================================

    /**
     * Recolecta los datos del formulario, los valida y crea el envío.
     *
     * <p>Proceso de validación en capas:</p>
     * <ol>
     *   <li>Verifica que todos los campos estén completos (validación en la vista).</li>
     *   <li>Verifica que el código no esté ya registrado (consulta al gestor).</li>
     *   <li>Convierte los campos numéricos a {@code double} (detecta texto inválido).</li>
     *   <li>Delega la creación y validación de rangos a {@link EnvioFactory}.</li>
     * </ol>
     *
     * <p>Si todo es válido, el envío creado queda almacenado en {@link #envioCreado}
     * y el diálogo se cierra. Si hay un error, se muestra un mensaje y el diálogo
     * permanece abierto para que el usuario corrija los datos.</p>
     */
    private void guardar() {
        // Leer y limpiar los valores ingresados por el usuario
        String tipo       = (String) cmbTipo.getSelectedItem();
        String codigo     = txtCodigo.getText().trim();
        String destino    = txtDestino.getText().trim();
        String pesoStr    = txtPeso.getText().trim();
        String datoEspStr = txtDatoEspecifico.getText().trim();

        // Validación 1: ningún campo puede estar vacío
        if (codigo.isEmpty() || destino.isEmpty() || pesoStr.isEmpty() || datoEspStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Todos los campos son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación 2: el código no puede repetirse en el sistema
        if (gestor.existeCodigo(codigo)) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un envío registrado con el código: " + codigo,
                    "Código duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación 3: los campos numéricos deben ser números válidos
        // Reemplazamos coma por punto para aceptar formatos locales (ej: "3,5" → 3.5)
        double peso;
        double datoEspecifico;
        try {
            peso           = Double.parseDouble(pesoStr.replace(',', '.'));
            datoEspecifico = Double.parseDouble(datoEspStr.replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El peso y el valor específico deben ser números válidos (use punto para decimales).",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación 4 y creación: delegamos al patrón Factory, que valida rangos y crea el objeto
        try {
            // PATRÓN FACTORY: centraliza la creación y validación del envío
            envioCreado = EnvioFactory.crearEnvio(tipo, codigo, destino, peso, datoEspecifico);
            // Si la creación fue exitosa, cerramos el diálogo
            dispose();
        } catch (Exception ex) {
            // La Factory rechazó los datos (ej: peso fuera de rango, distancia inválida)
            JOptionPane.showMessageDialog(this,
                    "Error de validación: " + ex.getMessage(),
                    "Validación rechazada", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // Acceso al resultado del diálogo
    // ============================================================

    /**
     * Retorna el envío creado por este diálogo.
     *
     * <p>La ventana principal debe llamar a este método después de que el diálogo
     * se cierre para saber si el usuario guardó un nuevo envío o canceló.</p>
     *
     * @return el envío creado, o {@code null} si el usuario canceló
     */
    public Envio getEnvioCreado() {
        return envioCreado;
    }
}

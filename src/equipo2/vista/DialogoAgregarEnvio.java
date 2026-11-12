package equipo2.vista;

import equipo2.logica.GestorEnvios;
import equipo2.logica.patron.factory.EnvioFactory;
import equipo2.modelo.Envio;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para ingresar un nuevo envío (LOCAL o INTERURBANO) con validaciones completas.
 */
public class DialogoAgregarEnvio extends JDialog {

    private final JComboBox<String> cmbTipo;
    private final JTextField txtCodigo;
    private final JTextField txtDestino;
    private final JTextField txtPeso;
    private final JLabel lblDatoEspecifico;
    private final JTextField txtDatoEspecifico;

    private Envio envioCreado = null;
    private final GestorEnvios gestor;

    public DialogoAgregarEnvio(Frame parent, GestorEnvios gestor) {
        super(parent, "Agregar Nuevo Envío", true);
        this.gestor = gestor;

        setSize(420, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(5, 2, 8, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        cmbTipo = new JComboBox<>(new String[]{"LOCAL", "INTERURBANO"});
        txtCodigo = new JTextField();
        txtDestino = new JTextField();
        txtPeso = new JTextField();
        lblDatoEspecifico = new JLabel("Tarifa por Kg ($):");
        txtDatoEspecifico = new JTextField();

        cmbTipo.addActionListener(e -> {
            String tipo = (String) cmbTipo.getSelectedItem();
            if ("LOCAL".equals(tipo)) {
                lblDatoEspecifico.setText("Tarifa por Kg ($):");
            } else {
                lblDatoEspecifico.setText("Distancia (Km):");
            }
        });

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

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnGuardar = new JButton("Guardar Envío");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void guardar() {
        String tipo = (String) cmbTipo.getSelectedItem();
        String codigo = txtCodigo.getText().trim();
        String destino = txtDestino.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String datoEspStr = txtDatoEspecifico.getText().trim();

        if (codigo.isEmpty() || destino.isEmpty() || pesoStr.isEmpty() || datoEspStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gestor.existeCodigo(codigo)) {
            JOptionPane.showMessageDialog(this, "Ya existe un envío registrado con el código: " + codigo, "Código duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double peso;
        double datoEspecifico;
        try {
            peso = Double.parseDouble(pesoStr.replace(',', '.'));
            datoEspecifico = Double.parseDouble(datoEspStr.replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El peso y el valor específico deben ser números válidos (use punto para decimales).", "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Se usa el patrón Factory para validar y construir el objeto
            envioCreado = EnvioFactory.crearEnvio(tipo, codigo, destino, peso, datoEspecifico);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + ex.getMessage(), "Validación rechazada", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Envio getEnvioCreado() {
        return envioCreado;
    }
}

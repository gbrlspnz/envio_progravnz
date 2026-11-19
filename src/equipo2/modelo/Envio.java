package equipo2.modelo;

/**
 * Clase abstracta que representa un envío genérico en el sistema.
 *
 * <p>Esta clase es la raíz de la jerarquía de envíos y aplica el principio de
 * <b>herencia y polimorfismo</b>: las subclases {@link EnvioLocal} y
 * {@link EnvioInterurbano} especializan el comportamiento de {@link #calcularImporte()},
 * que es el método abstracto que cada tipo de envío debe implementar de forma propia.</p>
 *
 * <p>Atributos comunes a todo envío:</p>
 * <ul>
 *   <li>{@code codigo}  – identificador único del envío</li>
 *   <li>{@code destino} – lugar al que se dirige el paquete</li>
 *   <li>{@code pesoKg}  – peso del paquete en kilogramos</li>
 * </ul>
 *
 * <p>Arquitectura: capa <b>Modelo</b> del patrón MVC.</p>
 */
public abstract class Envio {

    // ============================================================
    // Atributos (encapsulados con modificador private)
    // ============================================================

    /** Código único que identifica al envío (ej: "ENV-001"). */
    private String codigo;

    /** Ciudad o localidad de destino del paquete. */
    private String destino;

    /** Peso del paquete en kilogramos (debe estar entre 0.1 y 50 kg). */
    private double pesoKg;

    // ============================================================
    // Constructor
    // ============================================================

    /**
     * Construye un envío con los datos básicos comunes a todos los tipos.
     *
     * @param codigo   código identificador único del envío
     * @param destino  destino al que se enviará el paquete
     * @param pesoKg   peso del paquete en kilogramos
     */
    public Envio(String codigo, String destino, double pesoKg) {
        this.codigo  = codigo;
        this.destino = destino;
        this.pesoKg  = pesoKg;
    }

    // ============================================================
    // Getters y Setters
    // ============================================================

    /** @return el código único del envío */
    public String getCodigo() { return codigo; }

    /** @param codigo nuevo código para el envío */
    public void setCodigo(String codigo) { this.codigo = codigo; }

    /** @return el destino del envío */
    public String getDestino() { return destino; }

    /** @param destino nuevo destino del envío */
    public void setDestino(String destino) { this.destino = destino; }

    /** @return el peso del paquete en kilogramos */
    public double getPesoKg() { return pesoKg; }

    /** @param pesoKg nuevo peso en kilogramos */
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }

    // ============================================================
    // Método abstracto (POLIMORFISMO)
    // ============================================================

    /**
     * Calcula el importe base del envío según el tipo específico.
     *
     * <p>Este método es <b>abstracto</b> y obliga a cada subclase a definir
     * su propia fórmula de cálculo. Gracias al polimorfismo, el sistema puede
     * tratar cualquier envío de forma uniforme sin conocer su tipo concreto.</p>
     *
     * @return el importe base del envío en pesos argentinos
     */
    public abstract double calcularImporte();

    // ============================================================
    // Punto de entrada principal de la aplicación
    // ============================================================

    /**
     * Método {@code main} que delega el arranque a la clase principal de la vista.
     * Permite ejecutar la aplicación directamente desde esta clase.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        // Delegamos el inicio a la clase EnvioApp (capa Vista del MVC)
        equipo2.vista.EnvioApp.main(args);
    }
}

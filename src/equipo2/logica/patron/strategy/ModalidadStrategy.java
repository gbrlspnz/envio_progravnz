package equipo2.logica.patron.strategy;

// Patron Strategy: interfaz para calcular descuentos y totales segun la modalidad
public interface ModalidadStrategy {
    String getNombre();
    double calcularDescuento(double importeBase);
    double calcularTotal(double importeBase);
}

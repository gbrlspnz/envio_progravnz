package equipo2.logica.patron.strategy;

// Estrategia Normal: no aplica descuento (0%)
public class ModalidadNormalStrategy implements ModalidadStrategy {

    @Override
    public String getNombre() {
        return "NORMAL";
    }

    @Override
    public double calcularDescuento(double importeBase) {
        return 0.0;
    }

    @Override
    public double calcularTotal(double importeBase) {
        return importeBase;
    }
}

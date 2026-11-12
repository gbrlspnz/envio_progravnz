package equipo2.logica.patron.strategy;

/**
 * Estrategia de cálculo para Modalidad BENEFICIO:
 * Aplica un 10% de descuento sobre el importe base.
 * total = importe base - descuento.
 */
public class ModalidadBeneficioStrategy implements ModalidadStrategy {

    @Override
    public String getNombre() {
        return "BENEFICIO";
    }

    @Override
    public double calcularDescuento(double importeBase) {
        return importeBase * 0.10;
    }

    @Override
    public double calcularTotal(double importeBase) {
        return importeBase - calcularDescuento(importeBase);
    }
}

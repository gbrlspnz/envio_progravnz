package equipo2.modelo;

// Representa un envio dentro de la misma ciudad
public class EnvioLocal extends Envio {

    private double tarifaPorKg;

    public EnvioLocal(String codigo, String destino, double pesoKg, double tarifaPorKg) {
        super(codigo, destino, pesoKg);
        this.tarifaPorKg = tarifaPorKg;
    }

    public double getTarifaPorKg() {
        return tarifaPorKg;
    }

    public void setTarifaPorKg(double tarifaPorKg) {
        this.tarifaPorKg = tarifaPorKg;
    }

    // Regla del enunciado: base fija de $2000 + (peso * tarifa por kg)
    @Override
    public double calcularImporte() {
        return 2000.0 + (getPesoKg() * this.tarifaPorKg);
    }
}

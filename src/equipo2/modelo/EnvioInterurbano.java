package equipo2.modelo;

// Representa un envio entre distintas ciudades
public class EnvioInterurbano extends Envio {

    private double distanciaKm;

    public EnvioInterurbano(String codigo, String destino, double pesoKg, double distanciaKm) {
        super(codigo, destino, pesoKg);
        this.distanciaKm = distanciaKm;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    // Regla del enunciado: base de $5000 + (peso * 1500) + (distancia * 20)
    @Override
    public double calcularImporte() {
        return 5000.0 + (getPesoKg() * 1500.0) + (this.distanciaKm * 20.0);
    }
}

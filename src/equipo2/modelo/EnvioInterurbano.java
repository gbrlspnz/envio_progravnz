package equipo2.modelo;

/**
 * Clase hija inicial. Tipo en datos.txt: INTERURBANO.
 * El cálculo está pendiente y debe implementarlo el equipo.
 */
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

    /*
     * TODO: implementar calcularImporte() con retorno double.
     * Aplicar la regla de EnvioInterurbano descrita en ENUNCIADO.txt.
     * Sobrescribir el contrato que el equipo agregue a la clase padre.
     * Este comentario no implementa el método.
     */
    @Override
    public double calcularImporte() {
        return 5000.0 + (getPesoKg() * 1500.0) + (this.distanciaKm * 20.0);
    }
}

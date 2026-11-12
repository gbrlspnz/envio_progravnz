package equipo2.modelo;

/**
 * Clase hija inicial. Tipo en datos.txt: LOCAL.
 * El cálculo está pendiente y debe implementarlo el equipo.
 */
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

    /*
     * TODO: implementar calcularImporte() con retorno double.
     * Aplicar la regla de EnvioLocal descrita en ENUNCIADO.txt.
     * Sobrescribir el contrato que el equipo agregue a la clase padre.
     * Este comentario no implementa el método.
     */
    @Override
    public double calcularImporte() {
        return 2000.0 + (getPesoKg() * this.tarifaPorKg);
    }
}

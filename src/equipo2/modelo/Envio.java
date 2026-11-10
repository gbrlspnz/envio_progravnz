package equipo2.modelo;

/**
 * Base entregada para el equipo 2: Cotización de envíos.
 * Contiene datos y accesores; los comportamientos deben completarse.
 * Los constructores y setters iniciales no validan: el equipo debe incorporar
 * las validaciones del enunciado en una capa o clase adecuada.
 */
public abstract class Envio {
    private String codigo;
    private String destino;
    private double pesoKg;

    public Envio(String codigo, String destino, double pesoKg) {
        this.codigo = codigo;
        this.destino = destino;
        this.pesoKg = pesoKg;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    /*
     * TODO: incorporar al contrato común el método:
     * double calcularImporte()
     * Cada clase hija debe resolverlo según sus reglas.
     * Utilizar referencias y una colección del tipo Envio.
     * La selección NORMAL/BENEFICIO se aplica al importe base calculado.
     * No se entrega implementación de cálculo ni uso polimórfico.
     */
}

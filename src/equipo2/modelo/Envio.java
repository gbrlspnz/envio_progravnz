package equipo2.modelo;

// Clase base abstracta para todos los envios
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

    // Metodo abstracto: cada tipo de envio calcula su importe con su propia formula
    public abstract double calcularImporte();

    public static void main(String[] args) {
        equipo2.vista.EnvioApp.main(args);
    }
}

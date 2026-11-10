public class Paciente {

    private String id;
    private String nombre;
    private int edad;
    private String prevision;
    private String diagnostico;

    public paciente(String id, String nombre, int edad, String prevision, String diagnostico) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.prevision = prevision;
        this.diagnostico = diagnostico;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getEdad() { return edad; }
    public String getPrevision() { return prevision; }
    public String getDiagnostico() { return diagnostico; }

}

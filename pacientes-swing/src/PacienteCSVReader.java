import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacienteCSVReader {

    public List<Paciente> leer(String rutaArchivo) throws IOException {
        List<Paciente> pacientes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            br.readLine();

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");

                if (datos.length == 5) {
                    String id = datos[0].trim();
                    String nombre = datos[1].trim();

                    int edad = Integer.parseInt(datos[2].trim());
                    String prevision = datos[3].trim();
                    String diagnostico = datos[4].trim();
                    pacientes.add(new Paciente(id, nombre, edad, prevision, diagnostico));
                }
            }
        }
        return pacientes;
    }
}

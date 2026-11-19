package equipo2.logica;

import equipo2.modelo.Envio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Administra la lista de envios en memoria
public class GestorEnvios {

    // Coleccion de envios (contiene tanto EnvioLocal como EnvioInterurbano)
    private final List<Envio> envios = new ArrayList<>();

    public List<Envio> getEnvios() {
        return Collections.unmodifiableList(envios);
    }

    public List<Envio> getEnviosModificables() {
        return envios;
    }

    public void setEnvios(List<Envio> nuevosEnvios) {
        this.envios.clear();
        if (nuevosEnvios != null) {
            this.envios.addAll(nuevosEnvios);
        }
    }

    // Verifica si un codigo ya existe (sin distinguir mayusculas ni espacios)
    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String normalizado = codigo.trim();
        for (Envio e : envios) {
            if (e.getCodigo().trim().equalsIgnoreCase(normalizado)) {
                return true;
            }
        }
        return false;
    }

    // Agrega un nuevo envio al listado
    public void agregarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envio no puede ser nulo.");
        }
        if (existeCodigo(envio.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un envio con el codigo: " + envio.getCodigo());
        }
        this.envios.add(envio);
    }

    // Elimina un envio por su indice
    public Envio eliminarEnvio(int indice) {
        if (indice < 0 || indice >= envios.size()) {
            throw new IndexOutOfBoundsException("Indice invalido: " + indice);
        }
        return this.envios.remove(indice);
    }

    public int getCantidad() {
        return envios.size();
    }

    public void limpiar() {
        this.envios.clear();
    }
}

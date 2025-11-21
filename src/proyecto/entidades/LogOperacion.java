package proyecto.entidades;

public class LogOperacion {

    private String id_operacion;
    private String timestamp;
    private String tipo;
    private String nodo_emisor;  // "sede1" o "sede2" - Quién realizó la operación
    private String payload;

    public LogOperacion () {

    }

    public LogOperacion(String id_operacion, String timestamp, String tipo, String nodo_emisor, String payload) {
        this.id_operacion = id_operacion;
        this.timestamp = timestamp;
        this.tipo = tipo;
        this.nodo_emisor = nodo_emisor;
        this.payload = payload;
    }

    public String getId_operacion() {
        return id_operacion;
    }

    public void setId_operacion(String id_operacion) {
        this.id_operacion = id_operacion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNodo_emisor() {
        return nodo_emisor;
    }

    public void setNodo_emisor(String nodo_emisor) {
        this.nodo_emisor = nodo_emisor;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
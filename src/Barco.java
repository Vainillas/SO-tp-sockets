import java.io.*;
import java.net.Socket;

public class Barco {
    private BarcoSentido sentido;
    private int numero;
    private static final String HOST = "localhost";
    private static final int PORT = 3457;
    private Socket socket;

    public Barco(BarcoSentido sentido, int numero) {
        this.sentido = sentido;
        this.numero = numero;
    }

    public Barco(BarcoSentido sentido, Socket socket, int numero) {
        this(sentido, numero);
        this.socket = socket;
    }

    public void cruzarCanal() {
        // decirle a la exclusaServer que quiero cruzar el canal
        conexionPorSocket();
    }

    public void conexionPorSocket() {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Barco #" + numero + ": Conectado al servidor de exclusas.");

            solicitarPaseAlCanal(socket);
            esperarRespuestaDePaseAlCanal(socket);
            solicitarPaseAlOceano(socket);
            esperarRespuestaDePaseAlOceano(socket);

        } catch (IOException | NullPointerException e) {
            System.out.println("El barco número " + numero + " se hundió.");
        }
    }
    private void solicitarPaseAlCanal(Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        // Enviar solicitud para cruzar la esclusa indicando la dirección del barco
        writer.println("SOLICITUD_CRUCE_DESDE_DIRECCION_BARCO:" + numero + ":" + sentido + ":CANAL");
        System.out.println("Barco #" + numero + ": Intentando cruzar");
    }

    private void solicitarPaseAlOceano(Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        // Enviar solicitud para cruzar la esclusa indicando la dirección del barco
        writer.println("SOLICITUD_CRUCE_DESDE_DIRECCION_BARCO:" + numero + ":" + sentido + ":OCEANO");
        System.out.println("Barco #" + numero + ": Esperando permiso para cruzar al océano...");
    }
    private void esperarRespuestaDePaseAlCanal(Socket socket) throws IOException {
        esperarRespuesta(socket, "CANAL");
    }
    private void esperarRespuestaDePaseAlOceano(Socket socket) throws IOException {
        esperarRespuesta(socket, "OCEANO");
    }



    private void esperarRespuesta(Socket socket, String destino) throws IOException {
        // Esperar la respuesta de la esclusa para navegar
        String response = "";
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // Cambios para manejar la excepción de conexión reseteada
        try {
            response = reader.readLine();
        } catch (IOException e) { //Se debe informar al servidor que no se quede esperando el barco
            System.out.println("Barco #" + numero + ": La conexión se ha cerrado inesperadamente.");
            throw e;
        }
        // Verificar si la respuesta es null antes de comparar
        if (response != null && response.equals("NAVEGACION_EXITOSA_"+destino)) {
            System.out.println("Barco #" + numero + ": Permiso concedido. El barco ha navegado con éxito al " + destino + ".");
        } else {
            System.out.println("Barco #" + numero + ": No se concedió el permiso para navegar.");
        }
    }

    public boolean esteAOeste() {
        return this.sentido.equals(BarcoSentido.EO);
    }

    public boolean oesteAEste() {
        return this.sentido.equals(BarcoSentido.OE);
    }

    public int getNumero() {
        return this.numero;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "Barco " + this.numero + " con sentido " + this.sentido.descripcion;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Barco barco)) {
            return false;
        }
        return numero == barco.numero;
    }
}


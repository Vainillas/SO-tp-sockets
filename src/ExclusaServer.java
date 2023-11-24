import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ExclusaServer {
    private String host;
    private int port;
    private List<Barco> barcosEO = new ArrayList<>();
    private List<Barco> barcosOE = new ArrayList<>();
    private List<Barco> barcosCanal = new ArrayList<>();
    private Exclusa exclusaOeste;
    private Exclusa exclusaEste;

    public ExclusaServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.exclusaOeste = new Exclusa("Oeste");
        this.exclusaEste = new Exclusa("Este");


    }

    public void iniciarServidor() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Servidor de exclusas funcionando");
            while (true) {
                Socket socket = server.accept();
                manejarSolicitudes(socket);
            }
        } catch (IOException e) {
            System.out.println("Error al recibir información. Se necesita reiniciar | Detalle: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor de exclusas: " + e.getMessage());
        }
    }

    public void manejarSolicitudes(Socket socket) throws IOException {
        try {
            // Leer la dirección del barco y la barrera desde el cliente
            String message = "";
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            message = reader.readLine();
            System.out.println("Mensaje recibido: " + message);
            identificarBarco(message, socket);
        } catch (IOException e) {
            System.out.println("Error al recibir información del barco: " + e.getMessage());
            throw e;
        }
    }

    private void identificarBarco(String message, Socket socket) {
        Barco barco;
        // Dividir el mensaje para obtener la dirección del barco y la barrera
        String[] parts = message.split(":");
        String idBarco = parts[1].trim();
        BarcoSentido sentido = BarcoSentido.valueOf(parts[2].trim());
        String destino = parts[3].trim();
        barco = new Barco(sentido, socket, Integer.parseInt(idBarco));
        if (destino.equals("CANAL")) { //Si quieren pasar al canal, los recibo y los manejo
            recibirBarcos(barco);
            cruzarCanal();
        } else if (destino.equals("OCEANO") && barcosCanal.contains(barco)) { //Si están en el canal, los manejo
            cruzarCanal();
        }
    }

    public void recibirBarcos(Barco barco) { //Añado los barcos provenientes del oceano a alguna cola de las exclusas
        if (barco.esteAOeste()) {
            this.barcosEO.add(barco);
        } else {
            this.barcosOE.add(barco);
        }
    }

    public void cruzarCanal() {
        if (!barcosCanal.isEmpty()) {//Si hay barcos en el canal, los manejo
            manejarBarcosCanal();
        }
        if (!barcosEO.isEmpty()) {//Si hay barcos este-oeste los manejo primero
            manejarBarcosEO();
        }
        if (!barcosOE.isEmpty()) {//Si hay barcos oeste-este los manejo después
            manejarBarcosOE();
        }
    }

    private void manejarBarcosOE() {
        if (barcosOE.size() >= 2) {//Si hay dos barcos listos para pasar, veo si los puedo hacer pasar
            manejarBarcos(barcosOE, exclusaOeste, exclusaEste);
        }
    }

    private void manejarBarcosEO() {
        if (barcosEO.size() >= 2) {//Si hay dos barcos listos para pasar, veo si los puedo hacer pasar
            manejarBarcos(barcosEO, exclusaEste, exclusaOeste);
        }
    }

    private void manejarBarcos(List<Barco> barcos, Exclusa exclusaActual, Exclusa exclusaContraria) {
        if (!barcosCanal.isEmpty()) {
            //Si no está vacío, solo hay un barco en el canal. Lo que significa que el programa se rompió.
            System.out.println("Error: El canal no está vacío. El programa se rompió.");
        } else if (barcos.size() >= 2) { //Si el canal está vacío y hay dos barcos en la exclusa listos
            exclusaContraria.cerrar(); // Cerramos la exclusa contraria antes de abrir la actual
            exclusaActual.abrir(); //Abro la actual
            for (int i = 0; i < 2; i++) { //Hago pasar a los dos barcos
                Barco barco = barcos.get(i);
                notificarPaseAlCanal(barco);
                barcosCanal.add(barco);
                System.out.println("El " + barco.toString() + " está entrando en el canal.");
            }
            barcos.clear(); //Vacío la lista de barcos
            exclusaActual.cerrar(); //Cierro la exclusa
        }
    }

    private void manejarBarcosCanal() {
        if (barcosCanal.size() == 2) {
            //Si hay dos barcos, los hago pasar y dejo el canal vacío. Si hay uno, lo dejo en el canal
            int cantBarcos = 2;
            for (Barco barco : barcosCanal) {
                Exclusa exclusaActual = barco.esteAOeste() ? exclusaOeste : exclusaEste;
                Exclusa exclusaContraria = barco.esteAOeste() ? exclusaEste : exclusaOeste;
                exclusaActual.cerrar();
                manejarPaseCanal(barco, exclusaContraria, --cantBarcos == 0); //No se puede usar barcosCanal.size() porque se modifica en el for y rompe
            }
            barcosCanal.clear();
        }
    }

    private void manejarPaseCanal(Barco barco, Exclusa exclusaContraria, boolean ultimo) {
        if (!exclusaContraria.isAbierta()) {
            exclusaContraria.abrir();
        }
        notificarPaseAlOceano(barco);
        System.out.println("El " + barco.toString() + " ha cruzado el canal");
        if (ultimo)
            exclusaContraria.cerrar();
    }
    private void notificarPaseAlCanal(Barco barco) {
        notificarPase(barco, "CANAL");
    }
    private void notificarPaseAlOceano(Barco barco) {
        notificarPase(barco, "OCEANO");
    }
    private void notificarPase(Barco barco, String destino) {
        try {
            OutputStream output = barco.getSocket().getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("NAVEGACION_EXITOSA_"+destino);
        } catch (IOException e) {
            System.out.println("Error al notificar el pase al canal: " + e.getMessage());
        }
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

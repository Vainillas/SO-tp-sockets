public class MainInitBarquitos {
    private static final String HOST = "localhost";
    private static final int PORT = 3457;
    public static void main(String[] args) {
        int cantidadBarquitos = 8;
        for (int i = 0; i < cantidadBarquitos; i++) {
            BarcoSentido sentido = (i % 2 == 1) ? BarcoSentido.OE : BarcoSentido.EO;
            int id = i;
            new Thread(() -> new Barco(sentido, id).cruzarCanal()).start();
        }
    }

}
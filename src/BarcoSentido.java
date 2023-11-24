public enum BarcoSentido {
    EO("Este-Oeste"), OE("Oeste-Este");

    public final String descripcion;
    private BarcoSentido(String descripcion){
        this.descripcion = descripcion;
    }

}

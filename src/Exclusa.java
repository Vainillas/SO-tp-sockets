public class Exclusa {

    private boolean abierta;
    private String identificador;

    public Exclusa(String identificador){
        this.identificador = identificador;
        this.abierta = false;
    }
    public void alternar(){
        this.abierta = !this.abierta;
    }
    public void abrir(){
        this.abierta = true;
    }
    public void cerrar(){
        this.abierta = false;
    }
    public boolean isAbierta() {
        return this.abierta;
    }
}

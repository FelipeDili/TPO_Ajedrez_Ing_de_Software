package ajedrez.core.partida;

/** Razones por las que una partida termina empatada. */
public enum MotivoTablas {
    AHOGADO("rey ahogado"),
    REPETICION("triple repetición de la posición"),
    REGLA_DE_LOS_50_MOVIMIENTOS("regla de los 50 movimientos");

    private final String descripcion;

    MotivoTablas(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}

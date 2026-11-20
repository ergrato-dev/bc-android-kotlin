# Ya está hecho: el punto de entrada. Sin esta regla, R8 no ve a nadie que use el programa y lo quita entero.
-keep class dev.bitacora.keep.MainKt {
    public static void main(java.lang.String[]);
}

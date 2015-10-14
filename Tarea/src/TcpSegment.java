public class TcpSegment implements Segment {

	private byte[] bytes;
	private int numP;
	private int totP;
	private byte[] sumaB;
	private boolean warning;
	private int tam;
	
	public TcpSegment(byte [] b, int np, int tp, byte[]sb, boolean w, int t){
	    bytes = b;
	    numP = np;
	    totP = tp;
	    sumaB = sb;
	    warning = w;
	    tam= t;
	}
	/**
	 * Método que se encarga de regresar el payload del segmento
	 * es decir, el arreglo de bytes de datos.
	 * @return <byte[]>
	 */
	public byte[] getPayload(){
	    return bytes;
	}
	/**
	 * Método que se encarga de modificar el payload del segmento
	 * @param newPayload <byte[]> nuevo payload del segmento
	 */
	public void setPayload(byte[] newPayload){
	    bytes = newPayload;

	}
	/**
	 * Método que no dice si el segmento se produjo por un error 
	 * o no.
	 * @return <boolean> 
	 */
	public boolean isWarningSegment(){
	    return warning;
	}
	
	/**
	 * Método que regresa la suma en complemento A1.
	 * @return <byte[]>
	 */
	public byte[] getChecksum(){
	    return sumaB;
	}

	/**
	 * Método que modifica el checksum de un segmento
	 * @param checksum <byte[]> nuevo checksum del segmento
	 */
	public void setChecksum(byte[] newChecksum){
	    sumaB = newChecksum;
	}
	
	/**
	 * Método que regresa qué número de segmento es, de todo 
	 * el conjunto de segmentos de datos.
	 * @return <int>
	 */
	public int getNumberOfSegment(){
	    return numP;
	}

	/**
	 * Método que regresa cuantos segmentos en total se mandaron
	 * del cliente al servidor.
	 * @return <int>
	 */
	public int getNumberTotal(){
	    return totP;

	}
	
	/**
	 * Método que regresa el tamanio del segmento.
	 * @return <int>
	 */	
	public int getTam(){
	    return tam;
	}
}

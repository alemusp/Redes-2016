/**
 * Interfaz que representa un Segmento de la capa de 
 * transporte en el modelo TCP/IP
 * @author Miguel Alonso Vilchis Domínguez
 * 			<mvilchis@ciencias.unam.mx>
 * @version 1.0 
 *
 */
public interface Segment {
	/**
	 * Método que se encarga de regresar el payload del segmento
	 * es decir, el arreglo de bytes de datos.
	 * @return <byte[]>
	 */
	public byte[] getPayload();
	/**
	 * Método que se encarga de modificar el payload del segmento
	 * @param newPayload <byte[]> nuevo payload del segmento
	 */
	public void setPayload(byte[] newPayload);
	/**
	 * Método que no dice si el segmento se produjo por un error 
	 * o no.
	 * @return <boolean> 
	 */
	public boolean isWarningSegment();
	
	/**
	 * Método que regresa la suma en complemento A1.
	 * @return <byte[]>
	 */
	public byte[] getChecksum();
	/**
	 * Método que modifica el checksum de un segmento
	 * @param checksum <byte[]> nuevo checksum del segmento
	 */
	public void setChecksum(byte[] newChecksum);
	
	/**
	 * Método que regresa qué número de segmento es, de todo 
	 * el conjunto de segmentos de datos.
	 * @return <int>
	 */
	public int getNumberOfSegment();
	
	/**
	 * Método que regresa cuantos segmentos en total se mandaron
	 * del cliente al servidor.
	 * @return <int>
	 */
	public int getNumberTotal();
}

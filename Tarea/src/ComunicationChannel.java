import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Clase que se encarga de simular un canal de comunicaci��n al 
 * hacer un traslado de Segmentos, de un buffer de entrada a un 
 * buffer de salida
 * 
 * @author Vilchis Dom��nguez Miguel Alonso
 * 			<mvilchis@ciencias.unam.mx>
 * 
 * @version 1.0
 *
 */
public class ComunicationChannel{
	private SendChannel sChannel; //Canal para envio de segmenetos con datos
	private WarningChannel wChannel; //Canal para envio de segmentos de avisos
	private ConcurrentLinkedQueue<Segment> warningsIn;
	private ConcurrentLinkedQueue<Segment> bufferIn;
	/**
	 * Constructor de la clase, se encarga de inicializar los threads necesarios 
	 * y ponerlos en ejecuci��n 
	 * @param failureProbability <int> n��mero del 1 al 10 que representa la probabilidad
	 * 						    con la que fallar�� el canal, failureProbability/20
	 * 
	 * @param bufferIn <ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los segmentos
	 * 					que van del cliente al canal de	comunicacion
	 *  
	 * @param bufferOut<ConcurrentLinkedQueue <Segment>> buffer que almacenar�� los segmentos 
	 * 					que van del canal al servidor
	 *  
	 * @param warningsIn<ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los avisos del 
	 * 					servidor al cliente, debido a fallo en paquetes
	 * 
	 * @param warningsOut<ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los avisos 
	 * 					 del canal de comunicaci��n al cliente.
	 */
	public ComunicationChannel(int failureProbability, ConcurrentLinkedQueue<Segment> bufferIn,
			ConcurrentLinkedQueue<Segment> bufferOut, ConcurrentLinkedQueue<Segment> warningsIn,
			ConcurrentLinkedQueue<Segment> warningsOut){
		this.bufferIn = bufferIn;
		this.warningsIn = warningsIn;
		sChannel = new SendChannel(failureProbability, bufferIn, bufferOut);
		sChannel.start();
		wChannel = new WarningChannel(warningsIn, warningsOut);
		wChannel.start();	
			
	}
	public void sendWarning(Segment warning){
		warningsIn.add(warning);
	}
	public void sendDataSegment(Segment segment){
		bufferIn.add(segment);
	}
	
}
/**
 * Clase que representa el canal de avisos, estos avisos se producen 
 * cuando el servidor nota que hay algo mal en el segmento que le fue enviado
 * 
 * @author Miguel Alonso Vilchis Dom��nguez
 * 			<mvilchis@ciencias.unam.mx>
 * @version 1.0
 *
 */
class WarningChannel extends Thread{
	private ConcurrentLinkedQueue<Segment> warningsIn;
	private ConcurrentLinkedQueue<Segment> warningsOut;
	/**
	 * Constructor de la clase, toma como parametro los dos buffers con los que va a trabajar
	 * @param warningsIn <ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los avisos del 
	 * 					servidor al cliente, debido a fallo en paquetes
	 * @param warningsOut <ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los avisos 
	 * 					 del canal de comunicaci��n al cliente.
	 */
	public WarningChannel(ConcurrentLinkedQueue<Segment> warningsIn,
						 ConcurrentLinkedQueue<Segment> warningsOut) {
		super("WarningChannel");
		this.warningsIn = warningsIn;
		this.warningsOut = warningsOut;		
	}
	/**
	 * M��todo run. se encarga de ver si hay alg��n paquete en el buffer de entrada,
	 *  para colocarlo en el buffer de salida.
	 */
	
	public void run() {
		while(true){
		if(!warningsIn.isEmpty()) {
			warningsOut.add(warningsIn.poll());			
		}
		}
	}
}
/**
 * Clase que representa el canal de Segmentos de datos 
 * 
 * @author Miguel Alonso Vilchis Dom��nguez
 * 			<mvilchis@ciencias.unam.mx>
 * @version 1.0
 */
class SendChannel extends Thread{
	private ConcurrentLinkedQueue<Segment> bufferIn; //buffer de entrada de segmentos
	private ConcurrentLinkedQueue<Segment> bufferOut;  // buffer de salida de segmentos
	private Random random; 
	private int failureProbability; //n��mero que representa la probabilidad de fallo 
	public static final int TOTAL_PROBABILITY = 20; //Constante de segmentos totales
	/**
	 * Constructor de la clase toma como parametro los buffers con los que va a trabajar 
	 * y la probabilidad de fallo.
	 *  @param failureProbability<int> n��mero del 1 al 10 que representa la probabilidad
	 * 						    con la que fallar�� el canal, failureProbability/20
	 * 
	 * @param bufferIn <ConcurrentLinkedQueue<Segment>> buffer que almacenar�� los segmentos
	 * 					que van del cliente al canal de	comunicacion
	 *  
	 * @param bufferOut<ConcurrentLinkedQueue <Segment>> buffer que almacenar�� los segmentos 
	 * 					que van del canal al servidor
	 *  
	 */
	public SendChannel(int failureProbability, ConcurrentLinkedQueue<Segment> bufferIn,
			ConcurrentLinkedQueue<Segment> bufferOut){
		super("SendChannel");
		this.bufferIn = bufferIn;
		this.bufferOut = bufferOut;
		this.failureProbability = failureProbability;
		this.random = new Random();
		
	}
	
	private Segment changeSegment(Segment segmentTmp) {
		byte [] payload = segmentTmp.getPayload();
		int total = payload.length;
		int affectedByte = random.nextInt(total);
		int probability = random.nextInt(TOTAL_PROBABILITY);
//		probability = failureProbability-1;
	
		if  (probability < failureProbability) {
			byte tmp[] = new byte[1];
			random.nextBytes(tmp);
			payload[affectedByte] = tmp[0];
		}
	
		segmentTmp.setPayload(payload);
		return segmentTmp;
	}
	/**
	 * M��todo run, se encarga de ver si hay alg��n paquete en el buffer de entrada
	 * de haberlo, modifica un byte con probabilidad failureProbability/20 y 
	 * lo agrega al buffer de salida.
	 */
	public void run() {
		while(true){
			if(!bufferIn.isEmpty()) {
				bufferOut.add( changeSegment(bufferIn.poll()) );
			}
		}
	}	
}

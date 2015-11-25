import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

/*
 * Clase que recibe el paquete cifrado.
 * @author Jose Ricardo Rosas Bocanegra
 * @author Arturo Lemus Pablo
 * @version 2
 */
public class Receptor  extends Thread{
    //El canal de comunicación
    private ComunicationChannel canal;
    //La cola de salida
    private ConcurrentLinkedQueue<Segment> sal;
    //Llave publica para decifrar
    private PublicKey puk;
    //Lave privada para cifrar
    private PrivateKey prk;
    
    /*
     * Metodo constructor.
     * @param c ComunicationChannel El canal de comunicacion.
     * @param out ConcurrentLinkedQueue La cola de salida.
     */
    public Receptor(ComunicationChannel c, ConcurrentLinkedQueue out){
	canal = c;
	sal = out;
    }
    
    /*
     * Crea la suma de verificacion para el paquete.
     * @param arr byte[] El arreglo de bytes a verificar.
     * @return res La suma de verificacion del paquete.
     */
    private byte[] sumaB(byte[] arr){
	byte[] res = new byte[2];
	for (int i=0;i<arr.length;i+=2){
	    res[0]+=arr[i];
	    if(i+1<arr.length)
	    res[1]+=arr[i+1];
	}
	res[0]^=0xFF;
	res[1]^=0xFF;
	return res;
    }
    
    /*
     * Genera las llaves publica y privada para el cifrado RSA.
     */
    private void generaLlaves(){
	try{
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(2048);
	    KeyPair kp = kpg.genKeyPair();
	    puk = kp.getPublic();
	    prk = kp.getPrivate();
	}catch(Exception e){
	}
    }
    
    /*
     * Ejecuta las instrucciones del hilo.
     */
    public void run(){
	int ncomp=-1, tot=1;
	String nombre="";
	byte[][] bytes = new byte[1][245];
	boolean[] correctos= new boolean[0];
	int ultimo = 0;
	//crea las llaves pública y privada
	generaLlaves();
	byte [] llv = toByteArray(puk);
	//envía la llave
	TcpSegment msn = new TcpSegment(llv,-2,0,null,true,0);
	canal.sendWarning(msn);
	//entero para revisar que no pase demasiado tiempo sin recibir el siguiente mensaje
	int err=0;
	while (ncomp<tot){
	    //espera un tiempo antes de recisar si la cola está vacia
	    try{
		this.sleep(25);
	    }catch(Exception e){}
	    if(!sal.isEmpty()){
		msn = (TcpSegment)sal.poll();
		//si el mensaje tiene el número del segmento entonces es el nombre del archivo, con el numero de segmentos
		if(ncomp==-1){
		    int nmens = msn.getNumberTotal();
		    bytes = new byte[nmens][245];
		    correctos = new boolean[nmens];
		    for (int i=0;i<nmens;i++)
			correctos[i]=false;
		    tot=nmens;
		}
		byte [] mens = msn.getPayload();
		byte[] sum = msn.getChecksum();
		byte[] sum2 = sumaB(mens);
		int nseg = msn.getNumberOfSegment();
		//revisa que el paquete llegue en orden y que la checksum sea correcta
		if(nseg==ncomp && sum[0]==sum2[0]&&sum[1]==sum2[1]){
		    //si es el paquete -1 es el nombre del archivo
		    if(nseg ==-1){
			nombre = "copia_"+new String(decrypt(mens,prk));
		    }
		    else{
			bytes[nseg] = decrypt(mens,prk);
			//si es el último paquete correcto se avisa que se terminó de recibir
			if(nseg==tot-1){
			    ultimo=msn.getTam();
			    msn = new TcpSegment(null,-2,0,null,true,0);
			    canal.sendWarning(msn);
			}
			correctos[nseg]=true;
		    }

		    ncomp++;
		}
		//si el mensaje es incorrecto y no se ha recibido nunca de manera correcta se avisa
		else if(nseg !=-1 && !correctos[nseg]){
		    msn = new TcpSegment(null,ncomp,0,null,true,0);
		    canal.sendWarning(msn);
		}
		err=0;
	    }else if(ncomp!=-1)
		err++;
	    //si la cola es vacía durante mucho tiempo y aun faltan mensajes se avisa que no llegó el mensaje
	    if(err>=3&&!correctos[ncomp]){
		err=0;
		msn = new TcpSegment(null,ncomp,0,null,true,0);
		canal.sendWarning(msn);
	    }
	}
	System.out.println("acabé de recibir, el archivo que debo escribir es " +nombre);
	try{
	//se escribe el archivo
	    File f =new File(nombre);
	    BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(f));
	    byte[] ul = new byte[ultimo];
	    for(int i=0;i<ultimo;i++){
		ul[i]=bytes[tot-1][i];
	    }
	    for(int i=0;i<tot;i++){
		if(i==tot-1)
		    w.write(ul);
		else
		    w.write(bytes[i]);
	    }
	    w.close();
	    this.stop();
	}catch(Exception e){
	    e.printStackTrace();
	}

    }
    
    /*
     * Convierte un objeto en un arreglo de bytes.
     * @param obj Object El objeto a convertir.
     * @return byte[] El arreglo de bytes correspondiente al objeto.
     */
    private byte[] toByteArray(Object obj){
        byte[] bytes = null;
        try{
	    ByteArrayOutputStream bos = null;
	    ObjectOutputStream oos = null;
	    bos = new ByteArrayOutputStream();
	    oos = new ObjectOutputStream(bos);
	    oos.writeObject(obj);
	    oos.flush();
	    bytes = bos.toByteArray();
	    if (oos != null) {
		oos.close();
	    }
	    if (bos != null) {
		bos.close();
	    }
	}catch(Exception e){}
        return bytes;
    }
    
    /*
     * Metodo que decifra el arreglo de bytes que recibe.
     * @param text byte[] El arreglo de bytes a decifrar.
     * @param key PrivateKey La llave para decifrar con RSA.
     * @return byte[] El arreglo de bytes decifrado recibido. 
     */
    public static byte[] decrypt(byte[] text, PrivateKey key) {
	byte[] decryptedtext = null;
	try {
	    Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    decryptedtext = cipher.doFinal(text);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return decryptedtext;
    }	
}
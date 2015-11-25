import java.util.Scanner;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;


/*
 * Clase que emite el paquete cifrado.
 * @author Jose Ricardo Rosas Bocanegra
 * @author Arturo Lemus Pablo
 * @version 2
 */
public class Emisor  extends Thread{
    //Canal de comunicacion
    private ComunicationChannel canal;
    //Cola de warnings
    private ConcurrentLinkedQueue<Segment> warning;
    //Llave publica para decifrar
    private PublicKey puk;
    //La probabilidad de que el mensaje enviado se pierda
    private final double porcentaje =20.0;
    
    /*
     * Metodo constructor
     * @param c ComunicationChannel El canal de comunicacion.
     * @param err ConcurrentLinkedQueue<Segment>  La cola de warnings.
     */
    public Emisor(ComunicationChannel c, ConcurrentLinkedQueue<Segment> err){
	canal = c;
	warning =err;
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
     * Ejecuta las instrucciones del hilo.
     */
    public void run(){

	System.out.println("Escribe el nombre de un archivo:");
	Scanner sc = new Scanner(System.in);
	String nombre = sc.nextLine();
	byte[] tmp = nombre.getBytes();
	File f = new File(nombre);
	byte [] chk;
	TcpSegment msn;
	try{
	    BufferedInputStream l = new BufferedInputStream(new FileInputStream(f));
	    //se crea un arreglo un poco mas grande que el tamaño del archivo
	    byte[][] bytes = new byte[((int)(f.length()/245)*2)][245];
	    int leido, ultimo=0;
	    //entero que indicará cuantas lecturas se hacen
	    int i = 1;
	    leido=l.read(bytes[0]);
	    //se lee el archivo
	    while(leido!=-1){ 
		ultimo=leido;
		leido=l.read(bytes[i]);
		i++;
	    }
	    i=i-1;
	    l.close();
	    
	    //se espera a que el receptor mande su llave publica
	    while(warning.isEmpty());
	    msn = (TcpSegment)warning.poll();
	    puk=(PublicKey)toObject(msn.getPayload());
	    
	    int j=-1;
	    while (true){
		//si hay warnings se reenviarán todos los paquetes a partir de el
		if(!warning.isEmpty()){
		    msn = (TcpSegment)warning.poll();
		    j = msn.getNumberOfSegment();
		}
		//si aun hay paquetes por enviar
		if(j<i){
		    //el paquete -1 contendrá el nombre del archivo
		    if(j==-1){
			byte[] cifra = encrypt(tmp,puk);
			chk = sumaB(cifra);
			msn = new TcpSegment(cifra,j,i,chk,false, tmp.length);
		    }else if(j==-2){//cuando el receptor manda -2 significa que termina
			System.out.println("acabé de mandar paquetes");
			this.stop();
		    }
		    else{
			byte[] cifra = encrypt(bytes[j],puk);
			chk = sumaB(cifra);
			msn = new TcpSegment(cifra,j,i,chk,false, 245);
			System.out.println("Envié la parte "+(j+1));
		    }
		    //se crea un numero random y si es menor al porcentaje de fallo el mensaje se "pierde"
		    Random random = new Random();
		    int rd = random.nextInt()%100;
		    if(rd>porcentaje)
			canal.sendDataSegment(msn);
		    j++;
		    this.sleep(20);
		}
	    }
        }catch(FileNotFoundException e){
            e.printStackTrace();//traza de excepcion
           
        }catch(IOException e){
            e.printStackTrace();//traza de excepcion
        }catch(Exception e){
            e.printStackTrace();//traza de excepcion
        }
    }
    
    /*
     * Convierte un arreglo de bytes en un objeto.
     * @param bytes byte[] El arreglo de bytes a convertir.
     * @return Object El objeto convertido correspondiente al arreglo de bytes.
     */
    private Object toObject(byte[] bytes){
	Object obj = null;
	try{
	    ByteArrayInputStream bis = null;
	    ObjectInputStream ois = null;
	    bis = new ByteArrayInputStream(bytes);
	    ois = new ObjectInputStream(bis);
	    obj = ois.readObject();
	    if (bis != null) {
		bis.close();
	    }
	    if (ois != null) {
		ois.close();
	    }
	}catch(Exception e){
	
	}
        return obj;
    }
    
    /*
     * Metodo que cifra el arreglo de bytes para enviar.
     * @param text byte[] El arreglo de bytes a cifrar.
     * @param key PrivateKey La llave para cifrar con RSA.
     * @return byte[] El arreglo de bytes cifrado para enviar. 
     */
    public static byte[] encrypt(byte[] text,PublicKey key){
	byte[] ciphertext = null;
	try{
	    final Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    ciphertext = cipher.doFinal(text);
	}
	catch(Exception e){
	    e.printStackTrace();
	}	
	return ciphertext;
    }

}
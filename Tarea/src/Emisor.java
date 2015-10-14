import java.util.Scanner;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Emisor  extends Thread{
    private ComunicationChannel canal;
    private ConcurrentLinkedQueue<Segment> warning;
    public Emisor(ComunicationChannel c, ConcurrentLinkedQueue<Segment> err){
	canal = c;
	warning =err;
    }

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

    public void run(){

	    System.out.println("Escribe el nombre de un archivo:");
	    Scanner sc = new Scanner(System.in);
	    String nombre = sc.nextLine();
    	    byte[] tmp = nombre.getBytes();
	    File f =new File(nombre);
	    try{
		BufferedInputStream l = new BufferedInputStream(new FileInputStream(f));
		byte[][] bytes = new byte[1000][65536];
		int leido, ultimo=0;
		int i = 1;
		leido=l.read(bytes[0]);
		while(leido!=-1){ 
		    ultimo=leido;
		    leido=l.read(bytes[i]);
		    i++;
		}
		i=i-1;
		l.close();
		byte [] chk = sumaB(tmp);
		TcpSegment msn = new TcpSegment(tmp,-1,i,chk,false, tmp.length);
		canal.sendDataSegment(msn);
		for (int j=0;j<i;j++){
		    chk = sumaB(bytes[j]);
		    if(j==i-1)
			msn = new TcpSegment(bytes[j],j,i,chk,false,ultimo);
		    else
			msn = new TcpSegment(bytes[j],j,i,chk,false,65536);
		    canal.sendDataSegment(msn);
		    System.out.println("Envié "+(j+1)+" partes");
		    while(!warning.isEmpty()){
			msn = (TcpSegment)warning.poll();
			int nmens = msn.getNumberOfSegment();
			if(nmens!=-1){
			    chk = sumaB(bytes[nmens]);
			    if(nmens==i-1)
				msn = new TcpSegment(bytes[nmens],nmens,i,chk,false,ultimo);
			    else
				msn = new TcpSegment(bytes[nmens],nmens,i,chk,false, 65536);
			}
			else{
			    tmp = nombre.getBytes();
			    chk = sumaB(tmp);
			    msn = new TcpSegment(tmp,nmens,i,chk,false, tmp.length);
			}
			canal.sendDataSegment(msn);
		    }
		}
	    while (true){
		if(!warning.isEmpty()){
		    msn = (TcpSegment)warning.poll();
		    int nmens = msn.getNumberOfSegment();
		    if(nmens!=-1){
			chk = sumaB(bytes[nmens]);
			if(nmens==i-1)
			    msn = new TcpSegment(bytes[nmens],nmens,i,chk,false,ultimo);
			else
			    msn = new TcpSegment(bytes[nmens],nmens,i,chk,false, 65536);
		    }
		    else{
			tmp = nombre.getBytes();
			chk = sumaB(tmp);
			msn = new TcpSegment(tmp,nmens,i,chk,false, tmp.length);
		    }
		    canal.sendDataSegment(msn);
		}
	    }
        }catch(FileNotFoundException e){
            e.printStackTrace();//traza de excepcion
           
        }catch(IOException e){
            e.printStackTrace();//traza de excepcion
        }
   }

}
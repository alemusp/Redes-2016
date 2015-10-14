import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Receptor  extends Thread{
    private ComunicationChannel canal;
    private ConcurrentLinkedQueue<Segment> sal;
    
    public Receptor(ComunicationChannel c, ConcurrentLinkedQueue out){
	canal = c;
	sal = out;
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
	int ncomp=0, tot=1;
	String nombre="";
	byte[][] bytes = new byte[1][65536];
	int ultimo = 0;
	while (ncomp<=tot){
	    if(!sal.isEmpty()){
		TcpSegment msn = (TcpSegment)sal.poll();
		if(ncomp==0){
		    int nmens = msn.getNumberTotal();
		    bytes = new byte[nmens][65536];
		    tot=nmens;
		}
		byte [] mens = msn.getPayload();
		byte[] sum = msn.getChecksum();
		byte[] sum2 = sumaB(mens);
		if(sum[0]==sum2[0]&&sum[1]==sum2[1]){
		    int nseg = msn.getNumberOfSegment();
		    if(nseg ==-1){
			nombre = "copia_"+new String(mens);
		    }
		    else{
			bytes[nseg] = mens;
			if(nseg==tot-1)
			    ultimo=msn.getTam();
		    }
		    ncomp++;
		}
		else{
		    msn = new TcpSegment(null,msn.getNumberOfSegment(),0,null,true,0);
		    canal.sendWarning(msn);
		}
	    }
	}
	System.out.println("acabé de recibir, el archivo que debo escribir es " +nombre);
	try{
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
	}catch(Exception e){
	    e.printStackTrace();
	}

   }

}
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Clase que ejecuta el programa.
 * @author Jose Ricardo Rosas Bocanegra
 * @author Arturo Lemus Pablo
 * @version 2
 */
public class Main{
    /*
     * Metodo que prueba receptor y emisor.
     */
    public static void main(String [] args){
	ConcurrentLinkedQueue<Segment> in = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> out = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> inw = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> outw = new ConcurrentLinkedQueue<Segment>();
	ComunicationChannel cc = new ComunicationChannel(8, in, out, inw, outw);
	Emisor em = new Emisor(cc,outw);
	em.start();
	Receptor r = new Receptor(cc, out);
	r.start();
    }
}
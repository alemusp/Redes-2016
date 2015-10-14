import java.util.concurrent.ConcurrentLinkedQueue;

public class Main{
    public static void main(String [] args){
	ConcurrentLinkedQueue<Segment> in = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> out = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> inw = new ConcurrentLinkedQueue<Segment>();
	ConcurrentLinkedQueue<Segment> outw = new ConcurrentLinkedQueue<Segment>();
	ComunicationChannel cc = new ComunicationChannel(10, in, out, inw, outw);
	Emisor em = new Emisor(cc,outw);
	em.start();
	Receptor r = new Receptor(cc, out);
	r.start();
    }
}
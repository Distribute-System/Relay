

import java.util.concurrent.Semaphore;

@SuppressWarnings("serial")
public class Mutex extends Semaphore{
	public Mutex(int permits) {
		super(1);
		// TODO Auto-generated constructor stub
	}
}
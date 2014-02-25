package be.virtualsushi.tick5;

import org.junit.Test;

public class MultiInheritanceTest {

	public interface A {

		void doA();

	}

	public interface B extends A {

		void doB();

	}

	public class Service {

		private A aListener;
		private B bListener;

		public void addListener(A a) {
			System.out.println("added A");
			aListener = a;
		}

		public void addListener(B b) {
			System.out.println("added B");
			bListener = b;
		}

		public void service() {
			if (aListener != null) {
				aListener.doA();
			}
			if (bListener != null) {
				bListener.doB();
			}
		}

	}

	public class Worker implements A, B {

		@Override
		public void doB() {
			System.out.println("B");
		}

		@Override
		public void doA() {
			System.out.println("A");
		}

	}

	@Test
	public void test() {
		Service service = new Service();
		Worker worker = new Worker();
		service.addListener(worker);
		service.service();
	}

}

package com.suppresswarnings.osgi.alone;

import java.util.function.BiConsumer;

public interface State extends BiConsumer<String, Context<?>> {
	State Final = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			//final doesn't accept input
			//u.accept(t);
			System.out.println("[Final] don't accept input, ignored");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			return Final;
		}
		
		@Override
		public String name() {
			return "Final";
		}
	};
	public String name();
	public State to(String in, Context<?> context);
}

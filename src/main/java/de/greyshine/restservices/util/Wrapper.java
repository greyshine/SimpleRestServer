package de.greyshine.restservices.util;

public class Wrapper<T> {

	public T value;
	
	public Wrapper() {
		
	}
	public Wrapper(T v) {
		value=v;
	}
	
	@Override
	public String toString() {
		return String.valueOf( value );
	}
	
	
	
}

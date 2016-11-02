package de.greyshine.restservices;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultApplication extends Application {

	@Override
	public Collection<? extends Class<?>> getClasses() {
		return new ArrayList<>(0);
	}
}

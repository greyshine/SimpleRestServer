package de.greyshine.restservices.client;

import java.io.IOException;
import java.io.InputStream;

import de.greyshine.restservices.client.Client.BytesConsumer;
import de.greyshine.restservices.client.Client.IStreamConsumer;

public class TextConsumer implements IStreamConsumer<String> {

	public static final TextConsumer INSTANCE = new TextConsumer();
	
	@Override
	public String read(InputStream inIs) throws IOException {
		return inIs == null ? null : new String( new BytesConsumer().read( inIs ) );
	}

	public static TextConsumer getInstance() {
		return INSTANCE;
	}
}

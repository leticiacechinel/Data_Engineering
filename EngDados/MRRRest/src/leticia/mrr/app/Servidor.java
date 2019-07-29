package leticia.mrr.app;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Servidor {

	public static void main(String[] args) throws IOException {
		ResourceConfig config = new ResourceConfig().packages("leticia.mrr2");
		URI uri = URI.create("http://localhost/MRRWebApp/rest");
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri,config);
		
		server.start();
		System.out.println("Rodando");
		
		System.in.read();
	}
}

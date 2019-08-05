package leticia;

import org.glassfish.jersey.servlet.ServletContainer;

import leticia.infra.DAO;

public class MrrServlet extends ServletContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public MrrServlet() {
		DAO.setPersistenceUnit("MRR");
	}

}

package leticia.financeiro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import leticia.infra.DAO;
import leticia.mrr.ServicoMRR;

public class PagamentoTest {
	
	private static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	@BeforeClass
	public static void init() {
		DAO.setPersistenceUnit("MRR");
	}
	
	
	@Before
	public void zeraBaze () throws SQLException {
		
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
		conn.createStatement().execute("DROP SCHEMA PUBLIC CASCADE");
		conn.close();

	}

	@Test
	public void testePersistenciaPagamento() throws ParseException {

		ServicoMRR servico = new ServicoMRR();

		servico.pagamentoEfetuado(1, "10/01/2019", "Bronze/4", 400);
		servico.pagamentoEfetuado(2, "18/04/2019", "Platina/1", 399);

		DAO d = new DAO();
		d.begin();
		List<Pagamento> listaPagamentos = (List<Pagamento>) d.get(Pagamento.class);
	
		Date d1 = DF.parse("10/01/2019");
		Date d2 = DF.parse( "18/04/2019");
		Pagamento p1 = new Pagamento (1,d1);
		Pagamento p2 = new Pagamento (2,d2);

		Assert.assertEquals(2, listaPagamentos.size());
	    Assert.assertTrue(listaPagamentos.contains(p1));
	    Assert.assertTrue(listaPagamentos.contains(p2));
	
	    d.commit();
	    
	    
		servico.pagamentoEfetuado(1, "10/01/2019", "Bronze/4", 400);
		servico.pagamentoEfetuado(2, "18/04/2019", "Platina/1", 399);

		d = new DAO();
		d.begin();
		listaPagamentos = (List<Pagamento>) d.get(Pagamento.class);
	
	 

		Assert.assertEquals(2, listaPagamentos.size());
	    Assert.assertTrue(listaPagamentos.contains(p1));
	    Assert.assertTrue(listaPagamentos.contains(p2));
	
	    d.commit();

	}
	
	
}

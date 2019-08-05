package leticia.financeiro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import leticia.infra.DAO;
import leticia.mrr.Metrica;
import leticia.mrr.ServicoMRR;
import leticia.mrr.Tipo;

public class IntegracaoPagamentoTest {
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
	public void testaIntegracaoArray () throws ParseException {
		
		List<Pagamento> lista = new ArrayList<Pagamento>();
		lista.add(new Pagamento (1, "10/01/2019", "Bronze/4", 400));
		lista.add(new Pagamento (2, "12/01/2019", "Prata/3", 555));
		lista.add(new Pagamento (3, "11/01/2019", "Ouro/3", 750));
		lista.add(new Pagamento (5, "25/01/2019", "Ouro/1", 250));
		lista.add(new Pagamento (6, "09/04/2019", "Bronze/1", 100));
		lista.add(new Pagamento (2, "18/04/2019", "Platina/1", 399));
		lista.add(new Pagamento (3, "17/04/2019", "Prata/1", 185));
		lista.add(new Pagamento (5, "10/04/2019", "Platina/1", 399));
		lista.add(new Pagamento (5, "10/05/2019", "Platina/1", 399));
		lista.add(new Pagamento (4, "10/02/2019", "Prata/2", 370));

		ServicoMRR servico = new ServicoMRR();
		servico.uploadPagamentos(lista);
		
		DAO dao = new DAO();
		dao.begin();
		
		List<?> l = dao.get(Pagamento.class);
		
		Assert.assertEquals(10,l.size());
		
		
		Metrica[] metricas;
		Metrica metrica;

		// Cliente 1, Abril/2019
		metricas = servico.getMetricas(1, 201904);
		Assert.assertEquals(1, metricas.length);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		// Cliente 6, Abril/2019
		metricas = servico.getMetricas(6, 201904);
		Assert.assertEquals(2, metricas.length);

	
		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.New_MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		// Cliente 2, Abril/2019
		metricas = servico.getMetricas(2, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(399.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Expansion_MRR, metrica.getTipo());
		Assert.assertEquals(214.0, metrica.getValor(), 0.0001);

		// Cliente 3, Abril/2019
		metricas = servico.getMetricas(3, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(185.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Contraction_MRR, metrica.getTipo());
		Assert.assertEquals(65, metrica.getValor(), 0.0001);

		// Cliente 5, Abril/2019
		metricas = servico.getMetricas(5, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(399.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Resurrected_MRR, metrica.getTipo());
		Assert.assertEquals(399, metrica.getValor(), 0.0001);

		

		// Cliente 4, Abril/2019
		metricas = servico.getMetricas(4, 201904);
		Assert.assertEquals(1, metricas.length);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Cancelled_MRR, metrica.getTipo());
		Assert.assertEquals(185.0, metrica.getValor(), 0.0001);

	}
	
	
}
